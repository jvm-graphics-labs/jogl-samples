/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_430;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2GL3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.BufferUtils;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import jgli.Texture2d;
import jglm.Vec2;
import jglm.Vec2i;

/**
 *
 * @author GBarbieri
 */
public class Gl_430_direct_state_access_ext extends Test {

    public static void main(String[] args) {
        Gl_430_direct_state_access_ext gl_430_direct_state_access_ext = new Gl_430_direct_state_access_ext();
    }

    public Gl_430_direct_state_access_ext() {
        super("gl-430-direct-state-access-ext", Profile.CORE, 4, 3, new Vec2i(640, 480),
                new Vec2((float) Math.PI * 0.1f, (float) Math.PI * 0.1f));
    }

    private final String SHADERS_SOURCE = "direct-state-access";
    private final String SHADERS_ROOT = "src/data/gl_430";
    private final String TEXTURE_DIFFUSE = "kueken7_rgba8_srgb.dds";

    private final Vec2i FRAMEBUFFER_SIZE = new Vec2i(80, 60);

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

    private int[] vertexArrayName = {0}, pipelineName = {0}, samplerName = {0}, bufferName = new int[Buffer.MAX],
            textureName = new int[Texture.MAX], framebufferName = new int[Framebuffer.MAX];
    private int programName, uniformBlockSize;
    private float[] projection = new float[16];

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

            gl4.glGenProgramPipelines(1, pipelineName, 0);
            gl4.glUseProgramStages(pipelineName[0], GL_VERTEX_SHADER_BIT | GL_FRAGMENT_SHADER_BIT, programName);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        int[] uniformBufferOffset = {0};
        gl4.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset, 0);
        uniformBlockSize = Math.max(projection.length * Float.BYTES, uniformBufferOffset[0]);

        gl4.glGenBuffers(Buffer.MAX, bufferName, 0);
        /**
         * TODO switch to glCreateBuffers.
         */
        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        gl4.glNamedBufferData(bufferName[Buffer.ELEMENT], elementSize, elementBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(elementBuffer);

        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl4.glNamedBufferData(bufferName[Buffer.VERTEX], vertexSize, vertexBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(vertexBuffer);

        gl4.glNamedBufferData(bufferName[Buffer.TRANSFORM], uniformBlockSize, null, GL_DYNAMIC_DRAW);

        return true;
    }

    private boolean initSampler(GL4 gl4) {

        gl4.glGenSamplers(1, samplerName, 0);
        gl4.glSamplerParameteri(samplerName[0], GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        gl4.glSamplerParameteri(samplerName[0], GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        gl4.glSamplerParameteri(samplerName[0], GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        gl4.glSamplerParameteri(samplerName[0], GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        gl4.glSamplerParameteri(samplerName[0], GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        gl4.glSamplerParameterfv(samplerName[0], GL_TEXTURE_BORDER_COLOR, new float[]{0.0f, 0.0f, 0.0f, 0.0f}, 0);
        gl4.glSamplerParameterf(samplerName[0], GL_TEXTURE_MIN_LOD, -1000.f);
        gl4.glSamplerParameterf(samplerName[0], GL_TEXTURE_MAX_LOD, 1000.f);
        gl4.glSamplerParameterf(samplerName[0], GL_TEXTURE_LOD_BIAS, 0.0f);
        gl4.glSamplerParameteri(samplerName[0], GL_TEXTURE_COMPARE_MODE, GL_NONE);
        gl4.glSamplerParameteri(samplerName[0], GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);

        return true;
    }

    private boolean initTexture(GL4 gl4) {

        try {
            jgli.Texture2d texture = new Texture2d(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE));
            jgli.Gl.Format format = jgli.Gl.translate(texture.format());
            jgli.Gl.Swizzles swizzles = jgli.Gl.translate(texture.swizzles());

            gl4.glGenTextures(Texture.MAX, textureName, 0);

            gl4.glTextureParameteri(textureName[Texture.TEXTURE], GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTextureParameteri(textureName[Texture.TEXTURE], GL_TEXTURE_MAX_LEVEL, 0);
            gl4.glTextureParameteri(textureName[Texture.TEXTURE], GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            gl4.glTextureParameteri(textureName[Texture.TEXTURE], GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            gl4.glTextureParameteri(textureName[Texture.TEXTURE], GL_TEXTURE_SWIZZLE_R, swizzles.r.value);
            gl4.glTextureParameteri(textureName[Texture.TEXTURE], GL_TEXTURE_SWIZZLE_G, swizzles.g.value);
            gl4.glTextureParameteri(textureName[Texture.TEXTURE], GL_TEXTURE_SWIZZLE_B, swizzles.b.value);
            gl4.glTextureParameteri(textureName[Texture.TEXTURE], GL_TEXTURE_SWIZZLE_A, swizzles.a.value);

            gl4.glTextureStorage2D(textureName[Texture.TEXTURE], texture.levels(),
                    format.internal.value, texture.dimensions(0)[0], texture.dimensions(0)[1]);
            for (int level = 0; level < texture.levels(); ++level) {
                gl4.glTextureSubImage2D(textureName[Texture.TEXTURE],
                        level,
                        0, 0,
                        texture.dimensions(level)[0], texture.dimensions(level)[1],
                        format.external.value, format.type.value,
                        texture.data(level));
            }

            gl4.glTextureParameteri(textureName[Texture.MULTISAMPLE], GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTextureParameteri(textureName[Texture.MULTISAMPLE], GL_TEXTURE_MAX_LEVEL, 0);
            gl4.glTextureStorage2DMultisample(textureName[Texture.MULTISAMPLE], 4,
                    GL_RGBA8, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y, false);

            gl4.glTextureParameteri(textureName[Texture.COLORBUFFER], GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTextureParameteri(textureName[Texture.COLORBUFFER], GL_TEXTURE_MAX_LEVEL, 0);
            gl4.glTextureStorage2D(textureName[Texture.COLORBUFFER], 1,
                    GL_RGBA8, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y);

        } catch (IOException ex) {
            Logger.getLogger(Gl_430_direct_state_access_ext.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    private boolean initFramebuffer(GL4 gl4) {

        gl4.glGenFramebuffers(Framebuffer.MAX, framebufferName, 0);
        gl4.glNamedFramebufferTexture(framebufferName[Framebuffer.RENDER], GL_COLOR_ATTACHMENT0,
                textureName[Texture.MULTISAMPLE], 0);
        gl4.glNamedFramebufferTexture(framebufferName[Framebuffer.RESOLVE], GL_COLOR_ATTACHMENT0,
                textureName[Texture.COLORBUFFER], 0);

        if (gl4.glCheckNamedFramebufferStatus(framebufferName[Framebuffer.RENDER], GL_FRAMEBUFFER)
                != GL_FRAMEBUFFER_COMPLETE) {
            return false;
        }
        if (gl4.glCheckNamedFramebufferStatus(framebufferName[Framebuffer.RESOLVE], GL_FRAMEBUFFER)
                != GL_FRAMEBUFFER_COMPLETE) {
            return false;
        }

        return true;
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(1, vertexArrayName, 0);
        // Setup the formats
        gl4.glVertexArrayAttribFormat(vertexArrayName[0], Semantic.Attr.POSITION, 2, GL_FLOAT, false, 0);
        gl4.glVertexArrayAttribFormat(vertexArrayName[0], Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, 2 * Float.BYTES);
        // Setup the buffer source
        int bindingIndex = 0;
        gl4.glVertexArrayVertexBuffer(vertexArrayName[0], bindingIndex, bufferName[Buffer.VERTEX],
                0 * Float.BYTES, 2 * 2 * Float.BYTES);
        // Link them up
        gl4.glVertexArrayAttribBinding(vertexArrayName[0], Semantic.Attr.POSITION, bindingIndex);
        gl4.glVertexArrayAttribBinding(vertexArrayName[0], Semantic.Attr.TEXCOORD, bindingIndex);

        gl4.glEnableVertexArrayAttrib(vertexArrayName[0], Semantic.Attr.POSITION);
        gl4.glEnableVertexArrayAttrib(vertexArrayName[0], Semantic.Attr.TEXCOORD);

        return true;
    }
}
