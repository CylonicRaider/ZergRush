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
            // We sort by descending score and ascending date; thus, matching
            // a score present in the highscores might not be sufficient to
            // be added to them.
            if (getScore() != other.getScore())
                return Integer.compare(other.getScore(), getScore());
            return Long.compare(getDate(), other.getDate());
        }

        public Statistics getData() {
            return data;
        }

        public int getScore() {
            Integer value = data.get(GameStatistics.SCORE);
            if (value == null) return -1;
            return value;
        }

        public long getDate() {
            Long value = data.get(GameStatistics.ENDED);
            if (value == null) return Long.MAX_VALUE;
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
