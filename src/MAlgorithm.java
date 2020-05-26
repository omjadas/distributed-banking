import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Mattern's algorithm.
 */
public class MAlgorithm {
    public static final long BROADCAST_INTERVAL = 100;
    public static final int SEND = 1;
    public static final int RECEIVE = -1;

    private final Bank bank;
    private InitiatorInfo initiatorInfo;
    private final HashMap<UUID, Boolean> acknowledgements = new HashMap<>();
    private final Set<Snapshot> globalSnapshots = new HashSet<>();
    private final Set<Message> whiteMessages = new HashSet<>();
    public int msgCounter = 0; // out minus in
    private int globalCounter = 0;
    private int numSnapshot = 0; // num of snapshots collected
    private TerminationDetector terminationDetector;

    /**
     * Create an instance of MAlgorithm.
     *
     * @param bank bank the algorithm is for
     */
    public MAlgorithm(Bank bank) {
        this.bank = bank;
    }

    /**
     * Initialize mattern's algorithm.
     *
     * @throws InterruptedException if interrupted
     */
    public synchronized void initSnapshot() throws InterruptedException {
        acknowledgements.clear();
        globalSnapshots.clear();
        whiteMessages.clear();
        globalCounter = 0;
        numSnapshot = 0;

        // define a future tick for global snapshot
        long futureTick = VectorClock.getInstance()
                .findTick(this.bank.getBankId()) +
            BROADCAST_INTERVAL;
        initiatorInfo = new InitiatorInfo(bank.getBankId(), futureTick);

        initAcknowledgementMap();
        this.bank.broadcastFutureTick(futureTick);

        // wait for all acknowledgments
        while (acknowledgements.values().contains(false)) {
            wait();
        }

        // save local state
        synchronized (bank) {
            globalSnapshots.add(bank.takeSnapshot());
            globalCounter += msgCounter;
            numSnapshot += 1;
            VectorClock.getInstance().set(bank.getBankId(), futureTick);
        }

        terminationDetector = new TerminationDetector();
        terminationDetector.start();

        // broadcast dummy data
        this.bank.broadcastDummyMsg();
    }

    /**
     * Initialize an acknowledgement map.
     *
     * @throws InterruptedException if interrupted
     */
    private void initAcknowledgementMap() throws InterruptedException {
        while (bank.getRemoteBanks().size() < bank.getRemoteBankThreads()
                .size()) {
            wait();
        }

        for (Map.Entry<UUID, RemoteBank> rb : bank.getRemoteBanks()
                .entrySet()) {
            acknowledgements.put(rb.getKey(), false);
        }
    }

    /**
     * Handle receive an acknowledgement from other processed.
     *
     * @param processId the source process of the acknowledgement
     */
    public synchronized void receiveAcknowledgement(UUID processId) {
        this.acknowledgements.put(processId, true);
        notify();
    }

    /**
     * Called when the initiator receives the register response message from
     * another process.
     */
    public synchronized void notifyInitAck() {
        notify();
    }

    /**
     * Update global counter.
     *
     * @param count value of the counter
     */
    public void updateCounter(int count) {
        globalCounter += count;
        terminationDetector.notifyNewMsg();
    }

    /**
     * Update the number of received snapshots.
     */
    public void updateNumSnapshot() {
        numSnapshot += 1;
        terminationDetector.notifyNewMsg();
    }

    /**
     * Retrieve the bank.
     *
     * @return the bank this algorithm is for
     */
    public Bank getBank() {
        return bank;
    }

    /**
     * Retrieve the initiator info.
     *
     * @return info on the initiator of the algorithm
     */
    public InitiatorInfo getInitiatorInfo() {
        return initiatorInfo;
    }

    /**
     * Set the initiator info.
     *
     * @param initiatorInfo info on the initiator of the algorithm
     */
    public void setInitiatorInfo(InitiatorInfo initiatorInfo) {
        this.initiatorInfo = initiatorInfo;
    }

    /**
     * Get global snapshots.
     *
     * @return Snapshots for all processes.
     */
    public Set<Snapshot> getGlobalSnapshots() {
        return globalSnapshots;
    }

    /**
     * Retrieve white messages.
     *
     * @return white messages
     */
    public Set<Message> getWhiteMessages() {
        return whiteMessages;
    }

    /**
     * Retrieve the global counter.
     *
     * @return the global counter.
     */
    public int getGlobalCounter() {
        return globalCounter;
    }

    /**
     * Set the global counter.
     *
     * @param globalCounter value to set the global counter to
     */
    public void setGlobalCounter(int globalCounter) {
        this.globalCounter = globalCounter;
    }

    private class TerminationDetector extends Thread {
        @Override
        public void run() {
            try {
                checkAlgorithmTermination();
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.out.print("> ");
            }
            
            bank.printSnapshots(globalSnapshots);
            System.out.println();
            bank.printWhiteMessages(whiteMessages);
            // reset
            initiatorInfo = null;
        }

        /**
         * Check the termination of mattern's algorithm.
         *
         * @throws InterruptedException if interrupted
         */
        public synchronized void checkAlgorithmTermination()
                throws InterruptedException {
            while (true) {
            	if (globalCounter == 0 &&
                        numSnapshot == bank.getRemoteBanks().size() + 1) {
                        break;
                }
                wait();
            }
        }

        /**
         * Notify whenever receive a snapshot or forwarded white message.
         */
        public synchronized void notifyNewMsg() {
            notify();
        }
    }
}
