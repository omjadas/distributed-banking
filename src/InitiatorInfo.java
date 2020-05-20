import java.util.UUID;

/**
 * A class to store the info of initiator of Mattern's algorithm.
 */
public class InitiatorInfo {
    private final UUID initiatorId;
    private final long futureTick;

    public InitiatorInfo(UUID initiatorId, long futureTick) {
        this.initiatorId = initiatorId;
        this.futureTick = futureTick;
    }

    /**
     * Retrieve the initiator ID.
     *
     * @return ID of the initiator
     */
    public UUID getInitiatorId() {
        return initiatorId;
    }

    /**
     * Retrieve the future tick.
     *
     * @return the future tick
     */
    public long getFutureTick() {
        return futureTick;
    }
}
