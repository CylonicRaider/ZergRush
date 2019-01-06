package net.zergrush.sprites;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import net.zergrush.Game;

public class HPBar extends Sprite {

    public static final double SIZE = 0.025;
    public static final double OFFSET = 0.025;
    public static final Color COLOR_FG = new Color(0xC000FF00, true);
    public static final Color COLOR_BG = new Color(0xC0FF0000, true);

    protected final Sprite anchor;
    protected double value;
    protected double max;
    private final Rectangle2D.Double scratchRect;

    public HPBar(Game game, Sprite anchor) {
        super(game);
        this.anchor = anchor;
        this.scratchRect = new Rectangle2D.Double();
        updateLocation();
    }

    public double getHP() {
        return value;
    }

    public double getMaxHP() {
        return max;
    }

    public void setHP(double v, double m) {
        value = v;
        max = m;
    }

    public void setHP(double v) {
        setHP(v, v);
    }

    public void changeHP(double v) {
        value += v;
        if (value < 0) value = 0;
        if (value > max) value = max;
    }

    public void draw(Graphics2D g) {
        double effWidth;
        if (value == 0) {
            effWidth = 0;
        } else {
            effWidth = baseBounds.getWidth() * value / max;
        }
        scratchRect.setRect(position.x + baseBounds.x,
            position.y + baseBounds.y, effWidth, baseBounds.height);
        g.setColor(COLOR_FG);
        g.fill(scratchRect);
        scratchRect.x += effWidth;
        scratchRect.width = baseBounds.width - effWidth;
        g.setColor(COLOR_BG);
        g.fill(scratchRect);
    }

    protected void updateSelf() {
        updateLocation();
    }

    protected void updateLocation() {
        Rectangle2D ancBounds = anchor.getBounds();
        position.setLocation(ancBounds.getX() + ancBounds.getWidth() / 2,
                             ancBounds.getY() - OFFSET);
        baseBounds.setRect(-ancBounds.getWidth() / 2, -SIZE,
                           ancBounds.getWidth(), SIZE);
    }

}
