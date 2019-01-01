package net.zergrush;

import net.zergrush.ui.MainUI;

public class Main {

    public static void main(String[] args) {
        MainUI ui = new MainUI();
        ui.setMessage("ZERG RUSH", "They are coming...");
        MainUI.createWindow(ui).setVisible(true);
    }

}
