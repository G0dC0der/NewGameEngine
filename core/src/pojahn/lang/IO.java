package pojahn.lang;

import com.badlogic.gdx.files.FileHandle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class IO {

    public static void exportObject(final Object obj, final File dest) throws IOException {
        if (!dest.exists()) {
            dest.createNewFile();
        }

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(dest))) {
            out.writeObject(obj);
        }
    }

    public static Object importObject(final FileHandle src) throws ClassNotFoundException, IOException {
        try (ObjectInputStream in = new ObjectInputStream(src.read())) {
            return in.readObject();
        }
    }

    public static void exportObjectCompressed(final Object obj, final FileHandle dest) throws FileNotFoundException, IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(dest.file())))) {
            out.writeObject(obj);
        }
    }

    public static Object importObjectCompressed(final FileHandle src) throws ClassNotFoundException, IOException {
        try (ObjectInputStream in = new ObjectInputStream(new GZIPInputStream(src.read()))) {
            return in.readObject();
        }
    }
}	