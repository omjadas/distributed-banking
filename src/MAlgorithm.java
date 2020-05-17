import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Mattern's algorithm
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

    public MAlgorithm(Bank bank) {
        this.bank = bank;
    }

    /**
     * initialize mattern's algorithm
     * @throws IOException
     * @throws InterruptedException
     */
    public synchronized void initSnapshot() throws IOException,
            InterruptedException {
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
     * initialize an acknowledgement map
     * @throws InterruptedException
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
     * handle receive an acknowledgement from other processed
     * @param processId the source process of the acknowledgement
     */
    public synchronized void receiveAcknowledgement(UUID processId) {
        this.acknowledgements.put(processId, true);
        notify();
    }
    
    /**
     * be called when the initiator receives the register response
     * message from another process
     */
    public synchronized void notifyInitAck() {
        notify();
    }

    /**
     * update global counter
     * @param count value of the counter
     */
    public void updateCounter(int count) {
        globalCounter += count;
        terminationDetector.notifyNewMsg();
    }
    
    /**
     * update the number of received snapshots
     */
    public void updateNumSnapshot() {
        numSnapshot += 1;
        terminationDetector.notifyNewMsg();
    }

    public Bank getBank() {
        return bank;
    }

    public InitiatorInfo getInitiatorInfo() {
        return initiatorInfo;
    }

    public void setInitiatorInfo(InitiatorInfo initiatorInfo) {
        this.initiatorInfo = initiatorInfo;
    }

    public Set<Snapshot> getGlobalSnapshots() {
        return globalSnapshots;
    }

    public Set<Message> getWhiteMessages() {
        return whiteMessages;
    }

    public int getGlobalCounter() {
        return globalCounter;
    }

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
            }

            System.out.println("snapshot done");
            // reset
            initiatorInfo = null;
        }
        
        /**
         * check the termination of mattern's algorithm
         * @throws InterruptedException
         */
        public synchronized void checkAlgorithmTermination()
                throws InterruptedException {
            while (true) {
                // check termination every half a second
                wait();
                if (globalCounter == 0 &&
                    numSnapshot == bank.getRemoteBanks().size() + 1) {
                    break;
                }
            }
        }
        
        /**
         * notify whenever receive a snapshot or forwarded white message
         */
        public synchronized void notifyNewMsg() {
            notify();
        }
    }
}
