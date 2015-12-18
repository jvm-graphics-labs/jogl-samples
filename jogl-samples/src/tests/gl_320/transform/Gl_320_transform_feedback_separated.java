/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_320.transform;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL2ES2.GL_FLOAT_VEC4;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_QUERY_COUNTER_BITS;
import static com.jogamp.opengl.GL2ES2.GL_QUERY_RESULT;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES3.GL_RASTERIZER_DISCARD;
import static com.jogamp.opengl.GL2ES3.GL_SEPARATE_ATTRIBS;
import static com.jogamp.opengl.GL2ES3.GL_TRANSFORM_FEEDBACK_BUFFER;
import static com.jogamp.opengl.GL2ES3.GL_TRANSFORM_FEEDBACK_PRIMITIVES_WRITTEN;
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
public class Gl_320_transform_feedback_separated extends Test {

    public static void main(String[] args) {
        Gl_320_transform_feedback_separated gl_320_transform_feedback_separated
                = new Gl_320_transform_feedback_separated();
    }

    public Gl_320_transform_feedback_separated() {
        super("gl-320-transform-feedback-separated", Profile.CORE, 3, 2);
    }

    private final String SHADERS_SOURCE_TRANSFORM = "transform-feedback-transform";
    private final String SHADERS_SOURCE_FEEDBACK = "transform-feedback-feedback";
    private final String SHADERS_ROOT = "src/data/gl_320/transform";

    private int vertexCount = 6;
    private int positionSize = vertexCount * 4 * Float.BYTES;
    private float[] positionData = {
        -1.0f, -1.0f, 0.0f, 1.0f,
        +1.0f, -1.0f, 0.0f, 1.0f,
        +1.0f, +1.0f, 0.0f, 1.0f,
        +1.0f, +1.0f, 0.0f, 1.0f,
        -1.0f, +1.0f, 0.0f, 1.0f,
        -1.0f, -1.0f, 0.0f, 1.0f};

    private enum Program {
        TRANSFORM,
        FEEDBACK,
        MAX
    }

    private enum Buffer {
        VERTEX,
        POSITION,
        COLOR,
        MAX
    }

    private enum Shader {
        VERT_TRANSFORM,
        VERT_FEEDBACK,
        FRAG_FEEDBACK,
        MAX
    }

    private int[] bufferName = new int[Buffer.MAX.ordinal()], vertexArrayName = new int[Program.MAX.ordinal()],
            programName = new int[Program.MAX.ordinal()], queryName = {0};
    private int uniformMvp;
    private float[] projection = new float[16], model = new float[16], mvp = new float[16];

    @Override
    protected boolean begin(GL gl) {

        GL3 gl3 = (GL3) gl;

        boolean validated = true;

        if (validated) {
            validated = initQuery(gl3);
        }
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

        ShaderCode[] shaderCodes = new ShaderCode[Shader.MAX.ordinal()];

        shaderCodes[Shader.VERT_TRANSFORM.ordinal()] = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_TRANSFORM, "vert", null, true);
        shaderCodes[Shader.VERT_FEEDBACK.ordinal()] = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_FEEDBACK, "vert", null, true);
        shaderCodes[Shader.FRAG_FEEDBACK.ordinal()] = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_FEEDBACK, "frag", null, true);

        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            shaderProgram.add(shaderCodes[Shader.VERT_TRANSFORM.ordinal()]);

            shaderProgram.init(gl3);

            programName[Program.TRANSFORM.ordinal()] = shaderProgram.program();

            gl3.glBindAttribLocation(programName[Program.TRANSFORM.ordinal()], Semantic.Attr.POSITION, "position");

            String[] strings = {"gl_Position", "Block.color"};
            gl3.glTransformFeedbackVaryings(programName[Program.TRANSFORM.ordinal()], 2, strings, GL_SEPARATE_ATTRIBS);

            shaderProgram.link(gl3, System.out);

            byte[] name = new byte[64];
            int[] length = {0};
            int[] size = {0};
            int[] type = {0};

            gl3.glGetTransformFeedbackVarying(
                    programName[Program.TRANSFORM.ordinal()],
                    0,
                    name.length,
                    length, 0,
                    size, 0,
                    type, 0,
                    name, 0);

            validated = validated && (size[0] == 1) && (type[0] == GL_FLOAT_VEC4);
        }
        // Get variables locations
        if (validated) {

            uniformMvp = gl3.glGetUniformLocation(programName[Program.TRANSFORM.ordinal()], "mvp");
            validated = validated && (uniformMvp >= 0);
        }

        // Create program
        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            shaderProgram.add(shaderCodes[Shader.VERT_FEEDBACK.ordinal()]);
            shaderProgram.add(shaderCodes[Shader.FRAG_FEEDBACK.ordinal()]);

            shaderProgram.init(gl3);

            programName[Program.FEEDBACK.ordinal()] = shaderProgram.program();

            gl3.glBindAttribLocation(programName[Program.FEEDBACK.ordinal()], Semantic.Attr.POSITION, "position");
            gl3.glBindAttribLocation(programName[Program.FEEDBACK.ordinal()], Semantic.Attr.COLOR, "color");
            gl3.glBindFragDataLocation(programName[Program.FEEDBACK.ordinal()], Semantic.Frag.COLOR, "color");

            shaderProgram.link(gl3, System.out);
        }

        return validated & checkError(gl3, "initProgram");
    }

    private boolean initVertexArray(GL3 gl3) {

        gl3.glGenVertexArrays(Program.MAX.ordinal(), vertexArrayName, 0);

        // Build a vertex array object
        gl3.glBindVertexArray(vertexArrayName[Program.TRANSFORM.ordinal()]);
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 4, GL_FLOAT, false, 0, 0);
            gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);
        }
        gl3.glBindVertexArray(0);

        // Build a vertex array object
        gl3.glBindVertexArray(vertexArrayName[Program.FEEDBACK.ordinal()]);
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.POSITION.ordinal()]);
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 4, GL_FLOAT, false, 0, 0);
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.COLOR.ordinal()]);
            gl3.glVertexAttribPointer(Semantic.Attr.COLOR, 4, GL_FLOAT, false, 0, 0);
            gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl3.glEnableVertexAttribArray(Semantic.Attr.COLOR);
        }
        gl3.glBindVertexArray(0);

        return checkError(gl3, "initVertexArray");
    }

    private boolean initBuffer(GL3 gl3) {

        gl3.glGenBuffers(Buffer.MAX.ordinal(), bufferName, 0);

        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);
        FloatBuffer positionBuffer = GLBuffers.newDirectFloatBuffer(positionData);
        gl3.glBufferData(GL_ARRAY_BUFFER, positionSize, positionBuffer, GL_STATIC_DRAW);

        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.POSITION.ordinal()]);
        gl3.glBufferData(GL_ARRAY_BUFFER, 4 * Float.BYTES * vertexCount, null, GL_STATIC_DRAW);

        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.COLOR.ordinal()]);
        gl3.glBufferData(GL_ARRAY_BUFFER, 4 * Float.BYTES * vertexCount, null, GL_STATIC_DRAW);

        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        return checkError(gl3, "initBuffer");
    }

    private boolean initQuery(GL3 gl3) {

        gl3.glGenQueries(1, queryName, 0);

        int[] queryBits = {0};
        gl3.glGetQueryiv(GL_TRANSFORM_FEEDBACK_PRIMITIVES_WRITTEN, GL_QUERY_COUNTER_BITS, queryBits, 0);

        boolean validated = queryBits[0] >= 1;

        return validated && checkError(gl3, "initQuery");
    }

    @Override
    protected boolean render(GL gl) {

        GL3 gl3 = (GL3) gl;

        FloatUtil.makePerspective(projection, 0, true, (float) Math.PI * 0.25f, 4.0f / 3.0f, 0.1f, 100.0f);
        FloatUtil.makeIdentity(model);
        FloatUtil.multMatrix(projection, view(), mvp);
        FloatUtil.multMatrix(mvp, model);

        // Set the display viewport
        gl3.glViewport(0, 0, windowSize.x, windowSize.y);

        // Clear color buffer with black
        gl3.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        gl3.glClear(GL_COLOR_BUFFER_BIT);

        // First draw, capture the attributes
        {
            // Disable rasterisation, vertices processing only!
            gl3.glEnable(GL_RASTERIZER_DISCARD);

            gl3.glUseProgram(programName[Program.TRANSFORM.ordinal()]);
            gl3.glUniformMatrix4fv(uniformMvp, 1, false, mvp, 0);

            gl3.glBindBufferBase(GL_TRANSFORM_FEEDBACK_BUFFER, 0, bufferName[Buffer.POSITION.ordinal()]);
            gl3.glBindBufferBase(GL_TRANSFORM_FEEDBACK_BUFFER, 1, bufferName[Buffer.COLOR.ordinal()]);

            gl3.glBindVertexArray(vertexArrayName[Program.TRANSFORM.ordinal()]);

            gl3.glBeginQuery(GL_TRANSFORM_FEEDBACK_PRIMITIVES_WRITTEN, queryName[0]);
            gl3.glBeginTransformFeedback(GL_TRIANGLES);
            {
                gl3.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 1);
            }
            gl3.glEndTransformFeedback();
            gl3.glEndQuery(GL_TRANSFORM_FEEDBACK_PRIMITIVES_WRITTEN);

            gl3.glDisable(GL_RASTERIZER_DISCARD);
        }

        // Second draw, reuse the captured attributes
        {
            gl3.glUseProgram(programName[Program.FEEDBACK.ordinal()]);

            int[] primitivesWritten = {0};
            gl3.glGetQueryObjectuiv(queryName[0], GL_QUERY_RESULT, primitivesWritten, 0);

            gl3.glBindVertexArray(vertexArrayName[Program.FEEDBACK.ordinal()]);
            gl3.glDrawArraysInstanced(GL_TRIANGLES, 0, primitivesWritten[0] * 3, 1);
        }

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;

        for (int i = 0; i < Program.MAX.ordinal(); ++i) {
            gl3.glDeleteProgram(programName[i]);
        }
        gl3.glDeleteVertexArrays(Program.MAX.ordinal(), vertexArrayName, 0);
        gl3.glDeleteBuffers(Program.MAX.ordinal(), bufferName, 0);
        gl3.glDeleteQueries(1, queryName, 0);

        return checkError(gl3, "end");
    }
}
