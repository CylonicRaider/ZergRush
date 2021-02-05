package net.zergrush.stats;

import java.util.prefs.Preferences;
import net.zergrush.Game;
import net.zergrush.xml.XMLConversionException;
import net.zergrush.xml.XMLConverterDriver;

public class PreferencesHighscoresStorage implements HighscoresStorage {

    public static final String DEFAULT_KEY = "highscores";

    private static final PreferencesHighscoresStorage DEFAULT =
        new PreferencesHighscoresStorage();

    private final Preferences node;
    private final String key;

    protected PreferencesHighscoresStorage(Preferences node, String key) {
        this.node = node;
        this.key = key;
    }
    protected PreferencesHighscoresStorage() {
        this(Preferences.userNodeForPackage(Game.class), DEFAULT_KEY);
    }

    public Highscores createHighscores() {
        return new Highscores();
    }

    public boolean retrieveHighscores(Highscores drain) {
        String data = node.get(key, null);
        if (data == null) return false;
        Highscores replacement;
        try {
            replacement = XMLConverterDriver.getDefault().read(data,
                "highscores", Highscores.class);
        } catch (XMLConversionException exc) {
            return false;
        }
        drain.clear();
        drain.addAll(replacement.getEntries());
        return true;
    }

    public boolean storeHighscores(Highscores source) {
        String data;
        try {
            data = XMLConverterDriver.getDefault().write("highscores",
                                                         source);
        } catch (XMLConversionException exc) {
            return false;
        }
        node.put(key, data);
        return true;
    }

    public static PreferencesHighscoresStorage getDefault() {
        return DEFAULT;
    }

}
