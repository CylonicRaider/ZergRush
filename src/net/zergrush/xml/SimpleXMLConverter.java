package net.zergrush.xml;

public abstract class SimpleXMLConverter<T> implements XMLConverter<T> {

    public T readXML(XMLReader source) throws XMLConversionException {
        DataItem value = null;
        for (DataItem di : source) {
            if (! di.isAttribute() || ! di.getName().equals("value"))
                throw new XMLConversionException(
                    "Cannot map complex XML object to simple type");
            if (value != null)
                throw new XMLConversionException(
                    "Multiple values provided for simple type");
            value = di;
        }
        return fromString(value.getAttributeValue());
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
                try {
                    return Integer.valueOf(serialized);
                } catch (NumberFormatException exc) {
                    throw new XMLConversionException(exc);
                }
            }
        });
        reg.add(Long.class, new SimpleXMLConverter<Long>() {
            protected Long fromString(String serialized)
                    throws XMLConversionException {
                try {
                    return Long.valueOf(serialized);
                } catch (NumberFormatException exc) {
                    throw new XMLConversionException(exc);
                }
            }
        });
    }

}
