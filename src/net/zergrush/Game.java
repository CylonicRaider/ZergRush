package net.zergrush;

import java.awt.Graphics2D;
import net.zergrush.sprites.Zerg;

public class Game {

    public static final int UPDATE_INTERVAL = 16;

    private final GameUI ui;
    private Zerg demoZerg;

    public Game(GameUI ui) {
        this.ui = ui;
        this.demoZerg = new Zerg();
        ui.setGame(this);
    }

    public void update() {
        if (demoZerg == null) return;
        ui.markDamaged(demoZerg.getBounds());
        if (! demoZerg.update()) {
            demoZerg = null;
        } else {
            ui.markDamaged(demoZerg.getBounds());
        }
        ui.update();
    }

    public void draw(Graphics2D g) {
        if (demoZerg != null) demoZerg.draw(g);
    }

    public Runnable getUpdateRunnable() {
        return new Runnable() {
            public void run() {
                update();
            }
        };
    }

}
