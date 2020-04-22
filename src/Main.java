import java.io.IOException;
import java.util.Scanner;

public class Main implements Runnable {
    private final Bank bank;

    public Main(int port) throws IOException {
        bank = new Bank(port);
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Port number must be provided");
            System.exit(1);
        }

        Main main = new Main(Integer.parseInt(args[0]));
        new Thread(main).start();
    }

    @Override
    public void run() {
        new Thread(bank).start();
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        while (!input.equals("exit")) {
            String[] tokens = input.split(" ");
            String command = tokens[0];

            if (command.equals("deposit")) {
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
                String sourceId = tokens[1];
                String destId = tokens[2];
                int amount = Integer.parseInt(tokens[3]);
                try {
                    bank.transfer(sourceId, destId, amount);
                } catch (IOException e) {
                    System.out.println(
                        String.format(
                            "Unable to transfer $%d from account %s to account %s",
                            amount,
                            sourceId,
                            destId));
                } catch (UnknownAccountException e) {
                    System.out.println(e.getMessage());
                }
            } else if (command.equals("open")) {
                String accountId = tokens[1];
                bank.open(accountId);
            } else if (command.equals("connect")) {
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
            } else {
                System.out.println("Unknown command");
            }

            input = scanner.nextLine();
        }
        scanner.close();
    }
}
