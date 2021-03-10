package net.zergrush.stats;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class Statistics {

    public interface Displayer<T> {

        String display(T value);

    }

    public interface ChangeListener<T> {

        void valueChanged(Entry<T> ent);

    }

    public static class Key<T> {

        private final String name;
        private final Class<T> type;

        public Key(String name, Class<T> type) {
            if (name == null || type == null)
                throw new NullPointerException();
            this.name = name;
            this.type = type;
        }

        public String toString() {
            return name;
        }

        public boolean equals(Object other) {
            if (! (other instanceof Key<?>)) return false;
            return ((Key<?>) other).name.equals(name);
        }

        public int hashCode() {
            return name.hashCode();
        }

        public String getName() {
            return name;
        }

        public Class<T> getType() {
            return type;
        }

        protected void ensureMatches(Key<?> other) {
            if (! equals(other) || ! type.equals(other.getType()))
                throw new IllegalArgumentException("Statistics keys do not " +
                    "match: " + debugString(this) + " <--> " +
                    debugString(other));
        }

        protected static String debugString(Key<?> key) {
            if (key == null) return "null";
            return String.format("%s@%x[name=%s,type=%s]",
                key.getClass().getName(), System.identityHashCode(key),
                key.getName(), key.getType());
        }

    }

    public class Entry<T> {

        private final Key<T> key;
        private String description;
        private T value;
        private Displayer<? super T> displayer;
        private final List<ChangeListener<T>> listeners;

        public Entry(Key<T> key, String description, T value,
                     Displayer<? super T> displayer) {
            this.key = key;
            this.description = description;
            this.value = key.getType().cast(value);
            this.displayer = displayer;
            this.listeners = new CopyOnWriteArrayList<>();
        }
        public Entry(Key<T> key, T value) {
            this(key, null, value, null);
        }

        public Statistics getParent() {
            return Statistics.this;
        }

        public void init(String description, Displayer<? super T> displayer) {
            if (description == null || displayer == null)
                throw new NullPointerException();
            if (this.description != null && this.displayer != null)
                throw new IllegalStateException("Statistics entry already " +
                    "initialized");
            this.description = description;
            this.displayer = displayer;
        }

        public Key<T> getKey() {
            return key;
        }

        public String getDescription() {
            return description;
        }

        public T getValue() {
            return value;
        }

        public void setValue(T v) {
            value = key.getType().cast(v);
            fireChangeEvent();
        }

        public Displayer<? super T> getDisplayer() {
            return displayer;
        }

        public String getDisplayedValue() {
            return displayer.display(value);
        }

        public void addListener(ChangeListener<T> l) {
            listeners.add(l);
        }

        public void removeListener(ChangeListener<T> l) {
            listeners.remove(l);
        }

        protected void fireChangeEvent() {
            for (ChangeListener<T> l : listeners) {
                l.valueChanged(this);
            }
        }

    }

    public static final Displayer<Object> DEFAULT_DISPLAYER =
        new Displayer<Object>() {
            public String display(Object value) {
                return String.valueOf(value);
            }
        };

    private final Map<Key<?>, Entry<?>> data;

    public Statistics(Collection<Entry<?>> entries) {
        this.data = new LinkedHashMap<>();
        for (Entry<?> ent : entries) {
            importEntry(ent, data);
        }
    }
    public Statistics(Map<Key<?>, Entry<?>> data) {
        this(validatedEntries(data));
    }
    public Statistics(Statistics copyFrom) {
        this(copyFrom.entries());
    }
    public Statistics() {
        data = new LinkedHashMap<>();
    }

    private <T> void importEntry(Entry<T> ent, Map<Key<?>, Entry<?>> drain) {
        // We need to outline this into a method in order to be able to name
        // the type variable.
        drain.put(ent.getKey(), new Entry<>(ent.getKey(),
            ent.getDescription(), ent.getValue(), ent.getDisplayer()));
    }

    public <T> Entry<T> getEntry(Key<T> key) {
        @SuppressWarnings("unchecked")
        Entry<T> ret = (Entry<T>) data.get(key);
        return ret;
    }

    public Entry<?> getEntryByName(String name) {
        return getEntry(new Key<Object>(name, Object.class));
    }

    public Collection<Entry<?>> entries() {
        return data.values();
    }

    public <T> Entry<T> init(Key<T> key, String description, T defValue,
                             Displayer<? super T> displayer) {
        if (displayer == null) displayer = DEFAULT_DISPLAYER;
        Entry<T> ent = getEntry(key);
        if (ent == null) {
            ent = new Entry<>(key, defValue);
            data.put(key, ent);
        }
        ent.init(description, displayer);
        return ent;
    }

    public <T> T get(Key<T> key) {
        Entry<T> ent = getEntry(key);
        return (ent == null) ? null : ent.getValue();
    }

    public <T> void put(Key<T> key, T value) {
        Entry<T> ent = getEntry(key);
        if (ent == null) {
            ent = new Entry<>(key, value);
            data.put(key, ent);
        } else {
            ent.setValue(value);
        }
    }

    public void remove(Key<?> key) {
        data.remove(key);
    }

    public void clear() {
        data.clear();
    }

    public void increment(Key<Integer> key, int incr) {
        Entry<Integer> ent = getEntry(key);
        ent.setValue(ent.getValue() + incr);
    }

    public void increment(Key<Double> key, double incr) {
        Entry<Double> ent = getEntry(key);
        ent.setValue(ent.getValue() + incr);
    }

    private static Collection<Entry<?>> validatedEntries(
            Map<Key<?>, Entry<?>> data) {
        for (Map.Entry<Key<?>, Entry<?>> ent : data.entrySet()) {
            ent.getKey().ensureMatches(ent.getValue().getKey());
        }
        return data.values();
    }

}
