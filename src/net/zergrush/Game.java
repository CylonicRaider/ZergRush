package net.zergrush;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.zergrush.sprites.Base;
import net.zergrush.sprites.HPSprite;
import net.zergrush.sprites.Player;
import net.zergrush.sprites.Sprite;
import net.zergrush.sprites.Zerg;
import net.zergrush.stats.GameStatistics;

public class Game {

    public enum State { INTRO, PLAYING, PAUSED, OVER };

    public static final int UPDATE_INTERVAL = 16;
    public static final double ZERG_SPAWN_COUNTER = 60;
    public static final double ZERG_SPAWN_COUNTER_DECR = 0.995;

    private final GameUI ui;
    private final GameStatistics stats;
    private final Queue<Runnable> runQueue;
    private State state;
    private Base base;
    private Player player;
    private final List<Zerg> zergs;
    private int zergSpawnCounter;
    private double nextZergSpawnCounter;

    public Game(GameUI ui) {
        this.ui = ui;
        this.stats = new GameStatistics();
        this.runQueue = new ConcurrentLinkedQueue<>();
        this.base = null;
        this.player = null;
        this.zergs = new ArrayList<>();
        this.zergSpawnCounter = 0;
        this.nextZergSpawnCounter = ZERG_SPAWN_COUNTER;
        initUI();
    }

    protected void initUI() {
        ui.setGame(this);
        setState(State.INTRO);
    }

    public GameUI getUI() {
        return ui;
    }

    public GameStatistics getStats() {
        return stats;
    }

    public void runAfterNextUpdate(Runnable r) {
        runQueue.add(r);
    }

    public State getState() {
        return state;
    }

    public void setState(State s) {
        if (s == state) return;
        State prevState = state;
        state = s;
        switch (s) {
            case INTRO:
                ui.setMessage("ZERG RUSH",
                    new KeyboardAction("Enter", "start"));
                break;
            case PLAYING:
                ui.setMessage(null);
                if (prevState != State.PAUSED) reset();
                break;
            case PAUSED:
                ui.setMessage("PAUSED");
                break;
            case OVER:
                ui.setMessage("GAME OVER",
                    new KeyboardAction("Enter", "retry"),
                    new KeyboardAction("Escape", "quit"));
                base = null;
                player = null;
                ui.markDamaged(null);
                break;
        }
        ui.onGameStateChange();
    }

    protected void reset() {
        stats.reset();
        base = new Base(this, null);
        player = new Player(this, null);
        zergs.clear();
        zergSpawnCounter = 0;
        nextZergSpawnCounter = ZERG_SPAWN_COUNTER;
        ui.markDamaged(null);
    }

    private void eraseSprite(Sprite spr) {
        if (spr == null) return;
        ui.markDamaged(spr.getBounds());
        if (spr instanceof HPSprite)
            ui.markDamaged(((HPSprite) spr).getHPBar().getBounds());
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

    public boolean update() {
        /* Check for keyboard input */
        if (isKeyPressed(KeyEvent.VK_ESCAPE)) {
            return false;
        }
        if (isKeyPressedFirst(KeyEvent.VK_F1)) {
            if (state == State.PLAYING) setState(State.PAUSED);
            ui.showInfoScreen("intro");
        }
        if (isKeyPressedFirst(KeyEvent.VK_PAUSE)) {
            if (state == State.PLAYING) {
                setState(State.PAUSED);
            } else if (state == State.PAUSED) {
                setState(State.PLAYING);
            }
        }
        if (isKeyPressedFirst(KeyEvent.VK_ENTER)) {
            setState(State.PLAYING);
        }
        if (state == State.PLAYING || state == State.OVER) {
            /* Erase sprites
             * This has to happen before the updating pass because some HP bars
             * might be missed otherwise. */
            eraseSprite(base);
            for (Zerg z : zergs) eraseSprite(z);
            eraseSprite(player);
            /* Update sprites */
            updateSprite(base);
            updateSpriteList(zergs);
            updateSprite(player);
            /* Spawn new zergs as necessary */
            if (state == State.PLAYING) {
                if (zergSpawnCounter-- <= 0) {
                    zergSpawnCounter = (int) nextZergSpawnCounter;
                    nextZergSpawnCounter *= ZERG_SPAWN_COUNTER_DECR;
                    Point2D position = new Point2D.Double(
                        Math.random() * 2 - 1,
                        (Math.random() < 0.5) ? -1 : 1);
                    if (Math.random() < 0.5)
                        position.setLocation(position.getY(),
                                             position.getX());
                    zergs.add(new Zerg(this, position));
                }
                if (player == null || base == null) setState(State.OVER);
            }
        }
        /* Drain run queue */
        for (;;) {
            Runnable r = runQueue.poll();
            if (r == null) break;
            r.run();
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
            if (ret == null) ret = Collections.emptyList();
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
