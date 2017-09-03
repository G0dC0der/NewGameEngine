package pojahn.game.essentials.stages;

import com.badlogic.gdx.graphics.Pixmap;
import pojahn.game.core.Level;

public abstract class PixelBasedLevel extends Level {

    private static final byte HOLLOW = 0;
    private static final byte SOLID = 1;
    private static final byte GOAL = 2;
    private static final byte LETHAL = 3;
    private static final byte CUSTOM_1 = 4;
    private static final byte CUSTOM_2 = 5;
    private static final byte CUSTOM_3 = 6;
    private static final byte CUSTOM_4 = 7;
    private static final byte CUSTOM_5 = 8;
    private static final byte CUSTOM_6 = 9;
    private static final byte CUSTOM_7 = 10;
    private static final byte CUSTOM_8 = 11;
    private static final byte CUSTOM_9 = 12;
    private static final byte CUSTOM_10 = 13;

    /**
     * Maps to Tile.HOLLOW. RGB:125,125,125.
     */
    public static final int GRAY = 0x7d7d7dff;
    /**
     * Maps to Tile.SOLID. RGB:90,90,90
     */
    public static final int DARK_GRAY = 0x5a5a5aff;
    /**
     * Maps to Tile.GOAL. RGB:255,0,0
     */
    public static final int RED = 0xff0000ff;
    /**
     * Maps to Tile.LETHAL. RGB:255,255,0
     */
    public static final int YELLOW = 0xffff00ff;
    /**
     * Maps to Tile.CUSTOM_1. RGB:0,255,0
     */
    public static final int GREEN_1 = 0x00ff00ff;
    /**
     * Maps to Tile.CUSTOM_2. RGB:0,220,0
     */
    public static final int GREEN_2 = 0x00dc00ff;
    /**
     * Maps to Tile.CUSTOM_3. RGB:0,190,0
     */
    public static final int GREEN_3 = 0x00be00ff;
    /**
     * Maps to Tile.CUSTOM_4. RGB:0,160,0
     */
    public static final int GREEN_4 = 0x00a000ff;
    /**
     * Maps to Tile.CUSTOM_5. RGB:0,130,0
     */
    public static final int GREEN_5 = 0x008200ff;
    /**
     * Maps to Tile.CUSTOM_6. RGB:0,100,0
     */
    public static final int GREEN_6 = 0x006400ff;
    /**
     * Maps to Tile.CUSTOM_7. RGB:0,70,0
     */
    public static final int GREEN_7 = 0x004600ff;
    /**
     * Maps to Tile.CUSTOM_8. RGB:0,40,0
     */
    public static final int GREEN_8 = 0x002800ff;
    /**
     * Maps to Tile.CUSTOM_9. RGB:0,10,0
     */
    public static final int GREEN_9 = 0x000a00ff;
    /**
     * Maps to Tile.CUSTOM_10. RGB:0,0,0
     */
    public static final int GREEN_10 = 0x000000ff;

    private byte[][] stageData;

    protected PixelBasedLevel() {
    }

    public void createMap(Pixmap map) {
        stageData = new byte[map.getWidth()][map.getHeight()];

        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                int color = map.getPixel(x, y);

                switch (color) {
                    case DARK_GRAY:
                        stageData[x][y] = SOLID;
                        break;
                    case RED:
                        stageData[x][y] = GOAL;
                        break;
                    case YELLOW:
                        stageData[x][y] = LETHAL;
                        break;
                    case GREEN_1:
                        stageData[x][y] = CUSTOM_1;
                        break;
                    case GREEN_2:
                        stageData[x][y] = CUSTOM_2;
                        break;
                    case GREEN_3:
                        stageData[x][y] = CUSTOM_3;
                        break;
                    case GREEN_4:
                        stageData[x][y] = CUSTOM_4;
                        break;
                    case GREEN_5:
                        stageData[x][y] = CUSTOM_5;
                        break;
                    case GREEN_6:
                        stageData[x][y] = CUSTOM_6;
                        break;
                    case GREEN_7:
                        stageData[x][y] = CUSTOM_7;
                        break;
                    case GREEN_8:
                        stageData[x][y] = CUSTOM_8;
                        break;
                    case GREEN_9:
                        stageData[x][y] = CUSTOM_9;
                        break;
                    case GREEN_10:
                        stageData[x][y] = CUSTOM_10;
                        break;
                    case GRAY:
                    default:
                        stageData[x][y] = HOLLOW;
                        break;
                }
            }
        }
        map.dispose();
    }

    @Override
    protected Tile tileAtInternal(int x, int y) {
        return mapToTile(stageData[x][y]);
    }

    @Override
    public int getWidth() {
        return stageData.length;
    }

    @Override
    public int getHeight() {
        return stageData[0].length;
    }

    static byte mapToByte(Tile tile) {
        switch (tile) {
            case HOLLOW:
                return HOLLOW;
            case SOLID:
                return SOLID;
            case LETHAL:
                return LETHAL;
            case GOAL:
                return GOAL;
            case CUSTOM_1:
                return CUSTOM_1;
            case CUSTOM_2:
                return CUSTOM_2;
            case CUSTOM_3:
                return CUSTOM_3;
            case CUSTOM_4:
                return CUSTOM_4;
            case CUSTOM_5:
                return CUSTOM_5;
            case CUSTOM_6:
                return CUSTOM_6;
            case CUSTOM_7:
                return CUSTOM_7;
            case CUSTOM_8:
                return CUSTOM_8;
            case CUSTOM_9:
                return CUSTOM_9;
            case CUSTOM_10:
                return CUSTOM_10;
            default:
                throw new RuntimeException();
        }
    }

    static Tile mapToTile(byte tile) {
        switch (tile) {
            case HOLLOW:
                return Tile.HOLLOW;
            case SOLID:
                return Tile.SOLID;
            case LETHAL:
                return Tile.LETHAL;
            case GOAL:
                return Tile.GOAL;
            case CUSTOM_1:
                return Tile.CUSTOM_1;
            case CUSTOM_2:
                return Tile.CUSTOM_2;
            case CUSTOM_3:
                return Tile.CUSTOM_3;
            case CUSTOM_4:
                return Tile.CUSTOM_4;
            case CUSTOM_5:
                return Tile.CUSTOM_5;
            case CUSTOM_6:
                return Tile.CUSTOM_6;
            case CUSTOM_7:
                return Tile.CUSTOM_7;
            case CUSTOM_8:
                return Tile.CUSTOM_8;
            case CUSTOM_9:
                return Tile.CUSTOM_9;
            case CUSTOM_10:
                return Tile.CUSTOM_10;
            default:
                throw new RuntimeException();
        }
    }
}
