package net.zergrush.ui;

import java.util.Map;
import java.util.List;
import net.zergrush.stats.Statistics;
import java.util.Iterator;

public class StatisticsPageRenderer
        extends TablePageRenderer<Statistics.Entry<?>> {

    protected void renderHeaders(List<String> drain) {
        drain.add("Statistic");
        drain.add("Value");
    }

    protected Iterator<Statistics.Entry<?>> getDataIterator(Object data) {
        return ((Statistics) data).entries().iterator();
    }

    private <T> void renderRowInner(Statistics.Entry<T> item,
                                    List<String> drain) {
        drain.add(escapeHTML(item.getDescription()));
        drain.add(escapeHTML(item.getDisplayedValue()));
    }

    protected void renderRow(Statistics.Entry<?> item, List<String> drain) {
        renderRowInner(item, drain);
    }

    protected void renderReplacements(String pageName, Object data,
                                      Map<String, String> drain) {
        drain.put("title", "Game statistics");
        super.renderReplacements(pageName, data, drain);
    }

}
