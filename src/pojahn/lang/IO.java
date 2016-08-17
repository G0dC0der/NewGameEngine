package pojahn.lang;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.badlogic.gdx.files.FileHandle;

public class IO {

    public static void exportObject(Object obj, FileHandle dest) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(dest.file()))) {
            out.writeObject(obj);
        }
    }

    public static Object importObject(FileHandle src) throws ClassNotFoundException, IOException {
        try (ObjectInputStream in = new ObjectInputStream(src.read())) {
            return in.readObject();
        }
    }

    public static void exportObjectCompressed(Object obj, FileHandle dest) throws FileNotFoundException, IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(dest.file())))) {
            out.writeObject(obj);
        }
    }

    public static Object importObjectCompressed(FileHandle src) throws ClassNotFoundException, IOException {
        try (ObjectInputStream in = new ObjectInputStream(new GZIPInputStream(src.read()))) {
            return in.readObject();
        }
    }
}	