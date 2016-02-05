/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_430;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2ES3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import core.glm;
import dev.Mat4;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 *
 * @author elect
 */
public class Gl_430_query_occlusion extends Test {

    public static void main(String[] args) {
        Gl_430_query_occlusion gl_430_query_occlusion = new Gl_430_query_occlusion();
    }

    public Gl_430_query_occlusion() {
        super("gl-430-query-occlusion", Profile.CORE, 4, 3);
    }

    private final String SHADERS_SOURCE = "query-occlusion";
    private final String SHADERS_ROOT = "src/data/gl_430";

    private int vertexCount = 6;
    private int positionSize = vertexCount * 2 * Float.BYTES;
    private float[] positionData = {
        -1.0f, -1.0f,
        +1.0f, -1.0f,
        +1.0f, 1.0f,
        +1.0f, 1.0f,
        -1.0f, 1.0f,
        -1.0f, -1.0f};

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int TRANSFORM = 1;
        public static final int MAX = 2;
    }

    private int[] vertexArrayName = {0}, pipelineName = {0}, queryName = {0}, bufferName = new int[Buffer.MAX];
    private int programName;

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;
        validated = validated && checkExtension(gl4, "GL_ARB_ES3_compatibility");

        if (validated) {
            validated = initQuery(gl4);
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
        if (validated) {
            validated = initQuery(gl4);
        }

        return validated;
    }

    private boolean initQuery(GL4 gl4) {

        gl4.glGenQueries(1, queryName, 0);

        int[] queryBits = {0};
        gl4.glGetQueryiv(GL_ANY_SAMPLES_PASSED_CONSERVATIVE, GL_QUERY_COUNTER_BITS, queryBits, 0);

        boolean validated = queryBits[0] >= 1;

        return validated;
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        // Create shaders
        if (validated) {

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

            if (validated) {

                ShaderProgram shaderProgram = new ShaderProgram();
                shaderProgram.init(gl4);

                programName = shaderProgram.program();

                gl4.glProgramParameteri(programName, GL_PROGRAM_SEPARABLE, GL_TRUE);

                shaderProgram.add(vertShaderCode);
                shaderProgram.add(fragShaderCode);

                shaderProgram.link(gl4, System.out);
            }
        }

        if (validated) {

            gl4.glGenProgramPipelines(1, pipelineName, 0);
            gl4.glUseProgramStages(pipelineName[0], GL_VERTEX_SHADER_BIT | GL_FRAGMENT_SHADER_BIT, programName);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        gl4.glGenBuffers(Buffer.MAX, bufferName, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
        FloatBuffer positionBuffer = GLBuffers.newDirectFloatBuffer(positionData);
        gl4.glBufferData(GL_ARRAY_BUFFER, positionSize, positionBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        int[] uniformBufferOffset = {0};
        gl4.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset, 0);
        int uniformBlockSize = Math.max(Mat4.SIZE, uniformBufferOffset[0]);

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
        gl4.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_DYNAMIC_DRAW);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return true;
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(1, vertexArrayName, 0);
        gl4.glBindVertexArray(vertexArrayName[0]);
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 0, 0);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
        }
        gl4.glBindVertexArray(0);

        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        {
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
            ByteBuffer pointer = gl4.glMapBufferRange(GL_UNIFORM_BUFFER, 0, Mat4.SIZE,
                    GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, (float) windowSize.x / windowSize.y, 0.1f, 100.0f);
            Mat4 model = new Mat4(1.0f);

            pointer.asFloatBuffer().put(projection.mul(viewMat4()).mul(model).toFa_());

            gl4.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        // Set the display viewport
        gl4.glViewport(0, 0, windowSize.x, windowSize.y);

        // Clear color buffer with black
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{0.0f, 0.0f, 0.0f, 1.0f}, 0);

        gl4.glBindProgramPipeline(pipelineName[0]);
        gl4.glBindVertexArray(vertexArrayName[0]);
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.TRANSFORM]);

        // Samples count query
        gl4.glBeginQuery(GL_ANY_SAMPLES_PASSED_CONSERVATIVE, queryName[0]);
        {
            gl4.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 1);
        }
        gl4.glEndQuery(GL_ANY_SAMPLES_PASSED_CONSERVATIVE);

        // Get the count of samples. 
        // If the result of the query isn't here yet, we wait here...
        int[] samplesCount = {0};
        gl4.glGetQueryObjectuiv(queryName[0], GL_QUERY_RESULT, samplesCount, 0);
        System.out.println("Samples count: " + samplesCount[0]);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteProgramPipelines(1, pipelineName, 0);
        gl4.glDeleteBuffers(Buffer.MAX, bufferName, 0);
        gl4.glDeleteProgram(programName);
        gl4.glDeleteVertexArrays(1, vertexArrayName, 0);

        return true;
    }
}
