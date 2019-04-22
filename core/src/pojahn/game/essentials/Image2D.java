package pojahn.game.essentials;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FileTextureData;

import java.io.IOException;

public class Image2D extends Texture {

    private int[][] pixelData;

    public Image2D(final FileHandle file) {
        this(file, false);
    }

    public Image2D(final FileHandle file, final boolean createPixelData) {
        super(file);
        if (createPixelData)
            createPixelData();
    }

    public Image2D(final Pixmap map) {
        this(map, false);
    }

    public Image2D(final Pixmap map, final boolean createPixelData) {
        super(map);
        if (createPixelData)
            createPixelData(map);
    }

    public int getPixel(final int x, final int y) {
        return pixelData[x][y];
    }

    public void clearData() {
        pixelData = null;
    }

    public boolean isInvisible(final int x, final int y) {
        return (pixelData[x][y] & 0x000000FF) == 0;
    }

    public void createPixelData() {
        final FileTextureData td = (FileTextureData) getTextureData();
        td.prepare();

        final Pixmap map = td.consumePixmap();
        createPixelData(map);
        map.dispose();
    }

    public void createPixelData(final Pixmap img) {
        pixelData = new int[img.getWidth()][img.getHeight()];

        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                pixelData[x][y] = img.getPixel(x, y);
            }
        }
    }

    public static void createOnePixelData(final Image2D[] arr) {
        arr[0].clearData();
        for (int i = 1; i < arr.length; i++)
            arr[i].pixelData = arr[0].pixelData;
    }

    public static Image2D[] loadAnimation(final FileHandle directory) throws IOException {
        return loadAnimation(directory, false);
    }

    public static Image2D[] loadAnimation(final FileHandle directory, final boolean createPixelData) throws IOException {
        if (!directory.isDirectory())
            throw new IllegalArgumentException("The argument must be a directory.");

        final FileHandle[] pngFiles = directory.list((fileName) -> fileName.toString().toLowerCase().endsWith(".png"));

        final Image2D[] images = new Image2D[pngFiles.length];
        for (int i = 0; i < images.length; i++)
            images[i] = new Image2D(pngFiles[i], createPixelData);

        return images;
    }

    public static void mergePixelData(final Image2D... images) {
        final int[][] first = images[0].pixelData;
        for (int i = 1; i < images.length; i++)
            images[i].pixelData = first;
    }

    public static Animation<Image2D> animation(final Image2D... images) {
        return new Animation<Image2D>(images);
    }

    public static Animation<Image2D> animation(final int speed, final Image2D... images) {
        return new Animation<Image2D>(speed, images);
    }
}
