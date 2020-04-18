public class Account {
    private String accountId;
    private int balance;

    public Account() {
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
