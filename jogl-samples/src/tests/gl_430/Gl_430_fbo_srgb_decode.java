/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_430;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_ALPHA;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_COLOR_ATTACHMENT0;
import static com.jogamp.opengl.GL.GL_DEPTH_ATTACHMENT;
import static com.jogamp.opengl.GL.GL_DEPTH_COMPONENT24;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_DYNAMIC_DRAW;
import static com.jogamp.opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_FRAMEBUFFER;
import static com.jogamp.opengl.GL.GL_FRAMEBUFFER_SRGB;
import static com.jogamp.opengl.GL.GL_LESS;
import static com.jogamp.opengl.GL.GL_LINEAR;
import static com.jogamp.opengl.GL.GL_LINEAR_MIPMAP_LINEAR;
import static com.jogamp.opengl.GL.GL_MAP_INVALIDATE_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_MAP_WRITE_BIT;
import static com.jogamp.opengl.GL.GL_RGBA8;
import static com.jogamp.opengl.GL.GL_SRGB8_ALPHA8;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNPACK_ALIGNMENT;
import static com.jogamp.opengl.GL.GL_UNSIGNED_SHORT;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_RED;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES3.GL_BLUE;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import static com.jogamp.opengl.GL2ES3.GL_DEPTH;
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
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
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
import jgli.Texture2d;

/**
 *
 * @author GBarbieri
 */
public class Gl_430_fbo_srgb_decode extends Test {

    public static void main(String[] args) {
        Gl_430_fbo_srgb_decode gl_430_fbo_srgb_decode = new Gl_430_fbo_srgb_decode();
    }

    public Gl_430_fbo_srgb_decode() {
        super("gl-430-fbo-srgb-decode", Profile.CORE, 4, 3);
    }

    private final String SHADERS_SOURCE_TEXTURE = "fbo-srgb-decode";
    private final String SHADERS_SOURCE_SPLASH = "fbo-srgb-decode-blit";
    private final String SHADERS_ROOT = "src/data/gl_430";
    private final String TEXTURE_DIFFUSE = "kueken7_rgba8_srgb.dds";

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

    private enum Buffer {
        VERTEX,
        ELEMENT,
        TRANSFORM,
        MAX
    }

    private enum Texture {
        DIFFUSE_SRGB,
        DIFFUSE_RGB,
        COLORBUFFER,
        RENDERBUFFER,
        MAX
    }

    private enum Program {
        TEXTURE,
        SPLASH,
        MAX
    }

    private enum Shader {
        VERT_TEXTURE,
        FRAG_TEXTURE,
        VERT_SPLASH,
        FRAG_SPLASH,
        MAX
    }

    private int[] framebufferName = {0}, programName = new int[Program.MAX.ordinal()],
            vertexArrayName = new int[Program.MAX.ordinal()], bufferName = new int[Buffer.MAX.ordinal()],
            textureName = new int[Texture.MAX.ordinal()], uniformDiffuse = new int[Program.MAX.ordinal()];
    private int framebufferScale = 2, uniformTransform = -1;
    private float[] projection = new float[16];

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

        ShaderCode[] shaderCodes = new ShaderCode[Shader.MAX.ordinal()];

        // Create program
        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            shaderCodes[Shader.VERT_TEXTURE.ordinal()] = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_TEXTURE, "vert", null, true);
            shaderCodes[Shader.FRAG_TEXTURE.ordinal()] = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_TEXTURE, "frag", null, true);

            shaderProgram.init(gl4);
            programName[Program.TEXTURE.ordinal()] = shaderProgram.program();
            shaderProgram.add(shaderCodes[Shader.VERT_TEXTURE.ordinal()]);
            shaderProgram.add(shaderCodes[Shader.FRAG_TEXTURE.ordinal()]);
            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            shaderCodes[Shader.VERT_SPLASH.ordinal()] = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_SPLASH, "vert", null, true);
            shaderCodes[Shader.FRAG_SPLASH.ordinal()] = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_SPLASH, "frag", null, true);

            shaderProgram.init(gl4);
            programName[Program.SPLASH.ordinal()] = shaderProgram.program();
            shaderProgram.add(shaderCodes[Shader.VERT_SPLASH.ordinal()]);
            shaderProgram.add(shaderCodes[Shader.FRAG_SPLASH.ordinal()]);
            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            uniformTransform = gl4.glGetUniformBlockIndex(programName[Program.TEXTURE.ordinal()], "Transform");
            uniformDiffuse[Program.TEXTURE.ordinal()]
                    = gl4.glGetUniformLocation(programName[Program.TEXTURE.ordinal()], "diffuse");
            uniformDiffuse[Program.SPLASH.ordinal()]
                    = gl4.glGetUniformLocation(programName[Program.SPLASH.ordinal()], "diffuse");

            gl4.glUseProgram(programName[Program.TEXTURE.ordinal()]);
            gl4.glUniform1i(uniformDiffuse[Program.TEXTURE.ordinal()], 0);
            gl4.glUniformBlockBinding(programName[Program.TEXTURE.ordinal()], uniformTransform,
                    Semantic.Uniform.TRANSFORM0);

            gl4.glUseProgram(programName[Program.SPLASH.ordinal()]);
            gl4.glUniform1i(uniformDiffuse[Program.SPLASH.ordinal()], 0);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        gl4.glGenBuffers(Buffer.MAX.ordinal(), bufferName, 0);

        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT.ordinal()]);
        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(elementBuffer);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl4.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(vertexBuffer);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        int[] uniformBufferOffset = {0};
        gl4.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset, 0);
        int uniformBlockSize = Math.max(projection.length * Float.BYTES, uniformBufferOffset[0]);

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM.ordinal()]);
        gl4.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_DYNAMIC_DRAW);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return true;
    }

    private boolean initTexture(GL4 gl4) {

        try {
            jgli.Texture2d texture = new Texture2d(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE));
            assert (!texture.empty());
            jgli.Gl.Format format = jgli.Gl.instance.translate(texture.format());

            gl4.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

            gl4.glGenTextures(Texture.MAX.ordinal(), textureName, 0);

            gl4.glActiveTexture(GL_TEXTURE0);
            gl4.glBindTexture(GL_TEXTURE_2D_ARRAY, textureName[Texture.DIFFUSE_SRGB.ordinal()]);
            gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_BASE_LEVEL, 1);
            gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAX_LEVEL, texture.levels() - 1);
            gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SWIZZLE_R, GL_RED);
            gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SWIZZLE_G, GL_GREEN);
            gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SWIZZLE_B, GL_BLUE);
            gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SWIZZLE_A, GL_ALPHA);
            gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

            gl4.glTexStorage3D(GL_TEXTURE_2D_ARRAY,
                    texture.levels(), format.internal.value,
                    texture.dimensions(0)[0], texture.dimensions(0)[1], 1);

            for (int level = 0; level < texture.levels(); ++level) {
                gl4.glTexSubImage3D(GL_TEXTURE_2D_ARRAY, level,
                        0, 0, 0,
                        texture.dimensions(level)[0], texture.dimensions(level)[1], 1,
                        format.external.value, format.type.value,
                        texture.data(0, 0, level));
            }

            gl4.glTextureView(textureName[Texture.DIFFUSE_RGB.ordinal()], GL_TEXTURE_2D_ARRAY,
                    textureName[Texture.DIFFUSE_SRGB.ordinal()], GL_RGBA8, 0, texture.levels(), 0, 1);

            gl4.glActiveTexture(GL_TEXTURE0);
            gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.COLORBUFFER.ordinal()]);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            gl4.glTexStorage2D(GL_TEXTURE_2D, 1, GL_SRGB8_ALPHA8, windowSize.x * framebufferScale,
                    windowSize.y * framebufferScale);

            gl4.glActiveTexture(GL_TEXTURE0);
            gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.RENDERBUFFER.ordinal()]);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
            gl4.glTexStorage2D(GL_TEXTURE_2D, 1, GL_DEPTH_COMPONENT24, windowSize.x * framebufferScale,
                    windowSize.y * framebufferScale);

            gl4.glPixelStorei(GL_UNPACK_ALIGNMENT, 4);

        } catch (IOException ex) {
            Logger.getLogger(Gl_430_fbo_srgb_decode.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(Program.MAX.ordinal(), vertexArrayName, 0);
        gl4.glBindVertexArray(vertexArrayName[Program.TEXTURE.ordinal()]);
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 2 * 2 * Float.BYTES, 0);
            gl4.glVertexAttribPointer(Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, 2 * 2 * Float.BYTES, 2 * Float.BYTES);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl4.glEnableVertexAttribArray(Semantic.Attr.TEXCOORD);

            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT.ordinal()]);
        }
        gl4.glBindVertexArray(0);

        gl4.glBindVertexArray(vertexArrayName[Program.SPLASH.ordinal()]);
        gl4.glBindVertexArray(0);

        return true;
    }

    private boolean initFramebuffer(GL4 gl4) {

        gl4.glGenFramebuffers(1, framebufferName, 0);
        gl4.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[0]);
        gl4.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, textureName[Texture.COLORBUFFER.ordinal()], 0);
        gl4.glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, textureName[Texture.RENDERBUFFER.ordinal()], 0);

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
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM.ordinal()]);
            ByteBuffer pointer = gl4.glMapBufferRange(GL_UNIFORM_BUFFER, 0, projection.length * Float.BYTES,
                    GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            //glm::mat4 Projection = glm::perspectiveFov(glm::pi<float>() * 0.25f, 640.f, 480.f, 0.1f, 100.0f);
            FloatUtil.makePerspective(projection, 0, true, (float) Math.PI * 0.25f,
                    (float) windowSize.x / windowSize.y, 0.1f, 100.0f);

            FloatUtil.multMatrix(projection, view());

            pointer.asFloatBuffer().put(projection).rewind();

            // Make sure the uniform buffer is uploaded
            gl4.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        {
            gl4.glEnable(GL_DEPTH_TEST);
            gl4.glDepthFunc(GL_LESS);

            gl4.glViewport(0, 0, windowSize.x * framebufferScale, windowSize.y * framebufferScale);

            gl4.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[0]);

            // Convert linear clear color to sRGB color space, FramebufferName is a sRGB FBO
            gl4.glEnable(GL_FRAMEBUFFER_SRGB);
            float[] depth = {1.0f};
            gl4.glClearBufferfv(GL_DEPTH, 0, depth, 0);
            gl4.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 0.5f, 0.0f, 1.0f}, 0);

            // TextureName[texture::DIFFUSE] is a sRGB texture which sRGB conversion on fetch has been disabled
            // Hence in the shader, the value is stored as sRGB so we should not convert it to sRGB.
            gl4.glDisable(GL_FRAMEBUFFER_SRGB);
            gl4.glUseProgram(programName[Program.TEXTURE.ordinal()]);

            gl4.glActiveTexture(GL_TEXTURE0);
            gl4.glBindTexture(GL_TEXTURE_2D_ARRAY, textureName[Texture.DIFFUSE_RGB.ordinal()]);
            gl4.glBindVertexArray(vertexArrayName[Program.TEXTURE.ordinal()]);
            gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0,
                    bufferName[Buffer.TRANSFORM.ordinal()]);

            gl4.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 2, 0);
        }

        {
            gl4.glDisable(GL_DEPTH_TEST);

            gl4.glViewport(0, 0, windowSize.x, windowSize.y);

            gl4.glBindFramebuffer(GL_FRAMEBUFFER, 0);

            gl4.glUseProgram(programName[Program.SPLASH.ordinal()]);

            gl4.glActiveTexture(GL_TEXTURE0);
            gl4.glBindVertexArray(vertexArrayName[Program.SPLASH.ordinal()]);
            gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.COLORBUFFER.ordinal()]);

            gl4.glDrawArraysInstanced(GL_TRIANGLES, 0, 3, 1);
        }

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteFramebuffers(1, framebufferName, 0);
        gl4.glDeleteProgram(programName[Program.SPLASH.ordinal()]);
        gl4.glDeleteProgram(programName[Program.TEXTURE.ordinal()]);

        gl4.glDeleteBuffers(Buffer.MAX.ordinal(), bufferName, 0);
        gl4.glDeleteTextures(Texture.MAX.ordinal(), textureName, 0);
        gl4.glDeleteVertexArrays(Program.MAX.ordinal(), vertexArrayName, 0);

        return true;
    }
}
