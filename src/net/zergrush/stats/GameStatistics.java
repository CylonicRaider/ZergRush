package net.zergrush.stats;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameStatistics extends Statistics {

    public interface ResetListener {

        void onStatisticsReset(GameStatistics stats);

    }

    public static final Key<Integer> SCORE = intKey("score");

    private final List<ResetListener> listeners;

    public GameStatistics() {
        listeners = new CopyOnWriteArrayList<ResetListener>();
    }

    public void addResetListener(ResetListener l) {
        listeners.add(l);
    }

    public void removeResetListener(ResetListener l) {
        listeners.remove(l);
    }

    public void reset() {
        clear();
        init(SCORE, "Points reached", 0);
        fireResetListeners();
    }

    protected void fireResetListeners() {
        for (ResetListener l : listeners) {
            l.onStatisticsReset(this);
        }
    }

    protected static Key<Integer> intKey(String name) {
        return new Key<>(name, Integer.class);
    }

    protected static Key<Double> doubleKey(String name) {
        return new Key<>(name, Double.class);
    }

}
