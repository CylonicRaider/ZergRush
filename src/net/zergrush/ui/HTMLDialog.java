package net.zergrush.ui;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.event.HyperlinkEvent;

public class HTMLDialog extends JDialog
        implements HTMLPane.TitleChangeListener, HTMLPane.PageActionListener {

    private static final long serialVersionUID = -4460891275078197826L;

    private final HTMLPane display;
    private HTMLPane.PageActionListener listener;

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
        display.setTitleChangeListener(this);
        display.setPageActionListener(this);
        closeOnEscape(this);
    }

    public HTMLPane getDisplay() {
        return display;
    }

    public HTMLPane.PageActionListener getPageActionListener() {
        return listener;
    }

    public void setPageActionListener(HTMLPane.PageActionListener l) {
        listener = l;
    }

    public void titleChanged(HTMLPane pane, String newTitle) {
        setTitle(newTitle);
    }

    public boolean onPageActionInvoked(HTMLPane pane, HyperlinkEvent evt,
            String url, Map<String, String> formData) {
        if (url.startsWith("window:")) {
            if (url.equals("window:close")) {
                MainUI.closeWindow(this);
            }
            return true;
        }
        if (listener != null) {
            return listener.onPageActionInvoked(pane, evt, url, formData);
        }
        return false;
    }

    private static void closeOnEscape(final JDialog dialog) {
        dialog.getRootPane().registerKeyboardAction(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    MainUI.closeWindow(dialog);
                }
            },
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

}
