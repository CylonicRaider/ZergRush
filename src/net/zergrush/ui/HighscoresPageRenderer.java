package net.zergrush.ui;

import java.util.List;
import java.util.Map;
import net.zergrush.stats.Highscores;
import net.zergrush.stats.SelectedHighscores;

public class HighscoresPageRenderer extends TablePageRenderer {

    protected void renderCells(Object data, CellWriter writer) {
        Highscores hs;
        int selectIndex = -1;
        if (data instanceof Highscores) {
            hs = (Highscores) data;
        } else if (data instanceof SelectedHighscores) {
            hs = ((SelectedHighscores) data).getHighscores();
            selectIndex = ((SelectedHighscores) data).getIndex();
        } else {
            throw new ClassCastException("Cannot render object of type " +
                data.getClass().getName());
        }
        writer.header("#");
        writer.header("Name");
        writer.header("Score");
        writer.nextRow();
        List<Highscores.NumberedEntry> top =
            hs.getTopEntries(Highscores.MAX_SIZE);
        for (Highscores.NumberedEntry item : top) {
            writer.dataEsc(item.getIndex() + 1);
            if (item.getIndex() == selectIndex) {
                writer.data("<input type=\"text\" name=\"name\" value=\"" +
                    escapeHTML(item.getName()) + "\"/>");
            } else {
                writer.dataEsc(item.getName());
            }
            writer.dataEsc(item.getScore());
            writer.nextRow();
        }
    }

    protected void renderReplacements(String pageName, Object data,
                                      Map<String, String> drain) {
        drain.put("title", "Game statistics");
        if (data instanceof SelectedHighscores)
            drain.put("form", Boolean.toString(true));
        super.renderReplacements(pageName, data, drain);
    }

}
