/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_320.buffer;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2ES3.*;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import glm.glm;
import glm.mat._4.Mat4;
import glm.vec._2.Vec2;
import framework.BufferUtils;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 *
 * @author gbarbieri
 */
public class Gl_320_buffer_update extends Test {

    public static void main(String[] args) {
        Gl_320_buffer_update gl_320_buffer_update = new Gl_320_buffer_update();
    }

    public Gl_320_buffer_update() {
        super("gl-320-buffer-update", Profile.CORE, 3, 2);
    }

    private final String SHADERS_SOURCE = "buffer-update";
    private final String SHADERS_ROOT = "src/data/gl_320/buffer";

    private int vertexCount = 6;
    private int positionSize = vertexCount * Vec2.SIZE;
    private float[] positionData = new float[]{
        -1.0f, -1.0f,
        +1.0f, -1.0f,
        +1.0f, +1.0f,
        +1.0f, +1.0f,
        -1.0f, +1.0f,
        -1.0f, -1.0f,};

    private class Buffer {

        public static final int ARRAY = 0;
        public static final int COPY = 1;
        public static final int MATERIAL = 2;
        public static final int TRANSFORM = 3;
        public static final int MAX = 4;
    }

    private IntBuffer bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX),
            vertexArrayName = GLBuffers.newDirectIntBuffer(1);
    private int programName, uniformTransform, uniformMaterial;

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
        gl3.glGenBuffers(Buffer.MAX, bufferName);

        // Bind the buffer for use
        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.ARRAY));

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

        data.asFloatBuffer().put(positionData).rewind();

        // Explicitly send the data to the graphic card.
        gl3.glFlushMappedBufferRange(GL_ARRAY_BUFFER, 0, positionSize);

        gl3.glUnmapBuffer(GL_ARRAY_BUFFER);

        // Unbind the buffer
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        // Copy buffer
        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.COPY));
        gl3.glBufferData(GL_ARRAY_BUFFER, positionSize, null, GL_STATIC_DRAW);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        gl3.glBindBuffer(GL_COPY_READ_BUFFER, bufferName.get(Buffer.ARRAY));
        gl3.glBindBuffer(GL_COPY_WRITE_BUFFER, bufferName.get(Buffer.COPY));

        gl3.glCopyBufferSubData(
                GL_COPY_READ_BUFFER, GL_COPY_WRITE_BUFFER,
                0, 0,
                positionSize);

        gl3.glBindBuffer(GL_COPY_READ_BUFFER, 0);
        gl3.glBindBuffer(GL_COPY_WRITE_BUFFER, 0);

        int[] uniformBlockSize = {0};

        {
            gl3.glGetActiveUniformBlockiv(
                    programName,
                    uniformTransform,
                    GL_UNIFORM_BLOCK_DATA_SIZE,
                    uniformBlockSize, 0);

            gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.TRANSFORM));
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

            gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.MATERIAL));
            FloatBuffer diffuseBuffer = GLBuffers.newDirectFloatBuffer(diffuse);
            gl3.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize[0], diffuseBuffer, GL_DYNAMIC_DRAW);
            BufferUtils.destroyDirectBuffer(diffuseBuffer);
            gl3.glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }

        return checkError(gl3, "initBuffer");
    }

    private boolean initVertexArray(GL3 gl3) {

        gl3.glGenVertexArrays(1, vertexArrayName);
        gl3.glBindVertexArray(vertexArrayName.get(0));
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.COPY));
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, Vec2.SIZE, 0);
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
            gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.TRANSFORM));
            ByteBuffer transformBuffer = gl3.glMapBufferRange(GL_UNIFORM_BUFFER, 0, Mat4.SIZE,
                    GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            Mat4 projection = glm.perspective_((float) Math.PI * 0.25f,
                    (float) windowSize.x / windowSize.y, 0.1f, 100.0f);
            Mat4 model = new Mat4(1.0f);
            Mat4 mvp = projection.mul(viewMat4()).mul(model);

            transformBuffer.asFloatBuffer().put(mvp.toFa_());

            // Make sure the uniform buffer is uploaded
            gl3.glUnmapBuffer(GL_UNIFORM_BUFFER);
            gl3.glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }

        gl3.glViewport(0, 0, windowSize.x, windowSize.y);
        gl3.glClearBufferfv(GL_COLOR, 0, new float[]{0.0f, 0.0f, 0.0f, 1.0f}, 0);

        gl3.glUseProgram(programName);

        // Attach the buffer to UBO binding point semantic::uniform::TRANSFORM0
        gl3.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName.get(Buffer.TRANSFORM));
        // Attach the buffer to UBO binding point semantic::uniform::MATERIAL
        gl3.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.MATERIAL, bufferName.get(Buffer.MATERIAL));

        gl3.glBindVertexArray(vertexArrayName.get(0));
        gl3.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 1);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;

        gl3.glDeleteBuffers(Buffer.MAX, bufferName);
        gl3.glDeleteProgram(programName);
        gl3.glDeleteVertexArrays(1, vertexArrayName);

        BufferUtils.destroyDirectBuffer(bufferName);
        BufferUtils.destroyDirectBuffer(vertexArrayName);

        return true;
    }
}
