/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_330;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import static com.jogamp.opengl.GL3ES3.*;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import glm.vec._2.i.Vec2i;
import dev.Vec2i8;
import framework.BufferUtils;
import glm.glm;
import glm.mat._4.Mat4;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import glm.vec._2.Vec2;
import glm.vec._4.Vec4;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import jglm.Vec4i;

/**
 *
 * @author GBarbieri
 */
public class Gl_330_buffer_type extends Test {

    public static void main(String[] args) {
        Gl_330_buffer_type gl_330_buffer_type = new Gl_330_buffer_type();
    }

    public Gl_330_buffer_type() {
        super("gl-330-buffer-type", Profile.CORE, 3, 3);
    }

    private final String SHADERS_SOURCE = "flat-color";
    private final String SHADERS_ROOT = "src/data/gl_330";

    private int vertexCount = 6;

    private int positionSizeF32 = vertexCount * Vec2.SIZE;
    private float[] positionDataF32 = {
        -1.0f, -1.0f,
        +1.0f, -1.0f,
        +1.0f, +1.0f,
        +1.0f, +1.0f,
        -1.0f, +1.0f,
        -1.0f, -1.0f};

    private int positionSizeI8 = vertexCount * Vec2i8.SIZE;
    private byte[] positionDataI8 = {
        (byte) -1, (byte) -1,
        (byte) +1, (byte) -1,
        (byte) +1, (byte) +1,
        (byte) +1, (byte) +1,
        (byte) -1, (byte) +1,
        (byte) -1, (byte) -1};

    private int positionSizeRGB10A2 = vertexCount * Integer.BYTES;

    private int[] positionDataRGB10A2 = {
        glm.packSnorm3x10_1x2(new Vec4(-1.0f, -1.0f, 0.0f, 1.0f)),
        glm.packSnorm3x10_1x2(new Vec4(+1.0f, -1.0f, 0.0f, 1.0f)),
        glm.packSnorm3x10_1x2(new Vec4(+1.0f, +1.0f, 0.0f, 1.0f)),
        glm.packSnorm3x10_1x2(new Vec4(+1.0f, +1.0f, 0.0f, 1.0f)),
        glm.packSnorm3x10_1x2(new Vec4(-1.0f, +1.0f, 0.0f, 1.0f)),
        glm.packSnorm3x10_1x2(new Vec4(-1.0f, -1.0f, 0.0f, 1.0f))};

    private int positionSizeI32 = vertexCount * Vec2i.SIZE;
    private int[] positionDataI32 = {
        -1, -1,
        +1, -1,
        +1, +1,
        +1, +1,
        -1, +1,
        -1, -1};

    private class Buffer {

        public static final int RGB10A2 = 0;
        public static final int F32 = 1;
        public static final int I8 = 2;
        public static final int I32 = 3;
        public static final int MAX = 4;
    }

    private int programName, uniformMvp, uniformDiffuse;
    private int[] bufferName = new int[Buffer.MAX], vertexArrayName = new int[Buffer.MAX];
    private Vec4i[] viewport = new Vec4i[Buffer.MAX];

    @Override
    protected boolean begin(GL gl) {

        GL3 gl3 = (GL3) gl;

        viewport[Buffer.RGB10A2] = new Vec4i(0, 0, windowSize.x >> 1, windowSize.y >> 1);
        viewport[Buffer.F32] = new Vec4i(windowSize.x >> 1, 0, windowSize.x >> 1, windowSize.y >> 1);
        viewport[Buffer.I8] = new Vec4i(windowSize.x >> 1, windowSize.y >> 1, windowSize.x >> 1, windowSize.y >> 1);
        viewport[Buffer.I32] = new Vec4i(0, windowSize.y >> 1, windowSize.x >> 1, windowSize.y >> 1);

        boolean validated = true;

        if (validated) {
            validated = initProgram(gl3);
        }
        if (validated) {
            validated = initBuffer(gl3);
        }
        if (validated) {
            validated = initVertexArray(gl3);
        }

        return validated && checkError(gl3, "begin");
    }

    private boolean initProgram(GL3 gl3) {

        boolean validated = true;

        if (validated) {

            ShaderCode vertShaderCode = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

            ShaderProgram shaderProgram = new ShaderProgram();

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);

            shaderProgram.init(gl3);

            programName = shaderProgram.program();

            shaderProgram.link(gl3, System.out);
        }

        // Get variables locations
        if (validated) {

            uniformMvp = gl3.glGetUniformLocation(programName, "mvp");
            uniformDiffuse = gl3.glGetUniformLocation(programName, "diffuse");
        }

        return validated & checkError(gl3, "initProgram");
    }

    private boolean initBuffer(GL3 gl3) {

        // Generate a buffer object
        gl3.glGenBuffers(Buffer.MAX, bufferName, 0);

        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.F32]);
        FloatBuffer positionF32Buffer = GLBuffers.newDirectFloatBuffer(positionDataF32);
        gl3.glBufferData(GL_ARRAY_BUFFER, positionSizeF32, positionF32Buffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(positionF32Buffer);

        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.I8]);
        ByteBuffer positionI8Buffer = GLBuffers.newDirectByteBuffer(positionDataI8);
        gl3.glBufferData(GL_ARRAY_BUFFER, positionSizeI8, positionI8Buffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(positionI8Buffer);

        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.I32]);
        IntBuffer positionI32Buffer = GLBuffers.newDirectIntBuffer(positionDataI32);
        gl3.glBufferData(GL_ARRAY_BUFFER, positionSizeI32, positionI32Buffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(positionI32Buffer);

        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.RGB10A2]);
        IntBuffer positionRGB10A2Buffer = GLBuffers.newDirectIntBuffer(positionDataRGB10A2);
        gl3.glBufferData(GL_ARRAY_BUFFER, positionSizeRGB10A2, positionRGB10A2Buffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(positionRGB10A2Buffer);

        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        return checkError(gl3, "initBuffer");
    }

    private boolean initVertexArray(GL3 gl3) {

        gl3.glGenVertexArrays(Buffer.MAX, vertexArrayName, 0);

        gl3.glBindVertexArray(vertexArrayName[Buffer.F32]);
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.F32]);
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, Vec2.SIZE, 0);
            gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);
        }
        gl3.glBindVertexArray(0);

        gl3.glBindVertexArray(vertexArrayName[Buffer.I8]);
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.I8]);
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_BYTE, false, Vec2i8.SIZE, 0);
            gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);
        }
        gl3.glBindVertexArray(0);

        gl3.glBindVertexArray(vertexArrayName[Buffer.I32]);
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.I32]);
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_INT, false, Vec2i.SIZE, 0);
            gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);
        }
        gl3.glBindVertexArray(0);

        gl3.glBindVertexArray(vertexArrayName[Buffer.RGB10A2]);
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.RGB10A2]);
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 4, GL_INT_2_10_10_10_REV, true, Integer.BYTES, 0);
            gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);
        }
        gl3.glBindVertexArray(0);

        return checkError(gl3, "initVertexArray");
    }

    @Override
    protected boolean render(GL gl) {

        GL3 gl3 = (GL3) gl;

        Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, (float) windowSize.x / windowSize.y, 0.1f, 100.0f);
        Mat4 model = new Mat4(1.0f);
        Mat4 mvp = projection.mul(viewMat4()).mul(model);

        gl3.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, 0);

        gl3.glUseProgram(programName);
        gl3.glUniform4fv(uniformDiffuse, 1, new float[]{1.0f, 0.5f, 0.0f, 1.0f}, 0);
        gl3.glUniformMatrix4fv(uniformMvp, 1, false, mvp.toFa_(), 0);

        for (int index = 0; index < Buffer.MAX; ++index) {

            gl3.glViewport(viewport[index].x, viewport[index].y, viewport[index].z, viewport[index].w);

            gl3.glBindVertexArray(vertexArrayName[index]);
            gl3.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 1);
        }

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;

        gl3.glDeleteBuffers(Buffer.MAX, bufferName, 0);
        gl3.glDeleteVertexArrays(Buffer.MAX, vertexArrayName, 0);
        gl3.glDeleteProgram(programName);

        return checkError(gl3, "end");
    }
}
