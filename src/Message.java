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
    private int msgCounter;

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

    public VectorClock getVClock() {
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
            ((accountIDs == null) ? 0 : accountIDs.hashCode());
        result = prime * result + amount;
        result = prime * result + ((command == null) ? 0 : command.hashCode());
        result = prime * result + (int) (futureTick ^ (futureTick >>> 32));
        result = prime * result + msgCounter;
        result = prime * result +
            ((snapshot == null) ? 0 : snapshot.hashCode());
        result = prime * result +
            ((sourceID == null) ? 0 : sourceID.hashCode());
        result = prime * result +
            ((vectorClock == null) ? 0 : vectorClock.hashCode());
        result = prime * result +
            ((whiteMessage == null) ? 0 : whiteMessage.hashCode());
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
        Message other = (Message) obj;
        if (accountIDs == null) {
            if (other.accountIDs != null)
                return false;
        } else if (!accountIDs.equals(other.accountIDs))
            return false;
        if (amount != other.amount)
            return false;
        if (command != other.command)
            return false;
        if (futureTick != other.futureTick)
            return false;
        if (msgCounter != other.msgCounter)
            return false;
        if (snapshot == null) {
            if (other.snapshot != null)
                return false;
        } else if (!snapshot.equals(other.snapshot))
            return false;
        if (sourceID == null) {
            if (other.sourceID != null)
                return false;
        } else if (!sourceID.equals(other.sourceID))
            return false;
        if (vectorClock == null) {
            if (other.vectorClock != null)
                return false;
        } else if (!vectorClock.equals(other.vectorClock))
            return false;
        if (whiteMessage == null) {
            if (other.whiteMessage != null)
                return false;
        } else if (!whiteMessage.equals(other.whiteMessage))
            return false;
        return true;
    }
}
