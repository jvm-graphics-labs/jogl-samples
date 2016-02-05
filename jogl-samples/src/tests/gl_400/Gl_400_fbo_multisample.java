/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_400;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2GL3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import core.glm;
import dev.Mat4;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import jgli.Texture2d;
import dev.Vec2;
import dev.Vec2i;
import dev.Vec3;

/**
 *
 * @author GBarbieri
 */
public class Gl_400_fbo_multisample extends Test {

    public static void main(String[] args) {
        Gl_400_fbo_multisample gl_400_fbo_multisample = new Gl_400_fbo_multisample();
    }

    public Gl_400_fbo_multisample() {
        super("gl-400-fbo-multisample", Profile.CORE, 4, 0, new Vec2(Math.PI * 0.1f));
    }

    private final String SHADERS_SOURCE = "multisample";
    private final String SHADERS_ROOT = "src/data/gl_400";
    private final String TEXTURE_DIFFUSE = "kueken7_rgba8_srgb.dds";

    private final Vec2i FRAMEBUFFER_SIZE = new Vec2i(80, 60);

    private int vertexCount = 4;
    private int vertexSize = vertexCount * 2 * 2 * Float.BYTES;
    private float[] vertexData = {
        -2.0f * 0.8f, -1.5f * 0.8f, 0.0f, 0.0f,
        +2.0f * 0.8f, -1.5f * 0.8f, 1.0f, 0.0f,
        +2.0f * 0.8f, +1.5f * 0.8f, 1.0f, 1.0f,
        -2.0f * 0.8f, +1.5f * 0.8f, 0.0f, 1.0f};

    private int elementCount = 6;
    private int elementSize = elementCount * Short.BYTES;
    private short[] elementData = {
        0, 1, 2,
        2, 3, 0};

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int ELEMENT = 1;
        public static final int MAX = 2;
    };

    private int[] vertexArrayName = {0}, bufferName = new int[Buffer.MAX], texture2dName = {0},
            multisampleTextureName = {0}, colorTextureName = {0}, samplerName = {0}, framebufferRenderName = {0},
            framebufferResolveName = {0};
    private int programName, uniformMvp, uniformDiffuse;

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

        // Create program
        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

            shaderProgram.init(gl4);
            
            programName = shaderProgram.program();

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);


            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            uniformMvp = gl4.glGetUniformLocation(programName, "MVP");
            uniformDiffuse = gl4.glGetUniformLocation(programName, "Diffuse");
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        gl4.glGenBuffers(Buffer.MAX, bufferName, 0);

        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT]);
        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl4.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

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

        gl4.glBindSampler(0, samplerName[0]);

        return true;
    }

    private boolean initTexture(GL4 gl4) {

        try {
            gl4.glGenTextures(1, texture2dName, 0);
            gl4.glActiveTexture(GL_TEXTURE0);
            gl4.glBindTexture(GL_TEXTURE_2D, texture2dName[0]);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

            jgli.Texture2d texture = new Texture2d(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE));
            jgli.Gl.Format format = jgli.Gl.translate(texture.format());

            for (int level = 0; level < texture.levels(); ++level) {
                gl4.glTexImage2D(GL_TEXTURE_2D, level,
                        format.internal.value,
                        texture.dimensions(level)[0], texture.dimensions(level)[1],
                        0,
                        format.external.value, format.type.value,
                        texture.data(level));
            }

        } catch (IOException ex) {
            Logger.getLogger(Gl_400_fbo_multisample.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    private boolean initFramebuffer(GL4 gl4) {

        gl4.glGenTextures(1, multisampleTextureName, 0);
        gl4.glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, multisampleTextureName[0]);
        // The second parameter is the number of samples.
        gl4.glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, 4, GL_RGBA,
                FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y, false);

        gl4.glGenFramebuffers(1, framebufferRenderName, 0);
        gl4.glBindFramebuffer(GL_FRAMEBUFFER, framebufferRenderName[0]);
        gl4.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, multisampleTextureName[0], 0);

        if (!isFramebufferComplete(gl4, framebufferRenderName[0])) {
            return false;
        }
        gl4.glBindFramebuffer(GL_FRAMEBUFFER, 0);

        gl4.glGenTextures(1, colorTextureName, 0);
        gl4.glBindTexture(GL_TEXTURE_2D, colorTextureName[0]);
        gl4.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y, 0,
                GL_RGBA, GL_UNSIGNED_BYTE, 0);

        gl4.glGenFramebuffers(1, framebufferResolveName, 0);
        gl4.glBindFramebuffer(GL_FRAMEBUFFER, framebufferResolveName[0]);
        gl4.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, colorTextureName[0], 0);

        if (!isFramebufferComplete(gl4, framebufferResolveName[0])) {
            return false;
        }
        gl4.glBindFramebuffer(GL_FRAMEBUFFER, 0);

        return true;
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(1, vertexArrayName, 0);
        gl4.glBindVertexArray(vertexArrayName[0]);
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 2 * 2 * Float.BYTES, 0);
            gl4.glVertexAttribPointer(Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, 2 * 2 * Float.BYTES, 2 * Float.BYTES);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl4.glEnableVertexAttribArray(Semantic.Attr.TEXCOORD);

            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT]);
        }
        gl4.glBindVertexArray(0);

        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 0.5f, 0.0f, 1.0f}, 0);

        gl4.glUseProgram(programName);
        gl4.glUniform1i(uniformDiffuse, 0);

        // Pass 1, render the scene in a multisampled framebuffer
        gl4.glEnable(GL_MULTISAMPLE);
        gl4.glEnable(GL_SAMPLE_SHADING);
        gl4.glMinSampleShading(2.0f);

        //glEnable(GL_SAMPLE_MASK);
        //glSampleMaski(0, 0xFF);
        renderFBO(gl4, framebufferRenderName[0]);
        gl4.glDisable(GL_MULTISAMPLE);

        // Resolved multisampling
        gl4.glBindFramebuffer(GL_READ_FRAMEBUFFER, framebufferRenderName[0]);
        gl4.glBindFramebuffer(GL_DRAW_FRAMEBUFFER, framebufferResolveName[0]);
        gl4.glBlitFramebuffer(
                0, 0, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y,
                0, 0, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y,
                GL_COLOR_BUFFER_BIT, GL_NEAREST);
        gl4.glBindFramebuffer(GL_FRAMEBUFFER, 0);

        // Pass 2, render the colorbuffer from the multisampled framebuffer
        gl4.glViewport(0, 0, windowSize.x, windowSize.y);
        renderFB(gl4, colorTextureName[0]);

        return true;
    }

    private void renderFBO(GL4 gl4, int framebuffer) {

        Mat4 perspective = glm.perspective_((float) Math.PI * 0.25f, (float) FRAMEBUFFER_SIZE.x / FRAMEBUFFER_SIZE.y,
                0.1f, 100.0f);
        Mat4 model = new Mat4(1.0f).scale(new Vec3(1, -1, 1));
        Mat4 mvp = perspective.mul(viewMat4()).mul(model);

        gl4.glUniformMatrix4fv(uniformMvp, 1, false, mvp.toFa_(), 0);

        gl4.glViewport(0, 0, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y);
        gl4.glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{0.0f, 0.5f, 1.0f, 1.0f}, 0);

        gl4.glActiveTexture(GL_TEXTURE0);
        gl4.glBindTexture(GL_TEXTURE_2D, texture2dName[0]);

        gl4.glBindVertexArray(vertexArrayName[0]);
        gl4.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0);
    }

    private void renderFB(GL4 gl4, int texture2dName) {

        Mat4 perspective = glm.perspective_((float) Math.PI * 0.25f, (float) windowSize.x / windowSize.y, 0.1f, 100.0f);
        Mat4 model = new Mat4(1.0f);
        Mat4 mvp = perspective.mul(viewMat4()).mul(model);

        gl4.glUniformMatrix4fv(uniformMvp, 1, false, mvp.toFa_(), 0);

        gl4.glActiveTexture(GL_TEXTURE0);
        gl4.glBindTexture(GL_TEXTURE_2D, texture2dName);

        gl4.glBindVertexArray(vertexArrayName[0]);
        gl4.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0);
    }
}
