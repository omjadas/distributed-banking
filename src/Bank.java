import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Bank implements Runnable {
    private final ServerSocket serverSocket;
    private final HashMap<String, RemoteBank> remoteAccounts = new HashMap<>();
    private final HashMap<String, Account> localAccounts = new HashMap<>();

    public Bank() throws IOException {
        serverSocket = new ServerSocket();
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

    @Override
    public void run() {
        Socket socket;
        try {
            while ((socket = serverSocket.accept()) != null) {
                RemoteBank remoteBank = new RemoteBank(socket, this);
                new Thread(remoteBank).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
