package net.zergrush;

import net.zergrush.ui.MainUI;

public class Main {

    public static void main(String[] args) {
        MainUI ui = new MainUI();
        Game game = new Game(ui);
        ui.setMessage("ZERG RUSH", "They are coming...");
        MainUI.createWindow(ui).setVisible(true);
        MainUI.scheduleRepeatedly(game.getUpdateRunnable(),
                                  Game.UPDATE_INTERVAL);
    }

}
