package net.zergrush;

import java.util.Collection;
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

        public Key<T> getKey() {
            return key;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String d) {
            description = d;
        }

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            value = key.getType().cast(value);
            fireChangeEvent();
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

    private final Map<Key<?>, Entry<?>> data;

    public Statistics() {
        data = new LinkedHashMap<>();
    }

    public <T> Entry<T> getEntry(Key<T> key) {
        @SuppressWarnings("unchecked")
        Entry<T> ret = (Entry<T>) data.get(key);
        return ret;
    }

    public Collection<Entry<?>> entries() {
        return data.values();
    }

    public <T> void init(Key<T> key, String description, T value) {
        if (data.put(key, new Entry<T>(key, description, value)) != null)
            throw new IllegalStateException("Initializing already-existing " +
                "entry for key " + key);
    }

    public <T> T get(Key<T> key) {
        Entry<T> ent = getEntry(key);
        return (ent == null) ? null : ent.getValue();
    }

    public <T> void put(Key<T> key, T value) {
        Entry<T> ent = getEntry(key);
        if (ent == null) {
            ent = new Entry<T>(key, value);
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

}
