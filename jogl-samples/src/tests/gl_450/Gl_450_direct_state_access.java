/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_450;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_MAP_WRITE_BIT;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER;
import com.jogamp.opengl.GL4;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import glm.glm;
import glm.mat._4.Mat4;
import dev.Vec2i;
import framework.BufferUtils;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import jgli.Target;
import glm.vec._2.Vec2;
import glm.vec._3.Vec3;
import java.nio.IntBuffer;
import jgli.Texture2d;

/**
 *
 * @author GBarbieri
 */
public class Gl_450_direct_state_access extends Test {

    public static void main(String[] args) {
        Gl_450_direct_state_access gl_450_direct_state_access = new Gl_450_direct_state_access();
    }

    public Gl_450_direct_state_access() {
        super("gl-450-direct-state-access", Profile.CORE, 4, 5, new Vec2i(640, 480), new Vec2(Math.PI * 0.2f));
    }

    private final String SHADERS_SOURCE = "direct-state-access";
    private final String SHADERS_ROOT = "src/data/gl_450";
    private final String TEXTURE_DIFFUSE = "kueken7_rgba8_srgb.dds";
    private final Vec2i FRAMEBUFFER_SIZE = new Vec2i(160, 160);

    private int vertexCount = 4;
    private int vertexSize = vertexCount * glf.Vertex_v2fc4d.SIZE;
    private float[] vertexData = {
        -1.0f, -1.0f,/**/ 0.0f, 0.0f,
        +1.0f, -1.0f,/**/ 1.0f, 0.0f,
        +1.0f, +1.0f,/**/ 1.0f, 1.0f,
        -1.0f, +1.0f,/**/ 0.0f, 1.0f};

    private int elementCount = 6;
    private int elementSize = elementCount * Short.BYTES;
    private short[] elementData = {
        0, 1, 2,
        2, 3, 0};

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
    private ByteBuffer uniformPointer;
    private FloatBuffer clearColor = GLBuffers.newDirectFloatBuffer(new float[]{0.0f, 0.5f, 1.0f, 1.0f});
    /**
     * https://jogamp.org/bugzilla/show_bug.cgi?id=1287
     */
    private boolean bug1287 = true;

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;

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

        int[] uniformBufferOffset = {0};
        gl4.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset, 0);
        uniformBlockSize = Math.max(Mat4.SIZE, uniformBufferOffset[0]);

        gl4.glCreateBuffers(Buffer.MAX, bufferName);
        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementSize);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);

        if (!bug1287) {

            gl4.glNamedBufferStorage(bufferName.get(Buffer.ELEMENT), elementSize, elementBuffer, 0);
            gl4.glNamedBufferStorage(bufferName.get(Buffer.VERTEX), vertexSize, vertexBuffer, 0);
            gl4.glNamedBufferStorage(bufferName.get(Buffer.TRANSFORM), uniformBlockSize * 2, null, GL_MAP_WRITE_BIT
                    | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT);
        } else {

            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT));
            gl4.glBufferStorage(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, 0);
            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
            gl4.glBufferStorage(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, 0);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.TRANSFORM));
            gl4.glBufferStorage(GL_UNIFORM_BUFFER, uniformBlockSize * 2, null, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT
                    | GL_MAP_COHERENT_BIT);
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }
        BufferUtils.destroyDirectBuffer(elementBuffer);
        BufferUtils.destroyDirectBuffer(vertexBuffer);

        uniformPointer = gl4.glMapNamedBufferRange(bufferName.get(Buffer.TRANSFORM), 0, uniformBlockSize * 2,
                GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

        return true;
    }

    private boolean initSampler(GL4 gl4) {

        gl4.glCreateSamplers(1, samplerName);
        gl4.glSamplerParameteri(samplerName.get(0), GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_NEAREST);
        gl4.glSamplerParameteri(samplerName.get(0), GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        gl4.glSamplerParameteri(samplerName.get(0), GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        gl4.glSamplerParameteri(samplerName.get(0), GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        gl4.glSamplerParameteri(samplerName.get(0), GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        FloatBuffer borderColorBuffer = GLBuffers.newDirectFloatBuffer(new float[]{0.0f, 0.0f, 0.0f, 0.0f});
        gl4.glSamplerParameterfv(samplerName.get(0), GL_TEXTURE_BORDER_COLOR, borderColorBuffer);
        BufferUtils.destroyDirectBuffer(borderColorBuffer);
        gl4.glSamplerParameterf(samplerName.get(0), GL_TEXTURE_MIN_LOD, -1000.f);
        gl4.glSamplerParameterf(samplerName.get(0), GL_TEXTURE_MAX_LOD, 1000.f);
        gl4.glSamplerParameterf(samplerName.get(0), GL_TEXTURE_LOD_BIAS, 0.0f);
        gl4.glSamplerParameteri(samplerName.get(0), GL_TEXTURE_COMPARE_MODE, GL_NONE);
        gl4.glSamplerParameteri(samplerName.get(0), GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);

        return true;
    }

    private boolean initTexture(GL4 gl4) {

        //        textureName.put(Texture.TEXTURE, createTexture(gl4, TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE));
        try {
            jgli.Texture2d texture = new Texture2d(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE));
            if (texture.empty()) {
                return false;
            }

            jgli.Gl.Format format = jgli.Gl.translate(texture.format());

            textureName.position(Texture.TEXTURE);
            gl4.glCreateTextures(GL_TEXTURE_2D, 1, textureName);
            gl4.glTextureParameteri(textureName.get(Texture.TEXTURE), GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTextureParameteri(textureName.get(Texture.TEXTURE), GL_TEXTURE_MAX_LEVEL, texture.levels() - 1);
            gl4.glTextureParameteri(textureName.get(Texture.TEXTURE), GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            gl4.glTextureParameteri(textureName.get(Texture.TEXTURE), GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            gl4.glTextureStorage2D(textureName.get(Texture.TEXTURE), texture.levels(), format.internal.value,
                    texture.dimensions(0)[0], texture.dimensions(0)[1]);
            for (int level = 0; level < texture.levels(); ++level) {
                gl4.glTextureSubImage2D(textureName.get(Texture.TEXTURE), level,
                        0, 0,
                        texture.dimensions(level)[0], texture.dimensions(level)[1],
                        format.external.value, format.type.value,
                        texture.data(level));
            }
        } catch (IOException ex) {
            Logger.getLogger(Gl_450_direct_state_access.class.getName()).log(Level.SEVERE, null, ex);
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

        return true;
    }
    
    private boolean initFramebuffer(GL4 gl4) {

        gl4.glCreateFramebuffers(Framebuffer.MAX, framebufferName);
        gl4.glNamedFramebufferTexture(framebufferName.get(Framebuffer.RENDER), GL_COLOR_ATTACHMENT0,
                textureName.get(Texture.MULTISAMPLE), 0);
        gl4.glNamedFramebufferTexture(framebufferName.get(Framebuffer.RESOLVE), GL_COLOR_ATTACHMENT0,
                textureName.get(Texture.COLORBUFFER), 0);

        if (!isFramebufferComplete(gl4, framebufferName.get(Framebuffer.RENDER))) {
            return false;
        }
        if (!isFramebufferComplete(gl4, framebufferName.get(Framebuffer.RESOLVE))) {
            return false;
        }

        IntBuffer params = GLBuffers.newDirectIntBuffer(1);
        gl4.glGetNamedFramebufferParameteriv(framebufferName.get(Framebuffer.RENDER), GL_SAMPLES, params);
        int samples = params.get(0);
        BufferUtils.destroyDirectBuffer(params);
        return samples == 4;
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glCreateVertexArrays(1, vertexArrayName);

        gl4.glVertexArrayAttribBinding(vertexArrayName.get(0), Semantic.Attr.POSITION, Semantic.Buffer.STATIC);
        gl4.glVertexArrayAttribFormat(vertexArrayName.get(0), Semantic.Attr.POSITION, 2, GL_FLOAT, false, 0);
        gl4.glEnableVertexArrayAttrib(vertexArrayName.get(0), Semantic.Attr.POSITION);

        gl4.glVertexArrayAttribBinding(vertexArrayName.get(0), Semantic.Attr.TEXCOORD, Semantic.Buffer.STATIC);
        gl4.glVertexArrayAttribFormat(vertexArrayName.get(0), Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, Vec2.SIZE);
        gl4.glEnableVertexArrayAttrib(vertexArrayName.get(0), Semantic.Attr.TEXCOORD);

        gl4.glVertexArrayElementBuffer(vertexArrayName.get(0), bufferName.get(Buffer.ELEMENT));
        gl4.glVertexArrayVertexBuffer(vertexArrayName.get(0), Semantic.Buffer.STATIC, bufferName.get(Buffer.VERTEX), 0,
                glf.Vertex_v2fv2f.SIZE);

        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        {
            Mat4 projectionA = glm.perspective_((float) Math.PI * 0.25f, (float) FRAMEBUFFER_SIZE.x / FRAMEBUFFER_SIZE.y,
                    0.1f, 100.0f).scale(new Vec3(1, -1, 1));
            uniformPointer.asFloatBuffer().put(projectionA.mul(viewMat4()).mul(new Mat4(1)).toFa_());

            Mat4 projectionB = glm.perspective_((float) Math.PI * 0.25f, (float) windowSize.x / windowSize.y, 0.1f, 100.0f);
            uniformPointer.position(uniformBlockSize);
            uniformPointer.asFloatBuffer().put(projectionB.mul(viewMat4()).scale(new Vec3(2), new Mat4(1)).toFa_());
            uniformPointer.rewind();
        }

        // Step 1, render the scene in a multisampled framebuffer
        gl4.glBindProgramPipeline(pipelineName.get(0));

        renderFBO(gl4);

        // Step 2: blit
        gl4.glBlitNamedFramebuffer(framebufferName.get(Framebuffer.RENDER), framebufferName.get(Framebuffer.RESOLVE),
                0, 0, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y,
                0, 0, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y,
                GL_COLOR_BUFFER_BIT, GL_NEAREST);

        int[] maxColorAttachment = {GL_COLOR_ATTACHMENT0};
        gl4.glInvalidateNamedFramebufferData(framebufferName.get(Framebuffer.RENDER), 1, maxColorAttachment, 0);

        // Step 3, render the colorbuffer from the multisampled framebuffer
        renderFB(gl4);

        return true;
    }

    private void renderFBO(GL4 gl4) {

        gl4.glEnable(GL_MULTISAMPLE);
        gl4.glEnable(GL_SAMPLE_SHADING);
        gl4.glMinSampleShading(4 / 4.0f);

        gl4.glClipControl(GL_LOWER_LEFT, GL_NEGATIVE_ONE_TO_ONE);
        gl4.glViewportIndexedf(0, 0, 0, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y);

        gl4.glClearNamedFramebufferfv(framebufferName.get(Framebuffer.RENDER), GL_COLOR, 0, clearColor);

        gl4.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName.get(Framebuffer.RENDER));
        gl4.glBindBufferRange(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName.get(Buffer.TRANSFORM), 0,
                uniformBlockSize);
        gl4.glBindSamplers(0, 1, samplerName);
        gl4.glBindTextureUnit(0, textureName.get(Texture.TEXTURE));
        gl4.glBindVertexArray(vertexArrayName.get(0));

        gl4.glDrawElementsInstancedBaseVertexBaseInstance(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0, 0);

        gl4.glDisable(GL_MULTISAMPLE);
    }

    private void renderFB(GL4 gl4) {

        gl4.glClipControl(GL_LOWER_LEFT, GL_NEGATIVE_ONE_TO_ONE);
        gl4.glViewportIndexedf(0, 0, 0, windowSize.x, windowSize.y);
        gl4.glClearNamedFramebufferfv(0, GL_COLOR, 0, new float[]{0.0f, 0.5f, 1.0f, 1.0f}, 0);

        gl4.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        gl4.glBindBufferRange(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName.get(Buffer.TRANSFORM),
                uniformBlockSize, uniformBlockSize);
        gl4.glBindTextureUnit(0, textureName.get(Texture.COLORBUFFER));
        gl4.glBindVertexArray(vertexArrayName.get(0));

        gl4.glDrawElementsInstancedBaseVertexBaseInstance(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0, 0);
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glUnmapNamedBuffer(bufferName.get(Buffer.TRANSFORM));
        BufferUtils.destroyDirectBuffer(bufferName);

        gl4.glDeleteProgramPipelines(1, pipelineName);
        BufferUtils.destroyDirectBuffer(pipelineName);
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

        BufferUtils.destroyDirectBuffer(clearColor);

        return true;
    }

}
