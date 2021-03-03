package net.zergrush.ui;

import java.util.Map;
import net.zergrush.stats.Statistics;

public class StatisticsPageRenderer extends TablePageRenderer {

    protected void renderCells(Object data, CellWriter writer) {
        writer.header("Statistic");
        writer.header("Value");
        writer.finishRow();
        for (Statistics.Entry<?> item : ((Statistics) data).entries()) {
            writer.dataEsc(item.getDescription());
            writer.dataEsc(item.getDisplayedValue());
            writer.finishRow();
        }
    }

    protected void renderReplacements(String pageName, Object data,
                                      Map<String, String> drain) {
        drain.put("title", "Game statistics");
        super.renderReplacements(pageName, data, drain);
    }

}
