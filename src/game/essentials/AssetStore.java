package game.essentials;

import java.util.HashMap;

import com.badlogic.gdx.files.FileHandle;

public class AssetStore {

	private HashMap<String, Object> stuff;
	
	public AssetStore(){
		stuff = new HashMap<>();
	}
	
	public void loadImage(FileHandle path){
		stuff.put(stripString(path.toString()), new Image2D(path, false));
	}
	
	public void loadImage(FileHandle path, boolean createPixelData){
		stuff.put(stripString(path.toString()), new Image2D(path, createPixelData));
	}
	
	public Image2D getImage(String key){
		return (Image2D) stuff.get(key);
	}
	
	public void loadContentFromDirectory(FileHandle dir){
		
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
