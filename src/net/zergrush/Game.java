package net.zergrush;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.zergrush.sprites.Base;
import net.zergrush.sprites.HPSprite;
import net.zergrush.sprites.Player;
import net.zergrush.sprites.Sprite;
import net.zergrush.sprites.Zerg;
import net.zergrush.stats.GameStatistics;
import net.zergrush.stats.Highscores;
import net.zergrush.stats.HighscoresStorage;
import net.zergrush.stats.SelectedHighscores;

public class Game {

    public enum State { INTRO, PLAYING, PAUSED, OVER };

    private static class HighscoresRequest extends InfoScreenRequest {

        private final Highscores hs;

        public HighscoresRequest(Highscores hs) {
            super("highscores", hs);
            this.hs = hs;
        }
        public HighscoresRequest(HighscoresStorage storage) {
            this(storage.createHighscores());
            storage.retrieveHighscores(hs);
        }

        public InfoScreenRequest onDone(String newPageName, Object result) {
            if (newPageName.startsWith("stats/")) {
                int index;
                try {
                    index = Integer.parseInt(newPageName.substring(6));
                } catch (NumberFormatException exc) {
                    index = -1;
                }
                GameStatistics stats;
                try {
                    stats = hs.getTopEntry(index).getData();
                } catch (IndexOutOfBoundsException exc) {
                    stats = null;
                }
                if (stats != null) {
                    return new InfoScreenRequest("statistics", stats);
                }
            }
            return super.onDone(newPageName, result);
        }

    }

    public static final int UPDATE_INTERVAL = 16;
    public static final double ZERG_SPAWN_COUNTER = 60;
    public static final double ZERG_SPAWN_COUNTER_DECR = 0.995;
    public static final int GAME_OVER_COUNTER = 15;

    private final HighscoresStorage hsStorage;
    private final GameUI ui;
    private final GameStatistics stats;
    private final Queue<Runnable> runQueue;
    private State state;
    private Base base;
    private Player player;
    private final List<Zerg> zergs;
    private int zergSpawnCounter;
    private double nextZergSpawnCounter;
    private int gameOverCounter;

    public Game(HighscoresStorage hsStorage, GameUI ui) {
        this.hsStorage = hsStorage;
        this.ui = ui;
        this.stats = new GameStatistics();
        this.runQueue = new ConcurrentLinkedQueue<>();
        this.base = null;
        this.player = null;
        this.zergs = new ArrayList<>();
        this.zergSpawnCounter = 0;
        this.nextZergSpawnCounter = ZERG_SPAWN_COUNTER;
        this.gameOverCounter = GAME_OVER_COUNTER;
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
                    new KeyboardAction("Enter", "start"),
                    new KeyboardAction("F3", "highscores"));
                break;
            case PLAYING:
                ui.setMessage(null);
                if (prevState != State.PAUSED) reset();
                break;
            case PAUSED:
                ui.setMessage("PAUSED");
                break;
            case OVER:
                stats.put(GameStatistics.ENDED, System.currentTimeMillis());
                ui.setMessage("GAME OVER",
                    new KeyboardAction("Enter", "retry"),
                    new KeyboardAction("F2", "statistics"));
                maybeAddHighscore();
                base = null;
                player = null;
                ui.markDamaged(null);
                break;
        }
        ui.onGameStateChange();
    }

    public InfoScreenRequest getInfoScreenData(String pageName) {
        if (pageName == null) return null;
        switch (pageName) {
            case "highscores":
                return new HighscoresRequest(hsStorage);
            case "statistics":
                return new InfoScreenRequest("statistics",
                                             new GameStatistics(stats));
        }
        return new InfoScreenRequest(pageName);
    }

    protected void reset() {
        stats.reset();
        base = new Base(this, null);
        player = new Player(this, null);
        zergs.clear();
        zergSpawnCounter = 0;
        nextZergSpawnCounter = ZERG_SPAWN_COUNTER;
        gameOverCounter = GAME_OVER_COUNTER;
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
            if (state == State.PLAYING) {
                setState(State.OVER);
            } else {
                return false;
            }
        }
        if (isKeyPressedFirst(KeyEvent.VK_F1)) {
            if (state == State.PLAYING) setState(State.PAUSED);
            ui.showInfoScreen("intro");
        }
        if (isKeyPressedFirst(KeyEvent.VK_F2) && state == State.OVER) {
            ui.showInfoScreen("statistics");
        }
        if (isKeyPressedFirst(KeyEvent.VK_F3)) {
            if (state == State.PLAYING) setState(State.PAUSED);
            ui.showInfoScreen("highscores");
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
             * This has to happen before the updating pass because some HP
             * bars might be missed otherwise. */
            eraseSprite(base);
            for (Zerg z : zergs) eraseSprite(z);
            eraseSprite(player);
            /* Update sprites */
            updateSprite(base);
            updateSpriteList(zergs);
            updateSprite(player);
            if (state == State.PLAYING) {
                /* Spawn new zergs as necessary */
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
                /* End the game if the time has come */
                if (player == null || base == null) {
                    if (gameOverCounter > 0) {
                        gameOverCounter--;
                    } else {
                        setState(State.OVER);
                    }
                }
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
                    if (ret == null) ret = new ArrayList<>();
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

    private void maybeAddHighscore() {
        final Highscores hs = hsStorage.createHighscores();
        hsStorage.retrieveHighscores(hs);
        final Highscores.Entry newEnt = new Highscores.Entry(
            new GameStatistics(stats));
        final SelectedHighscores selection = SelectedHighscores.addAndLocate(
            hs, newEnt);
        if (selection.getIndex() == -1 ||
                selection.getIndex() >= Highscores.MAX_SIZE)
            return;
        ui.showInfoScreen(new InfoScreenRequest("highscores", selection) {
            public InfoScreenRequest onDone(String newPageName,
                                            Object result) {
                @SuppressWarnings("unchecked")
                Map<String, String> formData =
                    (Map<String, String>) result;
                if (! formData.get("index").equals(
                        String.valueOf(selection.getIndex())))
                    return null;
                String name = formData.get("name");
                if (name == null || name.isEmpty()) name = "(anonymous)";
                stats.put(GameStatistics.NAME, name);
                newEnt.getData().put(GameStatistics.NAME, name);
                // Re-read highscores to reduce race condition opportunity
                // windows among multiple instances of the program.
                hs.clear();
                hsStorage.retrieveHighscores(hs);
                hs.add(newEnt);
                hsStorage.storeHighscores(hs);
                return getInfoScreenData("highscores");
            }
        });
    }

}
