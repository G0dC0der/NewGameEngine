package game.lang;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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
}
