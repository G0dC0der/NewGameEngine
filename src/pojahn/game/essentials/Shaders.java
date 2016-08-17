package pojahn.game.essentials;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import java.text.ParseException;

public class Shaders {

    public static class DrugShader {
        public static final String VERT;
        public static final String FRAG;

        static {
            VERT = load("res/shaders/drugshader.vert");
            FRAG = load("res/shaders/drugshader.frag");
        }

        public static ShaderProgram get() {
            try {
                ShaderProgram shader = new ShaderProgram(VERT, FRAG);
                if(!shader.isCompiled())
                    throw new RuntimeException(("Failed to compile the shader: " + shader.getLog()));
                return shader;
            } catch (Exception e) {

            }
        }
    }

    private static String load(String file) {
        return Gdx.files.internal(file).readString();
    }
}
