import java.io.IOException;
import java.util.Scanner;

public class Main implements Runnable {
    private Bank bank;

    public Main() throws IOException {
        bank = new Bank();
    }

    public static void main(String[] args) throws IOException {
        Main main = new Main();
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

            if (command == "deposit") {
                String accountId = tokens[1];
                int amount = Integer.parseInt(tokens[2]);
                try {
                    bank.deposit(accountId, amount);
                } catch (IOException e) {
                    System.out.println(
                        String.format(
                            "Unable to deposit $%d from account %s",
                            accountId,
                            amount));
                }
            } else if (command == "withdraw") {
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
                }
            }

            input = scanner.nextLine();
        }
        scanner.close();
    }
}
