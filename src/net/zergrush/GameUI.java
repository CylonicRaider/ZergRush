package net.zergrush;

import java.awt.geom.Rectangle2D;

public interface GameUI {

    interface InfoScreenCallback {

        String onInfoScreenDone(Object result);

    }

    int KEY_RELEASED = 0;
    int KEY_PRESSED = 1;
    int KEY_PRESSED_INITIAL = 2;

    void setGame(Game game);

    void onGameStateChange();

    void setMessage(String heading, KeyboardAction... actions);

    void markDamaged(Rectangle2D rect);

    void update();

    int getKeyStatus(int keyCode);

    void showInfoScreen(String name);

    void showInfoScreen(String name, Object data, InfoScreenCallback cb);

}
