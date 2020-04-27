
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
		synchronized (MAlgorithm.getInstance().lockObject) {
			this.socket = new Socket(hostname, port);
			this.out = new BufferedWriter(
					new OutputStreamWriter(socket.getOutputStream()));
			this.in = new BufferedReader(
					new InputStreamReader(socket.getInputStream()));
			this.bank = bank;

			Gson gson = new Gson();
			VClock.getInstance().tick(bank.getBankID());
			Message message = new Message(
					Command.REGISTER, bank.getBankID(), 
					VClock.getInstance());
			
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
		VClock.getInstance().tick(bank.getBankID());
		Message message = new Message(
				Command.DEPOSIT, bank.getBankID(), 
				VClock.getInstance());
		
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
		VClock.getInstance().tick(bank.getBankID());
		Message message = new Message(
				Command.WITHDRAW, bank.getBankID(), 
				VClock.getInstance());
		
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
		synchronized (MAlgorithm.getInstance().lockObject) {
			Gson gson = new Gson();
			VClock.getInstance().tick(bank.getBankID());
			Message message = new Message(
					Command.GET_BALANCE, bank.getBankID(), 
					VClock.getInstance());
			
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
		Gson gson = new Gson();
		VClock.getInstance().tick(bank.getBankID());
		Message message = new Message(
				Command.TAKE_SNAPSHOT, bank.getBankID(), 
				VClock.getInstance());
		
		message.setFutureTick(tick);
		out.write(gson.toJson(message));
		out.newLine();
		out.flush();
		bank.sendMessageTo(remoteBankID);
		//    	out.write(String.format("snapshot %s %d", processID, tick));
		//        out.newLine();
		//        out.flush();
	}

	public void sendDummyMsg() throws IOException {
		synchronized (MAlgorithm.getInstance().lockObject) {
			Gson gson = new Gson();
			VClock.getInstance().tick(bank.getBankID());
			Message message = new Message(
					Command.DUMMY, bank.getBankID(), 
					VClock.getInstance());
			
			out.write(gson.toJson(message));
			out.newLine();
			out.flush();
			bank.sendMessageTo(remoteBankID);
		}
	}

	public void sendSnapshotToInitiator(Snapshot snapshot) throws IOException {
		Gson gson = new Gson();
		VClock.getInstance().tick(bank.getBankID());
		Message message = new Message(
				Command.SNAPSHOT, bank.getBankID(), 
				VClock.getInstance());
		
		message.setSnapshot(snapshot);
		message.setMessageHistory(bank.getHistory());
		out.write(gson.toJson(message));
		out.newLine();
		out.flush();
		bank.sendMessageTo(remoteBankID);
	}

	public void sendWhiteMessageToInitiator(Message whiteMessage) throws IOException {
		Gson gson = new Gson();
		VClock.getInstance().tick(bank.getBankID());
		Message message = new Message(
				Command.WHITE_MESSAGE, bank.getBankID(), 
				VClock.getInstance());
		
		message.setWhiteMessage(whiteMessage);
		out.write(gson.toJson(message));
		out.newLine();
		out.flush();
		bank.sendMessageTo(remoteBankID);
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
		synchronized (MAlgorithm.getInstance().lockObject) {
			Gson gson = new Gson();
			Message message = gson.fromJson(input, Message.class);

			InitiatorInfo info = MAlgorithm.getInstance().getInitiatorInfo();
			//exclude REGISTER and REGISTER_RESPONSE messages
			if (remoteBankID != null) {
				if (info == null) {
					//this is a white message
					bank.receiveMessageFrom(remoteBankID);
				}
				else if (message.getVClock().findTick(info.getInitiatorID()) <
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
			VClock.getInstance().merge(message.getVClock());
			VClock.getInstance().tick(bank.getBankID());

			if (message.getCommand() == Command.REGISTER) {
				
				//config this remoteBank
				bank.getRemoteBanks().put(message.getSourceID(), this);
				MAlgorithm.getInstance().notifyInitAck();
				remoteBankID = message.getSourceID();
				bank.receiveMessageFrom(remoteBankID);
				bank.sendMessageTo(remoteBankID); 
				//process register message
				for (String accountID :message.getAccountIDs()) {
					bank.register(accountID, this);
				}
				VClock.getInstance().tick(bank.getBankID());
				Message respMessage = new Message(
						Command.REGISTER_RESPONSE, 
						bank.getBankID(),
						VClock.getInstance());
				
				respMessage.addAccoundIDs(bank.getAccountIds());
				out.write(gson.toJson(respMessage));
				out.newLine();
				out.flush();
			}
			else if (message.getCommand() == Command.DEPOSIT) {
				bank.deposit(message.getAccountIDs().get(0), message.getAmount());
			}
			else if (message.getCommand() == Command.WITHDRAW) {
				bank.withdraw(message.getAccountIDs().get(0), message.getAmount());
			}
			else if (message.getCommand() == Command.REGISTER_RESPONSE) {
				//config this remoteBank
				bank.getRemoteBanks().put(message.getSourceID(), this);
				MAlgorithm.getInstance().notifyInitAck();
				remoteBankID = message.getSourceID();
				bank.receiveMessageFrom(remoteBankID);
				bank.sendMessageTo(remoteBankID);
				//process this register_response message
				for (String accountID :message.getAccountIDs()) {
					bank.register(accountID, this);
				}
			}
			else if (message.getCommand() == Command.GET_BALANCE) {
				VClock.getInstance().tick(bank.getBankID());
				Message respMessage = new Message(
						Command.GET_BALANCE_RESPONSE, 
						bank.getBankID(),
						VClock.getInstance());

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
				MAlgorithm.getInstance().setInitiatorInfo(newInfo);

				VClock.getInstance().tick(bank.getBankID());
				Message respMessage = new Message(
						Command.ACKNOWLEDGEMENT, 
						bank.getBankID(), 
						VClock.getInstance());

				out.write(gson.toJson(respMessage));
				out.newLine();
				out.flush();
				bank.sendMessageTo(remoteBankID);
			}
			else if (message.getCommand() == Command.ACKNOWLEDGEMENT) {
				MAlgorithm.getInstance().receiveAcknowledgement(message.getSourceID());
			}
			else if (message.getCommand() == Command.SNAPSHOT) {
				UUID sourceID = message.getSourceID();
				Snapshot snapshot = message.getSnapshot();
				WhiteMsgHistory history = message.getMessageHistory();
				MAlgorithm.getInstance().getGlobalSnapshots().add(snapshot);
				MAlgorithm.getInstance().accumulateHistories(sourceID, history);
			}
			else if (message.getCommand() == Command.WHITE_MESSAGE) {
				Message whiteMessage = message.getWhiteMessage();
				UUID sourceID = whiteMessage.getSourceID();
				UUID destID = message.getSourceID();
				MAlgorithm.getInstance().getWhiteMessages().add(whiteMessage);
				MAlgorithm.getInstance().accumulateHistories(sourceID, destID);
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
		VClock clockInMessage = message.getVClock();
		UUID initiatorID = MAlgorithm.getInstance().getInitiatorInfo().getInitiatorID();

		boolean whiteProcess = VClock.getInstance().findTick(initiatorID) < 
				MAlgorithm.getInstance().getInitiatorInfo().getFutureTick();
		boolean redMessage = clockInMessage.findTick(initiatorID) >= 
				MAlgorithm.getInstance().getInitiatorInfo().getFutureTick();

		if (whiteProcess && redMessage) {
			//test use
			//simulate a white message in transit
//			bank.broadcastDummyMsg();
			
			Snapshot snapshot = MAlgorithm.getInstance().saveState();
			//update local vector clock before send snapshot
			VClock.getInstance().merge(message.getVClock());
			VClock.getInstance().tick(bank.getBankID());
			bank.sendSnapshotToInitiator(snapshot);
		}
	}
	//forward message to initiator when the process is red and message is white
	private void checkFwdWhiteMessage(Message message) throws IOException {
		VClock clockInMessage = message.getVClock();
		UUID initiatorID = MAlgorithm.getInstance().getInitiatorInfo().getInitiatorID();

		boolean redProcess = VClock.getInstance().findTick(initiatorID) >= 
				MAlgorithm.getInstance().getInitiatorInfo().getFutureTick();
		boolean whiteMessage = clockInMessage.findTick(initiatorID) < 
				MAlgorithm.getInstance().getInitiatorInfo().getFutureTick();

		if (redProcess && whiteMessage) {
			//update local vector clock
			VClock.getInstance().merge(message.getVClock());
			VClock.getInstance().tick(bank.getBankID());

			if (bank.getBankID() != initiatorID) {
				bank.sendWhiteMessageToInitiator(message);
			}
			else {
				//this is the initiator, add white message to message history
				MAlgorithm.getInstance().getWhiteMessages().add(message);
				MAlgorithm.getInstance().accumulateHistories(message.getSourceID(), initiatorID);
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
