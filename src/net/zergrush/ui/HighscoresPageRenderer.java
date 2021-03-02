package net.zergrush.ui;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.zergrush.stats.Highscores;

public class HighscoresPageRenderer
        extends TablePageRenderer<
            HighscoresPageRenderer.NumberedHighscoresEntry> {

    static class NumberedHighscoresEntry {

        private final int index;
        private final Highscores.Entry entry;

        public NumberedHighscoresEntry(int index, Highscores.Entry entry) {
            this.index = index;
            this.entry = entry;
        }

        public int getIndex() {
            return index;
        }

        public Highscores.Entry getEntry() {
            return entry;
        }

        public String getName() {
            return entry.getName();
        }

        public int getScore() {
            return entry.getScore();
        }

        public long getDate() {
            return entry.getDate();
        }

    }

    protected void renderHeaders(List<String> drain) {
        drain.add("#");
        drain.add("Name");
        drain.add("Score");
    }

    protected Iterator<NumberedHighscoresEntry> getDataIterator(Object data) {
        final Iterator<Highscores.Entry> hsEntries =
            ((Highscores) data).getEntries().iterator();
        return new Iterator<NumberedHighscoresEntry>() {

            int counter = 1;

            public boolean hasNext() {
                return counter <= Highscores.MAX_SIZE && hsEntries.hasNext();
            }

            public NumberedHighscoresEntry next() {
                return new NumberedHighscoresEntry(counter++,
                                                   hsEntries.next());
            }

            public void remove() {
                throw new UnsupportedOperationException(
                    "May not remove from highscores");
            }

        };
    }

    protected void renderRow(NumberedHighscoresEntry item,
                             List<String> drain) {
        drain.add(String.valueOf(item.getIndex()));
        drain.add(escapeHTML(item.getName()));
        drain.add(escapeHTML(String.valueOf(item.getScore())));
    }

    protected void renderReplacements(String pageName, Object data,
                                      Map<String, String> drain) {
        drain.put("title", "Game statistics");
        super.renderReplacements(pageName, data, drain);
    }

}
