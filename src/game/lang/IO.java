package game.lang;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class IO {

	public static void exportObject(Object obj, String path) throws IOException{
		try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(new File(path)))){
			out.writeObject(obj);
		}
	}
	
	public static Object importObject(String path) throws ClassNotFoundException, IOException{
		try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(new File(path)))){
			return in.readObject();
		}
	}
	
	public static void exportObjectCompressed(Object obj, String path) throws FileNotFoundException, IOException{
		try(ObjectOutputStream out = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(new File(path))))){
			out.writeObject(obj);
		}
	}
	
	public static Object importObjectCompressed(String path) throws ClassNotFoundException, IOException{
		try(ObjectInputStream in = new ObjectInputStream(new GZIPInputStream(new FileInputStream(new File(path))))){
			return in.readObject();
		}
	}
}