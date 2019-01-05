package net.zergrush.sprites;

import java.awt.geom.Point2D;
import net.zergrush.Game;

public abstract class HPSprite extends Sprite {

    protected final HPBar bar;

    public HPSprite(Game game, Point2D position) {
        super(game);
        if (position != null) this.position.setLocation(position);
        bar = createHPBar();
    }

    public HPBar getHPBar() {
        return bar;
    }

    protected HPBar createHPBar() {
        return new HPBar(game, this);
    }

    protected void initHP(double value) {
        bar.setHP(value);
    }

}
