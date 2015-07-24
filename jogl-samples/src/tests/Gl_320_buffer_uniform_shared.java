/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_DYNAMIC_DRAW;
import static com.jogamp.opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BLOCK_DATA_SIZE;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GL3ES3;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.Semantic;
import framework.Test;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 *
 * @author elect
 */
public class Gl_320_buffer_uniform_shared extends Test {

    private final String SHADERS_SOURCE = "buffer-uniform-shared";
    private final String SHADERS_ROOT = "src/data/es_320";

    private final int vertexCount = 4;
    private final int positionSize = vertexCount * 2 * GLBuffers.SIZEOF_FLOAT;
    private final float[] positionData = new float[]{
        -1f, -1f,
        +1f, -1f,
        +1f, +1f,
        -1f, +1f};

    private final int elementCount = 6;
    private final int elementSize = elementCount * GLBuffers.SIZEOF_SHORT;
    private final short[] elementData = new short[]{
        0, 1, 2,
        2, 3, 0};

    private enum Buffer {

        vertex, element, uniform, max
    }

    private enum Shader {

        vert, frag, max
    }

    public Gl_320_buffer_uniform_shared() {
        super("Gl_320_buffer_uniform_shared", 3, 2);
    }

    private final ShaderCode[] shaderName = new ShaderCode[Shader.max.ordinal()];
    private final int[] bufferName = new int[Buffer.max.ordinal()];
    private int programName, uniformMaterial, uniformTransform;
    private int[] uniformBlockSizeTransform, uniformBlockSizeMaterial;

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
//        if (validated) {
//            validated = initVertexArray(gl3);
//        }
        return validated;
    }

    private boolean initProgram(GL3 gl3) {

        boolean validated = true;

        if (validated) {
            shaderName[Shader.vert.ordinal()] = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            shaderName[Shader.frag.ordinal()] = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

            ShaderProgram program = new ShaderProgram();
            program.add(shaderName[Shader.vert.ordinal()]);
            program.add(shaderName[Shader.frag.ordinal()]);

            program.link(gl3, System.out);

            programName = program.program();

            gl3.glBindAttribLocation(programName, Semantic.Attr.position, "position");
            gl3.glBindFragDataLocation(programName, Semantic.Frag.color, "color");
        }

        if (validated) {
            uniformMaterial = gl3.glGetUniformLocation(programName, "material");
            uniformTransform = gl3.glGetUniformLocation(programName, "transform");
        }

        return validated & checkError(gl3, "initProgram");
    }

    private boolean initBuffer(GL3 gl3) {

        gl3.glGenBuffers(Buffer.max.ordinal(), bufferName, 0);

        int[] uniformBufferOffset = new int[1];
        gl3.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset, 0);

        uniformBlockSizeTransform = new int[1];
        gl3.glGetActiveUniformBlockiv(programName, uniformTransform,
                GL_UNIFORM_BLOCK_DATA_SIZE, uniformBlockSizeTransform, 0);
        uniformBlockSizeTransform[0] = ((uniformBlockSizeTransform[0] / uniformBufferOffset[0] + 1)
                * uniformBufferOffset[0]);

        uniformBlockSizeMaterial = new int[1];
        gl3.glGetActiveUniformBlockiv(programName, uniformMaterial,
                GL_UNIFORM_BLOCK_DATA_SIZE, uniformBlockSizeMaterial, 0);
        uniformBlockSizeMaterial[0] = ((uniformBlockSizeMaterial[0] / uniformBufferOffset[0] + 1)
                * uniformBufferOffset[0]);

        gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.uniform.ordinal()]);
        gl3.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSizeTransform[0]
                + uniformBlockSizeMaterial[0], null, GL_DYNAMIC_DRAW);
        gl3.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.element.ordinal()]);
        FloatBuffer floatBuffer = GLBuffers.newDirectFloatBuffer(positionData);
        gl3.glBufferData(GL_ARRAY_BUFFER, positionSize, floatBuffer, GL_STATIC_DRAW);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        elementBufferName = new int[1];
        gl3.glGenBuffers(1, elementBufferName, 0);
        gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, elementBufferName[0]);
        IntBuffer intBuffer = GLBuffers.newDirectIntBuffer(elementData);
        gl3.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, intBuffer, GL_STATIC_DRAW);
        gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        return checkError(gl3, "initBuffer");
    }
}
