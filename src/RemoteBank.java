
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.UUID;

import com.google.gson.Gson;

public class RemoteBank implements Runnable {
	private final Socket socket;
	private final BufferedWriter out;
	private final BufferedReader in;
	private final Bank bank;
	private UUID remoteBankID;

	public RemoteBank(String hostname, int port, Bank bank) throws IOException {
		synchronized (MAlg.getInstance().lockObject) {
			this.socket = new Socket(hostname, port);
			this.out = new BufferedWriter(
					new OutputStreamWriter(socket.getOutputStream()));
			this.in = new BufferedReader(
					new InputStreamReader(socket.getInputStream()));
			this.bank = bank;

			Gson gson = new Gson();
			VectorClock.getInstance().tick(bank.getBankID());
			Message message = new Message(
					Command.REGISTER, bank.getBankID(), VectorClock.getInstance());
			message.addAccoundIDs(bank.getAccountIds());
			out.write(gson.toJson(message));
			out.newLine();
			out.flush();
		}

		//        out.write(
		//            String.format(
		//                "register %s",
		//                String.join(" ", bank.getAccountIds())));
		//        out.newLine();
		//        out.flush();
	}

	public RemoteBank(Socket socket, Bank bank) throws IOException {
		this.socket = socket;
		this.out = new BufferedWriter(
				new OutputStreamWriter(socket.getOutputStream()));
		this.in = new BufferedReader(
				new InputStreamReader(socket.getInputStream()));
		this.bank = bank;
	}

	public void deposit(String accountId, int amount) throws IOException {
		Gson gson = new Gson();
		VectorClock.getInstance().tick(bank.getBankID());
		Message message = new Message(
				Command.DEPOSIT, bank.getBankID(), VectorClock.getInstance());
		message.addAccoundID(accountId);
		message.setAmount(amount);
		out.write(gson.toJson(message));
		out.newLine();
		out.flush();
		bank.sendMessageTo(remoteBankID);
		//        out.write(String.format("deposit %s %d", accountId, amount));
		//        out.newLine();
		//        out.flush();
	}

	public void withdraw(String accountId, int amount) throws IOException {
		Gson gson = new Gson();
		VectorClock.getInstance().tick(bank.getBankID());
		Message message = new Message(
				Command.WITHDRAW, bank.getBankID(), VectorClock.getInstance());
		message.addAccoundID(accountId);
		message.setAmount(amount);
		out.write(gson.toJson(message));
		out.newLine();
		out.flush();
		bank.sendMessageTo(remoteBankID);
		//        out.write(String.format("withdraw %s %d", accountId, amount));
		//        out.newLine();
		//        out.flush();
	}

	public void printBalance(String accountId) throws IOException {
		synchronized (MAlg.getInstance().lockObject) {
			Gson gson = new Gson();
			VectorClock.getInstance().tick(bank.getBankID());
			Message message = new Message(
					Command.GET_BALANCE, bank.getBankID(), VectorClock.getInstance());
			message.addAccoundID(accountId);
			out.write(gson.toJson(message));
			out.newLine();
			out.flush();
			bank.sendMessageTo(remoteBankID);
		}
		//        out.write(String.format("getBalance %s", accountId));
		//        out.newLine();
		//        out.flush();
	}

	public void sendFutureTick(long tick) throws IOException {
		synchronized (MAlg.getInstance().lockObject) {
			Gson gson = new Gson();
			VectorClock.getInstance().tick(bank.getBankID());
			Message message = new Message(
					Command.TAKE_SNAPSHOT, bank.getBankID(), VectorClock.getInstance());
			message.setFutureTick(tick);
			out.write(gson.toJson(message));
			out.newLine();
			out.flush();
			bank.sendMessageTo(remoteBankID);
		}
		//    	out.write(String.format("snapshot %s %d", processID, tick));
		//        out.newLine();
		//        out.flush();
	}

	public void sendDummy() throws IOException {
		synchronized (MAlg.getInstance().lockObject) {
			Gson gson = new Gson();
			VectorClock.getInstance().tick(bank.getBankID());
			Message message = new Message(
					Command.DUMMY, bank.getBankID(), VectorClock.getInstance());
			out.write(gson.toJson(message));
			out.newLine();
			out.flush();
			bank.sendMessageTo(remoteBankID);
		}
	}

	public void sendSnapshotToInitiator(Snapshot snapshot) throws IOException {
		Gson gson = new Gson();
		VectorClock.getInstance().tick(bank.getBankID());
		Message message = new Message(
				Command.SNAPSHOT, bank.getBankID(), VectorClock.getInstance());
		message.setSnapshot(snapshot);
		message.setMessageHistory(bank.getHistory());
		out.write(gson.toJson(message));
		out.newLine();
		out.flush();
	}

	public void sendWhiteMessageToInitiator(Message whiteMessage) throws IOException {
		Gson gson = new Gson();
		VectorClock.getInstance().tick(bank.getBankID());
		Message message = new Message(
				Command.WHITE_MESSAGE, bank.getBankID(), VectorClock.getInstance());
		message.setWhiteMessage(whiteMessage);
		out.write(gson.toJson(message));
		out.newLine();
		out.flush();
	}

	@Override
	public void run() {
		String input;
		try {
			while ((input = in.readLine()) != null && !Thread.interrupted()) {
				process(input);
				//				System.out.println(input);

				//                String[] tokens = input.split(" ");
				//                String command = tokens[0];
				//                if (command.equals("register")) {
				//                    for (int i = 1; i < tokens.length; i++) {
				//                        bank.register(tokens[i], this);
				//                    }
				//                    out.write(
				//                        String.format(
				//                            "registerResponse %s",
				//                            String.join(" ", bank.getAccountIds())));
				//                    out.newLine();
				//                    out.flush();
				//                } else if (command.equals("deposit")) {
				//                    String accountId = tokens[1];
				//                    int amount = Integer.parseInt(tokens[2]);
				//                    bank.deposit(accountId, amount);
				//                } else if (command.equals("withdraw")) {
				//                    String accountId = tokens[1];
				//                    int amount = Integer.parseInt(tokens[2]);
				//                    bank.withdraw(accountId, amount);
				//                } else if (command.equals("registerResponse")) {
				//                    for (int i = 1; i < tokens.length; i++) {
				//                        bank.register(tokens[i], this);
				//                    }
				//                } else if (command.equals("getBalance")) {
				//                    String accountId = tokens[1];
				//                    out.write(
				//                        String.format(
				//                            "getBalanceResponse %d",
				//                            bank.getBalance(accountId)));
				//                } else if (command.equals("getBalanceResponse")) {
				//                    System.out.println(String.format("$%d", tokens[1]));
				//                } 
				//                else if (command.equals("snapshot")) {
				//                	UUID initiatorID = UUID.fromString(tokens[1]);
				//                	int futureClock = Integer.parseInt(tokens[2]);
				//                	InitiatorInfo info = new InitiatorInfo(initiatorID, futureClock);
				//                	MatternsAlgorithm.getInstance().setInitiatorInfo(info);
				//                	acknowledgement();
				//                } 
				//                else if (command.equals("acknowledgement")) {
				//
				//                } else {
				//                    // Unknown command
				//                }
			}
		} catch (IOException | UnknownAccountException e) {
			e.printStackTrace();
		}
	}

	//process an input
	public void process(String input) throws IOException, UnknownAccountException {
		System.out.println(input);
		synchronized (MAlg.getInstance().lockObject) {
			Gson gson = new Gson();
			Message message = gson.fromJson(input, Message.class);

			InitiatorInfo info = MAlg.getInstance().getInitiatorInfo();
			//exclude REGISTER and REGISTER_RESPONSE messages
			if (remoteBankID != null) {
				if (info == null) {
					bank.receiveMessageFrom(remoteBankID);
				}
				else if (message.getVectorClock().findTick(info.getInitiatorID()) <
						info.getFutureTick()) {
					//then this is a white message
					bank.receiveMessageFrom(remoteBankID);
				}
			}

			//check only when there is an initiator
			if (info != null) {
				checkTakeSnapshot(message);
				checkFwdWhiteMessage(message);
			}

			//update local vector clock
			VectorClock.getInstance().merge(message.getVectorClock());
			VectorClock.getInstance().tick(bank.getBankID());

			//process messages
			if (message.getCommand() == Command.REGISTER) {
				bank.getRemoteBanks().put(message.getSourceID(), this);
				remoteBankID = message.getSourceID();
				bank.receiveMessageFrom(remoteBankID);
				for (String accountID :message.getAccountIDs()) {
					bank.register(accountID, this);
				}
				VectorClock.getInstance().tick(bank.getBankID());
				Message respMessage = new Message(
						Command.REGISTER_RESPONSE, 
						bank.getBankID(),
						VectorClock.getInstance());

				respMessage.addAccoundIDs(bank.getAccountIds());
				out.write(gson.toJson(respMessage));
				out.newLine();
				out.flush();
				bank.sendMessageTo(remoteBankID);
			}
			else if (message.getCommand() == Command.DEPOSIT) {
				bank.deposit(message.getAccountIDs().get(0), message.getAmount());
			}
			else if (message.getCommand() == Command.WITHDRAW) {
				bank.withdraw(message.getAccountIDs().get(0), message.getAmount());
			}
			else if (message.getCommand() == Command.REGISTER_RESPONSE) {
				bank.getRemoteBanks().put(message.getSourceID(), this);
				remoteBankID = message.getSourceID();
				bank.receiveMessageFrom(remoteBankID);
				bank.sendMessageTo(remoteBankID);
				for (String accountID :message.getAccountIDs()) {
					bank.register(accountID, this);
				}
			}
			else if (message.getCommand() == Command.GET_BALANCE) {
				VectorClock.getInstance().tick(bank.getBankID());
				Message respMessage = new Message(
						Command.GET_BALANCE_RESPONSE, 
						bank.getBankID(),
						VectorClock.getInstance());

				respMessage.setAmount(bank.getBalance(message.getAccountIDs().get(0)));
				out.write(gson.toJson(respMessage));
				out.newLine();
				out.flush();
				bank.sendMessageTo(remoteBankID);
			}
			else if (message.getCommand() == Command.GET_BALANCE_RESPONSE) {
				System.out.println("balance is " + message.getAmount());
			}
			else if (message.getCommand() == Command.TAKE_SNAPSHOT) {
				UUID initiatorID = message.getSourceID();
				long futureTick = message.getFutureTick();
				InitiatorInfo newInfo = new InitiatorInfo(initiatorID, futureTick);
				MAlg.getInstance().setInitiatorInfo(newInfo);

				VectorClock.getInstance().tick(bank.getBankID());
				Message respMessage = new Message(
						Command.ACKNOWLEDGEMENT, 
						bank.getBankID(), 
						VectorClock.getInstance());

				out.write(gson.toJson(respMessage));
				out.newLine();
				out.flush();
				bank.sendMessageTo(remoteBankID);
			}
			else if (message.getCommand() == Command.ACKNOWLEDGEMENT) {
				MAlg.getInstance().receiveAcknowledgement(message.getSourceID());
			}
			else if (message.getCommand() == Command.SNAPSHOT) {
				UUID sourceID = message.getSourceID();
				Snapshot snapshot = message.getSnapshot();
				WhiteMessageHistory history = message.getMessageHistory();
				MAlg.getInstance().getGlobalSnapshots().add(snapshot);
				MAlg.getInstance().getGlobalMessageHistory().put(sourceID, history);
			}
			else if (message.getCommand() == Command.WHITE_MESSAGE) {
				Message whiteMessage = message.getWhiteMessage();
				UUID sourceID = whiteMessage.getSourceID();
				UUID destID = message.getSourceID();
				MAlg.getInstance().getWhiteMessages().add(whiteMessage);
				MAlg.getInstance().updateMessageHistory(sourceID, destID);
			}
			else if (message.getCommand() == Command.DUMMY) {
				//do nothing
			}
			else {
				System.out.println("unknown command from " + message.getSourceID());
			}
		}
	}

	//take snapshot when a white process receives a red message
	private void checkTakeSnapshot(Message message) throws IOException {
		VectorClock clockInMessage = message.getVectorClock();
		UUID initiatorID = MAlg.getInstance().getInitiatorInfo().getInitiatorID();

		boolean whiteProcess = VectorClock.getInstance().findTick(initiatorID) < 
				MAlg.getInstance().getInitiatorInfo().getFutureTick();
		boolean redMessage = clockInMessage.findTick(initiatorID) >= 
				MAlg.getInstance().getInitiatorInfo().getFutureTick();

		if (whiteProcess && redMessage) {
			Snapshot snapshot = MAlg.getInstance().saveState();
			//update local vector clock before send snapshot
			VectorClock.getInstance().merge(message.getVectorClock());
			VectorClock.getInstance().tick(bank.getBankID());
			bank.sendSnapshotToInitiator(snapshot);
		}
	}

	//forward message to initiator when the process is red and message is white
	private void checkFwdWhiteMessage(Message message) throws IOException {
		VectorClock clockInMessage = message.getVectorClock();
		UUID initiatorID = MAlg.getInstance().getInitiatorInfo().getInitiatorID();

		boolean redProcess = VectorClock.getInstance().findTick(initiatorID) >= 
				MAlg.getInstance().getInitiatorInfo().getFutureTick();
		boolean whiteMessage = clockInMessage.findTick(initiatorID) < 
				MAlg.getInstance().getInitiatorInfo().getFutureTick();

		if (redProcess && whiteMessage) {
			//update local vector clock
			VectorClock.getInstance().merge(message.getVectorClock());
			VectorClock.getInstance().tick(bank.getBankID());

			if (bank.getBankID() != initiatorID) {
				bank.sendWhiteMessageToInitiator(message);
			}
			else {
				//this is the initiator, add white message to message history
				MAlg.getInstance().getWhiteMessages().add(message);
				MAlg.getInstance().updateMessageHistory(message.getSourceID(), initiatorID);
			}
		}
	}

	public UUID getBankID() {
		return remoteBankID;
	}

	public void setBankID(UUID bankID) {
		this.remoteBankID = bankID;
	}
}
