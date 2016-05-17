package pojahn.game.essentials;

import java.io.IOException;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FileTextureData;

public class Image2D extends Texture {

    private int[][] pixelData;

    public Image2D(FileHandle file) {
        this(file, false);
    }

    public Image2D(FileHandle file, boolean createPixelData) {
        super(file);
        if (createPixelData)
            createPixelData();
    }

    public Image2D(Pixmap map) {
        this(map, false);
    }

    public Image2D(Pixmap map, boolean createPixelData) {
        super(map);
        if (createPixelData)
            createPixelData(map);
    }

    public int getPixel(int x, int y) {
        return pixelData[x][y];
    }

    public boolean hasPixelData() {
        return pixelData != null;
    }

    public void setPixelData(Image2D source) {
        pixelData = source.pixelData;
    }

    public void clearData() {
        pixelData = null;
    }

    public boolean isInvisible(int x, int y) {
        return (pixelData[x][y] & 0x000000FF) == 0;
    }

    public void createPixelData() {
        FileTextureData td = (FileTextureData) getTextureData();
        td.prepare();

        Pixmap map = td.consumePixmap();
        createPixelData(map);
        map.dispose();
    }

    public void createPixelData(Pixmap img) {
        pixelData = new int[img.getWidth()][img.getHeight()];

        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                pixelData[x][y] = img.getPixel(x, y);
            }
        }
    }

    public static Image2D[] loadAnimation(FileHandle directory) throws IOException {
        return loadAnimation(directory, false);
    }

    public static Image2D[] loadAnimation(FileHandle directory, boolean createPixelData) throws IOException {
        if (!directory.isDirectory())
            throw new IllegalArgumentException("The argument must be a directory.");

        FileHandle[] pngFiles = directory.list((fileName) -> fileName.toString().endsWith(".png"));

        Image2D[] images = new Image2D[pngFiles.length];
        for (int i = 0; i < images.length; i++)
            images[i] = new Image2D(pngFiles[i], createPixelData);

        return images;
    }

    public static void mergePixelData(Image2D... images) {
        int[][] first = images[0].pixelData;
        for (int i = 1; i < images.length; i++)
            images[i].pixelData = first;
    }
}
