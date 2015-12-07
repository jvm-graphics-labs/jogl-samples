/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl320.fbo;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_COLOR_ATTACHMENT0;
import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DRAW_FRAMEBUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_FRAMEBUFFER;
import static com.jogamp.opengl.GL.GL_NEAREST;
import static com.jogamp.opengl.GL.GL_READ_FRAMEBUFFER;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNPACK_ALIGNMENT;
import static com.jogamp.opengl.GL.GL_UNSIGNED_BYTE;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import static com.jogamp.opengl.GL2ES3.GL_RGBA8UI;
import static com.jogamp.opengl.GL2ES3.GL_RGBA_INTEGER;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_BASE_LEVEL;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_MAX_LEVEL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.Semantic;
import framework.Test;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author GBarbieri
 */
public class Gl_320_fbo_integer_blit extends Test {

    public static void main(String[] args) {
        Gl_320_fbo_integer_blit gl_320_fbo_integer_blit = new Gl_320_fbo_integer_blit();
    }

    public Gl_320_fbo_integer_blit() {
        super("Gl-320-fbo-integer-blit", 3, 2);
    }

    private final String SHADERS_SOURCE1 = "fbo-integer-render";
    private final String SHADERS_SOURCE2 = "fbo-integer-splash";
    private final String SHADERS_ROOT = "src/data/gl_320/fbo";
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

    private enum Texture {

        DIFFUSE,
        RENDERBUFFER,
        SPLASHBUFFER,
        MAX
    }

    private enum Program {

        RENDER,
        SPLASH,
        MAX
    }

    private enum Framebuffer {

        RENDER,
        RESOLVE,
        MAX
    }

    private enum Shader {

        VERT1,
        FRAG1,
        VERT2,
        FRAG2,
        MAX
    }

    private int[] vertexArrayName = new int[1], bufferName = new int[1], textureName = new int[Texture.MAX.ordinal()],
            framebufferName = new int[Framebuffer.MAX.ordinal()], programName = new int[Program.MAX.ordinal()],
            uniformMvp = new int[Program.MAX.ordinal()], uniformDiffuse = new int[Program.MAX.ordinal()];
    private float[] projection = new float[16], view = new float[16], model = new float[16], mvp = new float[16];

    @Override
    protected boolean begin(GL gl) {

        GL3 gl3 = (GL3) gl;

        boolean validated = true;

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

        return validated && checkError(gl3, "begin");
    }

    private boolean initProgram(GL3 gl3) {

        boolean validated = true;

        ShaderCode[] shaderCodes = new ShaderCode[Shader.MAX.ordinal()];

        // Create program
        if (validated) {

            shaderCodes[Shader.VERT1.ordinal()] = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE1, "vert", null, true);
            shaderCodes[Shader.FRAG1.ordinal()] = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE1, "frag", null, true);

            ShaderProgram program = new ShaderProgram();
            program.add(shaderCodes[Shader.VERT1.ordinal()]);
            program.add(shaderCodes[Shader.FRAG1.ordinal()]);
            program.init(gl3);

            programName[Program.RENDER.ordinal()] = program.program();

            gl3.glBindAttribLocation(programName[Program.RENDER.ordinal()], Semantic.Attr.POSITION, "position");
            gl3.glBindAttribLocation(programName[Program.RENDER.ordinal()], Semantic.Attr.TEXCOORD, "texCoord");
            gl3.glBindFragDataLocation(programName[Program.RENDER.ordinal()], Semantic.Frag.COLOR, "color");

            program.link(gl3, System.out);
        }
        if (validated) {

            shaderCodes[Shader.VERT2.ordinal()] = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE2, "vert", null, true);
            shaderCodes[Shader.FRAG2.ordinal()] = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE2, "frag", null, true);

            ShaderProgram program = new ShaderProgram();
            program.add(shaderCodes[Shader.VERT2.ordinal()]);
            program.add(shaderCodes[Shader.FRAG2.ordinal()]);
            program.init(gl3);

            programName[Program.SPLASH.ordinal()] = program.program();

            gl3.glBindAttribLocation(programName[Program.SPLASH.ordinal()], Semantic.Attr.POSITION, "position");
            gl3.glBindAttribLocation(programName[Program.SPLASH.ordinal()], Semantic.Attr.TEXCOORD, "texCoord");
            gl3.glBindFragDataLocation(programName[Program.SPLASH.ordinal()], Semantic.Frag.COLOR, "color");

            program.link(gl3, System.out);
        }
        if (validated) {

            uniformMvp[Program.RENDER.ordinal()]
                    = gl3.glGetUniformLocation(programName[Program.RENDER.ordinal()], "mvp");
            uniformDiffuse[Program.RENDER.ordinal()]
                    = gl3.glGetUniformLocation(programName[Program.RENDER.ordinal()], "diffuse");
            uniformMvp[Program.SPLASH.ordinal()]
                    = gl3.glGetUniformLocation(programName[Program.SPLASH.ordinal()], "mvp");
            uniformDiffuse[Program.SPLASH.ordinal()]
                    = gl3.glGetUniformLocation(programName[Program.SPLASH.ordinal()], "diffuse");
        }
        return validated & checkError(gl3, "initProgram");
    }

    private boolean initBuffer(GL3 gl3) {

        gl3.glGenBuffers(1, bufferName, 0);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[0]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl3.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        return checkError(gl3, "initBuffer");
    }

    private boolean initTexture(GL3 gl3) {

        try {

            jgli.Texture texture = jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE);
            gl3.glGenTextures(Texture.MAX.ordinal(), textureName, 0);
            gl3.glActiveTexture(GL_TEXTURE0);
            gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.DIFFUSE.ordinal()]);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

            gl3.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

            jgli.Gl.Format format = jgli.Gl.instance.translate(jgli.Format.FORMAT_RGB8_UINT);

            for (int level = 0; level < texture.levels(); level++) {
                gl3.glTexImage2D(GL_TEXTURE_2D, level,
                        format.internal.value,
                        texture.dimensions(level)[0], texture.dimensions(level)[1],
                        0,
                        format.external.value, format.type.value,
                        texture.data(0, 0, level));
            }
            gl3.glPixelStorei(GL_UNPACK_ALIGNMENT, 4);
            gl3.glBindTexture(GL_TEXTURE_2D, 0);

            gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.RENDERBUFFER.ordinal()]);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            gl3.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8UI, windowSize.x / framebufferSize,
                    windowSize.y / framebufferSize, 0, GL_RGBA_INTEGER, GL_UNSIGNED_BYTE, null);
            gl3.glBindTexture(GL_TEXTURE_2D, 0);

            gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.SPLASHBUFFER.ordinal()]);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            gl3.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8UI, windowSize.x / framebufferSize,
                    windowSize.y / framebufferSize, 0, GL_RGBA_INTEGER, GL_UNSIGNED_BYTE, null);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
            gl3.glBindTexture(GL_TEXTURE_2D, 0);

        } catch (IOException ex) {
            Logger.getLogger(Gl_320_fbo_integer_blit.class.getName()).log(Level.SEVERE, null, ex);
        }
        return checkError(gl3, "initTexture");
    }

    private boolean initFramebuffer(GL3 gl3) {

        gl3.glGenFramebuffers(Framebuffer.MAX.ordinal(), framebufferName, 0);

        gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[Framebuffer.RENDER.ordinal()]);
        gl3.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, textureName[Texture.RENDERBUFFER.ordinal()], 0);
        if (!isFramebufferComplete(gl3, framebufferName[Framebuffer.RENDER.ordinal()])) {
            return false;
        }
        gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);

        gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[Framebuffer.RESOLVE.ordinal()]);
        gl3.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, textureName[Texture.SPLASHBUFFER.ordinal()], 0);
        if (!isFramebufferComplete(gl3, framebufferName[Framebuffer.RESOLVE.ordinal()])) {
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

        // Pass 1
        {
            gl3.glViewport(0, 0,
                    windowSize.x / framebufferSize,
                    windowSize.y / framebufferSize);
            gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[Framebuffer.RENDER.ordinal()]);
            gl3.glClearBufferuiv(GL_COLOR, 0, new int[]{0, 128, 255, 255}, 0);

            FloatUtil.makeOrtho(projection, 0, true, -2.0f, 2.0f, 2.0f, -2.0f, 2.0f, -2.0f);
            FloatUtil.makeIdentity(view);
            FloatUtil.makeRotationAxis(model, 0, -0.3f, 0.f, 0.f, 1.f, tmpVec);

            FloatUtil.multMatrix(projection, view, mvp);
            FloatUtil.multMatrix(mvp, model);

            gl3.glUseProgram(programName[Program.RENDER.ordinal()]);
            gl3.glUniform1i(uniformDiffuse[Program.RENDER.ordinal()], 0);
            gl3.glUniformMatrix4fv(uniformMvp[Program.RENDER.ordinal()], 1, false, mvp, 0);

            gl3.glActiveTexture(GL_TEXTURE0);
            gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.DIFFUSE.ordinal()]);
            gl3.glBindVertexArray(vertexArrayName[0]);

            gl3.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 1);
        }

        // Resolved multisampling
        {
            gl3.glBindFramebuffer(GL_READ_FRAMEBUFFER, framebufferName[Framebuffer.RENDER.ordinal()]);
            gl3.glBindFramebuffer(GL_DRAW_FRAMEBUFFER, framebufferName[Framebuffer.RESOLVE.ordinal()]);
            gl3.glBlitFramebuffer(
                    0, 0,
                    windowSize.x / framebufferSize,
                    windowSize.y / framebufferSize,
                    0, 0,
                    windowSize.x / framebufferSize,
                    windowSize.y / framebufferSize,
                    GL_COLOR_BUFFER_BIT, GL_NEAREST);
            gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        }

        // Pass 2
        {
            FloatUtil.makePerspective(projection, 0, true, (float) Math.PI * 0.25f,
                    (float) windowSize.x / windowSize.y, 0.1f, 100.0f);
            FloatUtil.makeIdentity(model);
            FloatUtil.multMatrix(projection, view(), mvp);
            FloatUtil.multMatrix(mvp, model);

            gl3.glUseProgram(programName[Program.SPLASH.ordinal()]);
            gl3.glUniform1i(uniformDiffuse[Program.SPLASH.ordinal()], 0);
            gl3.glUniformMatrix4fv(uniformMvp[Program.SPLASH.ordinal()], 1, false, mvp, 0);

            gl3.glViewport(0, 0, windowSize.x, windowSize.y);
            gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);
            gl3.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 0.5f, 0.0f, 1.0f}, 0);

            gl3.glActiveTexture(GL_TEXTURE0);
            gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.SPLASHBUFFER.ordinal()]);
            gl3.glBindVertexArray(vertexArrayName[0]);

            gl3.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 1);
        }

        return checkError(gl3, "render");
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;

        gl3.glDeleteBuffers(1, bufferName, 0);
        gl3.glDeleteProgram(programName[Program.RENDER.ordinal()]);
        gl3.glDeleteProgram(programName[Program.SPLASH.ordinal()]);
        gl3.glDeleteTextures(Texture.MAX.ordinal(), textureName, 0);
        gl3.glDeleteFramebuffers(Framebuffer.MAX.ordinal(), framebufferName, 0);
        gl3.glDeleteVertexArrays(1, vertexArrayName, 0);

        return checkError(gl3, "end");
    }
}
