package net.zergrush.ui;

import java.awt.BorderLayout;
import java.io.IOException;
import java.net.URL;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.EditorKit;

public class HTMLPane extends JPanel {

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
