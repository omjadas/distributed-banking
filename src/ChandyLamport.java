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

    /**
     * Constructor.
     *
     * @param bank local bank
     */
    ChandyLamport(Bank bank) {
        this.bankId = bank.getBankId();
        this.bank = bank;
        this.stateRecorded = false;
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
     * Attempts to send the current state to the other banks.
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
        for (RemoteBank remoteBank : remoteBanks) {
            this.otherStates.put(remoteBank.getBankId(), null);
        }
        recordState(currentState);
        broadCastMarker(remoteBanks);
        return true;
    }

    /**
     * End of algorithm, call this to erase snapshot.
     */
    public void resetAlgorithm() {
        for (Map.Entry<UUID, Snapshot> state : otherStates.entrySet()) {
            otherStates.put(state.getKey(), null);
        }
        bankState = null;
        stateRecorded = false;
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

    // Method for what to do if a bank receives a chandy-lamport marker.

    /**
     * Handle a received marker.
     *
     * @param remoteBankId   ID of the remote bank
     * @param receivedMarker state of the remote bank
     * @param currentState   current local state
     * @return true if the algorithm
     * @throws IOException
     */
    public boolean handleReceivedMarker(
            UUID remoteBankId,
            Snapshot receivedMarker,
            Snapshot currentState) throws IOException {
        if (stateRecorded) {
            otherStates.put(remoteBankId, receivedMarker);
        } else {
            recordState(currentState);
            broadCastMarker(bank.getRemoteBanks().values());
        }

        boolean finished = true;
        for (Map.Entry<UUID, Snapshot> state : otherStates.entrySet()) {
            if (state.getValue() == null) {
                finished = false;
            }
        }

        if (finished) {
            HashMap<UUID, Snapshot> snapshot = getStates();
            for (Map.Entry<UUID, Snapshot> entry : otherStates.entrySet()) {
                UUID branch = entry.getKey();
                Snapshot branchState = snapshot.get(branch);
                System.out.println(
                    "Branch: " + branch + ", " + "State: " + branchState);
            }

            resetAlgorithm();
        }

        return finished;
    }
}
