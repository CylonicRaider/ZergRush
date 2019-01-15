package net.zergrush.ui;

import java.awt.BorderLayout;
import java.io.IOException;
import java.net.URL;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.EditorKit;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

public class HTMLPane extends JPanel implements HyperlinkListener {

    private static final long serialVersionUID = -1724213395749874932L;

    private final JScrollPane scroller;
    private final JEditorPane content;

    public HTMLPane() {
        scroller = new JScrollPane();
        content = createContent();
        createUI();
    }

    protected JEditorPane createContent() {
        JEditorPane ret = new JEditorPane("text/html", "");
        ret.setEditable(false);
        ret.addHyperlinkListener(this);
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

    public void hyperlinkUpdate(HyperlinkEvent e) {
        // Code adapted from JEditorPane docs.
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            JEditorPane pane = (JEditorPane) e.getSource();
            if (e instanceof HTMLFrameHyperlinkEvent) {
                HTMLFrameHyperlinkEvent  evt = (HTMLFrameHyperlinkEvent) e;
                HTMLDocument doc = (HTMLDocument) pane.getDocument();
                doc.processHTMLFrameHyperlinkEvent(evt);
            } else {
                loadPage(e.getURL());
            }
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
