package net.zergrush.xml;

public interface XMLConverter<T> {

    T readXML(XMLReader source) throws XMLConversionException;

    void writeXML(XMLWriter drain, T value) throws XMLConversionException;

}
