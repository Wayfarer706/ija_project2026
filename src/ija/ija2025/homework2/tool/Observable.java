package ija.ija2025.homework2.tool;

public interface Observable {
    void addObserver(GameObserver observer);
    void removeObserver(GameObserver observer);
    void notifyObservers();
}
