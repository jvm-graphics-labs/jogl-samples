/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_440;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_ALPHA;
import static com.jogamp.opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_LINEAR;
import static com.jogamp.opengl.GL.GL_LINEAR_MIPMAP_LINEAR;
import static com.jogamp.opengl.GL.GL_MAP_INVALIDATE_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_MAP_WRITE_BIT;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_TRUE;
import static com.jogamp.opengl.GL.GL_UNPACK_ALIGNMENT;
import static com.jogamp.opengl.GL.GL_UNSIGNED_SHORT;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER_BIT;
import static com.jogamp.opengl.GL2ES2.GL_PROGRAM_SEPARABLE;
import static com.jogamp.opengl.GL2ES2.GL_RED;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER_BIT;
import static com.jogamp.opengl.GL2ES3.GL_BLUE;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import static com.jogamp.opengl.GL2ES3.GL_COPY_READ_BUFFER;
import static com.jogamp.opengl.GL2ES3.GL_COPY_WRITE_BUFFER;
import static com.jogamp.opengl.GL2ES3.GL_GREEN;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_2D_ARRAY;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_BASE_LEVEL;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_MAX_LEVEL;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_SWIZZLE_A;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_SWIZZLE_B;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_SWIZZLE_G;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_SWIZZLE_R;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT;
import static com.jogamp.opengl.GL3ES3.GL_SHADER_STORAGE_BUFFER;
import com.jogamp.opengl.GL4;
import static com.jogamp.opengl.GL4.GL_MAP_COHERENT_BIT;
import static com.jogamp.opengl.GL4.GL_MAP_PERSISTENT_BIT;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import core.glm;
import dev.Mat4;
import dev.Vec2;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import jgli.Texture2d;

/**
 *
 * @author GBarbieri
 */
public class Gl_440_buffer_storage extends Test {

    public static void main(String[] args) {
        Gl_440_buffer_storage gl_440_buffer_storage = new Gl_440_buffer_storage();
    }

    public Gl_440_buffer_storage() {
        super("gl-430-buffer-storage", Profile.CORE, 4, 4);
    }

    private final String SHADERS_SOURCE = "buffer-storage";
    private final String SHADERS_ROOT = "src/data/gl_440";
    private final String TEXTURE_DIFFUSE = "kueken7_rgba8_srgb.dds";

    private int vertexCount = 4;
    private int vertexSize = vertexCount * 2 * Vec2.SIZEOF;
    private float[] vertexData = {
        -1.0f, -1.0f, 0.0f, 1.0f,
        +1.0f, -1.0f, 1.0f, 1.0f,
        +1.0f, +1.0f, 1.0f, 0.0f,
        -1.0f, +1.0f, 0.0f, 0.0f};

    private int elementCount = 6;
    private int elementSize = elementCount * Short.BYTES;
    private short[] elementData = {
        0, 1, 2,
        2, 3, 0};

    private enum Buffer {
        COPY,
        VERTEX,
        ELEMENT,
        TRANSFORM,
        MAX
    }

    private int[] pipelineName = {0}, vertexArrayName = {0}, textureName = {0}, bufferName = new int[Buffer.MAX.ordinal()];
    private int programName;
    private ByteBuffer uniformPointer;

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;
        validated = validated && checkExtension(gl4, "GL_ARB_buffer_storage");
        validated = validated && checkExtension(gl4, "GL_ARB_shader_storage_buffer_object");

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
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM.ordinal()]);
            uniformPointer = gl4.glMapBufferRange(
                    GL_UNIFORM_BUFFER, 0, Mat4.SIZEOF,
                    GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);
        }

        return validated;
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        // Create program
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

            gl4.glGenProgramPipelines(1, pipelineName, 0);
            gl4.glUseProgramStages(pipelineName[0], GL_VERTEX_SHADER_BIT | GL_FRAGMENT_SHADER_BIT, programName);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        int alignement = 256;

        boolean validated = true;

        gl4.glGenBuffers(Buffer.MAX.ordinal(), bufferName, 0);

        int copyBufferSize = glm.ceilMultiple(vertexSize, alignement) + glm.ceilMultiple(elementSize, alignement);

        gl4.glBindBuffer(GL_COPY_READ_BUFFER, bufferName[Buffer.COPY.ordinal()]);
        gl4.glBufferStorage(GL_COPY_READ_BUFFER, copyBufferSize, null, GL_MAP_WRITE_BIT);

        ByteBuffer copyBufferPointer = gl4.glMapBufferRange(GL_COPY_READ_BUFFER, 0, copyBufferSize,
                GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);
        copyBufferPointer.asFloatBuffer().put(vertexData);
        copyBufferPointer.position(glm.ceilMultiple(vertexSize, alignement));
        copyBufferPointer.asShortBuffer().put(elementData);
        copyBufferPointer.rewind();
//        for (int i = 0; i < 16; i++) {
//            System.out.println("copyBufferPointer[" + i + "]: " + copyBufferPointer.getFloat(i * Float.BYTES));
//        }
//        for (int i = 0; i < 6; i++) {
//            System.out.println("copyBufferPointer[" + (i + alignement) + "]: " + 
//                    copyBufferPointer.getShort(alignement + i * Short.BYTES));
//        }
        gl4.glUnmapBuffer(GL_COPY_READ_BUFFER);

        gl4.glBindBuffer(GL_COPY_WRITE_BUFFER, bufferName[Buffer.ELEMENT.ordinal()]);
        gl4.glBufferStorage(GL_COPY_WRITE_BUFFER, glm.ceilMultiple(elementSize, alignement), null, 0);
        gl4.glCopyBufferSubData(GL_COPY_READ_BUFFER, GL_COPY_WRITE_BUFFER, glm.ceilMultiple(vertexSize, alignement),
                0, glm.ceilMultiple(elementSize, alignement));

        gl4.glBindBuffer(GL_COPY_WRITE_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);
        gl4.glBufferStorage(GL_COPY_WRITE_BUFFER, glm.ceilMultiple(vertexSize, alignement), null, 0);
        gl4.glCopyBufferSubData(GL_COPY_READ_BUFFER, GL_COPY_WRITE_BUFFER, 0, 0,
                glm.ceilMultiple(vertexSize, alignement));

        int[] uniformBufferOffset = {0};
        gl4.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset, 0);
        int uniformBlockSize = Math.max(Mat4.SIZEOF, uniformBufferOffset[0]);

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM.ordinal()]);
        gl4.glBufferStorage(GL_UNIFORM_BUFFER, uniformBlockSize, null,
                GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return validated;
    }

    private boolean initTexture(GL4 gl4) {

        try {
            jgli.Texture2d texture = new Texture2d(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE));
            jgli.Gl.Format format = jgli.Gl.translate(texture.format());

            gl4.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

            gl4.glGenTextures(1, textureName, 0);
            gl4.glActiveTexture(GL_TEXTURE0);
            gl4.glBindTexture(GL_TEXTURE_2D_ARRAY, textureName[0]);
            gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SWIZZLE_R, GL_RED);
            gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SWIZZLE_G, GL_GREEN);
            gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SWIZZLE_B, GL_BLUE);
            gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SWIZZLE_A, GL_ALPHA);
            gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAX_LEVEL, texture.levels() - 1);
            gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

            gl4.glTexStorage3D(GL_TEXTURE_2D_ARRAY, texture.levels(),
                    format.internal.value,
                    texture.dimensions(0)[0], texture.dimensions(0)[1], 1);

            for (int level = 0; level < texture.levels(); ++level) {

                gl4.glTexSubImage3D(GL_TEXTURE_2D_ARRAY, level,
                        0, 0, 0,
                        texture.dimensions(level)[0], texture.dimensions(level)[1], 1,
                        format.external.value, format.type.value,
                        texture.data(level));
            }

            gl4.glPixelStorei(GL_UNPACK_ALIGNMENT, 4);

        } catch (IOException ex) {
            Logger.getLogger(Gl_440_buffer_storage.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    private boolean initVertexArray(GL4 gl4) {

        boolean validated = true;

        gl4.glGenVertexArrays(1, vertexArrayName, 0);
        gl4.glBindVertexArray(vertexArrayName[0]);
        {
            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT.ordinal()]);
        }
        gl4.glBindVertexArray(0);

        return validated;
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        {
            Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, (float) windowSize.x / windowSize.y, 0.1f, 100.0f);
            Mat4 mvp = projection.mul(viewMat4()).mul(new Mat4(1.0f));

            uniformPointer.asFloatBuffer().put(mvp.toFA_()).rewind();
        }

        gl4.glViewportIndexedf(0, 0, 0, windowSize.x, windowSize.y);
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, 0);

        gl4.glBindProgramPipeline(pipelineName[0]);
        gl4.glActiveTexture(GL_TEXTURE0 + Semantic.Sampler.DIFFUSE);
        gl4.glBindTexture(GL_TEXTURE_2D_ARRAY, textureName[0]);
        gl4.glBindVertexArray(vertexArrayName[0]);
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.TRANSFORM.ordinal()]);
        gl4.glBindBufferBase(GL_SHADER_STORAGE_BUFFER, Semantic.Storage.VERTEX, bufferName[Buffer.VERTEX.ordinal()]);

        gl4.glDrawElementsInstancedBaseVertexBaseInstance(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0, 0);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        if (uniformPointer == null) {

            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM.ordinal()]);
            gl4.glUnmapBuffer(GL_UNIFORM_BUFFER);
            uniformPointer = null;
        }

        gl4.glDeleteProgramPipelines(1, pipelineName, 0);
        gl4.glDeleteProgram(programName);
        gl4.glDeleteBuffers(Buffer.MAX.ordinal(), bufferName, 0);
        gl4.glDeleteTextures(1, textureName, 0);
        gl4.glDeleteVertexArrays(1, vertexArrayName, 0);

        return true;
    }
}
