/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl320.draw;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_DYNAMIC_DRAW;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_MAP_INVALIDATE_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_MAP_WRITE_BIT;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import static com.jogamp.opengl.GL2ES3.GL_DEPTH;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 *
 * @author GBarbieri
 */
public class Gl_320_draw_instanced extends Test {

    public static void main(String[] args) {
        Gl_320_draw_instanced gl_320_draw_instanced = new Gl_320_draw_instanced();
    }

    public Gl_320_draw_instanced() {
        super("gl-320-draw-instanced", Profile.CORE, 3, 2);
    }

    private final String SHADERS_SOURCE = "draw-instanced";
    private final String SHADERS_ROOT = "src/data/gl_320/draw";

    private int vertexCount = 6;
    private int positionSize = vertexCount * 2 * GLBuffers.SIZEOF_FLOAT;
    private float[] positionData = new float[]{
        -1.0f, -1.0f,
        +1.0f, -1.0f,
        +1.0f, +1.0f,
        +1.0f, +1.0f,
        -1.0f, +1.0f,
        -1.0f, -1.0f
    };

    private enum Buffer {

        VERTEX, TRANSFORM, MATERIAL, MAX
    }

    private int[] bufferName = new int[Buffer.MAX.ordinal()], vertexArrayName = new int[1];
    private int programName, uniformTransform, uniformMaterial;
    private float[] projection = new float[16], modelA = new float[16], modelB = new float[16], mvp = new float[16 * 2];

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;

        if (validated) {
            validated = initTest(gl4);
        }
        if (validated) {
            validated = initProgram(gl4);
        }
        if (validated) {
            validated = initBuffer(gl4);
        }
        if (validated) {
            validated = initVertexArray(gl4);
        }

        return validated & checkError(gl4, "begin");
    }

    private boolean initTest(GL4 gl4) {

        boolean validated = true;

        gl4.glEnable(GL_DEPTH_TEST);

        return validated & checkError(gl4, "initTest");
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

            gl3.glBindAttribLocation(programName, Semantic.Attr.POSITION, "position");
            gl3.glBindFragDataLocation(programName, Semantic.Frag.COLOR, "color");

            program.link(gl3, System.out);
        }
        // Get variables locations
        if (validated) {

            uniformTransform = gl3.glGetUniformBlockIndex(programName, "Transform");
            uniformMaterial = gl3.glGetUniformBlockIndex(programName, "Material");
        }

        return validated & checkError(gl3, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        gl4.glGenBuffers(Buffer.MAX.ordinal(), bufferName, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);
        FloatBuffer positionBuffer = GLBuffers.newDirectFloatBuffer(positionData);
        gl4.glBufferData(GL_ARRAY_BUFFER, positionSize, positionBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        int[] uniformBufferOffset = new int[]{0};
        gl4.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset, 0);

        int uniformTransformBlockSize = Math.max(16 * 2 * GLBuffers.SIZEOF_FLOAT, uniformBufferOffset[0]);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM.ordinal()]);
        gl4.glBufferData(GL_UNIFORM_BUFFER, uniformTransformBlockSize, null, GL_DYNAMIC_DRAW);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        int uniformMaterialBlockSize = Math.max(4 * 2 * GLBuffers.SIZEOF_FLOAT, uniformBufferOffset[0]);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.MATERIAL.ordinal()]);
        gl4.glBufferData(GL_UNIFORM_BUFFER, uniformMaterialBlockSize, null, GL_STATIC_DRAW);

        ByteBuffer pointer = gl4.glMapBufferRange(GL_UNIFORM_BUFFER, 0, 4 * 2 * GLBuffers.SIZEOF_FLOAT,
                GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);
        float[] vecs = new float[]{1.0f, 0.5f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f, 1.0f};
        for (int f = 0; f < vecs.length; f++) {
            pointer.putFloat(f * GLBuffers.SIZEOF_FLOAT, vecs[f]);
        }
        pointer.rewind();
        gl4.glUnmapBuffer(GL_UNIFORM_BUFFER);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return checkError(gl4, "initBuffer");
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(1, vertexArrayName, 0);
        gl4.glBindVertexArray(vertexArrayName[0]);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);
        gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 0, 0);

        gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
        gl4.glBindVertexArray(0);

        return checkError(gl4, "initVertexArray");
    }

    @Override
    protected boolean render(GL gl) {
        GL4 gl4 = (GL4) gl;
        {
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM.ordinal()]);
            ByteBuffer pointer = gl4.glMapBufferRange(
                    GL_UNIFORM_BUFFER, 0, 16 * 2 * GLBuffers.SIZEOF_FLOAT,
                    GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            FloatUtil.makePerspective(projection, 0, true, (float) Math.PI * 0.25f,
                    (float) glWindow.getWidth() / glWindow.getHeight(), 0.1f, 100.0f);
            FloatUtil.makeTranslation(modelA, true, -1.1f, 0.0f, 0.0f);
            FloatUtil.makeTranslation(modelB, true, +1.1f, 0.0f, 0.0f);

            FloatUtil.multMatrix(projection, 0, view(), 0, mvp, 0);
            FloatUtil.multMatrix(mvp, 0, modelA, 0);
            FloatUtil.multMatrix(projection, 0, view(), 0, mvp, 16);
            FloatUtil.multMatrix(mvp, 16, modelB, 0);

            for (int f = 0; f < mvp.length; f++) {
                pointer.putFloat(mvp[f]);
            }

            // Make sure the uniform buffer is uploaded
            gl4.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        gl4.glViewport(0, 0, glWindow.getWidth(), glWindow.getHeight());

        float[] depth = new float[]{1.0f};
        gl4.glClearBufferfv(GL_DEPTH, 0, depth, 0);
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, 0);

        gl4.glUseProgram(programName);
        gl4.glUniformBlockBinding(programName, uniformTransform, Semantic.Uniform.TRANSFORM0);
        gl4.glUniformBlockBinding(programName, uniformMaterial, Semantic.Uniform.MATERIAL);

        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.TRANSFORM.ordinal()]);
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.MATERIAL, bufferName[Buffer.MATERIAL.ordinal()]);
        gl4.glBindVertexArray(vertexArrayName[0]);

        gl4.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 2);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;
        gl3.glDeleteBuffers(Buffer.MAX.ordinal(), bufferName, 0);
        gl3.glDeleteProgram(programName);
        gl3.glDeleteVertexArrays(1, vertexArrayName, 0);
        
        return true;
    }
}
