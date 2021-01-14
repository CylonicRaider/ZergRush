package net.zergrush.xml;

import java.util.Collection;
import java.util.Map;

public final class XMLConverters {

    private XMLConverters() {}

    public static <T> Collection<T> readAll(XMLReader rd,
            String requiredName, Class<T> cls, Collection<T> drain)
            throws XMLConversionException {
        if (requiredName == null) throw new NullPointerException();
        for (DataItem item : rd) {
            if (! item.getName().equals(requiredName))
                throw new XMLConversionException("Wrong name in " +
                    cls.getName() + " collection: Expected " + requiredName +
                    "; got " + item.getName());
            drain.add(rd.read(cls, item));
        }
        return drain;
    }

    public static <T> void writeAll(XMLWriter wr, String name,
                                    Collection<T> source)
            throws XMLConversionException {
        for (T item : source) {
            wr.write(name, item);
        }
    }

    public static <T> Map<String, T> readRecord(XMLReader rd,
            Class<T> cls, Map<String, T> drain)
            throws XMLConversionException {
        for (DataItem item : rd) {
            drain.put(item.getName(), rd.read(cls, item));
        }
        return drain;
    }

    public static <T> void writeRecord(XMLWriter wr,
                                       Map<String, T> source)
            throws XMLConversionException {
        for (Map.Entry<String, T> ent : source.entrySet()) {
            wr.write(ent.getKey(), ent.getValue());
        }
    }

}
