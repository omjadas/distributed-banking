import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MAlgorithm {
	public static final long BROADCAST_INTERVAL = 100;
	
	private Bank bank;
	private InitiatorInfo initiatorInfo;
	private final HashMap<UUID, Boolean> acknowledgements = new HashMap<>();
	private final Set<Snapshot> globalSnapshots = new HashSet<>();
	private final Set<Message> whiteMessages = new HashSet<>();
	private int msgCounter = 0;
	private int globalCounte = 0;
	private int numSnapshot = 0;

	public MAlgorithm() {
	}

	//init Mattern's algorithm
	public synchronized void initSnapshot() throws IOException, InterruptedException {
		
		acknowledgements.clear();
		globalSnapshots.clear();
		whiteMessages.clear();
		globalCounte = 0;
		numSnapshot = 0;
		
		//define a future tick for global snapshot
		long futureTick = VClock.getInstance().findTick(
				this.bank.getBankID()) + BROADCAST_INTERVAL;
		initiatorInfo = new InitiatorInfo(bank.getBankID(), futureTick);

		initAcknowledgementMap();
		this.bank.broadcastFutureTick(futureTick);
		//wait for all acknowledgments
		while (acknowledgements.values().contains(false)) {
			wait();
		}
		
		//save local state
		synchronized (bank.LOCK_OBJECT) {
			globalSnapshots.add(saveState());
			globalCounte += msgCounter;
			VClock.getInstance().set(bank.getBankID(), futureTick);
		}
		//broadcast dummy data
		this.bank.broadcastDummyMsg();
		TerminationDetector terminationDetector = new TerminationDetector();
		terminationDetector.start();
	}

	//init acknowledgement map
	private void initAcknowledgementMap() throws InterruptedException {
		while (bank.getRemoteBanks().size() < bank.getRemoteBankThreads().size()) {
			wait();
		}
		
		for (Map.Entry<UUID, RemoteBank> rb : bank.getRemoteBanks().entrySet()) {
			acknowledgements.put(rb.getKey(), false);
		}
	}

	public synchronized void receiveAcknowledgement(UUID processID) {
		this.acknowledgements.put(processID, true);
		notify();
	}
	
	public synchronized void notifyInitAck() {
		notify();
	}

	public Snapshot saveState() {
		synchronized (bank.LOCK_OBJECT) {
			Snapshot snapshot = new Snapshot(bank.getBankID(), 
					bank.getLocalAccounts().values());
			return snapshot;
		}
	}

	//update global counter
	public void updateCounter(int newCounter) {
		globalCounte += newCounter;
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
	
	public int getMsgCounter() {
		return msgCounter;
	}

	public void setMsgCounter(int msgCounter) {
		this.msgCounter = msgCounter;
	}


	public int getGlobalCounte() {
		return globalCounte;
	}

	public void setGlobalCounte(int globalCounte) {
		this.globalCounte = globalCounte;
	}


	public int getNumSnapshot() {
		return numSnapshot;
	}

	public void setNumSnapshot(int numSnapshot) {
		this.numSnapshot = numSnapshot;
	}


	private class TerminationDetector extends Thread {
		
		public TerminationDetector() {
		}
		
		@Override
        public void run() {
			try {
				checkAlgorithmTermination();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			System.out.println("snapshot done");
			//reset
			initiatorInfo = null;
			bank.broadcastSnapshotDoneMsg();
		}
		
		public void checkAlgorithmTermination() throws InterruptedException {
			while (globalCounte != 0 || numSnapshot != bank.getRemoteBanks().size()) {
				//check termination half a second
				Thread.sleep(500);
			}
		}
	}
}
