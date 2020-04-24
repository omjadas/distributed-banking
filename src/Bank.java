
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Bank implements Runnable {
	private final ServerSocket serverSocket;
	private final UUID bankID;
	private final HashMap<String, RemoteBank> remoteAccounts = new HashMap<>();
	private final HashMap<String, Account> localAccounts = new HashMap<>();
	private final HashMap<UUID, RemoteBank> remoteBanks = new HashMap<>();
	private final Set<Thread> remoteBankThreads = new HashSet<>();
	private final WhiteMsgHistory messageHistory;

	public Bank(UUID bankID, int port) throws IOException {
		this.bankID = bankID;
		serverSocket = new ServerSocket(port);
		messageHistory = new WhiteMsgHistory(bankID);
	}

	public void connect(String hostname, int port) throws IOException {
		RemoteBank remoteBank = new RemoteBank(hostname, port, this);
		Thread remoteBankThread = new Thread(remoteBank);
		remoteBankThread.start();
		remoteBankThreads.add(remoteBankThread);
	}

	public void open(String accountId) {
		localAccounts.put(accountId, new Account(accountId));
	}

	public void register(String accountId, RemoteBank bank) {
		remoteAccounts.put(accountId, bank);
	}

	public void deposit(String accountId, int amount) throws IOException,
	UnknownAccountException {
		synchronized (MAlgorithm.getInstance().lockObject) {
			if (localAccounts.containsKey(accountId)) {
				localAccounts.get(accountId).deposit(amount);
			} else if (remoteAccounts.containsKey(accountId)) {
				remoteAccounts.get(accountId).deposit(accountId, amount);
			} else {
				throw new UnknownAccountException(
						String.format("Unknown account %s", accountId));
			}
		}
	}

	public void withdraw(String accountId, int amount) throws IOException,
	UnknownAccountException {
		synchronized (MAlgorithm.getInstance().lockObject) {
			if (localAccounts.containsKey(accountId)) {
				localAccounts.get(accountId).withdraw(amount);
			} else if (remoteAccounts.containsKey(accountId)) {
				remoteAccounts.get(accountId).withdraw(accountId, amount);
			} else {
				throw new UnknownAccountException(
						String.format("Unknown account %s", accountId));
			}
		}
	}

	public void transfer(String sourceId, String destId, int amount)
			throws IOException,
			UnknownAccountException {
		synchronized (MAlgorithm.getInstance().lockObject) {
			withdraw(sourceId, amount);
			deposit(destId, amount);
		}
	}

	public void printBalance(String accountId) throws IOException {
		if (localAccounts.containsKey(accountId)) {
			System.out.println(
					String.format(
							"$%d",
							localAccounts.get(accountId).getBalance()));
		} else if (remoteAccounts.containsKey(accountId)) {
			remoteAccounts.get(accountId).printBalance(accountId);
		}
	}

	public int getBalance(String accountId) {
		return localAccounts.get(accountId).getBalance();
	}

	public Set<String> getAccountIds() {
		return localAccounts.keySet();
	}

	@Override
	public void run() {
		Socket socket;
		try {
			while ((socket = serverSocket.accept()) != null &&
					!Thread.interrupted()) {
				RemoteBank remoteBank;
				remoteBank = new RemoteBank(socket, this);
				Thread remoteBankThread = new Thread(remoteBank);
				remoteBankThread.start();
				remoteBankThreads.add(remoteBankThread);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		remoteBankThreads.forEach(remoteBankThread -> {
			remoteBankThread.interrupt();
		});
	}

	public UUID getBankID() {
		return bankID;
	}

	public HashMap<String, Account> getLocalAccounts() {
		return localAccounts;
	}

	public HashMap<UUID, RemoteBank> getRemoteBanks() {
		return remoteBanks;
	}

	public void broadcastFutureTick(long tick) {
		this.remoteBanks.values().forEach(remoteBank -> {
			try {
				remoteBank.sendFutureTick(tick);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	public void broadcastDummy() {
		this.remoteBanks.values().forEach(remoteBank -> {
			try {
				remoteBank.sendDummy();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	public void sendSnapshotToInitiator(Snapshot snapshot) throws IOException {
		UUID initiatorID = MAlgorithm.getInstance().getInitiatorInfo().getInitiatorID();
		remoteBanks.get(initiatorID).sendSnapshotToInitiator(snapshot);
	}

	public void sendWhiteMessageToInitiator(Message whiteMessage) throws IOException {
		UUID initiatorID = MAlgorithm.getInstance().getInitiatorInfo().getInitiatorID();
		remoteBanks.get(initiatorID).sendWhiteMessageToInitiator(whiteMessage);
	}

	public void sendMessageTo(UUID processID) {
		messageHistory.sendTo(processID);
	}

	public void receiveMessageFrom(UUID processID) {
		messageHistory.receiveFrom(processID);
	}

	public WhiteMsgHistory getHistory() {
		return messageHistory;
	}
}
