package net.zergrush.ui;

import java.util.Map;

public abstract class TablePageRenderer extends SimplePageRenderer {

    protected static class CellWriter {

        private final StringBuilder drain;
        private boolean insideRow;

        public CellWriter(StringBuilder drain) {
            this.drain = drain;
        }

        private void maybeStartRow() {
            if (insideRow) return;
            drain.append("<tr>");
            insideRow = true;
        }

        public void header(String html) {
            maybeStartRow();
            drain.append("<th>").append(html).append("</th>");
        }

        public void headerEsc(Object value) {
            header(escapeHTML(String.valueOf(value)));
        }

        public void data(String html) {
            maybeStartRow();
            drain.append("<td>").append(html).append("</td>");
        }

        public void dataEsc(Object value) {
            data(escapeHTML(String.valueOf(value)));
        }

        public void finishRow() {
            if (! insideRow) return;
            drain.append("</tr>\n");
            insideRow = false;
        }

        public void nextRow() {
            maybeStartRow();
            finishRow();
        }

    }

    public TablePageRenderer() {
        super(TablePageRenderer.class, "/res/template.html");
    }

    protected abstract void renderCells(Object data, CellWriter writer);

    protected void renderReplacements(String pageName, Object data,
                                      Map<String, String> drain) {
        StringBuilder tb = new StringBuilder();
        tb.append(getOrEmpty(drain, "content_top"));
        boolean form = Boolean.parseBoolean(drain.get("form"));
        if (form) {
            tb.append("<form action=\"\" method=\"post\" " +
                "enctype=\"application/x-www-form-urlencoded\">");
            tb.append(getOrEmpty(drain, "form_top"));
        }
        tb.append("<table border=\"0\" cellpadding=\"0\" " +
            "cellspacing=\"0\" width=\"100%\">\n");
        CellWriter writer = new CellWriter(tb);
        renderCells(data, writer);
        writer.finishRow();
        tb.append("</table>\n");
        if (form) {
            tb.append(getOrEmpty(drain, "form_bottom"));
            tb.append("</form>\n");
        }
        tb.append(getOrEmpty(drain, "content_bottom"));
        drain.put("content", tb.toString());
        if (! drain.containsKey("window_title")) {
            drain.put("window_title", drain.get("title"));
        }
    }

}
