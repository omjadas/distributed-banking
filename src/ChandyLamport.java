import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Chandy-Lamport algorithm.
 */
public class ChandyLamport {
    private final UUID bankId;
    private final Bank bank;
    private Snapshot bankState;
    private HashMap<UUID, Snapshot> otherStates;
    private boolean stateRecorded;
    private boolean finished;

    /**
     * Constructor.
     *
     * @param bank local bank
     */
    ChandyLamport(Bank bank) {
        this.bankId = bank.getBankId();
        this.bank = bank;
        this.stateRecorded = false;
        this.finished = false;
        this.otherStates = new HashMap<>();
    }

    /**
     * Store the current state of the bank.
     *
     * @param currentState state to store
     */
    public void recordState(Snapshot currentState) {
        bankState = currentState;
        stateRecorded = true;
    }

    /**
     * Attempts to send the current state to the other branches.
     *
     * @throws IOException if unable to send markers
     */
    public void broadCastMarker() throws IOException {
        for (RemoteBank remoteBank : bank.getRemoteBanks().values()) {
            remoteBank.sendChandyLamportMarker(bankState);
        }
    }

    /**
     * Start the algorithm.
     *
     * @param currentState current state of the local bank
     * @throws IOException if unable to start algorithm
     */
    public void startAlgorithm(Snapshot currentState) throws IOException {
        if (bank.getRemoteBanks().isEmpty()) {
            recordState(currentState);
            HashMap<UUID, Snapshot> snapshots = getStates();
            bank.printSnapshots(snapshots.values());
        } else {
            resetAlgorithm();
            recordState(currentState);
            broadCastMarker();
        }
    }

    /**
     * Sends a message to all the branches to reset their snapshots.
     *
     * @throws IOException if unable to send reset
     */
    public void resetAlgorithm() throws IOException {
        for (RemoteBank remoteBank : bank.getRemoteBanks().values()) {
            remoteBank.resetChandyLamportAlgorithm();
        }
        eraseSnapshot();
    }

    /**
     * Resets the snapshot of the branch.
     */
    public void eraseSnapshot() {
        otherStates = new HashMap<>();
        for (Map.Entry<UUID, RemoteBank> state : bank.getRemoteBanks()
                .entrySet()) {
            otherStates.put(state.getKey(), null);
        }
        bankState = null;
        stateRecorded = false;
        finished = false;
    }

    /**
     * Get the snapshots.
     *
     * @return the snapshots for all connected banks.
     */
    public HashMap<UUID, Snapshot> getStates() {
        HashMap<UUID, Snapshot> allStates = new HashMap<UUID, Snapshot>(
            otherStates);
        allStates.put(bankId, bankState);
        return allStates;
    }

    /**
     * Handle a received marker. This is the bulk of the algorithm logic.
     *
     * @param remoteBankId   ID of the remote bank
     * @param receivedMarker state of the remote bank
     * @param currentState   current local state
     * @return true if the algorithm is finished
     * @throws IOException if unable to broadcast marker
     */
    public boolean handleReceivedMarker(
            UUID remoteBankId,
            Snapshot receivedMarker,
            Snapshot currentState) throws IOException {
        if (!finished) {
            if (stateRecorded) {
                otherStates.put(remoteBankId, receivedMarker);
            } else {
                recordState(currentState);
                otherStates.put(remoteBankId, receivedMarker);
                broadCastMarker();
            }
        }

        finished = true;
        for (Map.Entry<UUID, Snapshot> state : otherStates.entrySet()) {
            if (state.getValue() == null) {
                finished = false;
            }
        }

        if (finished) {
            HashMap<UUID, Snapshot> snapshots = getStates();
            bank.printSnapshots(snapshots.values());
            System.out.print("> ");
        }

        return finished;
    }
}
