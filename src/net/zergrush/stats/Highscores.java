package net.zergrush.stats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;
import net.zergrush.xml.XMLConversionException;
import net.zergrush.xml.XMLConverter;
import net.zergrush.xml.XMLConverterRegistry;
import net.zergrush.xml.XMLReader;
import net.zergrush.xml.XMLWriter;

public class Highscores {

    public static class Entry implements Comparable<Entry> {

        private final GameStatistics data;

        public Entry(GameStatistics data) {
            this.data = new GameStatistics(data.entries());
        }

        public int compareTo(Entry other) {
            // We sort by descending score and ascending date; thus, matching
            // a score present in the highscores might not be sufficient to
            // be added to them.
            if (getScore() != other.getScore())
                return Integer.compare(other.getScore(), getScore());
            return Long.compare(getDate(), other.getDate());
        }

        public GameStatistics getData() {
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

        public String getName() {
            return data.get(GameStatistics.NAME);
        }

    }

    public static class NumberedEntry extends Entry {

        private final int index;

        public NumberedEntry(GameStatistics data, int index) {
            super(data);
            this.index = index;
        }

        public int getIndex() {
            return index;
        }

    }

    static {
        XMLConverterRegistry.getDefault().add(Entry.class,
            new XMLConverter<Entry>() {

                public Entry readXML(XMLReader rd)
                        throws XMLConversionException {
                    return new Entry(rd.readAs(GameStatistics.class));
                }

                public void writeXML(XMLWriter wr, Entry ent)
                        throws XMLConversionException {
                    wr.writeAs(ent.getData());
                }

            });
        XMLConverterRegistry.getDefault().add(Highscores.class,
            new XMLConverter<Highscores>() {

                public Highscores readXML(XMLReader rd)
                        throws XMLConversionException {
                    Highscores hs = new Highscores();
                    hs.addAll(rd.readAll("entry", Entry.class));
                    return hs;
                }

                public void writeXML(XMLWriter wr, Highscores hs)
                        throws XMLConversionException {
                    wr.writeAll("entry", hs.getEntries());
                }

            });
    }

    public static final int MAX_SIZE = 10;

    private final NavigableSet<Entry> entries;

    public Highscores() {
        entries = new TreeSet<>();
    }

    public NavigableSet<Entry> getEntries() {
        return entries;
    }

    public List<NumberedEntry> getTopEntries(int amount) {
        List<NumberedEntry> ret = new ArrayList<>(amount);
        Iterator<Entry> topEntries = getEntries().iterator();
        while (topEntries.hasNext() && ret.size() < amount) {
            ret.add(new NumberedEntry(topEntries.next().getData(),
                                      ret.size()));
        }
        return ret;
    }

    public void clear() {
        entries.clear();
    }

    public void add(Entry ent) {
        entries.add(ent);
    }

    public void addAll(Collection<Entry> ents) {
        entries.addAll(ents);
    }

}
