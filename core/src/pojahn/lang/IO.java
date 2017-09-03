package pojahn.lang;

import com.badlogic.gdx.files.FileHandle;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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