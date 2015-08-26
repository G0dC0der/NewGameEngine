package pojahn.lang;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.badlogic.gdx.files.FileHandle;

public class IO {

	public static void exportObject(Object obj, FileHandle dest) throws IOException{
		try(ObjectOutputStream out = new ObjectOutputStream(dest.write(true))){
			out.writeObject(obj);
		}
	}
	
	public static Object importObject(FileHandle src) throws ClassNotFoundException, IOException{
		try(ObjectInputStream in = new ObjectInputStream(src.read())){
			return in.readObject();
		}
	}
	
	public static void exportObjectCompressed(Object obj, FileHandle dest) throws FileNotFoundException, IOException{
		try(ObjectOutputStream out = new ObjectOutputStream(new GZIPOutputStream(dest.write(true)))){
			out.writeObject(obj);
		}
	}
	
	public static Object importObjectCompressed(FileHandle src) throws ClassNotFoundException, IOException{
		try(ObjectInputStream in = new ObjectInputStream(new GZIPInputStream(src.read()))){
			return in.readObject();
		}
	}
}	