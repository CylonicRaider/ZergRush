package net.zergrush.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import javax.swing.JComponent;
import net.zergrush.Game;

public class GameArea extends JComponent {

    protected class ResizeListener extends ComponentAdapter {

        public void componentResized(ComponentEvent e) {
            gameArea.setSize(-1, -1);
            repaint();
        }

    }

    private static final Color BORDER_COLOR = new Color(0x404040);

    private final Rectangle gameArea;
    private final AffineTransform gameAreaTransform;
    private Game game;

    public GameArea() {
        gameArea = new Rectangle(0, 0, -1, -1);
        gameAreaTransform = new AffineTransform();
        addComponentListener(new ResizeListener());
    }

    public void setGame(Game g) {
        game = g;
    }

    protected void calculateGameArea() {
        if (gameArea.width >= 0) return;
        final int width = getWidth(), height = getHeight();
        final int size = Math.min(width, height);
        gameArea.setLocation((width - size) / 2, (height - size) / 2);
        gameArea.setSize(size, size);
        final double factor = size / 2.0;
        gameAreaTransform.setTransform(factor, 0, 0, factor,
            gameArea.x + factor, gameArea.y + factor);
    }

    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        calculateGameArea();
        // Draw game area.
        if (game != null) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.transform(gameAreaTransform);
            g.setClip(-1, -1, 2, 2);
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
        calculateGameArea();
        final double[] coords = { rect.getX(), rect.getY(),
            rect.getX() + rect.getWidth(), rect.getY() + rect.getHeight() };
        gameAreaTransform.transform(coords, 0, coords, 0, 2);
        final int left = (int) Math.floor(coords[0]);
        final int top = (int) Math.floor(coords[1]);
        repaint(0, left, top, (int) Math.ceil(coords[2] - left),
                (int) Math.ceil(coords[3] - top));
    }

}
