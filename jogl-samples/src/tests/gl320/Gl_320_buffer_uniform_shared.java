/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl320;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_DYNAMIC_DRAW;
import static com.jogamp.opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_MAP_INVALIDATE_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_MAP_WRITE_BIT;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNSIGNED_SHORT;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BLOCK_DATA_SIZE;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT;
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
 * @author elect
 */
public class Gl_320_buffer_uniform_shared extends Test {

    public static void main(String[] args) {
        Gl_320_buffer_uniform_shared gl_320_buffer_uniform_shared = new Gl_320_buffer_uniform_shared();
    }

    public Gl_320_buffer_uniform_shared() {
        super("gl-320-buffer-uniform-shared", 3, 2);
    }

    private final String SHADERS_SOURCE = "buffer-uniform-shared";
    private final String SHADERS_ROOT = "src/data/gl_320";

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

    private final ShaderCode[] shaderName = new ShaderCode[Shader.max.ordinal()];
    private final int[] bufferName = new int[Buffer.max.ordinal()];
    private int programName, uniformMaterial, uniformTransform;
    private int[] uniformBlockSizeTransform, uniformBlockSizeMaterial, vertexArrayName;
    private float[] projection = new float[16], model = new float[16];

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

            program.init(gl3);

            programName = program.program();

            gl3.glBindAttribLocation(programName, Semantic.Attr.POSITION, "position");
            gl3.glBindFragDataLocation(programName, Semantic.Frag.COLOR, "color");

            program.link(gl3, System.out);
        }
        if (validated) {
            uniformMaterial = gl3.glGetUniformBlockIndex(programName, "Material");
            uniformTransform = gl3.glGetUniformBlockIndex(programName, "Transform");
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

        gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.element.ordinal()]);
        ShortBuffer shortBuffer = GLBuffers.newDirectShortBuffer(elementData);
        gl3.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, shortBuffer, GL_STATIC_DRAW);
        gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.vertex.ordinal()]);
        FloatBuffer floatBuffer = GLBuffers.newDirectFloatBuffer(positionData);
        gl3.glBufferData(GL_ARRAY_BUFFER, positionSize, floatBuffer, GL_STATIC_DRAW);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        return checkError(gl3, "initBuffer");
    }

    private boolean initVertexArray(GL3 gl3) {
        vertexArrayName = new int[1];
        gl3.glGenVertexArrays(1, vertexArrayName, 0);
        gl3.glBindVertexArray(vertexArrayName[0]);
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.vertex.ordinal()]);
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 0, 0);

            gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);

            gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.element.ordinal()]);
        }
        gl3.glBindVertexArray(0);

        return checkError(gl3, "initVertexArray");
    }

    @Override
    protected boolean render(GL gl) {

        GL3 gl3 = (GL3) gl;
        {
            // Compute the MVP (Model View Projection matrix)
            FloatUtil.makePerspective(projection, 0, true, FloatUtil.QUARTER_PI, 
                    (float) windowSize.x / windowSize.y, .1f, 100f);
            FloatUtil.makeIdentity(model);
            FloatUtil.multMatrix(projection, view());
            FloatUtil.multMatrix(projection, model);

            float[] diffuse = new float[]{1f, .5f, 0f, 1f};

            gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.uniform.ordinal()]);
            ByteBuffer pointer = gl3.glMapBufferRange(GL_UNIFORM_BUFFER, 0,
                    uniformBlockSizeTransform[0] + diffuse.length * GLBuffers.SIZEOF_FLOAT,
                    GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            for (int i = 0; i < projection.length; i++) {
                pointer.putFloat(i * GLBuffers.SIZEOF_FLOAT, projection[i]);
            }
            for (int i = 0; i < diffuse.length; i++) {
                pointer.putFloat(uniformBlockSizeTransform[0] + i * GLBuffers.SIZEOF_FLOAT, diffuse[i]);
            }

            // Make sure the uniform buffer is uploaded
            gl3.glUnmapBuffer(GL_UNIFORM_BUFFER);
            gl3.glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }

        gl3.glViewport(0, 0, windowSize.x, windowSize.y);
        gl3.glClearBufferfv(GL_COLOR, 0, new float[]{0.0f, 0.0f, 0.0f, 1.0f}, 0);

        gl3.glUseProgram(programName);
        gl3.glUniformBlockBinding(programName, uniformTransform, Semantic.Uniform.TRANSFORM0);
        gl3.glUniformBlockBinding(programName, uniformMaterial, Semantic.Uniform.MATERIAL);

        // Attach the buffer to UBO binding point semantic::uniform::TRANSFORM0
        gl3.glBindBufferRange(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0,
                bufferName[Buffer.uniform.ordinal()], 0, uniformBlockSizeTransform[0]);
        // Attach the buffer to UBO binding point semantic::uniform::MATERIAL 
        gl3.glBindBufferRange(GL_UNIFORM_BUFFER, Semantic.Uniform.MATERIAL, bufferName[Buffer.uniform.ordinal()],
                uniformBlockSizeTransform[0], uniformBlockSizeMaterial[0]);

        // Bind vertex array & draw 
        gl3.glBindVertexArray(vertexArrayName[0]);
        gl3.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0);
        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;

        gl3.glDeleteVertexArrays(1, vertexArrayName, 0);
        gl3.glDeleteBuffers(Buffer.max.ordinal(), bufferName, 0);
        gl3.glDeleteProgram(programName);

        return true;
    }
}
