/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_420;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2ES3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import glm.glm;
import glm.mat._4.Mat4;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import glm.vec._2.Vec2;
import glm.vec._2.i.Vec2i;
import framework.BufferUtils;
import glm.vec._4.Vec4;

/**
 *
 * @author GBarbieri
 */
public class Gl_420_draw_base_instance extends Test {

    public static void main(String[] args) {
        Gl_420_draw_base_instance gl_420_draw_base_instance = new Gl_420_draw_base_instance();
    }

    public Gl_420_draw_base_instance() {
        super("gl-420-draw-base-instance", Profile.CORE, 4, 2, new Vec2i(640, 480), new Vec2(Math.PI * 0.2f));
    }

    private final String SHADERS_SOURCE = "draw-base-instance";
    private final String SHADERS_ROOT = "src/data/gl_420";

    private int elementCount = 7;
    private int elementSize = elementCount * Integer.BYTES;
    private int[] elementData = {
        100, // will be skipped
        0, 1, 2,
        0, 2, 3};

    private int vertexCount = 6;
    private int positionSize = vertexCount * Vec2.SIZE;
    private float[] positionData = {
        +0.0f, +0.0f, // will be skipper
        +0.0f, +0.0f, // will be skipper
        -1.0f, -1.0f,
        +1.0f, -1.0f,
        +1.0f, +1.0f,
        -1.0f, +1.0f};

    private int colorCount = 10;
    private int colorSize = colorCount * Vec4.SIZE;
    private float[] colorData = {
        1.0f, 0.5f, 0.0f, 1.0f,
        0.8f, 0.4f, 0.0f, 1.0f,
        0.6f, 0.3f, 0.0f, 1.0f,
        0.4f, 0.2f, 0.0f, 1.0f,
        0.2f, 0.1f, 0.0f, 1.0f,
        0.0f, 0.1f, 0.2f, 1.0f,
        0.0f, 0.2f, 0.4f, 1.0f,
        0.0f, 0.3f, 0.6f, 1.0f,
        0.0f, 0.4f, 0.8f, 1.0f,
        0.0f, 0.5f, 1.0f, 1.0f};

    private class Buffer {

        public static final int POSITION = 0;
        public static final int COLOR = 1;
        public static final int ELEMENT = 2;
        public static final int TRANSFORM = 3;
        public static final int MAX = 4;
    }

    private int[] pipelineName = {0}, vertexArrayName = {0}, bufferName = new int[Buffer.MAX];
    private int programName;

    private boolean initTest(GL4 gl4) {

        boolean validated = true;

        gl4.glEnable(GL_DEPTH_TEST);

        return validated;
    }

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;

        if (validated) {
            validated = initTest(gl4);
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

        return validated;
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        gl4.glGenProgramPipelines(1, pipelineName, 0);

        // Create program
        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

            shaderProgram.init(gl4);
            programName = shaderProgram.program();

            gl4.glProgramParameteri(programName, GL_PROGRAM_SEPARABLE, GL_TRUE);

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);

            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            gl4.glUseProgramStages(pipelineName[0], GL_VERTEX_SHADER_BIT | GL_FRAGMENT_SHADER_BIT, programName);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        boolean validated = true;

        gl4.glGenBuffers(Buffer.MAX, bufferName, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.POSITION]);
        FloatBuffer positionBuffer = GLBuffers.newDirectFloatBuffer(positionData);
        gl4.glBufferData(GL_ARRAY_BUFFER, positionSize, positionBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(positionBuffer);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.COLOR]);
        FloatBuffer colorBuffer = GLBuffers.newDirectFloatBuffer(colorData);
        gl4.glBufferData(GL_ARRAY_BUFFER, colorSize, colorBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(colorBuffer);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT]);
        IntBuffer elementBuffer = GLBuffers.newDirectIntBuffer(elementData);
        gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(elementBuffer);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
        gl4.glBufferData(GL_UNIFORM_BUFFER, Mat4.SIZE, null, GL_DYNAMIC_DRAW);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return validated;
    }

    private boolean initVertexArray(GL4 gl4) {

        boolean validated = true;

        gl4.glGenVertexArrays(1, vertexArrayName, 0);
        gl4.glBindVertexArray(vertexArrayName[0]);
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.POSITION]);
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 0, 0);
            gl4.glVertexAttribDivisor(Semantic.Attr.POSITION, 0);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.COLOR]);
            gl4.glVertexAttribPointer(Semantic.Attr.COLOR, 4, GL_FLOAT, false, 0, 0);
            gl4.glVertexAttribDivisor(Semantic.Attr.COLOR, 1);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl4.glEnableVertexAttribArray(Semantic.Attr.COLOR);
        }
        gl4.glBindVertexArray(0);

        return validated;
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        {
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
            ByteBuffer pointer = gl4.glMapBufferRange(
                    GL_UNIFORM_BUFFER, 0, Mat4.SIZE,
                    GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT | GL_MAP_UNSYNCHRONIZED_BIT);

            Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, (float) windowSize.x / windowSize.y, 0.1f, 100.0f);
            Mat4 model = new Mat4(1.0f);

            pointer.asFloatBuffer().put(projection.mul(viewMat4()).mul(model).toFa_());

            // Make sure the uniform buffer is uploaded
            gl4.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        gl4.glViewportIndexedf(0, 0, 0, windowSize.x, windowSize.y);

        float[] depth = {1.0f};
        gl4.glClearBufferfv(GL_DEPTH, 0, depth, 0);
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, 0);

        gl4.glBindProgramPipeline(pipelineName[0]);
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.TRANSFORM]);
        gl4.glBindVertexArray(vertexArrayName[0]);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT]);

        gl4.glDrawElementsInstancedBaseVertexBaseInstance(GL_TRIANGLES, elementCount, GL_UNSIGNED_INT,
                1 * Integer.BYTES, // indices offset
                5, // instance count
                2, // base vertex
                5); // base instance

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;

        gl4.glDeleteProgramPipelines(1, pipelineName, 0);
        gl4.glDeleteProgram(programName);
        gl4.glDeleteBuffers(Buffer.MAX, bufferName, 0);
        gl4.glDeleteVertexArrays(1, vertexArrayName, 0);

        return validated;
    }
}
