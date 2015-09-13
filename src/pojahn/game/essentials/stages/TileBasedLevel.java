package pojahn.game.essentials.stages;


import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

import pojahn.game.core.Entity;
import pojahn.game.core.Level;

public abstract class TileBasedLevel extends Level{
	
	private int width, height, tilesX, tilesY, tileWidth, tileHeight;
	private float rotation;
	private Entity image;
	private TiledMap map;
	private TiledMapRenderer tiledMapRenderer;
	private TiledMapTileLayer layer;
	private OrthographicCamera camera;
	private MapProperties props;
	private Map<Integer, Byte> tileLayer;
	private HashMap<Integer, RegionData> tiledata;
	
	protected TileBasedLevel(){
		tileLayer = new HashMap<>();
		tiledata = new HashMap<>();
	}
	
	public void parse(TiledMap map){
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
		if(outOfBounds(x, y))
			return Tile.HOLLOW;
		
		int tileX = x / tileWidth;
		int tileY = y / tileHeight;
		
		Cell cell = layer.getCell(tileX, tileY);
		if(cell != null){
			TextureRegion region = cell.getTile().getTextureRegion();
			int key = region.getRegionX() * 31 + region.getRegionY();
			int relativeX = x - (tileX * tileWidth);
			int relativeY = y - (tileY * tileHeight);
			
			boolean[][] alpha = tiledata.get(key).alpha;
			
			return alpha[relativeX][relativeY] ? Tile.SOLID : Tile.HOLLOW;
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
	
	@Override
	public void setTileOnLayer(int x, int y, Tile tile) {
		int key = x * 31 + y;
		
		if(tile == null)
			tileLayer.remove(key);
		else
			tileLayer.put(key, PixelBasedLevel.mapToByte(tile));		
	}
	
	@Override
	public void removeTileOnLayer(int x, int y) {
		tileLayer.remove(x * 31 + y);		
	}
	
	@Override
	public void clearTileLayer() {
		tileLayer.clear();
	}
	
	public void setTile(int x, int y, Cell cell){
//		y = tilesY - y;
		
		if(x < 0 || x > tilesX || y < 0 || y > tilesY)
			return;
		
		
		layer.setCell(x, y, cell);
		//TODO:Allow it to expand
	}
	
	public void transformTiles(int cx, int cy, int radius, Cell cell) {
	    int rr = radius*radius;

	    for(int x = cx - radius; x <= cx + radius; ++x){
	        for(int y = cy - radius; y <= cy + radius; ++y){
	            int dx = cx - x;
	            int dy = cy - y;
	            if((dx*dx + dy*dy) < rr)
	                setTile(x, y, cell);
	         }
	    }
	}
	
	public void restoreTiles(){
		
	}
	
	public Entity getWorldImage(){
		if(image == null){
			image = new Entity(){{
//				zIndex(100);
			}
				@Override
				public void render(SpriteBatch batch) {
					if(tiledMapRenderer == null)
						tiledMapRenderer = new OrthogonalTiledMapRenderer(map, batch);

					if(getRotation() != 0 || flipX || flipY)
						throw new RuntimeException("Rotation and flip are not supported for tile based image.");

					Color color = batch.getColor();
					Color newColor = new Color(color);
					newColor.a = alpha;

					batch.setColor(newColor);
					
					camera.position.x = getEngine().tx();
					camera.position.y = getEngine().ty();
					camera.zoom = getEngine().getZoom();
					camera.rotate(-rotation);
					camera.rotate((rotation = getEngine().getRotation()));
					camera.update();
					
					tiledMapRenderer.setView(camera);
					tiledMapRenderer.renderTileLayer(layer);

					batch.setColor(color);
					getEngine().gameCamera();
				}
			};
		}
		
		return image;
	}
	
	private void encode(){
		List<TextureRegion> regions = new ArrayList<>();
		
		for(int x = 0; x < tilesX; x++){
			for(int y = 0; y < tilesY; y++){
				Cell cell = layer.getCell(x, y);
				if(cell != null){
					TextureRegion region = cell.getTile().getTextureRegion();
					if(!regions.contains(region)){
						regions.add(region);
					}
				}
			}
		}
		
		Pixmap pix = null;
		TextureData tdata = regions.get(0).getTexture().getTextureData();
		tdata.prepare();
		pix = tdata.consumePixmap();
		
		PixmapIO.writePNG(Gdx.files.absolute("C:/test.png"), pix);
		
		for(TextureRegion region : regions){
			int startX = region.getRegionX();
			int startY = region.getRegionY();
			int w = region.getRegionWidth();
			int h = region.getRegionHeight();
			int key = startX * 31 + startY;
			boolean[][] alpha = new boolean[w][h];
			
			for(int x2 = 0; x2 < w; x2++){
				for(int y2 = 0; y2 < h; y2++){
					alpha[x2][y2] = new Color(pix.getPixel(x2 + startX, y2 + startY)).a > 0.0f;
				}
			}
			
			tiledata.put(key, new RegionData(region, alpha));
		}
		
		pix.dispose();
	}
	
	private static class RegionData{
		TextureRegion region;
		boolean[][] alpha;
		
		RegionData(TextureRegion region, boolean[][] alpha){
			this.region = region;
			this.alpha = alpha;
		}
		
		@Override
		public int hashCode() {
			return region.getRegionX() * 31 + region.getRegionY();
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof RegionData){
				RegionData rg = (RegionData) obj;
				return rg.region == region;
			}else
				return false;
		}
	}
	
	private static class Holder{
		Cell cell;
		int x,y;
		
		Holder(Cell cell, int x, int y) {
			this.cell = cell;
			this.x = x;
			this.y = y;
		}

		@Override
		public boolean equals(Object obj) {
			Holder holder = (Holder)obj;
			return (x * 31 + y) == (holder.x * 31 + holder.y);
		}
	}
}
