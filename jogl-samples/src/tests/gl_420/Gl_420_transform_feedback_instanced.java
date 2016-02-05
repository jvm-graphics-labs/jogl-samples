/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_420;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL3ES3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.BufferUtils;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import dev.Vec2;

/**
 *
 * @author GBarbieri
 */
public class Gl_420_transform_feedback_instanced extends Test {

    public static void main(String[] args) {
        Gl_420_transform_feedback_instanced gl_420_transform_feedback_instanced = new Gl_420_transform_feedback_instanced();
    }

    public Gl_420_transform_feedback_instanced() {
        super("gl-420-transform-feedback-instanced", Profile.CORE, 4, 2, new Vec2(Math.PI * 0.2f));
    }

    private final String SHADERS_SOURCE_TRANSFORM = "transform-stream";
    private final String SHADERS_SOURCE_FEEDBACK = "feedback-stream";
    private final String SHADERS_ROOT = "src/data/gl_420";

    private int vertexCount = 4;
    private int vertexSize = vertexCount * 4 * Float.BYTES;
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

    private class Pipeline {

        public static final int TRANSFORM = 0;
        public static final int FEEDBACK = 1;
        public static final int MAX = 2;
    }

    private class Buffer {

        public static final int TRANSFORM_ELEMENT = 0;
        public static final int TRANSFORM_VERTEX = 1;
        public static final int FEEDBACK_VERTEX = 2;
        public static final int MAX = 3;
    }

    private int[] feedbackName = {0}, pipelineName = new int[Pipeline.MAX],
            programName = new int[Pipeline.MAX], vertexArrayName = new int[Pipeline.MAX],
            bufferName = new int[Buffer.MAX];
    private int transformUniformMvp, feedbackUniformMvp;
    private float[] projection = new float[16], model = new float[16], mvp = new float[16];

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;

        gl4.glEnable(GL_DEPTH_TEST);

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

        return validated;
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        // Create program
        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_TRANSFORM, "vert", null, true);
            ShaderCode geomShaderCode = ShaderCode.create(gl4, GL_GEOMETRY_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_TRANSFORM, "geom", null, true);

            shaderProgram.init(gl4);
            programName[Pipeline.TRANSFORM] = shaderProgram.program();
            gl4.glProgramParameteri(programName[Pipeline.TRANSFORM], GL_PROGRAM_SEPARABLE, GL_TRUE);
            shaderProgram.add(vertShaderCode);
            shaderProgram.add(geomShaderCode);

            String[] strings = {"gl_Position", "Block.color"};
            gl4.glTransformFeedbackVaryings(programName[Pipeline.TRANSFORM], 2, strings, GL_INTERLEAVED_ATTRIBS);

            shaderProgram.link(gl4, System.out);
        }

        // Get variables locations
        if (validated) {

            transformUniformMvp = gl4.glGetUniformLocation(programName[Pipeline.TRANSFORM], "mvp");
            validated = validated && (transformUniformMvp >= 0);
        }

        // Create program
        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_FEEDBACK, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_FEEDBACK, "frag", null, true);

            shaderProgram.init(gl4);
            programName[Pipeline.FEEDBACK] = shaderProgram.program();
            gl4.glProgramParameteri(programName[Pipeline.FEEDBACK], GL_PROGRAM_SEPARABLE, GL_TRUE);
            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);
            shaderProgram.link(gl4, System.out);
        }

        // Get variables locations
        if (validated) {

            feedbackUniformMvp = gl4.glGetUniformLocation(programName[Pipeline.FEEDBACK], "mvp");
            validated = validated && (feedbackUniformMvp >= 0);
        }

        if (validated) {

            gl4.glGenProgramPipelines(Pipeline.MAX, pipelineName, 0);
            gl4.glUseProgramStages(pipelineName[Pipeline.FEEDBACK], GL_VERTEX_SHADER_BIT
                    | GL_FRAGMENT_SHADER_BIT, programName[Pipeline.FEEDBACK]);
            gl4.glUseProgramStages(pipelineName[Pipeline.TRANSFORM], GL_VERTEX_SHADER_BIT
                    | GL_GEOMETRY_SHADER_BIT, programName[Pipeline.TRANSFORM]);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initVertexArray(GL4 gl4) {

        // Build a vertex array objects
        gl4.glGenVertexArrays(Pipeline.MAX, vertexArrayName, 0);

        gl4.glBindVertexArray(vertexArrayName[Pipeline.TRANSFORM]);
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.TRANSFORM_VERTEX]);
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 4, GL_FLOAT, false, 0, 0);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);

            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.TRANSFORM_ELEMENT]);
        }
        gl4.glBindVertexArray(0);

        gl4.glBindVertexArray(vertexArrayName[Pipeline.FEEDBACK]);
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.FEEDBACK_VERTEX]);
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 4, GL_FLOAT, false, 2 * 4 * Float.BYTES, 0);
            gl4.glVertexAttribPointer(Semantic.Attr.COLOR, 4, GL_FLOAT, false, 2 * 4 * Float.BYTES, 4 * Float.BYTES);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl4.glEnableVertexAttribArray(Semantic.Attr.COLOR);

            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        }
        gl4.glBindVertexArray(0);

        return true;
    }

    private boolean initFeedback(GL4 gl4) {

        // Generate a buffer object
        gl4.glGenTransformFeedbacks(1, feedbackName, 0);
        gl4.glBindTransformFeedback(GL_TRANSFORM_FEEDBACK, feedbackName[0]);
        gl4.glBindBufferBase(GL_TRANSFORM_FEEDBACK_BUFFER, 0, bufferName[Buffer.FEEDBACK_VERTEX]);
        gl4.glBindTransformFeedback(GL_TRANSFORM_FEEDBACK, 0);

        return true;
    }

    private boolean initBuffer(GL4 gl4) {

        gl4.glGenBuffers(Buffer.MAX, bufferName, 0);

        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.TRANSFORM_ELEMENT]);
        IntBuffer elementBuffer = GLBuffers.newDirectIntBuffer(elementData);
        gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(elementBuffer);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.TRANSFORM_VERTEX]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl4.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(vertexBuffer);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.FEEDBACK_VERTEX]);
        gl4.glBufferData(GL_ARRAY_BUFFER, 2 * 4 * Float.BYTES * elementCount, null, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        FloatUtil.makePerspective(projection, 0, true, (float) Math.PI * 0.25f, 4.0f / 3.0f, 0.1f, 100.0f);
        FloatUtil.makeIdentity(model);
        FloatUtil.multMatrix(projection, view(), mvp);
        FloatUtil.multMatrix(mvp, model);

        gl4.glProgramUniformMatrix4fv(programName[Pipeline.TRANSFORM],
                transformUniformMvp, 1, false, mvp, 0);
        gl4.glProgramUniformMatrix4fv(programName[Pipeline.FEEDBACK],
                feedbackUniformMvp, 1, false, mvp, 0);

        gl4.glViewportIndexedf(0, 0, 0, windowSize.x, windowSize.y);

        float[] depth = {1.0f};
        gl4.glClearBufferfv(GL_DEPTH, 0, depth, 0);
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{0.0f, 0.0f, 0.0f, 1.0f}, 0);

        // First draw, capture the attributes
        // Disable rasterisation, vertices processing only!
        gl4.glEnable(GL_RASTERIZER_DISCARD);

        gl4.glBindProgramPipeline(pipelineName[Pipeline.TRANSFORM]);
        gl4.glBindVertexArray(vertexArrayName[Pipeline.TRANSFORM]);

        gl4.glBindTransformFeedback(GL_TRANSFORM_FEEDBACK, feedbackName[0]);
        gl4.glBeginTransformFeedback(GL_TRIANGLES);
        {
            gl4.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_INT, 0, 1, 0);
        }
        gl4.glEndTransformFeedback();

        gl4.glDisable(GL_RASTERIZER_DISCARD);

        // Second draw, reuse the captured attributes
        gl4.glBindProgramPipeline(pipelineName[Pipeline.FEEDBACK]);
        gl4.glBindVertexArray(vertexArrayName[Pipeline.FEEDBACK]);

        gl4.glDrawTransformFeedbackStreamInstanced(GL_TRIANGLE_STRIP, feedbackName[0], 0, 5);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteProgramPipelines(Pipeline.MAX, pipelineName, 0);
        gl4.glDeleteVertexArrays(Pipeline.MAX, vertexArrayName, 0);
        gl4.glDeleteBuffers(Buffer.MAX, bufferName, 0);
        gl4.glDeleteProgram(programName[Pipeline.TRANSFORM]);
        gl4.glDeleteProgram(programName[Pipeline.FEEDBACK]);
        gl4.glDeleteTransformFeedbacks(1, feedbackName, 0);

        return true;
    }
}
