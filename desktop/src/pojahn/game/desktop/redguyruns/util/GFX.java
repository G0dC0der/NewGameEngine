package pojahn.game.desktop.redguyruns.util;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import pojahn.game.core.Entity;
import pojahn.game.core.Level;
import pojahn.game.essentials.ResourceManager;

import java.awt.*;

public class GFX {

    public static void renderCheckpoint(ResourceManager resource, Level level) {
        level.temp(new Entity(){
            boolean once;
            Sound takeSound;
            {
                zIndex(Integer.MAX_VALUE);
                setImage(resource.getImage("checkpoint.png"));
                takeSound = resource.getSound("collect2.wav");
            }

            @Override
            public void render(SpriteBatch batch) {
                getEngine().hudCamera();
                Dimension screenSize = getEngine().getScreenSize();

                batch.draw(nextImage(),
                        screenSize.width / 2 - halfWidth(),
                        screenSize.height / 2 - halfHeight(),
                        0,
                        0,
                        bounds.size.width,
                        bounds.size.height,
                        1,
                        1,
                        0,
                        0,
                        0,
                        (int) bounds.size.width,
                        (int) bounds.size.height,
                        flipX,
                        !flipY);

                getEngine().gameCamera();

                if(!once) {
                    once = true;
                    takeSound.play();
                }
            }
        }, 120);
    }
}
