package net.zergrush.stats;

public interface HighscoresStorage {

    Highscores createHighscores();

    boolean retrieveHighscores(Highscores drain);

    boolean storeHighscores(Highscores source);

}
