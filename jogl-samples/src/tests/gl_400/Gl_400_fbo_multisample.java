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
import glm.glm;
import glm.mat._4.Mat4;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.io.IOException;
import java.nio.ShortBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import jgli.Texture2d;
import glm.vec._2.Vec2;
import glm.vec._2.i.Vec2i;
import framework.BufferUtils;
import glf.Vertex_v2fv2f;
import glm.vec._3.Vec3;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

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
    private int vertexSize = vertexCount * Vertex_v2fv2f.SIZE;
    private Vertex_v2fv2f[] vertexData = {
        new Vertex_v2fv2f(new Vec2(-2.0f, -1.5f).mul(0.8f), new Vec2(0.0f, 0.0f)),
        new Vertex_v2fv2f(new Vec2(+2.0f, -1.5f).mul(0.8f), new Vec2(1.0f, 0.0f)),
        new Vertex_v2fv2f(new Vec2(+2.0f, +1.5f).mul(0.8f), new Vec2(1.0f, 1.0f)),
        new Vertex_v2fv2f(new Vec2(-2.0f, +1.5f).mul(0.8f), new Vec2(0.0f, 1.0f))};

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

    private class Texture {

        public static final int DIFFUSE = 0;
        public static final int MULTISAMPLE = 1;
        public static final int COLOR = 2;
        public static final int MAX = 3;
    };

    private class Framebuffer {

        public static final int RENDER = 0;
        public static final int RESOLVE = 1;
        public static final int MAX = 2;
    };

    private IntBuffer vertexArrayName = GLBuffers.newDirectIntBuffer(1),
            bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX),
            textureName = GLBuffers.newDirectIntBuffer(Texture.MAX),
            samplerName = GLBuffers.newDirectIntBuffer(1),
            framebufferName = GLBuffers.newDirectIntBuffer(Framebuffer.MAX);
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

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER, this.getClass(), SHADERS_ROOT, null,
                    SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER, this.getClass(), SHADERS_ROOT, null,
                    SHADERS_SOURCE, "frag", null, true);

            shaderProgram.init(gl4);

            programName = shaderProgram.program();

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);

            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            uniformMvp = gl4.glGetUniformLocation(programName, "mvp");
            uniformDiffuse = gl4.glGetUniformLocation(programName, "diffuse");
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        ByteBuffer vertexBuffer = GLBuffers.newDirectByteBuffer(vertexSize);

        gl4.glGenBuffers(Buffer.MAX, bufferName);

        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT));
        gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
        for (int i = 0; i < vertexCount; i++) {
            vertexData[i].toBb(vertexBuffer, i);
        }
        vertexBuffer.rewind();
        gl4.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        BufferUtils.destroyDirectBuffer(elementBuffer);
        BufferUtils.destroyDirectBuffer(vertexBuffer);

        return true;
    }

    private boolean initSampler(GL4 gl4) {

        FloatBuffer borderColor = GLBuffers.newDirectFloatBuffer(new float[]{0.0f, 0.0f, 0.0f, 0.0f});

        gl4.glGenSamplers(1, samplerName);

        gl4.glSamplerParameteri(samplerName.get(0), GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        gl4.glSamplerParameteri(samplerName.get(0), GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        gl4.glSamplerParameteri(samplerName.get(0), GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        gl4.glSamplerParameteri(samplerName.get(0), GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        gl4.glSamplerParameteri(samplerName.get(0), GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        gl4.glSamplerParameterfv(samplerName.get(0), GL_TEXTURE_BORDER_COLOR, borderColor);
        gl4.glSamplerParameterf(samplerName.get(0), GL_TEXTURE_MIN_LOD, -1000.f);
        gl4.glSamplerParameterf(samplerName.get(0), GL_TEXTURE_MAX_LOD, 1000.f);
        gl4.glSamplerParameterf(samplerName.get(0), GL_TEXTURE_LOD_BIAS, 0.0f);
        gl4.glSamplerParameteri(samplerName.get(0), GL_TEXTURE_COMPARE_MODE, GL_NONE);
        gl4.glSamplerParameteri(samplerName.get(0), GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);

        gl4.glBindSampler(0, samplerName.get(0));

        BufferUtils.destroyDirectBuffer(borderColor);

        return true;
    }

    private boolean initTexture(GL4 gl4) {

        try {
            gl4.glGenTextures(Texture.MAX, textureName);
            gl4.glActiveTexture(GL_TEXTURE0);
            gl4.glBindTexture(GL_TEXTURE_2D, textureName.get(Texture.DIFFUSE));
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

        gl4.glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, textureName.get(Texture.MULTISAMPLE));
        // The second parameter is the number of samples.
        gl4.glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, 4, GL_RGBA, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y, false);

        gl4.glGenFramebuffers(Framebuffer.MAX, framebufferName);
        gl4.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName.get(Framebuffer.RENDER));
        gl4.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, textureName.get(Texture.MULTISAMPLE), 0);

        if (!isFramebufferComplete(gl4, framebufferName.get(0))) {
            return false;
        }
        gl4.glBindFramebuffer(GL_FRAMEBUFFER, 0);

        gl4.glBindTexture(GL_TEXTURE_2D, textureName.get(Texture.COLOR));
        gl4.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y, 0, GL_RGBA,
                GL_UNSIGNED_BYTE, null);

        gl4.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName.get(Framebuffer.RESOLVE));
        gl4.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, textureName.get(Texture.COLOR), 0);

        if (!isFramebufferComplete(gl4, framebufferName.get(Framebuffer.RESOLVE))) {
            return false;
        }
        gl4.glBindFramebuffer(GL_FRAMEBUFFER, 0);

        return true;
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(1, vertexArrayName);
        gl4.glBindVertexArray(vertexArrayName.get(0));
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, Vertex_v2fv2f.SIZE, 0);
            gl4.glVertexAttribPointer(Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, Vertex_v2fv2f.SIZE, Vec2.SIZE);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl4.glEnableVertexAttribArray(Semantic.Attr.TEXCOORD);

            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT));
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
        gl4.glMinSampleShading(4 / 2.0f);

        float[] min = {0};
        gl4.glGetFloatv(GL_MIN_SAMPLE_SHADING_VALUE, min, 0);
        //glEnable(GL_SAMPLE_MASK);
        //glSampleMaski(0, 0xFF);
        renderFBO(gl4, framebufferName.get(Framebuffer.RENDER));
        gl4.glDisable(GL_MULTISAMPLE);

        // Resolved multisampling
        gl4.glBindFramebuffer(GL_READ_FRAMEBUFFER, framebufferName.get(Framebuffer.RENDER));
        gl4.glBindFramebuffer(GL_DRAW_FRAMEBUFFER, framebufferName.get(Framebuffer.RESOLVE));
        gl4.glBlitFramebuffer(
                0, 0, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y,
                0, 0, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y,
                GL_COLOR_BUFFER_BIT, GL_NEAREST);
        gl4.glBindFramebuffer(GL_FRAMEBUFFER, 0);

        // Pass 2, render the colorbuffer from the multisampled framebuffer
        gl4.glViewport(0, 0, windowSize.x, windowSize.y);
        renderFB(gl4, textureName.get(Texture.COLOR));

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
        gl4.glClearBufferfv(GL_COLOR, 0, clearColor.put(0, 0).put(1, .5f).put(2, 1).put(3, 1));

        gl4.glActiveTexture(GL_TEXTURE0);
        gl4.glBindTexture(GL_TEXTURE_2D, textureName.get(Texture.DIFFUSE));

        gl4.glBindVertexArray(vertexArrayName.get(0));
        gl4.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0);
    }

    private void renderFB(GL4 gl4, int texture2dName) {

        Mat4 perspective = glm.perspective_((float) Math.PI * 0.25f, (float) windowSize.x / windowSize.y, 0.1f, 100.0f);
        Mat4 model = new Mat4(1.0f);
        Mat4 mvp = perspective.mul(viewMat4()).mul(model);

        gl4.glUniformMatrix4fv(uniformMvp, 1, false, mvp.toFa_(), 0);

        gl4.glActiveTexture(GL_TEXTURE0);
        gl4.glBindTexture(GL_TEXTURE_2D, texture2dName);

        gl4.glBindVertexArray(vertexArrayName.get(0));
        gl4.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0);
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteBuffers(Buffer.MAX, bufferName);
        gl4.glDeleteProgram(programName);
        gl4.glDeleteTextures(Texture.MAX, textureName);
        gl4.glDeleteFramebuffers(Framebuffer.MAX, framebufferName);
        gl4.glDeleteVertexArrays(1, vertexArrayName);
        gl4.glDeleteSamplers(1, samplerName);

        BufferUtils.destroyDirectBuffer(bufferName);
        BufferUtils.destroyDirectBuffer(textureName);
        BufferUtils.destroyDirectBuffer(framebufferName);
        BufferUtils.destroyDirectBuffer(vertexArrayName);
        BufferUtils.destroyDirectBuffer(samplerName);

        return true;
    }
}
