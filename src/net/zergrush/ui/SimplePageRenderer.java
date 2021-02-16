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

public abstract class SimplePageRenderer implements HTMLPane.PageGenerator {

    public static final Pattern TEMPLATE_TAG =
        Pattern.compile("\\{\\{([a-zA-Z0-9]+)\\}\\}");

    private final String template;

    public SimplePageRenderer(URL templateLocation) throws IOException {
        template = readResource(templateLocation, StandardCharsets.UTF_8);
    }

    public Map<String, String> createReplacementMap() {
        return new HashMap<>();
    }

    public abstract void renderReplacements(String pageName, Object data,
                                            Map<String, String> drain);

    public String renderPage(HTMLPane pane, String name, Object data) {
        Map<String, String> replacements = createReplacementMap();
        renderReplacements(name, data, replacements);
        StringBuffer sb = new StringBuffer();
        Matcher m = TEMPLATE_TAG.matcher(template);
        while (m.find()) {
            m.appendReplacement(sb, "");
            String replacement = replacements.get(m.group(1));
            if (replacement == null) replacement = "";
            sb.append(replacement);
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

}
