package net.zergrush.sprites;

import java.awt.geom.Point2D;

public enum HomingMode {
    DIAGONAL(0), HORIZONTAL_FIRST(1), VERTICAL_FIRST(2);

    private final int moveMask;

    private HomingMode(int moveMask) {
        this.moveMask = moveMask;
    }

    public boolean moveTo(Point2D position, Point2D target,
                          double speed) {
        if (position.equals(target)) return true;
        double dx = target.getX() - position.getX();
        double dy = target.getY() - position.getY();
        dx = Math.max(-speed, Math.min(dx, speed));
        dy = Math.max(-speed, Math.min(dy, speed));
        if ((moveMask & 1) != 0 && dx != 0)
            dy = 0;
        if ((moveMask & 2) != 0 && dy != 0)
            dx = 0;
        if (dx != 0 && dy != 0) {
            double l = Math.sqrt(dx * dx + dy * dy);
            if (l > speed) {
                dx *= speed / l;
                dy *= speed / l;
            }
        }
        position.setLocation(position.getX() + dx, position.getY() + dy);
        return false;
    }

    public static HomingMode selectRandom() {
        HomingMode[] arr = values();
        return arr[(int) (Math.random() * arr.length)];
    }

}
