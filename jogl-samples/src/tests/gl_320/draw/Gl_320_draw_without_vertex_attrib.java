/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_320.draw;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2ES3.*;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.nio.ByteBuffer;

/**
 *
 * @author GBarbieri
 */
public class Gl_320_draw_without_vertex_attrib extends Test {

    public static void main(String[] args) {
        Gl_320_draw_without_vertex_attrib gl_320_draw_without_vertex_attrib = new Gl_320_draw_without_vertex_attrib();
    }

    public Gl_320_draw_without_vertex_attrib() {
        super("gl-320-draw-without-vertex-attrib", Profile.CORE, 3, 2);
    }

    private final String SHADERS_SOURCE = "draw-without-vertex-attrib";
    private final String SHADERS_ROOT = "src/data/gl_320/draw";

    private int programName, uniformTransform;
    private int[] bufferName = new int[1], vertexArrayName = new int[1];
    private float[] projection = new float[16], model = new float[16];

    @Override
    protected boolean begin(GL gl) {

        GL3 gl3 = (GL3) gl;

        boolean validated = true;

        if (validated) {
            validated = initBuffer(gl3);
        }
        if (validated) {
            validated = initProgram(gl3);
        }
        if (validated) {
            validated = initVertexArray(gl3);
        }

        return validated && checkError(gl3, "begin");
    }

    private boolean initProgram(GL3 gl3) {

        boolean validated = true;

        // Create program
        if (validated) {

            ShaderCode vertShader = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragShader = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

            ShaderProgram program = new ShaderProgram();
            program.add(vertShader);
            program.add(fragShader);

            program.init(gl3);

            programName = program.program();

            gl3.glBindFragDataLocation(programName, Semantic.Frag.COLOR, "color");

            program.link(gl3, System.out);
        }
        // Get variables locations
        if (validated) {

            uniformTransform = gl3.glGetUniformBlockIndex(programName, "Transform");
            gl3.glUniformBlockBinding(programName, uniformTransform, Semantic.Uniform.TRANSFORM0);
        }

        return validated & checkError(gl3, "initProgram");
    }

    private boolean initBuffer(GL3 gl3) {

        gl3.glGenBuffers(1, bufferName, 0);

        int[] uniformBufferOffset = new int[1];

        gl3.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset, 0);

        int uniformBlockSize = Math.max(16 * Float.BYTES, uniformBufferOffset[0]);

        gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[0]);
        gl3.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_DYNAMIC_DRAW);
        gl3.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return checkError(gl3, "initBuffer");
    }

    private Boolean initVertexArray(GL3 gl3) {

        gl3.glGenVertexArrays(1, vertexArrayName, 0);
        gl3.glBindVertexArray(vertexArrayName[0]);
        gl3.glBindVertexArray(0);

        return checkError(gl3, "initVertexArray");
    }

    @Override
    protected boolean render(GL gl) {

        GL3 gl3 = (GL3) gl;

        {
            gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[0]);
            ByteBuffer pointer = gl3.glMapBufferRange(
                    GL_UNIFORM_BUFFER, 0, 16 * Float.BYTES,
                    GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            FloatUtil.makePerspective(projection, 0, true, (float) Math.PI * 0.25f,
                    (float) windowSize.x / windowSize.y, 0.1f, 100.0f);
            FloatUtil.makeIdentity(model);

            FloatUtil.multMatrix(projection, view());
            FloatUtil.multMatrix(projection, model);

            pointer.asFloatBuffer().put(projection).rewind();

            // Make sure the uniform buffer is uploaded
            gl3.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        gl3.glViewport(0, 0, windowSize.x, windowSize.y);

        float[] depth = {1.0f};
        gl3.glClearBufferfv(GL_DEPTH, 0, depth, 0);
        gl3.glClearBufferfv(GL_COLOR, 0, new float[]{0.0f, 0.0f, 0.0f, 1.0f}, 0);

        gl3.glUseProgram(programName);
        gl3.glBindVertexArray(vertexArrayName[0]);
        gl3.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[0]);

        gl3.glDrawArraysInstanced(GL_TRIANGLES, 0, 6, 1);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;
        gl3.glDeleteProgram(programName);
        gl3.glDeleteVertexArrays(1, vertexArrayName, 0);

        return true;
    }
}
