package net.zergrush.xml;

import java.util.Iterator;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLReader implements Iterable<DataItem> {

    private static class ItemIterator implements Iterator<DataItem> {

        private final ItemIterator parent;
        private final DataItem source;
        private final NamedNodeMap attributes;
        private final NodeList children;
        private DataItem next;
        private int attrIndex;
        private int childIndex;

        public ItemIterator(ItemIterator parent, DataItem source) {
            this.parent = parent;
            this.source = source;
            if (source.isAttribute()) {
                attributes = source.getElementValue().getAttributes();
                children = source.getElementValue().getChildNodes();
                advance();
            } else {
                attributes = null;
                children = null;
                next = new DataItem("value", source.getAttributeValue());
            }
        }

        public ItemIterator getParent() {
            return parent;
        }

        public DataItem getSource() {
            return source;
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

    }

    private final XMLConverterRegistry registry;
    private ItemIterator iter;

    public XMLReader(XMLConverterRegistry registry) {
        if (registry == null) throw new NullPointerException();
        this.registry = registry;
        this.iter = null;
    }

    public DataItem load(Element source) {
        return new DataItem(source);
    }

    public void enter(DataItem item) {
        if (item == null) throw new NullPointerException();
        iter = new ItemIterator(iter, item);
    }

    public String getName() {
        return iter.getSource().getName();
    }

    public Iterator<DataItem> iterator() {
        return iter;
    }

    public void exit() {
        iter = iter.getParent();
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
