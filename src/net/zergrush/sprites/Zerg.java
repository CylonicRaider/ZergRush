package net.zergrush.sprites;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import net.zergrush.Game;

public class Zerg extends HPSprite {

    public static final double HITPOINTS = 10;
    public static final double SIZE = 0.15;
    public static final double SPEED = 0.005;
    public static final Color COLOR = new Color(0x800000);

    private double sx, sy;

    public Zerg(Game game, Point2D position) {
        super(game, position);
        baseBounds.setRect(-SIZE / 2, -SIZE / 2, SIZE, SIZE);
        initHP(HITPOINTS);
        double angle = 2 * Math.PI * Math.random();
        sx = SPEED * Math.cos(angle);
        sy = SPEED * Math.sin(angle);
    }

    public void draw(Graphics2D g) {
        g.setColor(COLOR);
        g.fill(getBounds());
    }

    public void updateSelf() {
        position.setLocation(position.x + sx, position.y + sy);
        // We will replace this with homing toward the base once the latter is
        // implemented.
        if (position.x <= -1) {
            sx = Math.abs(sx);
        } else if (position.x >= 1) {
            sx = -Math.abs(sx);
        }
        if (position.y <= -1) {
            sy = Math.abs(sy);
        } else if (position.y >= 1) {
            sy = -Math.abs(sy);
        }
    }

}
