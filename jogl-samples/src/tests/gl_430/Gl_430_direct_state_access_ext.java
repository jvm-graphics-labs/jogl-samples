/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_430;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL2GL3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import dev.Vec2i;
import glm.mat._4.Mat4;
import framework.BufferUtils;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import glf.Vertex_v2fv2f;
import glm.glm;
import glm.vec._2.Vec2;
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
public class Gl_430_direct_state_access_ext extends Test {

    public static void main(String[] args) {
        Gl_430_direct_state_access_ext gl_430_direct_state_access_ext = new Gl_430_direct_state_access_ext();
    }

    public Gl_430_direct_state_access_ext() {
        super("gl-430-direct-state-access-ext", Profile.CORE, 4, 3, new Vec2i(640, 480), new Vec2(Math.PI * 0.1f));
    }

    private final String SHADERS_SOURCE = "direct-state-access";
    private final String SHADERS_ROOT = "src/data/gl_430";
    private final String TEXTURE_DIFFUSE = "kueken7_rgba8_srgb.dds";

    private final Vec2i FRAMEBUFFER_SIZE = new Vec2i(80, 60);

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

    private class Program {

        public static final int VERTEX = 0;
        public static final int FRAGMENT = 1;
        public static final int MAX = 2;
    }

    private class Framebuffer {

        public static final int RENDER = 0;
        public static final int RESOLVE = 1;
        public static final int MAX = 2;
    }

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int ELEMENT = 1;
        public static final int TRANSFORM = 2;
        public static final int MAX = 3;
    }

    private class Texture {

        public static final int TEXTURE = 0;
        public static final int MULTISAMPLE = 1;
        public static final int COLORBUFFER = 2;
        public static final int MAX = 3;
    }

    private IntBuffer vertexArrayName = GLBuffers.newDirectIntBuffer(1), pipelineName = GLBuffers.newDirectIntBuffer(1),
            samplerName = GLBuffers.newDirectIntBuffer(1), bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX),
            textureName = GLBuffers.newDirectIntBuffer(Texture.MAX),
            framebufferName = GLBuffers.newDirectIntBuffer(Framebuffer.MAX);
    private int programName, uniformBlockSize;

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = checkExtension(gl4, "GL_EXT_direct_state_access");

        if (validated) {
            validated = initProgram(gl4);
        }
        if (validated) {
            validated = initSampler(gl4);
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

        //glEnable(GL_SAMPLE_MASK);
        //glSampleMaski(0, 0xFF);
        return validated;
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        // Create program
        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

            shaderProgram.init(gl4);
            programName = shaderProgram.program();

            gl4.glProgramParameteri(programName, GL_PROGRAM_SEPARABLE, GL_TRUE);

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);

            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            gl4.glCreateProgramPipelines(1, pipelineName);
            gl4.glUseProgramStages(pipelineName.get(0), GL_VERTEX_SHADER_BIT | GL_FRAGMENT_SHADER_BIT, programName);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        int[] uniformBufferOffset = {0};
        gl4.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset, 0);
        uniformBlockSize = Math.max(Mat4.SIZE, uniformBufferOffset[0]);

        gl4.glCreateBuffers(Buffer.MAX, bufferName);
        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        gl4.glNamedBufferData(bufferName.get(Buffer.ELEMENT), elementSize, elementBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(elementBuffer);

        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl4.glNamedBufferData(bufferName.get(Buffer.VERTEX), vertexSize, vertexBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(vertexBuffer);

        gl4.glNamedBufferData(bufferName.get(Buffer.TRANSFORM), uniformBlockSize, null, GL_DYNAMIC_DRAW);

        return true;
    }

    private boolean initSampler(GL4 gl4) {

        gl4.glCreateSamplers(1, samplerName);
        gl4.glSamplerParameteri(samplerName.get(0), GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        gl4.glSamplerParameteri(samplerName.get(0), GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        gl4.glSamplerParameteri(samplerName.get(0), GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        gl4.glSamplerParameteri(samplerName.get(0), GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        gl4.glSamplerParameteri(samplerName.get(0), GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        gl4.glSamplerParameterfv(samplerName.get(0), GL_TEXTURE_BORDER_COLOR, new float[]{0.0f, 0.0f, 0.0f, 0.0f}, 0);
        gl4.glSamplerParameterf(samplerName.get(0), GL_TEXTURE_MIN_LOD, -1000.f);
        gl4.glSamplerParameterf(samplerName.get(0), GL_TEXTURE_MAX_LOD, 1000.f);
        gl4.glSamplerParameterf(samplerName.get(0), GL_TEXTURE_LOD_BIAS, 0.0f);
        gl4.glSamplerParameteri(samplerName.get(0), GL_TEXTURE_COMPARE_MODE, GL_NONE);
        gl4.glSamplerParameteri(samplerName.get(0), GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);

        return true;
    }

    private boolean initTexture(GL4 gl4) {

        try {
            jgli.Texture2d texture = new Texture2d(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE));
            jgli.Gl.Format format = jgli.Gl.translate(texture.format());
            jgli.Gl.Swizzles swizzles = jgli.Gl.translate(texture.swizzles());

            textureName.position(Texture.TEXTURE);
            gl4.glCreateTextures(GL_TEXTURE_2D, 1, textureName);
            gl4.glTextureParameteri(textureName.get(Texture.TEXTURE), GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTextureParameteri(textureName.get(Texture.TEXTURE), GL_TEXTURE_MAX_LEVEL, 0);
            gl4.glTextureParameteri(textureName.get(Texture.TEXTURE), GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            gl4.glTextureParameteri(textureName.get(Texture.TEXTURE), GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            gl4.glTextureParameteri(textureName.get(Texture.TEXTURE), GL_TEXTURE_SWIZZLE_R, swizzles.r.value);
            gl4.glTextureParameteri(textureName.get(Texture.TEXTURE), GL_TEXTURE_SWIZZLE_G, swizzles.g.value);
            gl4.glTextureParameteri(textureName.get(Texture.TEXTURE), GL_TEXTURE_SWIZZLE_B, swizzles.b.value);
            gl4.glTextureParameteri(textureName.get(Texture.TEXTURE), GL_TEXTURE_SWIZZLE_A, swizzles.a.value);

            gl4.glTextureStorage2D(textureName.get(Texture.TEXTURE), texture.levels(), format.internal.value,
                    texture.dimensions(0)[0], texture.dimensions(0)[1]);

            for (int level = 0; level < texture.levels(); ++level) {
                gl4.glTextureSubImage2D(textureName.get(Texture.TEXTURE),
                        level,
                        0, 0,
                        texture.dimensions(level)[0], texture.dimensions(level)[1],
                        format.external.value, format.type.value,
                        texture.data(level));
            }

            textureName.position(Texture.MULTISAMPLE);
            gl4.glCreateTextures(GL_TEXTURE_2D_MULTISAMPLE, 1, textureName);
            gl4.glTextureParameteri(textureName.get(Texture.MULTISAMPLE), GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTextureParameteri(textureName.get(Texture.MULTISAMPLE), GL_TEXTURE_MAX_LEVEL, 0);
            gl4.glTextureStorage2DMultisample(textureName.get(Texture.MULTISAMPLE), 4, GL_RGBA8, FRAMEBUFFER_SIZE.x,
                    FRAMEBUFFER_SIZE.y, false);

            textureName.position(Texture.COLORBUFFER);
            gl4.glCreateTextures(GL_TEXTURE_2D, 1, textureName);
            gl4.glTextureParameteri(textureName.get(Texture.COLORBUFFER), GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTextureParameteri(textureName.get(Texture.COLORBUFFER), GL_TEXTURE_MAX_LEVEL, 0);
            gl4.glTextureStorage2D(textureName.get(Texture.COLORBUFFER), 1, GL_RGBA8, FRAMEBUFFER_SIZE.x,
                    FRAMEBUFFER_SIZE.y);

            textureName.rewind();

        } catch (IOException ex) {
            Logger.getLogger(Gl_430_direct_state_access_ext.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    private boolean initFramebuffer(GL4 gl4) {

        gl4.glCreateFramebuffers(Framebuffer.MAX, framebufferName);
        gl4.glNamedFramebufferTexture(framebufferName.get(Framebuffer.RENDER), GL_COLOR_ATTACHMENT0,
                textureName.get(Texture.MULTISAMPLE), 0);
        gl4.glNamedFramebufferTexture(framebufferName.get(Framebuffer.RESOLVE), GL_COLOR_ATTACHMENT0,
                textureName.get(Texture.COLORBUFFER), 0);

        if (gl4.glCheckNamedFramebufferStatus(framebufferName.get(Framebuffer.RENDER), GL_FRAMEBUFFER)
                != GL_FRAMEBUFFER_COMPLETE) {
            return false;
        }
        return gl4.glCheckNamedFramebufferStatus(framebufferName.get(Framebuffer.RESOLVE), GL_FRAMEBUFFER)
                == GL_FRAMEBUFFER_COMPLETE;
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glCreateVertexArrays(1, vertexArrayName);
        // Setup the formats
        gl4.glVertexArrayAttribFormat(vertexArrayName.get(0), Semantic.Attr.POSITION, 2, GL_FLOAT, false, 0);
        gl4.glVertexArrayAttribFormat(vertexArrayName.get(0), Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, Vec2.SIZE);
        // Setup the buffer source
        gl4.glVertexArrayVertexBuffer(vertexArrayName.get(0), Semantic.Buffer.STATIC, bufferName.get(Buffer.VERTEX),
                0 * Float.BYTES, Vertex_v2fv2f.SIZE);
        // Link them up
        gl4.glVertexArrayAttribBinding(vertexArrayName.get(0), Semantic.Attr.POSITION, Semantic.Buffer.STATIC);
        gl4.glVertexArrayAttribBinding(vertexArrayName.get(0), Semantic.Attr.TEXCOORD, Semantic.Buffer.STATIC);

        gl4.glEnableVertexArrayAttrib(vertexArrayName.get(0), Semantic.Attr.POSITION);
        gl4.glEnableVertexArrayAttrib(vertexArrayName.get(0), Semantic.Attr.TEXCOORD);

        gl4.glVertexArrayElementBuffer(vertexArrayName.get(0), bufferName.get(Buffer.ELEMENT));

        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;
        {
            ByteBuffer pointer = gl4.glMapNamedBufferRange(bufferName.get(Buffer.TRANSFORM),
                    0, uniformBlockSize, GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, FRAMEBUFFER_SIZE.x / FRAMEBUFFER_SIZE.y, 0.1f,
                    100.0f);
            pointer.position(0);
            pointer.asFloatBuffer().put(projection.mul(viewMat4()).mul(new Mat4(1)).toFa_());

            // Make sure the uniform buffer is uploaded
            gl4.glUnmapNamedBuffer(bufferName.get(Buffer.TRANSFORM));
        }

        gl4.glBindProgramPipeline(pipelineName.get(0));

        // Step 1: render the scene in a multisampled framebuffer
        renderFBO(gl4);

        // Step 2: resolve MSAA
        gl4.glBindFramebuffer(GL_READ_FRAMEBUFFER, framebufferName.get(Framebuffer.RENDER));
        gl4.glBindFramebuffer(GL_DRAW_FRAMEBUFFER, framebufferName.get(Framebuffer.RESOLVE));
        gl4.glBlitFramebuffer(
                0, 0, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y,
                0, 0, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y, GL_COLOR_BUFFER_BIT, GL_NEAREST);

        // Step 3: Blit resolved colorbuffer. Resolve and blit can't be done in a single step
        gl4.glBindFramebuffer(GL_READ_FRAMEBUFFER, framebufferName.get(Framebuffer.RESOLVE));
        gl4.glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
        gl4.glBlitFramebuffer(
                0, 0, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y,
                0, 0, windowSize.x, windowSize.y, GL_COLOR_BUFFER_BIT, GL_NEAREST);

        return true;
    }

    private void renderFBO(GL4 gl4) {
        //glEnable(GL_SAMPLE_MASK);
        //glSampleMaski(0, 0xFF);

        gl4.glEnable(GL_MULTISAMPLE);
        gl4.glEnable(GL_SAMPLE_SHADING);
        gl4.glMinSampleShading(4.0f);

        gl4.glViewportIndexedf(0, 0, 0, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y);
        gl4.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName.get(Framebuffer.RENDER));
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, 0);

        gl4.glBindBufferRange(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName.get(Buffer.TRANSFORM), 0,
                uniformBlockSize);
        gl4.glBindSampler(0, samplerName.get(0));
        gl4.glBindTextureUnit(0, textureName.get(Texture.TEXTURE));
        gl4.glBindVertexArray(vertexArrayName.get(0));

        gl4.glDrawElementsInstancedBaseVertexBaseInstance(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0, 0);

        gl4.glDisable(GL_MULTISAMPLE);
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteBuffers(Buffer.MAX, bufferName);
        BufferUtils.destroyDirectBuffer(bufferName);
        gl4.glDeleteProgram(programName);
        gl4.glDeleteTextures(Texture.MAX, textureName);
        BufferUtils.destroyDirectBuffer(textureName);
        gl4.glDeleteFramebuffers(Framebuffer.MAX, framebufferName);
        BufferUtils.destroyDirectBuffer(framebufferName);
        gl4.glDeleteVertexArrays(1, vertexArrayName);
        BufferUtils.destroyDirectBuffer(vertexArrayName);
        gl4.glDeleteSamplers(1, samplerName);
        BufferUtils.destroyDirectBuffer(samplerName);

        return true;
    }
}
