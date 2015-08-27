/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl320;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2ES3.*;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.Semantic;
import framework.Test;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
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
        super("gl-320-draw-base-vertex", 3, 2);
    }

    private final String SHADERS_SOURCE = "draw-base-vertex";
    private final String SHADERS_ROOT = "src/data/gl_320";

    private int elementCount = 6;
    private int elementSize = elementCount * GLBuffers.SIZEOF_SHORT;
    private short[] elementData = new short[]{
        0, 1, 2,
        0, 2, 3
    };

    private int vertexCount = 8;
    private int positionSize = vertexCount * 3 * GLBuffers.SIZEOF_FLOAT;
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

    private int colorSize = vertexCount * 4 * GLBuffers.SIZEOF_BYTE;
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

    private enum Buffer {

        POSITION, COLOR, ELEMENT, TRANSFORM, MAX
    }

    private int[] bufferName = new int[Buffer.MAX.ordinal()], vertexArrayName = new int[1];
    private int programName, uniformTransform;
    private float[] projection = new float[16], model = new float[16], mvp = new float[16];

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

            gl3.glBindAttribLocation(programName, Semantic.Attr.POSITION, "Position");
            gl3.glBindAttribLocation(programName, Semantic.Attr.COLOR, "Color");
            gl3.glBindFragDataLocation(programName, Semantic.Frag.COLOR, "Color");

            program.link(gl3, System.out);
        }
        // Get variables locations
        if (validated) {

            uniformTransform = gl3.glGetUniformBlockIndex(programName, "transform");
            gl3.glUniformBlockBinding(programName, uniformTransform, Semantic.Uniform.TRANSFORM0);
        }

        return validated & checkError(gl3, "initProgram");
    }

    private boolean initBuffer(GL3 gl3) {
        gl3.glGenBuffers(Buffer.MAX.ordinal(), bufferName, 0);

        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.POSITION.ordinal()]);
        FloatBuffer positionBuffer = GLBuffers.newDirectFloatBuffer(positionData);
        gl3.glBufferData(GL_ARRAY_BUFFER, positionSize, positionBuffer, GL_STATIC_DRAW);

        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.COLOR.ordinal()]);
        ByteBuffer colorBuffer = GLBuffers.newDirectByteBuffer(colorData);
        gl3.glBufferData(GL_ARRAY_BUFFER, colorSize, colorBuffer, GL_STATIC_DRAW);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT.ordinal()]);
        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        gl3.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        int[] uniformBlockSize = new int[1];
        gl3.glGetActiveUniformBlockiv(programName, uniformTransform, GL_UNIFORM_BLOCK_DATA_SIZE, uniformBlockSize, 0);
        gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM.ordinal()]);
        gl3.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize[0], null, GL_DYNAMIC_DRAW);
        gl3.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return checkError(gl3, "initBuffer");
    }

    private boolean initVertexArray(GL3 gl3) {

        gl3.glGenVertexArrays(1, vertexArrayName, 0);
        gl3.glBindVertexArray(vertexArrayName[0]);
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.POSITION.ordinal()]);
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 3, GL_FLOAT, false, 3 * GLBuffers.SIZEOF_FLOAT, 0);
            gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);

            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.COLOR.ordinal()]);
            gl3.glVertexAttribPointer(Semantic.Attr.COLOR, 4, GL_UNSIGNED_BYTE, true, 4 * GLBuffers.SIZEOF_BYTE, 0);
            gl3.glEnableVertexAttribArray(Semantic.Attr.COLOR);

            gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);
            gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT.ordinal()]);
        }
        gl3.glBindVertexArray(0);

        return checkError(gl3, "initVertexArray");
    }

    @Override
    protected boolean render(GL gl) {

        GL3 gl3 = (GL3) gl;
        {
            gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM.ordinal()]);
            ByteBuffer transformBuffer = gl.glMapBufferRange(GL_UNIFORM_BUFFER,
                    0, 16 * GLBuffers.SIZEOF_FLOAT, GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);
            
            FloatUtil.makePerspective(projection, 0, true, (float) Math.PI * 0.25f,
                    (float) glWindow.getWidth() / (float) glWindow.getHeight(), 0.1f, 100.0f);
//            FloatUtil.makeIdentity(model);
            FloatUtil.makeRotationEuler(model, 0, (float)Math.PI/8, (float)Math.PI/4, 0);

            view();
            FloatUtil.multMatrix(view, model, mvp);
            FloatUtil.multMatrix(projection, mvp, mvp);

//            FloatUtil.makeScale(model, true, .5f, .5f, .5f);
            for (int i = 0; i < mvp.length; i++) {
                transformBuffer.putFloat(i * GLBuffers.SIZEOF_FLOAT, mvp[i]);
            }
            transformBuffer.rewind();

            // Make sure the uniform buffer is uploaded
            gl3.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        gl.glViewport(0, 0, glWindow.getWidth(), glWindow.getHeight());

        float[] depth = new float[]{1.0f};
        gl3.glClearBufferfv(GL_DEPTH, 0, depth, 0);
        gl3.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, 0);

        gl3.glUseProgram(programName);
        gl3.glBindVertexArray(vertexArrayName[0]);
        gl3.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.TRANSFORM.ordinal()]);

        gl3.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0);
        gl3.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 4);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;

        gl3.glDeleteBuffers(Buffer.MAX.ordinal(), bufferName, 0);
        gl3.glDeleteProgram(programName);
        gl3.glDeleteVertexArrays(1, vertexArrayName, 0);

        return true;
    }
}
