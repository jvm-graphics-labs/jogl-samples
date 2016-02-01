/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_320.fbo;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2ES3.*;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.BufferUtils;
import framework.Caps;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import jgli.Texture2d;

/**
 *
 * @author GBarbieri
 */
public class Gl_320_fbo_multisample_integer extends Test {

    public static void main(String[] args) {
        Gl_320_fbo_multisample_integer gl_320_fbo_multisample_integer = new Gl_320_fbo_multisample_integer();
    }

    public Gl_320_fbo_multisample_integer() {
        super("Gl-320-fbo-multisample-integer", Profile.CORE, 3, 2);
    }

    private final String SHADERS_SOURCE1 = "fbo-multisample-integer";
    private final String SHADERS_SOURCE2 = "texture-integer";
    private final String SHADERS_ROOT_SOURCE1 = "src/data/gl_320/fbo";
    private final String SHADERS_ROOT_SOURCE2 = "src/data/gl_320/texture";
    private final String TEXTURE_DIFFUSE = "kueken7_rgb8_unorm.dds";

    private int framebufferSize = 4;

    // With DDS textures, v texture coordinate are reversed, from top to bottom
    private int vertexCount = 6;
    private int vertexSize = vertexCount * 2 * 2 * Float.BYTES;
    private float[] vertexData = {
        -1.0f, -1.0f, 0.0f, 1.0f,
        +1.0f, -1.0f, 1.0f, 1.0f,
        +1.0f, +1.0f, 1.0f, 0.0f,
        +1.0f, +1.0f, 1.0f, 0.0f,
        -1.0f, +1.0f, 0.0f, 0.0f,
        -1.0f, -1.0f, 0.0f, 1.0f};

    private class Texture {

        public static final int DIFFUSE = 0;
        public static final int COLORBUFFER = 1;
        public static final int MULTISAMPLE = 2;
        public static final int MAX = 3;
    }

    private class Program {

        public static final int RENDER = 0;
        public static final int SPLASH = 1;
        public static final int MAX = 2;
    }

    private class Framebuffer {

        public static final int RENDER = 0;
        public static final int RESOLVE = 1;
        public static final int MAX = 2;
    }

    private class Shader {

        public static final int VERT1 = 0;
        public static final int FRAG1 = 1;
        public static final int VERT2 = 2;
        public static final int FRAG2 = 3;
        public static final int MAX = 4;
    }

    private int[] vertexArrayName = new int[1], bufferName = new int[1], shaderName = new int[Shader.MAX],
            textureName = new int[Texture.MAX], framebufferName = new int[Framebuffer.MAX],
            programName = new int[Program.MAX], uniformMvp = new int[Program.MAX],
            uniformDiffuse = new int[Program.MAX];
    private float[] projection = new float[16], view = new float[16], model = new float[16], mvp = new float[16];

    @Override
    protected boolean begin(GL gl) {

        GL3 gl3 = (GL3) gl;

        Caps caps = new Caps(gl3, Profile.CORE);

        // Multisample integer texture is optional
        boolean validated = caps.limits.MAX_INTEGER_SAMPLES > 1;

        if (validated) {
            validated = initProgram(gl3);
        }
        if (validated) {
            validated = initBuffer(gl3);
        }
        if (validated) {
            validated = initVertexArray(gl3);
        }
        if (validated) {
            validated = initTexture(gl3);
        }
        if (validated) {
            validated = initFramebuffer(gl3);
        }

        return validated & checkError(gl3, "begin");
    }

    private boolean initProgram(GL3 gl3) {

        boolean validated = true;

        ShaderCode[] shaderCodes = new ShaderCode[Shader.MAX];

        if (validated) {

            shaderCodes[Shader.VERT1] = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT_SOURCE1, null, SHADERS_SOURCE1, "vert", null, true);
            shaderCodes[Shader.FRAG1] = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT_SOURCE1, null, SHADERS_SOURCE1, "frag", null, true);

            ShaderProgram program = new ShaderProgram();
            program.add(shaderCodes[Shader.VERT1]);
            program.add(shaderCodes[Shader.FRAG1]);

            program.init(gl3);

            programName[Program.RENDER] = program.program();

            gl3.glBindAttribLocation(programName[Program.RENDER], Semantic.Attr.POSITION, "position");
            gl3.glBindAttribLocation(programName[Program.RENDER], Semantic.Attr.TEXCOORD, "texCoord");
            gl3.glBindFragDataLocation(programName[Program.RENDER], Semantic.Frag.COLOR, "color");

            program.link(gl3, System.out);
        }

        if (validated) {

            shaderCodes[Shader.VERT2] = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT_SOURCE2, null, SHADERS_SOURCE2, "vert", null, true);
            shaderCodes[Shader.FRAG2] = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT_SOURCE2, null, SHADERS_SOURCE2, "frag", null, true);

            ShaderProgram program = new ShaderProgram();
            program.add(shaderCodes[Shader.VERT2]);
            program.add(shaderCodes[Shader.FRAG2]);

            program.init(gl3);

            programName[Program.SPLASH] = program.program();

            gl3.glBindAttribLocation(programName[Program.SPLASH], Semantic.Attr.POSITION, "position");
            gl3.glBindAttribLocation(programName[Program.SPLASH], Semantic.Attr.TEXCOORD, "texCoord");
            gl3.glBindFragDataLocation(programName[Program.SPLASH], Semantic.Frag.COLOR, "color");

            program.link(gl3, System.out);
        }

        if (validated) {

            uniformMvp[Program.RENDER]
                    = gl3.glGetUniformLocation(programName[Program.RENDER], "mvp");
            uniformDiffuse[Program.RENDER]
                    = gl3.glGetUniformLocation(programName[Program.RENDER], "diffuse");

            uniformMvp[Program.SPLASH]
                    = gl3.glGetUniformLocation(programName[Program.SPLASH], "mvp");
            uniformDiffuse[Program.SPLASH]
                    = gl3.glGetUniformLocation(programName[Program.SPLASH], "diffuse");
        }
        return validated & checkError(gl3, "initProgram");
    }

    private boolean initBuffer(GL3 gl3) {

        gl3.glGenBuffers(1, bufferName, 0);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[0]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl3.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(vertexBuffer);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        return checkError(gl3, "initBuffer");
    }

    private boolean initTexture(GL3 gl3) {

        try {
            int[] maxSampleMaskWords = new int[1];
            int[] maxColorTextureSamples = new int[1];
            int[] maxDepthTextureSamples = new int[1];
            int[] maxIntegerSamples = new int[1];

            gl3.glGetIntegerv(GL_MAX_SAMPLE_MASK_WORDS, maxSampleMaskWords, 0);
            gl3.glGetIntegerv(GL_MAX_COLOR_TEXTURE_SAMPLES, maxColorTextureSamples, 0);
            gl3.glGetIntegerv(GL_MAX_DEPTH_TEXTURE_SAMPLES, maxDepthTextureSamples, 0);
            gl3.glGetIntegerv(GL_MAX_INTEGER_SAMPLES, maxIntegerSamples, 0);

            jgli.Texture2d texture = new Texture2d(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE));

            gl3.glGenTextures(Texture.MAX, textureName, 0);
            gl3.glActiveTexture(GL_TEXTURE0);
            gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.DIFFUSE]);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

            gl3.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

            for (int level = 0; level < texture.levels(); ++level) {
                gl3.glTexImage2D(GL_TEXTURE_2D, level,
                        GL_RGBA8UI,
                        texture.dimensions(level)[0], texture.dimensions(level)[1],
                        0,
                        GL_RGB_INTEGER, GL_UNSIGNED_BYTE,
                        texture.data(level));
            }
            gl3.glBindTexture(GL_TEXTURE_2D, 0);

            gl3.glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, textureName[Texture.MULTISAMPLE]);
            gl3.glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, maxIntegerSamples[0], GL_RGBA8UI,
                    windowSize.x / framebufferSize, windowSize.y / framebufferSize, true);

            gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.COLORBUFFER]);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
            gl3.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8UI, windowSize.x / framebufferSize,
                    windowSize.y / framebufferSize, 0, GL_RGBA_INTEGER, GL_UNSIGNED_BYTE, null);

            gl3.glBindTexture(GL_TEXTURE_2D, 0);

        } catch (IOException ex) {
            Logger.getLogger(Gl_320_fbo_multisample_integer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return checkError(gl3, "initTexture");
    }

    private boolean initFramebuffer(GL3 gl3) {

        gl3.glGenFramebuffers(Framebuffer.MAX, framebufferName, 0);

        gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[Framebuffer.RENDER]);
        gl3.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
                textureName[Texture.MULTISAMPLE], 0);
        if (!isFramebufferComplete(gl3, framebufferName[Framebuffer.RENDER])) {
            return false;
        }
        gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);

        gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[Framebuffer.RESOLVE]);
        gl3.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
                textureName[Texture.COLORBUFFER], 0);
        if (!isFramebufferComplete(gl3, framebufferName[Framebuffer.RESOLVE])) {
            return false;
        }
        gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);

        return checkError(gl3, "initFramebuffer");
    }

    private boolean initVertexArray(GL3 gl3) {

        gl3.glGenVertexArrays(1, vertexArrayName, 0);
        gl3.glBindVertexArray(vertexArrayName[0]);
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[0]);
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 2 * 2 * Float.BYTES, 0);
            gl3.glVertexAttribPointer(Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, 2 * 2 * Float.BYTES, 2 * Float.BYTES);
            gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl3.glEnableVertexAttribArray(Semantic.Attr.TEXCOORD);
        }
        gl3.glBindVertexArray(0);

        return checkError(gl3, "initVertexArray");
    }

    @Override
    protected boolean render(GL gl) {

        GL3 gl3 = (GL3) gl;

        // Clear the framebuffer
        gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        gl3.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 0.5f, 0.0f, 1.0f}, 0);

        // Pass 1
        // Render the scene in a multisampled framebuffer
        gl3.glEnable(GL_MULTISAMPLE);
        renderFBO(gl3, framebufferName[Framebuffer.RENDER]);
        gl3.glDisable(GL_MULTISAMPLE);

        // Resolved multisampling
        gl3.glBindFramebuffer(GL_READ_FRAMEBUFFER, framebufferName[Framebuffer.RENDER]);
        gl3.glBindFramebuffer(GL_DRAW_FRAMEBUFFER, framebufferName[Framebuffer.RESOLVE]);
        gl3.glBlitFramebuffer(
                0, 0, windowSize.x / framebufferSize, windowSize.y / framebufferSize,
                0, 0, windowSize.x / framebufferSize, windowSize.y / framebufferSize,
                GL_COLOR_BUFFER_BIT, GL_NEAREST);
        gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);

        // Pass 2
        // Render the colorbuffer from the multisampled framebuffer
        renderFB(gl3, textureName[Texture.COLORBUFFER]);

        return checkError(gl3, "render");
    }

    private void renderFBO(GL3 gl3, int framebuffer) {

        FloatUtil.makeOrtho(projection, 0, true, -1.1f, 1.1f, 1.1f, -1.1f, 1.1f, -1.1f);
        FloatUtil.makeIdentity(view);
        FloatUtil.makeRotationAxis(model, 0, -0.3f, 0.f, 0.f, 1.f, new float[3]);
        FloatUtil.multMatrix(projection, view, mvp);
        FloatUtil.multMatrix(mvp, model);

        gl3.glUseProgram(programName[Program.RENDER]);
        gl3.glUniform1i(uniformDiffuse[Program.RENDER], 0);
        gl3.glUniformMatrix4fv(uniformMvp[Program.RENDER], 1, false, mvp, 0);

        gl3.glViewport(0, 0, windowSize.x / framebufferSize, windowSize.y / framebufferSize);
        gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);
        gl3.glClearBufferuiv(GL_COLOR, 0, new int[]{0, 128, 255, 255}, 0);

        gl3.glActiveTexture(GL_TEXTURE0);
        gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.DIFFUSE]);
        gl3.glBindVertexArray(vertexArrayName[0]);

        gl3.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 1);

        checkError(gl3, "renderFBO");
    }

    private void renderFB(GL3 gl3, int textureName) {

        FloatUtil.makePerspective(projection, 0, true, (float) Math.PI * 0.25f,
                (float) windowSize.x / windowSize.y, 0.1f, 100.0f);
        FloatUtil.makeIdentity(model);
        FloatUtil.multMatrix(projection, view(), mvp);
        FloatUtil.multMatrix(mvp, model);

        gl3.glUseProgram(programName[Program.SPLASH]);
        gl3.glUniform1i(uniformDiffuse[Program.SPLASH], 0);
        gl3.glUniformMatrix4fv(uniformMvp[Program.SPLASH], 1, false, mvp, 0);

        gl3.glViewport(0, 0, windowSize.x, windowSize.y);
        gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);

        gl3.glActiveTexture(GL_TEXTURE0);
        gl3.glBindTexture(GL_TEXTURE_2D, textureName);
        gl3.glBindVertexArray(vertexArrayName[0]);

        gl3.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 1);

        checkError(gl3, "renderFB");
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;

        gl3.glDeleteBuffers(1, bufferName, 0);
        gl3.glDeleteProgram(programName[Program.RENDER]);
        gl3.glDeleteProgram(programName[Program.SPLASH]);
        gl3.glDeleteTextures(Texture.MAX, textureName, 0);
        gl3.glDeleteFramebuffers(Framebuffer.MAX, framebufferName, 0);
        gl3.glDeleteVertexArrays(1, vertexArrayName, 0);

        return checkError(gl3, "end");
    }

}
