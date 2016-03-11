/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_450;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2ES2.GL_QUERY_RESULT;
import com.jogamp.opengl.GL4;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import glm.glm;
import glm.mat._4.Mat4;
import glm.vec._2.Vec2;
import glm.vec._3.Vec3;
import glm.vec._4.Vec4;
import framework.BufferUtils;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 *
 * @author GBarbieri
 */
public class Gl_450_query_conditional extends Test {

    public static void main(String[] args) {
        Gl_450_query_conditional gl_450_query_conditional = new Gl_450_query_conditional();
    }

    public Gl_450_query_conditional() {
        super("gl-450-query-conditional", Profile.CORE, 4, 5);
    }

    private final String SHADERS_SOURCE = "query-conditional";
    private final String SHADERS_ROOT = "src/data/gl_450";

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
        public static final int MATERIAL = 2;
        public static final int MAX = 3;
    }

    private IntBuffer bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX),
            vertexArrayName = GLBuffers.newDirectIntBuffer(1), pipelineName = GLBuffers.newDirectIntBuffer(1),
            queryName = GLBuffers.newDirectIntBuffer(1);
    private int programName, uniformMaterialOffset, uniformTransformOffset;
    private FloatBuffer whiteColor = GLBuffers.newDirectFloatBuffer(new float[]{1, 1, 1, 1});
    private FloatBuffer blackColor = GLBuffers.newDirectFloatBuffer(new float[]{0, 0, 0, 1});
    private boolean toggle = true;

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;
        validated = validated && gl4.isExtensionAvailable("GL_ARB_conditional_render_inverted");

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

        gl4.glGenQueries(1, queryName);

        int[] queryBits = {0};
        gl4.glGetQueryiv(GL_ANY_SAMPLES_PASSED_CONSERVATIVE, GL_QUERY_COUNTER_BITS, queryBits, 0);

        return queryBits[0] >= 1;
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

            gl4.glGenProgramPipelines(1, pipelineName);
            gl4.glUseProgramStages(pipelineName.get(0), GL_VERTEX_SHADER_BIT | GL_FRAGMENT_SHADER_BIT, programName);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        int[] uniformBufferOffset = {0};
        gl4.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset, 0);

        gl4.glGenBuffers(Buffer.MAX, bufferName);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
        FloatBuffer positionBuffer = GLBuffers.newDirectFloatBuffer(positionData);
        gl4.glBufferData(GL_ARRAY_BUFFER, positionSize, positionBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(positionBuffer);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        uniformTransformOffset = Math.max(Mat4.SIZE, uniformBufferOffset[0]);

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.TRANSFORM));
        gl4.glBufferData(GL_UNIFORM_BUFFER, uniformTransformOffset * 2, null, GL_DYNAMIC_DRAW);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        uniformMaterialOffset = Math.max(Vec4.SIZE, uniformBufferOffset[0]);

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.MATERIAL));
        gl4.glBufferData(GL_UNIFORM_BUFFER, uniformMaterialOffset * 2, null, GL_STATIC_DRAW);
        {
            ByteBuffer pointer = gl4.glMapBufferRange(GL_UNIFORM_BUFFER, 0, uniformMaterialOffset * 2,
                    GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            pointer.asFloatBuffer().put(new float[]{0.0f, 0.5f, 1.0f, 1.0f});
            pointer.position(uniformMaterialOffset);
            pointer.asFloatBuffer().put(new float[]{1.0f, 0.5f, 0.0f, 1.0f});
            pointer.rewind();

            gl4.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return true;
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(1, vertexArrayName);
        gl4.glBindVertexArray(vertexArrayName.get(0));
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
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
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.TRANSFORM));
            ByteBuffer pointer = gl4.glMapBufferRange(GL_UNIFORM_BUFFER,
                    0, uniformTransformOffset * 2, GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, (float) windowSize.x / windowSize.y, 0.1f, 10.0f);
            /**
             * A translation of Vec3(0.0f, 0.0f, 0.0f) pushes the square outside the screen, it makes no sense.
             */
            Mat4 model0 = new Mat4(1.0f).translate(new Vec3(0.0f, 0.0f, 0.0f));
            Mat4 model1 = new Mat4(1.0f);

            pointer.position(uniformTransformOffset * 0);
            pointer.asFloatBuffer().put(projection.mul_(viewMat4()).mul(model0).toFa_());
            pointer.position(uniformTransformOffset * 1);
            pointer.asFloatBuffer().put(projection.mul(viewMat4()).mul(model1).toFa_());
            pointer.rewind();

            gl4.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        // Set the display viewport
        gl4.glViewport(0, 0, windowSize.x, windowSize.y);

        // Clear color buffer with black
        gl4.glClearBufferfv(GL_COLOR, 0, blackColor);

        gl4.glBindProgramPipeline(pipelineName.get(0));
        gl4.glBindVertexArray(vertexArrayName.get(0));
        gl4.glBindBufferRange(GL_UNIFORM_BUFFER, Semantic.Uniform.MATERIAL, bufferName.get(Buffer.MATERIAL), 0,
                Vec4.SIZE);
        gl4.glBindBufferRange(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName.get(Buffer.TRANSFORM), 0,
                Mat4.SIZE);

        // The first orange quad is not written in the framebuffer.
        gl4.glColorMaski(0, false, false, false, false);

        // Beginning of the samples count query
        gl4.glBeginQuery(GL_ANY_SAMPLES_PASSED_CONSERVATIVE, queryName.get(0));
        {
            if (toggle) {
                // To test the condional rendering, comment this line, the next draw call won't happen.
                gl4.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 1);
                // End of the samples count query
            }
        }
        gl4.glEndQuery(GL_ANY_SAMPLES_PASSED_CONSERVATIVE);

        // The second blue quad is written in the framebuffer only if a sample pass the occlusion query.
        gl4.glColorMaski(0, true, true, true, true);

        gl4.glBindBufferRange(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName.get(Buffer.TRANSFORM),
                uniformTransformOffset, Mat4.SIZE);

        // Draw only if one sample went through the tests, 
        // we don't need to get the query result which prevent the rendering pipeline to stall.
        gl4.glBeginConditionalRender(queryName.get(0), GL_QUERY_WAIT);
        {	// Clear color buffer with white
            gl4.glClearBufferfv(GL_COLOR, 0, whiteColor);

            gl4.glBindBufferRange(GL_UNIFORM_BUFFER, Semantic.Uniform.MATERIAL, bufferName.get(Buffer.MATERIAL), 0,
                    Vec4.SIZE);
            gl4.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 1);
        }
        gl4.glEndConditionalRender();

        gl4.glBeginConditionalRender(queryName.get(0), GL_QUERY_WAIT_INVERTED);
        {	// Clear color buffer with black
            gl4.glClearBufferfv(GL_COLOR, 0, blackColor);

            gl4.glBindBufferRange(GL_UNIFORM_BUFFER, Semantic.Uniform.MATERIAL, bufferName.get(Buffer.MATERIAL),
                    uniformMaterialOffset, Vec4.SIZE);
            gl4.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 1);
        }
        gl4.glEndConditionalRender();

        toggle = !toggle;

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteProgramPipelines(1, pipelineName);
        BufferUtils.destroyDirectBuffer(pipelineName);
        gl4.glDeleteProgram(programName);
        gl4.glDeleteBuffers(Buffer.MAX, bufferName);
        BufferUtils.destroyDirectBuffer(bufferName);
        gl4.glDeleteVertexArrays(1, vertexArrayName);
        BufferUtils.destroyDirectBuffer(vertexArrayName);
        gl4.glDeleteQueries(1, queryName);
        BufferUtils.destroyDirectBuffer(queryName);

        return true;
    }
}
