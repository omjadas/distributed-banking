import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Chandy-Lamport algorithm.
 */
public class ChandyLamport {
    private UUID bankId;
    private Bank bank;
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
     * @param remoteBanks connected remote banks
     * @throws IOException if unable to send markers
     */
    public void broadCastMarker(Collection<RemoteBank> remoteBanks)
            throws IOException {
        for (RemoteBank remoteBank : remoteBanks) {
            remoteBank.sendChandyLamportMarker(bankState);
        }
    }

    /**
     * Start the algorithm.
     *
     * @param currentState current state of the local bank
     * @param remoteBanks  all of the connected remote banks
     * @return false if no remote banks are connected
     * @throws IOException if unable to start algorithm
     */
    public void startAlgorithm(
            Snapshot currentState,
            Collection<RemoteBank> remoteBanks) throws IOException {
        recordState(currentState);
        if (remoteBanks.isEmpty()) {
            HashMap<UUID, Snapshot> snapshots = getStates();
            bank.printSnapshots(snapshots.values());
            return;
        }
        resetAlgorithm(remoteBanks);
        for (RemoteBank remoteBank : remoteBanks) {
            this.otherStates.put(remoteBank.getBankId(), null);
        }
        
        broadCastMarker(remoteBanks);
        return;
    }

    /**
     * Sends a message to all the branches to reset their snapshots.
     *
     * @param remoteBanks all of the connected remote banks
     * @throws IOException if unable to send reset
     */
    public void resetAlgorithm(Collection<RemoteBank> remoteBanks)
            throws IOException {
        for (RemoteBank remoteBank : remoteBanks) {
            remoteBank.resetChandyLamportAlgorithm();
        }
        eraseSnapshot();
    }

    /**
     * Resets the snapshot of the branch.
     */
    public void eraseSnapshot() {
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
                broadCastMarker(bank.getRemoteBanks().values());
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
