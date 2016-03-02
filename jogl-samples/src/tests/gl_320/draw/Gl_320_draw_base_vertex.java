/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_320.draw;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2ES3.*;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import dev.Vec4u8;
import glm.glm;
import glm.mat._4.Mat4;
import glm.vec._2.Vec2;
import framework.BufferUtils;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import glm.vec._3.Vec3;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 *
 * @author gbarbieri
 */
public class Gl_320_draw_base_vertex extends Test {

    public static void main(String[] args) {
        Gl_320_draw_base_vertex gl_320_draw_base_vertex = new Gl_320_draw_base_vertex();
    }

    public Gl_320_draw_base_vertex() {
        super("gl-320-draw-base-vertex", Profile.CORE, 3, 2, new Vec2(Math.PI * 0.2f, Math.PI * 0.2f));
    }

    private final String SHADERS_SOURCE = "draw-base-vertex";
    private final String SHADERS_ROOT = "src/data/gl_320/draw";

    private int elementCount = 6;
    private int elementSize = elementCount * Short.BYTES;
    private short[] elementData = new short[]{
        0, 1, 2,
        0, 2, 3
    };

    private int vertexCount = 8;
    private int positionSize = vertexCount * Vec3.SIZE;
    private float[] positionData = new float[]{
        -1.0f, -1.0f, +0.5f,
        +1.0f, -1.0f, +0.5f,
        +1.0f, +1.0f, +0.5f,
        -1.0f, +1.0f, +0.5f,
        -0.5f, -1.0f, -0.5f,
        +0.5f, -1.0f, -0.5f,
        +1.5f, +1.0f, -0.5f,
        -1.5f, +1.0f, -0.5f
    };

    private int colorSize = vertexCount * Vec4u8.SIZE;
    private byte[] colorData = new byte[]{
        (byte) 255, (byte) 0, (byte) 0, (byte) 255,
        (byte) 255, (byte) 255, 0, (byte) 255,
        (byte) 0, (byte) 255, (byte) 0, (byte) 255,
        (byte) 0, (byte) 0, (byte) 255, (byte) 255,
        (byte) 255, (byte) 128, (byte) 0, (byte) 255,
        (byte) 255, (byte) 128, (byte) 0, (byte) 255,
        (byte) 255, (byte) 128, (byte) 0, (byte) 255,
        (byte) 255, (byte) 128, (byte) 0, (byte) 255
    };

    private class Buffer {

        public static final int POSITION = 0;
        public static final int COLOR = 1;
        public static final int ELEMENT = 2;
        public static final int TRANSFORM = 3;
        public static final int MAX = 4;
    }

    private IntBuffer bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX), 
            vertexArrayName = GLBuffers.newDirectIntBuffer(1);
    private int programName, uniformTransform;

    @Override
    protected boolean begin(GL gl) {

        boolean validated = true;

        GL3 gl3 = (GL3) gl;

        if (validated) {
            validated = initProgram(gl3);
        }
        if (validated) {
            validated = initBuffer(gl3);
        }
        if (validated) {
            validated = initVertexArray(gl3);
        }
        gl3.glEnable(GL_DEPTH_TEST);
        gl3.glDepthFunc(GL_LESS);

        return validated && checkError(gl, "begin");
    }

    private boolean initProgram(GL3 gl3) {

        boolean validated = true;
        // Create program
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
            gl3.glBindAttribLocation(programName, Semantic.Attr.COLOR, "color");
            gl3.glBindFragDataLocation(programName, Semantic.Frag.COLOR, "color");

            program.link(gl3, System.out);
        }
        // Get variables locations
        if (validated) {

            uniformTransform = gl3.glGetUniformBlockIndex(programName, "Transform");
            gl3.glUniformBlockBinding(programName, uniformTransform, Semantic.Uniform.TRANSFORM0);
        }

        return validated & checkError(gl3, "initProgram");
    }

    private boolean initBuffer(GL3 gl3) {
        
        
        FloatBuffer positionBuffer = GLBuffers.newDirectFloatBuffer(positionData);
        ByteBuffer colorBuffer = GLBuffers.newDirectByteBuffer(colorData);
        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        
        gl3.glGenBuffers(Buffer.MAX, bufferName);

        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.POSITION));
        gl3.glBufferData(GL_ARRAY_BUFFER, positionSize, positionBuffer, GL_STATIC_DRAW);

        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.COLOR));
        gl3.glBufferData(GL_ARRAY_BUFFER, colorSize, colorBuffer, GL_STATIC_DRAW);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT));
        gl3.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        int[] uniformBlockSize = {0};
        gl3.glGetActiveUniformBlockiv(programName, uniformTransform, GL_UNIFORM_BLOCK_DATA_SIZE, uniformBlockSize, 0);
        gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.TRANSFORM));
        gl3.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize[0], null, GL_DYNAMIC_DRAW);
        gl3.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        
        BufferUtils.destroyDirectBuffer(positionBuffer);        
        BufferUtils.destroyDirectBuffer(colorBuffer);
        BufferUtils.destroyDirectBuffer(elementBuffer);
        
        return checkError(gl3, "initBuffer");
    }

    private boolean initVertexArray(GL3 gl3) {

        gl3.glGenVertexArrays(1, vertexArrayName);
        gl3.glBindVertexArray(vertexArrayName.get(0));
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.POSITION));
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 3, GL_FLOAT, false, Vec3.SIZE, 0);
            gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);

            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.COLOR));
            gl3.glVertexAttribPointer(Semantic.Attr.COLOR, 4, GL_UNSIGNED_BYTE, true, Vec4u8.SIZE, 0);
            gl3.glEnableVertexAttribArray(Semantic.Attr.COLOR);

            gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);
            gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT));
        }
        gl3.glBindVertexArray(0);

        return checkError(gl3, "initVertexArray");
    }

    @Override
    protected boolean render(GL gl) {

        GL3 gl3 = (GL3) gl;
        {
            gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.TRANSFORM));
            ByteBuffer pointer = gl.glMapBufferRange(GL_UNIFORM_BUFFER, 0, Mat4.SIZE, GL_MAP_WRITE_BIT 
                    | GL_MAP_INVALIDATE_BUFFER_BIT);

            Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, (float) windowSize.x / windowSize.y, 0.1f, 100.0f);
            Mat4 model = new Mat4(1.0f);

            pointer.asFloatBuffer().put(projection.mul(viewMat4()).mul(model).toFa_());

            // Make sure the uniform buffer is uploaded
            gl3.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        gl.glViewport(0, 0, windowSize.x, windowSize.y);

        float[] depth = new float[]{1.0f};
        gl3.glClearBufferfv(GL_DEPTH, 0, depth, 0);
        gl3.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, 0);

        gl3.glUseProgram(programName);
        gl3.glBindVertexArray(vertexArrayName.get(0));
        gl3.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName.get(Buffer.TRANSFORM));

        gl3.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0);
        gl3.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 4);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;

        gl3.glDeleteBuffers(Buffer.MAX, bufferName);
        gl3.glDeleteProgram(programName);
        gl3.glDeleteVertexArrays(1, vertexArrayName);

        BufferUtils.destroyDirectBuffer(bufferName);
        BufferUtils.destroyDirectBuffer(vertexArrayName);
        
        return true;
    }
}
