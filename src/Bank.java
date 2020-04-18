import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;

public class Bank implements Runnable {
    private ServerSocket serverSocket;
    private HashMap<String, RemoteBank> remoteAccounts = new HashMap<>();
    private HashMap<String, Account> localAccounts = new HashMap<>();

    public Bank() throws IOException {
        serverSocket = new ServerSocket();
    }

    public void register(String[] accountIds, RemoteBank bank) {
    }

    public void deposit(String accountId, int amount) {
    }

    public void withdraw(String sourceId, String destId, int amount) {
    }

    public int getBalance(String accountId) {
        return 0;
    }

	@Override
	public void run() {
	}
}
