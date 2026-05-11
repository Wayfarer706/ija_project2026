package xyuguyn00.log;

public record UnitSnapshot (
    int x,
    int y,
    String type,
    String owner,
    int hp,
    boolean moved
) {}