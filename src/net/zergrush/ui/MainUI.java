package net.zergrush.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.OverlayLayout;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.HyperlinkEvent;
import net.zergrush.Game;
import net.zergrush.GameUI;
import net.zergrush.InfoScreenRequest;
import net.zergrush.KeyboardAction;
import net.zergrush.stats.GameStatistics;
import net.zergrush.stats.Statistics;

public class MainUI extends JPanel implements GameUI,
        GameArea.FontSizeListener, GameStatistics.ResetListener,
        HTMLPane.PageActionListener {

    protected class EventTracker extends KeyAdapter implements FocusListener {

        public void keyPressed(KeyEvent e) {
            keyStates.put(e.getKeyCode(), KEY_PRESSED_INITIAL);
        }

        public void keyReleased(KeyEvent e) {
            keyStates.remove(e.getKeyCode());
        }

        public void focusGained(FocusEvent e) {}

        public void focusLost(FocusEvent e) {
            // After losing focus, we may not receive key release events;
            // clear all key states to prevent perceivedly stuck keys.
            keyStates.clear();
        }

    }

    private static final long serialVersionUID = -2329967477365368049L;

    public static final int WINDOW_ICON_SIZE = 128;

    private final GameArea gameArea;
    private final JLabel headingMessage;
    private final JPanel actionPanel;
    private final JLabel scoreMessage;
    private final Map<Integer, Integer> keyStates;
    private HTMLDialog dialog;
    private InfoScreenRequest dialogRequest;

    public MainUI() {
        gameArea = new GameArea();
        headingMessage = new JLabel();
        actionPanel = new JPanel();
        scoreMessage = new JLabel();
        keyStates = new HashMap<>();
        dialog = null;
        dialogRequest = null;
        createUI();
    }

    protected Font getBaseFont() {
        int fontSize = gameArea.getFontSize();
        if (fontSize == -1) fontSize = 12;
        return new Font(Font.SANS_SERIF, Font.PLAIN, fontSize);
    }

    protected void createUI() {
        setBackground(GameArea.BORDER_COLOR);
        setLayout(new SquareLayout());
        EventTracker et = new EventTracker();
        addKeyListener(et);
        addFocusListener(et);

        JPanel gameLayers = new JPanel();
        gameLayers.setBackground(GameArea.BACKGROUND_COLOR);
        gameLayers.setLayout(new OverlayLayout(gameLayers));

        JPanel messageOverlay = new JPanel();
        messageOverlay.setLayout(new GridLayout(2, 1));

        headingMessage.setHorizontalAlignment(JLabel.CENTER);
        headingMessage.setVerticalAlignment(JLabel.BOTTOM);
        headingMessage.setForeground(Color.BLACK);
        messageOverlay.add(headingMessage);

        actionPanel.setLayout(new FlowLayout());
        actionPanel.setOpaque(false);
        messageOverlay.add(actionPanel);

        messageOverlay.setOpaque(false);
        gameLayers.add(messageOverlay);

        JPanel scoreLayer = new JPanel();
        scoreLayer.setLayout(new BorderLayout());

        scoreMessage.setHorizontalAlignment(JLabel.RIGHT);
        scoreMessage.setVerticalAlignment(JLabel.TOP);
        scoreMessage.setForeground(Color.BLACK);
        scoreLayer.add(scoreMessage);

        scoreLayer.setOpaque(false);
        gameLayers.add(scoreLayer);

        gameArea.setFSListener(this);
        gameLayers.add(gameArea);

        add(gameLayers);

        updateFonts();
    }

    protected void updateFonts() {
        Font baseFont = getBaseFont();
        headingMessage.setFont(baseFont.deriveFont(Font.BOLD)
            .deriveFont(2.0f * baseFont.getSize()));
        scoreMessage.setFont(baseFont.deriveFont(0.75f * baseFont.getSize()));
        Font labelFont = baseFont.deriveFont(Font.ITALIC);
        for (Component comp : actionPanel.getComponents()) {
            comp.setFont(labelFont);
        }
        Graphics g = getGraphics();
        if (g == null) return;
        FontMetrics metrics = g.getFontMetrics(labelFont);
        FlowLayout layout = (FlowLayout) actionPanel.getLayout();
        // This *should* be about 1em... if I use the API correctly.
        layout.setHgap(Math.max(metrics.getHeight(), 1));
    }

    protected void registerPageRenderers(HTMLPane pane) {
        pane.addPageRenderer("statistics", new StatisticsPageRenderer());
        pane.addPageRenderer("highscores", new HighscoresPageRenderer());
    }

    public HTMLDialog getHTMLDialog() {
        if (dialog == null) {
            dialog = new HTMLDialog(SwingUtilities.getWindowAncestor(this));
            dialog.getDisplay().setPreferredSize(getSize());
            dialog.pack();
            dialog.setLocationRelativeTo(dialog.getOwner());
            dialog.setPageActionListener(this);
            registerPageRenderers(dialog.getDisplay());
        }
        return dialog;
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

    public boolean onPageActionInvoked(HTMLPane pane, HyperlinkEvent event,
            String url, Map<String, String> formData) {
        String newLocation = null;
        if (url != null && url.startsWith("info:")) {
            newLocation = url.substring(5);
        }
        InfoScreenRequest newRequest = null;
        if (dialogRequest != null) {
            newRequest = dialogRequest.onDone(newLocation, formData);
        }
        if (newRequest == null && newLocation != null) {
            newRequest = new InfoScreenRequest(newLocation);
        }
        showInfoScreen(newRequest);
        return true;
    }

    public void setGame(Game game) {
        gameArea.setGame(game);
        game.getStats().addResetListener(this);
    }

    public void onGameStateChange() {
        Game.State st = gameArea.getGame().getState();
        switch (st) {
            case PLAYING: case PAUSED: case OVER:
                updateScoreText();
                break;
            default:
                scoreMessage.setText("");
                break;
        }
    }

    public void setMessage(String heading, KeyboardAction... actions) {
        headingMessage.setVisible(heading != null);
        if (heading != null) headingMessage.setText(heading);
        actionPanel.removeAll();
        final Font labelFont = getBaseFont().deriveFont(Font.ITALIC);
        for (int i = 0; i < actions.length; i++) {
            final KeyboardAction act = actions[i];
            ClickableLabel label = new ClickableLabel(
                act.getKeyDescription() + " \u2014 " + act.getDescription());
            label.setFont(labelFont);
            label.setForeground(Color.BLACK);
            label.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    KeyStroke ks = KeyStroke.getKeyStroke(act.getKey());
                    final int keyCode = ks.getKeyCode();
                    keyStates.put(keyCode, KEY_PRESSED_INITIAL);
                    gameArea.getGame().runAfterNextUpdate(new Runnable() {
                        public void run() {
                            keyStates.remove(keyCode);
                        }
                    });
                }
            });
            actionPanel.add(label);
        }
    }

    private GameStatistics getStatistics() {
        return gameArea.getGame().getStats();
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

    public void showInfoScreen(String name) {
        showInfoScreen(gameArea.getGame().getInfoScreenData(name));
    }

    public void showInfoScreen(InfoScreenRequest req) {
        HTMLDialog d = getHTMLDialog();
        if (req == null) {
            closeWindow(d);
            return;
        }
        String name = req.getPageName();
        if (d.getDisplay().hasPageRenderer(name)) {
            d.getDisplay().loadGeneratedPage(name, req.getPageData());
        } else {
            d.getDisplay().loadPage(getClass().getResource("/res/" + name +
                                                           ".html"));
        }
        dialogRequest = req;
        d.setVisible(true);
        d.getDisplay().autofocus();
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

    public static void closeWindow(Window win) {
        // This ensures that a JFrame's DefaultCloseOperation is invoked.
        win.dispatchEvent(new WindowEvent(win, WindowEvent.WINDOW_CLOSING));
    }

    public static JFrame createWindow(MainUI ui) {
        JFrame window = new JFrame("Zerg Rush");
        ui.setPreferredSize(new Dimension(480, 480));
        window.add(ui);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setIconImage(GameArea.renderWindowIcon(WINDOW_ICON_SIZE));
        ui.requestFocusInWindow();
        return window;
    }

}
