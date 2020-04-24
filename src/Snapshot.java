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
}
