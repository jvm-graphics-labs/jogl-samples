/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_440;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import glm.glm;
import glm.mat._4.Mat4;
import glm.vec._4.Vec4;
import framework.BufferUtils;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 *
 * @author GBarbieri
 */
public class Gl_440_transform_feedback extends Test {

    public static void main(String[] args) {
        Gl_440_transform_feedback gl_440_transform_feedback = new Gl_440_transform_feedback();
    }

    public Gl_440_transform_feedback() {
        super("gl-440-transform-feedback", Profile.CORE, 4, 4);
    }

    private final String SHADER_SOURCE_TRANSFORM = "transform-feedback-transform";
    private final String SHADERS_SOURCE_FEEDBACK = "transform-feedback-feedback";
    private final String SHADERS_ROOT = "src/data/gl_440";

    private int vertexCount = 6;
    private int positionSize = vertexCount * Vec4.SIZE;
    private float[] positionData = {
        -1.0f, -1.0f, 0.0f, 1.0f,
        +1.0f, -1.0f, 0.0f, 1.0f,
        +1.0f, +1.0f, 0.0f, 1.0f,
        +1.0f, +1.0f, 0.0f, 1.0f,
        -1.0f, +1.0f, 0.0f, 1.0f,
        -1.0f, -1.0f, 0.0f, 1.0f};

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int FEEDBACK = 1;
        public static final int TRANSFORM = 2;
        public static final int MAX = 3;
    }

    private class Program {

        public static final int FEEDBACK = 0;
        public static final int TRANSFORM = 1;
        public static final int MAX = 2;
    }

    private int[] bufferName = new int[Buffer.MAX], feedbackName = {0}, pipelineName = new int[Program.MAX],
            programName = new int[Program.MAX], vertexArrayName = new int[Program.MAX];
    private ByteBuffer uniformPointer;

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;
        validated = validated && gl4.isExtensionAvailable("GL_ARB_enhanced_layouts");

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
        if (validated) {
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
            uniformPointer = gl4.glMapBufferRange(GL_UNIFORM_BUFFER, 0, Mat4.SIZE,
                    GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);
        }

        return validated;
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        ShaderCode vertTransformShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                this.getClass(), SHADERS_ROOT, null, SHADER_SOURCE_TRANSFORM, "vert", null, true);
        ShaderCode vertFeedbackShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_FEEDBACK, "vert", null, true);
        ShaderCode fragFeedbackShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_FEEDBACK, "frag", null, true);

        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();
            shaderProgram.init(gl4);

            programName[Program.TRANSFORM] = shaderProgram.program();

            gl4.glProgramParameteri(programName[Program.TRANSFORM], GL_PROGRAM_SEPARABLE, GL_TRUE);

            shaderProgram.add(vertTransformShaderCode);
            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();
            shaderProgram.init(gl4);

            programName[Program.FEEDBACK] = shaderProgram.program();

            gl4.glProgramParameteri(programName[Program.FEEDBACK], GL_PROGRAM_SEPARABLE, GL_TRUE);

            shaderProgram.add(vertFeedbackShaderCode);
            shaderProgram.add(fragFeedbackShaderCode);
            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            gl4.glGenProgramPipelines(Program.MAX, pipelineName, 0);
            gl4.glUseProgramStages(pipelineName[Program.TRANSFORM], GL_VERTEX_SHADER_BIT | GL_FRAGMENT_SHADER_BIT,
                    programName[Program.TRANSFORM]);
            gl4.glUseProgramStages(pipelineName[Program.FEEDBACK], GL_VERTEX_SHADER_BIT | GL_FRAGMENT_SHADER_BIT,
                    programName[Program.FEEDBACK]);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(Program.MAX, vertexArrayName, 0);

        gl4.glBindVertexArray(vertexArrayName[Program.TRANSFORM]);
        {
            gl4.glVertexAttribFormat(Semantic.Attr.POSITION, 4, GL_FLOAT, false, 0);
            gl4.glVertexAttribBinding(Semantic.Attr.POSITION, Semantic.Buffer.STATIC);
            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
        }
        gl4.glBindVertexArray(0);

        gl4.glBindVertexArray(vertexArrayName[Program.FEEDBACK]);
        {
            gl4.glVertexAttribFormat(Semantic.Attr.POSITION, 4, GL_FLOAT, false, 0);
            gl4.glVertexAttribBinding(Semantic.Attr.POSITION, Semantic.Buffer.STATIC);
            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);

            gl4.glVertexAttribFormat(Semantic.Attr.COLOR, 4, GL_FLOAT, false, Vec4.SIZE);
            gl4.glVertexAttribBinding(Semantic.Attr.COLOR, Semantic.Buffer.STATIC);
            gl4.glEnableVertexAttribArray(Semantic.Attr.COLOR);
        }
        gl4.glBindVertexArray(0);

        return checkError(gl4, "initVertexArray");
    }

    private boolean initFeedback(GL4 gl4) {

        gl4.glGenTransformFeedbacks(1, feedbackName, 0);
        gl4.glBindTransformFeedback(GL_TRANSFORM_FEEDBACK, feedbackName[0]);
        gl4.glBindBufferBase(GL_TRANSFORM_FEEDBACK_BUFFER, 0, bufferName[Buffer.FEEDBACK]);
        gl4.glBindTransformFeedback(GL_TRANSFORM_FEEDBACK, 0);

        return checkError(gl4, "initFeedback");
    }

    private boolean initBuffer(GL4 gl4) {

        gl4.glGenBuffers(Buffer.MAX, bufferName, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
        FloatBuffer positionBuffer = GLBuffers.newDirectFloatBuffer(positionData);
        gl4.glBufferStorage(GL_ARRAY_BUFFER, positionSize, positionBuffer, 0);
        BufferUtils.destroyDirectBuffer(positionBuffer);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.FEEDBACK]);
        gl4.glBufferStorage(GL_ARRAY_BUFFER, glf.Vertex_v4fc4f.SIZE * vertexCount, null, 0);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        int[] uniformBufferOffset = {0};
        gl4.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset, 0);
        int uniformBlockSize = Math.max(Mat4.SIZE, uniformBufferOffset[0]);

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
        gl4.glBufferStorage(GL_UNIFORM_BUFFER, uniformBlockSize, null,
                GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT);

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return checkError(gl4, "initBuffer");
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        {
            Mat4 model = new Mat4(1.0f);
            Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, (float) windowSize.x / windowSize.y, 0.1f, 100.0f);
            Mat4 mvp = projection.mul(viewMat4()).mul(model);

            uniformPointer.asFloatBuffer().put(mvp.toFa_());
        }

        // Set the display viewport
        gl4.glViewportIndexedf(0, 0.0f, 0.0f, windowSize.x, windowSize.y);

        // Clear color buffer
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{0.0f, 0.0f, 0.0f, 1.0f}, 0);

        // First draw, capture the attributes
        // Disable rasterisation, vertices processing only!
        gl4.glEnable(GL_RASTERIZER_DISCARD);

        gl4.glBindProgramPipeline(pipelineName[Program.TRANSFORM]);
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.TRANSFORM]);
        gl4.glBindVertexArray(vertexArrayName[Program.TRANSFORM]);
        gl4.glBindVertexBuffer(Semantic.Buffer.STATIC, bufferName[Buffer.VERTEX], 0, Vec4.SIZE);

        gl4.glBindTransformFeedback(GL_TRANSFORM_FEEDBACK, feedbackName[0]);
        gl4.glBeginTransformFeedback(GL_TRIANGLES);
        {
            gl4.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 1);
        }
        gl4.glEndTransformFeedback();
        gl4.glBindTransformFeedback(GL_TRANSFORM_FEEDBACK, 0);

        gl4.glDisable(GL_RASTERIZER_DISCARD);

        // Second draw, reuse the captured attributes
        gl4.glBindProgramPipeline(pipelineName[Program.FEEDBACK]);
        gl4.glBindVertexArray(vertexArrayName[Program.FEEDBACK]);
        gl4.glBindVertexBuffer(Semantic.Buffer.STATIC, bufferName[Buffer.FEEDBACK], 0, glf.Vertex_v4fc4f.SIZE);

        gl4.glDrawTransformFeedback(GL_TRIANGLES, feedbackName[0]);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        if (uniformPointer == null) {

            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
            gl4.glUnmapBuffer(GL_UNIFORM_BUFFER);
            BufferUtils.destroyDirectBuffer(uniformPointer);
        }

        gl4.glDeleteVertexArrays(Program.MAX, vertexArrayName, 0);
        gl4.glDeleteBuffers(Buffer.MAX, bufferName, 0);
        gl4.glDeleteProgram(programName[Program.FEEDBACK]);
        gl4.glDeleteProgram(programName[Program.TRANSFORM]);
        gl4.glDeleteProgramPipelines(Program.MAX, pipelineName, 0);
        gl4.glDeleteTransformFeedbacks(1, feedbackName, 0);

        return true;
    }
}
