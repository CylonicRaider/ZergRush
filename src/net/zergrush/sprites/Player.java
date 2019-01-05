package net.zergrush.sprites;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.event.KeyEvent;
import net.zergrush.Game;

public class Player extends ShapeSprite {

    public enum Orientation { RIGHT, DOWN, LEFT, UP }

    public static final double HITPOINTS = 50;
    public static final double SIZE = 0.2;
    public static final double SPEED = 0.025;
    public static final double BOUND = 0.8;
    public static final Color COLOR = new Color(0x008000);
    public static final Shape SHAPE;

    static {
        Path2D shape = new Path2D.Double();
        shape.moveTo(SIZE / 2, 0);
        shape.lineTo(-SIZE / 2, SIZE / 2);
        shape.lineTo(-SIZE / 2, -SIZE / 2);
        shape.closePath();
        SHAPE = shape;
    }

    private Orientation rot;

    public Player(Game game, Point2D position) {
        super(game, position);
        rot = Orientation.RIGHT;
        initShape(SHAPE, COLOR);
        initHP(HITPOINTS);
    }

    public void move(Orientation dir) {
        if (dir != null) rot = dir;
        switch (rot) {
            case RIGHT:
                position.x += SPEED;
                if (position.x > BOUND) position.x = BOUND;
                break;
            case DOWN:
                position.y += SPEED;
                if (position.y > BOUND) position.y = BOUND;
                break;
            case LEFT:
                position.x -= SPEED;
                if (position.x < -BOUND) position.x = -BOUND;
                break;
            case UP:
                position.y -= SPEED;
                if (position.y < -BOUND) position.y = -BOUND;
                break;
        }
    }

    protected void drawSelf(Graphics2D g) {
        g.rotate(Math.PI / 2 * rot.ordinal());
        super.drawSelf(g);
    }

    public boolean updateSelf() {
        if (game.isKeyPressed(KeyEvent.VK_UP)) {
            move(Orientation.UP);
        } else if (game.isKeyPressed(KeyEvent.VK_DOWN)) {
            move(Orientation.DOWN);
        } else if (game.isKeyPressed(KeyEvent.VK_LEFT)) {
            move(Orientation.LEFT);
        } else if (game.isKeyPressed(KeyEvent.VK_RIGHT)) {
            move(Orientation.RIGHT);
        }
        return true;
    }

}
