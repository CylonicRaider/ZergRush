package net.zergrush.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLReader implements Iterable<DataItem> {

    private static class State implements Iterable<DataItem> {

        private class ItemIterator implements Iterator<DataItem> {

            private DataItem next;
            private int attrIndex;
            private int childIndex;

            public ItemIterator(ItemIterator copyFrom) {
                next = copyFrom.next;
                attrIndex = copyFrom.attrIndex;
                childIndex = copyFrom.childIndex;
            }
            public ItemIterator() {
                if (! source.isAttribute()) {
                    advance();
                } else {
                    next = new DataItem("value", source.getAttributeValue());
                    index(next);
                    indexDone = true;
                }
            }

            private void advance() {
                if (source.isAttribute()) {
                    next = null;
                    return;
                }
                next = null;
                if (attrIndex < attributes.getLength()) {
                     Attr cur = (Attr) attributes.item(attrIndex++);
                     next = new DataItem(cur.getName(), cur.getValue());
                }
                while (next == null && childIndex < children.getLength()) {
                    Node child = children.item(childIndex++);
                    if (! (child instanceof Element)) continue;
                    next = new DataItem((Element) child);
                }
                if (next == null) {
                    indexDone = true;
                } else if (! indexDone) {
                    index(next);
                }
            }

            public boolean hasNext() {
                return (next != null);
            }

            public DataItem next() {
                DataItem ret = next;
                advance();
                return ret;
            }

            public void remove() {
                throw new UnsupportedOperationException(
                    "Cannot remove from ItemIterator");
            }

            private void index(DataItem item) {
                List<DataItem> bucket = index.get(item.getName());
                if (bucket == null) {
                    bucket = new ArrayList<>();
                    index.put(item.getName(), bucket);
                }
                bucket.add(item);
            }

        }

        private final State parent;
        private final DataItem source;
        private final NamedNodeMap attributes;
        private final NodeList children;
        private final Map<String, List<DataItem>> index;
        private final ItemIterator iter;
        private boolean indexDone;

        public State(State parent, DataItem source) {
            this.parent = parent;
            this.source = source;
            if (! source.isAttribute()) {
                attributes = source.getElementValue().getAttributes();
                children = source.getElementValue().getChildNodes();
            } else {
                attributes = null;
                children = null;
            }
            // These need to be initialized first as the ItemIterator
            // constructor may advance() immediately.
            index = new LinkedHashMap<>();
            indexDone = false;
            iter = new ItemIterator();
        }

        public State getParent() {
            return parent;
        }

        public DataItem getSource() {
            return source;
        }

        public Iterator<DataItem> iterator() {
            return iter;
        }

        public List<DataItem> getItems(String name) {
            if (name == null) throw new NullPointerException();
            if (! indexDone) {
                ItemIterator consumer = new ItemIterator(iter);
                while (consumer.hasNext()) consumer.next();
            }
            List<DataItem> bucket = index.get(name);
            if (bucket == null) {
                bucket = Collections.emptyList();
            } else {
                bucket = Collections.unmodifiableList(bucket);
            }
            return bucket;
        }

    }

    private final XMLConverterRegistry registry;
    private State state;

    public XMLReader(XMLConverterRegistry registry) {
        if (registry == null) throw new NullPointerException();
        this.registry = registry;
        this.state = null;
    }

    public DataItem load(Element source) {
        return new DataItem(source);
    }

    public void enter(DataItem item) {
        if (item == null) throw new NullPointerException();
        state = new State(state, item);
    }

    public String getName() {
        return state.getSource().getName();
    }

    public Iterator<DataItem> iterator() {
        return state.iterator();
    }

    public List<DataItem> getItems(String name) {
        return state.getItems(name);
    }

    public DataItem getOnlyItem(String name) throws XMLConversionException {
        List<DataItem> bucket = getItems(name);
        if (bucket.size() != 1)
            throw new XMLConversionException("Expected exactly one " + name +
                " member, got " + bucket.size());
        return bucket.get(0);
    }

    public <T> T readAs(Class<T> cls) throws XMLConversionException {
        if (cls == null) throw new NullPointerException();
        return registry.get(cls).readXML(this);
    }

    public <T> T read(DataItem data, Class<T> cls)
            throws XMLConversionException {
        enter(data);
        try {
            return readAs(cls);
        } finally {
            exit();
        }
    }

    public void exit() {
        state = state.getParent();
    }

    public <T> T readOnly(String name, Class<T> cls)
            throws XMLConversionException {
        return read(getOnlyItem(name), cls);
    }

    public <T, U extends Collection<? super T>> U readAll(String name,
            Class<T> cls, U drain) throws XMLConversionException {
        for (DataItem item : getItems(name)) {
            drain.add(read(item, cls));
        }
        return drain;
    }

    public <T> List<T> readAll(String name, Class<T> cls)
            throws XMLConversionException {
        return readAll(name, cls, new ArrayList<T>());
    }

    public <T, U extends Map<String, ? super T>> U readMap(Class<T> cls,
            U drain) throws XMLConversionException {
        for (DataItem item : this) {
            drain.put(item.getName(), read(item, cls));
        }
        return drain;
    }

    public <T> Map<String, T> readMap(Class<T> cls)
            throws XMLConversionException {
        return readMap(cls, new LinkedHashMap<String, T>());
    }

}
