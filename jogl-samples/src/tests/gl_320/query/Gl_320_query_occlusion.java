/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_320.query;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2GL3.*;
import com.jogamp.opengl.GL3;
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
public class Gl_320_query_occlusion extends Test {

    public static void main(String[] args) {
        Gl_320_query_occlusion gl_320_query_occlusion = new Gl_320_query_occlusion();
    }

    public Gl_320_query_occlusion() {
        super("gl-320-query-occlusion", Profile.CORE, 3, 2);
    }

    private final String SHADERS_SOURCE = "query-occlusion";
    private final String SHADERS_ROOT = "src/data/gl_320/query";

    private int vertexCount = 6;
    private int positionSize = vertexCount * 2 * Float.BYTES;
    private float[] positionData = {
        -1.0f, -1.0f,
        +1.0f, -1.0f,
        +1.0f, +1.0f,
        +1.0f, +1.0f,
        -1.0f, +1.0f,
        -1.0f, -1.0f};

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int TRANSFORM = 1;
        public static final int MAX = 2;
    }

    private int[] bufferName = new int[Buffer.MAX], vertexArrayName = new int[1], queryName = new int[1];
    private int programName;
    private float[] projection = new float[16], model = new float[16];

    @Override
    protected boolean begin(GL gl) {

        GL3 gl3 = (GL3) gl;

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
        if (validated) {
            validated = initQuery(gl3);
        }

        return validated && checkError(gl3, "begin");
    }

    private boolean initQuery(GL3 gl3) {

        gl3.glGenQueries(1, queryName, 0);

        int[] queryBits = {0};
        gl3.glGetQueryiv(GL_SAMPLES_PASSED, GL_QUERY_COUNTER_BITS, queryBits, 0);

        boolean validated = queryBits[0] >= 32;

        return validated && checkError(gl3, "initQuery");
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

            gl3.glBindAttribLocation(programName, Semantic.Attr.POSITION, "position");
            gl3.glBindFragDataLocation(programName, Semantic.Frag.COLOR, "color");

            shaderProgram.link(gl3, System.out);
        }
        if (validated) {

            gl3.glUniformBlockBinding(
                    programName,
                    gl3.glGetUniformBlockIndex(programName, "Transform"),
                    Semantic.Uniform.TRANSFORM0);
        }

        return validated & checkError(gl3, "initProgram");
    }

    private boolean initBuffer(GL3 gl3) {

        gl3.glGenBuffers(Buffer.MAX, bufferName, 0);

        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
        FloatBuffer positionBuffer = GLBuffers.newDirectFloatBuffer(positionData);
        gl3.glBufferData(GL_ARRAY_BUFFER, positionSize, positionBuffer, GL_STATIC_DRAW);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        int[] uniformBufferOffset = {0};
        gl3.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset, 0);
        int uniformBlockSize = Math.max(16 * Float.BYTES, uniformBufferOffset[0]);

        gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
        gl3.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_DYNAMIC_DRAW);
        gl3.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return checkError(gl3, "initBuffer");
    }

    private boolean initVertexArray(GL3 gl3) {

        gl3.glGenVertexArrays(1, vertexArrayName, 0);
        gl3.glBindVertexArray(vertexArrayName[0]);
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 0, 0);
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
            gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
            ByteBuffer pointer = gl3.glMapBufferRange(GL_UNIFORM_BUFFER,
                    0, 16 * Float.BYTES, GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            FloatUtil.makePerspective(projection, 0, true, (float) Math.PI * 0.25f,
                    (float) windowSize.x / windowSize.y, 0.1f, 100.0f);
            FloatUtil.makeIdentity(model);

            FloatUtil.multMatrix(projection, view());
            FloatUtil.multMatrix(projection, model);

            pointer.asFloatBuffer().put(projection).rewind();

            gl3.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        // Set the display viewport
        gl3.glViewport(0, 0, windowSize.x, windowSize.y);

        // Clear color buffer with black
        gl3.glClearBufferfv(GL_COLOR, 0, new float[]{0.0f, 0.0f, 0.0f, 1.0f}, 0);

        gl3.glUseProgram(programName);
        gl3.glBindVertexArray(vertexArrayName[0]);
        gl3.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.TRANSFORM]);

        // Samples count query
        gl3.glBeginQuery(GL_SAMPLES_PASSED, queryName[0]);
        {
            gl3.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 1);
        }
        gl3.glEndQuery(GL_SAMPLES_PASSED);

        // Get the count of samples. 
        // If the result of the query isn't here yet, we wait here...
        int[] samplesCount = {0};
        gl3.glGetQueryObjectuiv(queryName[0], GL_QUERY_RESULT, samplesCount, 0);
        System.out.println("Samples count: " + samplesCount[0]);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;

        gl3.glDeleteBuffers(Buffer.MAX, bufferName, 0);
        gl3.glDeleteProgram(programName);
        gl3.glDeleteVertexArrays(1, vertexArrayName, 0);

        return checkError(gl3, "end");
    }
}
