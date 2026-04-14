package xyuguyn00.tool;

public interface Observable {
    void addObserver(GameObserver observer);
    void removeObserver(GameObserver observer);
    void notifyObservers();
}
