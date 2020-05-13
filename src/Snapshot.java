import java.util.Collection;
import java.util.UUID;

public class Snapshot {
    private final UUID processID;
    private final Collection<Account> accounts;

    public Snapshot(UUID processID, Collection<Account> accounts) {
        this.processID = processID;
        this.accounts = accounts;
    }

    public UUID getProcessID() {
        return processID;
    }

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
            ((processID == null) ? 0 : processID.hashCode());
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
        Snapshot other = (Snapshot) obj;
        if (accounts == null) {
            if (other.accounts != null)
                return false;
        } else if (!accounts.equals(other.accounts))
            return false;
        if (processID == null) {
            if (other.processID != null)
                return false;
        } else if (!processID.equals(other.processID))
            return false;
        return true;
    }
}
