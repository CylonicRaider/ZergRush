package net.zergrush.ui;

import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.BorderFactory;
import javax.swing.JLabel;

public class ClickableLabel extends JLabel {

    protected class MouseTracker extends MouseAdapter {

        public void mouseEntered(MouseEvent evt) {
            setUnderlined(true);
        }

        public void mousePressed(MouseEvent evt) {
            if (evt.getButton() == MouseEvent.BUTTON1)
                setOutline(true);
        }

        public void mouseClicked(MouseEvent evt) {
            if (evt.getButton() == MouseEvent.BUTTON1 &&
                    evt.getClickCount() == 1)
                fireActionEvent(evt);
        }

        public void mouseReleased(MouseEvent evt) {
            if (evt.getButton() == MouseEvent.BUTTON1)
                setOutline(false);
        }

        public void mouseExited(MouseEvent evt) {
            setUnderlined(false);
        }

    }

    private static final long serialVersionUID = -6802915363248886563L;

    private final List<ActionListener> listeners;
    private String actionCommand;

    public ClickableLabel(String text) {
        super(text);
        listeners = new CopyOnWriteArrayList<>();
        createUI();
    }
    public ClickableLabel() {
        this(null);
    }

    protected void createUI() {
        addMouseListener(new MouseTracker());
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setOutline(false);
    }

    public String getActionCommand() {
        return actionCommand;
    }

    public void setActionCommand(String cmd) {
        actionCommand = cmd;
    }

    public void addActionListener(ActionListener l) {
        listeners.add(l);
    }

    public void removeActionListener(ActionListener l) {
        listeners.remove(l);
    }

    public ActionListener[] getActionListeners() {
        return listeners.toArray(new ActionListener[listeners.size()]);
    }

    protected void fireActionEvent(ActionEvent evt) {
        for (ActionListener l : listeners) {
            l.actionPerformed(evt);
        }
    }

    private void fireActionEvent(MouseEvent source) {
        fireActionEvent(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
            actionCommand, source.getWhen(), source.getModifiers()));
    }

    public boolean isUnderlined() {
        Font font = getFont();
        Map<TextAttribute, ?> attrs = font.getAttributes();
        Object value = attrs.get(TextAttribute.UNDERLINE);
        return TextAttribute.UNDERLINE_ON.equals(value);
    }

    public void setUnderlined(boolean underline) {
        Font font = getFont();
        // That API design is wonderful.
        @SuppressWarnings("unchecked")
        Map<TextAttribute, Object> attrs =
            (Map<TextAttribute, Object>) font.getAttributes();
        // There is, of course, no constant for *no* underline.
        attrs.put(TextAttribute.UNDERLINE,
            (underline ? TextAttribute.UNDERLINE_ON : -1));
        setFont(font.deriveFont(attrs));
    }

    public void setOutline(boolean outline) {
        if (outline) {
            setBorder(BorderFactory.createDashedBorder(null));
        } else {
            setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        }
    }

}
