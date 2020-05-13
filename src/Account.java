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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result +
            ((accountId == null) ? 0 : accountId.hashCode());
        result = prime * result + balance;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Account other = (Account) obj;
        if (accountId == null) {
            if (other.accountId != null)
                return false;
        } else if (!accountId.equals(other.accountId))
            return false;
        if (balance != other.balance)
            return false;
        return true;
    }
}
