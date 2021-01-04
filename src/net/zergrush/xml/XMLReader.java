package net.zergrush.xml;

import java.util.Iterator;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLReader implements Iterable<DataItem> {

    private static class ItemIterator implements Iterator<DataItem> {

        private final DataItem source;
        private final NamedNodeMap attributes;
        private final NodeList children;
        private DataItem next;
        private int attrIndex;
        private int childIndex;

        public ItemIterator(DataItem source) {
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
        this.registry = registry;
    }

    public DataItem load(Element source) {
        iter = new ItemIterator(new DataItem(source));
        return iter.getSource();
    }

    public String getName() {
        return iter.getSource().getName();
    }

    public Iterator<DataItem> iterator() {
        return iter;
    }

    public <T> T read(Class<T> cls, DataItem data) {
        ItemIterator iterBackup = iter;
        try {
            iter = new ItemIterator(data);
            return registry.get(cls).readXML(this);
        } finally {
            iter = iterBackup;
        }
    }

}
