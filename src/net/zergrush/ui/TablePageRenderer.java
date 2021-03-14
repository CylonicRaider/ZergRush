package net.zergrush.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class TablePageRenderer extends SimplePageRenderer {

    protected enum Alignment {
        LEFT, CENTER, RIGHT;

        public String toString() {
            return name().toLowerCase();
        }
    }

    protected enum Width { COMPACT }

    protected static class CellParameters {

        private Alignment align;
        private Width width;

        public CellParameters() {
            align = null;
            width = null;
        }
        public CellParameters(CellParameters copyFrom) {
            align = copyFrom.align();
            width = copyFrom.width();
        }

        public Alignment align() {
            return align;
        }

        public Width width() {
            return width;
        }

        public CellParameters align(Alignment newAlign) {
            align = newAlign;
            return this;
        }

        public CellParameters width(Width newWidth) {
            width = newWidth;
            return this;
        }

    }

    protected static class CellWriter {

        private final StringBuilder drain;
        private final List<CellParameters> colParams;
        private boolean insideRow;
        private int column;
        private CellParameters nextCellParams;

        public CellWriter(StringBuilder drain) {
            this.drain = drain;
            this.colParams = new ArrayList<>();
        }

        public CellParameters column(int index) {
            if (index < 0)
                throw new IndexOutOfBoundsException(
                    "Column index must be >= 0");
            while (colParams.size() >= index) colParams.add(null);
            CellParameters ret = colParams.get(index);
            if (ret == null) {
                ret = new CellParameters();
                colParams.set(index, ret);
            }
            return ret;
        }

        public CellParameters nextColumn() {
            colParams.add(new CellParameters());
            return colParams.get(colParams.size() - 1);
        }

        private void maybeStartRow() {
            if (insideRow) return;
            drain.append("<tr>");
            insideRow = true;
            column = 0;
        }

        public CellParameters params() {
            if (nextCellParams == null) {
                if (column < colParams.size())
                    nextCellParams = new CellParameters(
                        colParams.get(column));
                if (nextCellParams == null)
                    nextCellParams = new CellParameters();
            }
            return nextCellParams;
        }

        private void startCell(String tag) {
            drain.append('<').append(tag);
            if (nextCellParams == null && column < colParams.size()) {
                nextCellParams = colParams.get(column);
            }
            if (nextCellParams != null) {
                Alignment align = nextCellParams.align();
                if (align != null)
                    drain.append(" align=\"").append(align).append("\"");
                Width width = nextCellParams.width();
                if (width == Width.COMPACT)
                    drain.append(" width=\"0\" nowrap=\"nowrap\"");
            }
            drain.append('>');
            column++;
            nextCellParams = null;
        }

        public void header(String html) {
            maybeStartRow();
            startCell("th");
            drain.append(html);
            drain.append("</th>");
        }

        public void headerEsc(Object value) {
            header(escapeHTML(String.valueOf(value)));
        }

        public void data(String html) {
            maybeStartRow();
            startCell("td");
            drain.append(html);
            drain.append("</td>");
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
