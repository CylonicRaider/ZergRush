package net.zergrush;

public class ZergRush {

    public static void main(String[] args) {
        MainUI ui = new MainUI();
        ui.setMessage("ZERG RUSH", "They are coming...");
        MainUI.createWindow(ui).setVisible(true);
    }

}
