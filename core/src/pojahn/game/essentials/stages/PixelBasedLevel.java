package pojahn.game.essentials.stages;

import com.badlogic.gdx.graphics.Pixmap;
import com.google.common.collect.ImmutableMap;
import pojahn.game.core.Level;

import java.util.Map;

public abstract class PixelBasedLevel extends Level {

    private static final Map<Integer, Byte> COLOR_BYTE_MAP = ImmutableMap.<Integer, Byte>builder()
        .put(0x7d7d7dff, (byte) 0)
        .put(0x5a5a5aff, (byte) 1)
        .put(0xff0000ff, (byte) 2)
        .put(0xffff00ff, (byte) 3)
        .put(0x00ff00ff, (byte) 4)
        .put(0x00dc00ff, (byte) 5)
        .put(0x00be00ff, (byte) 6)
        .put(0x00a000ff, (byte) 7)
        .put(0x008200ff, (byte) 8)
        .put(0x006400ff, (byte) 9)
        .put(0x004600ff, (byte) 10)
        .put(0x002800ff, (byte) 11)
        .put(0x000a00ff, (byte) 12)
        .put(0x000000ff, (byte) 13)
        .build();

    private static final Map<Byte, Tile> BYTE_TILE_MAP = ImmutableMap.<Byte, Tile>builder()
        .put((byte) 0, Tile.HOLLOW)
        .put((byte) 1, Tile.SOLID)
        .put((byte) 2, Tile.GOAL)
        .put((byte) 3, Tile.LETHAL)
        .put((byte) 4, Tile.CUSTOM_1)
        .put((byte) 5, Tile.CUSTOM_2)
        .put((byte) 6, Tile.CUSTOM_3)
        .put((byte) 7, Tile.CUSTOM_4)
        .put((byte) 8, Tile.CUSTOM_5)
        .put((byte) 9, Tile.CUSTOM_6)
        .put((byte) 10, Tile.CUSTOM_7)
        .put((byte) 11, Tile.CUSTOM_8)
        .put((byte) 12, Tile.CUSTOM_9)
        .put((byte) 13, Tile.CUSTOM_10)
        .build();


    private byte[][] stageData;

    protected PixelBasedLevel() {
    }

    public void createMap(final Pixmap map) {
        stageData = new byte[map.getWidth()][map.getHeight()];

        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                stageData[x][y] = COLOR_BYTE_MAP.getOrDefault(map.getPixel(x, y), (byte) 0);
            }
        }
        map.dispose();
    }

    @Override
    protected Tile tileAtInternal(final int x, final int y) {
        return BYTE_TILE_MAP.get(stageData[x][y]);
    }

    @Override
    public int getWidth() {
        return stageData.length;
    }

    @Override
    public int getHeight() {
        return stageData[0].length;
    }
}
