import java.util.Collection;
import java.util.UUID;

public class Snapshot {
    private final UUID processId;
    private final Collection<Account> accounts;

    public Snapshot(UUID processId, Collection<Account> accounts) {
        this.processId = processId;
        this.accounts = accounts;
    }

    public UUID getProcessId() {
        return processId;
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
            ((processId == null) ? 0 : processId.hashCode());
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
        if (processId == null) {
            if (other.processId != null) {
                return false;
            }
        } else if (!processId.equals(other.processId)) {
            return false;
        }
        return true;
    }
}
