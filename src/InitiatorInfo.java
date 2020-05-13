import java.util.UUID;

public class InitiatorInfo {
    private final UUID initiatorId;
    private final long futureTick;

    public InitiatorInfo(UUID initiatorId, long futureTick) {
        this.initiatorId = initiatorId;
        this.futureTick = futureTick;
    }

    public UUID getInitiatorId() {
        return initiatorId;
    }

    public long getFutureTick() {
        return futureTick;
    }
}
