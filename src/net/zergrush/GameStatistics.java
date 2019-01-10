package net.zergrush;

public class GameStatistics extends Statistics {

    public static final Key<Integer> SCORE = intKey("score");

    protected void reset() {
        clear();
        init(SCORE, "Points reached", 0);
    }

    protected static Key<Integer> intKey(String name) {
        return new Key<>(name, Integer.class);
    }

    protected static Key<Double> doubleKey(String name) {
        return new Key<>(name, Double.class);
    }

}
