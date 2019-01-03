package net.zergrush.sprites;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import net.zergrush.Game;
import net.zergrush.GameUI;

public abstract class Sprite {

    protected final Game game;
    protected final Point2D position;
    protected final Rectangle2D baseBounds;

    public Sprite(Game game) {
        this.game = game;
        this.position = new Point2D.Double();
        this.baseBounds = new Rectangle2D.Double();
    }

    public Game getGame() {
        return game;
    }

    public Rectangle2D getBounds() {
        return new Rectangle2D.Double(position.getX() + baseBounds.getX(),
            position.getY() + baseBounds.getY(), baseBounds.getWidth(),
            baseBounds.getHeight());
    }

    public abstract void draw(Graphics2D g);

    public abstract boolean update();

    protected boolean isKeyPressed(int keyCode) {
        return game.getUI().getKeyStatus(keyCode) >= GameUI.KEY_PRESSED;
    }

    protected boolean isKeyPressedFirst(int keyCode) {
        int status = game.getUI().getKeyStatus(keyCode);
        return status == GameUI.KEY_PRESSED_INITIAL;
    }

}
