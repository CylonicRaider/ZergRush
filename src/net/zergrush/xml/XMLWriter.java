package net.zergrush.xml;

import org.w3c.dom.Element;

public class XMLWriter {

    private Element drain;

    public XMLWriter(Element drain) {
        this.drain = drain;
    }

    public void enterElement(String name) {
        Element newElem = drain.getOwnerDocument().createElement(name);
        drain.appendChild(newElem);
        drain = newElem;
    }

    public void writeAttribute(String name, String value) {
        drain.setAttribute(name, value);
    }

    public void exitElement() {
        drain = (Element) drain.getParentNode();
    }

}
