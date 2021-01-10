package net.zergrush.xml;

import org.w3c.dom.Element;

public class DataItem {

    private final boolean attribute;
    private final String name;
    private final Object value;

    private DataItem(boolean attribute, String name, Object value) {
        if (name == null || value == null) throw new NullPointerException();
        this.attribute = attribute;
        this.name = name;
        this.value = value;
    }
    public DataItem(String name, String value) {
        this(true, name, value);
    }
    public DataItem(Element value) {
        this(false, value.getTagName(), value);
    }

    public boolean isAttribute() {
        return attribute;
    }

    public String getName() {
        return name;
    }

    public String getAttributeValue() {
        if (! attribute)
            throw new IllegalStateException(
                "Retrieving attribute value from non-attribute");
        return (String) value;
    }

    public Element getElementValue() {
        if (attribute)
            throw new IllegalStateException(
                "Retrieving element value from non-element");
        return (Element) value;
    }

}
