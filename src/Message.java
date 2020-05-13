import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

/**
 * Message that is sent between banks.
 */
public class Message {
    private final Command command;
    private final UUID sourceId;
    private final VectorClock vectorClock;
    private long futureTick;
    private ArrayList<String> accountIds = new ArrayList<>();
    private int amount;
    private Snapshot snapshot;
    private Message whiteMessage;
    private int msgCounter;

    public Message(Command command, UUID sourceId, VectorClock vectorClock) {
        this.command = command;
        this.sourceId = sourceId;
        this.vectorClock = vectorClock;
    }

    /**
     * Retrieve the command of the message.
     *
     * @return the command of the message
     */
    public Command getCommand() {
        return command;
    }

    /**
     * Retrieve the ID of the message source.
     *
     * @return the ID of the message source
     */
    public UUID getSourceId() {
        return sourceId;
    }

    /**
     * Retrieve the vector clock contained in the message.
     *
     * @return the vector clock contained in the message.
     */
    public VectorClock getVectorClock() {
        return vectorClock;
    }

    /**
     * Retrieve the account IDs contained in the message.
     *
     * @return the account IDs contained in the message
     */
    public ArrayList<String> getAccountIds() {
        return accountIds;
    }

    /**
     * Retrieve the amount contained in the message.
     *
     * @return the amount contained in the message
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Set the amount of the message.
     *
     * @param amount the amount to set
     */
    public void setAmount(int amount) {
        this.amount = amount;
    }

    /**
     * Add an account ID to the message.
     *
     * @param id the account ID to add
     */
    public void addAccountId(String id) {
        this.accountIds.add(id);
    }

    /**
     * Add multiple account IDs to the message.
     *
     * @param ids the account IDs to add
     */
    public void addAccountIds(Set<String> ids) {
        this.accountIds.addAll(ids);
    }

    public long getFutureTick() {
        return futureTick;
    }

    public void setFutureTick(long futureTick) {
        this.futureTick = futureTick;
    }

    /**
     * Retrieve the snapshot contained in the message.
     *
     * @return the snapshot contained in the message
     */
    public Snapshot getSnapshot() {
        return snapshot;
    }

    /**
     * Set the snapshot of the message.
     *
     * @param snapshot the snapshot to set.
     */
    public void setSnapshot(Snapshot snapshot) {
        this.snapshot = snapshot;
    }

    public Message getWhiteMessage() {
        return whiteMessage;
    }

    public void setWhiteMessage(Message whiteMessage) {
        this.whiteMessage = whiteMessage;
    }

    public int getMsgCounter() {
        return msgCounter;
    }

    public void setMsgCounter(int msgCounter) {
        this.msgCounter = msgCounter;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result +
            ((accountIds == null) ? 0 : accountIds.hashCode());
        result = prime * result + amount;
        result = prime * result + ((command == null) ? 0 : command.hashCode());
        result = prime * result + (int) (futureTick ^ (futureTick >>> 32));
        result = prime * result + msgCounter;
        result = prime * result +
            ((snapshot == null) ? 0 : snapshot.hashCode());
        result = prime * result +
            ((sourceId == null) ? 0 : sourceId.hashCode());
        result = prime * result +
            ((vectorClock == null) ? 0 : vectorClock.hashCode());
        result = prime * result +
            ((whiteMessage == null) ? 0 : whiteMessage.hashCode());
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
        Message other = (Message) obj;
        if (accountIds == null) {
            if (other.accountIds != null) {
                return false;
            }
        } else if (!accountIds.equals(other.accountIds)) {
            return false;
        }
        if (amount != other.amount) {
            return false;
        }
        if (command != other.command) {
            return false;
        }
        if (futureTick != other.futureTick) {
            return false;
        }
        if (msgCounter != other.msgCounter) {
            return false;
        }
        if (snapshot == null) {
            if (other.snapshot != null) {
                return false;
            }
        } else if (!snapshot.equals(other.snapshot)) {
            return false;
        }
        if (sourceId == null) {
            if (other.sourceId != null) {
                return false;
            }
        } else if (!sourceId.equals(other.sourceId)) {
            return false;
        }
        if (vectorClock == null) {
            if (other.vectorClock != null) {
                return false;
            }
        } else if (!vectorClock.equals(other.vectorClock)) {
            return false;
        }
        if (whiteMessage == null) {
            if (other.whiteMessage != null) {
                return false;
            }
        } else if (!whiteMessage.equals(other.whiteMessage)) {
            return false;
        }
        return true;
    }
}
