/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_320.buffer;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2ES3.*;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import glm.glm;
import glm.mat._4.Mat4;
import glm.vec._2.Vec2;
import glm.vec._4.Vec4;
import framework.BufferUtils;
import framework.Profile;
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
        super("gl-320-buffer-uniform-shared", Profile.CORE, 3, 2);
    }

    private final String SHADERS_SOURCE = "buffer-uniform-shared";
    private final String SHADERS_ROOT = "src/data/gl_320/buffer";

    private final int vertexCount = 4;
    private final int positionSize = vertexCount * Vec2.SIZE;
    private final float[] positionData = new float[]{
        -1f, -1f,
        +1f, -1f,
        +1f, +1f,
        -1f, +1f};

    private final int elementCount = 6;
    private final int elementSize = elementCount * Short.BYTES;
    private final short[] elementData = new short[]{
        0, 1, 2,
        2, 3, 0};

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int ELEMENT = 1;
        public static final int UNIFORM = 2;
        public static final int MAX = 3;
    }

    private class Shader {

        public static final int VERT = 0;
        public static final int FRAG = 1;
        public static final int MAX = 2;
    }

    private final ShaderCode[] shaderName = new ShaderCode[Shader.MAX];
    private final int[] bufferName = new int[Buffer.MAX], uniformBlockSizeTransform = {0}, uniformBlockSizeMaterial = {0},
            vertexArrayName = {0};
    private int programName, uniformMaterial, uniformTransform;

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
            shaderName[Shader.VERT] = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            shaderName[Shader.FRAG] = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

            ShaderProgram program = new ShaderProgram();
            program.add(shaderName[Shader.VERT]);
            program.add(shaderName[Shader.FRAG]);

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

        gl3.glGenBuffers(Buffer.MAX, bufferName, 0);

        int[] uniformBufferOffset = {0};
        gl3.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset, 0);

        gl3.glGetActiveUniformBlockiv(
                programName,
                uniformTransform,
                GL_UNIFORM_BLOCK_DATA_SIZE,
                uniformBlockSizeTransform, 0);

        uniformBlockSizeTransform[0]
                = ((uniformBlockSizeTransform[0] / uniformBufferOffset[0] + 1) * uniformBufferOffset[0]);

        gl3.glGetActiveUniformBlockiv(
                programName,
                uniformMaterial,
                GL_UNIFORM_BLOCK_DATA_SIZE,
                uniformBlockSizeMaterial, 0);

        uniformBlockSizeMaterial[0]
                = ((uniformBlockSizeMaterial[0] / uniformBufferOffset[0] + 1) * uniformBufferOffset[0]);

        gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.UNIFORM]);
        gl3.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSizeTransform[0] + uniformBlockSizeMaterial[0], null,
                GL_DYNAMIC_DRAW);
        gl3.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT]);
        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        gl3.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(elementBuffer);
        gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
        FloatBuffer positionBuffer = GLBuffers.newDirectFloatBuffer(positionData);
        gl3.glBufferData(GL_ARRAY_BUFFER, positionSize, positionBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(positionBuffer);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        return checkError(gl3, "initBuffer");
    }

    private boolean initVertexArray(GL3 gl3) {

        gl3.glGenVertexArrays(1, vertexArrayName, 0);
        gl3.glBindVertexArray(vertexArrayName[0]);
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 0, 0);

            gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);

            gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT]);
        }
        gl3.glBindVertexArray(0);

        return checkError(gl3, "initVertexArray");
    }

    @Override
    protected boolean render(GL gl) {

        GL3 gl3 = (GL3) gl;
        {
            // Compute the MVP (Model View Projection matrix)
            Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, (float) windowSize.x / windowSize.y, .1f, 100f);
            Mat4 model = new Mat4(1.0f);
            Mat4 mvp = projection.mul(viewMat4()).mul(model);

            float[] diffuse = new float[]{1f, .5f, 0f, 1f};

            gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.UNIFORM]);
            ByteBuffer pointer = gl3.glMapBufferRange(GL_UNIFORM_BUFFER, 0,
                    uniformBlockSizeTransform[0] + Vec4.SIZE,
                    GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            pointer.asFloatBuffer().put(mvp.toFa_());
            pointer.position(uniformBlockSizeTransform[0]);
            pointer.asFloatBuffer().put(diffuse);
            pointer.rewind();

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
        gl3.glBindBufferRange(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.UNIFORM],
                0, uniformBlockSizeTransform[0]);
        // Attach the buffer to UBO binding point semantic::uniform::MATERIAL 
        gl3.glBindBufferRange(GL_UNIFORM_BUFFER, Semantic.Uniform.MATERIAL, bufferName[Buffer.UNIFORM],
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
        gl3.glDeleteBuffers(Buffer.MAX, bufferName, 0);
        gl3.glDeleteProgram(programName);

        return true;
    }
}
