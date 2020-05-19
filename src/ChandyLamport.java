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
     */
    public void broadCastMarker(Collection<RemoteBank> remoteBanks) {
        for (RemoteBank remoteBank : remoteBanks) {
            try {
                remoteBank.sendChandyLamportMarker(bankState);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Start the algorithm.
     *
     * @param currentState current state of the local bank
     * @param remoteBanks  all of the connected remote banks
     * @return false if no remote banks are connected
     * @throws IOException
     */
    public boolean startAlgorithm(
            Snapshot currentState,
            Collection<RemoteBank> remoteBanks) throws IOException {
        if (remoteBanks.isEmpty()) {
            return false;
        }
        resetAlgorithm(remoteBanks);
        for (RemoteBank remoteBank : remoteBanks) {
            this.otherStates.put(remoteBank.getBankId(), null);
        }
        recordState(currentState);
        broadCastMarker(remoteBanks);
        return true;
    }

    /**
     * Sends a message to all the branches to reset their snapshots.
     *
     * @param remoteBanks all of the connected remote banks
     */
    public void resetAlgorithm(Collection<RemoteBank> remoteBanks) {
        for (RemoteBank remoteBank : remoteBanks) {
            try {
                remoteBank.resetChandyLamportAlgorithm();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
     * @throws IOException
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
                System.out.println(state.toString() + " is not recorded yet.");
                finished = false;
            }
        }

        if (finished) {
            HashMap<UUID, Snapshot> snapshot = getStates();
            for (Map.Entry<UUID, Snapshot> entry : snapshot.entrySet()) {
                UUID branch = entry.getKey();
                Snapshot branchState = snapshot.get(branch);
                System.out.println(
                    "Branch: " + branch + ", " + "State: " + branchState);
            }
        }

        return finished;
    }
}
