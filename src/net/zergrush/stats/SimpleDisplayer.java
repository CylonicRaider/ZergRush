package net.zergrush.stats;

import java.util.Locale;

public class SimpleDisplayer<T> implements Statistics.Displayer<T> {

    private final Locale locale;
    private final String format;
    private final String nullValue;

    public SimpleDisplayer(Locale locale, String format, String nullValue) {
        if (format == null) throw new NullPointerException();
        this.locale = locale;
        this.format = format;
        this.nullValue = nullValue;
    }
    public SimpleDisplayer(String format, String nullValue) {
        this(null, format, nullValue);
    }
    public SimpleDisplayer(String format) {
        this(null, format, null);
    }

    public Locale getLocale() {
        return locale;
    }

    public String getFormat() {
        return format;
    }

    public String getNullValue() {
        return nullValue;
    }

    public String display(T value) {
        if (value == null && nullValue != null) return nullValue;
        return String.format(locale, format, value);
    }

}
