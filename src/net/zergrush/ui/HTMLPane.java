package net.zergrush.ui;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
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

    public interface PageRenderer {

        URL getBaseURL();

        String renderPage(HTMLPane pane, String pageName, Object data);

    }

    public interface TitleChangeListener {

        void titleChanged(HTMLPane pane, String newTitle);

    }

    public interface PageActionListener {

        boolean onPageActionInvoked(HTMLPane pane, HyperlinkEvent event,
                                    String url, Map<String, String> formData);

    }

    private static final long serialVersionUID = -1724213395749874932L;

    private final JScrollPane scroller;
    private final JEditorPane content;
    private final Map<String, PageRenderer> pageRenderers;
    private TitleChangeListener titleListener;
    private PageActionListener actionListener;
    private String lastTitle;

    public HTMLPane() {
        scroller = new JScrollPane();
        content = createContent();
        pageRenderers = new HashMap<>();
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

    public PageActionListener getPageActionListener() {
        return actionListener;
    }

    public void setPageActionListener(PageActionListener l) {
        actionListener = l;
    }

    public String getTitle() {
        Document doc = content.getDocument();
        if (doc == null) return null;
        return (String) doc.getProperty(Document.TitleProperty);
    }

    protected void updateTitle() {
        String newTitle = getTitle();
        if ((newTitle == null) ? lastTitle == null :
                                 newTitle.equals(lastTitle))
            return;
        lastTitle = newTitle;
        if (titleListener != null)
            titleListener.titleChanged(this, newTitle);
    }

    public void hyperlinkUpdate(HyperlinkEvent e) {
        // Code adapted from JEditorPane docs.
        if (e.getEventType() != HyperlinkEvent.EventType.ACTIVATED)
            return;
        if (e instanceof HTMLFrameHyperlinkEvent) {
            HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent) e;
            HTMLDocument doc = (HTMLDocument) content.getDocument();
            if (! evt.getTarget().equals("_self")) {
                doc.processHTMLFrameHyperlinkEvent(evt);
                return;
            }
        }
        Map<String, String> formData = null;
        if (e instanceof FormSubmitEvent) {
            FormSubmitEvent evt = (FormSubmitEvent) e;
            formData = new LinkedHashMap<>();
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
                formData.put(key, value);
            }
        }
        boolean consumed = false;
        if (actionListener != null)
            consumed = actionListener.onPageActionInvoked(this, e,
                e.getDescription(), formData);
        if (! consumed)
            loadPage(e.getURL());
    }

    public void propertyChange(PropertyChangeEvent e) {
        if (e.getSource() == content && e.getPropertyName().equals("page")) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    updateTitle();
                }
            });
        }
    }

    public boolean hasPageRenderer(String name) {
        return pageRenderers.containsKey(name);
    }

    public PageRenderer getPageRenderer(String name) {
        return pageRenderers.get(name);
    }

    public void addPageRenderer(String name, PageRenderer gen) {
        pageRenderers.put(name, gen);
    }

    public void removePageRenderer(String name) {
        pageRenderers.remove(name);
    }

    protected void reset() {
        EditorKit editor = content.getEditorKit();
        content.setDocument(editor.createDefaultDocument());
        if (editor instanceof HTMLEditorKit) {
            ((HTMLEditorKit) editor).setAutoFormSubmission(false);
        }
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
        if (text != null) content.setText(text);
        updateTitle();
    }

    public void loadGeneratedPage(String name, Object data) {
        reset();
        if (name != null) {
            PageRenderer gen = getPageRenderer(name);
            if (gen != null) {
                URL baseURL = gen.getBaseURL();
                if (baseURL != null &&
                        content.getDocument() instanceof HTMLDocument)
                    ((HTMLDocument) content.getDocument()).setBase(baseURL);
                content.setText(gen.renderPage(this, name, data));
            }
        }
        updateTitle();
    }

}
