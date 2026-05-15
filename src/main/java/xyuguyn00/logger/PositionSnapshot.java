/**
 * Project: Advance Wars Clone
 * Authors: Mariia Zhdaniuk
 * Description: Serializes coordinates. Kept separate from the common Position class 
 * to ensure that internal Engine logic is completely isolated from Logger specifications.
 */
package xyuguyn00.logger;

public record PositionSnapshot (
    int x,
    int y
) {}