import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.UUID;

import com.google.gson.Gson;

/**
 * Class to interact with remote banks.
 */
public class RemoteBank implements Runnable {
    private final Socket socket;
    private final BufferedWriter out;
    private final BufferedReader in;
    private final Bank bank;
    private UUID bankId;

    public RemoteBank(String hostname, int port, Bank bank) throws IOException {
        synchronized (bank) {
            this.socket = new Socket(hostname, port);
            this.out = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream()));
            this.in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            this.bank = bank;

            VectorClock.getInstance().tick(bank.getBankId());
            bank.getmAlgorithm().msgCounter += MAlgorithm.SEND;
            Message message = new Message(
                Command.REGISTER,
                bank.getBankId(),
                VectorClock.getInstance());

            message.addAccountIds(bank.getAccountIds());
            out.write(new Gson().toJson(message));
            out.newLine();
            out.flush();
        }
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
        VectorClock.getInstance().tick(bank.getBankId());
        bank.getmAlgorithm().msgCounter += MAlgorithm.SEND;
        Message message = new Message(
            Command.DEPOSIT,
            bank.getBankId(),
            VectorClock.getInstance());

        message.addAccountId(accountId);
        message.setAmount(amount);
        out.write(new Gson().toJson(message));
        out.newLine();
        out.flush();
    }

    public void withdraw(String accountId, int amount) throws IOException {
        VectorClock.getInstance().tick(bank.getBankId());
        bank.getmAlgorithm().msgCounter += MAlgorithm.SEND;
        Message message = new Message(
            Command.WITHDRAW,
            bank.getBankId(),
            VectorClock.getInstance());

        message.addAccountId(accountId);
        message.setAmount(amount);
        out.write(new Gson().toJson(message));
        out.newLine();
        out.flush();
    }

    public void printBalance(String accountId) throws IOException {
        synchronized (bank) {
            VectorClock.getInstance().tick(bank.getBankId());
            bank.getmAlgorithm().msgCounter += MAlgorithm.SEND;
            Message message = new Message(
                Command.GET_BALANCE,
                bank.getBankId(),
                VectorClock.getInstance());

            message.addAccountId(accountId);
            out.write(new Gson().toJson(message));
            out.newLine();
            out.flush();
        }
    }

    public void sendFutureTick(long tick) throws IOException {
        VectorClock.getInstance().tick(bank.getBankId());
        bank.getmAlgorithm().msgCounter += MAlgorithm.SEND;
        Message message = new Message(
            Command.TAKE_SNAPSHOT,
            bank.getBankId(),
            VectorClock.getInstance());

        message.setFutureTick(tick);
        out.write(new Gson().toJson(message));
        out.newLine();
        out.flush();
    }

    public void sendDummyMsg() throws IOException {
        synchronized (bank) {
            VectorClock.getInstance().tick(bank.getBankId());
            bank.getmAlgorithm().msgCounter += MAlgorithm.SEND;
            Message message = new Message(
                Command.DUMMY,
                bank.getBankId(),
                VectorClock.getInstance());

            out.write(new Gson().toJson(message));
            out.newLine();
            out.flush();
        }
    }

    public void sendTestMsg() throws IOException {
        synchronized (bank) {
            VectorClock.getInstance().tick(bank.getBankId());
            bank.getmAlgorithm().msgCounter += MAlgorithm.SEND;
            Message message = new Message(
                Command.DUMMY,
                bank.getBankId(),
                VectorClock.getInstance());

            out.write(new Gson().toJson(message));
            out.newLine();
            out.flush();
        }
    }

    public void sendSnapshotToInitiator(Snapshot snapshot) throws IOException {
        VectorClock.getInstance().tick(bank.getBankId());
        bank.getmAlgorithm().msgCounter += MAlgorithm.SEND;
        Message message = new Message(
            Command.SNAPSHOT,
            bank.getBankId(),
            VectorClock.getInstance());

        message.setSnapshot(snapshot);
        message.setMsgCounter(bank.getmAlgorithm().msgCounter);
        out.write(new Gson().toJson(message));
        out.newLine();
        out.flush();
    }

    public void sendWhiteMessageToInitiator(Message whiteMessage) throws IOException {
        VectorClock.getInstance().tick(bank.getBankId());
        bank.getmAlgorithm().msgCounter += MAlgorithm.SEND;
        Message message = new Message(
            Command.WHITE_MESSAGE,
            bank.getBankId(),
            VectorClock.getInstance());

        message.setWhiteMessage(whiteMessage);
        out.write(new Gson().toJson(message));
        out.newLine();
        out.flush();
    }

    public void sendChandyLamportMarker(Snapshot snapshot) throws IOException {
        Message message = new Message(
            Command.CHANDY_LAMPORT_MARKER,
            bank.getBankId(),
            VectorClock.getInstance());

        message.setSnapshot(snapshot);
        out.write(new Gson().toJson(message));
        out.newLine();
        out.flush();
    }

    @Override
    public void run() {
        String input;
        try {
            while ((input = in.readLine()) != null && !Thread.interrupted()) {
                process(input);
            }
        } catch (IOException | UnknownAccountException e) {
            e.printStackTrace();
        }
    }

    // process an input
    public void process(String input) throws IOException, UnknownAccountException {
        synchronized (bank) {
            Gson gson = new Gson();
            Message message = gson.fromJson(input, Message.class);
            InitiatorInfo info = bank.getmAlgorithm().getInitiatorInfo();
            bank.getmAlgorithm().msgCounter += MAlgorithm.RECEIVE;

            // check only when there is an initiator
            if (info != null) {
                checkTakeSnapshot(message);
                checkFwdWhiteMessage(message);
            }

            // update local vector clock
            VectorClock.getInstance().merge(message.getVectorClock());
            VectorClock.getInstance().tick(bank.getBankId());

            if (message.getCommand() == Command.REGISTER) {
                // config this remoteBank
                bank.getRemoteBanks().put(message.getSourceId(), this);
                bank.getmAlgorithm().notifyInitAck();
                bankId = message.getSourceId();

                bank.registerBank(bankId, this);

                // process register message
                for (String accountId : message.getAccountIds()) {
                    bank.registerAccount(accountId, this);
                }

                VectorClock.getInstance().tick(bank.getBankId());
                bank.getmAlgorithm().msgCounter += MAlgorithm.SEND;
                Message respMessage = new Message(
                    Command.REGISTER_RESPONSE,
                    bank.getBankId(),
                    VectorClock.getInstance());

                respMessage.addAccountIds(bank.getAccountIds());
                out.write(gson.toJson(respMessage));
                out.newLine();
                out.flush();
            } else if (message.getCommand() == Command.DEPOSIT) {
                bank.deposit(
                    message.getAccountIds().get(0),
                    message.getAmount());
            } else if (message.getCommand() == Command.WITHDRAW) {
                bank.withdraw(
                    message.getAccountIds().get(0),
                    message.getAmount());
            } else if (message.getCommand() == Command.REGISTER_RESPONSE) {
                // config this remoteBank
                bank.getRemoteBanks().put(message.getSourceId(), this);
                bank.getmAlgorithm().notifyInitAck();
                bankId = message.getSourceId();
                // process this register_response message
                for (String accountId : message.getAccountIds()) {
                    bank.registerAccount(accountId, this);
                }
            } else if (message.getCommand() == Command.GET_BALANCE) {
                VectorClock.getInstance().tick(bank.getBankId());
                bank.getmAlgorithm().msgCounter += MAlgorithm.SEND;
                Message responseMessage = new Message(
                    Command.GET_BALANCE_RESPONSE,
                    bank.getBankId(),
                    VectorClock.getInstance());

                responseMessage.setAmount(
                    bank.getBalance(message.getAccountIds().get(0)));
                out.write(gson.toJson(responseMessage));
                out.newLine();
                out.flush();
            } else if (message.getCommand() == Command.GET_BALANCE_RESPONSE) {
                System.out.println("$" + message.getAmount());
            } else if (message.getCommand() == Command.TAKE_SNAPSHOT) {
                UUID initiatorId = message.getSourceId();
                long futureTick = message.getFutureTick();
                InitiatorInfo newInfo = new InitiatorInfo(
                    initiatorId,
                    futureTick);
                bank.getmAlgorithm().setInitiatorInfo(newInfo);

                VectorClock.getInstance().tick(bank.getBankId());
                bank.getmAlgorithm().msgCounter += MAlgorithm.SEND;
                Message respMessage = new Message(
                    Command.ACKNOWLEDGEMENT,
                    bank.getBankId(),
                    VectorClock.getInstance());

                out.write(gson.toJson(respMessage));
                out.newLine();
                out.flush();
            } else if (message.getCommand() == Command.ACKNOWLEDGEMENT) {
                bank.getmAlgorithm()
                        .receiveAcknowledgement(message.getSourceId());
            } else if (message.getCommand() == Command.SNAPSHOT) {
                Snapshot snapshot = message.getSnapshot();
                int msgCounter = message.getMsgCounter();
                bank.getmAlgorithm().getGlobalSnapshots().add(snapshot);
                bank.getmAlgorithm().updateCounter(msgCounter);
                bank.getmAlgorithm().updateNumSnapshot();
            } else if (message.getCommand() == Command.WHITE_MESSAGE) {
                Message whiteMessage = message.getWhiteMessage();
                bank.getmAlgorithm().getWhiteMessages().add(whiteMessage);
                bank.getmAlgorithm().updateCounter(MAlgorithm.RECEIVE);
            } else if (message.getCommand() == Command.DUMMY) {
                // do nothing
            } else if (message.getCommand() == Command.CHANDY_LAMPORT_MARKER) {
                bank.handleChandyLamportMarker(
                    message.getSourceId(),
                    message.getSnapshot(),
                    bank.takeSnapshot());
            } else {
                System.out.println(
                    "unknown command from " + message.getSourceId());
            }
        }
    }

    // take snapshot when a white process receives a red message
    private void checkTakeSnapshot(Message message) throws IOException {
        VectorClock clockInMessage = message.getVectorClock();
        UUID initiatorId = bank.getmAlgorithm().getInitiatorInfo()
                .getInitiatorId();

        boolean whiteProcess = VectorClock.getInstance()
                .findTick(initiatorId) < bank.getmAlgorithm().getInitiatorInfo()
                        .getFutureTick();
        boolean redMessage = clockInMessage.findTick(initiatorId) >= bank
                .getmAlgorithm().getInitiatorInfo().getFutureTick();

        if (whiteProcess && redMessage) {
            Snapshot snapshot = bank.takeSnapshot();
            // update local vector clock before send snapshot
            VectorClock.getInstance().merge(message.getVectorClock());
            VectorClock.getInstance().tick(bank.getBankId());
            bank.sendSnapshotToInitiator(snapshot);
        }
    }

    // forward message to initiator when the process is red and message is white
    private void checkFwdWhiteMessage(Message message) throws IOException {
        VectorClock clockInMessage = message.getVectorClock();
        UUID initiatorId = bank.getmAlgorithm().getInitiatorInfo()
                .getInitiatorId();

        boolean redProcess = VectorClock.getInstance()
                .findTick(initiatorId) >= bank.getmAlgorithm()
                        .getInitiatorInfo().getFutureTick();
        boolean whiteMessage = clockInMessage.findTick(initiatorId) < bank
                .getmAlgorithm().getInitiatorInfo().getFutureTick();

        if (redProcess && whiteMessage) {
            // update local vector clock
            VectorClock.getInstance().merge(message.getVectorClock());
            VectorClock.getInstance().tick(bank.getBankId());

            if (bank.getBankId() != initiatorId) {
                bank.sendWhiteMessageToInitiator(message);
            } else {
                // this is the initiator, add white message to message history
                bank.getmAlgorithm().getWhiteMessages().add(message);
                bank.getmAlgorithm().updateCounter(MAlgorithm.RECEIVE);
            }
        }
    }

    /**
     * Retrieve the ID of the remote bank.
     *
     * @return the ID of the remote bank
     */
    public UUID getBankId() {
        return bankId;
    }

    /**
     * Set the ID of the remote bank.
     *
     * @param bankId the new ID of the remote bank
     */
    public void setBankId(UUID bankId) {
        this.bankId = bankId;
    }
}
