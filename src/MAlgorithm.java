import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MAlgorithm {
    public static final long BROADCAST_INTERVAL = 100;
    public static final int SEND = 1;
    public static final int RECEIVE = -1;

    private Bank bank;
    private InitiatorInfo initiatorInfo;
    private final HashMap<UUID, Boolean> acknowledgements = new HashMap<>();
    private final Set<Snapshot> globalSnapshots = new HashSet<>();
    private final Set<Message> whiteMessages = new HashSet<>();
    public int msgCounter = 0; // out minus in
    private int globalCounter = 0;
    private int numSnapshot = 0; // num of snapshots collected
    private TerminationDetector terminationDetector;

    // init Mattern's algorithm
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

        // broadcast dummy data
        this.bank.broadcastDummyMsg();
        terminationDetector = new TerminationDetector();
        terminationDetector.start();
    }

    // init acknowledgement map
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

    public synchronized void receiveAcknowledgement(UUID processId) {
        this.acknowledgements.put(processId, true);
        notify();
    }

    public synchronized void notifyInitAck() {
        notify();
    }

    // update global counter
    public void updateCounter(int newCounter) {
        globalCounter += newCounter;
        terminationDetector.notifyNewMsg();
    }

    public void updateNumSnapshot() {
        numSnapshot += 1;
        terminationDetector.notifyNewMsg();
    }

    public Bank getBank() {
        return bank;
    }

    public void setBank(Bank bank) {
        this.bank = bank;
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

        public synchronized void notifyNewMsg() {
            notify();
        }
    }
}
