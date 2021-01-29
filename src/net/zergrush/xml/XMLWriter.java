package net.zergrush.xml;

import java.io.IOException;
import java.io.Writer;
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

        public void add(DataItem item) {
            if (! buffer.containsKey(item.getName()))
                buffer.put(item.getName(), new ArrayList<DataItem>());
            buffer.get(item.getName()).add(item);
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
            for (Map.Entry<String, List<DataItem>> pair : buffer.entrySet()) {
                List<DataItem> items = pair.getValue();
                if (items.size() == 1 && items.get(0).isAttribute()) {
                    res.setAttribute(pair.getKey(),
                                     items.get(0).getAttributeValue());
                    continue;
                }
                for (DataItem item : items) {
                    if (item.isAttribute()) {
                        Element inflatedItem = document.createElement(
                            pair.getKey());
                        inflatedItem.setAttribute("value",
                                                  item.getAttributeValue());
                        res.appendChild(inflatedItem);
                    } else {
                        res.appendChild(item.getElementValue());
                    }
                }
            }
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

    public void enter(String name) {
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
        registry.get(cls).writeXML(this, value);
    }
    public void write(String name, Object value)
            throws XMLConversionException {
        if (name == null) throw new NullPointerException();
        if (value == null) return;
        enter(name);
        try {
            dispatchWrite(value);
        } finally {
            exit();
        }
    }

    public void exit() {
        ItemWriter parent = curWriter.getParent();
        if (parent == null) {
            document.appendChild(curWriter.aggregate(true).getElementValue());
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

    public static <T> String write(XMLConverterRegistry registry, String name,
                                   T value)
            throws XMLConversionException {
        XMLIO io = XMLIO.getDefault();
        Document doc = io.createDocument();
        write(registry, doc, name, value);
        try {
            return io.writeString(doc);
        } catch (IOException exc) {
            throw new XMLConversionException(exc);
        }
    }
    public static <T> void write(XMLConverterRegistry registry, Writer drain,
                                 String name, T value)
            throws XMLConversionException {
        XMLIO io = XMLIO.getDefault();
        Document doc = io.createDocument();
        write(registry, doc, name, value);
        try {
            io.write(doc, drain);
        } catch (IOException exc) {
            throw new XMLConversionException(exc);
        }
    }
    public static <T> void write(XMLConverterRegistry registry,
                                 Document drain, String name, T value)
            throws XMLConversionException {
        XMLWriter wr = new XMLWriter(registry, drain);
        wr.write(name, value);
    }

}
