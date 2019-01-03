package net.zergrush;

import javax.swing.SwingUtilities;
import net.zergrush.ui.MainUI;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                MainUI ui = new MainUI();
                Game game = new Game(ui);
                ui.setMessage("ZERG RUSH", "They are coming...");
                MainUI.createWindow(ui).setVisible(true);
                MainUI.scheduleRepeatedly(game.getUpdateRunnable(),
                                      Game.UPDATE_INTERVAL);
            }
        });
    }

}
