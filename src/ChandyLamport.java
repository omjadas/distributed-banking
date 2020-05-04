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
    
    // Constructors.
    
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
    
    // Add a bank to the list of connected banks.
    
    public void addBank(String newBank) {
        otherStates.put(newBank, "-");
    }
    
    // Take a string to store the current state of bank.
    
    public void recordState(String currentState) {
        bankState = currentState;
        stateRecorded = true;
    }
    
    // Attempts to send the current state to the other banks.
    
    public void broadCastMarker() {
        try {
            for (Map.Entry<String, String> entry: otherStates.entrySet()) {
                String[] tokens = entry.getKey().split("/");
                Socket socket = new Socket(tokens[0], Integer.parseInt(tokens[1]));
                
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                
                out.writeUTF("chandyLamportMarker " + bankId + " " + bankState);
                
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Start of algorithm.
    
    public boolean startAlgorithm(String currentState) throws IOException {
        if (otherStates.isEmpty()) {
            return false;
        }
        recordState(currentState);
        broadCastMarker();
        return true;
    }
    
    // End of algorithm - call this to erase snapshot.
    
    public void resetAlgorithm() {
        for (Map.Entry<String, String> state : otherStates.entrySet()) {
            otherStates.put(state.getKey(), "-");
        }
        bankState = "-";
        stateRecorded = false;
    }
    
    // Getter for the snapshot.
    
    public HashMap<String, String> getStates() {
        HashMap<String, String> allStates = new HashMap<String, String>(otherStates);
        allStates.put(bankId, bankState);
        return allStates;
    }
    
    // Method for what to do if a bank receives a chandylamport marker.
    
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
            HashMap<String, String> snapshot = getStates();
            for (Map.Entry<String, String> entry: otherStates.entrySet()){
                String branch = entry.getKey();
                String branchState = snapshot.get(branch);
                System.out.println("Branch: " + branch + ", " + "State: " + branchState);
            }
            
            resetAlgorithm();
        }
        
        return finished;
    }
    
}