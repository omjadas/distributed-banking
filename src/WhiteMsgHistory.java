import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WhiteMsgHistory {
	private final UUID processID;
	private HashMap<UUID, Long> history = new HashMap<>();

	public WhiteMsgHistory(UUID processID) {
		this.processID = processID;
	}
	public HashMap<UUID, Long> getHistory() {
		return history;
	}
	public void setHistory(HashMap<UUID, Long> history) {
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
			history.put(destID, (long)1);
		}
	}

	public void receiveFrom(UUID sourceID) {
		if (history.containsKey(sourceID)) {
			history.put(sourceID, history.get(sourceID) - 1);
		}
		else {
			history.put(sourceID, (long)-1);
		}
	}

	//accumulate a history with another
	public void accumulate(WhiteMsgHistory other) {
		for (Map.Entry<UUID, Long> otherHistory : other.history.entrySet()) {
			Long num = this.history.get(otherHistory.getKey());
			if (num == null) {
				this.history.put(otherHistory.getKey(), otherHistory.getValue());
			} 
			else {
				num += otherHistory.getValue();
				this.history.put(otherHistory.getKey(), num);
			}
		}
	}
}
