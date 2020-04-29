import java.util.*;
import java.io.*;
import java.net.*;
import java.util.Map.Entry;

// Chandy-Lamport Algorithm and related methods.

public class ChandyLamport implements Runnable {
    
    private String bankId;
    private HashMap<String, String> states;
    private boolean stateRecorded;
    private ServerSocket serverSocket;
    
    ChandyLamport(String myBankId, List<String> allBankIds, int portNumber) throws IOException {
        bankId = myBankId;
        stateRecorded = false;
        serverSocket = new ServerSocket(portNumber);
        for (String currentBankId : allBankIds) {
            states.put(currentBankId, '');
        }
    }
    
    
    public void recordState(String state) {
        states.put(bankId, state);
        stateRecorded = true;
    }
    
    
    public void broadCastMarker() throws IOException {
        
    }
    
    public void startAlgorithm() {
        recordState();
        broadCastMarker();
    }
    
    public void resetAlgorithm() {
        for (Map.Entry<String, String> state : states.entrySet()) {
            states.put(state.getKey(), '');
        }
        stateRecorded = false;
    }
    
    public HashMap<String, String> getStates() {
        return states;
    }
    
    public boolean handleReceivedMarker(String remoteBankId, String marker) throws IOException {
        
        if (stateRecorded) {
            states.put(remoteBankId, marker);
        }
        else {
            recordState();
            broadCastMarker();
        }
        
        boolean finished = true;
        for (Map.Entry<String, String> state : states.entrySet()) {
            if (state.getValue().isEmpty()) {
                finished = false;
            }
        }
        
        if (finished) {
            resetAlgorithm();
        }
        
        return finished;
    }
    
    @Override
    public void run() {
        try {
            Socket socket;
            boolean algorithmFinished = true;
            while (true) {
                socket = serverSocket.accept();
                DataInputStream input = new DataInputStream(socket.getInputStream());
                String msg = input.readUTF();
                String info[] = msg.split("\\|", 0);
                handleReceivedMarker(info[0], info[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}