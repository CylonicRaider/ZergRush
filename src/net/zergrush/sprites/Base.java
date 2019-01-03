package net.zergrush.sprites;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import net.zergrush.Game;

public class Base extends Sprite {

    public static final double SIZE = 0.5;
    public static final Color COLOR = new Color(0x000080);
    public static final Shape SHAPE;

    static {
        SHAPE = new Ellipse2D.Double(-SIZE / 2, -SIZE / 2, SIZE, SIZE);
    }

    public Base(Game game) {
        super(game);
    }

    public void draw(Graphics2D g) {
        g.setColor(COLOR);
        g.fill(SHAPE);
    }

    public boolean update() {
        return true;
    }

}
