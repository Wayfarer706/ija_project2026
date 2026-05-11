package xyuguyn00.log;

public record BuildingSnapshot (
    int x,
    int y,
    String type,
    String owner,
    int capturePoints
) {}