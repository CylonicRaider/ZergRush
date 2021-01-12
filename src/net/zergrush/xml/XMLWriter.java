package net.zergrush.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XMLWriter {

    private final XMLConverterRegistry registry;
    private final Document document;
    private Map<String, List<DataItem>> buffer;

    public XMLWriter(XMLConverterRegistry registry, Document document) {
        if (registry == null || document == null)
            throw new NullPointerException();
        this.registry = registry;
        this.document = document;
    }

    private DataItem aggregateBuffer(String name, boolean forceElement) {
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
    private void write(String name, Object value, boolean forceElement)
            throws XMLConversionException {
        if (name == null) throw new NullPointerException();
        if (value == null) return;
        Map<String, List<DataItem>> bufferBackup = buffer;
        try {
            buffer = new LinkedHashMap<>();
            dispatchWrite(value);
            if (bufferBackup == null) {
                document.appendChild(aggregateBuffer(name, true)
                    .getElementValue());
            } else {
                add(bufferBackup, aggregateBuffer(name, forceElement));
            }
        } finally {
            buffer = bufferBackup;
        }
    }

    public void writeValue(String value) throws XMLConversionException {
        if (value == null) return;
        add(buffer, new DataItem("value", value));
    }

    public void write(String name, Object value)
            throws XMLConversionException {
        write(name, value, false);
    }

    public void writeItem(String name, Object value)
            throws XMLConversionException {
        write(name, value, true);
    }

    private static void add(Map<String, List<DataItem>> buffer,
                            DataItem newItem) {
        if (! buffer.containsKey(newItem.getName()))
            buffer.put(newItem.getName(), new ArrayList<DataItem>());
        buffer.get(newItem.getName()).add(newItem);
    }

    public static <T> void write(XMLConverterRegistry registry,
                                 Document drain, String name, T value)
            throws XMLConversionException {
        XMLWriter wr = new XMLWriter(registry, drain);
        wr.write(name, value);
    }
    public static <T> void write(Document drain, String name, T value)
            throws XMLConversionException {
        write(XMLConverterRegistry.DEFAULT, drain, name, value);
    }

}
