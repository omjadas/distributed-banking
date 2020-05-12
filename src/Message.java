import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class Message {
    private final Command command;
    private final UUID sourceID;
    private final VClock vClock;
    private long futureTick;
    private ArrayList<String> accountIDs = new ArrayList<>();
    private int amount;
    private Snapshot snapshot;
    private Message whiteMessage;
    private int msgCounter;

    public Message(Command command, UUID sourceID, VClock vectorClock) {
        this.command = command;
        this.sourceID = sourceID;
        this.vClock = vectorClock;
    }

    public Command getCommand() {
        return command;
    }

    public UUID getSourceID() {
        return sourceID;
    }

    public VClock getVClock() {
        return vClock;
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
}
