package pojahn.game.essentials.shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public abstract class Shader {

    private final String vertexShader;
    private final String fragmentShader;
    private ShaderProgram shader;

    protected Shader(final String vertexShader, final String fragmentShader) {
        this.vertexShader = vertexShader;
        this.fragmentShader = fragmentShader;
    }

    public void use(final SpriteBatch batch) {
        batch.setShader(get());
    }

    public void prepare() {
    }

    public void dispose() {
        get().dispose();
    }

    public ShaderProgram get() {
        if (shader != null)
            return shader;

        shader = new ShaderProgram(vertexShader, fragmentShader);

        if (!shader.isCompiled()) {
            throw new RuntimeException("Failed to compile the shader DrugShader:\n" + shader.getLog());
        }

        return shader;
    }

    public static String read(final String path) {
        return Gdx.files.internal(path).readString();
    }
}
