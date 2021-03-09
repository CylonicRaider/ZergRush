package net.zergrush;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import net.zergrush.stats.PreferencesHighscoresStorage;
import net.zergrush.ui.MainUI;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                MainUI ui = new MainUI();
                final Game game = new Game(
                    PreferencesHighscoresStorage.getDefault(), ui);
                final JFrame win = MainUI.createWindow(ui);
                win.setVisible(true);
                MainUI.scheduleRepeatedly(new Runnable() {

                    boolean running = true;

                    public void run() {
                        if (! running)
                            return;
                        if (! game.update()) {
                            MainUI.closeWindow(win);
                            running = false;
                        }
                    }

                }, Game.UPDATE_INTERVAL);
            }
        });
    }

}
