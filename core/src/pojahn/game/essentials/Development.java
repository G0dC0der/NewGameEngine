package pojahn.game.essentials;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import pojahn.game.core.Entity;
import pojahn.game.core.PlayableEntity;
import pojahn.game.events.Event;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Development {

    public static Event adjustColor(final Color color) {
        return () -> {
            if (Gdx.input.isKeyPressed(Keys.NUM_5))
                color.r += .01f;
            else if (Gdx.input.isKeyPressed(Keys.T))
                color.r -= .01f;
            else if (Gdx.input.isKeyPressed(Keys.NUM_6))
                color.g += .01f;
            else if (Gdx.input.isKeyPressed(Keys.Y))
                color.g -= .01f;
        };
    }

    public static Event debugMovement(final PlayableEntity play) {
        return () -> {
            final Keystrokes strokes = play.getKeysDown();
            if (strokes.up || strokes.jump)
                System.out.println("Can go up: " + !play.occupiedAt(play.x(), play.y() - 1));
            else if (strokes.down)
                System.out.println("Can go down: " + !play.occupiedAt(play.x(), play.height() + play.y() + 1));

            if (strokes.left)
                System.out.println("Can go left: " + !play.occupiedAt(play.x() - 1, play.y()));
            else if (strokes.right)
                System.out.println("Can go right: " + !play.occupiedAt(play.width() + play.x() + 1, play.y()));
        };
    }

    public static Event debugPosition(final Entity entity) {
        return () -> {
            System.out.println(entity.x() + " " + entity.y());
        };
    }

    public static Event debugCollision(final Entity e1, final Entity e2) {
        return () -> {
            if (e1.collidesWith(e2))
                System.out.println(Math.random());
        };
    }

    public static void print2DArray(final Object[][] arr) {
        final StringBuilder builder = new StringBuilder(arr.length * arr[0].length);
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[i].length; j++) {
                builder.append(arr[i][j].toString());
            }
            builder.append("\n");
        }
        System.out.println(builder.toString());
    }

    public static Event steerEntity(final Entity entity, final float strength) {
        return ()-> {
            if (Gdx.input.isKeyPressed(Keys.A)) {
                entity.bounds.pos.x--;
            } else if (Gdx.input.isKeyPressed(Keys.D)) {
                entity.bounds.pos.x++;
            }
            if (Gdx.input.isKeyPressed(Keys.W)) {
                entity.bounds.pos.y--;
            } else if (Gdx.input.isKeyPressed(Keys.S)) {
                entity.bounds.pos.y++;
            }

            if (Gdx.input.isKeyPressed(Keys.R)) {
                entity.bounds.rotation += strength;
            } else if (Gdx.input.isKeyPressed(Keys.E)) {
                entity.bounds.rotation -= strength;
            }

            if (Gdx.input.isKeyJustPressed(Keys.P)) {
                System.out.println(String.format("Position: %s, %s\nRotation: %s", entity.x(), entity.y(), entity.getRotation()));
            }
        };
    }

    public static void splitImage(final int rows, final int cols, final File sourceImage) throws IOException {
        final FileInputStream fis = new FileInputStream(sourceImage);
        final BufferedImage image = ImageIO.read(fis);

        final int chunks = rows * cols;

        final int chunkWidth = image.getWidth() / cols;
        final int chunkHeight = image.getHeight() / rows;
        final BufferedImage[] imgs = new BufferedImage[chunks];
        for (int x = 0, count = 0; x < rows; x++) {
            for (int y = 0; y < cols; y++) {
                imgs[count] = new BufferedImage(chunkWidth, chunkHeight, image.getType());

                final Graphics2D gr = imgs[count++].createGraphics();
                gr.drawImage(image, 0, 0, chunkWidth, chunkHeight, chunkWidth * y, chunkHeight * x, chunkWidth * y + chunkWidth, chunkHeight * x + chunkHeight, null);
                gr.dispose();
            }
        }

        for (int i = 0; i < imgs.length; i++) {
            ImageIO.write(imgs[i], "png", new File("img " + i + ".png"));
        }
    }

    public static void main(String... args) throws IOException {
        splitImage(4, 4, new File("/Users/pojahnmoradi/Code/mygame/NewGameEngine/res/bcastle/roller.png"));
    }
}