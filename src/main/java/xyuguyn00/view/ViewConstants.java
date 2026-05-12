package xyuguyn00.view;

public final class ViewConstants {
    
    // Prevent instantiation of this utility class
    private ViewConstants() {}

    // Core Board Size
    public static final int TILE_SIZE = 60;
    
    // Fonts
    public static final String FONT_MAIN = "Arial";
    public static final int FONT_SIZE_BADGE = 10;

    // Unit Image Sizing
    public static final int UNIT_PADDING = 10;
    public static final int UNIT_SIZE = TILE_SIZE - UNIT_PADDING;

    // HP Badge Sizing & Offsets
    public static final int HP_BADGE_WIDTH = 22;
    public static final int HP_BADGE_HEIGHT = 14;
    // Calculate the position so it dynamically anchors to the bottom-right
    public static final double HP_OFFSET_X = (TILE_SIZE / 2.0) - (HP_BADGE_WIDTH / 2.0) - 2;
    public static final double HP_OFFSET_Y = (TILE_SIZE / 2.0) - (HP_BADGE_HEIGHT / 2.0) - 2;

    // CP Badge Sizing & Offsets
    public static final int CP_BADGE_WIDTH = 24;
    public static final int CP_BADGE_HEIGHT = 14;
    // Calculate the position so it dynamically anchors to the top-left
    public static final double CP_OFFSET_X = -(TILE_SIZE / 2.0) + (CP_BADGE_WIDTH / 2.0) + 2;
    public static final double CP_OFFSET_Y = -(TILE_SIZE / 2.0) + (CP_BADGE_HEIGHT / 2.0) + 2;
    
    // Path Dot Sizing
    public static final double PATH_DOT_RADIUS = TILE_SIZE / 6.0;
}