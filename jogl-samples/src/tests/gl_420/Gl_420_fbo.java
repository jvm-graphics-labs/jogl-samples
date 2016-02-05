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
import core.glm;
import dev.Mat4;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import jgli.Texture2d;
import dev.Vec2;

/**
 *
 * @author GBarbieri
 */
public class Gl_420_fbo extends Test {

    public static void main(String[] args) {
        Gl_420_fbo gl_420_fbo = new Gl_420_fbo();
    }

    public Gl_420_fbo() {
        super("gl-420-fbo", Profile.CORE, 4, 2, new Vec2(Math.PI * 0.2f));
    }

    private final String SHADERS_SOURCE_TEXTURE = "fbo-texture-2d";
    private final String SHADERS_SOURCE_SPLASH = "fbo-splash";
    private final String SHADERS_ROOT = "src/data/gl_420";
    private final String TEXTURE_DIFFUSE = "kueken7_rgb_dxt1_unorm.dds";

    private int vertexCount = 4;
    private int vertexSize = vertexCount * 2 * 2 * Float.BYTES;
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

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int ELEMENT = 1;
        public static final int TRANSFORM = 2;
        public static final int MAX = 3;
    }

    private class Texture {

        public static final int DIFFUSE = 0;
        public static final int COLORBUFFER = 1;
        public static final int RENDERBUFFER = 2;
        public static final int MAX = 3;
    }

    private class Pipeline {

        public static final int TEXTURE = 0;
        public static final int SPLASH = 1;
        public static final int MAX = 2;
    }

    private int[] framebufferName = {0}, programName = new int[Pipeline.MAX], vertexArrayName = new int[Pipeline.MAX],
            bufferName = new int[Buffer.MAX], textureName = new int[Texture.MAX], pipelineName = new int[Pipeline.MAX];

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;

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
            validated = initFramebuffer(gl4);
        }

        return validated;
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        gl4.glGenProgramPipelines(Pipeline.MAX, pipelineName, 0);

        // Create program
        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_TEXTURE, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_TEXTURE, "frag", null, true);

            shaderProgram.init(gl4);
            programName[Pipeline.TEXTURE] = shaderProgram.program();

            gl4.glProgramParameteri(programName[Pipeline.TEXTURE], GL_PROGRAM_SEPARABLE, GL_TRUE);

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);

            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            gl4.glUseProgramStages(pipelineName[Pipeline.TEXTURE],
                    GL_VERTEX_SHADER_BIT | GL_FRAGMENT_SHADER_BIT, programName[Pipeline.TEXTURE]);
        }

        // Create program
        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_SPLASH, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_SPLASH, "frag", null, true);

            shaderProgram.init(gl4);
            programName[Pipeline.SPLASH] = shaderProgram.program();

            gl4.glProgramParameteri(programName[Pipeline.SPLASH], GL_PROGRAM_SEPARABLE, GL_TRUE);

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);

            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            gl4.glUseProgramStages(pipelineName[Pipeline.SPLASH],
                    GL_VERTEX_SHADER_BIT | GL_FRAGMENT_SHADER_BIT, programName[Pipeline.SPLASH]);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        gl4.glGenBuffers(Buffer.MAX, bufferName, 0);

        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT]);
        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl4.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        int[] uniformBufferOffset = {0};

        gl4.glGetIntegerv(
                GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT,
                uniformBufferOffset, 0);

        int uniformBlockSize = Math.max(Mat4.SIZE, uniformBufferOffset[0]);

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
        gl4.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_DYNAMIC_DRAW);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return true;
    }

    private boolean initTexture(GL4 gl4) {

        boolean validated = true;

        try {

            jgli.Texture2d texture = new Texture2d(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE));
            assert (!texture.empty());

            gl4.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

            gl4.glGenTextures(Texture.MAX, textureName, 0);

            gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.DIFFUSE]);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, texture.levels() - 1);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_NEAREST);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_R, GL_RED);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_G, GL_GREEN);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_B, GL_BLUE);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_A, GL_ALPHA);
            gl4.glTexStorage2D(GL_TEXTURE_2D, texture.levels(), GL_COMPRESSED_RGB_S3TC_DXT1_EXT,
                    texture.dimensions()[0], texture.dimensions()[1]);

            for (int level = 0; level < texture.levels(); ++level) {
                gl4.glCompressedTexSubImage2D(
                        GL_TEXTURE_2D,
                        level,
                        0, 0,
                        texture.dimensions(level)[0],
                        texture.dimensions(level)[1],
                        GL_COMPRESSED_RGB_S3TC_DXT1_EXT,
                        texture.size(level),
                        texture.data(level));
            }

            gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.COLORBUFFER]);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
            gl4.glTexStorage2D(GL_TEXTURE_2D, 1, GL_RGBA8, windowSize.x, windowSize.y);

            gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.RENDERBUFFER]);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
            gl4.glTexStorage2D(GL_TEXTURE_2D, 1, GL_DEPTH_COMPONENT24, windowSize.x, windowSize.y);

            gl4.glPixelStorei(GL_UNPACK_ALIGNMENT, 4);

        } catch (IOException ex) {
            Logger.getLogger(Gl_420_fbo.class.getName()).log(Level.SEVERE, null, ex);
        }
        return validated;
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(Pipeline.MAX, vertexArrayName, 0);
        gl4.glBindVertexArray(vertexArrayName[Pipeline.TEXTURE]);
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

        gl4.glBindVertexArray(vertexArrayName[Pipeline.SPLASH]);
        gl4.glBindVertexArray(0);

        return true;
    }

    private boolean initFramebuffer(GL4 gl4) {

        gl4.glGenFramebuffers(1, framebufferName, 0);
        gl4.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[0]);
        gl4.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, textureName[Texture.COLORBUFFER], 0);
        gl4.glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, textureName[Texture.RENDERBUFFER], 0);

        if (!isFramebufferComplete(gl4, framebufferName[0])) {
            return false;
        }

        gl4.glBindFramebuffer(GL_FRAMEBUFFER, 0);
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

            //glm::mat4 Projection = glm::perspectiveFov(glm::pi<float>() * 0.25f, 640.f, 480.f, 0.1f, 100.0f);
            Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, (float) windowSize.x / windowSize.y, 0.1f, 100.0f);
            Mat4 model = new Mat4(1.0f);

            pointer.asFloatBuffer().put(projection.mul(viewMat4()).mul(model).toFa_());

            // Make sure the uniform buffer is uploaded
            gl4.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        gl4.glEnable(GL_DEPTH_TEST);
        gl4.glDepthFunc(GL_LESS);

        gl4.glViewportIndexedf(0, 0, 0, windowSize.x, windowSize.y);

        gl4.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[0]);
        float[] depth = {1.0f};
        gl4.glClearBufferfv(GL_DEPTH, 0, depth, 0);
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 0.5f, 0.0f, 1.0f}, 0);

        // Bind rendering objects
        gl4.glBindProgramPipeline(pipelineName[Pipeline.TEXTURE]);
        gl4.glActiveTexture(GL_TEXTURE0);
        gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.DIFFUSE]);
        gl4.glBindVertexArray(vertexArrayName[Pipeline.TEXTURE]);
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.TRANSFORM]);

        gl4.glDrawElementsInstancedBaseVertexBaseInstance(
                GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 2, 0, 0);

        gl4.glDisable(GL_DEPTH_TEST);

        gl4.glBindFramebuffer(GL_FRAMEBUFFER, 0);

        gl4.glBindProgramPipeline(pipelineName[Pipeline.SPLASH]);
        gl4.glActiveTexture(GL_TEXTURE0);
        gl4.glBindVertexArray(vertexArrayName[Pipeline.SPLASH]);
        gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.COLORBUFFER]);

        gl4.glDrawArraysInstancedBaseInstance(GL_TRIANGLES, 0, 3, 1, 0);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteProgramPipelines(Pipeline.MAX, pipelineName, 0);
        gl4.glDeleteProgram(programName[Pipeline.SPLASH]);
        gl4.glDeleteProgram(programName[Pipeline.TEXTURE]);
        gl4.glDeleteBuffers(Buffer.MAX, bufferName, 0);
        gl4.glDeleteFramebuffers(1, framebufferName, 0);
        gl4.glDeleteTextures(Texture.MAX, textureName, 0);
        gl4.glDeleteVertexArrays(Pipeline.MAX, vertexArrayName, 0);

        return true;
    }
}
