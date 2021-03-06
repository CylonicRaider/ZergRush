package net.zergrush.stats;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import net.zergrush.xml.DataItem;
import net.zergrush.xml.XMLConversionException;
import net.zergrush.xml.XMLConverter;
import net.zergrush.xml.XMLConverterRegistry;
import net.zergrush.xml.XMLReader;
import net.zergrush.xml.XMLWriter;

public class GameStatistics extends Statistics {

    public interface ResetListener {

        void onStatisticsReset(GameStatistics stats);

    }

    protected static final Set<Key<?>> KEYS;
    protected static final Displayer<Long> DATE_DISPLAYER;
    protected static final Displayer<String> NAME_DISPLAYER;

    static {
        KEYS = new LinkedHashSet<>();
        DATE_DISPLAYER = new SimpleDisplayer<Long>("%tF %<tT %<tZ");
        NAME_DISPLAYER = new SimpleDisplayer<String>("%s", "(N/A)");
        XMLConverterRegistry.getDefault().add(GameStatistics.class,
            new XMLConverter<GameStatistics>() {

                private <T> void decodeEntry(XMLReader rd, Entry<T> ent)
                        throws XMLConversionException {
                    ent.setValue(rd.readOnly("value",
                                             ent.getKey().getType()));
                }

                public GameStatistics readXML(XMLReader rd)
                        throws XMLConversionException {
                    GameStatistics ret = new GameStatistics();
                    for (DataItem item : rd.getItems("stat")) {
                        rd.enter(item);
                        try {
                            Entry<?> ent = ret.getEntryByName(
                                rd.readOnly("key", String.class));
                            if (ent == null) continue;
                            decodeEntry(rd, ent);
                        } finally {
                            rd.exit();
                        }
                    }
                    return ret;
                }

                public void writeXML(XMLWriter wr, GameStatistics value)
                        throws XMLConversionException {
                    for (Entry<?> ent : value.entries()) {
                        wr.enter("stat");
                        wr.write("key", ent.getKey().getName());
                        wr.write("value", ent.getValue());
                        wr.exit();
                    }
                }

            });
    }

    public static final Key<Integer> SCORE = intKey("score");
    public static final Key<Long> STARTED = longKey("started");
    public static final Key<Long> ENDED = longKey("ended");
    public static final Key<String> NAME = stringKey("name");

    private final List<ResetListener> listeners;

    public GameStatistics(Collection<Entry<?>> entries) {
        listeners = new CopyOnWriteArrayList<>();
        resetInner();
        importEntries(entries);
    }
    public GameStatistics(GameStatistics copyFrom) {
        this(copyFrom.entries());
    }
    public GameStatistics() {
        this(Collections.<Entry<?>>emptyList());
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
        init(SCORE, "Points reached", 0, null);
        init(STARTED, "Game started", System.currentTimeMillis(),
             DATE_DISPLAYER);
        init(ENDED, "Game ended", Long.MAX_VALUE, DATE_DISPLAYER);
        init(NAME, "Player name", null, NAME_DISPLAYER);
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
    protected static Key<String> stringKey(String name) {
        return register(new Key<>(name, String.class));
    }

}
