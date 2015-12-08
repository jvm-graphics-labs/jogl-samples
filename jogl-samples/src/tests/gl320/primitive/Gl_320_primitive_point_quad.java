/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl320.primitive;

import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import com.jogamp.opengl.GL3;
import static com.jogamp.opengl.GL3ES3.GL_GEOMETRY_SHADER;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.Semantic;
import framework.Test;

/**
 *
 * @author GBarbieri
 */
public class Gl_320_primitive_point_quad extends Test {

    public static void main(String[] args) {
        Gl_320_primitive_point_quad gl_320_primitive_point_quad = new Gl_320_primitive_point_quad();
    }

    public Gl_320_primitive_point_quad() {
        super("gl-320-primitive-point-quad", 3, 2);
    }

    private final String SHADERS_SOURCE = "primitive-point-quad";
    private final String SHADERS_ROOT = "src/data/gl_320/primitive";

    private int vertexCount, programName, uniformMvp, uniformMv, uniformCameraPosition;
    private int[] vertexArrayName = new int[1], bufferName = new int[1];
    
    private boolean initProgram(GL3 gl3) {

        boolean validated = true;

        if (validated) {

            ShaderCode vertShaderCode = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            ShaderCode geomShaderCode = ShaderCode.create(gl3, GL_GEOMETRY_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "geom", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

            ShaderProgram shaderProgram = new ShaderProgram();
            shaderProgram.add(vertShaderCode);
            shaderProgram.add(geomShaderCode);
            shaderProgram.add(fragShaderCode);

            shaderProgram.init(gl3);

            programName = shaderProgram.program();

            gl3.glBindAttribLocation(programName, Semantic.Attr.POSITION, "Position");
            gl3.glBindAttribLocation(programName, Semantic.Attr.COLOR, "Color");
            gl3.glBindFragDataLocation(programName, Semantic.Frag.COLOR, "Color");

            shaderProgram.link(gl3, System.out);
        }
        if (validated) {

            uniformMvp = gl3.glGetUniformLocation(programName, "MVP");
            uniformMv = gl3.glGetUniformLocation(programName, "MV");
            uniformCameraPosition = gl3.glGetUniformLocation(programName, "CameraPosition");
        }

        return validated & checkError(gl3, "initProgram");
    }
}
