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

    public Rectangle2D getBounds() {
        return new Rectangle2D.Double(position.x + baseBounds.x,
            position.y + baseBounds.y, baseBounds.width, baseBounds.height);
    }

    public abstract void draw(Graphics2D g);

    public boolean update() {
        game.getUI().markDamaged(getBounds());
        boolean ret = updateSelf();
        if (ret) game.getUI().markDamaged(getBounds());
        return ret;
    }

    protected abstract boolean updateSelf();

}
