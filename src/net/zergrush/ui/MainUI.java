package net.zergrush.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import net.zergrush.Game;
import net.zergrush.GameUI;
import net.zergrush.GameStatistics;
import net.zergrush.Statistics;

public class MainUI extends JPanel implements GameUI,
        GameArea.FontSizeListener, GameStatistics.ResetListener {

    protected class KeyTracker extends KeyAdapter {

        public void keyPressed(KeyEvent e) {
            keyStates.put(e.getKeyCode(), KEY_PRESSED_INITIAL);
        }

        public void keyReleased(KeyEvent e) {
            keyStates.remove(e.getKeyCode());
        }

    }

    private final GameArea gameArea;
    private final JLabel headingMessage;
    private final JLabel textMessage;
    private final JLabel scoreMessage;
    private final Map<Integer, Integer> keyStates;

    public MainUI() {
        gameArea = new GameArea();
        headingMessage = new JLabel();
        textMessage = new JLabel();
        scoreMessage = new JLabel();
        keyStates = new HashMap<>();
        createUI();
    }

    protected Font getBaseFont() {
        int fontSize = gameArea.getFontSize();
        if (fontSize == -1) fontSize = 12;
        return new Font(Font.SANS_SERIF, Font.PLAIN, fontSize);
    }

    protected void createUI() {
        setBackground(Color.WHITE);
        setLayout(new OverlayLayout(this));
        addKeyListener(new KeyTracker());

        JPanel messageOverlay = new JPanel();
        messageOverlay.setLayout(new BoxLayout(messageOverlay,
                                               BoxLayout.Y_AXIS));

        headingMessage.setAlignmentX(CENTER_ALIGNMENT);
        headingMessage.setForeground(Color.BLACK);
        messageOverlay.add(headingMessage);

        textMessage.setAlignmentX(CENTER_ALIGNMENT);
        textMessage.setForeground(Color.BLACK);
        messageOverlay.add(textMessage);

        messageOverlay.setOpaque(false);
        add(messageOverlay);

        JPanel scoreLayer = new JPanel();
        scoreLayer.setLayout(new BorderLayout());

        scoreMessage.setHorizontalAlignment(JLabel.RIGHT);
        scoreMessage.setVerticalAlignment(JLabel.TOP);
        scoreMessage.setForeground(Color.BLACK);
        scoreLayer.add(scoreMessage);

        scoreLayer.setOpaque(false);
        add(scoreLayer);

        gameArea.setFSListener(this);
        add(gameArea);

        updateFonts();
    }

    protected void updateFonts() {
        Font baseFont = getBaseFont();
        headingMessage.setFont(baseFont.deriveFont(Font.BOLD)
            .deriveFont(2.0f * baseFont.getSize()));
        textMessage.setFont(baseFont.deriveFont(Font.ITALIC));
        scoreMessage.setFont(baseFont.deriveFont(0.75f * baseFont.getSize()));
    }

    public void onFontSizeChanged(int newSize) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                updateFonts();
                invalidate();
            }
        });
    }

    public void onStatisticsReset(GameStatistics stats) {
        stats.getEntry(GameStatistics.SCORE).addListener(
            new Statistics.ChangeListener<Integer>() {
                public void valueChanged(Statistics.Entry<Integer> ent) {
                    updateScoreText();
                }
            }
        );
    }

    private GameStatistics getStatistics() {
        return gameArea.getGame().getStats();
    }

    public void setGame(Game game) {
        gameArea.setGame(game);
        game.getStats().addResetListener(this);
    }

    public void onGameStateChange() {
        Game.State st = gameArea.getGame().getState();
        switch (st) {
            case PLAYING: case OVER:
                updateScoreText();
                break;
            default:
                scoreMessage.setText("");
                break;
        }
    }

    public void setMessage(String heading, String text) {
        headingMessage.setVisible(heading != null);
        if (heading != null) headingMessage.setText(heading);
        textMessage.setVisible(text != null);
        if (text != null) textMessage.setText(text);
    }

    protected void updateScoreText() {
        int score = getStatistics().get(GameStatistics.SCORE);
        scoreMessage.setText("Score: " + score);
    }

    public void markDamaged(Rectangle2D rect) {
        gameArea.markDamaged(rect);
    }

    public void update() {
        for (Map.Entry<Integer, Integer> ent : keyStates.entrySet()) {
            if (ent.getValue() == KEY_PRESSED_INITIAL)
                ent.setValue(KEY_PRESSED);
        }
    }

    public int getKeyStatus(int keyCode) {
        Integer ret = keyStates.get(keyCode);
        if (ret == null) return KEY_RELEASED;
        return ret;
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
        ui.setPreferredSize(new Dimension(480, 480));
        window.add(ui);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ui.requestFocusInWindow();
        return window;
    }

}
