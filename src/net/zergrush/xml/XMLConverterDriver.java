package net.zergrush.xml;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XMLConverterDriver {

    private static final XMLConverterDriver DEFAULT =
        new XMLConverterDriver();

    private XMLConverterRegistry registry;
    private XMLIO io;

    public XMLConverterDriver(XMLConverterRegistry registry, XMLIO io) {
        this.registry = ensureNotNull(registry);
        this.io = ensureNotNull(io);
    }
    public XMLConverterDriver() {
        this(XMLConverterRegistry.getDefault(), XMLIO.getDefault());
    }

    public XMLConverterRegistry getRegistry() {
        return registry;
    }
    public void setRegistry(XMLConverterRegistry registry) {
        this.registry = ensureNotNull(registry);
    }

    public XMLIO getIO() {
        return io;
    }
    public void setUI(XMLIO io) {
        this.io = ensureNotNull(io);
    }

    public XMLReader createReader() {
        return new XMLReader(registry);
    }

    public XMLWriter createWriter(Document drain) {
        return new XMLWriter(registry, drain);
    }

    public <T> T read(Element source, String expectedName, Class<T> cls)
            throws XMLConversionException {
        XMLReader rd = createReader();
        DataItem root = rd.load(source);
        if (! root.getName().equals(expectedName))
            throw new XMLConversionException("Expected serialized " +
                expectedName + " object, got " + root.getName());
        return rd.read(root, cls);
    }
    public <T> T read(Document source, String expectedName, Class<T> cls)
            throws XMLConversionException {
        return read(source.getDocumentElement(), expectedName, cls);
    }
    public <T> T read(Reader source, String expectedName, Class<T> cls)
            throws XMLConversionException {
        try {
            return read(io.read(source), expectedName, cls);
        } catch (IOException exc) {
            throw new XMLConversionException(exc);
        }
    }
    public <T> T read(String source, String expectedName, Class<T> cls)
            throws XMLConversionException {
        try {
            return read(io.readString(source), expectedName, cls);
        } catch (IOException exc) {
            throw new XMLConversionException(exc);
        }
    }

    public void write(Document drain, String name, Object value)
            throws XMLConversionException {
        createWriter(drain).write(name, value);
    }
    public void write(Writer drain, String name, Object value)
            throws XMLConversionException {
        Document doc = io.createDocument();
        write(doc, name, value);
        try {
            io.write(doc, drain);
        } catch (IOException exc) {
            throw new XMLConversionException(exc);
        }
    }
    public String write(String name, Object value)
            throws XMLConversionException {
        Document doc = io.createDocument();
        write(doc, name, value);
        try {
            return io.writeString(doc);
        } catch (IOException exc) {
            throw new XMLConversionException(exc);
        }
    }

    private static <T> T ensureNotNull(T obj) {
        if (obj == null) throw new NullPointerException();
        return obj;
    }

    public static XMLConverterDriver getDefault() {
        return DEFAULT;
    }

}
