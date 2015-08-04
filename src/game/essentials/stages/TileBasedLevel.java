package game.essentials.stages;


import game.core.Entity;
import game.core.Level;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;

public abstract class TileBasedLevel extends Level{
	
	private int width, height, tilesX, tilesY, tileWidth, tileHeight;
	private Entity image;
	private TiledMap map;
	private TiledMapRenderer tiledMapRenderer;
	private MapProperties props;
	
	protected TileBasedLevel() {}
	
	public void createMap(TiledMap map){
		this.map = map;
		props = map.getProperties();
		tilesX = props.get("width", Integer.class);
		tilesY = props.get("height", Integer.class);
		tileWidth = props.get("tilewidth", Integer.class);
		tileHeight = props.get("tileheight", Integer.class);
		width = tilesX * tileWidth;
		height = tilesY * tileHeight;
	}
	
	@Override
	public int getWidth() {
		return width;
	}
	
	@Override
	public int getHeight() {
		return height;
	}
	
	public void setTile(float x, float y, Cell tile){
		//TODO:
	}
	
	@Override
	public Tile tileAt(int x, int y) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean isHollow(int x, int y) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean isSolid(int x, int y) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public Entity getImage(){
		if(image == null){
			image = new Entity(){
				@Override
				public void render(SpriteBatch batch) {
					if(tiledMapRenderer == null){
						tiledMapRenderer = new OrthogonalTiledMapRenderer(map, batch);
					}
					
					if(getRotation() != 0 || scaleX != 0 || scaleY != 0 || flipX || flipY)
						throw new RuntimeException("Rotation, scale and flip are not supported for TileBased image.");

					Color color = batch.getColor();
					Color newColor = new Color(color);
					newColor.a = alpha;

					batch.setColor(newColor);
					tiledMapRenderer.setView(getEngine().getCamera());
					tiledMapRenderer.render();
					
					batch.setColor(color);
				}
			};
		}
		
		return image;
	}
	
	private void encode(){
		TiledMapTileLayer layer = (TiledMapTileLayer)map.getLayers().get(0); 
		List<Vector2> checked = new ArrayList<>();
		
		for(int x = 0; x < tilesX; x++){
			for(int y = 0; y < tilesY; y++){
				TiledMapTile tile = layer.getCell(x, y).getTile();
				Vector2 offset = new Vector2(tile.getOffsetX(), tile.getOffsetY());
//				if(!checked.contains(offset))
			}
		}
		
		//Translate the tiles present in the tmx to a bunch of 2d byte arrays.
		//TODO:
	}
}
