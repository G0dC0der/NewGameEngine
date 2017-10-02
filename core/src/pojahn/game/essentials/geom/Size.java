package pojahn.game.essentials.geom;

import pojahn.game.essentials.Image2D;

public class Size {

    public float width, height;

    public Size() {
    }

    public Size(final float width, final float height) {
        this.width = width;
        this.height = height;
    }

    public void set(final float width, final float height) {
        this.width = width;
        this.height = height;
    }

    public Size copy() {
        return new Size(width, height);
    }

    public static Size from(final Image2D image2D) {
        return new Size(image2D.getWidth(), image2D.getHeight());
    }
}
