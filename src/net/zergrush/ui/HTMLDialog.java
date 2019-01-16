package net.zergrush.ui;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.net.URL;
import javax.swing.JDialog;

public class HTMLDialog extends JDialog {

    private static final long serialVersionUID = -4460891275078197826L;

    private final HTMLPane display;

    public HTMLDialog(Dialog owner) {
        super(owner);
    }
    public HTMLDialog(Window owner) {
        super(owner);
    }
    public HTMLDialog(Frame owner) {
        super(owner);
    }

    {
        display = new HTMLPane();
        add(display);
    }

    public HTMLPane getDisplay() {
        return display;
    }

    public void loadPage(URL url) {
        display.loadPage(url);
    }

    public void loadPage(String text) {
        display.loadPage(text);
    }

}
