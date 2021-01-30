package net.zergrush.xml;

public abstract class SimpleXMLConverter<T> implements XMLConverter<T> {

    public T readXML(XMLReader source) throws XMLConversionException {
        DataItem value = source.getOnlyItem("value");
        try {
            return fromString(value.getAttributeValue());
        } catch (IllegalArgumentException exc) {
            throw new XMLConversionException(exc);
        }
    }

    public void writeXML(XMLWriter drain, T value)
            throws XMLConversionException {
        drain.writeValue(toString(value));
    }

    protected abstract T fromString(String serialized)
        throws XMLConversionException;

    protected String toString(T value) throws XMLConversionException {
        return value.toString();
    }

    public static void registerDefaults(XMLConverterRegistry reg) {
        reg.add(Boolean.class, new SimpleXMLConverter<Boolean>() {
            protected Boolean fromString(String serialized) {
                return Boolean.valueOf(serialized);
            }
        });
        reg.add(String.class, new SimpleXMLConverter<String>() {
            protected String fromString(String serialized) {
                return serialized;
            }
        });
        reg.add(Integer.class, new SimpleXMLConverter<Integer>() {
            protected Integer fromString(String serialized)
                    throws XMLConversionException {
                return Integer.valueOf(serialized);
            }
        });
        reg.add(Long.class, new SimpleXMLConverter<Long>() {
            protected Long fromString(String serialized)
                    throws XMLConversionException {
                return Long.valueOf(serialized);
            }
        });
    }

}
