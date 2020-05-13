import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Bank implements Runnable {
    private final ServerSocket serverSocket;
    private final UUID bankId;
    private final HashMap<String, RemoteBank> remoteAccounts = new HashMap<>();
    private final HashMap<String, Account> localAccounts = new HashMap<>();
    private final HashMap<UUID, RemoteBank> remoteBanks = new HashMap<>();
    private final Set<Thread> remoteBankThreads = new HashSet<>();

    private MAlgorithm mAlgorithm;

    public Bank(UUID bankId, int port) throws IOException {
        this.bankId = bankId;
        serverSocket = new ServerSocket(port);
    }

    public void connect(String hostname, int port) throws IOException {
        RemoteBank remoteBank = new RemoteBank(hostname, port, this);
        Thread remoteBankThread = new Thread(remoteBank);
        remoteBankThread.start();
        remoteBankThreads.add(remoteBankThread);
    }

    public void open(String accountId) {
        localAccounts.put(accountId, new Account(accountId));
    }

    public void register(String accountId, RemoteBank bank) {
        remoteAccounts.put(accountId, bank);
    }

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

    public synchronized void transfer(
            String sourceId,
            String destId,
            int amount) throws IOException,
            UnknownAccountException {
        withdraw(sourceId, amount);
        deposit(destId, amount);
    }

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

    public synchronized Snapshot takeSnapshot() {
        Snapshot snapshot = new Snapshot(
            getBankId(),
            getLocalAccounts().values());
        return snapshot;
    }

    public synchronized void broadcastFutureTick(long tick) {
        this.remoteBanks.values().forEach(remoteBank -> {
            try {
                remoteBank.sendFutureTick(tick);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public synchronized void broadcastDummyMsg() {
        this.remoteBanks.values().forEach(remoteBank -> {
            try {
                remoteBank.sendDummyMsg();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public synchronized void broadcastTestMsg() {
        this.remoteBanks.values().forEach(remoteBank -> {
            try {
                remoteBank.sendTestMsg();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void sendSnapshotToInitiator(Snapshot snapshot) throws IOException {
        UUID initiatorId = mAlgorithm.getInitiatorInfo().getInitiatorId();
        remoteBanks.get(initiatorId).sendSnapshotToInitiator(snapshot);
    }

    public void sendWhiteMessageToInitiator(Message whiteMessage)
            throws IOException {
        UUID initiatorId = mAlgorithm.getInitiatorInfo().getInitiatorId();
        remoteBanks.get(initiatorId).sendWhiteMessageToInitiator(whiteMessage);
    }

    public Set<Thread> getRemoteBankThreads() {
        return remoteBankThreads;
    }

    public MAlgorithm getmAlgorithm() {
        return mAlgorithm;
    }

    public void setmAlgorithm(MAlgorithm mAlgorithm) {
        this.mAlgorithm = mAlgorithm;
    }
}
