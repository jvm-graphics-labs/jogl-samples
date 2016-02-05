/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_320.draw;

import com.jogamp.common.nio.PointerBuffer;
import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2ES3.*;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import core.glm;
import dev.Mat4;
import dev.Vec2;
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
public class Gl_320_draw_multiple extends Test {

    public static void main(String[] args) {
        Gl_320_draw_multiple gl_320_draw_multiple = new Gl_320_draw_multiple();
    }

    public Gl_320_draw_multiple() {
        super("gl-320-draw-multiple", Profile.CORE, 3, 2, new Vec2(Math.PI * 0.2f));
    }

    private final String SHADERS_SOURCE = "draw-multiple";
    private final String SHADERS_ROOT = "src/data/gl_320/draw";

    private int ElementCount = 6;
    private int ElementSize = ElementCount * Integer.BYTES;
    private int[] ElementData = new int[]{
        0, 1, 2,
        0, 2, 3
    };

    private int VertexCount = 8;
    private int PositionSize = VertexCount * 3 * Float.BYTES;
    private float[] PositionData = new float[]{
        -1.0f, -1.0f, +0.5f,
        +1.0f, -1.0f, +0.5f,
        +1.0f, +1.0f, +0.5f,
        -1.0f, +1.0f, +0.5f,
        -0.5f, -1.0f, -0.5f,
        +0.5f, -1.0f, -0.5f,
        +1.5f, +1.0f, -0.5f,
        -1.5f, +1.0f, -0.5f};

    private IntBuffer count = GLBuffers.newDirectIntBuffer(new int[]{ElementCount, ElementCount});
    private IntBuffer baseVertex = GLBuffers.newDirectIntBuffer(new int[]{0, 4});

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int ELEMENT = 1;
        public static final int TRANSFORM = 2;
        public static final int MAX = 3;
    };

    private int programName, uniformTransform;
    private int[] bufferName = new int[Buffer.MAX], vertexArrayName = {0};

    @Override
    protected boolean begin(GL gl) {

        GL3 gl3 = (GL3) gl;

        boolean validated = true;

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

        if (validated) {

            ShaderCode vertShader = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragShader = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

            ShaderProgram program = new ShaderProgram();
            program.add(vertShader);
            program.add(fragShader);
            program.init(gl3);

            programName = program.program();

            gl3.glBindAttribLocation(programName, Semantic.Attr.POSITION, "position");
            gl3.glBindFragDataLocation(programName, Semantic.Frag.COLOR, "color");

            program.link(gl3, System.out);
        }
        if (validated) {

            uniformTransform = gl3.glGetUniformBlockIndex(programName, "Transform");
            gl3.glUniformBlockBinding(programName, uniformTransform, Semantic.Uniform.TRANSFORM0);
        }
        return validated & checkError(gl3, "initProgram");
    }

    private boolean initBuffer(GL3 gl3) {

        gl3.glGenBuffers(Buffer.MAX, bufferName, 0);

        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
        FloatBuffer positionBuffer = GLBuffers.newDirectFloatBuffer(PositionData);
        gl3.glBufferData(GL_ARRAY_BUFFER, PositionSize, positionBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(positionBuffer);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT]);
        IntBuffer elementBuffer = GLBuffers.newDirectIntBuffer(ElementData);
        gl3.glBufferData(GL_ELEMENT_ARRAY_BUFFER, ElementSize, elementBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(elementBuffer);
        gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        int[] uniformBlockSize = {0};
        gl3.glGetActiveUniformBlockiv(programName, uniformTransform, GL_UNIFORM_BLOCK_DATA_SIZE, uniformBlockSize, 0);
        gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
        gl3.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize[0], null, GL_DYNAMIC_DRAW);
        gl3.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return checkError(gl3, "initBuffer");
    }

    private boolean initVertexArray(GL3 gl3) {

        gl3.glGenVertexArrays(1, vertexArrayName, 0);
        gl3.glBindVertexArray(vertexArrayName[0]);
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 3, GL_FLOAT, false, 0, 0);

            gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);

            gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT]);
        }
        gl3.glBindVertexArray(0);

        return checkError(gl3, "initVertexArray");
    }

    @Override
    protected boolean render(GL gl) {

        GL3 gl3 = (GL3) gl;

        Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, 4.0f / 3.0f, 0.1f, 100.0f);
        Mat4 model = new Mat4(1.0f);
        Mat4 mvp = projection.mul(viewMat4()).mul(model);
        {
            gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
            ByteBuffer pointer = gl3.glMapBufferRange(GL_UNIFORM_BUFFER, 0, 16 * Float.BYTES,
                    GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            pointer.asFloatBuffer().put(mvp.toFa_());

            // Make sure the uniform buffer is uploaded
            gl3.glUnmapBuffer(GL_UNIFORM_BUFFER);
            gl3.glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }

        gl3.glViewport(0, 0, windowSize.x, windowSize.y);

        float[] depth = new float[]{1.0f};
        gl3.glClearBufferfv(GL_DEPTH, 0, depth, 0);
        gl3.glClearBufferfv(GL_COLOR, 0, new float[]{0.0f, 0.0f, 0.0f, 1.0f}, 0);

        gl3.glUseProgram(programName);

        // Attach the buffer to UBO binding point semantic::uniform::TRANSFORM0
        gl3.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.TRANSFORM]);
        gl3.glBindVertexArray(vertexArrayName[0]);
        PointerBuffer indices = PointerBuffer.allocateDirect(2).put(0).put(0).rewind();
        /**
         * public void glMultiDrawElementsBaseVertex(int mode, IntBuffer count,
         * int type, PointerBuffer indices, int drawcount, IntBuffer basevertex)
         */
        gl3.glMultiDrawElementsBaseVertex(
                GL_TRIANGLES,
                count,
                GL_UNSIGNED_INT,
                indices,
                2,
                baseVertex);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;

        gl3.glDeleteBuffers(Buffer.MAX, bufferName, 0);
        gl3.glDeleteProgram(programName);
        gl3.glDeleteVertexArrays(1, vertexArrayName, 0);
        BufferUtils.destroyDirectBuffer(count);
        BufferUtils.destroyDirectBuffer(baseVertex);

        return true;
    }
}
