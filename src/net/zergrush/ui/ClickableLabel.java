package net.zergrush.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JLabel;

public class ClickableLabel extends JLabel {

    protected class MouseTracker extends MouseAdapter {

        public void mouseClicked(MouseEvent evt) {
            if (evt.getButton() == MouseEvent.BUTTON1 &&
                    evt.getClickCount() == 1) {
                fireActionEvent(evt);
            }
        }

    }

    private static final long serialVersionUID = -6802915363248886563L;

    private final List<ActionListener> listeners;
    private String actionCommand;

    public ClickableLabel(String text) {
        super(text);
        listeners = new CopyOnWriteArrayList<>();
        addMouseListener(new MouseTracker());
    }
    public ClickableLabel() {
        this(null);
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

}
