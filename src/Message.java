import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class Message {
	private final Command command;
	private final UUID sourceID;
	private final VectorClock vectorClock;
	private long futureTick;
	private ArrayList<String> accountIDs = new ArrayList<>();
	private int amount;
	private Snapshot snapshot;
	private Message whiteMessage;
	private WhiteMessageHistory messageHistory;
	
	public Message(Command command, UUID sourceID, VectorClock vectorClock) {
		this.command = command;
		this.sourceID = sourceID;
		this.vectorClock = vectorClock;
	}
	
	public Command getCommand() {
		return command;
	}
	public UUID getSourceID() {
		return sourceID;
	}
	public VectorClock getVectorClock() {
		return vectorClock;
	}
	public ArrayList<String> getAccountIDs() {
		return accountIDs;
	}
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	
	public void addAccoundID(String id) {
		this.accountIDs.add(id);
	}
	
	public void addAccoundIDs(Set<String> ids) {
		this.accountIDs.addAll(ids);
	}

	public long getFutureTick() {
		return futureTick;
	}

	public void setFutureTick(long futureTick) {
		this.futureTick = futureTick;
	}

	public Snapshot getSnapshot() {
		return snapshot;
	}

	public void setSnapshot(Snapshot snapshot) {
		this.snapshot = snapshot;
	}

	public Message getWhiteMessage() {
		return whiteMessage;
	}

	public void setWhiteMessage(Message whiteMessage) {
		this.whiteMessage = whiteMessage;
	}

	public WhiteMessageHistory getMessageHistory() {
		return messageHistory;
	}

	public void setMessageHistory(WhiteMessageHistory messageHistory) {
		this.messageHistory = messageHistory;
	}
	
}