package game.essentials;

import java.io.IOException;
import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Disposable;

import game.lang.IO;

public class ResourceBag {

	private HashMap<String, Object> stuff;
	
	public ResourceBag(){
		stuff = new HashMap<>();
	}
	
	public void addAsset(String key, Object obj){
		stuff.put(key, obj);
	}
	
	public void loadObject(FileHandle path) throws ClassNotFoundException, IOException{
		stuff.put(stripString(path.toString()), IO.importObject(path.toString()));
	}
	
	public void loadImage(FileHandle path){
		loadImage(path, false);
	}
	
	public void loadPixmap(FileHandle path){
		stuff.put(stripString(path.toString()), new Pixmap(path));
	}
	
	public void loadImage(FileHandle path, boolean createPixelData){
		stuff.put(stripString(path.toString()), new Image2D(path, createPixelData));
	}
	
	public void loadAnimation(FileHandle path, boolean createPixelData) throws IOException{
		stuff.put(stripString(path.toString(), true), Image2D.loadAnimation(path, createPixelData));
	}
	
	public void loadAnimation(FileHandle path) throws IOException{
		loadAnimation(path, false);
	}
	
	public Image2D getImage(String key){
		return (Image2D) stuff.get(key);
	}
	
	public void loadSound(FileHandle path){
		stuff.put(stripString(path.toString()), Gdx.audio.newSound(path));
	}
	
	public void loadMusic(FileHandle path){
		stuff.put(stripString(path.toString()), Gdx.audio.newMusic(path));
	}
	
	/**
	 * Loads all the content from the given directory. For this to work, the content must follow a set of rules:
	 * - sound files whose name contains "music" are loaded as a Music object instead of Sound
	 * - png files whose name contains "pix" are loaded as Pixmap instead of Image2D
	 * - other files whose name contains "non-obj" are ignored. The rest will be deserialized.
	 * - subdirectories consist of images only. These are loaded as Image2D.
	 */
	public void loadContentFromDirectory(FileHandle dir) throws IOException{
		if(!dir.isDirectory())
			throw new IllegalArgumentException("Argument must be a directory!");
		
		for(FileHandle content : dir.list()){
			String name = content.path();
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
			} else if(content.isDirectory()){
				loadAnimation(content);
			} else if(!name.contains("non-obj")){
				try{
					loadObject(content);
				}catch(ClassNotFoundException e){
					System.err.println("The following file could not be imported: " + content.toString());
				}
			}
		}
	}
	
	public void disposeAll(){
		stuff.forEach((key, value)->{
			if(value instanceof Disposable)
				((Disposable)value).dispose();
		});
	}
	
	public void dispose(String key){
		((Disposable)stuff.get(key)).dispose();
	}
	
	private static String stripString(String str){
		return stripString(str, false);
	}
	
	@Override
	public String toString(){
		StringBuilder bu = new StringBuilder();
		stuff.forEach((key, value) -> bu.append(key).append(": ").append(value.getClass().getSimpleName()).append(System.lineSeparator()));
		return bu.toString();
	}
	
	private static String stripString(String str, boolean isDirectory){
		str = str.replace("\\", "/");
		int startIndex = str.lastIndexOf("/");
		if(startIndex > 0)
			startIndex++;
		
		int endIndex = isDirectory ? str.length() : str.lastIndexOf(".") - 1;
		
		return str.substring(startIndex, endIndex);
	}
}
