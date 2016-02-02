/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_400;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2ES3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.Profile;
import framework.Test;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import test.Semantic;

/**
 *
 * @author GBarbieri
 */
public class Gl_400_transform_feedback_object extends Test {

    public static void main(String[] args) {
        Gl_400_transform_feedback_object gl_400_transform_feedback_object = new Gl_400_transform_feedback_object();
    }

    public Gl_400_transform_feedback_object() {
        super("gl-400-transform-feedback-object", Profile.CORE, 4, 0);
    }

    private final String SHADERS_SOURCE_TRANSFORM = "transform";
    private final String SHADERS_SOURCE_FEEDBACK = "feedback";
    private final String SHADERS_ROOT = "src/data/gl_400";

    private int vertexCount = 6;
    private int positionSize = vertexCount * 4 * Float.BYTES;
    private float[] positionData = {
        -1.0f, -1.0f, 0.0f, 1.0f,
        +1.0f, -1.0f, 0.0f, 1.0f,
        +1.0f, +1.0f, 0.0f, 1.0f,
        +1.0f, +1.0f, 0.0f, 1.0f,
        -1.0f, +1.0f, 0.0f, 1.0f,
        -1.0f, -1.0f, 0.0f, 1.0f};

    private class Buffer {

        public static final int TRANSFORM = 0;
        public static final int MAX = 1;
    }

    private int[] feedbackName = {0}, transformArrayBufferName = {0}, transformVertexArrayName = {0},
            feedbackArrayBufferName = {0}, feedbackVertexArrayName = {0}, bufferName = new int[Buffer.MAX];
    private int transformProgramName, feedbackProgramName;
    private float[] projection = new float[16], model = new float[16];

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
        if (validated) {
            validated = initFeedback(gl4);
        }

        return validated && checkError(gl4, "begin");
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        // Create program
        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            ShaderCode vertexShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_TRANSFORM, "vert", null, true);

            shaderProgram.init(gl4);

            shaderProgram.add(vertexShaderCode);

            transformProgramName = shaderProgram.program();

            String[] strings = {"gl_Position", "Block.color"};
            gl4.glTransformFeedbackVaryings(transformProgramName, 2, strings, GL_INTERLEAVED_ATTRIBS);

            shaderProgram.link(gl4, System.out);
        }

        // Create program
        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            ShaderCode vertexShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_FEEDBACK, "vert", null, true);
            ShaderCode fragmentShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_FEEDBACK, "frag", null, true);

            shaderProgram.init(gl4);

            shaderProgram.add(vertexShaderCode);
            shaderProgram.add(fragmentShaderCode);

            feedbackProgramName = shaderProgram.program();

            shaderProgram.link(gl4, System.out);

        }

        // Get variables locations
        if (validated) {

            gl4.glUniformBlockBinding(transformProgramName,
                    gl4.glGetUniformBlockIndex(transformProgramName, "Transform"), Semantic.Uniform.TRANSFORM0);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initVertexArray(GL4 gl4) {

        checkError(gl4, "initVertexArray 0");

        // Build a vertex array object
        gl4.glGenVertexArrays(1, transformVertexArrayName, 0);
        gl4.glBindVertexArray(transformVertexArrayName[0]);
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, transformArrayBufferName[0]);
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 4, GL_FLOAT, false, 0, 0);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
        }
        gl4.glBindVertexArray(0);

        checkError(gl4, "initVertexArray 1");

        // Build a vertex array object
        gl4.glGenVertexArrays(1, feedbackVertexArrayName, 0);
        gl4.glBindVertexArray(feedbackVertexArrayName[0]);
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, feedbackArrayBufferName[0]);
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 4, GL_FLOAT, false, 2 * 4 * Float.BYTES, 0);
            gl4.glVertexAttribPointer(Semantic.Attr.COLOR, 4, GL_FLOAT, false, 2 * 4 * Float.BYTES, 4 * Float.BYTES);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl4.glEnableVertexAttribArray(Semantic.Attr.COLOR);
        }
        gl4.glBindVertexArray(0);

        return checkError(gl4, "initVertexArray");
    }

    private boolean initFeedback(GL4 gl4) {

        // Generate a buffer object
        gl4.glGenTransformFeedbacks(1, feedbackName, 0);
        gl4.glBindTransformFeedback(GL_TRANSFORM_FEEDBACK, feedbackName[0]);
        gl4.glBindBufferBase(GL_TRANSFORM_FEEDBACK_BUFFER, 0, feedbackArrayBufferName[0]);
        gl4.glBindTransformFeedback(GL_TRANSFORM_FEEDBACK, 0);

        return checkError(gl4, "initFeedback");
    }

    private boolean initBuffer(GL4 gl4) {

        gl4.glGenBuffers(Buffer.MAX, bufferName, 0);

        int[] uniformBufferOffset = {0};
        gl4.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset, 0);
        int uniformBlockSize = Math.max(16 * Float.BYTES, uniformBufferOffset[0]);

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
        gl4.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_DYNAMIC_DRAW);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        gl4.glGenBuffers(1, transformArrayBufferName, 0);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, transformArrayBufferName[0]);
        FloatBuffer positionBuffer = GLBuffers.newDirectFloatBuffer(positionData);
        gl4.glBufferData(GL_ARRAY_BUFFER, positionSize, positionBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        gl4.glGenBuffers(1, feedbackArrayBufferName, 0);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, feedbackArrayBufferName[0]);
        gl4.glBufferData(GL_ARRAY_BUFFER, 2 * 4 * Float.BYTES * vertexCount, null, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        return checkError(gl4, "initArrayBuffer");
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        // Compute the MVP (Model View Projection matrix)
        {
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
            ByteBuffer pointer = gl4.glMapBufferRange(GL_UNIFORM_BUFFER, 0,
                    16 * Float.BYTES, GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            FloatUtil.makePerspective(projection, 0, true, (float) Math.PI * 0.25f,
                    (float) windowSize.x / windowSize.y, 0.1f, 100.0f);
            FloatUtil.makeIdentity(model);

            FloatUtil.multMatrix(projection, view());
            FloatUtil.multMatrix(projection, model);

            for (float f : projection) {
                pointer.putFloat(f);
            }
            pointer.rewind();

            gl4.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        // Set the display viewport
        gl4.glViewport(0, 0, windowSize.x, windowSize.y);

        // Clear color buffer
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{0.0f, 0.0f, 0.0f, 1.0f}, 0);

        // First draw, capture the attributes
        // Disable rasterisation, vertices processing only!
        gl4.glEnable(GL_RASTERIZER_DISCARD);

        gl4.glUseProgram(transformProgramName);

        gl4.glBindVertexArray(transformVertexArrayName[0]);
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.TRANSFORM]);

        gl4.glBindTransformFeedback(GL_TRANSFORM_FEEDBACK, feedbackName[0]);
        gl4.glBeginTransformFeedback(GL_TRIANGLES);
        gl4.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 1);
        gl4.glEndTransformFeedback();
        gl4.glBindTransformFeedback(GL_TRANSFORM_FEEDBACK, 0);

        gl4.glDisable(GL_RASTERIZER_DISCARD);

        // Second draw, reuse the captured attributes
        gl4.glUseProgram(feedbackProgramName);

        gl4.glBindVertexArray(feedbackVertexArrayName[0]);
        gl4.glDrawTransformFeedback(GL_TRIANGLES, feedbackName[0]);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteVertexArrays(1, transformVertexArrayName, 0);
        gl4.glDeleteBuffers(1, transformArrayBufferName, 0);
        gl4.glDeleteProgram(transformProgramName);

        gl4.glDeleteVertexArrays(1, feedbackVertexArrayName, 0);
        gl4.glDeleteBuffers(1, feedbackArrayBufferName, 0);
        gl4.glDeleteProgram(feedbackProgramName);

        gl4.glDeleteTransformFeedbacks(1, feedbackName, 0);

        return checkError(gl4, "end");
    }
}
