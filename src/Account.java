public class Account {
    private final String accountId;
    private int balance;

    public Account(String accountId) {
        this.accountId = accountId;
    }

    public void deposit(int amount) {
        balance += amount;
    }

    public void withdraw(int amount) {
        balance -= amount;
    }

    public String getAccountId() {
        return accountId;
    }

    public int getBalance() {
        return balance;
    }
}
