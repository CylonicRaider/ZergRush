package net.zergrush.xml;

import org.w3c.dom.Element;

public class DataItem {

    private final DataItem parent;
    private final boolean attribute;
    private final String name;
    private final Object value;

    private DataItem(DataItem parent, boolean attribute, String name,
                     Object value) {
        this.parent = parent;
        this.attribute = attribute;
        this.name = name;
        this.value = value;
    }
    public DataItem(DataItem parent, String name, String value) {
        this(parent, true, name, value);
    }
    public DataItem(DataItem parent, String name, Element value) {
        this(parent, false, name, value);
    }

    public DataItem getParent() {
        return parent;
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
