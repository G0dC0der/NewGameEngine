package game.essentials;

import java.util.HashMap;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

public class AssetStore {

	private HashMap<String, Image2D> images;
	private HashMap<String, Image2D[]> animations;
	private HashMap<String, Sound> sounds;
	private HashMap<String, Music> music;
	private HashMap<String, Object> other;
	
	public AssetStore(){
		images = new HashMap<>();
		animations = new HashMap<>();
		sounds = new HashMap<>();
		music = new HashMap<>();
		other = new HashMap<>();
	}
	
	public void loadImage(FileHandle path){
		images.put(stripString(path.toString()), new Image2D(path, false));
	}
	
	public void loadImage(FileHandle path, boolean createPixelData){
		images.put(stripString(path.toString()), new Image2D(path, createPixelData));
	}
	
	public Image2D get(String key){
		return images.get(key);
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
