/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_440;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_DYNAMIC_DRAW;
import static com.jogamp.opengl.GL.GL_MAP_INVALIDATE_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_MAP_READ_BIT;
import static com.jogamp.opengl.GL.GL_MAP_WRITE_BIT;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_TRUE;
import static com.jogamp.opengl.GL2ES2.GL_ANY_SAMPLES_PASSED;
import static com.jogamp.opengl.GL2ES2.GL_ANY_SAMPLES_PASSED_CONSERVATIVE;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER_BIT;
import static com.jogamp.opengl.GL2ES2.GL_PROGRAM_SEPARABLE;
import static com.jogamp.opengl.GL2ES2.GL_QUERY_COUNTER_BITS;
import static com.jogamp.opengl.GL2ES2.GL_QUERY_RESULT;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER_BIT;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import static com.jogamp.opengl.GL2ES3.GL_DYNAMIC_COPY;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT;
import static com.jogamp.opengl.GL3ES3.GL_SHADER_STORAGE_BUFFER;
import com.jogamp.opengl.GL4;
import static com.jogamp.opengl.GL4.GL_QUERY_BUFFER;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import core.glm;
import dev.Mat4;
import dev.Vec2;
import dev.Vec4;
import framework.BufferUtils;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 *
 * @author elect
 */
public class Gl_440_query_occlusion extends Test {

    public static void main(String[] args) {
        Gl_440_query_occlusion gl_440_query_occlusion = new Gl_440_query_occlusion();
    }

    public Gl_440_query_occlusion() {
        super("gl-440-query-occlusion", Profile.CORE, 4, 4);
    }

    private final String SHADERS_SOURCE = "query-occlusion";
    private final String SHADERS_ROOT = "src/data/gl_440";

    private int vertexCount = 6;
    private int positionSize = vertexCount * Vec2.SIZE;
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
        public static final int QUERY = 2;
        public static final int MAX = 3;
    }

    private int[] bufferName = new int[Buffer.MAX], vertexArrayName = {0}, pipelineName = {0}, queryName = new int[4];
    private Vec4[] viewports = new Vec4[4];
    private int programName;

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;
        validated = validated && checkExtension(gl4, "GL_ARB_query_buffer_object");

        int[] queryCounter = {0};
        gl4.glGetQueryiv(GL_ANY_SAMPLES_PASSED_CONSERVATIVE, GL_QUERY_COUNTER_BITS, queryCounter, 0);
        assert (queryCounter[0] > 0);

        viewports[0] = new Vec4(windowSize.x * -0.5f, windowSize.y * -0.5f, windowSize.x * 0.5f, windowSize.y * 0.5f);
        viewports[1] = new Vec4(0, 0, windowSize.x * 0.5f, windowSize.y * 0.5f);
        viewports[2] = new Vec4(windowSize.x * 0.5f, windowSize.y * 0.5f, windowSize.x * 0.5f, windowSize.y * 0.5f);
        viewports[3] = new Vec4(windowSize.x * 1.0f, windowSize.y * 1.0f, windowSize.x * 0.5f, windowSize.y * 0.5f);

        if (validated) {
            validated = initProgram(gl4);
        }
        if (validated) {
            validated = initBuffer(gl4);
        }
        if (validated) {
            validated = initVertexArray(gl4);
        }
        if (validated) {
            validated = initQuery(gl4);
        }

        return validated && checkError(gl4, "begin");
    }

    private boolean initQuery(GL4 gl4) {

        gl4.glGenQueries(queryName.length, queryName, 0);

        return checkError(gl4, "initQuery");
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        if (validated) {

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

            ShaderProgram shaderProgram = new ShaderProgram();
            shaderProgram.init(gl4);

            programName = shaderProgram.program();

            gl4.glProgramParameteri(programName, GL_PROGRAM_SEPARABLE, GL_TRUE);

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);
            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            gl4.glGenProgramPipelines(1, pipelineName, 0);
            gl4.glUseProgramStages(pipelineName[0], GL_VERTEX_SHADER_BIT | GL_FRAGMENT_SHADER_BIT, programName);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        gl4.glGenBuffers(Buffer.MAX, bufferName, 0);

        gl4.glBindBuffer(GL_SHADER_STORAGE_BUFFER, bufferName[Buffer.VERTEX]);
        FloatBuffer positionBuffer = GLBuffers.newDirectFloatBuffer(positionData);
        gl4.glBufferData(GL_SHADER_STORAGE_BUFFER, positionSize, positionBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(positionBuffer);
        gl4.glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);

        int[] uniformBufferOffset = {0};
        gl4.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset, 0);
        int uniformBlockSize = Math.max(Mat4.SIZE, uniformBufferOffset[0]);

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
        gl4.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_DYNAMIC_DRAW);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        gl4.glBindBuffer(GL_QUERY_BUFFER, bufferName[Buffer.QUERY]);
        gl4.glBufferData(GL_QUERY_BUFFER, Integer.BYTES * queryName.length, null, GL_DYNAMIC_COPY);
        gl4.glBindBuffer(GL_QUERY_BUFFER, 0);

        return checkError(gl4, "initBuffer");
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(1, vertexArrayName, 0);
        gl4.glBindVertexArray(vertexArrayName[0]);
        gl4.glBindVertexArray(0);

        return checkError(gl4, "initVertexArray");
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        {
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
            ByteBuffer pointer = gl4.glMapBufferRange(GL_UNIFORM_BUFFER,
                    0, Mat4.SIZE, GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, (float) windowSize.x / windowSize.y, 0.1f, 100.0f);
            Mat4 model = new Mat4(1.0f);

            pointer.asFloatBuffer().put(projection.mul(viewMat4()).mul(model).toFA_());

            gl4.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        // Clear color buffer with black
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{0.0f, 0.0f, 0.0f, 1.0f}, 0);

        gl4.glBindProgramPipeline(pipelineName[0]);
        gl4.glBindVertexArray(vertexArrayName[0]);
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.TRANSFORM]);
        gl4.glBindBufferBase(GL_SHADER_STORAGE_BUFFER, Semantic.Storage.VERTEX, bufferName[Buffer.VERTEX]);

        // Samples count query
        for (int i = 0; i < viewports.length; ++i) {
            gl4.glViewportArrayv(0, 1, viewports[i].toFA_(), 0);

            gl4.glBeginQuery(GL_ANY_SAMPLES_PASSED, queryName[i]);
            {
                gl4.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 1);
            }
            gl4.glEndQuery(GL_ANY_SAMPLES_PASSED);
        }

        gl4.glBindBuffer(GL_QUERY_BUFFER, bufferName[Buffer.QUERY]);
        for (int i = 0; i < viewports.length; ++i) {
            int[] params = {Integer.BYTES * i};
            gl4.glGetQueryObjectuiv(queryName[i], GL_QUERY_RESULT, params, 0);
        }

        ByteBuffer pointer = gl4.glMapBufferRange(GL_QUERY_BUFFER, 0, Integer.BYTES * queryName.length, GL_MAP_READ_BIT);

        System.out.println("Samples count: " + pointer.get(0) + ", " + pointer.get(1) + ", " + pointer.get(2)
                + ", " + pointer.get(3) + "\r");

        gl4.glUnmapBuffer(GL_QUERY_BUFFER);
        gl4.glBindBuffer(GL_QUERY_BUFFER, 0);

        return true;
    }
}
