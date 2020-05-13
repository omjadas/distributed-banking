import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

// Chandy-Lamport Algorithm and related methods.
// States with "-" imply non-recorded state.

public class ChandyLamport {
    private UUID bankId;
    private Bank bank;
    private Snapshot bankState;
    private HashMap<UUID, Snapshot> otherStates;
    private boolean stateRecorded;

    // Constructors.

    ChandyLamport(Bank bank) {
        this.bankId = bank.getBankId();
        this.bank = bank;
        this.stateRecorded = false;
        this.otherStates = new HashMap<>();
    }

    ChandyLamport(UUID bankId, Set<UUID> allBankIds) {
        this.bankId = bankId;
        this.stateRecorded = false;
        this.otherStates = new HashMap<>();
        for (UUID currentBankId : allBankIds) {
            otherStates.put(currentBankId, null);
        }
    }

    // Add a bank to the list of connected banks.

    public void addBank(UUID bankId) {
        otherStates.put(bankId, null);
    }

    // Take a string to store the current state of bank.

    public void recordState(Snapshot currentState) {
        bankState = currentState;
        stateRecorded = true;
    }

    // Attempts to send the current state to the other banks.

    public void broadCastMarker(Collection<RemoteBank> remoteBanks) {
        for (RemoteBank remoteBank : remoteBanks) {
            try {
                remoteBank.sendChandyLamportMarker(bankState);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Start of algorithm.

    public boolean startAlgorithm(
            Snapshot currentState,
            Collection<RemoteBank> remoteBanks) throws IOException {
        if (otherStates.isEmpty()) {
            return false;
        }
        recordState(currentState);
        broadCastMarker(remoteBanks);
        return true;
    }

    // End of algorithm - call this to erase snapshot.

    public void resetAlgorithm() {
        for (Map.Entry<UUID, Snapshot> state : otherStates.entrySet()) {
            otherStates.put(state.getKey(), null);
        }
        bankState = null;
        stateRecorded = false;
    }

    // Getter for the snapshot.

    public HashMap<UUID, Snapshot> getStates() {
        HashMap<UUID, Snapshot> allStates = new HashMap<UUID, Snapshot>(
            otherStates);
        allStates.put(bankId, bankState);
        return allStates;
    }

    // Method for what to do if a bank receives a chandy-lamport marker.

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
            if (state.getValue().equals("-")) {
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