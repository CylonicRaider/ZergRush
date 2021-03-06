package net.zergrush.sprites;

import java.awt.geom.Point2D;
import java.util.List;
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

    public double getHP() {
        return bar.getHP();
    }

    public double getHPMax() {
        return bar.getHPMax();
    }

    public void changeHP(double increment) {
        bar.changeHP(increment);
    }

    public void update() {
        super.update();
        bar.update();
    }

    public void die(HPSprite victor) {
        game.removeSprite(this);
    }

    protected <T extends HPSprite> boolean addHealth(Class<T> cls,
                                                     double strength) {
        List<T> intersecting = game.getIntersecting(this, cls);
        for (T other : intersecting) {
            other.changeHP(strength);
            if (other.getHP() == 0) other.die(this);
        }
        return (! intersecting.isEmpty());
    }

    protected <T extends HPSprite> boolean battleWith(Class<T> cls,
                                                      double strength) {
        return addHealth(cls, -strength);
    }

}
