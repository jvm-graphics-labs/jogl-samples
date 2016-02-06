/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_440;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2GL3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import glm.glm;
import glm.mat._4.Mat4;
import glm.vec._2.Vec2;
import dev.Vec3;
import dev.Vec4i;
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

/**
 *
 * @author GBarbieri
 */
public class Gl_440_texture_compressed extends Test {

    public static void main(String[] args) {
        Gl_440_texture_compressed gl_440_texture_compressed = new Gl_440_texture_compressed();
    }

    public Gl_440_texture_compressed() {
        super("gl-440-texture-compressed", Profile.CORE, 4, 4);
    }

    private final String SHADERS_SOURCE = "texture-2d";
    private final String SHADERS_ROOT = "src/data/gl_440";
    private final String TEXTURE_DIFFUSE_RGB_ETC2_SRGB = "kueken7_rgb_etc2_srgb.ktx";
    private final String TEXTURE_DIFFUSE_RGB9E5_UFLOAT = "kueken7_rgb9e5_ufloat.ktx";
    private final String TEXTURE_DIFFUSE_RGBA_DXT5_UNORM = "kueken7_rgba_dxt5_unorm.ktx";
    private final String TEXTURE_DIFFUSE_RG_EAC_UNORM = "kueken7_rg_eac_snorm.ktx";

    private int vertexCount = 4;
    private int vertexSize = vertexCount * glf.Vertex_v2fv2f.SIZE;
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

        public static final int VERTEX = 0;
        public static final int ELEMENT = 1;
        public static final int TRANSFORM = 2;
        public static final int MAX = 3;
    }

    private class Texture {

        public static final int RGB8 = 0;
        public static final int RGB10 = 1;
        public static final int BC1 = 2;
        public static final int BC3 = 3;
        public static final int MAX = 4;
    }

    private int[] pipelineName = {0}, vertexArrayName = {0}, samplerName = {0}, bufferName = new int[Buffer.MAX],
            textureName = new int[Texture.MAX];
    private int programName;
    private Vec4i[] viewport = new Vec4i[Texture.MAX];

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        viewport[Texture.RGB8] = new Vec4i(0, 0, windowSize.x >> 1, windowSize.y >> 1);
        viewport[Texture.RGB10] = new Vec4i(windowSize.x >> 1, 0, windowSize.x >> 1, windowSize.y >> 1);
        viewport[Texture.BC1] = new Vec4i(windowSize.x >> 1, windowSize.y >> 1, windowSize.x >> 1, windowSize.y >> 1);
        viewport[Texture.BC3] = new Vec4i(0, windowSize.y >> 1, windowSize.x >> 1, windowSize.y >> 1);

        boolean validated = checkExtension(gl4, "GL_ARB_multi_bind");

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
            validated = initSampler(gl4);
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

            gl4.glGenProgramPipelines(1, pipelineName, 0);
            gl4.glUseProgramStages(pipelineName[0], GL_VERTEX_SHADER_BIT | GL_FRAGMENT_SHADER_BIT, programName);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        gl4.glGenBuffers(Buffer.MAX, bufferName, 0);

        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT]);
        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(elementBuffer);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl4.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(vertexBuffer);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
        gl4.glBufferData(GL_UNIFORM_BUFFER, Mat4.SIZE, null, GL_DYNAMIC_DRAW);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return true;
    }

    private boolean initTexture(GL4 gl4) {

        try {
            gl4.glGenTextures(Texture.MAX, textureName, 0);

            {
                jgli.Texture texture = jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE_RGBA_DXT5_UNORM);
                assert (!texture.empty());
                jgli.Gl.Format format = jgli.Gl.translate(texture.format());
                jgli.Gl.Swizzles swizzles = jgli.Gl.translate(texture.swizzles());

                gl4.glBindTexture(GL_TEXTURE_2D_ARRAY, textureName[Texture.BC1]);
                gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_BASE_LEVEL, 0);
                gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAX_LEVEL, texture.levels() - 1);
                gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SWIZZLE_R, swizzles.r.value);
                gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SWIZZLE_G, swizzles.g.value);
                gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SWIZZLE_B, swizzles.b.value);
                gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SWIZZLE_A, swizzles.a.value);
                gl4.glTexStorage3D(GL_TEXTURE_2D_ARRAY, texture.levels(),
                        format.internal.value,
                        texture.dimensions()[0], texture.dimensions()[1], 1);

                for (int level = 0; level < texture.levels(); ++level) {

                    int[] dimensions = texture.dimensions(level);

                    gl4.glCompressedTexSubImage3D(GL_TEXTURE_2D_ARRAY, level,
                            0, 0, 0,
                            dimensions[0], dimensions[1], 1,
                            format.internal.value,
                            texture.size(level),
                            texture.data(0, 0, level));
                }

            }

            {
                jgli.Texture texture = jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE_RG_EAC_UNORM);
                assert (!texture.empty());
                jgli.Gl.Format format = jgli.Gl.translate(texture.format());
                jgli.Gl.Swizzles swizzles = jgli.Gl.translate(texture.swizzles());

                gl4.glBindTexture(GL_TEXTURE_2D_ARRAY, textureName[Texture.BC3]);
                gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_BASE_LEVEL, 0);
                gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAX_LEVEL, texture.levels() - 1);
                gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SWIZZLE_R, swizzles.r.value);
                gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SWIZZLE_G, swizzles.g.value);
                gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SWIZZLE_B, swizzles.b.value);
                gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SWIZZLE_A, swizzles.a.value);
                gl4.glTexStorage3D(GL_TEXTURE_2D_ARRAY, texture.levels(),
                        format.internal.value,
                        texture.dimensions()[0], texture.dimensions()[1], 1);

                for (int level = 0; level < texture.levels(); ++level) {

                    gl4.glCompressedTexSubImage3D(GL_TEXTURE_2D_ARRAY, level,
                            0, 0, 0,
                            texture.dimensions(level)[0], texture.dimensions(level)[1], 1,
                            format.internal.value,
                            texture.size(level),
                            texture.data(0, 0, level));
                }
            }

            {
                jgli.Texture texture = jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE_RGB9E5_UFLOAT);
                assert (!texture.empty());
                jgli.Gl.Format format = jgli.Gl.translate(texture.format());
                jgli.Gl.Swizzles swizzles = jgli.Gl.translate(texture.swizzles());

                gl4.glBindTexture(GL_TEXTURE_2D_ARRAY, textureName[Texture.RGB8]);
                gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_BASE_LEVEL, 0);
                gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAX_LEVEL, texture.levels() - 1);
                gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SWIZZLE_R, swizzles.r.value);
                gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SWIZZLE_G, swizzles.g.value);
                gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SWIZZLE_B, swizzles.b.value);
                gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SWIZZLE_A, swizzles.a.value);
                gl4.glTexStorage3D(GL_TEXTURE_2D_ARRAY, texture.levels(),
                        format.internal.value,
                        texture.dimensions()[0], texture.dimensions()[1], 1);

                for (int level = 0; level < texture.levels(); ++level) {

                    gl4.glTexSubImage3D(GL_TEXTURE_2D_ARRAY, level,
                            0, 0, 0,
                            texture.dimensions(level)[0], texture.dimensions(level)[1], 1,
                            format.external.value, format.type.value,
                            texture.data(0, 0, level));
                }
            }

            {
                jgli.Texture texture = jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE_RGB_ETC2_SRGB);
                assert (!texture.empty());
                jgli.Gl.Format format = jgli.Gl.translate(texture.format());
                jgli.Gl.Swizzles swizzles = jgli.Gl.translate(texture.swizzles());

                gl4.glBindTexture(GL_TEXTURE_2D_ARRAY, textureName[Texture.RGB10]);
                gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_BASE_LEVEL, 0);
                gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAX_LEVEL, texture.levels() - 1);
                gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SWIZZLE_R, swizzles.r.value);
                gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SWIZZLE_G, swizzles.g.value);
                gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SWIZZLE_B, swizzles.b.value);
                gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SWIZZLE_A, swizzles.a.value);
                gl4.glTexStorage3D(GL_TEXTURE_2D_ARRAY, texture.levels(),
                        format.internal.value,
                        texture.dimensions()[0], texture.dimensions()[1], 1);

                for (int level = 0; level < texture.levels(); ++level) {

                    gl4.glCompressedTexSubImage3D(GL_TEXTURE_2D_ARRAY, level,
                            0, 0, 0,
                            texture.dimensions(level)[0], texture.dimensions(level)[1], 1,
                            format.internal.value,
                            texture.size(level),
                            texture.data(0, 0, level));
                }
            }

            gl4.glBindTextures(0, 1, null, 0);
        } catch (IOException ex) {
            Logger.getLogger(Gl_440_texture_compressed.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(1, vertexArrayName, 0);
        gl4.glBindVertexArray(vertexArrayName[0]);
        {
            gl4.glVertexAttribBinding(Semantic.Attr.POSITION, Semantic.Buffer.STATIC);
            gl4.glVertexAttribFormat(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 0);

            gl4.glVertexAttribBinding(Semantic.Attr.TEXCOORD, Semantic.Buffer.STATIC);
            gl4.glVertexAttribFormat(Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, Vec2.SIZE);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl4.glEnableVertexAttribArray(Semantic.Attr.TEXCOORD);

            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT]);
            gl4.glBindVertexBuffer(Semantic.Buffer.STATIC, bufferName[Buffer.VERTEX], 0, glf.Vertex_v2fv2f.SIZE);
        }
        gl4.glBindVertexArray(0);

        return true;
    }

    private boolean initSampler(GL4 gl4) {

        gl4.glGenSamplers(1, samplerName, 0);
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

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        // Asynchrone update of the uniform buffer
        {
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
            ByteBuffer pointer = gl4.glMapBufferRange(GL_UNIFORM_BUFFER, 0, Mat4.SIZE,
                    GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, 4.0f / 3.0f, 0.1f, 1000.0f);
            Mat4 model = new Mat4(1.0f).scale(new Vec3(4.0f));

            pointer.asFloatBuffer().put(projection.mul(viewMat4()).mul(model).toFa_());

            // Make sure the uniform buffer is uploaded
            gl4.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        // Clear the color buffer
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{0.0f, 0.5f, 1.0f, 1.0f}, 0);

        gl4.glBindProgramPipeline(pipelineName[0]);
        gl4.glBindBuffersBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, 1, bufferName, Buffer.TRANSFORM);
        gl4.glBindSamplers(0, 1, samplerName, 0);
        gl4.glBindVertexArray(vertexArrayName[0]);

        // Draw each texture in different viewports
        for (int index = 0; index < Texture.MAX; ++index) {
            gl4.glViewportIndexedfv(0, viewport[index].toFA_(), 0);
            gl4.glBindTextures(0, 1, textureName, index);

            gl4.glDrawElementsInstancedBaseVertexBaseInstance(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0, 0);
        }

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteProgramPipelines(1, pipelineName, 0);
        gl4.glDeleteBuffers(Buffer.MAX, bufferName, 0);
        gl4.glDeleteProgram(programName);
        gl4.glDeleteTextures(Texture.MAX, textureName, 0);
        gl4.glDeleteVertexArrays(1, vertexArrayName, 0);
        gl4.glDeleteSamplers(1, samplerName, 0);

        return true;
    }
}
