package net.zergrush;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import net.zergrush.stats.PreferencesHighscoresStorage;
import net.zergrush.ui.MainUI;

public class Main {

    public static void main(String[] args) {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                           "[%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL " +
                           "%4$s %3$s] %5$s%6$s%n");
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
