package net.zergrush;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import net.zergrush.sprites.Base;
import net.zergrush.sprites.HPSprite;
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
        this.base = new Base(this, null);
        this.player = new Player(this, null);
        this.demoZerg = new Zerg(this, null);
        ui.setGame(this);
    }

    public GameUI getUI() {
        return ui;
    }

    private boolean updateSprite(Sprite spr) {
        if (spr == null) return true;
        return spr.update();
    }
    private void updateHPBar(HPSprite spr) {
        if (spr == null || spr.getHPBar() == null) return;
        spr.getHPBar().update();
    }

    public void update() {
        if (! updateSprite(base)) base = null;
        if (! updateSprite(player)) player = null;
        if (! updateSprite(demoZerg)) demoZerg = null;
        updateHPBar(base);
        updateHPBar(player);
        updateHPBar(demoZerg);
        ui.update();
    }

    public void draw(Graphics2D g) {
        if (base != null) base.draw(g);
        if (demoZerg != null) demoZerg.draw(g);
        if (player != null) player.draw(g);
        if (base != null) base.getHPBar().draw(g);
        if (demoZerg != null) demoZerg.getHPBar().draw(g);
        if (player != null) player.getHPBar().draw(g);
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
