package net.zergrush.ui;

import java.util.Map;
import net.zergrush.stats.Statistics;

public class StatisticsPageRenderer extends TablePageRenderer {

    protected void renderCells(Object data, CellWriter writer) {
        writer.nextColumn().align(Alignment.LEFT).width(Width.COMPACT);
        writer.nextColumn().align(Alignment.LEFT);
        writer.header("Statistic");
        writer.header("Value");
        writer.finishRow();
        for (Statistics.Entry<?> item : ((Statistics) data).entries()) {
            writer.data(escapeHTML(item.getDescription()) + ":&nbsp;");
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
