package net.zergrush.stats;

import java.util.NavigableSet;
import java.util.TreeSet;

public class Highscores {

    public static class Entry implements Comparable<Entry> {

        private final Statistics data;

        public Entry(Statistics data) {
            this.data = data.freeze();
        }

        public int compareTo(Entry other) {
            // We sort by *descending* score.
            return Integer.compare(other.getScore(), getScore());
        }

        public Statistics getData() {
            return data;
        }

        public int getScore() {
            Integer value = data.get(GameStatistics.SCORE);
            if (value == null) return -1;
            return value;
        }

    }

    private final NavigableSet<Entry> entries;

    public Highscores() {
        entries = new TreeSet<>();
    }

    public NavigableSet<Entry> getEntries() {
        return entries;
    }

    public void add(Entry ent) {
        entries.add(ent);
    }

}
