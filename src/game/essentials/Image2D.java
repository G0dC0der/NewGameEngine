package game.essentials;

import java.io.IOException;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;

public class Image2D extends Texture{
	
	private int[][] pixelData;
	
	public Image2D(FileHandle file) {
		this(file, false);
	}
	
	public Image2D(FileHandle file, boolean createPixelData) {
		super(file);
	}
	
	public int getPixel(int x, int y){
		return pixelData[x][y];
	}
	
	public boolean hasPixelData(){
		return pixelData != null;
	}
	
	public static Image2D[] loadAnimation(FileHandle directory) throws IOException{
		return loadAnimation(directory, false);
	}

	public static Image2D[] loadAnimation(FileHandle directory, boolean createPixelData) throws IOException{
		if(!directory.isDirectory())
			throw new IllegalArgumentException("The argument must be a directory.");
		
		FileHandle[] pngFiles = directory.list((fileName)-> fileName.toString().endsWith(".png"));
		
		Image2D[] images = new Image2D[pngFiles.length];
		for(int i = 0; i < images.length; i++)
			images[i] = new Image2D(pngFiles[i], createPixelData);
		
		return images;
	}
}
