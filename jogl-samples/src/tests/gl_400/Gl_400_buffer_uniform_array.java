/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_400;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_BACK;
import static com.jogamp.opengl.GL.GL_DYNAMIC_DRAW;
import static com.jogamp.opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_FRAMEBUFFER;
import static com.jogamp.opengl.GL.GL_MAP_INVALIDATE_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_MAP_WRITE_BIT;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNSIGNED_SHORT;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT;
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
import java.nio.ShortBuffer;

/**
 *
 * @author GBarbieri
 */
public class Gl_400_buffer_uniform_array extends Test {

    public static void main(String[] args) {
        Gl_400_buffer_uniform_array gl_400_buffer_uniform_array = new Gl_400_buffer_uniform_array();
    }

    public Gl_400_buffer_uniform_array() {
        super("gl-400-buffer-uniform-array", Profile.CORE, 4, 0);
    }

    private final String SHADERS_SOURCE = "buffer-uniform-array";
    private final String SHADERS_ROOT = "src/data/gl_400";

    private int vertexCount = 4;
    private int positionSize = vertexCount * 2 * Float.BYTES;
    private float[] positionData = {
        -1.0f * 0.8f, -1.0f * 0.8f,
        +1.0f * 0.8f, -1.0f * 0.8f,
        +1.0f * 0.8f, +1.0f * 0.8f,
        -1.0f * 0.8f, +1.0f * 0.8f};

    private int elementCount = 6;
    private int elementSize = elementCount * Short.BYTES;
    private short[] elementData = {
        0, 1, 2,
        2, 3, 0};

    private enum Buffer {
        VERTEX,
        ELEMENT,
        TRANSFORM,
        MATERIAL,
        MAX
    }

    private int[] vertexArrayName = {0}, uniformBufferAlignment = {0}, bufferName = new int[Buffer.MAX.ordinal()];
    private int programName;
    private float[] projection = new float[16], model0 = new float[16], model1 = new float[16],
            mvp0 = new float[16], mvp1 = new float[16];

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

        gl4.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        gl4.glDrawBuffer(GL_BACK);
        if (!isFramebufferComplete(gl, 0)) {
            return false;
        }

        return validated;
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

            shaderProgram.init(gl4);

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);

            programName = shaderProgram.program();

            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            int uniformMaterial = gl4.glGetUniformBlockIndex(programName, "Material");
            int uniformTransform0 = gl4.glGetUniformBlockIndex(programName, "Transform[0]");
            int uniformTransform1 = gl4.glGetUniformBlockIndex(programName, "Transform[1]");

            gl4.glUniformBlockBinding(programName, uniformMaterial, Semantic.Uniform.MATERIAL);
            gl4.glUniformBlockBinding(programName, uniformTransform0, Semantic.Uniform.TRANSFORM0);
            gl4.glUniformBlockBinding(programName, uniformTransform1, Semantic.Uniform.TRANSFORM1);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(1, vertexArrayName, 0);
        gl4.glBindVertexArray(vertexArrayName[0]);
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 0, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);

            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT.ordinal()]);
        }
        gl4.glBindVertexArray(0);

        return true;
    }

    private boolean initBuffer(GL4 gl4) {

        gl4.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferAlignment, 0);

        gl4.glGenBuffers(Buffer.MAX.ordinal(), bufferName, 0);

        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT.ordinal()]);
        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);
        FloatBuffer positionBuffer = GLBuffers.newDirectFloatBuffer(positionData);
        gl4.glBufferData(GL_ARRAY_BUFFER, positionSize, positionBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        int UniformBufferSize = Math.max(uniformBufferAlignment[0], 16 * Float.BYTES) * 2;

        {
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM.ordinal()]);
            gl4.glBufferData(GL_UNIFORM_BUFFER, UniformBufferSize, null, GL_DYNAMIC_DRAW);
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }

        {
            float[] diffuse = {
                1.0f, 0.5f, 0.0f, 1.0f,
                0.0f, 0.5f, 1.0f, 1.0f};

            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.MATERIAL.ordinal()]);
            FloatBuffer diffuseBuffer = GLBuffers.newDirectFloatBuffer(diffuse);
            gl4.glBufferData(GL_UNIFORM_BUFFER, diffuse.length * Float.BYTES, diffuseBuffer, GL_STATIC_DRAW);
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }

        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        int uniformBufferOffset = Math.max(uniformBufferAlignment[0], 16 * Float.BYTES);
        int uniformBufferRange = uniformBufferOffset * 2;

        {
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM.ordinal()]);
            ByteBuffer pointer = gl4.glMapBufferRange(GL_UNIFORM_BUFFER,
                    0, uniformBufferRange, GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            FloatUtil.makePerspective(projection, 0, true, (float) Math.PI * 0.25f, 4.0f / 3.0f, 0.1f, 100.0f);
            FloatUtil.makeTranslation(model0, true, 1, 0, 0);
            FloatUtil.makeTranslation(model1, true, -1, 0, 0);

            FloatUtil.multMatrix(projection, view(), mvp0);
            FloatUtil.multMatrix(mvp0, model0);
            FloatUtil.multMatrix(projection, view(), mvp1);
            FloatUtil.multMatrix(mvp1, model1);

            for (int i = 0; i < 16; i++) {
                pointer.putFloat(uniformBufferOffset * 0 + i * Float.BYTES, mvp0[i]);
                pointer.putFloat(uniformBufferOffset * 1 + i * Float.BYTES, mvp1[i]);
            }
            pointer.rewind();

            // Make sure the uniform buffer is uploaded
            gl4.glUnmapBuffer(GL_UNIFORM_BUFFER);
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }

        gl4.glViewport(0, 0, windowSize.x, windowSize.y);
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, 0);

        gl4.glUseProgram(programName);

        // Attach the buffer to UBO binding point semantic::uniform::TRANSFORM0
        gl4.glBindBufferRange(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0,
                bufferName[Buffer.TRANSFORM.ordinal()], 0, 16 * Float.BYTES);
        gl4.glBindBufferRange(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM1,
                bufferName[Buffer.TRANSFORM.ordinal()], uniformBufferOffset, 16 * Float.BYTES);

        // Attach the buffer to UBO binding point semantic::uniform::MATERIAL
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.MATERIAL, bufferName[Buffer.MATERIAL.ordinal()]);

        gl4.glBindVertexArray(vertexArrayName[0]);
        gl4.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 2, 0);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteVertexArrays(1, vertexArrayName, 0);
        gl4.glDeleteBuffers(Buffer.MAX.ordinal(), bufferName, 0);
        gl4.glDeleteProgram(programName);

        return true;
    }
}
