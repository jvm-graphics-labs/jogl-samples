/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_330;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2ES3.*;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.nio.FloatBuffer;

/**
 *
 * @author GBarbieri
 */
public class Gl_330_query_time extends Test {

    public static void main(String[] args) {
        Gl_330_query_time gl_330_query_time = new Gl_330_query_time();
    }

    public Gl_330_query_time() {
        super("gl-330-query-time", Profile.CORE, 3, 3);
    }

    private final String SHADERS_SOURCE = "flat-color";
    private final String SHADERS_ROOT = "src/data/gl_330";

    private int vertexCount = 6;
    private int positionSize = vertexCount * 2 * Float.BYTES;
    private float[] positionData = {
        -1.0f, -1.0f,
        +1.0f, -1.0f,
        +1.0f, +1.0f,
        +1.0f, +1.0f,
        -1.0f, +1.0f,
        -1.0f, -1.0f};

    private int[] vertexArrayName = {0}, bufferName = {0}, queryName = {0};
    private int programName, uniformMvp, uniformColor;
    private float[] projection = new float[16], model = new float[16], mvp = new float[16];

    @Override
    protected boolean begin(GL gl) {

        GL3 gl3 = (GL3) gl;

        boolean validated = true;

        if (validated) {
            validated = initProgram(gl3);
        }
        if (validated) {
            validated = initArrayBuffer(gl3);
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
        gl3.glGetQueryiv(GL_TIME_ELAPSED, GL_QUERY_COUNTER_BITS, queryBits, 0);

        boolean validated = queryBits[0] >= 30;

        return validated && checkError(gl3, "initQuery");
    }

    private boolean initProgram(GL3 gl3) {

        boolean validated = true;

        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            ShaderCode vertShaderCode = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

            shaderProgram.init(gl3);

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);

            programName = shaderProgram.program();

            shaderProgram.link(gl3, System.out);
        }

        // Get variables locations
        if (validated) {

            uniformMvp = gl3.glGetUniformLocation(programName, "mvp");
            uniformColor = gl3.glGetUniformLocation(programName, "diffuse");
        }

        return validated & checkError(gl3, "initProgram");
    }

    // Buffer update using glBufferSubData
    private boolean initArrayBuffer(GL3 gl3) {

        // Generate a buffer object
        gl3.glGenBuffers(1, bufferName, 0);

        // Bind the buffer for use
        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[0]);

        // Reserve buffer memory but and copy the values
        FloatBuffer positionBuffer = GLBuffers.newDirectFloatBuffer(positionData);
        gl3.glBufferData(GL_ARRAY_BUFFER, positionSize, positionBuffer, GL_STATIC_DRAW);

        // Unbind the buffer
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        return checkError(gl3, "initArrayBuffer");
    }

    private boolean initVertexArray(GL3 gl3) {

        gl3.glGenVertexArrays(1, vertexArrayName, 0);
        gl3.glBindVertexArray(vertexArrayName[0]);
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[0]);
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

        FloatUtil.makePerspective(projection, 0, true, (float) Math.PI * 0.25f, 4.0f / 3.0f, 0.1f, 100.0f);
        FloatUtil.makeIdentity(model);
        FloatUtil.multMatrix(projection, view(), mvp);
        FloatUtil.multMatrix(mvp, model);

        // Beginning of the time query
        gl3.glBeginQuery(GL_TIME_ELAPSED, queryName[0]);

        // Set the display viewport
        gl3.glViewport(0, 0, windowSize.x, windowSize.y);

        // Clear color buffer with black
        gl3.glClearBufferfv(GL_COLOR, 0, new float[]{0.0f, 0.0f, 0.0f, 1.0f}, 0);

        // Bind program
        gl3.glUseProgram(programName);
        gl3.glUniformMatrix4fv(uniformMvp, 1, false, mvp, 0);
        gl3.glUniform4fv(uniformColor, 1, new float[]{1.0f, 0.5f, 0.0f, 1.0f}, 0);

        gl3.glBindVertexArray(vertexArrayName[0]);
        gl3.glDrawArrays(GL_TRIANGLES, 0, vertexCount);

        // Unbind program
        gl3.glUseProgram(0);

        // End of the time query
        gl3.glEndQuery(GL_TIME_ELAPSED);

        // Get the count of samples. 
        // If the result of the query isn't here yet, we wait here...
        int[] time = {0};
        gl3.glGetQueryObjectuiv(queryName[0], GL_QUERY_RESULT, time, 0);
        System.out.println("Time: " + (time[0] / 1000.f / 1000.f) + " ms");

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;

        gl3.glDeleteBuffers(1, bufferName, 0);
        gl3.glDeleteProgram(programName);
        gl3.glDeleteVertexArrays(1, vertexArrayName, 0);

        return checkError(gl3, "end");
    }
}
