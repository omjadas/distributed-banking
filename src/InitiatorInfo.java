import java.util.UUID;

public class InitiatorInfo {
	private final UUID initiatorID;
	private final long futureTick;
	
	public InitiatorInfo(UUID initiatorID, long futureTick) {
		this.initiatorID = initiatorID;
		this.futureTick = futureTick;
	}
	
	public UUID getInitiatorID() {
		return initiatorID;
	}
	public long getFutureTick() {
		return futureTick;
	}
}
