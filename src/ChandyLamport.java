import java.util.*;
import java.io.*;
import java.net.*;
import java.util.Map.Entry;

// Chandy-Lamport Algorithm and related methods.
// States with "-" imply non-recorded state.

public class ChandyLamport {
    
    private String bankId;
    private HashMap<String, String> states;
    private boolean stateRecorded;
    
    ChandyLamport(String bankId) {
        this.bankId = bankId;
        this.stateRecorded = false;
        this.states = new HashMap<>();
    }
    
    ChandyLamport(String bankId, Set<String> allBankIds) {
        this.bankId = bankId;
        this.stateRecorded = false;
        this.states = new HashMap<>();
        for (String currentBankId : allBankIds) {
            states.put(currentBankId, "-");
        }
    }
    
    public void addBank(String newBank) {
        states.put(newBank, "-");
    }
    
    public void recordState(String currentState) {
        states.put(bankId, currentState);
        stateRecorded = true;
    }
    
    
    public void broadCastMarker() throws IOException {
        // broadcast to every other bank saying something like "chandyLamportMarker potato".
    }
    
    public void startAlgorithm(String currentState) throws IOException {
        recordState(currentState);
        broadCastMarker();
    }
    
    public void resetAlgorithm() {
        for (Map.Entry<String, String> state : states.entrySet()) {
            states.put(state.getKey(), "-");
        }
        stateRecorded = false;
    }
    
    public HashMap<String, String> getStates() {
        return states;
    }
    
    public boolean handleReceivedMarker(String remoteBankId, String receivedMarker, String currentState) throws IOException {
        
        if (stateRecorded) {
            states.put(remoteBankId, receivedMarker);
        }
        else {
            recordState(currentState);
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
    
}