import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Bank implements Runnable {
    private final ServerSocket serverSocket;
    private final HashMap<String, RemoteBank> remoteAccounts = new HashMap<>();
    private final HashMap<String, Account> localAccounts = new HashMap<>();

    private final Set<Thread> remoteBankThreads = new HashSet<>();

    public Bank(int port) throws IOException {
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

    public void deposit(String accountId, int amount) throws IOException,
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

    public void withdraw(String accountId, int amount) throws IOException,
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

    public void transfer(String sourceId, String destId, int amount)
            throws IOException,
            UnknownAccountException {
        withdraw(sourceId, amount);
        deposit(destId, amount);
    }

    public int getBalance(String accountId) {
        return 0;
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
}
