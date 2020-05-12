
public class Account {
    private final String accountId;
    private volatile int balance;

    public Account(String accountId) {
        this.accountId = accountId;
        this.balance = 500;
    }

    public synchronized void deposit(int amount) {
        balance += amount;
    }

    public synchronized void withdraw(int amount) {
        balance -= amount;
    }

    public String getAccountId() {
        return accountId;
    }

    public synchronized int getBalance() {
        return balance;
    }
}
