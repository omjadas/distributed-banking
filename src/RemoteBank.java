import java.net.Socket;

public class RemoteBank {
    private Socket socket = new Socket();
    private String hostname;
    private String[] accountIds;

    public RemoteBank() {
    }

    public String[] getAccountIds() {
        return accountIds;
    }

    public void deposit(String accountId, int amount) {
    }

    public void withdraw(String accountId, int amount) {
    }

    public int getBalance(String accountId) {
        return 0;
    }
}
