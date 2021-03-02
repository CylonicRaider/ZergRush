package net.zergrush.ui;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.zergrush.stats.Highscores;

public class HighscoresPageRenderer
        extends TablePageRenderer<Highscores.NumberedEntry> {

    protected void renderHeaders(List<String> drain) {
        drain.add("#");
        drain.add("Name");
        drain.add("Score");
    }

    protected Iterator<Highscores.NumberedEntry> getDataIterator(
            Object data) {
        Highscores hs = (Highscores) data;
        return hs.getTopEntries(Highscores.MAX_SIZE).iterator();
    }

    protected void renderRow(Highscores.NumberedEntry item,
                             List<String> drain) {
        drain.add(String.valueOf(item.getIndex() + 1));
        drain.add(escapeHTML(item.getName()));
        drain.add(escapeHTML(String.valueOf(item.getScore())));
    }

    protected void renderReplacements(String pageName, Object data,
                                      Map<String, String> drain) {
        drain.put("title", "Game statistics");
        super.renderReplacements(pageName, data, drain);
    }

}
