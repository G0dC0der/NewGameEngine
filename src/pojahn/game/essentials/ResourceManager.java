package pojahn.game.essentials;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.resolvers.AbsoluteFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.TmxMapLoader.Parameters;
import com.badlogic.gdx.utils.Disposable;

public class ResourceManager {

	private HashMap<String, Object> stuff;
	
	public ResourceManager(){
		stuff = new HashMap<>();
	}
	
	public void addAsset(String key, Object obj){
		stuff.put(key, obj);
	}
	
	public void loadFont(FileHandle path){
		stuff.put(path.name(), new BitmapFont(path, true));
	}
	
	public BitmapFont getFont(String key){
		return (BitmapFont) stuff.get(key);
	}
	
	public void loadTiledMap(FileHandle path){
		stuff.put(path.name(), getTiledMap(path));
	}
	
	public TiledMap getTiledMap(String key){
		return (TiledMap) stuff.get(key);
	}
	
	public void loadObject(FileHandle path) throws ClassNotFoundException, IOException{
		stuff.put(path.name(), path);
	}
	
	public Object getAsset(String key){
		return stuff.get(key);
	}
	
	public void loadImage(FileHandle path){
		loadImage(path, false);
	}
	
	public void loadPixmap(FileHandle path){
		stuff.put(path.name(), new Pixmap(path));
	}
	
	public void loadImage(FileHandle path, boolean createPixelData){
		stuff.put(path.name(), new Image2D(path, createPixelData));
	}
	
	public void loadAnimation(FileHandle path, boolean createPixelData) throws IOException{
		stuff.put(path.name(), Image2D.loadAnimation(path, createPixelData));
	}
	
	public void loadAnimation(FileHandle path) throws IOException{
		loadAnimation(path, false);
	}
	
	public Image2D getImage(String key){
		return (Image2D) stuff.get(key);
	}
	
	public Image2D[] getAnimation(String key){
		return (Image2D[]) stuff.get(key);
	}
	
	public Pixmap getPixmap(String key){
		return (Pixmap) stuff.get(key);
	}
	
	public Sound getSound(String key){
		return (Sound) stuff.get(key);
	}
	
	public Music getMusic(String key){
		return (Music) stuff.get(key);
	}
	
	public void loadSound(FileHandle path){
		stuff.put(path.name(), Gdx.audio.newSound(path));
	}
	
	public void loadMusic(FileHandle path){
		stuff.put(path.name(), Gdx.audio.newMusic(path));
	}
	
	/**
	 * Loads all the content from the given directory. For this to work, the content must follow a set of rules:
	 * - File name does not contains "skip"
	 * - sound files whose name contains "music" are loaded as a Music object instead of Sound
	 * - png files whose name contains "pix" are loaded as Pixmap instead of Image2D
	 * - files with .fnt extension are loaded as BitmapFont.
	 * - files with .tmx extension are loaded as TiledMap
	 * - other files whose name contains "non-obj" are ignored. The rest will be deserialized.
	 * - subdirectories consist of images only. These are loaded as Image2D.
	 */
	public void loadContentFromDirectory(FileHandle dir) throws IOException{
		if(!dir.exists())
			throw new NullPointerException("The given directory doesn't exist: " + dir.file().getAbsolutePath());
		if(!dir.isDirectory())
			throw new IllegalArgumentException("Argument must be a directory:" + dir.file().getAbsolutePath());
		
		for(FileHandle content : dir.list()){
			String name = content.path().toLowerCase();
			if(name.contains("skip"))
				continue;
			
			if(name.endsWith(".png")){
				if(name.contains("pix"))
					loadPixmap(content);
				else
					loadImage(content);
			} else if(name.endsWith(".wav") || name.endsWith(".ogg") || name.endsWith(".mp3")){
				if(name.contains("music"))
					loadMusic(content);
				else
					loadSound(content);
			} else if(name.endsWith(".tmx")){
				loadTiledMap(content);
			} else if(content.isDirectory()){
				loadAnimation(content);
			} else if(name.endsWith(".fnt")){
				loadFont(content);
			} else if(!name.contains("non-obj")){
				try{
					loadObject(content);
				}catch(ClassNotFoundException | IOException e){
					System.err.println("The following file could not be imported: " + content.toString());
				}
			}
		}
	}
	
	public void disposeAll(){
		stuff.forEach((key, value)->{
			tryDispose(value);
		});
	}
	
	public void dispose(String key){
		tryDispose(stuff.get(key));
	}
	
	@Override
	public String toString(){
		StringBuilder bu = new StringBuilder();
		stuff.forEach((key, value) -> bu.append(key).append(": ").append(value.getClass().getSimpleName()).append(System.lineSeparator()));
		return bu.toString();
	}
	
	public static TiledMap getTiledMap(FileHandle path){
		String str = path.file().getAbsolutePath();
		Parameters params = new Parameters();
		params.flipY = false;
		TiledMap map = new TmxMapLoader(new AbsoluteFileHandleResolver()).load(str, params);
		MapProperties props = map.getProperties();
		
		int tilesX = props.get("width", Integer.class);
		int tilesY = props.get("height", Integer.class);
		
		MapLayers layers = map.getLayers();
		layers.forEach(l->{
			TiledMapTileLayer layer = (TiledMapTileLayer) l;
			Set<TextureRegion> used = new HashSet<>();

			for(int x = 0; x < tilesX; x++){
				for(int y = 0; y < tilesY; y++){
					Cell cell = layer.getCell(x, y);
					if(cell != null){
						TextureRegion region = cell.getTile().getTextureRegion();
						if(used.add(region)){
							region.flip(false, true);
						}
					}
				}
			}
			
		});
		
		return map;
	}
	
	private void tryDispose(Object obj){
		if(obj instanceof Disposable)
			((Disposable)obj).dispose();
		else if(obj.getClass().isArray()){
			Object[] arr = (Object[]) obj;
			
			if(arr.length > 0 && arr[0] instanceof Disposable){
				for(Object disposable : arr)
					((Disposable)disposable).dispose();
			}
		}
	}
}
