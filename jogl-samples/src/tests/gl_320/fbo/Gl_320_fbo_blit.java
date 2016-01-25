/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_320.fbo;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_COLOR_ATTACHMENT0;
import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DRAW_FRAMEBUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_FRAMEBUFFER;
import static com.jogamp.opengl.GL.GL_LINEAR;
import static com.jogamp.opengl.GL.GL_LINEAR_MIPMAP_LINEAR;
import static com.jogamp.opengl.GL.GL_READ_FRAMEBUFFER;
import static com.jogamp.opengl.GL.GL_RENDERBUFFER;
import static com.jogamp.opengl.GL.GL_RGBA;
import static com.jogamp.opengl.GL.GL_RGBA8;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNSIGNED_BYTE;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_BASE_LEVEL;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_MAX_LEVEL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.BufferUtils;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import jgli.Texture2d;
import jglm.Vec2i;

/**
 *
 * @author elect
 */
public class Gl_320_fbo_blit extends Test {

    public static void main(String[] args) {
        Gl_320_fbo_blit gl_320_fbo_blit = new Gl_320_fbo_blit();
    }

    public Gl_320_fbo_blit() {
        super("gl-320-fbo-blit", Profile.CORE, 3, 2);
    }

    private final String SHADERS_SOURCE = "fbo-blit";
    private final String SHADERS_ROOT = "src/data/gl_320/fbo";
    private final String TEXTURE_DIFFUSE = "kueken7_rgba8_srgb.dds";

    // With DDS textures, v texture coordinate are reversed, from top to bottom
    private int vertexCount = 6;
    private int vertexSize = vertexCount * 2 * 2 * Float.BYTES;
    private float[] vertexData = {
        -1.5f, -1.5f, 0.0f, 0.0f,
        +1.5f, -1.5f, 1.0f, 0.0f,
        +1.5f, +1.5f, 1.0f, 1.0f,
        +1.5f, +1.5f, 1.0f, 1.0f,
        -1.5f, +1.5f, 0.0f, 1.0f,
        -1.5f, -1.5f, 0.0f, 0.0f};

    private enum Framebuffer {

        RENDER,
        RESOLVE,
        MAX
    }

    private enum Texture {

        DIFFUSE,
        COLORBUFFER,
        MAX
    }

    private int[] textureName = new int[Texture.MAX.ordinal()],
            framebufferName = new int[Framebuffer.MAX.ordinal()], vertexArrayName = new int[1],
            bufferName = new int[1], colorRenderbufferName = new int[1];
    private int programName, uniformMvp, uniformDiffuse;
    private Vec2i FRAMEBUFFER_SIZE = new Vec2i(512, 512);
    private float[] perspective = new float[16], model = new float[16];

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
            validated = initTexture(gl3);
        }
        if (validated) {
            validated = initFramebuffer(gl3);
        }
        if (validated) {
            validated = initVertexArray(gl3);
        }

        return validated && checkError(gl3, "begin");
    }

    private boolean initProgram(GL3 gl3) {

        boolean validated = true;

        // Create program
        if (validated) {

            ShaderCode vertShader = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragShader = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

            ShaderProgram program = new ShaderProgram();
            program.add(vertShader);
            program.add(fragShader);
            program.init(gl3);

            programName = program.program();

            gl3.glBindAttribLocation(programName, Semantic.Attr.POSITION, "position");
            gl3.glBindAttribLocation(programName, Semantic.Attr.TEXCOORD, "texCoord");
            gl3.glBindFragDataLocation(programName, Semantic.Frag.COLOR, "color");

            program.link(gl3, System.out);
        }
        if (validated) {

            uniformMvp = gl3.glGetUniformLocation(programName, "mvp");
            uniformDiffuse = gl3.glGetUniformLocation(programName, "diffuse");
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
            jgli.Texture2d texture = new Texture2d(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE));

            gl3.glGenTextures(Texture.MAX.ordinal(), textureName, 0);
            gl3.glActiveTexture(GL_TEXTURE0);
            gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.DIFFUSE.ordinal()]);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, texture.levels() - 1);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

            jgli.Gl.Format format = jgli.Gl.translate(texture.format());

            for (int level = 0; level < texture.levels(); level++) {

                gl3.glTexImage2D(GL_TEXTURE_2D, level,
                        format.internal.value,
                        texture.dimensions(level)[0], texture.dimensions(level)[1],
                        0,
                        format.external.value, format.type.value,
                        texture.data(level));
            }
            gl3.glGenerateMipmap(GL_TEXTURE_2D); // Allocate all mipmaps memory

            gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.COLORBUFFER.ordinal()]);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            gl3.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, FRAMEBUFFER_SIZE.x,
                    FRAMEBUFFER_SIZE.y, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);

            gl3.glBindTexture(GL_TEXTURE_2D, 0);

        } catch (IOException ex) {
            Logger.getLogger(Gl_320_fbo_blit.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    private boolean initFramebuffer(GL3 gl3) {

        gl3.glGenFramebuffers(Framebuffer.MAX.ordinal(), framebufferName, 0);

        gl3.glGenRenderbuffers(1, colorRenderbufferName, 0);
        gl3.glBindRenderbuffer(GL_RENDERBUFFER, colorRenderbufferName[0]);
        gl3.glRenderbufferStorage(GL_RENDERBUFFER, GL_RGBA8, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y);

        gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[Framebuffer.RENDER.ordinal()]);
        gl3.glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
                GL_RENDERBUFFER, colorRenderbufferName[0]);
        if (!isFramebufferComplete(gl3, framebufferName[Framebuffer.RENDER.ordinal()])) {
            return false;
        }
        gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);

        gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[Framebuffer.RESOLVE.ordinal()]);
        gl3.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, textureName[Texture.COLORBUFFER.ordinal()], 0);
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

        gl3.glUseProgram(programName);
        gl3.glUniform1i(uniformDiffuse, 0);

        // Pass 1
        gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[Framebuffer.RENDER.ordinal()]);
        gl3.glViewport(0, 0, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y);
        gl3.glClearBufferfv(GL_COLOR, 0, new float[]{0.0f, 0.5f, 1.0f, 1.0f}, 0);
        renderFBO(gl3);

        gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);

        // Generate FBO mipmaps
        gl3.glActiveTexture(GL_TEXTURE0);
        gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.COLORBUFFER.ordinal()]);
        gl3.glGenerateMipmap(GL_TEXTURE_2D);
        gl3.glBindTexture(GL_TEXTURE_2D, 0);

        // Blit framebuffers
        int border = 2;
        int tile = 4;
        gl3.glBindFramebuffer(GL_READ_FRAMEBUFFER, framebufferName[Framebuffer.RENDER.ordinal()]);
        gl3.glBindFramebuffer(GL_DRAW_FRAMEBUFFER, framebufferName[Framebuffer.RESOLVE.ordinal()]);
        gl3.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 0.5f, 0.0f, 1.0f}, 0);

        for (int j = 0; j < tile; ++j) {
            for (int i = 0; i < tile; ++i) {
                if (((i + j) % 2) != 0) {
                    continue;
                }

                gl3.glBlitFramebuffer(0, 0, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y,
                        FRAMEBUFFER_SIZE.x / tile * (i + 0) + border,
                        FRAMEBUFFER_SIZE.x / tile * (j + 0) + border,
                        FRAMEBUFFER_SIZE.y / tile * (i + 1) - border,
                        FRAMEBUFFER_SIZE.y / tile * (j + 1) - border,
                        GL_COLOR_BUFFER_BIT, GL_LINEAR);
            }
        }

        // Pass 2
        gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        gl3.glViewport(0, 0, windowSize.x, windowSize.y);
        gl3.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, 0);
        renderFB(gl3);

        return true;
    }

    private void renderFBO(GL3 gl3) {

        FloatUtil.makePerspective(perspective, 0, true, (float) Math.PI * 0.25f,
                (float) FRAMEBUFFER_SIZE.y / FRAMEBUFFER_SIZE.x, 0.1f, 100.0f);
        FloatUtil.makeScale(model, true, 1.0f, -1.0f, 1.0f);
        FloatUtil.multMatrix(perspective, view());
        FloatUtil.multMatrix(perspective, model);

        gl3.glUniformMatrix4fv(uniformMvp, 1, false, perspective, 0);

        gl3.glActiveTexture(GL_TEXTURE0);
        gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.DIFFUSE.ordinal()]);
        gl3.glBindVertexArray(vertexArrayName[0]);

        gl3.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 1);
    }

    private void renderFB(GL3 gl3) {

        FloatUtil.makePerspective(perspective, 0, true, (float) Math.PI * 0.25f,
                (float) FRAMEBUFFER_SIZE.y / FRAMEBUFFER_SIZE.x, 0.1f, 100.0f);
        FloatUtil.makeIdentity(model);
        FloatUtil.multMatrix(perspective, view());
        FloatUtil.multMatrix(perspective, model);

        gl3.glUniformMatrix4fv(uniformMvp, 1, false, perspective, 0);

        gl3.glActiveTexture(GL_TEXTURE0);
        gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.COLORBUFFER.ordinal()]);
        gl3.glBindVertexArray(vertexArrayName[0]);

        gl3.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 1);
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;

        gl3.glDeleteProgram(programName);
        gl3.glDeleteBuffers(1, bufferName, 0);
        gl3.glDeleteTextures(Texture.MAX.ordinal(), textureName, 0);
        gl3.glDeleteRenderbuffers(1, colorRenderbufferName, 0);
        gl3.glDeleteFramebuffers(Framebuffer.MAX.ordinal(), framebufferName, 0);
        gl3.glDeleteVertexArrays(1, vertexArrayName, 0);

        return true;
    }
}
