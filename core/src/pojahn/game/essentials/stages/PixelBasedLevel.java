package pojahn.game.essentials.stages;

import com.badlogic.gdx.graphics.Pixmap;
import pojahn.game.core.Level;

import java.util.AbstractMap;
import java.util.Map;

public abstract class PixelBasedLevel extends Level {

    private static final Map<Integer, Byte> COLOR_BYTE_MAP = Map.ofEntries(
        new AbstractMap.SimpleEntry<>(0x7d7d7dff, (byte) 0),
        new AbstractMap.SimpleEntry<>(0x5a5a5aff, (byte) 1),
        new AbstractMap.SimpleEntry<>(0xff0000ff, (byte) 2),
        new AbstractMap.SimpleEntry<>(0xffff00ff, (byte) 3),
        new AbstractMap.SimpleEntry<>(0x00ff00ff, (byte) 4),
        new AbstractMap.SimpleEntry<>(0x00dc00ff, (byte) 5),
        new AbstractMap.SimpleEntry<>(0x00be00ff, (byte) 6),
        new AbstractMap.SimpleEntry<>(0x00a000ff, (byte) 7),
        new AbstractMap.SimpleEntry<>(0x008200ff, (byte) 8),
        new AbstractMap.SimpleEntry<>(0x006400ff, (byte) 9),
        new AbstractMap.SimpleEntry<>(0x004600ff, (byte) 10),
        new AbstractMap.SimpleEntry<>(0x002800ff, (byte) 11),
        new AbstractMap.SimpleEntry<>(0x000a00ff, (byte) 12),
        new AbstractMap.SimpleEntry<>(0x000000ff, (byte) 13));

    private static final Map<Byte, Tile> BYTE_TILE_MAP = Map.ofEntries(
        new AbstractMap.SimpleEntry<>((byte) 0, Tile.HOLLOW),
        new AbstractMap.SimpleEntry<>((byte) 1, Tile.SOLID),
        new AbstractMap.SimpleEntry<>((byte) 2, Tile.GOAL),
        new AbstractMap.SimpleEntry<>((byte) 3, Tile.LETHAL),
        new AbstractMap.SimpleEntry<>((byte) 4, Tile.CUSTOM_1),
        new AbstractMap.SimpleEntry<>((byte) 5, Tile.CUSTOM_2),
        new AbstractMap.SimpleEntry<>((byte) 6, Tile.CUSTOM_3),
        new AbstractMap.SimpleEntry<>((byte) 7, Tile.CUSTOM_4),
        new AbstractMap.SimpleEntry<>((byte) 8, Tile.CUSTOM_5),
        new AbstractMap.SimpleEntry<>((byte) 9, Tile.CUSTOM_6),
        new AbstractMap.SimpleEntry<>((byte) 10, Tile.CUSTOM_7),
        new AbstractMap.SimpleEntry<>((byte) 11, Tile.CUSTOM_8),
        new AbstractMap.SimpleEntry<>((byte) 12, Tile.CUSTOM_9),
        new AbstractMap.SimpleEntry<>((byte) 13, Tile.CUSTOM_10));

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
