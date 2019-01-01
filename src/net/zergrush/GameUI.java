package net.zergrush;

import java.awt.geom.Rectangle2D;

public interface GameUI {

    void setMessage(String heading, String text);

    void markDamaged(Rectangle2D rect);

}
