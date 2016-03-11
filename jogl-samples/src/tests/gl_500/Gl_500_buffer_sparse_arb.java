/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_500;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_MAP_WRITE_BIT;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER;
import static com.jogamp.opengl.GL3ES3.GL_SHADER_STORAGE_BUFFER;
import com.jogamp.opengl.GL4;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import glm.glm;
import glm.mat._4.Mat4;
import framework.BufferUtils;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import glf.Vertex_v2fv2f;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import jgli.Texture2d;

/**
 *
 * @author GBarbieri
 */
public class Gl_500_buffer_sparse_arb extends Test {

    public static void main(String[] args) {
        Gl_500_buffer_sparse_arb gl_500_buffer_sparse_arb = new Gl_500_buffer_sparse_arb();
    }

    public Gl_500_buffer_sparse_arb() {
        super("gl-500-buffer-sparse-arb", Profile.CORE, 4, 5);
    }

    private final String SHADERS_SOURCE = "buffer-sparse";
    private final String SHADERS_ROOT = "src/data/gl_500";
    private final String TEXTURE_DIFFUSE = "kueken7_rgba8_srgb.dds";

    private int vertexCount = 4;
    private int vertexSize = vertexCount * Vertex_v2fv2f.SIZE;
    private float[] vertexData = {
        -1.0f, -1.0f,/**/ 0.0f, 1.0f,
        +1.0f, -1.0f,/**/ 1.0f, 1.0f,
        +1.0f, +1.0f,/**/ 1.0f, 0.0f,
        -1.0f, +1.0f,/**/ 0.0f, 0.0f};

    private int elementCount = 6;
    private int elementSize = elementCount * Short.BYTES;
    private short[] elementData = {
        0, 1, 2,
        2, 3, 0};

    private class Buffer {

        public static final int COPY = 0;
        public static final int VERTEX = 1;
        public static final int ELEMENT = 2;
        public static final int TRANSFORM = 3;
        public static final int MAX = 4;
    }

    private IntBuffer pipelineName = GLBuffers.newDirectIntBuffer(1), vertexArrayName = GLBuffers.newDirectIntBuffer(1),
            textureName = GLBuffers.newDirectIntBuffer(1), bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX);
    private int programName;
    private ByteBuffer uniformPointer;
    /**
     * https://jogamp.org/bugzilla/show_bug.cgi?id=1287
     */
    private boolean bug1287 = true;

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = gl4.isExtensionAvailable("GL_ARB_sparse_buffer");

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
            validated = initTexture(gl4);
        }
        if (validated) {
            uniformPointer = gl4.glMapNamedBufferRange(bufferName.get(Buffer.TRANSFORM), 0, Mat4.SIZE, GL_MAP_WRITE_BIT
                    | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);
        }

        return validated;
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

        int alignement = 256;
        IntBuffer bufferPageSize = GLBuffers.newDirectIntBuffer(1);        
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        
        gl4.glGetIntegerv(GL_SPARSE_BUFFER_PAGE_SIZE_ARB, bufferPageSize);

        boolean validated = true;

        int copyBufferSize = glm.ceilMultiple(vertexSize, alignement) + glm.ceilMultiple(elementSize, alignement);

        gl4.glCreateBuffers(Buffer.MAX, bufferName);

        if (!bug1287) {
            gl4.glNamedBufferStorage(bufferName.get(Buffer.COPY), copyBufferSize, null, GL_MAP_WRITE_BIT);
        } else {
            gl4.glBindBuffer(GL_SHADER_STORAGE_BUFFER, bufferName.get(Buffer.COPY));
            gl4.glBufferStorage(GL_SHADER_STORAGE_BUFFER, copyBufferSize, null, GL_MAP_WRITE_BIT);
            gl4.glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
        }

        ByteBuffer copyBufferPointer = gl4.glMapNamedBufferRange(bufferName.get(Buffer.COPY), 0, copyBufferSize,
                GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);
        copyBufferPointer.asFloatBuffer().put(vertexBuffer);
        copyBufferPointer.position(glm.ceilMultiple(vertexSize, alignement));
        copyBufferPointer.asShortBuffer().put(elementBuffer);
        gl4.glUnmapNamedBuffer(bufferName.get(Buffer.COPY));

        gl4.glBindBuffer(GL_COPY_READ_BUFFER, bufferName.get(Buffer.COPY));

        gl4.glBindBuffer(GL_COPY_WRITE_BUFFER, bufferName.get(Buffer.ELEMENT));
        gl4.glBufferStorage(GL_COPY_WRITE_BUFFER, glm.ceilMultiple(elementSize, bufferPageSize.get(0)), null,
                GL_SPARSE_STORAGE_BIT_ARB);
        gl4.glBufferPageCommitmentARB(GL_COPY_WRITE_BUFFER, 0, bufferPageSize.get(0), true);
        gl4.glCopyBufferSubData(GL_COPY_READ_BUFFER, GL_COPY_WRITE_BUFFER, glm.ceilMultiple(vertexSize, alignement),
                0, glm.ceilMultiple(elementSize, alignement));

        gl4.glBindBuffer(GL_COPY_WRITE_BUFFER, bufferName.get(Buffer.VERTEX));
        gl4.glBufferStorage(GL_COPY_WRITE_BUFFER, glm.ceilMultiple(vertexSize, bufferPageSize.get(0)), null,
                GL_SPARSE_STORAGE_BIT_ARB);
        gl4.glBufferPageCommitmentARB(GL_COPY_WRITE_BUFFER, 0, bufferPageSize.get(0), true);
        gl4.glCopyBufferSubData(GL_COPY_READ_BUFFER, GL_COPY_WRITE_BUFFER, 0, 0, glm.ceilMultiple(vertexSize, alignement));

        int[] uniformBufferOffset = {0};
        gl4.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset, 0);
        int uniformBlockSize = Math.max(Mat4.SIZE, uniformBufferOffset[0]);

        if (!bug1287) {
            gl4.glNamedBufferStorage(bufferName.get(Buffer.TRANSFORM), uniformBlockSize, null, GL_MAP_WRITE_BIT
                    | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT);
        } else {
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.TRANSFORM));
            gl4.glBufferStorage(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT
                    | GL_MAP_COHERENT_BIT);
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }

        BufferUtils.destroyDirectBuffer(vertexBuffer);
        BufferUtils.destroyDirectBuffer(elementBuffer);
        BufferUtils.destroyDirectBuffer(bufferPageSize);

        return validated;
    }

    private boolean initTexture(GL4 gl4) {

        boolean validated = true;

        try {

            jgli.Texture2d texture = new Texture2d(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE));
            jgli.Gl.Format format = jgli.Gl.translate(texture.format());

            gl4.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

            gl4.glCreateTextures(GL_TEXTURE_2D_ARRAY, 1, textureName);
            gl4.glTextureParameteri(textureName.get(0), GL_TEXTURE_SWIZZLE_R, GL_RED);
            gl4.glTextureParameteri(textureName.get(0), GL_TEXTURE_SWIZZLE_G, GL_GREEN);
            gl4.glTextureParameteri(textureName.get(0), GL_TEXTURE_SWIZZLE_B, GL_BLUE);
            gl4.glTextureParameteri(textureName.get(0), GL_TEXTURE_SWIZZLE_A, GL_ALPHA);
            gl4.glTextureParameteri(textureName.get(0), GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTextureParameteri(textureName.get(0), GL_TEXTURE_MAX_LEVEL, texture.levels() - 1);
            gl4.glTextureParameteri(textureName.get(0), GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            gl4.glTextureParameteri(textureName.get(0), GL_TEXTURE_MAG_FILTER, GL_LINEAR);

            gl4.glTextureStorage3D(textureName.get(0), texture.levels(),
                    format.internal.value,
                    texture.dimensions(0)[0], texture.dimensions(0)[1], 1);

            for (int level = 0; level < texture.levels(); ++level) {

                gl4.glTextureSubImage3D(textureName.get(0), level,
                        0, 0, 0,
                        texture.dimensions(level)[0], texture.dimensions(level)[1], 1,
                        format.external.value, format.type.value,
                        texture.data(level));
            }

        } catch (IOException ex) {
            Logger.getLogger(Gl_500_buffer_sparse_arb.class.getName()).log(Level.SEVERE, null, ex);
        }
        return validated;
    }

    private boolean initVertexArray(GL4 gl4) {

        boolean validated = true;

        gl4.glCreateVertexArrays(1, vertexArrayName);
        gl4.glVertexArrayElementBuffer(vertexArrayName.get(0), bufferName.get(Buffer.ELEMENT));

        return validated;
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        {
            Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, (float) windowSize.x / windowSize.y, 0.1f, 100.0f);
            Mat4 mvp = projection.mul(viewMat4()).mul(new Mat4(1.0f));

            uniformPointer.asFloatBuffer().put(mvp.toFa_());
        }

        gl4.glViewportIndexedf(0, 0, 0, windowSize.x, windowSize.y);
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, 0);

        gl4.glBindProgramPipeline(pipelineName.get(0));
        gl4.glBindTextureUnit(Semantic.Sampler.DIFFUSE, textureName.get(0));
        gl4.glBindVertexArray(vertexArrayName.get(0));
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName.get(Buffer.TRANSFORM));
        gl4.glBindBufferBase(GL_SHADER_STORAGE_BUFFER, Semantic.Storage.VERTEX, bufferName.get(Buffer.VERTEX));

        gl4.glDrawElementsInstancedBaseVertexBaseInstance(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0, 0);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        if (uniformPointer == null) {
            gl4.glUnmapNamedBuffer(bufferName.get(Buffer.TRANSFORM));
            BufferUtils.destroyDirectBuffer(uniformPointer);
        }

        gl4.glDeleteProgramPipelines(1, pipelineName);
        BufferUtils.destroyDirectBuffer(pipelineName);
        gl4.glDeleteProgram(programName);
        gl4.glDeleteBuffers(Buffer.MAX, bufferName);
        BufferUtils.destroyDirectBuffer(bufferName);
        gl4.glDeleteTextures(1, textureName);
        BufferUtils.destroyDirectBuffer(textureName);
        gl4.glDeleteVertexArrays(1, vertexArrayName);
        BufferUtils.destroyDirectBuffer(vertexArrayName);

        return true;
    }
}
