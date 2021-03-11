package net.zergrush.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class SimplePageRenderer implements HTMLPane.PageRenderer {

    public static final Pattern TEMPLATE_TAG =
        Pattern.compile("\\{\\{([a-zA-Z0-9_]+|[<>])\\}\\}");

    private final URL base;
    private final String template;

    public SimplePageRenderer(URL base, String template) {
        this.base = base;
        this.template = template;
    }
    public SimplePageRenderer(URL templateLocation) throws IOException {
        this(templateLocation,
             readResource(templateLocation, StandardCharsets.UTF_8));
    }
    public SimplePageRenderer(Class<?> reference, String resourceName) {
        this(reference.getResource(resourceName),
             readResourceOrBailOut(reference.getResource(resourceName),
                                   StandardCharsets.UTF_8));
    }

    public URL getBaseURL() {
        return base;
    }

    protected Map<String, String> createReplacementMap() {
        Map<String, String> ret = new HashMap<>();
        ret.put("<", "{{");
        ret.put(">", "}}");
        return ret;
    }

    protected abstract void renderReplacements(String pageName, Object data,
                                               Map<String, String> drain);

    public String renderPage(HTMLPane pane, String name, Object data) {
        Map<String, String> replacements = createReplacementMap();
        renderReplacements(name, data, replacements);
        StringBuffer sb = new StringBuffer();
        Matcher m = TEMPLATE_TAG.matcher(template);
        while (m.find()) {
            m.appendReplacement(sb, "");
            sb.append(getOrEmpty(replacements, m.group(1)));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String readResource(URL location, Charset cs)
            throws IOException {
        InputStream input = location.openStream();
        try {
            Reader rd = new InputStreamReader(input, cs);
            char[] buf = new char[2048];
            int length = 0;
            for (;;) {
                int read = rd.read(buf, length, buf.length - length);
                if (read == -1) break;
                length += read;
                if (length == buf.length) {
                    char[] newbuf = new char[buf.length * 2];
                    System.arraycopy(buf, 0, newbuf, 0, length);
                    buf = newbuf;
                }
            }
            return new String(buf, 0, length);
        } finally {
            input.close();
        }
    }
    private static String readResourceOrBailOut(URL location, Charset cs) {
        try {
            return readResource(location, cs);
        } catch (IOException exc) {
            throw new RuntimeException(exc);
        }
    }

    public static String nullToEmpty(String value) {
        return (value == null) ? "" : value;
    }

    public static String getOrEmpty(Map<?, String> map, Object key) {
        return nullToEmpty(map.get(key));
    }

    public static String escapeHTML(String text) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0, ch; i < text.length(); i += Character.charCount(ch)) {
            ch = text.codePointAt(i);
            if (ch == '<' || ch == '>' || ch == '&' || ch == '"' ||
                    ch == '\'' || ch < ' ' || ch > '~') {
                sb.append("&#").append(ch).append(';');
            } else {
                sb.appendCodePoint(ch);
            }
        }
        return sb.toString();
    }

}
