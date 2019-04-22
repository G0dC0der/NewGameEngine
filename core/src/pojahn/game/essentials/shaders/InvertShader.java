package pojahn.game.essentials.shaders;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class InvertShader extends Shader {

    private static final String VERT = "#version 150 core\n" +
            "\n" +
            "attribute vec4 a_color;\n" +
            "attribute vec3 a_position;\n" +
            "attribute vec2 a_texCoord0;\n" +
            "\n" +
            "uniform mat4 u_projTrans;\n" +
            "uniform float invertValue;\n" +
            "\n" +
            "varying vec4 v_color;\n" +
            "varying vec2 v_texCoord0;\n" +
            "\n" +
            "void main() {\n" +
            "\tv_color = a_color;\n" +
            "\tv_texCoord0 = a_texCoord0;\n" +
            "\tgl_Position =  u_projTrans * vec4(a_position, 1.);\n" +
            "}\n";

    private static final String FRAG = "#version 150 core\n" +
            "\n" +
            "varying vec4 v_color;\n" +
            "varying vec2 v_texCoord0;\n" +
            "\n" +
            "uniform sampler2D u_sampler2D;\n" +
            "uniform float invertValue;\n" +
            "\n" +
            "void main() {\n" +
            "\tvec4 color = texture2D(u_sampler2D, v_texCoord0) * v_color;\n" +
            "\tcolor.rgb = invertValue - color.rgb;\n" +
            "\tgl_FragColor = color;\n" +
            "}";

    private float invertValue = 1.0f;

    public InvertShader() {
        super(VERT, FRAG);
    }

    public float getInvertValue() {
        return invertValue;
    }

    public void setInvertValue(final float invertValue) {
        this.invertValue = invertValue;
    }

    @Override
    public void prepare() {
        final ShaderProgram shader = get();
        shader.begin();
        shader.setUniformf("invertValue", invertValue);
        shader.end();
    }
}
