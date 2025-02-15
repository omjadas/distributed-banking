import java.io.IOException;
import java.util.Scanner;
import java.util.UUID;

/**
 * Main class.
 */
public class Main implements Runnable {
    private final Bank bank;

    /**
     * Create a bank, and initialise algorithms.
     *
     * @param port port to start listening socket on
     * @throws IOException if unable to open socket
     */
    public Main(int port) throws IOException {
        bank = new Bank(UUID.randomUUID(), port);
    }

    /**
     * Entry point.
     *
     * @param args command line arguments
     * @throws IOException if unable to open socket
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Port number must be provided");
            System.exit(1);
        }
        Main main = new Main(Integer.parseInt(args[0]));
        new Thread(main).start();
    }

    /**
     * Run the banking system.
     */
    @Override
    public void run() {
        Thread bankThread = new Thread(bank);
        bankThread.start();
        Scanner scanner = new Scanner(System.in);
        while (!Thread.interrupted()) {
            System.out.print("> ");
            String input = scanner.nextLine();
            String[] tokens = input.split(" ");
            String command = tokens[0];

            if (command.equals("deposit")) {
                if (tokens.length < 3) {
                    System.out.println(
                        "Please provide an account ID and deposit amount");
                    continue;
                }

                String accountId = tokens[1];
                int amount = Integer.parseInt(tokens[2]);
                try {
                    bank.deposit(accountId, amount);
                } catch (IOException e) {
                    System.out.println(
                        String.format(
                            "Unable to deposit $%d into account %s",
                            accountId,
                            amount));
                } catch (UnknownAccountException e) {
                    System.out.println(e.getMessage());
                }
            } else if (command.equals("withdraw")) {
                if (tokens.length < 3) {
                    System.out.println(
                        "Please provide an account ID and withdrawal amount");
                    continue;
                }

                String accountId = tokens[1];
                int amount = Integer.parseInt(tokens[2]);
                try {
                    bank.withdraw(accountId, amount);
                } catch (IOException e) {
                    System.out.println(
                        String.format(
                            "Unable to withdraw $%d from account %s",
                            accountId,
                            amount));
                } catch (UnknownAccountException e) {
                    System.out.println(e.getMessage());
                }
            } else if (command.equals("transfer")) {
                if (tokens.length < 4) {
                    System.out.println(
                        "Please provide a source ID, destination ID and " +
                            "transfer amount");
                    continue;
                }

                String sourceId = tokens[1];
                String destId = tokens[2];
                int amount = Integer.parseInt(tokens[3]);
                try {
                    bank.transfer(sourceId, destId, amount);
                } catch (IOException e) {
                    System.out.println(
                        String.format(
                            "Unable to transfer $%d from account %s to " +
                                "account %s",
                            amount,
                            sourceId,
                            destId));
                } catch (UnknownAccountException e) {
                    System.out.println(e.getMessage());
                }
            } else if (command.equals("open")) {
                if (tokens.length < 2) {
                    System.out.println("Please provide an account ID");
                    continue;
                }

                String accountId = tokens[1];
                try {
                    bank.open(accountId);
                } catch (IOException e) {
                    System.out.println("Unable to open account " + accountId);
                }
            } else if (command.equals("connect")) {
                if (tokens.length < 3) {
                    System.out.println(
                        "Please provide a hostname and port number");
                    continue;
                }

                String hostname = tokens[1];
                int port = Integer.parseInt(tokens[2]);
                try {
                    bank.connect(hostname, port);
                } catch (IOException e) {
                    System.out.println(
                        String.format(
                            "Unable to connect to bank %s:%d",
                            hostname,
                            port));
                }
            } else if (command.equals("balance")) {
                if (tokens.length < 2) {
                    System.out.println("Please provide an account ID");
                    continue;
                }

                String accountId = tokens[1];
                try {
                    bank.printBalance(accountId);
                } catch (IOException e) {
                    System.out.println(
                        "Unable to print balance for " + accountId);
                }
            } else if (command.equals("list")) {
                for (String accountId : bank.getLocalAccountIds()) {
                    System.out.println("Account: " + accountId);
                }
                for (String accountId : bank.getRemoteAccountIds()) {
                    System.out.println("Account: " + accountId);
                }
            } else if (command.equals("exit")) {
                break;
            } else if (command.equals("delay")) {
                // simulate a delay of this process for 10 seconds
                synchronized (bank) {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        System.out.println("Unable to sleep.");
                    }
                }
            } else if (command.equals("mattern")) {
                try {
                    bank.getmAlgorithm().initSnapshot();
                } catch (InterruptedException e) {
                    System.out
                            .println("Unable to initiate Mattern's algorithm");
                }
            } else if (command.equals("chandy-lamport")) {
                try {
                    bank.startChandyLamport();
                } catch (IOException e) {
                    System.out.println(
                        "Unable to complete the snapshot process.");
                }
            } else {
                System.out.println("Unknown command");
            }
        }
        bankThread.interrupt();
        scanner.close();
        System.exit(0);
    }
}
