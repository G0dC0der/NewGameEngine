package game.essentials.stages;


import java.awt.Dimension;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;

import game.core.Entity;
import game.core.Level;

public abstract class TileBasedLevel extends Level{
	
	private int width, height, tilesX, tilesY, tileWidth, tileHeight;
	private Entity image;
	private TiledMap map;
	private TiledMapRenderer tiledMapRenderer;
	private TiledMapTileLayer layer;
	private OrthographicCamera camera;
	private MapProperties props;
	private byte[][] tiledata;
	
	protected TileBasedLevel(){
	}
	
	public void createMap(TiledMap map){
		this.map = map;
		props = map.getProperties();
		layer =  (TiledMapTileLayer)map.getLayers().get(0);
		tilesX = props.get("width", Integer.class);
		tilesY = props.get("height", Integer.class);
		tileWidth = props.get("tilewidth", Integer.class);
		tileHeight = props.get("tileheight", Integer.class);
		width = tilesX * tileWidth;
		height = tilesY * tileHeight;
		Dimension size = getEngine().getScreenSize();
		camera = new OrthographicCamera();
		camera.setToOrtho(true, size.width, size.height);
		encode();
	}
	
	@Override
	public int getWidth() {
		return width;
	}
	
	@Override
	public int getHeight() {
		return height;
	}
	
	@Override
	public Tile tileAt(int x, int y) {
		int tileX = x % tileWidth;
		int tileY = layer.getHeight() - (y % tileHeight);
		int relativeX = x - tileX;
		int relativeY = y - tileY;
		
		Cell cell = layer.getCell(tileX, tileY);
		if(cell != null){
			TextureRegion region = cell.getTile().getTextureRegion();
			int offX = region.getRegionX();
			int offY = region.getRegionY();
			
			return PixelBasedLevel.mapToTile(tiledata[offY + relativeX][offX + relativeY]);
		}
		else
			return Tile.HOLLOW;
	}
	
	@Override
	public boolean isHollow(int x, int y) {
		return tileAt(x,y) == Tile.HOLLOW;
	}
	
	@Override
	public boolean isSolid(int x, int y) {
		return tileAt(x,y) == Tile.SOLID;
	}
	
	public Entity getImage(){
		if(image == null){
			image = new Entity(){
				@Override
				public void render(SpriteBatch batch) {
					if(tiledMapRenderer == null)
						tiledMapRenderer = new OrthogonalTiledMapRenderer(map, batch);
					
					if(getRotation() != 0 || flipX || flipY)
						throw new RuntimeException("Rotation, scale and flip are not supported for TileBased image.");

					Color color = batch.getColor();
					Color newColor = new Color(color);
					newColor.a = alpha;

					batch.setColor(newColor);
					
					Vector2 trans = getEngine().getTranslation();
					camera.position.x = trans.x;
					camera.position.y = trans.y;
					camera.zoom = getEngine().getZoom();
					//TODO: Handle the damn rotation value
					camera.update();
					
					tiledMapRenderer.setView(camera);
					tiledMapRenderer.render();
					
					batch.setColor(color);
				}
			};
		}
		
		return image;
	}
	
	private void encode(){
		for(int x = 0; x < tilesX; x++){
			for(int y = 0; y < tilesY; y++){
				Cell cell = layer.getCell(x, y);
				if(cell != null){
					TextureData tdata = cell.getTile().getTextureRegion().getTexture().getTextureData();
					tdata.prepare();
					Pixmap pix = tdata.consumePixmap();
					
					tiledata = new byte[pix.getHeight()][pix.getWidth()];
					for(int y2 = 0; y2 < tiledata.length; y2++){
						for(int x2 = 0; x2 < tiledata[y2].length; x2++)
							tiledata[y2][x2] = (byte) (new Color(pix.getPixel(x2, y2)).a == 0 ? 0 : 1);
					}
					return;
				}
			}
		}
	}
}
