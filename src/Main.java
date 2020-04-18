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
        Scanner scanner = new Scanner(System.in);
        String command = scanner.nextLine();
        while (!command.equals("exit")) {
            command = scanner.nextLine();
        }
        scanner.close();
    }
}
