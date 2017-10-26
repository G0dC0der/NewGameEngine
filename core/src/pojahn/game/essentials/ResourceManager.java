package pojahn.game.essentials;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.utils.Disposable;
import pojahn.lang.IO;

import java.io.IOException;
import java.util.HashMap;

public class ResourceManager {

    private HashMap<String, Object> stuff;

    public ResourceManager() {
        stuff = new HashMap<>();
    }

    public void addAsset(final String key, final Object obj) {
        stuff.put(key, obj);
    }

    public void loadFont(final FileHandle path) {
        stuff.put(path.name(), new BitmapFont(path, true));
    }

    public BitmapFont getFont(final String key) {
        return (BitmapFont) stuff.get(key);
    }

    public void loadTiledMap(final FileHandle path) {
        stuff.put(path.name(), Utils.loadTiledMap(path));
    }

    public TiledMap getTiledMap(final String key) {
        return (TiledMap) stuff.get(key);
    }

    public void loadObject(final FileHandle path) throws ClassNotFoundException, IOException {
        stuff.put(path.name(), IO.importObject(path));
    }

    public Object getAsset(final String key) {
        return stuff.get(key);
    }

    public void loadImage(final FileHandle path) {
        loadImage(path, false);
    }

    public void loadPixmap(final FileHandle path) {
        stuff.put(path.name(), new Pixmap(path));
    }

    public void loadImage(final FileHandle path, final boolean createPixelData) {
        stuff.put(path.name(), new Image2D(path, createPixelData));
    }

    public void loadAnimation(final FileHandle path, final boolean createPixelData) throws IOException {
        stuff.put(path.name(), Image2D.loadAnimation(path, createPixelData));
    }

    public void loadAnimation(final FileHandle path) throws IOException {
        loadAnimation(path, false);
    }

    public Image2D getImage(final String key) {
        return (Image2D) stuff.get(key);
    }

    public Image2D[] getAnimation(final String key) {
        return (Image2D[]) stuff.get(key);
    }

    public Pixmap getPixmap(final String key) {
        return (Pixmap) stuff.get(key);
    }

    public Sound getSound(final String key) {
        return (Sound) stuff.get(key);
    }

    public Music getMusic(final String key) {
        return (Music) stuff.get(key);
    }

    public void loadSound(final FileHandle path) {
        stuff.put(path.name(), Gdx.audio.newSound(path));
    }

    public void loadMusic(final FileHandle path) {
        stuff.put(path.name(), Gdx.audio.newMusic(path));
    }

    /**
     * Loads all the content from the given directory. For this to work, the content must follow a set of rules:
     * - File name does not contains "skip"
     * - sound files whose name contains "music" are loaded as a Music object instead of Sound
     * - png files whose name contains "pix" are loaded as Pixmap instead of Image2D
     * - files with .fnt extension are loaded as BitmapFont.
     * - files with .tmx extension are loaded as TiledMap
     * - other files whose name contains "non-obj" are ignored. The rest will be deserialized.
     * - subdirectories consist of images only. These are loaded as Image2D.
     */
    public void loadContentFromDirectory(final FileHandle dir) throws IOException {
        if (!dir.exists())
            throw new NullPointerException("The given directory doesn't exist: " + dir.file().getAbsolutePath());
        if (!dir.isDirectory())
            throw new IllegalArgumentException("Argument must be a directory:" + dir.file().getAbsolutePath());

        for (final FileHandle content : dir.list()) {
            final String name = content.path().toLowerCase();
            if (name.contains("skip"))
                continue;

            if (name.endsWith(".png")) {
                if (name.contains("pix"))
                    loadPixmap(content);
                else
                    loadImage(content);
            } else if (name.endsWith(".wav") || name.endsWith(".ogg") || name.endsWith(".mp3")) {
                if (name.contains("music"))
                    loadMusic(content);
                else
                    loadSound(content);
            } else if (name.endsWith(".tmx")) {
                loadTiledMap(content);
            } else if (content.isDirectory()) {
                loadAnimation(content);
            } else if (name.endsWith(".fnt")) {
                loadFont(content);
            } else if (!name.contains("non-obj")) {
                try {
                    loadObject(content);
                } catch (ClassNotFoundException | IOException e) {
                    System.err.println("The following file could not be imported: " + content.toString());
                }
            }
        }
    }

    public void disposeAll() {
        stuff.forEach((key, value) -> tryDispose(value));
    }

    public void dispose(final String key) {
        tryDispose(stuff.get(key));
    }

    public void remove(final String key) {
        stuff.remove(key);
    }

    @Override
    public String toString() {
        final StringBuilder bu = new StringBuilder();
        stuff.forEach((key, value) -> bu.append(key).append(": ").append(value.getClass().getSimpleName()).append(System.lineSeparator()));
        return bu.toString();
    }

    private void tryDispose(final Object obj) {
        try {
            if (obj instanceof Disposable)
                ((Disposable) obj).dispose();
            else if (obj.getClass().isArray()) {
                final Object[] arr = (Object[]) obj;

                if (arr.length > 0 && arr[0] instanceof Disposable) {
                    for (final Object disposable : arr)
                        ((Disposable) disposable).dispose();
                }
            }
        } catch (final Exception e) {
            System.err.println("Failed to dispose resource: " + e.getMessage());
        }
    }
}
