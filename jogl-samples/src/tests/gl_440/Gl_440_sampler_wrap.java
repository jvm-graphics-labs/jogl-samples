/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_440;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_CLAMP_TO_EDGE;
import static com.jogamp.opengl.GL.GL_COMPRESSED_RGBA_S3TC_DXT5_EXT;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_LINEAR;
import static com.jogamp.opengl.GL.GL_LINEAR_MIPMAP_LINEAR;
import static com.jogamp.opengl.GL.GL_MAP_INVALIDATE_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_MAP_WRITE_BIT;
import static com.jogamp.opengl.GL.GL_MIRRORED_REPEAT;
import static com.jogamp.opengl.GL.GL_REPEAT;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_S;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_T;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_TRUE;
import static com.jogamp.opengl.GL2.GL_MIRROR_CLAMP_TO_BORDER_EXT;
import static com.jogamp.opengl.GL2ES2.GL_CLAMP_TO_BORDER;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER_BIT;
import static com.jogamp.opengl.GL2ES2.GL_PROGRAM_SEPARABLE;
import static com.jogamp.opengl.GL2ES2.GL_TEXTURE_BORDER_COLOR;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER_BIT;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_BASE_LEVEL;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_MAX_LEVEL;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT;
import com.jogamp.opengl.GL4;
import static com.jogamp.opengl.GL4.GL_MAP_COHERENT_BIT;
import static com.jogamp.opengl.GL4.GL_MAP_PERSISTENT_BIT;
import static com.jogamp.opengl.GL4.GL_MIRROR_CLAMP_TO_EDGE;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import glm.glm;
import glm.mat._4.Mat4;
import glm.vec._2.Vec2;
import dev.Vec4;
import framework.BufferUtils;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import jgli.Texture2d;

/**
 *
 * @author GBarbieri
 */
public class Gl_440_sampler_wrap extends Test {

    public static void main(String[] args) {
        Gl_440_sampler_wrap gl_440_sampler_wrap = new Gl_440_sampler_wrap();
    }

    public Gl_440_sampler_wrap() {
        super("gl-440-sampler-wrap", Profile.CORE, 4, 4);
    }

    private final String SHADERS_SOURCE = "sampler-wrap";
    private final String SHADERS_ROOT = "src/data/gl_440";
    private final String TEXTURE_DIFFUSE_DXT5 = "kueken7_rgba_dxt5_unorm.dds";

    // With DDS textures, v texture coordinate are reversed, from top to bottom
    private int vertexCount = 6;
    private int vertexSize = vertexCount * 2 * Vec2.SIZE;
    private float[] vertexData = {
        -1.0f, -1.0f,/**/ -2.0f, +2.0f,
        +1.0f, -1.0f,/**/ +2.0f, +2.0f,
        +1.0f, +1.0f,/**/ +2.0f, -2.0f,
        +1.0f, +1.0f,/**/ +2.0f, -2.0f,
        -1.0f, +1.0f,/**/ -2.0f, -2.0f,
        -1.0f, -1.0f,/**/ -2.0f, +2.0f};

    private class Viewport {

        public static final int _0 = 0;
        public static final int _1 = 1;
        public static final int _2 = 2;
        public static final int _3 = 3;
        public static final int _4 = 4;
        public static final int _5 = 5;
        public static final int MAX = 6;
    }

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int TRANSFORM = 1;
        public static final int MAX = 2;
    }

    private int[] pipelineName = {0}, vertexArrayName = {0}, textureName = {0}, bufferName = new int[Buffer.MAX],
            samplerName = new int[Viewport.MAX];
    private int programName;
    private ByteBuffer uniformPointer;
    private Vec4[] viewport = new Vec4[Viewport.MAX];

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        Vec2 viewportSize = new Vec2(windowSize.x * 0.33f, windowSize.y * 0.50f);

        viewport[Viewport._0] = new Vec4(viewportSize.x * 0.0f, viewportSize.y * 0.0f, viewportSize.x * 1.0f,
                viewportSize.y * 1.0f);
        viewport[Viewport._1] = new Vec4(viewportSize.x * 1.0f, viewportSize.y * 0.0f, viewportSize.x * 1.0f,
                viewportSize.y * 1.0f);
        viewport[Viewport._2] = new Vec4(viewportSize.x * 2.0f, viewportSize.y * 0.0f, viewportSize.x * 1.0f,
                viewportSize.y * 1.0f);
        viewport[Viewport._3] = new Vec4(viewportSize.x * 0.0f, viewportSize.y * 1.0f, viewportSize.x * 1.0f,
                viewportSize.y * 1.0f);
        viewport[Viewport._4] = new Vec4(viewportSize.x * 1.0f, viewportSize.y * 1.0f, viewportSize.x * 1.0f,
                viewportSize.y * 1.0f);
        viewport[Viewport._5] = new Vec4(viewportSize.x * 2.0f, viewportSize.y * 1.0f, viewportSize.x * 1.0f,
                viewportSize.y * 1.0f);

        boolean validated = true;
        validated = validated && checkExtension(gl4, "GL_ARB_texture_mirror_clamp_to_edge");

        if (validated) {
            validated = initProgram(gl4);
        }
        if (validated) {
            validated = initBuffer(gl4);
        }
        if (validated) {
            validated = initTexture(gl4);
        }
        if (validated) {
            validated = initSampler(gl4);
        }
        if (validated) {
            validated = initVertexArray(gl4);
        }
        if (validated) {
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
            uniformPointer = gl4.glMapBufferRange(GL_UNIFORM_BUFFER, 0, Mat4.SIZE,
                    GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT);
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

        // Generate a buffer object
        gl4.glGenBuffers(Buffer.MAX, bufferName, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl4.glBufferStorage(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, 0);
        BufferUtils.destroyDirectBuffer(vertexBuffer);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        int[] uniformBufferOffset = {0};
        gl4.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset, 0);
        int uniformBlockSize = Math.max(Mat4.SIZE, uniformBufferOffset[0]);

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
        gl4.glBufferStorage(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT
                | GL_MAP_COHERENT_BIT);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return checkError(gl4, "initBuffer");
    }

    private boolean initSampler(GL4 gl4) {

        gl4.glGenSamplers(Viewport.MAX, samplerName, 0);

        for (int i = 0; i < Viewport.MAX; ++i) {

            gl4.glSamplerParameteri(samplerName[i], GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            gl4.glSamplerParameteri(samplerName[i], GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            Vec4 borderColor = new Vec4(0.0f, 0.5f, 1.0f, 1.0f);
            gl4.glSamplerParameterfv(samplerName[i], GL_TEXTURE_BORDER_COLOR, borderColor.toFA_(), 0);
        }

        gl4.glSamplerParameteri(samplerName[Viewport._0], GL_TEXTURE_WRAP_S, GL_REPEAT);
        gl4.glSamplerParameteri(samplerName[Viewport._1], GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        gl4.glSamplerParameteri(samplerName[Viewport._2], GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
        gl4.glSamplerParameteri(samplerName[Viewport._3], GL_TEXTURE_WRAP_S, GL_MIRRORED_REPEAT);
        gl4.glSamplerParameteri(samplerName[Viewport._4], GL_TEXTURE_WRAP_S, GL_MIRROR_CLAMP_TO_EDGE);
        if (checkExtension(gl4, "GL_EXT_texture_mirror_clamp")) {
            gl4.glSamplerParameteri(samplerName[Viewport._5], GL_TEXTURE_WRAP_S, GL_MIRROR_CLAMP_TO_BORDER_EXT);
        } else {
            gl4.glSamplerParameteri(samplerName[Viewport._5], GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
        }

        gl4.glSamplerParameteri(samplerName[Viewport._0], GL_TEXTURE_WRAP_T, GL_REPEAT);
        gl4.glSamplerParameteri(samplerName[Viewport._1], GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        gl4.glSamplerParameteri(samplerName[Viewport._2], GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
        gl4.glSamplerParameteri(samplerName[Viewport._3], GL_TEXTURE_WRAP_T, GL_MIRRORED_REPEAT);
        gl4.glSamplerParameteri(samplerName[Viewport._4], GL_TEXTURE_WRAP_T, GL_MIRROR_CLAMP_TO_EDGE);
        if (checkExtension(gl4, "GL_EXT_texture_mirror_clamp")) {
            gl4.glSamplerParameteri(samplerName[Viewport._5], GL_TEXTURE_WRAP_T, GL_MIRROR_CLAMP_TO_BORDER_EXT);
        } else {
            gl4.glSamplerParameteri(samplerName[Viewport._5], GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
        }

        return checkError(gl4, "initSampler");
    }

    private boolean initTexture(GL4 gl4) {

        try {
            jgli.Texture2d texture = new Texture2d(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE_DXT5));

            gl4.glGenTextures(1, textureName, 0);

            gl4.glBindTexture(GL_TEXTURE_2D, textureName[0]);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, texture.levels() - 1);

            for (int level = 0; level < texture.levels(); ++level) {

                gl4.glCompressedTexImage2D(
                        GL_TEXTURE_2D,
                        level,
                        GL_COMPRESSED_RGBA_S3TC_DXT5_EXT,
                        texture.dimensions(level)[0],
                        texture.dimensions(level)[1],
                        0,
                        texture.size(level),
                        texture.data(level));
            }

        } catch (IOException ex) {
            Logger.getLogger(Gl_440_sampler_wrap.class.getName()).log(Level.SEVERE, null, ex);
        }
        return checkError(gl4, "initTexture");
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(1, vertexArrayName, 0);
        gl4.glBindVertexArray(vertexArrayName[0]);
        {
            gl4.glVertexAttribFormat(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 0);
            gl4.glVertexAttribBinding(Semantic.Attr.POSITION, Semantic.Buffer.STATIC);
            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);

            gl4.glVertexAttribFormat(Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, Vec2.SIZE);
            gl4.glVertexAttribBinding(Semantic.Attr.TEXCOORD, Semantic.Buffer.STATIC);
            gl4.glEnableVertexAttribArray(Semantic.Attr.TEXCOORD);
        }
        gl4.glBindVertexArray(0);

        return checkError(gl4, "initVertexArray");
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        {
            float aspect = (windowSize.x * 0.33f) / (windowSize.y * 0.50f);
            Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, aspect, 0.1f, 100.0f);
            Mat4 mvp = projection.mul(viewMat4()).mul(new Mat4(1.0f));

            uniformPointer.asFloatBuffer().put(mvp.toFa_());
        }

        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 0.5f, 0.0f, 1.0f}, 0);

        gl4.glBindProgramPipeline(pipelineName[0]);
        gl4.glBindBuffersBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, 1, bufferName, Buffer.TRANSFORM);
        gl4.glBindTextures(Semantic.Sampler.DIFFUSE, 1, textureName, 0);
        gl4.glBindVertexArray(vertexArrayName[0]);
        gl4.glBindVertexBuffer(Semantic.Buffer.STATIC, bufferName[Buffer.VERTEX], 0, 2 * Vec2.SIZE);

        for (int index = 0; index < Viewport.MAX; ++index) {

            gl4.glViewportIndexedf(0,
                    viewport[index].x,
                    viewport[index].y,
                    viewport[index].z,
                    viewport[index].w);

            gl4.glBindSamplers(0, 1, samplerName, index);
            gl4.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 1);
        }

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteBuffers(Buffer.MAX, bufferName, 0);
        gl4.glDeleteProgram(programName);
        gl4.glDeleteProgramPipelines(1, pipelineName, 0);
        gl4.glDeleteTextures(1, textureName, 0);
        gl4.glDeleteVertexArrays(1, vertexArrayName, 0);

        return true;
    }
}
