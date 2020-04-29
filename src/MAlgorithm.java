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
	private final HashMap<UUID, WhiteMsgHistory> globalMessageHistory = new HashMap<>();

	public MAlgorithm() {
	}

	//init Mattern's algorithm
	public synchronized void initSnapshot() throws IOException, InterruptedException {
		
		acknowledgements.clear();
		globalSnapshots.clear();
		whiteMessages.clear();
		globalMessageHistory.clear();
		
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
			WhiteMsgHistory newHistory = cloneLocalHistory(bank.getHistory());
			globalMessageHistory.put(bank.getBankID(), newHistory);
			VClock.getInstance().set(
					bank.getBankID(), futureTick);
		}
		//broadcast dummy data
		this.bank.broadcastDummyMsg();
		TerminationDetector terminationDetector = new TerminationDetector();
		terminationDetector.start();
	}
	
	private WhiteMsgHistory cloneLocalHistory(WhiteMsgHistory history) {
		WhiteMsgHistory newHistory = new WhiteMsgHistory(history.getProcessID());
		newHistory.setHistory(new HashMap<>(history.getHistory()));
		return newHistory;
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

	//the initiator received a copied white message
	public void accumulateHistories(UUID sourceID, UUID destID) {
		WhiteMsgHistory newHistory = new WhiteMsgHistory(destID);
		newHistory.receiveFrom(sourceID);
		accumulateHistories(destID, newHistory);
	}
	
	//accumulate two histories together
	public void accumulateHistories(UUID id, WhiteMsgHistory newHistory) {
		if (!globalMessageHistory.containsKey(id)) {
			globalMessageHistory.put(id, newHistory);
		}
		else {
			WhiteMsgHistory existingHistory = globalMessageHistory.get(id);
			existingHistory.accumulate(newHistory);
			globalMessageHistory.put(id, existingHistory);
		}
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

	public HashMap<UUID, WhiteMsgHistory> getGlobalMessageHistory() {
		return globalMessageHistory;
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
			System.out.println(whiteMessages.size());
		}
		
		public void checkAlgorithmTermination() throws InterruptedException {
			while (!checkSum()) {
				//check termination half a second
				Thread.sleep(100);
			}
		}
		
		//check consistency between message histories
		private boolean checkSum() {
			int totalRemoteBanks = globalMessageHistory.get(bank.getBankID()).getHistory().size();
			if (globalMessageHistory.size() < totalRemoteBanks + 1) {
				//indicates only part of the global system snapshots are collected
				return false;
			}
			
			for (Map.Entry<UUID, WhiteMsgHistory> e1 : globalMessageHistory.entrySet()) {
				for (Map.Entry<UUID, WhiteMsgHistory> e2 : globalMessageHistory.entrySet()) {
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
							long result = e1.getValue().getHistory().get(e2.getKey()) +
									e2.getValue().getHistory().get(e1.getKey());
//							System.out.println(e1.getValue().getHistory().get(e2.getKey()));
//							System.out.println(e2.getValue().getHistory().get(e1.getKey()));
							if (result != 0) {
								return false;
							}
						}
					}
				}
			}
			
			return true;
		}
	}
}
