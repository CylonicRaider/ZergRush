package net.zergrush.stats;

public interface HighscoresStorage {

    boolean retrieveHighscores(Highscores drain);

    boolean storeHighscores(Highscores source);

}
