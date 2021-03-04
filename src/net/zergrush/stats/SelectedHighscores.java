package net.zergrush.stats;

import java.util.Iterator;

public class SelectedHighscores {

    private final Highscores highscores;
    private final int index;

    public SelectedHighscores(Highscores highscores, int index) {
        if (index < -1 || index >= highscores.getEntries().size())
            throw new IndexOutOfBoundsException("SelectedHighscores index " +
                "out of range");
        this.highscores = highscores;
        this.index = index;
    }

    public Highscores getHighscores() {
        return highscores;
    }

    public int getIndex() {
        return index;
    }

    public static SelectedHighscores addAndLocate(Highscores hs,
                                                  Highscores.Entry ent) {
        hs.add(ent);
        int index = 0;
        Iterator<Highscores.Entry> iter = hs.getEntries().iterator();
        for (;;) {
            if (! iter.hasNext()) {
                index = -1;
                break;
            }
            Highscores.Entry check = iter.next();
            if (check == ent) break;
            index++;
        }
        return new SelectedHighscores(hs, index);
    }

}
