package net.zergrush.xml;

public interface XMLConverter<T> {

    T readXML(XMLReader source) throws XMLConversionException;

    void writeXML(T value, XMLWriter drain) throws XMLConversionException;

}
