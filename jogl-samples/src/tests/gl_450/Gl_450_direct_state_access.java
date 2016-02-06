/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_450;

import com.jogamp.opengl.GL;
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
import dev.Vec3;

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
    private ByteBuffer uniformPointer;

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

            gl4.glGenProgramPipelines(1, pipelineName, 0);
            gl4.glUseProgramStages(pipelineName[0], GL_VERTEX_SHADER_BIT | GL_FRAGMENT_SHADER_BIT, programName);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        int[] uniformBufferOffset = {0};
        gl4.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset, 0);
        uniformBlockSize = Math.max(Mat4.SIZE, uniformBufferOffset[0]);

        gl4.glCreateBuffers(Buffer.MAX, bufferName, 0);
        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementSize);
        gl4.glNamedBufferStorage(bufferName[Buffer.ELEMENT], elementSize, elementBuffer, 0);
        BufferUtils.destroyDirectBuffer(elementBuffer);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl4.glNamedBufferStorage(bufferName[Buffer.VERTEX], vertexSize, vertexBuffer, 0);
        BufferUtils.destroyDirectBuffer(vertexBuffer);
        gl4.glNamedBufferStorage(bufferName[Buffer.TRANSFORM], uniformBlockSize * 2, null,
                GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT);

        uniformPointer = gl4.glMapNamedBufferRange(bufferName[Buffer.TRANSFORM], 0, uniformBlockSize * 2,
                GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

        return true;
    }

    private boolean initSampler(GL4 gl4) {

        gl4.glCreateSamplers(1, samplerName, 0);
        gl4.glSamplerParameteri(samplerName[0], GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_NEAREST);
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

        textureName[Texture.TEXTURE] = createTexture(gl4, TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE);

        /*
		gli::texture2D Texture(gli::load((getDataDirectory() + TEXTURE_DIFFUSE).c_str()));
		if(Texture.empty())
			return false;

		gli::gl GL;
		gli::gl::format const Format = GL.translate(Texture.format());

		glCreateTextures(GL_TEXTURE_2D, 1, &TextureName[texture::TEXTURE]);
		glTextureParameteri(TextureName[texture::TEXTURE], GL_TEXTURE_BASE_LEVEL, 0);
		glTextureParameteri(TextureName[texture::TEXTURE], GL_TEXTURE_MAX_LEVEL, static_cast<GLint>(Texture.levels() - 1));
		glTextureParameteri(TextureName[texture::TEXTURE], GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTextureParameteri(TextureName[texture::TEXTURE], GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTextureStorage2D(TextureName[texture::TEXTURE], GLint(Texture.levels()), Format.Internal, GLsizei(Texture[0].dimensions().x), GLsizei(Texture[0].dimensions().y));
		for(std::size_t Level = 0; Level < Texture.levels(); ++Level)
		{
			glTextureSubImage2D(TextureName[texture::TEXTURE], GLint(Level),
				0, 0, 
				GLsizei(Texture[Level].dimensions().x), GLsizei(Texture[Level].dimensions().y),
				Format.External, Format.Type,
				Texture[Level].data());
		}
         */
        gl4.glCreateTextures(GL_TEXTURE_2D_MULTISAMPLE, 1, textureName, Texture.MULTISAMPLE);
        gl4.glTextureParameteri(textureName[Texture.MULTISAMPLE], GL_TEXTURE_BASE_LEVEL, 0);
        gl4.glTextureParameteri(textureName[Texture.MULTISAMPLE], GL_TEXTURE_MAX_LEVEL, 0);
        gl4.glTextureStorage2DMultisample(textureName[Texture.MULTISAMPLE], 4, GL_RGBA8,
                FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y, false);

        gl4.glCreateTextures(GL_TEXTURE_2D, 1, textureName, Texture.COLORBUFFER);
        gl4.glTextureParameteri(textureName[Texture.COLORBUFFER], GL_TEXTURE_BASE_LEVEL, 0);
        gl4.glTextureParameteri(textureName[Texture.COLORBUFFER], GL_TEXTURE_MAX_LEVEL, 0);
        gl4.glTextureStorage2D(textureName[Texture.COLORBUFFER], 1, GL_RGBA8, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y);
        return true;
    }

    private int createTexture(GL4 gl4, String filename) {

        int[] textName = {0};

        try {
            jgli.Texture texture = jgli.Load.load(filename);
            if (texture.empty()) {
                return 0;
            }

            jgli.Gl.Format format = jgli.Gl.translate(texture.format());
            jgli.Gl.Swizzles swizzles = jgli.Gl.translate(texture.swizzles());
            jgli.Gl.Target target = jgli.Gl.translate(texture.target());

            gl4.glGenTextures(1, textName, 0);
            gl4.glBindTexture(target.value, textName[0]);
            gl4.glTexParameteri(target.value, GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTexParameteri(target.value, GL_TEXTURE_MAX_LEVEL, texture.levels() - 1);
            gl4.glTexParameteri(target.value, GL_TEXTURE_SWIZZLE_R, swizzles.r.value);
            gl4.glTexParameteri(target.value, GL_TEXTURE_SWIZZLE_G, swizzles.g.value);
            gl4.glTexParameteri(target.value, GL_TEXTURE_SWIZZLE_B, swizzles.b.value);
            gl4.glTexParameteri(target.value, GL_TEXTURE_SWIZZLE_A, swizzles.a.value);

            int[] dimensions = texture.dimensions();

            switch (texture.target()) {
                case TARGET_1D:
                    gl4.glTexStorage1D(
                            target.value, texture.levels(), format.internal.value, dimensions[0]);
                    break;
                case TARGET_1D_ARRAY:
                case TARGET_2D:
                case TARGET_CUBE:
                    gl4.glTexStorage2D(
                            target.value, texture.levels(), format.internal.value, dimensions[0],
                            texture.target() == Target.TARGET_2D ? dimensions[1] : texture.layers() * texture.faces());
                    break;
                case TARGET_2D_ARRAY:
                case TARGET_3D:
                case TARGET_CUBE_ARRAY:
                    gl4.glTexStorage3D(
                            target.value, texture.levels(), format.internal.value, dimensions[0], dimensions[1],
                            texture.target() == Target.TARGET_3D ? dimensions[0] : texture.layers() * texture.faces());
                    break;
                default:
                    assert false;
                    break;
            }

            for (int layer = 0; layer < texture.layers(); ++layer) {
                for (int face = 0; face < texture.faces(); ++face) {
                    for (int level = 0; level < texture.levels(); ++level) {

                        dimensions = texture.dimensions(level);
                        int targetValue = texture.target().isTargetCube() ? (GL_TEXTURE_CUBE_MAP_POSITIVE_X + face)
                                : target.value;

                        switch (texture.target()) {
                            case TARGET_1D:
                                if (texture.format().isCompressed()) {
                                    gl4.glCompressedTexSubImage1D(
                                            targetValue, level, 0, dimensions[0],
                                            format.internal.value, texture.size(level), texture.data(layer, face, level));
                                } else {
                                    gl4.glTexSubImage1D(
                                            targetValue, level, 0, dimensions[0],
                                            format.external.value, format.type.value, texture.data(layer, face, level));
                                }
                                break;
                            case TARGET_1D_ARRAY:
                            case TARGET_2D:
                            case TARGET_CUBE:
                                if (texture.format().isCompressed()) {
                                    gl4.glCompressedTexSubImage2D(
                                            targetValue, level,
                                            0, 0, dimensions[0],
                                            texture.target() == Target.TARGET_1D_ARRAY ? layer : dimensions[1],
                                            format.internal.value, texture.size(level), texture.data(layer, face, level));
                                } else {
                                    gl4.glTexSubImage2D(
                                            targetValue, level,
                                            0, 0, dimensions[0],
                                            texture.target() == Target.TARGET_1D_ARRAY ? layer : dimensions[1],
                                            format.external.value, format.type.value, texture.data(layer, face, level));
                                }
                                break;
                            case TARGET_2D_ARRAY:
                            case TARGET_3D:
                            case TARGET_CUBE_ARRAY:
                                if (texture.format().isCompressed()) {
                                    gl4.glCompressedTexSubImage3D(
                                            targetValue, level,
                                            0, 0, 0, dimensions[0], dimensions[1],
                                            texture.target() == Target.TARGET_3D ? dimensions[2] : layer,
                                            format.internal.value, texture.size(level), texture.data(layer, face, level));
                                } else {
                                    gl4.glTexSubImage3D(
                                            targetValue, level,
                                            0, 0, 0, dimensions[0], dimensions[1],
                                            texture.target() == Target.TARGET_3D ? dimensions[2] : layer,
                                            format.external.value, format.type.value, texture.data(layer, face, level));
                                }
                                break;
                            default:
                                assert false;
                                break;
                        }
                    }
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(Gl_450_direct_state_access.class.getName()).log(Level.SEVERE, null, ex);
        }
        return textName[0];
    }

    private boolean initFramebuffer(GL4 gl4) {

        gl4.glCreateFramebuffers(Framebuffer.MAX, framebufferName, 0);
        gl4.glNamedFramebufferTexture(framebufferName[Framebuffer.RENDER], GL_COLOR_ATTACHMENT0,
                textureName[Texture.MULTISAMPLE], 0);
        gl4.glNamedFramebufferTexture(framebufferName[Framebuffer.RESOLVE], GL_COLOR_ATTACHMENT0,
                textureName[Texture.COLORBUFFER], 0);

        if (!isFramebufferComplete(gl4, framebufferName[Framebuffer.RENDER])) {
            return false;
        }
        if (!isFramebufferComplete(gl4, framebufferName[Framebuffer.RESOLVE])) {
            return false;
        }

        int[] samples = {0};
        gl4.glGetNamedFramebufferParameteriv(framebufferName[Framebuffer.RENDER], GL_SAMPLES, samples, 0);
        return samples[0] == 4;
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glCreateVertexArrays(1, vertexArrayName, 0);

        gl4.glVertexArrayAttribBinding(vertexArrayName[0], Semantic.Attr.POSITION, Semantic.Buffer.STATIC);
        gl4.glVertexArrayAttribFormat(vertexArrayName[0], Semantic.Attr.POSITION, 2, GL_FLOAT, false, 0);
        gl4.glEnableVertexArrayAttrib(vertexArrayName[0], Semantic.Attr.POSITION);

        gl4.glVertexArrayAttribBinding(vertexArrayName[0], Semantic.Attr.TEXCOORD, Semantic.Buffer.STATIC);
        gl4.glVertexArrayAttribFormat(vertexArrayName[0], Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, Vec2.SIZE);
        gl4.glEnableVertexArrayAttrib(vertexArrayName[0], Semantic.Attr.TEXCOORD);

        gl4.glVertexArrayElementBuffer(vertexArrayName[0], bufferName[Buffer.ELEMENT]);
        gl4.glVertexArrayVertexBuffer(vertexArrayName[0], Semantic.Buffer.STATIC, bufferName[Buffer.VERTEX], 0,
                glf.Vertex_v2fv2f.SIZE);

        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        {
            Mat4 projectionA = glm.perspective_((float) Math.PI * 0.25f,
                    (float) FRAMEBUFFER_SIZE.x / FRAMEBUFFER_SIZE.y, 0.1f, 100.0f).scale(new Vec3(1, -1, 1));
            uniformPointer.asFloatBuffer().put(projectionA.mul(viewMat4()).mul(new Mat4(1)).toFa_());

            Mat4 projectionB = glm.perspective_((float) Math.PI * 0.25f, (float) windowSize.x / windowSize.y, 0.1f, 100.0f);
            uniformPointer.position(uniformBlockSize);
            uniformPointer.asFloatBuffer().put(projectionB.mul(viewMat4()).scale(new Vec3(2), new Mat4(1)).toFa_());
            uniformPointer.rewind();
        }

        // Step 1, render the scene in a multisampled framebuffer
        gl4.glBindProgramPipeline(pipelineName[0]);

        renderFBO(gl4);

        // Step 2: blit
        gl4.glBlitNamedFramebuffer(framebufferName[Framebuffer.RENDER], framebufferName[Framebuffer.RESOLVE],
                0, 0, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y,
                0, 0, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y,
                GL_COLOR_BUFFER_BIT, GL_NEAREST);

        int[] maxColorAttachment = {GL_COLOR_ATTACHMENT0};
        gl4.glInvalidateNamedFramebufferData(framebufferName[Framebuffer.RENDER], 1, maxColorAttachment, 0);

        // Step 3, render the colorbuffer from the multisampled framebuffer
        renderFB(gl4);

        return true;
    }

    private void renderFBO(GL4 gl4) {

        gl4.glEnable(GL_MULTISAMPLE);
        gl4.glEnable(GL_SAMPLE_SHADING);
        gl4.glMinSampleShading(4.0f);

        gl4.glClipControl(GL_LOWER_LEFT, GL_NEGATIVE_ONE_TO_ONE);
        gl4.glViewportIndexedf(0, 0, 0, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y);
        gl4.glClearNamedFramebufferfv(framebufferName[Framebuffer.RENDER], GL_COLOR, 0,
                new float[]{0.0f, 0.5f, 1.0f, 1.0f}, 0);

        gl4.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[Framebuffer.RENDER]);
        gl4.glBindBufferRange(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.TRANSFORM], 0,
                uniformBlockSize);
        gl4.glBindSamplers(0, 1, samplerName, 0);
        gl4.glBindTextureUnit(0, textureName[Texture.TEXTURE]);
        gl4.glBindVertexArray(vertexArrayName[0]);

        gl4.glDrawElementsInstancedBaseVertexBaseInstance(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0, 0);

        gl4.glDisable(GL_MULTISAMPLE);
    }

    private void renderFB(GL4 gl4) {

        gl4.glClipControl(GL_LOWER_LEFT, GL_NEGATIVE_ONE_TO_ONE);
        gl4.glViewportIndexedf(0, 0, 0, windowSize.x, windowSize.y);
        gl4.glClearNamedFramebufferfv(0, GL_COLOR, 0, new float[]{0.0f, 0.5f, 1.0f, 1.0f}, 0);

        gl4.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        gl4.glBindBufferRange(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.TRANSFORM],
                uniformBlockSize, uniformBlockSize);
        gl4.glBindTextureUnit(0, textureName[Texture.COLORBUFFER]);
        gl4.glBindVertexArray(vertexArrayName[0]);

        gl4.glDrawElementsInstancedBaseVertexBaseInstance(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0, 0);
    }
}
