package net.zergrush.ui;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.html.FormSubmitEvent;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

public class HTMLPane extends JPanel implements HyperlinkListener,
        PropertyChangeListener {

    public interface TitleChangeListener {

        void titleChanged(HTMLPane pane, String newTitle);

    }

    public interface FormSubmitListener {

        void formSubmitted(FormSubmitEvent event, Map<String, String> data);

    }

    private static final long serialVersionUID = -1724213395749874932L;

    private final JScrollPane scroller;
    private final JEditorPane content;
    private TitleChangeListener titleListener;
    private FormSubmitListener formListener;

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
        ret.putClientProperty(JEditorPane.W3C_LENGTH_UNITS, Boolean.TRUE);
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
        return titleListener;
    }

    public void setTitleChangeListener(TitleChangeListener l) {
        titleListener = l;
    }

    public FormSubmitListener getFormSubmitListener() {
        return formListener;
    }

    public void setFormSubmitListener(FormSubmitListener l) {
        formListener = l;
        EditorKit ekt = content.getEditorKit();
        if (ekt instanceof HTMLEditorKit) {
            ((HTMLEditorKit) ekt).setAutoFormSubmission(l == null);
        }
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
            } else if (e instanceof FormSubmitEvent) {
                final FormSubmitEvent evt = (FormSubmitEvent) e;
                final Map<String, String> decodedData = new LinkedHashMap<>();
                for (String item : evt.getData().split("&")) {
                    if (item.isEmpty()) continue;
                    String[] parts = item.split("=", 2);
                    String key, value;
                    try {
                        key = URLDecoder.decode(parts[0], "utf-8");
                        if (parts.length == 2) {
                            value = URLDecoder.decode(parts[1], "utf-8");
                        } else {
                            value = null;
                        }
                    } catch (UnsupportedEncodingException exc) {
                        throw new RuntimeException(exc);
                    }
                    decodedData.put(key, value);
                }
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (formListener != null)
                            formListener.formSubmitted(evt, decodedData);
                    }
                });
            } else {
                loadPage(e.getURL());
            }
        }
    }

    public void propertyChange(PropertyChangeEvent e) {
        if (e.getSource() == content && e.getPropertyName().equals("page")) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (titleListener != null)
                        titleListener.titleChanged(HTMLPane.this, getTitle());
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
