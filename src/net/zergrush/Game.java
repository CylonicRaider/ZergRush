package net.zergrush;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import net.zergrush.sprites.Base;
import net.zergrush.sprites.HPSprite;
import net.zergrush.sprites.Player;
import net.zergrush.sprites.Sprite;
import net.zergrush.sprites.Zerg;

public class Game {

    public enum State { INTRO, PLAYING, OVER };

    public static final int UPDATE_INTERVAL = 16;

    private final GameUI ui;
    private State state;
    private Base base;
    private Player player;

    public Game(GameUI ui) {
        this.ui = ui;
        this.base = null;
        this.player = null;
        initUI();
    }

    protected void initUI() {
        ui.setGame(this);
        setState(State.INTRO);
    }

    public GameUI getUI() {
        return ui;
    }

    public State getState() {
        return state;
    }

    public void setState(State s) {
        if (s == state) return;
        state = s;
        switch (s) {
            case INTRO:
                ui.setMessage("ZERG RUSH", "They are coming...");
                break;
            case PLAYING:
                ui.setMessage(null, null);
                base = new Base(this, null);
                player = new Player(this, null);
                break;
            case OVER:
                ui.setMessage("GAME OVER", null);
                base = null;
                player = null;
                break;
        }
    }

    private void updateSprite(Sprite spr) {
        if (spr == null) return;
        spr.update();
    }
    private void updateHPBar(HPSprite spr) {
        if (spr == null || spr.getHPBar() == null) return;
        spr.getHPBar().update();
    }

    public boolean update() {
        if (isKeyPressed(KeyEvent.VK_ESCAPE))
            return false;
        if (state != State.PLAYING && isKeyPressedFirst(KeyEvent.VK_ENTER))
            setState(State.PLAYING);
        updateSprite(base);
        updateSprite(player);
        updateHPBar(base);
        updateHPBar(player);
        ui.update();
        return true;
    }

    public void draw(Graphics2D g) {
        if (base != null) base.draw(g);
        if (player != null) player.draw(g);
        if (base != null) base.getHPBar().draw(g);
        if (player != null) player.getHPBar().draw(g);
    }

    public void removeSprite(Sprite spr) {
        if (spr == base) base = null;
        if (player == null) player = null;
    }

    public boolean isKeyPressed(int keyCode) {
        return ui.getKeyStatus(keyCode) >= GameUI.KEY_PRESSED;
    }

    public boolean isKeyPressedFirst(int keyCode) {
        int status = ui.getKeyStatus(keyCode);
        return status == GameUI.KEY_PRESSED_INITIAL;
    }

}
