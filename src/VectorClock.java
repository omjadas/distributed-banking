import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VectorClock {
    private static VectorClock vectorClock = null;
    private final HashMap<UUID, Long> vc = new HashMap<>();

    public static VectorClock getInstance() {
        if (vectorClock == null) {
            vectorClock = new VectorClock();
        }

        return vectorClock;
    }

    // this method is called when send or receive a message
    public synchronized void tick(UUID pid) {
        if (this.vc.containsKey(pid)) {
            this.vc.put(pid, this.vc.get(pid) + 1);
        } else {
            this.vc.put(pid, (long) 1);
        }
    }

    public synchronized void set(UUID pid, Long ticks) {
        this.vc.put(pid, ticks);
    }

    public synchronized long findTick(UUID pid) {
        if (!this.vc.containsKey(pid)) {
            return -1;
        }
        return this.vc.get(pid);
    }

    public HashMap<UUID, Long> getVc() {
        return vc;
    }

    // merge local lock with another clock
    public synchronized void merge(VectorClock other) {
        for (Map.Entry<UUID, Long> clock : other.vc.entrySet()) {
            Long time = this.vc.get(clock.getKey());
            if (time == null) {
                this.vc.put(clock.getKey(), clock.getValue());
            } else if (time < clock.getValue()) {
                this.vc.put(clock.getKey(), clock.getValue());
            }
        }
    }

    public String returnVCString() {
        int mapSize = this.vc.size();
        int i = 0;
        StringBuilder vcString = new StringBuilder();
        vcString.append("{");
        for (Map.Entry<UUID, Long> clock : this.vc.entrySet()) {
            vcString.append("\"");
            vcString.append(clock.getKey());
            vcString.append("\":");
            vcString.append(clock.getValue());
            if (i < mapSize - 1) {
                vcString.append(", ");
            }
            i++;
        }
        vcString.append("}");
        return vcString.toString();
    }

}
