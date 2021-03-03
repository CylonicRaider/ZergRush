package net.zergrush.ui;

import java.util.List;
import java.util.Map;
import net.zergrush.stats.Highscores;

public class HighscoresPageRenderer extends TablePageRenderer {

    protected void renderCells(Object data, CellWriter writer) {
        writer.header("#");
        writer.header("Name");
        writer.header("Score");
        writer.nextRow();
        List<Highscores.NumberedEntry> top = ((Highscores) data)
            .getTopEntries(Highscores.MAX_SIZE);
        for (Highscores.NumberedEntry item : top) {
            writer.dataEsc(item.getIndex() + 1);
            writer.dataEsc(item.getName());
            writer.dataEsc(item.getScore());
            writer.nextRow();
        }
    }

    protected void renderReplacements(String pageName, Object data,
                                      Map<String, String> drain) {
        drain.put("title", "Game statistics");
        super.renderReplacements(pageName, data, drain);
    }

}
