import java.util.Collection;
import java.util.UUID;

/**
 * Snapshot of a bank.
 */
public class Snapshot {
    private final UUID bankId;
    private final Collection<Account> accounts;

    /**
     * Create a snapshot for a bank.
     *
     * @param bankId ID of the bank the snapshot is for
     * @param accounts the accounts that the bank contains
     */
    public Snapshot(UUID bankId, Collection<Account> accounts) {
        this.bankId = bankId;
        this.accounts = accounts;
    }

    /**
     * Retrieve the bank ID.
     *
     * @return ID of the bank the snapshot is for
     */
    public UUID getBankId() {
        return bankId;
    }

    /**
     * Retrieve the accounts as they were when the snapshot was created.
     *
     * @return accounts as they were when the snapshot was created
     */
    public Collection<Account> getAccounts() {
        return accounts;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result +
            ((accounts == null) ? 0 : accounts.hashCode());
        result = prime * result +
            ((bankId == null) ? 0 : bankId.hashCode());
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
        Snapshot other = (Snapshot) obj;
        if (accounts == null) {
            if (other.accounts != null) {
                return false;
            }
        } else if (!accounts.equals(other.accounts)) {
            return false;
        }
        if (bankId == null) {
            if (other.bankId != null) {
                return false;
            }
        } else if (!bankId.equals(other.bankId)) {
            return false;
        }
        return true;
    }
}
