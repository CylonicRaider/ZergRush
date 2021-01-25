package net.zergrush.stats;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class Statistics {

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
        private final List<ChangeListener<T>> listeners;

        public Entry(Key<T> key, String description, T value) {
            this.key = key;
            this.description = description;
            this.value = key.getType().cast(value);
            this.listeners = new CopyOnWriteArrayList<>();
        }
        public Entry(Key<T> key, T value) {
            this(key, null, value);
        }

        public Statistics getParent() {
            return Statistics.this;
        }

        public void init(String description) {
            if (this.description != null)
                throw new IllegalStateException("Statistics entry already " +
                    "initialized");
            this.description = description;
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
            ensureNotFrozen();
            value = key.getType().cast(v);
            fireChangeEvent();
        }

        public void addListener(ChangeListener<T> l) {
            ensureNotFrozen();
            listeners.add(l);
        }

        public void removeListener(ChangeListener<T> l) {
            ensureNotFrozen();
            listeners.remove(l);
        }

        protected void fireChangeEvent() {
            for (ChangeListener<T> l : listeners) {
                l.valueChanged(this);
            }
        }

    }

    private final Map<Key<?>, Entry<?>> data;
    private final boolean frozen;

    public Statistics(Collection<Entry<?>> entries, boolean frozen) {
        Map<Key<?>, Entry<?>> data = new LinkedHashMap<>();
        this.data = Collections.unmodifiableMap(data);
        this.frozen = frozen;
        for (Entry<?> ent : entries) {
            importEntry(data, ent);
        }
    }
    public Statistics(Map<Key<?>, Entry<?>> data, boolean frozen) {
        this(validatedEntries(data), frozen);
    }
    public Statistics() {
        data = new LinkedHashMap<>();
        frozen = false;
    }

    private <T> void importEntry(Map<Key<?>, Entry<?>> data, Entry<T> ent) {
        // We need to outline this into a method in order to be able to name
        // the type variable.
        data.put(ent.getKey(), new Entry<>(ent.getKey(),
            ent.getDescription(), ent.getValue()));
    }

    public <T> Entry<T> getEntry(Key<T> key) {
        @SuppressWarnings("unchecked")
        Entry<T> ret = (Entry<T>) data.get(key);
        return ret;
    }

    public Collection<Entry<?>> entries() {
        return data.values();
    }

    public <T> Entry<T> init(Key<T> key, String description, T defValue) {
        Entry<T> ent = getEntry(key);
        if (ent == null) {
            ent = new Entry<>(key, defValue);
            data.put(key, ent);
        }
        ent.init(description);
        return ent;
    }

    public <T> T get(Key<T> key) {
        Entry<T> ent = getEntry(key);
        return (ent == null) ? null : ent.getValue();
    }

    public <T> void put(Key<T> key, T value) {
        ensureNotFrozen();
        Entry<T> ent = getEntry(key);
        if (ent == null) {
            ent = new Entry<>(key, value);
            data.put(key, ent);
        } else {
            ent.setValue(value);
        }
    }

    public void remove(Key<?> key) {
        ensureNotFrozen();
        data.remove(key);
    }

    public void clear() {
        ensureNotFrozen();
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

    public boolean isFrozen() {
        return frozen;
    }

    public Statistics freeze() {
        return new Statistics(data, true);
    }

    private void ensureNotFrozen() {
        if (frozen)
            throw new UnsupportedOperationException("Modifying a frozen " +
                "Statistics");
    }

    private static Collection<Entry<?>> validatedEntries(
            Map<Key<?>, Entry<?>> data) {
        for (Map.Entry<Key<?>, Entry<?>> ent : data.entrySet()) {
            ent.getKey().ensureMatches(ent.getValue().getKey());
        }
        return data.values();
    }

}
