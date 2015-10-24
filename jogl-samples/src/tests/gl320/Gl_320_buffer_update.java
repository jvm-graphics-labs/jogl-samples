/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl320;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_DYNAMIC_DRAW;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_MAP_FLUSH_EXPLICIT_BIT;
import static com.jogamp.opengl.GL.GL_MAP_INVALIDATE_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_MAP_UNSYNCHRONIZED_BIT;
import static com.jogamp.opengl.GL.GL_MAP_WRITE_BIT;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import static com.jogamp.opengl.GL2ES3.GL_COPY_READ_BUFFER;
import static com.jogamp.opengl.GL2ES3.GL_COPY_WRITE_BUFFER;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BLOCK_DATA_SIZE;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.Semantic;
import framework.Test;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 *
 * @author gbarbieri
 */
public class Gl_320_buffer_update extends Test {

    public static void main(String[] args) {
        Gl_320_buffer_update gl_320_buffer_update = new Gl_320_buffer_update();
    }

    public Gl_320_buffer_update() {
        super("gl-320-buffer-update", 3, 2);
    }

    private final String SHADERS_SOURCE = "buffer-update";
    private final String SHADERS_ROOT = "src/data/gl_320";

    private int vertexCount = 6;
    private int positionSize = vertexCount * 2 * GLBuffers.SIZEOF_FLOAT;
    private float[] positionData = new float[]{
        -1.0f, -1.0f,
        +1.0f, -1.0f,
        +1.0f, +1.0f,
        +1.0f, +1.0f,
        -1.0f, +1.0f,
        -1.0f, -1.0f,};

    private enum Buffer {

        array, copy, material, transform, max
    }

    private int[] bufferName = new int[Buffer.max.ordinal()], vertexArrayName = new int[1];
    private int programName, uniformTransform, uniformMaterial;
    private float[] projection = new float[16], model = new float[16], mvp = new float[16];

    @Override
    protected boolean begin(GL gl) {

        boolean validated = true;

        GL3 gl3 = (GL3) gl;

        if (validated) {
            validated = initProgram(gl3);
        }
        if (validated) {
            validated = initBuffer(gl3);
        }
        if (validated) {
            validated = initVertexArray(gl3);
        }
        return validated && checkError(gl, "begin");
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
            gl3.glBindAttribLocation(programName, Semantic.Attr.COLOR, "color");

            program.link(gl3, System.out);
        }
        // Get variables locations
        if (validated) {
            uniformTransform = gl3.glGetUniformBlockIndex(programName, "Transform");
            uniformMaterial = gl3.glGetUniformBlockIndex(programName, "Material");

            gl3.glUniformBlockBinding(programName, uniformTransform, Semantic.Uniform.TRANSFORM0);
            gl3.glUniformBlockBinding(programName, uniformMaterial, Semantic.Uniform.MATERIAL);
        }

        return validated & checkError(gl3, "initProgram");
    }

    // Buffer update using glMapBufferRange
    private boolean initBuffer(GL3 gl3) {
        // Generate a buffer object
        gl3.glGenBuffers(Buffer.max.ordinal(), bufferName, 0);

        // Bind the buffer for use
        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.array.ordinal()]);

        // Reserve buffer memory but don't copy the values
        gl3.glBufferData(
                GL_ARRAY_BUFFER,
                positionSize,
                null,
                GL_STATIC_DRAW);

        // Copy the vertex data in the buffer, in this sample for the whole range of data.
        // It doesn't required to be the buffer size but pointers require no memory overlapping.
        ByteBuffer data = gl3.glMapBufferRange(
                GL_ARRAY_BUFFER,
                0, // Offset
                positionSize, // Size,
                GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT | GL_MAP_UNSYNCHRONIZED_BIT | GL_MAP_FLUSH_EXPLICIT_BIT);

        for (int f = 0; f < positionData.length; f++) {
            data.putFloat(f * GLBuffers.SIZEOF_FLOAT, positionData[f]);
        }

        // Explicitly send the data to the graphic card.
        gl3.glFlushMappedBufferRange(GL_ARRAY_BUFFER, 0, positionSize);

        gl3.glUnmapBuffer(GL_ARRAY_BUFFER);

        // Unbind the buffer
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        // Copy buffer
        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.copy.ordinal()]);
        gl3.glBufferData(GL_ARRAY_BUFFER, positionSize, null, GL_STATIC_DRAW);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        gl3.glBindBuffer(GL_COPY_READ_BUFFER, bufferName[Buffer.array.ordinal()]);
        gl3.glBindBuffer(GL_COPY_WRITE_BUFFER, bufferName[Buffer.copy.ordinal()]);

        gl3.glCopyBufferSubData(
                GL_COPY_READ_BUFFER, GL_COPY_WRITE_BUFFER,
                0, 0,
                positionSize);

        gl3.glBindBuffer(GL_COPY_READ_BUFFER, 0);
        gl3.glBindBuffer(GL_COPY_WRITE_BUFFER, 0);

        int[] uniformBlockSize = new int[1];

        {
            gl3.glGetActiveUniformBlockiv(
                    programName,
                    uniformTransform,
                    GL_UNIFORM_BLOCK_DATA_SIZE,
                    uniformBlockSize, 0);

            gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.transform.ordinal()]);
            gl3.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize[0], null, GL_DYNAMIC_DRAW);
            gl3.glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }

        {
            float[] diffuse = new float[]{1.0f, 0.5f, 0.0f, 1.0f};

            gl3.glGetActiveUniformBlockiv(
                    programName,
                    uniformMaterial,
                    GL_UNIFORM_BLOCK_DATA_SIZE,
                    uniformBlockSize, 0);

            gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.material.ordinal()]);
            FloatBuffer diffuseBuffer = GLBuffers.newDirectFloatBuffer(diffuse);
            gl3.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize[0], diffuseBuffer, GL_DYNAMIC_DRAW);
            gl3.glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }

        return checkError(gl3, "initBuffer");
    }

    private boolean initVertexArray(GL3 gl3) {

        gl3.glGenVertexArrays(1, vertexArrayName, 0);
        gl3.glBindVertexArray(vertexArrayName[0]);
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.copy.ordinal()]);
            int stride = 2 * GLBuffers.SIZEOF_FLOAT, offset = 0;
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, stride, offset);
            gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);
        }
        gl3.glBindVertexArray(0);

        return checkError(gl3, "initVertexArray");
    }

    @Override
    protected boolean render(GL gl) {

        GL3 gl3 = (GL3) gl;

        {
            gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.transform.ordinal()]);
            ByteBuffer transformBuffer = gl3.glMapBufferRange(
                    GL_UNIFORM_BUFFER, 0, 16 * GLBuffers.SIZEOF_FLOAT,
                    GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);
            projection = FloatUtil.makePerspective(projection, 0, true, (float) Math.PI * 0.25f,
                    (float) glWindow.getWidth() / glWindow.getHeight(), 0.1f, 100.0f);
            FloatUtil.makeIdentity(model);
            // update view matrix
            view();
            // view *= model
            FloatUtil.multMatrix(view, model);
            mvp = FloatUtil.multMatrix(projection, view);

            for (int f = 0; f < mvp.length; f++) {
                transformBuffer.putFloat(f * GLBuffers.SIZEOF_FLOAT, mvp[f]);
            }
            transformBuffer.rewind();

            // Make sure the uniform buffer is uploaded
            gl3.glUnmapBuffer(GL_UNIFORM_BUFFER);
            gl3.glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }

        gl3.glViewport(0, 0, glWindow.getWidth(), glWindow.getHeight());
        gl3.glClearBufferfv(GL_COLOR, 0, new float[]{0.0f, 0.0f, 0.0f, 1.0f}, 0);

        gl3.glUseProgram(programName);

        // Attach the buffer to UBO binding point semantic::uniform::TRANSFORM0
        gl3.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.transform.ordinal()]);
        // Attach the buffer to UBO binding point semantic::uniform::MATERIAL
        gl3.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.MATERIAL, bufferName[Buffer.material.ordinal()]);

        gl3.glBindVertexArray(vertexArrayName[0]);
        gl3.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 1);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;

        gl3.glDeleteBuffers(Buffer.max.ordinal(), bufferName, 0);
        gl3.glDeleteProgram(programName);
        gl3.glDeleteVertexArrays(1, vertexArrayName, 0);

        return true;
    }
}
