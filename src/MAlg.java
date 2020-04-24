import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MAlg {
	public static final long BROADCAST_INTERVAL = 100;
	private static MAlg matternsAlgorithm = null; 
	public final Object lockObject = new Object();
	
	private Bank bank;
	private InitiatorInfo initiatorInfo;
	private final HashMap<UUID, Boolean> acknowledgements = new HashMap<>();
	private final Set<Snapshot> globalSnapshots = new HashSet<>();
	private final Set<Message> whiteMessages = new HashSet<>();
	private final HashMap<UUID, WhiteMessageHistory> globalMessageHistory = new HashMap<>();

	private MAlg() {
	}

	public static MAlg getInstance() 
	{ 
		if (matternsAlgorithm == null) 
			matternsAlgorithm = new MAlg(); 

		return matternsAlgorithm; 
	}

	//init Mattern's algorithm
	public synchronized void initSnapshot() throws IOException, InterruptedException {
		
		//define a future tick for global snapshot
		long futureTick = VectorClock.getInstance().findTick(
				this.bank.getBankID()) + BROADCAST_INTERVAL;
		initiatorInfo = new InitiatorInfo(bank.getBankID(), futureTick);

		initAcknowledgementMap();
		this.bank.broadcastFutureTick(futureTick);
		//wait for all acknowledgments
		while (acknowledgements.values().contains(false)) {
			wait();
		}
		
		//save local state
		synchronized (lockObject) {
			globalSnapshots.add(saveState());
			WhiteMessageHistory newHistory = cloneLocalHistory(bank.getHistory());
			globalMessageHistory.put(bank.getBankID(), newHistory);
			VectorClock.getInstance().set(
					bank.getBankID(), futureTick);
		}
		//broadcast dummy data
		this.bank.broadcast();
		checkAlgorithmTermination();
		System.out.println("snapshot done");
	}
	
	private WhiteMessageHistory cloneLocalHistory(WhiteMessageHistory history) {
		WhiteMessageHistory newHistory = new WhiteMessageHistory(history.getProcessID());
		newHistory.setHistory(new HashMap<>(history.getHistory()));
		return newHistory;
	}

	//init acknowledgement map
	private void initAcknowledgementMap() {
		for (Map.Entry<UUID, RemoteBank> rb : bank.getRemoteBanks().entrySet()) {
			acknowledgements.put(rb.getKey(), false);
		}
	}

	public synchronized void receiveAcknowledgement(UUID processID) {
		this.acknowledgements.put(processID, true);
		notify();
	}

	public Snapshot saveState() {
		synchronized (lockObject) {
			Snapshot snapshot = new Snapshot(bank.getBankID(), 
					bank.getLocalAccounts().values());
			return snapshot;
		}
	}
	
	public synchronized void updateMessageHistory(UUID sourceID, UUID destID) {
		globalMessageHistory.get(destID).receiveFrom(sourceID);
	}
	
	public void checkAlgorithmTermination() throws InterruptedException {
		while (!checkSum()) {
		}
	}
	
	//check consistency between message histories
	private boolean checkSum() {
		if (globalMessageHistory.size() < 2) {
			return false;
		}
		
		for (Map.Entry<UUID, WhiteMessageHistory> e1 : globalMessageHistory.entrySet()) {
			for (Map.Entry<UUID, WhiteMessageHistory> e2 : globalMessageHistory.entrySet()) {
				if (e1 != e2) {
					//if there is communication between e1 and e2
					boolean e1_has_e2 = e1.getValue().getHistory().containsKey(e2.getKey());
					boolean e2_has_e1 = e2.getValue().getHistory().containsKey(e1.getKey());
					
					//one-way indicates the message is not received
					if (e1_has_e2 && !e2_has_e1) {
						return false;
					}
					else if (!e1_has_e2 && e2_has_e1) {
						return false;
					}
					else if (e1_has_e2 && e2_has_e1) {
						int result = e1.getValue().getHistory().get(e2.getKey()) +
								e2.getValue().getHistory().get(e1.getKey());
						if (result != 0) {
							return false;
						}
					}
				}
			}
		}
		
		return true;
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

	public HashMap<UUID, WhiteMessageHistory> getGlobalMessageHistory() {
		return globalMessageHistory;
	}
}
