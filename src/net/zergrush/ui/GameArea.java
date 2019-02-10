package net.zergrush.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import net.zergrush.Game;
import net.zergrush.sprites.Player;
import net.zergrush.sprites.Zerg;

public class GameArea extends JComponent {

    public interface FontSizeListener {

        void onFontSizeChanged(int newSize);

    }

    protected class ResizeListener extends ComponentAdapter {

        public void componentResized(ComponentEvent e) {
            gameArea.setSize(-1, -1);
            repaint();
        }

    }

    private static final long serialVersionUID = -5393846493473658297L;

    public static final double FONT_SIZE = 0.05;
    public static final Color BACKGROUND_COLOR = Color.WHITE;
    public static final Color BORDER_COLOR = new Color(0x101010);

    private static final RenderingHints RENDERING_HINTS;

    static {
        RENDERING_HINTS = new RenderingHints(null);
        RENDERING_HINTS.put(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
    }

    private final Rectangle gameArea;
    private final AffineTransform gameAreaTransform;
    private FontSizeListener fsListener;
    private int fontSize;
    private Game game;

    public GameArea() {
        gameArea = new Rectangle(0, 0, -1, -1);
        gameAreaTransform = new AffineTransform();
        fontSize = -1;
        addComponentListener(new ResizeListener());
    }

    public FontSizeListener getFSListener() {
        return fsListener;
    }

    public void setFSListener(FontSizeListener l) {
        fsListener = l;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game g) {
        game = g;
    }

    public int getFontSize() {
        return fontSize;
    }

    protected void calculateGameArea() {
        if (gameArea.width >= 0) return;
        int width = getWidth(), height = getHeight();
        int size = Math.min(width, height);
        gameArea.setLocation((width - size) / 2, (height - size) / 2);
        gameArea.setSize(size, size);
        double factor = size / 2.0;
        gameAreaTransform.setTransform(factor, 0, 0, factor,
            gameArea.x + factor, gameArea.y + factor);
        updateFontSize();
    }

    protected void updateFontSize() {
        if (gameArea.width < 0) calculateGameArea();
        int newFontSize = (int) (gameArea.width * FONT_SIZE);
        if (newFontSize != fontSize) {
            fontSize = newFontSize;
            if (fsListener != null) fsListener.onFontSizeChanged(newFontSize);
        }
    }

    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        calculateGameArea();
        // Draw game area.
        if (game != null) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.transform(gameAreaTransform);
            g.setClip(-1, -1, 2, 2);
            g.addRenderingHints(RENDERING_HINTS);
            game.draw(g);
            g.dispose();
        }
        // Draw borders.
        graphics.setColor(BORDER_COLOR);
        final int width = getWidth(), height = getHeight();
        if (gameArea.width < width) {
            final int gaRight = gameArea.x + gameArea.width;
            graphics.fillRect(0, 0, gameArea.x, height);
            graphics.fillRect(gaRight, 0, width - gaRight, height);
        }
        if (gameArea.height < height) {
            final int gaBottom = gameArea.y + gameArea.height;
            graphics.fillRect(0, 0, width, gameArea.y);
            graphics.fillRect(0, gaBottom, width, height - gaBottom);
        }
    }

    public void markDamaged(Rectangle2D rect) {
        if (rect == null) {
            repaint();
            return;
        }
        calculateGameArea();
        final double[] coords = { rect.getX(), rect.getY(),
            rect.getX() + rect.getWidth(), rect.getY() + rect.getHeight() };
        gameAreaTransform.transform(coords, 0, coords, 0, 2);
        final int left = (int) Math.floor(coords[0]);
        final int top = (int) Math.floor(coords[1]);
        repaint(0, left, top, (int) Math.ceil(coords[2] - left),
                (int) Math.ceil(coords[3] - top));
    }

    public void doLayout() {
        super.doLayout();
        updateFontSize();
    }

    public static Image renderWindowIcon(int s) {
        BufferedImage result = new BufferedImage(s, s,
                                                 BufferedImage.TYPE_INT_RGB);
        Graphics2D g = result.createGraphics();
        g.addRenderingHints(GameArea.RENDERING_HINTS);
        /* Background */
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, s, s);
        /* Zerg */
        g.setColor(Zerg.COLOR);
        g.fillRect(s / 2, 0, s, s / 2);
        /* Player */
        g.setColor(Player.COLOR);
        g.translate(s / 3.0, s * 2 / 3.0);
        g.scale(s * 3.0, s * 3.0);
        g.fill(Player.SHAPE);
        /* Done */
        g.dispose();
        return result;
    }

}
