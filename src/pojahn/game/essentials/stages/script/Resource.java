package pojahn.game.essentials.stages.script;

import com.badlogic.gdx.Gdx;
import pojahn.game.essentials.Image2D;

public class Resource {

    public static Image2D loadImage(String path) {
        return new Image2D(Gdx.files.internal(path));
    }
}
