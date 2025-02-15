import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of vector clock algorithm.
 */
public class VectorClock {
    private static VectorClock vectorClock = null;
    private final HashMap<UUID, Long> vc = new HashMap<>();

    /**
     * Retrieve the singleton VectorClock.
     *
     * @return the singleton VectorClock
     */
    public static VectorClock getInstance() {
        if (vectorClock == null) {
            vectorClock = new VectorClock();
        }

        return vectorClock;
    }

    /**
     * Increment the logic clock of a process by 1.
     *
     * @param pid the ID of process
     */
    public synchronized void tick(UUID pid) {
        if (this.vc.containsKey(pid)) {
            this.vc.put(pid, this.vc.get(pid) + 1);
        } else {
            this.vc.put(pid, (long) 1);
        }
    }

    /**
     * Set a logic clock for a process.
     *
     * @param pid   the ID of the process
     * @param ticks the clock value of the process
     */
    public synchronized void set(UUID pid, Long ticks) {
        this.vc.put(pid, ticks);
    }

    /**
     * Get the clock value of a process.
     *
     * @param pid the ID of the process
     * @return the clock value
     */
    public synchronized long findTick(UUID pid) {
        if (!this.vc.containsKey(pid)) {
            return -1;
        }
        return this.vc.get(pid);
    }

    /**
     * Merge local vector lock with another clock.
     *
     * @param other the other vector clock
     */
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
}
