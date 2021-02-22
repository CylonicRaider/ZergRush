package net.zergrush.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class TablePageRenderer<T> extends SimplePageRenderer {

    public TablePageRenderer() throws IOException {
        super(TablePageRenderer.class.getResource("/res/template.html"));
    }

    protected List<String> createRowList() {
        return new ArrayList<>();
    }

    protected abstract void renderHeaders(List<String> drain);

    protected abstract Iterator<T> getDataIterator(Object data);

    protected abstract void renderRow(T item, List<String> drain);

    protected void renderReplacements(String pageName, Object data,
                                      Map<String, String> drain) {
        StringBuilder tb = new StringBuilder();
        tb.append(getOrEmpty(drain, "content_top"));
        tb.append("<table border=\"0\" cellpadding=\"0\" " +
            "cellspacing=\"0\">\n<tr>");
        List<String> rowDrain = createRowList();
        renderHeaders(rowDrain);
        for (String header : rowDrain) {
            tb.append("<th>").append(header).append("</th>");
        }
        tb.append("</tr>\n");
        Iterator<T> di = getDataIterator(data);
        while (di.hasNext()) {
            tb.append("<tr>");
            rowDrain.clear();
            renderRow(di.next(), rowDrain);
            for (String value : rowDrain) {
                tb.append("<td>").append(value).append("</td>");
            }
            tb.append("</tr>\n");
        }
        tb.append("</table>\n");
        tb.append(getOrEmpty(drain, "content_bottom"));
        drain.put("content", tb.toString());
        if (! drain.containsKey("window_title")) {
            drain.put("window_title", drain.get("title"));
        }
    }

}
