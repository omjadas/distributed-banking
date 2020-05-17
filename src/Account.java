/**
 * Bank account.
 */
public class Account {
    private final String accountId;
    private volatile int balance;

    /**
     * Create an account with an initial balance of 500.
     *
     * @param accountId ID of the account to create
     */
    public Account(String accountId) {
        this.accountId = accountId;
        this.balance = 500;
    }
    
    /**
     * Create an account with a specified balance.
     *
     * @param accountId ID of the account to create
     * @param balance balance of this account
     */
    public Account(String accountId, int balance) {
        this.accountId = accountId;
        this.balance = balance;
    }

    /**
     * Deposit money into the account.
     *
     * @param amount amount to deposit
     */
    public synchronized void deposit(int amount) {
        balance += amount;
    }

    /**
     * Withdraw money from the account.
     *
     * @param amount amount to withdraw
     */
    public synchronized void withdraw(int amount) {
        balance -= amount;
    }

    /**
     * Retrieve the ID of the account.
     *
     * @return the ID of the account
     */
    public String getAccountId() {
        return accountId;
    }

    /**
     * Retrieve the balance for the account.
     *
     * @return the balance of the account
     */
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
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {

            return false;
        }
        Account other = (Account) obj;
        if (accountId == null) {
            if (other.accountId != null) {
                return false;
            }
        } else if (!accountId.equals(other.accountId)) {
            return false;
        }
        if (balance != other.balance) {
            return false;
        }
        return true;
    }
}
