package net.zergrush;

public class KeyboardAction {

    private final String key;
    private final String keyDescription;
    private final String description;

    public KeyboardAction(String key, String keyDescription,
                          String description) {
        this.key = key;
        this.keyDescription = keyDescription;
        this.description = description;
    }
    public KeyboardAction(String key, String description) {
        this(key.toUpperCase(), key, description);
    }

    public String getKey() {
        return key;
    }

    public String getKeyDescription() {
        return keyDescription;
    }

    public String getDescription() {
        return description;
    }

}
