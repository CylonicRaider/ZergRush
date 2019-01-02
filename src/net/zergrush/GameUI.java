package net.zergrush;

import java.awt.geom.Rectangle2D;

public interface GameUI {

    void setGame(Game game);

    void setMessage(String heading, String text);

    void markDamaged(Rectangle2D rect);

}
