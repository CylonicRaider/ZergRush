package net.zergrush.xml;

import java.util.HashMap;
import java.util.Map;

public class XMLConverterRegistry {

    public static final XMLConverterRegistry DEFAULT;

    static {
        DEFAULT = new XMLConverterRegistry();
        SimpleXMLConverter.registerDefaults(DEFAULT);
    }

    private final Map<Class<?>, XMLConverter<?>> converters;

    public XMLConverterRegistry() {
        converters = new HashMap<>();
    }

    public <T> XMLConverter<T> get(Class<T> cls) {
        @SuppressWarnings("unchecked")
        XMLConverter<T> ret = (XMLConverter<T>) converters.get(cls);
        return ret;
    }

    public <T> void add(Class<T> cls, XMLConverter<T> converter) {
        converters.put(cls, converter);
    }

}
