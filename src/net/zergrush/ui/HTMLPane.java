package net.zergrush.ui;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

public class HTMLPane extends JPanel implements HyperlinkListener,
        PropertyChangeListener {

    public interface TitleChangeListener {

        void titleChanged(HTMLPane pane, String newTitle);

    }

    private static final long serialVersionUID = -1724213395749874932L;

    private final JScrollPane scroller;
    private final JEditorPane content;
    private TitleChangeListener listener;

    public HTMLPane() {
        scroller = new JScrollPane();
        content = createContent();
        createUI();
    }

    protected JEditorPane createContent() {
        JEditorPane ret = new JEditorPane("text/html", "");
        ret.setEditable(false);
        ret.addHyperlinkListener(this);
        ret.addPropertyChangeListener(this);
        return ret;
    }

    protected void createUI() {
        setLayout(new BorderLayout());
        scroller.setViewportView(content);
        add(scroller);
    }

    public JScrollPane getScroller() {
        return scroller;
    }

    public JEditorPane getContent() {
        return content;
    }

    public TitleChangeListener getTitleChangeListener() {
        return listener;
    }

    public void setTitleChangeListener(TitleChangeListener l) {
        listener = l;
    }

    public String getTitle() {
        Document doc = content.getDocument();
        if (doc == null) return null;
        return (String) doc.getProperty(Document.TitleProperty);
    }

    public void hyperlinkUpdate(HyperlinkEvent e) {
        // Code adapted from JEditorPane docs.
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED &&
                e.getSource() == content) {
            if (e instanceof HTMLFrameHyperlinkEvent) {
                HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent) e;
                HTMLDocument doc = (HTMLDocument) content.getDocument();
                doc.processHTMLFrameHyperlinkEvent(evt);
            } else {
                loadPage(e.getURL());
            }
        }
    }

    public void propertyChange(PropertyChangeEvent e) {
        if (e.getSource() == content && e.getPropertyName().equals("page")) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (listener != null)
                        listener.titleChanged(HTMLPane.this, getTitle());
                }
            });
        }
    }

    protected void reset() {
        EditorKit editor = content.getEditorKit();
        content.setDocument(editor.createDefaultDocument());
    }

    public void loadPage(URL url) {
        reset();
        if (url == null) return;
        try {
            content.setPage(url);
        } catch (IOException exc) {
            throw new RuntimeException(exc);
        }
    }

    public void loadPage(String text) {
        reset();
        if (text == null) return;
        content.setText(text);
    }

}
