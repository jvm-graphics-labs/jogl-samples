/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl320;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_BACK;
import static com.jogamp.opengl.GL.GL_COLOR_ATTACHMENT0;
import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DRAW_FRAMEBUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_FRAMEBUFFER;
import static com.jogamp.opengl.GL.GL_MULTISAMPLE;
import static com.jogamp.opengl.GL.GL_NEAREST;
import static com.jogamp.opengl.GL.GL_READ_FRAMEBUFFER;
import static com.jogamp.opengl.GL.GL_RGBA;
import static com.jogamp.opengl.GL.GL_RGBA8;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNPACK_ALIGNMENT;
import static com.jogamp.opengl.GL.GL_UNSIGNED_BYTE;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_TEXTURE_2D_MULTISAMPLE;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
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
import jglm.Vec2;
import jglm.Vec2i;

/**
 *
 * @author GBarbieri
 */
public class Gl_320_fbo_multisample extends Test {

    public static void main(String[] args) {
        Gl_320_fbo_multisample gl_320_fbo_multisample = new Gl_320_fbo_multisample();
    }

    public Gl_320_fbo_multisample() {
        super("Gl-320-fbo-multisample", 3, 2, new Vec2((float) Math.PI * 0.2f, (float) Math.PI * 0.2f));
    }

    private final String SHADERS_SOURCE = "fbo-multisample";
    private final String SHADERS_ROOT = "src/data/gl_320";
    private final String TEXTURE_DIFFUSE = "kueken7_rgba8_srgb.dds";

    private final Vec2i FRAMEBUFFER_SIZE = new Vec2i(160, 120);

    // With DDS textures, v texture coordinate are reversed, from top to bottom
    int vertexCount = 6;
    int vertexSize = vertexCount * 2 * 2 * Float.BYTES;
    float[] vertexData = {
        -1.0f, -1.0f, 0.0f, 1.0f,
        +1.0f, -1.0f, 1.0f, 1.0f,
        +1.0f, +1.0f, 1.0f, 0.0f,
        +1.0f, +1.0f, 1.0f, 0.0f,
        -1.0f, +1.0f, 0.0f, 0.0f,
        -1.0f, -1.0f, 0.0f, 1.0f};

    private enum Texture {
        DIFFUSE,
        COLORBUFFER,
        MULTISAMPLE,
        MAX
    }

    private enum Framebuffer {
        RENDER,
        RESOLVE,
        MAX
    }

    private enum Shader {
        VERT,
        FRAG,
        MAX
    }

    private int[] textureName = new int[Texture.MAX.ordinal()], framebufferName = new int[Framebuffer.MAX.ordinal()],
            vertexArrayName = new int[1], bufferName = new int[1], programName = new int[1];
    private int uniformMvp, uniformDiffuse;
    private float[] perspective = new float[16], model = new float[16], mvp = new float[16],
            projection = new float[16], view = new float[16];

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

        if (validated) {

            ShaderCode vertexShaderCode = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragmentShaderCode = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

            ShaderProgram program = new ShaderProgram();
            program.add(vertexShaderCode);
            program.add(fragmentShaderCode);

            program.init(gl3);

            programName[0] = program.program();

            gl3.glBindAttribLocation(programName[0], Semantic.Attr.POSITION, "position");
            gl3.glBindAttribLocation(programName[0], Semantic.Attr.TEXCOORD, "texCoord");
            gl3.glBindFragDataLocation(programName[0], Semantic.Frag.COLOR, "color");

            program.link(gl3, System.out);
        }

        if (validated) {

            uniformMvp = gl3.glGetUniformLocation(programName[0], "mvp");
            uniformDiffuse = gl3.glGetUniformLocation(programName[0], "diffuse");
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
            gl3.glGenTextures(Texture.MAX.ordinal(), textureName, 0);

            jgli.Texture texture = jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE);

            jgli.Gl.Format format = jgli.Gl.instance.translate(texture.format());

            gl3.glActiveTexture(GL_TEXTURE0);
            gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.DIFFUSE.ordinal()]);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, texture.levels() - 1);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

            gl3.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

            for (int level = 0; level < texture.levels(); ++level) {
                gl3.glTexImage2D(GL_TEXTURE_2D, level,
                        format.internal.value,
                        texture.dimensions(level)[0], texture.dimensions(level)[1],
                        0,
                        format.external.value, format.type.value,
                        texture.data(0, 0, level));
            }

            gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.COLORBUFFER.ordinal()]);
            gl3.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y, 0,
                    GL_RGBA, GL_UNSIGNED_BYTE, null);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            gl3.glBindTexture(GL_TEXTURE_2D, 0);

            gl3.glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, textureName[Texture.MULTISAMPLE.ordinal()]);
            gl3.glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, 8, format.internal.value,
                    FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y, true); // The second parameter is the number of samples.
            gl3.glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, 0);

        } catch (IOException ex) {
            Logger.getLogger(Gl_320_fbo_multisample.class.getName()).log(Level.SEVERE, null, ex);
        }
        return checkError(gl3, "initTexture");
    }

    private boolean initFramebuffer(GL3 gl3) {

        gl3.glGenFramebuffers(Framebuffer.MAX.ordinal(), framebufferName, 0);

        int[] buffersRender = new int[]{GL_COLOR_ATTACHMENT0};
        gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[Framebuffer.RENDER.ordinal()]);
        gl3.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, textureName[Texture.MULTISAMPLE.ordinal()], 0);
        gl3.glDrawBuffers(1, buffersRender, 0);
        if (!isFramebufferComplete(gl3, framebufferName[Framebuffer.RENDER.ordinal()])) {
            return false;
        }
        int[] buffersResolve = new int[]{GL_COLOR_ATTACHMENT0};
        gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[Framebuffer.RESOLVE.ordinal()]);
        gl3.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, textureName[Texture.COLORBUFFER.ordinal()], 0);
        gl3.glDrawBuffers(1, buffersResolve, 0);
        if (!isFramebufferComplete(gl3, framebufferName[Framebuffer.RESOLVE.ordinal()])) {
            return false;
        }

        gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        gl3.glDrawBuffer(GL_BACK);
        if (!isFramebufferComplete(gl3, 0)) {
            return false;
        }

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

        gl3.glUseProgram(programName[0]);
        gl3.glUniform1i(uniformDiffuse, 0);

        // Pass 1
        // Render the scene in a multisampled framebuffer
        gl3.glEnable(GL_MULTISAMPLE);
        renderFBO(gl3, framebufferName[Framebuffer.RENDER.ordinal()]);
        gl3.glDisable(GL_MULTISAMPLE);

        // Resolved multisampling
        gl3.glBindFramebuffer(GL_READ_FRAMEBUFFER, framebufferName[Framebuffer.RENDER.ordinal()]);
        gl3.glBindFramebuffer(GL_DRAW_FRAMEBUFFER, framebufferName[Framebuffer.RESOLVE.ordinal()]);
        gl3.glBlitFramebuffer(
                0, 0, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y,
                0, 0, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y,
                GL_COLOR_BUFFER_BIT, GL_NEAREST);
        gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);

        // Pass 2
        // Render the colorbuffer from the multisampled framebuffer
        renderFB(gl3, textureName[Texture.COLORBUFFER.ordinal()]);

        return true;
    }

    private void renderFBO(GL3 gl3, int framebuffer) {
        FloatUtil.makePerspective(perspective, 0, true, (float) Math.PI * 0.25f,
                (float) FRAMEBUFFER_SIZE.x / FRAMEBUFFER_SIZE.y, 0.1f, 100.0f);
        FloatUtil.makeScale(model, true, 1.0f, 1.0f, 1.0f);
        FloatUtil.multMatrix(perspective, view(), mvp);
        FloatUtil.multMatrix(mvp, model);

        gl3.glUniformMatrix4fv(uniformMvp, 1, false, mvp, 0);

        gl3.glViewport(0, 0, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y);
        gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);
        gl3.glClearBufferfv(GL_COLOR, 0, new float[]{0.0f, 0.5f, 1.0f, 1.0f}, 0);

        gl3.glActiveTexture(GL_TEXTURE0);
        gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.DIFFUSE.ordinal()]);

        gl3.glBindVertexArray(vertexArrayName[0]);

        gl3.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 1);
    }

    private void renderFB(GL3 gl3, int textureName) {

        FloatUtil.makeOrtho(projection, 0, true, -1.1f, 1.1f, 1.1f, -1.1f, 0.0f, 10.0f);
        FloatUtil.makeTranslation(view, true, 0.0f, 0.0f, -cameraDistance() * 0.1f);
        FloatUtil.makeIdentity(model);
        FloatUtil.multMatrix(projection, view, mvp);
        FloatUtil.multMatrix(mvp, model);

        gl3.glUniformMatrix4fv(uniformMvp, 1, false, mvp, 0);

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
        gl3.glDeleteProgram(programName[0]);
        gl3.glDeleteTextures(Texture.MAX.ordinal(), textureName, 0);
        gl3.glDeleteFramebuffers(Framebuffer.MAX.ordinal(), framebufferName, 0);
        gl3.glDeleteVertexArrays(1, vertexArrayName, 0);

        return true;
    }
}
