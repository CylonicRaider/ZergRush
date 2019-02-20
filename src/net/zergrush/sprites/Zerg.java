package net.zergrush.sprites;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import net.zergrush.Game;
import net.zergrush.stats.GameStatistics;

public class Zerg extends HPSprite {

    public static final double HITPOINTS = 10;
    public static final double ATTACK = 1;
    public static final double HEALING = 1;
    public static final int DEATH_POINTS = 5;
    public static final int STAY_COUNT = 5;
    public static final double SIZE = 0.15;
    public static final double SPEED = 0.005;
    public static final Color COLOR = new Color(0x800000);

    protected final Point2D.Double target;
    protected final HomingMode mode;
    private int stayCounter;

    public Zerg(Game game, Point2D position) {
        super(game, position);
        baseBounds.setRect(-SIZE / 2, -SIZE / 2, SIZE, SIZE);
        initHP(HITPOINTS);
        target = new Point2D.Double();
        mode = HomingMode.selectRandom();
        stayCounter = STAY_COUNT;
    }

    public void setTarget(Point2D t) {
        target.setLocation(t);
    }

    public void draw(Graphics2D g) {
        g.setColor(COLOR);
        g.fill(getBounds());
    }

    public void updateSelf() {
        // The bitwise OR is intentional -- a zerg can battle the base and
        // the player at the same time.
        if (battleWith(Base.class, ATTACK) |
                battleWith(Player.class, ATTACK)) {
            stayCounter = STAY_COUNT;
        } else if (! mode.moveTo(position, target, SPEED)) {
            stayCounter = STAY_COUNT;
        } else if (getHP() < getHPMax()) {
            changeHP(HEALING);
        } else if (stayCounter > 0) {
            stayCounter--;
        } else {
            game.removeSprite(this);
        }
    }

    public void die(HPSprite victor) {
        super.die(victor);
        if (victor instanceof Player)
            game.getStats().increment(GameStatistics.SCORE, DEATH_POINTS);
    }

}
