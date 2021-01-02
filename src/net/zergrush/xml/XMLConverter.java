package net.zergrush.xml;

public interface XMLConverter<T> {

    T readXML(XMLReader source);

    void writeXML(T value, XMLWriter drain);

}
