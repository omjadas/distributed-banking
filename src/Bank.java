import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Class of bank system.
 */
public class Bank implements Runnable {
    private final ServerSocket serverSocket;
    private final UUID bankId;
    private final HashMap<String, RemoteBank> remoteAccounts = new HashMap<>();
    private final HashMap<String, Account> localAccounts = new HashMap<>();
    private final HashMap<UUID, RemoteBank> remoteBanks = new HashMap<>();
    private final Set<Thread> remoteBankThreads = new HashSet<>();
    private final ChandyLamport chandyLamportAlgorithm;
    private final MAlgorithm mAlgorithm;

    /**
     * Initialise a bank.
     *
     * @param bankId ID of the bank
     * @param port   port to listen on
     * @throws IOException if unable to open socket
     */
    public Bank(UUID bankId, int port) throws IOException {
        this.bankId = bankId;
        serverSocket = new ServerSocket(port);
        chandyLamportAlgorithm = new ChandyLamport(this);
        mAlgorithm = new MAlgorithm(this);
    }

    /**
     * The method to initiate the Chandy-Lamport algorithm.
     *
     * <p>
     * To be treated similarly to other system messages such as deposit,
     * withdraw, etc.
     *
     * @throws IOException
     */
    public void startChandyLamport() throws IOException {
        Snapshot snapshot = takeSnapshot();
        if (chandyLamportAlgorithm
                .startAlgorithm(snapshot, remoteBanks.values())) {
            System.out.println("Completed snapshot process.");
        } else {
            System.out.println("Not connected to other banks.");
        }
    }

    /**
     * Method to handle all chandy lamport messages - usage can be found in
     * run() method of RemoteBank.
     *
     * @param remoteBankId  ID of the remote bank
     * @param markerMessage state of the remote bank
     * @param currentState  current local state
     */
    public void handleChandyLamportMarker(
            UUID remoteBankId,
            Snapshot markerMessage,
            Snapshot currentState) throws IOException {
        chandyLamportAlgorithm.handleReceivedMarker(
            remoteBankId,
            markerMessage,
            currentState);
    }

    /**
     * Method to erase the snapshot stored in local branch.
     */
    public void resetChandyLamport() {
        chandyLamportAlgorithm.eraseSnapshot();
    }

    /**
     * Make a connect request to another process.
     *
     * @param hostname host name of the other process
     * @param port     port of the other process
     * @throws IOException
     */
    public void connect(String hostname, int port) throws IOException {
        RemoteBank remoteBank = new RemoteBank(hostname, port, this);
        Thread remoteBankThread = new Thread(remoteBank);
        remoteBankThread.start();
        remoteBankThreads.add(remoteBankThread);
    }

    /**
     * Open a local account.
     *
     * @param accountId id of the account
     */
    public void open(String accountId) {
        localAccounts.put(accountId, new Account(accountId));
    }

    /**
     * Register a bank of another process.
     *
     * @param bankId id of the remote bank
     * @param bank   remote bank instance
     */
    public void registerBank(UUID bankId, RemoteBank bank) {
        remoteBanks.put(bankId, bank);
    }

    /**
     * Record the id of another account and the bank which owns it.
     *
     * @param accountId id of the account
     * @param bank      remote bank instance which owns the account
     */
    public void registerAccount(String accountId, RemoteBank bank) {
        remoteAccounts.put(accountId, bank);
    }

    /**
     * Deposit to an account.
     *
     * @param accountId id of the account to be deposited to
     * @param amount    amount to be deposited
     * @throws IOException
     * @throws UnknownAccountException
     */
    public synchronized void deposit(String accountId, int amount)
            throws IOException,
            UnknownAccountException {
        if (localAccounts.containsKey(accountId)) {
            localAccounts.get(accountId).deposit(amount);
        } else if (remoteAccounts.containsKey(accountId)) {
            remoteAccounts.get(accountId).deposit(accountId, amount);
        } else {
            throw new UnknownAccountException(
                String.format("Unknown account %s", accountId));
        }
    }

    /**
     * Withdraw from an account.
     *
     * @param accountId id of the account to be withdrawn from
     * @param amount    amount to be withdrawn
     * @throws IOException
     * @throws UnknownAccountException
     */
    public synchronized void withdraw(String accountId, int amount)
            throws IOException,
            UnknownAccountException {
        if (localAccounts.containsKey(accountId)) {
            localAccounts.get(accountId).withdraw(amount);
        } else if (remoteAccounts.containsKey(accountId)) {
            remoteAccounts.get(accountId).withdraw(accountId, amount);
        } else {
            throw new UnknownAccountException(
                String.format("Unknown account %s", accountId));
        }
    }

    /**
     * Transfer from one account to another.
     *
     * @param sourceId id of the source account
     * @param destId   id of the destination account
     * @param amount   amount to be transferred
     * @throws IOException
     * @throws UnknownAccountException
     */
    public synchronized void transfer(
            String sourceId,
            String destId,
            int amount) throws IOException,
            UnknownAccountException {
        withdraw(sourceId, amount);
        deposit(destId, amount);
    }

    /**
     * Print the balance of an account.
     *
     * @param accountId id of the account to be printed
     * @throws IOException
     */
    public void printBalance(String accountId) throws IOException {
        if (localAccounts.containsKey(accountId)) {
            System.out.println(
                String.format(
                    "$%d",
                    localAccounts.get(accountId).getBalance()));
        } else if (remoteAccounts.containsKey(accountId)) {
            remoteAccounts.get(accountId).printBalance(accountId);
        }
    }

    public int getBalance(String accountId) {
        return localAccounts.get(accountId).getBalance();
    }

    public Set<String> getAccountIds() {
        return localAccounts.keySet();
    }

    @Override
    public void run() {
        Socket socket;
        try {
            while ((socket = serverSocket.accept()) != null &&
                !Thread.interrupted()) {
                RemoteBank remoteBank;
                remoteBank = new RemoteBank(socket, this);
                Thread remoteBankThread = new Thread(remoteBank);
                remoteBankThread.start();
                remoteBankThreads.add(remoteBankThread);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        remoteBankThreads.forEach(remoteBankThread -> {
            remoteBankThread.interrupt();
        });
    }

    public UUID getBankId() {
        return bankId;
    }

    public HashMap<String, Account> getLocalAccounts() {
        return localAccounts;
    }

    public HashMap<UUID, RemoteBank> getRemoteBanks() {
        return remoteBanks;
    }

    /**
     * Make a clone of the local accounts and form a snapshot.
     *
     * @return a snapshot containing info of local accounts
     */
    public synchronized Snapshot takeSnapshot() {
        ArrayList<Account> clone = new ArrayList<>();
        for (Account account : localAccounts.values()) {
            clone.add(
                new Account(account.getAccountId(), account.getBalance()));
        }
        Snapshot snapshot = new Snapshot(getBankId(), clone);
        return snapshot;
    }

    /**
     * Broad future tick of a vector clock to all other processes.
     *
     * @param tick the tick of a vector clock
     */
    public synchronized void broadcastFutureTick(long tick) {
        this.remoteBanks.values().forEach(remoteBank -> {
            try {
                remoteBank.sendFutureTick(tick);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Broadcast a dummy message.
     */
    public synchronized void broadcastDummyMsg() {
        this.remoteBanks.values().forEach(remoteBank -> {
            try {
                remoteBank.sendDummyMsg();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Send a snapshot to the initiator.
     *
     * @param snapshot the snapshot instance to be sent
     * @throws IOException
     */
    public void sendSnapshotToInitiator(Snapshot snapshot) throws IOException {
        UUID initiatorId = mAlgorithm.getInitiatorInfo().getInitiatorId();
        remoteBanks.get(initiatorId).sendSnapshotToInitiator(snapshot);
    }

    /**
     * Forward a white message to the initiator.
     *
     * @param whiteMessage the white message instance
     * @throws IOException
     */
    public void sendWhiteMessageToInitiator(Message whiteMessage)
            throws IOException {
        UUID initiatorId = mAlgorithm.getInitiatorInfo().getInitiatorId();
        remoteBanks.get(initiatorId).sendWhiteMessageToInitiator(whiteMessage);
    }

    /**
     * Visualize the collected snapshots.
     *
     * @param snapshots global snapshots
     */
    public void printSnapshots(Set<Snapshot> snapshots) {
        System.out.println("Snapshots:");
        for (Snapshot snapshot : snapshots) {
            System.out.println("-------------------------------");
            System.out.println("process id: " + snapshot.getProcessId());
            for (Account account : snapshot.getAccounts()) {
                System.out.print("account id:" + account.getAccountId());
                System.out.println(", balance: " + account.getBalance());
            }
        }
    }

    /**
     * Visualize the message in transit (white messages).
     *
     * @param whiteMessages forwarded white messages to initiator
     */
    public void printWhiteMessages(Set<Message> whiteMessages) {
        System.out.println("Messages in transit:");
        for (Message message : whiteMessages) {
            System.out.println("------------------------------");
            System.out.println("source process: " + message.getSourceId());
            System.out.println("command: " + message.getCommand());
            System.out.println("amount: " + message.getAmount());
        }
    }

    public Set<Thread> getRemoteBankThreads() {
        return remoteBankThreads;
    }

    public MAlgorithm getmAlgorithm() {
        return mAlgorithm;
    }
}
