/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_400;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL3ES3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

/**
 *
 * @author GBarbieri
 */
public class Gl_400_primitive_smooth_shading extends Test {

    public static void main(String[] args) {
        Gl_400_primitive_smooth_shading gl_400_primitive_smooth_shading = new Gl_400_primitive_smooth_shading();
    }

    public Gl_400_primitive_smooth_shading() {
        super("gl-400-primitive-smooth-shading", Profile.CORE, 4, 0);
    }

    private final String SHADERS_SOURCE1 = "tess";
    private final String SHADERS_SOURCE2 = "smooth-shading-geom";
    private final String SHADERS_ROOT = "src/data/gl_400";

    private int vertexCount = 4;
    private int vertexSize = vertexCount * (2 * Float.BYTES + 4 * Byte.BYTES);
    private float[] vertexV2fData = {
        -1.0f, -1.0f,
        +1.0f, -1.0f,
        +1.0f, +1.0f,
        -1.0f, +1.0f,};
    private byte[] vertexC4ubData = {
        (byte) 255, (byte) 0, (byte) 0, (byte) 255,
        (byte) 255, (byte) 255, (byte) 255, (byte) 255,
        (byte) 0, (byte) 255, (byte) 0, (byte) 255,
        (byte) 0, (byte) 0, (byte) 255, (byte) 255};

    private int elementCount = 6;
    private int elementSize = elementCount * Short.BYTES;
    private short[] elementData = {
        0, 1, 2,
        2, 3, 0};

    private int[] programName = {0, 0}, elementBufferName = {0}, arrayBufferName = {0}, vertexArrayName = {0},
            uniformMvp = {0, 0};
    private float[] projection = new float[16], model = new float[16], mvp = new float[16];

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;

        if (validated) {
            validated = initProgram(gl4);
        }
        if (validated) {
            validated = initBuffer(gl4);
        }
        if (validated) {
            validated = initVertexArray(gl4);
        }

        return validated && checkError(gl4, "begin");
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE1, "vert", null, true);
            ShaderCode contShaderCode = ShaderCode.create(gl4, GL_TESS_CONTROL_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE1, "cont", null, true);
            ShaderCode evalShaderCode = ShaderCode.create(gl4, GL_TESS_EVALUATION_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE1, "eval", null, true);
            ShaderCode geomShaderCode = ShaderCode.create(gl4, GL_GEOMETRY_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE1, "geom", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE1, "frag", null, true);

            shaderProgram.init(gl4);

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(geomShaderCode);
            shaderProgram.add(contShaderCode);
            shaderProgram.add(evalShaderCode);
            shaderProgram.add(fragShaderCode);

            programName[0] = shaderProgram.program();

            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE1, "vert", null, true);
            ShaderCode geomShaderCode = ShaderCode.create(gl4, GL_GEOMETRY_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE1, "geom", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE1, "frag", null, true);

            shaderProgram.init(gl4);

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(geomShaderCode);
            shaderProgram.add(fragShaderCode);

            programName[1] = shaderProgram.program();

            shaderProgram.link(gl4, System.out);
        }

        // Get variables locations
        if (validated) {

            uniformMvp[0] = gl4.glGetUniformLocation(programName[0], "mvp");
            uniformMvp[1] = gl4.glGetUniformLocation(programName[1], "mvp");
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(1, vertexArrayName, 0);
        gl4.glBindVertexArray(vertexArrayName[0]);
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, arrayBufferName[0]);
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 2 * Float.BYTES + 4 * Byte.BYTES, 0);
            gl4.glVertexAttribPointer(Semantic.Attr.COLOR, 4, GL_UNSIGNED_BYTE, true,
                    2 * Float.BYTES + 4 * Byte.BYTES, 2 * Float.BYTES);
            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl4.glEnableVertexAttribArray(Semantic.Attr.COLOR);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);
        }
        gl4.glBindVertexArray(0);

        return checkError(gl4, "initVertexArray");
    }

    private boolean initBuffer(GL4 gl4) {

        gl4.glGenBuffers(1, elementBufferName, 0);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, elementBufferName[0]);
        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl4.glGenBuffers(1, arrayBufferName, 0);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, arrayBufferName[0]);
        ByteBuffer vertexBuffer = GLBuffers.newDirectByteBuffer(vertexSize);
        for (int vertex = 0; vertex < vertexCount; vertex++) {
            for (int position = 0; position < 2; position++) {
                vertexBuffer.putFloat(vertexV2fData[vertex * 2 + position]);
            }
            for (int color = 0; color < 4; color++) {
                vertexBuffer.put(vertexC4ubData[vertex * 4 + color]);
            }
        }
        vertexBuffer.rewind();
        gl4.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        return checkError(gl4, "initBuffer");
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        FloatUtil.makePerspective(projection, 0, true, (float) Math.PI * 0.25f,
                windowSize.x * 0.5f / windowSize.y, 0.1f, 100.0f);
        FloatUtil.makeIdentity(model);
        FloatUtil.multMatrix(projection, view(), mvp);
        FloatUtil.multMatrix(mvp, model);

        gl4.glViewport(0, 0, windowSize.x, windowSize.y);
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{0.0f, 0.0f, 0.0f, 1.0f}, 0);

        gl4.glBindVertexArray(vertexArrayName[0]);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, elementBufferName[0]);

        gl4.glViewport(0, 0, (int) (windowSize.x * 0.5f), windowSize.y);
        gl4.glUseProgram(programName[0]);
        gl4.glUniformMatrix4fv(uniformMvp[0], 1, false, mvp, 0);

        gl4.glPatchParameteri(GL_PATCH_VERTICES, vertexCount);
        gl4.glDrawArraysInstanced(GL_PATCHES, 0, vertexCount, 1);

        gl4.glViewport((int) (windowSize.x * 0.5f), 0, (int) (windowSize.x * 0.5f), windowSize.y);
        gl4.glUseProgram(programName[1]);
        gl4.glUniformMatrix4fv(uniformMvp[1], 1, false, mvp, 0);

        gl4.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteVertexArrays(1, vertexArrayName, 0);
        gl4.glDeleteBuffers(1, arrayBufferName, 0);
        gl4.glDeleteBuffers(1, elementBufferName, 0);
        gl4.glDeleteProgram(programName[0]);
        gl4.glDeleteProgram(programName[1]);

        return checkError(gl4, "end");
    }
}
