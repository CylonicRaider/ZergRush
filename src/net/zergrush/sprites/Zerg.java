package net.zergrush.sprites;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class Zerg extends Sprite {

    public static final double SIZE = 0.1;
    public static final double SPEED = 0.01;
    public static final Color COLOR = new Color(0x800000);

    private double sx, sy;

    public Zerg() {
        sx = Math.random() * SPEED - SPEED / 2;
        sy = Math.random() * SPEED - SPEED / 2;
        baseBounds.setRect(-SIZE / 2, -SIZE / 2, SIZE, SIZE);
    }

    public void draw(Graphics2D g) {
        g.setColor(COLOR);
        g.fill(getBounds());
    }

    public boolean update() {
        position.setLocation(position.getX() + sx, position.getY() + sy);
        // We will replace this with homing toward the base once the latter is
        // implemented.
        if (position.getX() <= -1) {
            sx = Math.abs(sx);
        } else if (position.getX() >= 1) {
            sx = -Math.abs(sx);
        }
        if (position.getY() <= -1) {
            sy = Math.abs(sy);
        } else if (position.getY() >= 1) {
            sy = -Math.abs(sy);
        }
        return true;
    }

}
