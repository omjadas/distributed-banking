import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;
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
    private final Set<String> accountIds = new HashSet<>();

    /**
     * Initialize a remote bank instance, called when making a connection
     * request to another process.
     *
     * @param hostname host name of the other process
     * @param port     port of the other process
     * @param bank     local bank reference
     * @throws IOException if unable to connect to the remote bank
     */
    public RemoteBank(String hostname, int port, Bank bank) throws IOException {
        synchronized (bank) {
            this.socket = new Socket(hostname, port);
            this.out = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream()));
            this.in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            this.bank = bank;
            register();
        }
    }

    /**
     * Initialize a remote bank instance, called when there is a connection
     * request from another process.
     *
     * @param socket socket this remote bank will listen to
     * @param bank   local bank reference
     * @throws IOException if unable to connect to the remote bank
     */
    public RemoteBank(Socket socket, Bank bank) throws IOException {
        this.socket = socket;
        this.out = new BufferedWriter(
            new OutputStreamWriter(socket.getOutputStream()));
        this.in = new BufferedReader(
            new InputStreamReader(socket.getInputStream()));
        this.bank = bank;
    }

    /**
     * Register the local bank and all its accounts with the remote bank.
     *
     * @throws IOException if unable to send message
     */
    public void register() throws IOException {
        synchronized (bank) {
            VectorClock.getInstance().tick(bank.getBankId());
            bank.getmAlgorithm().msgCounter += MAlgorithm.SEND;
            Message message = new Message(
                Command.REGISTER,
                bank.getBankId(),
                VectorClock.getInstance());

            message.addAccountIds(bank.getLocalAccountIds());
            out.write(new Gson().toJson(message));
            out.newLine();
            out.flush();
        }
    }

    /**
     * Deposit to an account, this method will be called when the account is not
     * at the local branch.
     *
     * @param accountId ID of the account to be deposited to
     * @param amount    amount to be deposited
     * @throws IOException if unable to send message
     */
    public void deposit(String accountId, int amount) throws IOException {
        synchronized (bank) {
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
    }

    /**
     * Withdraw from an account, this will be called when the account is not at
     * the local branch.
     *
     * @param accountId ID of the account to be withdrawn from
     * @param amount    amount to be withdrawn
     * @throws IOException if unable to send message
     */
    public void withdraw(String accountId, int amount) throws IOException {
        synchronized (bank) {
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
    }

    /**
     * Print the balance of a remote account.
     *
     * @param accountId ID of the account whose balance should be printed
     * @throws IOException if unable to send message
     */
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

    /**
     * Send a tick to another process.
     *
     * @param tick the vector clock time of snapshot
     * @throws IOException if unable to send message
     */
    public void sendFutureTick(long tick) throws IOException {
        synchronized (bank) {
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
    }

    /**
     * Send a dummy message to another process.
     *
     * @throws IOException if unable to send message
     */
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

    /**
     * Send the local snapshot to initiator after record the local states.
     *
     * @param snapshot the snapshot to be sent
     * @throws IOException if unable to send message
     */
    public void sendSnapshotToInitiator(Snapshot snapshot) throws IOException {
        synchronized (bank) {
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
    }

    /**
     * Send a white message to initiator.
     *
     * @param whiteMessage the white message to be sent
     * @throws IOException if unable to send message
     */
    public void sendWhiteMessageToInitiator(Message whiteMessage)
            throws IOException {
        synchronized (bank) {
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
    }

    /**
     * Send a marker (snapshot) to another process.
     *
     * @param snapshot the snapshot to be sent
     * @throws IOException if unable to send message
     */
    public void sendChandyLamportMarker(Snapshot snapshot) throws IOException {
        synchronized (bank) {
            VectorClock.getInstance().tick(bank.getBankId());
            bank.getmAlgorithm().msgCounter += MAlgorithm.SEND;
            Message message = new Message(
                Command.CHANDY_LAMPORT_MARKER,
                bank.getBankId(),
                VectorClock.getInstance());

            message.setSnapshot(snapshot);
            out.write(new Gson().toJson(message));
            out.newLine();
            out.flush();
        }
    }

    /**
     * Send the chandy lamport reset message.
     *
     * @throws IOException if unable to send the message
     */
    public void resetChandyLamportAlgorithm() throws IOException {
        synchronized (bank) {
            VectorClock.getInstance().tick(bank.getBankId());
            bank.getmAlgorithm().msgCounter += MAlgorithm.SEND;
            Message message = new Message(
                Command.CHANDY_LAMPORT_RESET,
                bank.getBankId(),
                VectorClock.getInstance());
            out.write(new Gson().toJson(message));
            out.newLine();
            out.flush();
        }
    }

    @Override
    public void run() {
        String input;
        try {
            while ((input = in.readLine()) != null && !Thread.interrupted()) {
                process(input);
            }
        } catch (SocketException e) {
            // do nothing
        } catch (IOException | UnknownAccountException e) {
            e.printStackTrace();
            System.out.print("> ");
        } finally {
            try {
                for (String accountId : accountIds) {
                    bank.removeRemoteAccount(accountId);
                }
                bank.removeBank(bankId);
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.print("> ");
            }
        }
    }

    /**
     * Receive and process messages from other processes.
     *
     * @param input JSON string from other processes
     * @throws IOException             if the response message to the sender is
     *                                 unable to be sent
     * @throws UnknownAccountException if a message with an unknown account ID
     *                                 is processed
     */
    public void process(String input) throws IOException,
            UnknownAccountException {
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
                    accountIds.add(accountId);
                    bank.registerRemoteAccount(accountId, this);
                }

                VectorClock.getInstance().tick(bank.getBankId());
                bank.getmAlgorithm().msgCounter += MAlgorithm.SEND;
                Message respMessage = new Message(
                    Command.REGISTER_RESPONSE,
                    bank.getBankId(),
                    VectorClock.getInstance());

                respMessage.addAccountIds(bank.getLocalAccountIds());
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
                    bank.registerRemoteAccount(accountId, this);
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
                System.out.println("\n$" + message.getAmount());
                System.out.print("> ");
            } else if (message.getCommand() == Command.TAKE_SNAPSHOT) {
                // remember the information of the initiator
                UUID initiatorId = message.getSourceId();
                long futureTick = message.getFutureTick();
                InitiatorInfo newInfo = new InitiatorInfo(
                    initiatorId,
                    futureTick);
                bank.getmAlgorithm().setInitiatorInfo(newInfo);

                VectorClock.getInstance().tick(bank.getBankId());
                bank.getmAlgorithm().msgCounter += MAlgorithm.SEND;
                // return an acknowledgement
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
                // update num of snapshots and counter to detect termination
                bank.getmAlgorithm().updateCounter(msgCounter);
                bank.getmAlgorithm().updateNumSnapshot();
            } else if (message.getCommand() == Command.WHITE_MESSAGE) {
                Message whiteMessage = message.getWhiteMessage();
                bank.getmAlgorithm().getWhiteMessages().add(whiteMessage);
                // update counter to detect termination
                bank.getmAlgorithm().updateCounter(MAlgorithm.RECEIVE);
            } else if (message.getCommand() == Command.DUMMY) {
                // do nothing
            } else if (message.getCommand() == Command.CHANDY_LAMPORT_MARKER) {
                bank.handleChandyLamportMarker(
                    message.getSourceId(),
                    message.getSnapshot(),
                    bank.takeSnapshot());
            } else if (message.getCommand() == Command.CHANDY_LAMPORT_RESET) {
                bank.resetChandyLamport();
            } else {
                System.out.println(
                    "\nUnknown command from " + message.getSourceId());
                System.out.print("> ");
            }
        }
    }

    /**
     * Take snapshot when a white process receives a red message.
     *
     * @param message incoming message from other process
     * @throws IOException if unable to send snapshot to the initiator
     */
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

    /**
     * Forward message to initiator when process is red and message is white.
     *
     * @param message incoming message from other processes
     * @throws IOException if unable to send white message to initiator
     */
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
