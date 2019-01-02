package net.zergrush.sprites;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public abstract class Sprite {

    protected final Point2D position;
    protected final Rectangle2D baseBounds;

    public Sprite() {
        position = new Point2D.Double();
        baseBounds = new Rectangle2D.Double();
    }

    public Rectangle2D getBounds() {
        return new Rectangle2D.Double(position.getX() + baseBounds.getX(),
            position.getY() + baseBounds.getY(), baseBounds.getWidth(),
            baseBounds.getHeight());
    }

    public abstract void draw(Graphics2D g);

    public abstract boolean update();

}
