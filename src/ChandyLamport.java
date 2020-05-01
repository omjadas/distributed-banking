import java.util.*;
import java.io.*;
import java.net.*;
import java.util.Map.Entry;

// Chandy-Lamport Algorithm and related methods.
// States with "-" imply non-recorded state.

public class ChandyLamport {
    
    private String bankId;
    private String bankState;
    private HashMap<String, String> otherStates;
    private boolean stateRecorded;
    
    ChandyLamport(String bankId) {
        this.bankId = bankId;
        this.bankState = "-";
        this.stateRecorded = false;
        this.otherStates = new HashMap<>();
    }
    
    ChandyLamport(String bankId, Set<String> allBankIds) {
        this.bankId = bankId;
        this.stateRecorded = false;
        this.otherStates = new HashMap<>();
        for (String currentBankId : allBankIds) {
            otherStates.put(currentBankId, "-");
        }
    }
    
    public void addBank(String newBank) {
        otherStates.put(newBank, "-");
    }
    
    public void recordState(String currentState) {
        bankState = currentState;
        stateRecorded = true;
    }
    
    
    public void broadCastMarker() {
        try {
            for (Map.Entry<String, String> entry: otherStates.entrySet()) {
                String[] tokens = entry.getKey().split("/");
                Socket socket = new Socket(tokens[0], Integer.parseInt(tokens[1]));
                
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                
                out.writeUTF("chandyLamportMarker " + bankId + " " + bankState);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void startAlgorithm(String currentState) throws IOException {
        recordState(currentState);
        broadCastMarker();
    }
    
    public void resetAlgorithm() {
        for (Map.Entry<String, String> state : otherStates.entrySet()) {
            otherStates.put(state.getKey(), "-");
        }
        bankState = "-";
        stateRecorded = false;
    }
    
    public HashMap<String, String> getStates() {
        HashMap<String, String> allStates = new HashMap<String, String>(otherStates);
        allStates.put(bankId, bankState);
        return allStates;
    }
    
    public boolean handleReceivedMarker(String remoteBankId, String receivedMarker, String currentState) throws IOException {
        
        if (stateRecorded) {
            otherStates.put(remoteBankId, receivedMarker);
        }
        else {
            recordState(currentState);
            broadCastMarker();
        }
        
        boolean finished = true;
        for (Map.Entry<String, String> state : otherStates.entrySet()) {
            if (state.getValue().equals("-")) {
                finished = false;
            }
        }
        
        if (finished) {
            // INSERT CODE TO DO SOMETHING WITH THE STATES HERE.
            
            resetAlgorithm();
        }
        
        return finished;
    }
    
}