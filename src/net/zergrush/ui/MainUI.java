package net.zergrush.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;
import javax.swing.Timer;
import net.zergrush.Game;
import net.zergrush.GameUI;

public class MainUI extends JPanel implements GameUI {

    private final GameArea gameArea;
    private final JLabel headingMessage;
    private final JLabel textMessage;

    public MainUI() {
        gameArea = new GameArea();
        headingMessage = new JLabel();
        textMessage = new JLabel();
        createUI();
    }

    protected Font getBaseFont() {
        return new Font("Helvetica", Font.PLAIN, 12);
    }

    protected void createUI() {
        setBackground(Color.WHITE);
        setLayout(new OverlayLayout(this));

        Font baseFont = getBaseFont();

        JPanel messageOverlay = new JPanel();
        messageOverlay.setLayout(new BoxLayout(messageOverlay,
                                               BoxLayout.Y_AXIS));

        headingMessage.setAlignmentX(CENTER_ALIGNMENT);
        headingMessage.setFont(baseFont.deriveFont(Font.BOLD)
            .deriveFont(2.0f * baseFont.getSize()));
        headingMessage.setForeground(Color.BLACK);
        messageOverlay.add(headingMessage);

        textMessage.setAlignmentX(CENTER_ALIGNMENT);
        textMessage.setFont(baseFont.deriveFont(Font.ITALIC));
        textMessage.setForeground(Color.BLACK);
        messageOverlay.add(textMessage);

        messageOverlay.setOpaque(false);
        add(messageOverlay);

        add(gameArea);
    }

    public void setGame(Game game) {
        gameArea.setGame(game);
    }

    public void setMessage(String heading, String text) {
        headingMessage.setVisible(heading != null);
        if (heading != null) headingMessage.setText(heading);
        textMessage.setVisible(text != null);
        if (text != null) textMessage.setText(text);
    }

    public void markDamaged(Rectangle2D rect) {
        gameArea.markDamaged(rect);
    }

    // While this is not strictly not a responsibility of the UI, Swing likes
    // to have timers its own way anyway.
    public static void scheduleRepeatedly(final Runnable r, int delay) {
        new Timer(delay, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                r.run();
            }
        }).start();
    }

    public static JFrame createWindow(MainUI ui) {
        JFrame window = new JFrame("Zerg Rush");
        window.add(ui);
        window.setPreferredSize(new Dimension(320, 240));
        window.pack();
        window.setLocationRelativeTo(null);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        return window;
    }

}
