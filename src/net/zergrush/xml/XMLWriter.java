package net.zergrush.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XMLWriter {

    private class ItemWriter {

        private final ItemWriter parent;
        private final String name;
        private final Map<String, List<DataItem>> buffer;

        public ItemWriter(ItemWriter parent, String name) {
            if (name == null) throw new NullPointerException();
            this.parent = parent;
            this.name = name;
            this.buffer = new LinkedHashMap<>();
        }

        public ItemWriter getParent() {
            return parent;
        }

        public String getName() {
            return name;
        }

        public void add(DataItem item) {
            if (! buffer.containsKey(item.getName()))
                buffer.put(item.getName(), new ArrayList<DataItem>());
            buffer.get(item.getName()).add(item);
        }

        public void aggregateInto(Element drain) {
            if (! drain.getTagName().equals(name))
                throw new RuntimeException("Wrong XML element name " +
                    "(expected " + name + "; got " + drain.getTagName() +
                    ")");
            for (Map.Entry<String, List<DataItem>> pair : buffer.entrySet()) {
                List<DataItem> items = pair.getValue();
                if (items.size() == 1 && items.get(0).isAttribute()) {
                    drain.setAttribute(pair.getKey(),
                                       items.get(0).getAttributeValue());
                    continue;
                }
                for (DataItem item : items) {
                    if (item.isAttribute()) {
                        Element inflatedItem = document.createElement(
                            pair.getKey());
                        inflatedItem.setAttribute("value",
                                                  item.getAttributeValue());
                        drain.appendChild(inflatedItem);
                    } else {
                        drain.appendChild(item.getElementValue());
                    }
                }
            }
        }

        public DataItem aggregate(boolean forceElement) {
            if (! forceElement && buffer.size() == 1) {
                Map.Entry<String, List<DataItem>> pair =
                    buffer.entrySet().iterator().next();
                if (pair.getKey().equals("value") &&
                        pair.getValue().size() == 1 &&
                        pair.getValue().get(0).isAttribute()) {
                    return new DataItem(name,
                        pair.getValue().get(0).getAttributeValue());
                }
            }
            Element res = document.createElement(name);
            aggregateInto(res);
            return new DataItem(res);
        }

    }

    private final XMLConverterRegistry registry;
    private final Document document;
    private ItemWriter curWriter;

    public XMLWriter(XMLConverterRegistry registry, Document document) {
        if (registry == null || document == null)
            throw new NullPointerException();
        this.registry = registry;
        this.document = document;
        this.curWriter = null;
    }

    public void enter(String name) throws XMLConversionException {
        if (name == null) throw new NullPointerException();
        if (curWriter == null && document.getDocumentElement() != null &&
                ! document.getDocumentElement().getTagName().equals(name))
            throw new XMLConversionException("Document root has wrong name " +
                "(expected " + name + "; got " +
                document.getDocumentElement().getTagName() + ")");
        curWriter = new ItemWriter(curWriter, name);
    }

    public void writeValue(String value) throws XMLConversionException {
        if (value == null) return;
        curWriter.add(new DataItem("value", value));
    }

    private <T> void dispatchWrite(T value) throws XMLConversionException {
        // This one's nasty: From the type checker's perspective, T can be any
        // supertype of the runtime class of value; thus, value.getClass()
        // need not be a Class<T> at all. We, of course, know that the
        // XMLConverter retrieved for value's runtime class will be compatible
        // with value, but that is not expressible in Java's type system.
        @SuppressWarnings("unchecked")
        Class<T> cls = (Class<T>) value.getClass();
        XMLConverter<T> converter = registry.get(cls);
        if (converter == null)
            throw new XMLConversionException("Cannot convert type " +
                cls.getName());
        converter.writeXML(this, value);
    }

    public void writeAs(Object value) throws XMLConversionException {
        if (value == null) return;
        dispatchWrite(value);
    }

    public void write(String name, Object value)
            throws XMLConversionException {
        if (value == null) return;
        enter(name);
        try {
            writeAs(value);
        } finally {
            exit();
        }
    }

    public void exit() {
        ItemWriter parent = curWriter.getParent();
        if (parent == null) {
            Element root = document.getDocumentElement();
            if (root == null) {
                root = document.createElement(curWriter.getName());
                document.appendChild(root);
            }
            curWriter.aggregateInto(root);
        } else {
            parent.add(curWriter.aggregate(false));
        }
        curWriter = parent;
    }

    public void writeAll(String name, Collection<?> items)
            throws XMLConversionException {
        for (Object item : items) {
            write(name, item);
        }
    }

    public void writeMap(Map<String, ?> items)
            throws XMLConversionException {
        for (Map.Entry<String, ?> ent : items.entrySet()) {
            write(ent.getKey(), ent.getValue());
        }
    }

}
