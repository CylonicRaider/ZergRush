package net.zergrush;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import net.zergrush.sprites.Base;
import net.zergrush.sprites.Player;
import net.zergrush.sprites.Sprite;
import net.zergrush.sprites.Zerg;

public class Game {

    public static final int UPDATE_INTERVAL = 16;

    private final GameUI ui;
    private Base base;
    private Player player;
    private Zerg demoZerg;

    public Game(GameUI ui) {
        this.ui = ui;
        this.base = new Base(this);
        this.player = new Player(this);
        this.demoZerg = new Zerg(this);
        ui.setGame(this);
    }

    public GameUI getUI() {
        return ui;
    }

    private boolean updateSprite(Sprite spr) {
        if (spr == null) return true;
        ui.markDamaged(spr.getBounds());
        boolean ret = spr.update();
        if (ret) ui.markDamaged(spr.getBounds());
        return ret;
    }

    public void update() {
        if (! updateSprite(base)) base = null;
        if (! updateSprite(player)) player = null;
        if (! updateSprite(demoZerg)) demoZerg = null;
        ui.update();
    }

    public void draw(Graphics2D g) {
        if (base != null) base.draw(g);
        if (demoZerg != null) demoZerg.draw(g);
        if (player != null) player.draw(g);
    }

    public Runnable getUpdateRunnable() {
        return new Runnable() {
            public void run() {
                update();
            }
        };
    }

    public boolean isKeyPressed(int keyCode) {
        return ui.getKeyStatus(keyCode) >= GameUI.KEY_PRESSED;
    }

    public boolean isKeyPressedFirst(int keyCode) {
        int status = ui.getKeyStatus(keyCode);
        return status == GameUI.KEY_PRESSED_INITIAL;
    }

}
