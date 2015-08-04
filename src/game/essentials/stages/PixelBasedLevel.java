package game.essentials.stages;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Vector2;

import game.core.Level;

public abstract class PixelBasedLevel extends Level{
	
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
	public static final Color GRAY 	  	  = Color.valueOf("7d7d7dff");
	/**
	 * Maps to Tile.SOLID. RGB:90,90,90
	 */
	public static final Color DARK_GRAY   = Color.valueOf("5a5a5aff");
	/**
	 * Maps to Tile.GOAL. RGB:255,0,0
	 */
	public static final Color RED		  = Color.valueOf("ff0000ff");
	/**
	 * Maps to Tile.LETHAL. RGB:255,255,0
	 */
	public static final Color YELLOW      = Color.valueOf("ffff00ff");
	/**
	 * Maps to Tile.CUSTOM_1. RGB:0,255,0
	 */
	public static final Color GREEN_1	  = Color.valueOf("00ff00ff");
	/**
	 * Maps to Tile.CUSTOM_2. RGB:0,220,0
	 */
	public static final Color GREEN_2	  = Color.valueOf("00dc00ff");
	/**
	 * Maps to Tile.CUSTOM_3. RGB:0,190,0
	 */
	public static final Color GREEN_3	  = Color.valueOf("00be00ff");
	/**
	 * Maps to Tile.CUSTOM_4. RGB:0,160,0
	 */
	public static final Color GREEN_4	  = Color.valueOf("00a000ff");
	/**
	 * Maps to Tile.CUSTOM_5. RGB:0,130,0
	 */
	public static final Color GREEN_5	  = Color.valueOf("008200ff");
	/**
	 * Maps to Tile.CUSTOM_6. RGB:0,100,0
	 */
	public static final Color GREEN_6	  = Color.valueOf("006400ff");
	/**
	 * Maps to Tile.CUSTOM_7. RGB:0,70,0
	 */
	public static final Color GREEN_7	  = Color.valueOf("004600ff");
	/**
	 * Maps to Tile.CUSTOM_8. RGB:0,40,0
	 */
	public static final Color GREEN_8	  = Color.valueOf("002800ff");
	/**
	 * Maps to Tile.CUSTOM_9. RGB:0,10,0
	 */
	public static final Color GREEN_9	  = Color.valueOf("000a00ff");
	/**
	 * Maps to Tile.CUSTOM_10. RGB:0,0,0
	 */
	public static final Color GREEN_10	  = Color.valueOf("000000ff");
	
	private byte[][] stageData;
	private Map<Vector2, Byte> deforms; 
	
	protected PixelBasedLevel() {
		deforms = new HashMap<>();
	}
	
	public void createMap(Pixmap map){
		stageData = new byte[map.getHeight()][map.getWidth()];
		
		for (int y = 0; y < map.getHeight(); y++){
			for (int x = 0; x < map.getWidth(); x++){
				Color c = new Color(map.getPixel(x, y));
				
				if (c.equals(GRAY))
					stageData[y][x] = HOLLOW;
				else if (c.equals(DARK_GRAY))
					stageData[y][x] = SOLID;
				else if (c.equals(RED))
					stageData[y][x] = GOAL;
				else if (c.equals(YELLOW))
					stageData[y][x] = LETHAL;
				else if (c.equals(GREEN_1))
					stageData[y][x] = CUSTOM_1;
				else if (c.equals(GREEN_2))
					stageData[y][x] = CUSTOM_2;
				else if (c.equals(GREEN_3))
					stageData[y][x] = CUSTOM_3;
				else if (c.equals(GREEN_4))
					stageData[y][x] = CUSTOM_4;
				else if (c.equals(GREEN_5))
					stageData[y][x] = CUSTOM_5;
				else if (c.equals(GREEN_6))
					stageData[y][x] = CUSTOM_6;
				else if (c.equals(GREEN_7))
					stageData[y][x] = CUSTOM_7;
				else if (c.equals(GREEN_8))
					stageData[y][x] = CUSTOM_8;
				else if (c.equals(GREEN_9))
					stageData[y][x] = CUSTOM_9;
				else if (c.equals(GREEN_10))
					stageData[y][x] = CUSTOM_10;
				else
					stageData[y][x] = HOLLOW;
			}
		}
	}
	
	
	@Override
	public boolean isHollow(int x, int y) {
		return tileAt(y,x) == Tile.HOLLOW;
	}
	
	@Override
	public boolean isSolid(int x, int y) {
		return tileAt(y,x) == Tile.SOLID;
	}
	
	@Override
	public Tile tileAt(int x, int y) {
		Byte tile = deforms.get(new Vector2(x,y));
		if(tile != null)
			return mapToTile(tile);
		
		return mapToTile(stageData[y][x]);
	}
	
	public void reshape(int x, int y, Tile tile){
		deforms.put(new Vector2(x,y), mapToByte(tile));
	}
	
	public void restoreShape(){
		deforms.clear();
	}
	
	@Override
	public int getWidth() {
		return stageData[0].length;
	}
	
	@Override
	public int getHeight() {
		return stageData.length;
	}
	
	static byte mapToByte(Tile tile){
		switch(tile){
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
	
	static Tile mapToTile(byte tile){
		switch(tile){
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
