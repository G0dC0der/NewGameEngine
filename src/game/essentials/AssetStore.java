package game.essentials;

import game.lang.IO;

import java.io.IOException;
import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;

public class AssetStore {

	private HashMap<String, Object> stuff;
	
	public AssetStore(){
		stuff = new HashMap<>();
	}
	
	public void addAsset(String key, Object obj){
		stuff.put(key, obj);
	}
	
	public void addObject(FileHandle path) throws ClassNotFoundException, IOException{
		stuff.put(stripString(path.toString()), IO.importObject(path.toString()));
	}
	
	public void loadImage(FileHandle path){
		loadImage(path, false);
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
	
	public void loadContentFromDirectory(FileHandle dir){
		
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
	
	private static String stripString(String str, boolean isDirectory){
		str = str.replace("\\", "/");
		int startIndex = str.lastIndexOf("/");
		if(startIndex > 0)
			startIndex++;
		
		int endIndex = isDirectory ? str.length() : str.lastIndexOf(".") - 1;
		
		return str.substring(startIndex, endIndex);
	}
}
