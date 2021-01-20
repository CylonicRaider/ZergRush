package net.zergrush.xml;

import java.util.ArrayList;
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
                if (source.isAttribute()) {
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
                if (attrIndex < attributes.getLength()) {
                     Attr cur = (Attr) attributes.item(attrIndex++);
                     next = new DataItem(cur.getName(), cur.getValue());
                     return;
                }
                while (childIndex < children.getLength()) {
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
        private final ItemIterator iter;
        private final Map<String, List<DataItem>> index;
        private boolean indexDone;

        public State(State parent, DataItem source) {
            this.parent = parent;
            this.source = source;
            if (source.isAttribute()) {
                attributes = source.getElementValue().getAttributes();
                children = source.getElementValue().getChildNodes();
            } else {
                attributes = null;
                children = null;
            }
            iter = new ItemIterator();
            index = new LinkedHashMap<>();
            indexDone = false;
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

    public void exit() {
        state = state.getParent();
    }

    public <T> T read(Class<T> cls, DataItem data)
            throws XMLConversionException {
        if (cls == null || data == null) throw new NullPointerException();
        enter(data);
        try {
            return registry.get(cls).readXML(this);
        } finally {
            exit();
        }
    }

    public static <T> T read(XMLConverterRegistry registry, Element source,
                             String expectedName, Class<T> cls)
            throws XMLConversionException {
        if (expectedName == null) throw new NullPointerException();
        XMLReader rd = new XMLReader(registry);
        DataItem root = rd.load(source);
        if (! root.getName().equals(expectedName))
            throw new XMLConversionException("Expected serialized " +
                expectedName + " object, got " + root.getName());
        return rd.read(cls, root);
    }
    public static <T> T read(Element source, String expectedName,
                             Class<T> cls) throws XMLConversionException {
        return read(XMLConverterRegistry.DEFAULT, source, expectedName, cls);
    }

}
