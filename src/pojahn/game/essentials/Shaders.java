package pojahn.game.essentials;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class Shaders {

    public static class DefaultShader {
        static final String VERT = "#version 330 core\n"
                + "in vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
                + "in vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
                + "in vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
                + "uniform mat4 u_projTrans;\n" //
                + "out vec4 v_color;\n" //
                + "out vec2 v_texCoords;\n" //
                + "\n" //
                + "void main()\n" //
                + "{\n" //
                + "   v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
                + "   v_color.a = v_color.a * (255.0/254.0);\n" //
                + "   v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
                + "   gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
                + "}\n";

        static final String FRAG = "#version 330 core\n"
                + "#ifdef GL_ES\n" //
                + "#define LOWP lowp\n" //
                + "precision mediump float;\n" //
                + "#else\n" //
                + "#define LOWP \n" //
                + "#endif\n" //
                + "in LOWP vec4 v_color;\n" //
                + "in vec2 v_texCoords;\n" //
                + "out vec4 fragColor;\n" //
                + "uniform sampler2D u_texture;\n" //
                + "void main()\n"//
                + "{\n" //
                + "  fragColor = v_color * texture(u_texture, v_texCoords);\n" //
                + "}";

        public static ShaderProgram get() {
            return Shaders.get(VERT, FRAG);
        }
    }

    public static class DrugShader {
        static final String VERT;
        static final String FRAG;

        static {
            VERT = load("res/shaders/drugshader.vert");
            FRAG = load("res/shaders/drugshader.frag");
        }

        public static ShaderProgram get() {
            return Shaders.get(VERT, FRAG);
        }
    }

    private static String load(String file) {
        return Gdx.files.internal(file).readString();
    }

    private static ShaderProgram get(final String vert, final String frag) {
        try {
            ShaderProgram shader = new ShaderProgram(vert, frag);
            if(!shader.isCompiled()) {
                System.err.println(shader.getLog());
                throw new RuntimeException("Failed to compile the shader DrugShader");
            }
            return shader;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
