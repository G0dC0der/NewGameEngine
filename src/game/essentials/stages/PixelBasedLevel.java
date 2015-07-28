package game.essentials.stages;

import java.util.HashMap;
import java.util.Map;

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
	
	private byte[][] stageData;
	private Map<Vector2, Byte> deforms; 
	
	protected PixelBasedLevel() {
		deforms = new HashMap<>();
	}
	
	public final void generateData(Pixmap map){
		
	}
	
	
	@Override
	public boolean isHollow(int x, int y) {
		return tileAt(x,y) == Tile.HOLLOW;
	}
	
	@Override
	public boolean isSolid(int x, int y) {
		return tileAt(x,y) == Tile.SOLID;
	}
	
	@Override
	public Tile tileAt(int x, int y) {
		Byte tile = deforms.get(new Vector2(x,y));
		if(tile != null)
			return map(tile);
		
		return map(stageData[x][y]);
	}
	
	public void deform(int x, int y, Tile tile){
		deforms.put(new Vector2(x,y), map(tile));
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
	
	private static byte map(Tile tile){
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
	
	private static Tile map(byte tile){
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
