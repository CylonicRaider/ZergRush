package net.zergrush.stats;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameStatistics extends Statistics {

    public interface ResetListener {

        void onStatisticsReset(GameStatistics stats);

    }

    protected static final Set<Key<?>> KEYS;

    static {
        KEYS = new LinkedHashSet<>();
    }

    public static final Key<Integer> SCORE = intKey("score");
    public static final Key<Long> STARTED = longKey("started");
    public static final Key<Long> ENDED = longKey("ended");

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
        resetInner();
        fireResetListeners();
    }

    protected void resetInner() {
        init(SCORE, "Points reached", 0);
        init(STARTED, "Game started", System.currentTimeMillis());
        init(ENDED, "Game ended", Long.MAX_VALUE);
    }

    protected void fireResetListeners() {
        for (ResetListener l : listeners) {
            l.onStatisticsReset(this);
        }
    }

    protected static <T> Key<T> register(Key<T> key) {
        KEYS.add(key);
        return key;
    }
    protected static Key<Integer> intKey(String name) {
        return register(new Key<>(name, Integer.class));
    }
    protected static Key<Long> longKey(String name) {
        return register(new Key<>(name, Long.class));
    }
    protected static Key<Double> doubleKey(String name) {
        return register(new Key<>(name, Double.class));
    }

}
