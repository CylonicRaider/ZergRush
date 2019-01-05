package net.zergrush.sprites;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import net.zergrush.Game;

// It would be a better design to compose sprite drawing in instead of
// inheriting it; we can restructure the code if that becomes necessary.
public abstract class ShapeSprite extends HPSprite {

    protected Shape shape;
    protected Color color;

    public ShapeSprite(Game game, Point2D position) {
        super(game, position);
    }

    protected void initShape(Shape s, Color c) {
        shape = s;
        color = c;
        baseBounds.setRect(s.getBounds2D());
    }

    public void draw(Graphics2D g) {
        AffineTransform tr = g.getTransform();
        g.translate(position.x, position.y);
        g.setColor(color);
        drawSelf(g);
        g.setTransform(tr);
    }

    protected void drawSelf(Graphics2D g) {
        g.fill(shape);
    }

}
