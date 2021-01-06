package net.zergrush.xml;

public abstract class SimpleXMLConverter<T> implements XMLConverter<T> {

    public T readXML(XMLReader source) {
        DataItem value = null;
        for (DataItem di : source) {
            if (! di.isAttribute() || ! di.getName().equals("value"))
                throw new IllegalArgumentException(
                    "Cannot map complex XML object to simple type");
            if (value != null)
                throw new IllegalArgumentException(
                    "Multiple values provided for simple type");
            value = di;
        }
        return fromString(value.getAttributeValue());
    }

    public void writeXML(T value, XMLWriter drain) {
        drain.writeValue(toString(value));
    }

    protected abstract T fromString(String serialized);

    protected String toString(T value) {
        return value.toString();
    }

    public static void registerDefaults(XMLConverterRegistry reg) {
        reg.add(String.class, new SimpleXMLConverter<String>() {
            protected String fromString(String serialized) {
                return serialized;
            }
        });
        reg.add(Integer.class, new SimpleXMLConverter<Integer>() {
            protected Integer fromString(String serialized) {
                return Integer.valueOf(serialized);
            }
        });
        reg.add(Long.class, new SimpleXMLConverter<Long>() {
            protected Long fromString(String serialized) {
                return Long.valueOf(serialized);
            }
        });
    }

}
