package net.zergrush;

import java.awt.geom.AffineTransform;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JComponent;

public class GameUI extends JComponent {

    protected class ResizeListener extends ComponentAdapter {

        public void componentResized(ComponentEvent e) {
            gameArea.setSize(-1, -1);
        }

    }

    private static final Color BORDER_COLOR = new Color(0x404040);

    private final Rectangle gameArea;
    private final AffineTransform gameAreaTransform;

    public GameUI() {
        gameArea = new Rectangle(0, 0, -1, -1);
        gameAreaTransform = new AffineTransform();
        addComponentListener(new ResizeListener());
    }

    protected void calculateGameArea() {
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
        if (gameArea.width == -1) calculateGameArea();
        Graphics2D g = (Graphics2D) graphics.create();
        // Draw game area.
        g.transform(gameAreaTransform);
        // NYI
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

}
