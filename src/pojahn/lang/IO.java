package pojahn.lang;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.badlogic.gdx.files.FileHandle;

public class IO {

	public static void exportObject(Object obj, FileHandle dest) throws IOException{
		try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(dest.file()))){
			out.writeObject(obj);
		}
	}
	
	public static Object importObject(FileHandle src) throws ClassNotFoundException, IOException{
		try(ObjectInputStream in = new ObjectInputStream(src.read())){
			return in.readObject();
		}
	}
	
	public static void exportObjectCompressed(Object obj, FileHandle dest) throws FileNotFoundException, IOException{
		try(ObjectOutputStream out = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(dest.file())))){
			out.writeObject(obj);
		}
	}
	
	public static Object importObjectCompressed(FileHandle src) throws ClassNotFoundException, IOException{
		try(ObjectInputStream in = new ObjectInputStream(new GZIPInputStream(src.read()))){
			return in.readObject();
		}
	}
	
	public static void writeText(String text, FileHandle dest) throws IOException{
		try(BufferedWriter out = new BufferedWriter(new FileWriter(dest.file()))){
			out.write(text);
		}
	}
	
	public static String readText(FileHandle dest) throws IOException{
		StringBuilder bu = new StringBuilder((int)dest.length());

		try(BufferedReader in = new BufferedReader(new FileReader(dest.file()))){
			String line;
			while((line = in.readLine()) != null)
				bu.append(line);
		}
		
		return bu.toString();
	}

	public static void close(Closeable closeable) {
		try {
			closeable.close();
		} catch (Exception e) {

		}
	}
}	