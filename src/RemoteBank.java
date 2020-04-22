import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class RemoteBank implements Runnable {
    private final Socket socket;
    private final BufferedWriter out;
    private final BufferedReader in;
    private final Bank bank;

    public RemoteBank(String hostname, int port, Bank bank) throws IOException {
        this.socket = new Socket(hostname, port);
        this.out = new BufferedWriter(
            new OutputStreamWriter(socket.getOutputStream()));
        this.in = new BufferedReader(
            new InputStreamReader(socket.getInputStream()));
        this.bank = bank;
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
        out.write(String.format("deposit %s %d", accountId, amount));
        out.newLine();
        out.flush();
    }

    public void withdraw(String accountId, int amount) throws IOException {
        out.write(String.format("withdraw %s %d", accountId, amount));
        out.newLine();
        out.flush();
    }

    public int getBalance(String accountId) {
        return 0;
    }

    @Override
    public void run() {
        String input;
        try {
            while ((input = in.readLine()) != null) {
                String[] tokens = input.split(" ");
                String command = tokens[0];
                if (command == "register") {
                    for (int i = 1; i < tokens.length; i++) {
                        bank.register(tokens[i], this);
                    }
                } else if (command == "deposit") {
                    String accountId = tokens[1];
                    int amount = Integer.parseInt(tokens[2]);
                    bank.deposit(accountId, amount);
                } else if (command == "withdraw") {
                    String accountId = tokens[1];
                    int amount = Integer.parseInt(tokens[2]);
                    bank.withdraw(accountId, amount);
                } else {
                    // Unknown command
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
