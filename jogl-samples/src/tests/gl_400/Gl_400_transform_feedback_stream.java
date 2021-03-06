/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_400;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL3ES3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.BufferUtils;
import glm.glm;
import glm.mat._4.Mat4;
import framework.Profile;
import framework.Test;
import glm.vec._4.Vec4;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import test.Semantic;

/**
 *
 * @author GBarbieri
 */
public class Gl_400_transform_feedback_stream extends Test {

    public static void main(String[] args) {
        Gl_400_transform_feedback_stream gl_400_transform_feedback_stream = new Gl_400_transform_feedback_stream();
    }

    public Gl_400_transform_feedback_stream() {
        super("gl-400-transform-feedback-stream", Profile.CORE, 4, 0);
    }

    private final String SHADERS_SOURCE_TRANSFORM = "transform-stream";
    private final String SHADERS_SOURCE_FEEDBACK = "feedback-stream";
    private final String SHADERS_ROOT = "src/data/gl_400";

    private int vertexCount = 4;
    private int vertexSize = vertexCount * Vec4.SIZE;
    private float[] vertexData = {
        -1.0f, -1.0f, 0.0f, 1.0f,
        +1.0f, -1.0f, 0.0f, 1.0f,
        +1.0f, +1.0f, 0.0f, 1.0f,
        -1.0f, +1.0f, 0.0f, 1.0f};

    private int elementCount = 6;
    private int elementSize = elementCount * Integer.BYTES;
    private int[] elementData = {
        0, 1, 2,
        2, 3, 0};

    private class Buffer {

        public static final int TRANSFORM0 = 0;
        public static final int FEEDBACK = 1;
        public static final int TRANSFORM_ARRAY = 2;
        public static final int TRANSFORM_ELEMENT = 3;
        public static final int MAX = 4;
    }

    private class VertexArray {

        public static final int FEEDBACK = 0;
        public static final int TRANSFORM_FEEDBACK = 1;
        public static final int MAX = 2;
    }

    private IntBuffer feedbackName = GLBuffers.newDirectIntBuffer(1),
            vertexArrayName = GLBuffers.newDirectIntBuffer(VertexArray.MAX),
            bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX);
    private int transformProgramName, transformUniformMvp, feedbackProgramName;

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;

        if (validated) {
            validated = initProgram(gl4);
        }
        if (validated) {
            validated = initArrayBuffer(gl4);
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

            ShaderCode vertexShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER, this.getClass(), SHADERS_ROOT,
                    null, SHADERS_SOURCE_TRANSFORM, "vert", null, true);
            ShaderCode geometryShaderCode = ShaderCode.create(gl4, GL_GEOMETRY_SHADER, this.getClass(), SHADERS_ROOT,
                    null, SHADERS_SOURCE_TRANSFORM, "geom", null, true);

            shaderProgram.init(gl4);

            shaderProgram.add(vertexShaderCode);
            shaderProgram.add(geometryShaderCode);

            transformProgramName = shaderProgram.program();

            String[] strings = {"gl_Position", "Block.color"};
            gl4.glTransformFeedbackVaryings(transformProgramName, 2, strings, GL_INTERLEAVED_ATTRIBS);

            shaderProgram.link(gl4, System.out);
        }

        // Create program
        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            ShaderCode vertexShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER, this.getClass(), SHADERS_ROOT,
                    null, SHADERS_SOURCE_FEEDBACK, "vert", null, true);
            ShaderCode fragmentShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER, this.getClass(), SHADERS_ROOT,
                    null, SHADERS_SOURCE_FEEDBACK, "frag", null, true);

            shaderProgram.init(gl4);

            shaderProgram.add(vertexShaderCode);
            shaderProgram.add(fragmentShaderCode);

            feedbackProgramName = shaderProgram.program();

            shaderProgram.link(gl4, System.out);

        }

        // Get variables locations
        if (validated) {

            transformUniformMvp = gl4.glGetUniformLocation(transformProgramName, "mvp");
            validated = validated && transformUniformMvp >= 0;
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initVertexArray(GL4 gl4) {

        checkError(gl4, "initVertexArray 0");

        // Build a vertex array object
        gl4.glGenVertexArrays(VertexArray.MAX, vertexArrayName);

        gl4.glBindVertexArray(vertexArrayName.get(VertexArray.TRANSFORM_FEEDBACK));
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.TRANSFORM_ARRAY));
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 4, GL_FLOAT, false, 0, 0);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
        }
        gl4.glBindVertexArray(0);

        checkError(gl4, "initVertexArray 1");

        // Build a vertex array object
        gl4.glBindVertexArray(vertexArrayName.get(VertexArray.FEEDBACK));
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.FEEDBACK));
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 4, GL_FLOAT, false, 2 * Vec4.SIZE, 0);
            gl4.glVertexAttribPointer(Semantic.Attr.COLOR, 4, GL_FLOAT, false, 2 * Vec4.SIZE, Vec4.SIZE);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl4.glEnableVertexAttribArray(Semantic.Attr.COLOR);
        }
        gl4.glBindVertexArray(0);

        return checkError(gl4, "initVertexArray");
    }

    private boolean initFeedback(GL4 gl4) {

        // Generate a buffer object
        gl4.glGenTransformFeedbacks(1, feedbackName);
        gl4.glBindTransformFeedback(GL_TRANSFORM_FEEDBACK, feedbackName.get(0));
        gl4.glBindBufferBase(GL_TRANSFORM_FEEDBACK_BUFFER, 0, bufferName.get(Buffer.FEEDBACK));
        gl4.glBindTransformFeedback(GL_TRANSFORM_FEEDBACK, 0);

        return checkError(gl4, "initFeedback");
    }

    private boolean initArrayBuffer(GL4 gl4) {

        IntBuffer elementBuffer = GLBuffers.newDirectIntBuffer(elementData);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);

        gl4.glGenBuffers(Buffer.MAX, bufferName);

        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.TRANSFORM_ELEMENT));
        gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.TRANSFORM_ARRAY));
        gl4.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.FEEDBACK));
        gl4.glBufferData(GL_ARRAY_BUFFER, 2 * Vec4.SIZE * elementCount, null, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        BufferUtils.destroyDirectBuffer(elementBuffer);
        BufferUtils.destroyDirectBuffer(vertexBuffer);

        return checkError(gl4, "initArrayBuffer");
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        // Compute the MVP (Model View Projection matrix)
        Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, (float) windowSize.x / windowSize.y, 0.1f, 100.0f);
        Mat4 model = new Mat4(1.0f);
        Mat4 mvp = projection.mul(viewMat4()).mul(model);

        // Set the display viewport
        gl4.glViewport(0, 0, windowSize.x, windowSize.y);

        // Clear color buffer
        gl4.glClearBufferfv(GL_COLOR, 0, clearColor.put(0, 0).put(1, 0).put(2, 0).put(3, 1));

        // First draw, capture the attributes
        // Disable rasterisation, vertices processing only!
        gl4.glEnable(GL_RASTERIZER_DISCARD);

        gl4.glUseProgram(transformProgramName);
        gl4.glUniformMatrix4fv(transformUniformMvp, 1, false, mvp.toFa_(), 0);

        gl4.glBindVertexArray(vertexArrayName.get(VertexArray.TRANSFORM_FEEDBACK));
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.TRANSFORM_ELEMENT));

        gl4.glBindTransformFeedback(GL_TRANSFORM_FEEDBACK, feedbackName.get(0));
        gl4.glBeginTransformFeedback(GL_TRIANGLES);
        {
            gl4.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_INT, 0, 1, 0);
        }
        gl4.glEndTransformFeedback();
        gl4.glBindTransformFeedback(GL_TRANSFORM_FEEDBACK, 0);

        gl4.glDisable(GL_RASTERIZER_DISCARD);

        // Second draw, reuse the captured attributes
        gl4.glUseProgram(feedbackProgramName);

        gl4.glBindVertexArray(vertexArrayName.get(VertexArray.FEEDBACK));
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        gl4.glDrawTransformFeedbackStream(GL_TRIANGLES, feedbackName.get(0), 0);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteVertexArrays(VertexArray.MAX, vertexArrayName);
        gl4.glDeleteBuffers(Buffer.MAX, bufferName);
        gl4.glDeleteProgram(transformProgramName);
        gl4.glDeleteProgram(feedbackProgramName);

        gl4.glDeleteTransformFeedbacks(1, feedbackName);

        BufferUtils.destroyDirectBuffer(vertexArrayName);
        BufferUtils.destroyDirectBuffer(bufferName);
        BufferUtils.destroyDirectBuffer(feedbackName);

        return checkError(gl4, "end");
    }
}
