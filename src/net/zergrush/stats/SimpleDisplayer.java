package net.zergrush.stats;

import java.util.Locale;

public class SimpleDisplayer<T> implements Statistics.Displayer<T> {

    private final Locale locale;
    private final String format;

    public SimpleDisplayer(Locale locale, String format) {
        if (format == null) throw new NullPointerException();
        this.locale = locale;
        this.format = format;
    }
    public SimpleDisplayer(String format) {
        this(null, format);
    }

    public Locale getLocale() {
        return locale;
    }

    public String getFormat() {
        return format;
    }

    public String display(T value) {
        return String.format(locale, format, value);
    }

}
