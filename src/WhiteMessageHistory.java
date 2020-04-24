import java.util.HashMap;
import java.util.UUID;

public class WhiteMessageHistory {
	private final UUID processID;
	private HashMap<UUID, Integer> history = new HashMap<>();

	public WhiteMessageHistory(UUID processID) {
		this.processID = processID;
	}
	public HashMap<UUID, Integer> getHistory() {
		return history;
	}
	public void setHistory(HashMap<UUID, Integer> history) {
		this.history = history;
	}
	public UUID getProcessID() {
		return processID;
	}

	public void sendTo(UUID destID) {
		if (history.containsKey(destID)) {
			history.put(destID, history.get(destID) + 1);
		}
		else {
			history.put(destID, 1);
		}
	}

	public void receiveFrom(UUID sourceID) {
		if (history.containsKey(sourceID)) {
			history.put(sourceID, history.get(sourceID) - 1);
		}
		else {
			history.put(sourceID, -1);
		}
	}

	public Object clone()throws CloneNotSupportedException{  
		return super.clone();  
	}  
}
