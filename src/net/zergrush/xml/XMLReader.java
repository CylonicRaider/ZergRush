package net.zergrush.xml;

import java.util.Iterator;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLReader implements Iterable<DataItem> {

    private static class ItemIterator implements Iterator<DataItem> {

        private final DataItem parent;
        private final NamedNodeMap attributes;
        private final NodeList children;
        private int attrIndex;
        private int childIndex;
        private DataItem next;

        public ItemIterator(DataItem item) {
            parent = item;
            attributes = item.getElementValue().getAttributes();
            children = item.getElementValue().getChildNodes();
            attrIndex = 0;
            childIndex = 0;
            next = null;
            advance();
        }

        private void advance() {
            if (attrIndex < attributes.getLength()) {
                 Attr cur = (Attr) attributes.item(attrIndex++);
                 next = new DataItem(parent, cur.getName(), cur.getValue());
                 return;
            }
            while (childIndex < children.getLength()) {
                Node child = children.item(childIndex++);
                if (! (child instanceof Element)) continue;
                next = new DataItem(parent, child.getNodeName(),
                                    (Element) child);
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

    private DataItem source;

    public XMLReader(DataItem source) {
        this.source = source;
    }

    public Iterator<DataItem> iterator() {
        return new ItemIterator(source);
    }

    public void enter(DataItem item) {
        source = item;
    }

    public void exit() {
        source = source.getParent();
    }

}
