/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_400;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2ES3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.Profile;
import framework.Test;
import jglm.Vec2i;
import jglm.Vec4;

/**
 *
 * @author GBarbieri
 */
public class Gl_400_fbo_rtt extends Test {

    public static void main(String[] args) {
        Gl_400_fbo_rtt gl_400_fbo_rtt = new Gl_400_fbo_rtt();
    }

    public Gl_400_fbo_rtt() {
        super("gl-400-fbo-rtt", Profile.CORE, 4, 0);
    }

    private final String SHADERS_SOURCE = "fbo-rtt";
    private final String SHADERS_ROOT = "src/data/gl_400";

    private final int FRAMEBUFFER_FACTOR = 2;

    private int vertexCount = 3;

    private enum Texture {
        R,
        G,
        B,
        MAX
    };

    private int[] framebufferName = {0}, vertexArrayName = {0}, textureName = new int[Texture.MAX.ordinal()];
    private int programName, uniformDiffuse;
    private Vec4[] viewport = new Vec4[Texture.MAX.ordinal()];

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        Vec2i framebufferSize = new Vec2i(windowSize.x / FRAMEBUFFER_FACTOR, windowSize.y / FRAMEBUFFER_FACTOR);

        viewport[Texture.R.ordinal()] = new Vec4(windowSize.x >> 1, 0, framebufferSize.x, framebufferSize.y);
        viewport[Texture.G.ordinal()] = new Vec4(windowSize.x >> 1, windowSize.y >> 1,
                framebufferSize.x, framebufferSize.y);
        viewport[Texture.B.ordinal()] = new Vec4(0, windowSize.y >> 1, framebufferSize.x, framebufferSize.y);

        boolean validated = true;

        if (validated) {
            validated = initProgram(gl4);
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

        return validated && checkError(gl4, "begin");
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

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);

            programName = shaderProgram.program();

            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            uniformDiffuse = gl4.glGetUniformLocation(programName, "diffuse");
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initTexture(GL4 gl4) {

        gl4.glActiveTexture(GL_TEXTURE0);
        gl4.glGenTextures(Texture.MAX.ordinal(), textureName, 0);

        for (int i = Texture.R.ordinal(); i <= Texture.B.ordinal(); ++i) {
            gl4.glBindTexture(GL_TEXTURE_2D, textureName[i]);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_R, GL_RED);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_G, GL_GREEN);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_B, GL_BLUE);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_A, GL_ALPHA);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

            gl4.glTexImage2D(GL_TEXTURE_2D,
                    0,
                    GL_RGBA8,
                    windowSize.x / FRAMEBUFFER_FACTOR,
                    windowSize.y / FRAMEBUFFER_FACTOR,
                    0,
                    GL_RGB, GL_UNSIGNED_BYTE,
                    null);
        }

        return checkError(gl4, "initTexture");
    }

    private boolean initFramebuffer(GL4 gl4) {

        gl4.glGenFramebuffers(1, framebufferName, 0);
        gl4.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[0]);
        gl4.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, textureName[0], 0);
        gl4.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, textureName[1], 0);
        gl4.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT2, textureName[2], 0);

        if (!isFramebufferComplete(gl4, framebufferName[0])) {
            return false;
        }

        gl4.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        return checkError(gl4, "initFramebuffer");
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(1, vertexArrayName, 0);
        gl4.glBindVertexArray(vertexArrayName[0]);
        gl4.glBindVertexArray(0);

        return checkError(gl4, "initVertexArray");
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        Vec2i framebufferSize = new Vec2i(windowSize.x / FRAMEBUFFER_FACTOR, windowSize.y / FRAMEBUFFER_FACTOR);

        gl4.glViewport(0, 0, framebufferSize.x, framebufferSize.y);

        // Pass 1
        gl4.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[0]);
        int[] drawBuffers = {GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1, GL_COLOR_ATTACHMENT2};
        gl4.glDrawBuffers(3, drawBuffers, 0);

        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 0.0f, 0.0f, 1.0f}, 0);
        gl4.glClearBufferfv(GL_COLOR, 1, new float[]{0.0f, 1.0f, 0.0f, 1.0f}, 0);
        gl4.glClearBufferfv(GL_COLOR, 2, new float[]{0.0f, 0.0f, 1.0f, 1.0f}, 0);

        // Pass 2
        gl4.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 0.5f, 0.0f, 1.0f}, 0);

        gl4.glUseProgram(programName);
        gl4.glUniform1i(uniformDiffuse, 0);

        gl4.glBindVertexArray(vertexArrayName[0]);

        for (int i = 0; i < Texture.MAX.ordinal(); ++i) {
            gl4.glViewport((int) viewport[i].x, (int) viewport[i].y, (int) viewport[i].z, (int) viewport[i].w);

            gl4.glActiveTexture(GL_TEXTURE0);
            gl4.glBindTexture(GL_TEXTURE_2D, textureName[i]);

            gl4.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 1);
        }

        gl4.glUseProgram(0);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteTextures(Texture.MAX.ordinal(), textureName, 0);
        gl4.glDeleteFramebuffers(1, framebufferName, 0);
        gl4.glDeleteProgram(programName);

        return checkError(gl4, "end");
    }
}
