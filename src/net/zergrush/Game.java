package net.zergrush;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.zergrush.sprites.Base;
import net.zergrush.sprites.HPSprite;
import net.zergrush.sprites.Player;
import net.zergrush.sprites.Sprite;
import net.zergrush.sprites.Zerg;

public class Game {

    public enum State { INTRO, PLAYING, OVER };

    public static final int UPDATE_INTERVAL = 16;
    public static final int INITIAL_ZERG_COUNT = 1;
    public static final int ZERG_COUNT_INCR_COUNTER = 60;

    private final GameUI ui;
    private State state;
    private Base base;
    private Player player;
    private final List<Zerg> zergs;
    private int zergCount;
    private int zergCountCounter;

    public Game(GameUI ui) {
        this.ui = ui;
        this.base = null;
        this.player = null;
        this.zergs = new ArrayList<>();
        this.zergCount = INITIAL_ZERG_COUNT;
        this.zergCountCounter = ZERG_COUNT_INCR_COUNTER;
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
                zergs.clear();
                zergCount = INITIAL_ZERG_COUNT;
                zergCountCounter = ZERG_COUNT_INCR_COUNTER;
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
    private <T extends Sprite> void updateSpriteList(List<T> sprites) {
        // Updating zergs requires special care as they could remove
        // themselves.
        for (int i = 0; i < sprites.size(); i++) {
            T spr = sprites.get(i);
            spr.update();
            if (i < sprites.size() && sprites.get(i) != spr) i--;
        }
    }
    private void updateHPBar(HPSprite spr) {
        if (spr == null || spr.getHPBar() == null) return;
        spr.getHPBar().update();
    }

    public boolean update() {
        /* Check for keyboard input */
        if (isKeyPressed(KeyEvent.VK_ESCAPE))
            return false;
        if (state != State.PLAYING && isKeyPressedFirst(KeyEvent.VK_ENTER))
            setState(State.PLAYING);
        /* Update sprites */
        updateSprite(base);
        updateSpriteList(zergs);
        updateSprite(player);
        updateHPBar(base);
        for (Zerg z : zergs) updateHPBar(z);
        updateHPBar(player);
        /* Spawn new zergs as necessary */
        if (zergCountCounter-- <= 0) {
            zergCount++;
            zergCountCounter = ZERG_COUNT_INCR_COUNTER;
        }
        if (zergs.size() < zergCount && base != null && player != null) {
            Point2D position = new Point2D.Double();
            while (zergs.size() < zergCount) {
                position.setLocation(Math.random() * 2 - 1,
                                     (Math.random() < 0.5) ? -1 : 1);
                if (Math.random() < 0.5)
                    position.setLocation(position.getY(), position.getX());
                Zerg z = new Zerg(this, position);
                z.setTarget(base.getPosition());
                zergs.add(z);
            }
        }
        /* Update UI; done */
        ui.update();
        return true;
    }

    public void draw(Graphics2D g) {
        if (base != null) base.draw(g);
        for (Zerg z : zergs) z.draw(g);
        if (player != null) player.draw(g);
        if (base != null) base.getHPBar().draw(g);
        for (Zerg z : zergs) z.getHPBar().draw(g);
        if (player != null) player.getHPBar().draw(g);
    }

    private <T extends Sprite> List<T> getIntersectingSingle(Sprite test,
                                                             T reference) {
        if (reference != null &&
                test.getBounds().intersects(reference.getBounds())) {
            return Collections.singletonList(reference);
        } else {
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Sprite> List<T> getIntersecting(Sprite spr,
                                                      Class<T> other) {
        if (other == Base.class) {
            return (List<T>) getIntersectingSingle(spr, base);
        } else if (other == Player.class) {
            return (List<T>) getIntersectingSingle(spr, player);
        } else if (other == Zerg.class) {
            Rectangle2D bounds = spr.getBounds();
            List<Zerg> ret = null;
            for (Zerg z : zergs) {
                if (bounds.intersects(z.getBounds())) {
                    if (ret == null) ret = new ArrayList<Zerg>();
                    ret.add(z);
                }
            }
            return (List<T>) ret;
        } else {
            return Collections.emptyList();
        }
    }

    public void removeSprite(Sprite spr) {
        if (spr == base) base = null;
        if (spr == player) player = null;
        if (spr instanceof Zerg) zergs.remove(spr);
    }

    public boolean isKeyPressed(int keyCode) {
        return ui.getKeyStatus(keyCode) >= GameUI.KEY_PRESSED;
    }

    public boolean isKeyPressedFirst(int keyCode) {
        int status = ui.getKeyStatus(keyCode);
        return status == GameUI.KEY_PRESSED_INITIAL;
    }

}
