package net.zergrush.sprites;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import net.zergrush.Game;

public abstract class Sprite {

    protected final Game game;
    protected final Point2D.Double position;
    protected final Rectangle2D.Double baseBounds;

    public Sprite(Game game) {
        this.game = game;
        this.position = new Point2D.Double();
        this.baseBounds = new Rectangle2D.Double();
    }

    public Game getGame() {
        return game;
    }

    public Point2D getPosition() {
        return position;
    }

    public Rectangle2D getBounds() {
        return new Rectangle2D.Double(position.x + baseBounds.x,
            position.y + baseBounds.y, baseBounds.width, baseBounds.height);
    }

    public abstract void draw(Graphics2D g);

    public void update() {
        game.getUI().markDamaged(getBounds());
        updateSelf();
        game.getUI().markDamaged(getBounds());
    }

    protected abstract void updateSelf();

}
