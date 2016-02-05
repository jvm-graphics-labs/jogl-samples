/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_430;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL3ES3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import core.glm;
import dev.Mat4;
import framework.BufferUtils;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import jgli.Texture2d;

/**
 *
 * @author GBarbieri
 */
public class Gl_430_program_subroutine extends Test {

    public static void main(String[] args) {
        Gl_430_program_subroutine gl_430_program_subroutine = new Gl_430_program_subroutine();
    }

    public Gl_430_program_subroutine() {
        super("gl-430-program-subroutine", Profile.CORE, 4, 3);
    }

    private final String SHADERS_SOURCE = "program-subroutine";
    private final String SHADERS_ROOT = "src/data/gl_430";
    private final String TEXTURE_DIFFUSE_RGB8 = "kueken7_rgba8_srgb.dds";
    private final String TEXTURE_DIFFUSE_DXT1 = "kueken7_rgb_dxt1_unorm.dds";

    private int vertexCount = 4;
    private int vertexSize = vertexCount * 2 * 2 * Float.BYTES;
    private float[] vertexData = {
        -1.0f, -1.0f, 0.0f, 1.0f,
        +1.0f, -1.0f, 1.0f, 1.0f,
        +1.0f, +1.0f, 1.0f, 0.0f,
        -1.0f, +1.0f, 0.0f, 0.0f};

    private int elementCount = 6;
    private int elementSize = elementCount * Integer.BYTES;
    private int[] elementData = {
        0, 1, 2,
        2, 3, 0};

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int ELEMENT = 1;
        public static final int TRANSFORM = 2;
        public static final int MAX = 3;
    }

    private class Texture {

        public static final int DXT1 = 0;
        public static final int RGB8 = 1;
        public static final int MAX = 2;
    }

    private class Sampler {

        public static final int DXT1 = 0;
        public static final int RGB8 = 1;
        public static final int MAX = 2;
    }

    private class Sampling {

        public static final int DXT1 = 0;
        public static final int RGB8 = 1;
        public static final int MAX = 2;
    }

    private int[] pipelineName = {0}, vertexArrayName = {0}, textureName = new int[Texture.MAX],
            bufferName = new int[Buffer.MAX];
    private int programName;

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;
        validated = validated && checkExtension(gl4, "GL_ARB_explicit_uniform_location");

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
        if (validated) {
            validated = initTexture(gl4);
        }

        return validated;
    }

    private boolean initTest(GL4 gl4) {

        gl4.glEnable(GL_DEPTH_TEST);

        return checkError(gl4, "initTest");
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        gl4.glGenProgramPipelines(1, pipelineName, 0);

        // Create shaders
        if (validated) {

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            ShaderCode geomShaderCode = ShaderCode.create(gl4, GL_GEOMETRY_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "geom", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

            if (validated) {

                ShaderProgram shaderProgram = new ShaderProgram();
                shaderProgram.init(gl4);

                programName = shaderProgram.program();

                gl4.glProgramParameteri(programName, GL_PROGRAM_SEPARABLE, GL_TRUE);

                shaderProgram.add(vertShaderCode);
                shaderProgram.add(geomShaderCode);
                shaderProgram.add(fragShaderCode);

                shaderProgram.link(gl4, System.out);
            }
        }

        if (validated) {

            gl4.glUseProgramStages(pipelineName[0], GL_VERTEX_SHADER_BIT | GL_GEOMETRY_SHADER_BIT
                    | GL_FRAGMENT_SHADER_BIT, programName);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        gl4.glGenBuffers(Buffer.MAX, bufferName, 0);

        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT]);
        IntBuffer elementBuffer = GLBuffers.newDirectIntBuffer(elementData);
        gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(elementBuffer);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl4.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(vertexBuffer);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        int[] uniformBufferOffset = {0};

        gl4.glGetIntegerv(
                GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT,
                uniformBufferOffset, 0);

        int uniformBlockSize = Math.max(Mat4.SIZE, uniformBufferOffset[0]);

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
        gl4.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_DYNAMIC_COPY);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return true;
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(1, vertexArrayName, 0);
        gl4.glBindVertexArray(vertexArrayName[0]);
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 2 * 2 * Float.BYTES, 0);
            gl4.glVertexAttribPointer(Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, 2 * 2 * Float.BYTES, 2 * Float.BYTES);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl4.glEnableVertexAttribArray(Semantic.Attr.TEXCOORD);

            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT]);
        }
        gl4.glBindVertexArray(0);

        return true;
    }

    private boolean initTexture(GL4 gl4) {

        try {
            gl4.glGenTextures(Texture.MAX, textureName, 0);

            {

                jgli.Texture2d texture = new Texture2d(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE_RGB8));
                assert (!texture.empty());

                jgli.Gl.Format format = jgli.Gl.translate(texture.format());
                jgli.Gl.Swizzles swizzles = jgli.Gl.translate(texture.swizzles());

                gl4.glActiveTexture(GL_TEXTURE0);
                gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.RGB8]);
                gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
                gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, texture.levels());
                gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_R, swizzles.r.value);
                gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_G, swizzles.g.value);
                gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_B, swizzles.b.value);
                gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_A, swizzles.a.value);
                for (int level = 0; level < texture.levels(); ++level) {
                    gl4.glTexImage2D(GL_TEXTURE_2D, level,
                            format.internal.value,
                            texture.dimensions(level)[0], texture.dimensions(level)[1],
                            0,
                            format.external.value, format.type.value,
                            texture.data(level));
                }
            }

            {
                jgli.Texture2d texture = new Texture2d(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE_DXT1));
                assert (!texture.empty());

                jgli.Gl.Format format = jgli.Gl.translate(texture.format());

                gl4.glActiveTexture(GL_TEXTURE0);
                gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.DXT1]);
                gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
                gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, texture.levels());
                gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_R, GL_RED);
                gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_G, GL_GREEN);
                gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_B, GL_BLUE);
                gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_A, GL_ALPHA);
                for (int level = 0; level < texture.levels(); ++level) {
                    gl4.glCompressedTexImage2D(GL_TEXTURE_2D, level,
                            format.internal.value,
                            texture.dimensions(level)[0],
                            texture.dimensions(level)[1],
                            0,
                            texture.size(level),
                            texture.data(level));
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Gl_430_program_subroutine.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        {
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
            ByteBuffer pointer = gl4.glMapBufferRange(
                    GL_UNIFORM_BUFFER, 0, Mat4.SIZE,
                    GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, windowSize.x * 0.5f / windowSize.y, 0.1f, 100.0f);
            Mat4 model = new Mat4(1.0f);

            pointer.asFloatBuffer().put(projection.mul(viewMat4()).mul(model).toFa_());

            // Make sure the uniform buffer is uploaded
            gl4.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        float[] depth = {1.0f};
        gl4.glClearBufferfv(GL_DEPTH, 0, depth, 0);
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 0.5f, 0.0f, 1.0f}, 0);

        gl4.glActiveTexture(GL_TEXTURE0 + Sampler.RGB8);
        gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.RGB8]);

        gl4.glActiveTexture(GL_TEXTURE0 + Sampler.DXT1);
        gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.DXT1]);

        gl4.glBindProgramPipeline(pipelineName[0]);
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.TRANSFORM]);
        gl4.glBindVertexArray(vertexArrayName[0]);

        gl4.glUseProgram(programName);
        int[] index = new int[2];
        index[0] = Sampling.RGB8;
        index[1] = Sampling.DXT1;

        gl4.glViewportIndexedf(0, 0, 0, windowSize.x / 2.0f, windowSize.y);
        gl4.glUniformSubroutinesuiv(GL_FRAGMENT_SHADER, 1, index, 0);
        gl4.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_INT, 0, 1, 0);

        gl4.glViewportIndexedf(0, windowSize.x / 2.0f, 0, windowSize.x / 2.0f, windowSize.y);
        gl4.glUniformSubroutinesuiv(GL_FRAGMENT_SHADER, 1, index, 1);
        gl4.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_INT, 0, 1, 0);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteVertexArrays(1, vertexArrayName, 0);
        gl4.glDeleteBuffers(Buffer.MAX, bufferName, 0);
        gl4.glDeleteTextures(Texture.MAX, textureName, 0);
        gl4.glDeleteProgram(programName);
        gl4.glDeleteProgramPipelines(1, pipelineName, 0);

        return true;
    }
}
