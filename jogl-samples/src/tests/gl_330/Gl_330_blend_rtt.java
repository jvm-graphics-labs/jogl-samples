/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_330;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2GL3.*;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import jgli.Texture2d;
import jglm.Vec4i;

/**
 *
 * @author GBarbieri
 */
public class Gl_330_blend_rtt extends Test {

    public static void main(String[] args) {
        Gl_330_blend_rtt gl_330_blend_rtt = new Gl_330_blend_rtt();
    }

    public Gl_330_blend_rtt() {
        super("gl-330-blend-rtt", Profile.CORE, 3, 3);
    }

    private final String SHADERS_SOURCE = "image-2d";
    private final String SHADERS_ROOT = "src/data/gl_330";
    private final String TEXTURE_DIFFUSE = "kueken7_rgba8_srgb.dds";

    public static class Vertex {

        public float[] position;
        public float[] texCoord;
        public static final int SIZEOF = 2 * 2 * Float.BYTES;

        public Vertex(float[] position, float[] texCoord) {
            this.position = position;
            this.texCoord = texCoord;
        }

        public float[] toFloatArray() {
            return new float[]{position[0], position[1], texCoord[0], texCoord[1]};
        }
    }

    // With DDS textures, v texture coordinate are reversed, from top to bottom
    private int vertexCount = 6;
    private int vertexSize = vertexCount * Vertex.SIZEOF;
    private Vertex[] vertexData = {
        new Vertex(new float[]{-1.0f, -1.0f}, new float[]{0.0f, 1.0f}),
        new Vertex(new float[]{+1.0f, -1.0f}, new float[]{1.0f, 1.0f}),
        new Vertex(new float[]{+1.0f, +1.0f}, new float[]{1.0f, 0.0f}),
        new Vertex(new float[]{+1.0f, +1.0f}, new float[]{1.0f, 0.0f}),
        new Vertex(new float[]{-1.0f, +1.0f}, new float[]{0.0f, 0.0f}),
        new Vertex(new float[]{-1.0f, -1.0f}, new float[]{0.0f, 1.0f})};

    private class Texture {

        public static final int RGB8 = 0;
        public static final int R = 1;
        public static final int G = 2;
        public static final int B = 3;
        public static final int MAX = 4;
    };

    private class Shader {

        public static final int VERT = 0;
        public static final int FRAG = 1;
        public static final int MAX = 2;
    }

    private int[] framebufferName = {0}, vertexArrayName = {0}, bufferName = {0},
            texture2dName = new int[Texture.MAX], samplerName = {0};
    private int programNameSingle, uniformMvpSingle, uniformDiffuseSingle;
    private Vec4i[] viewport = new Vec4i[Texture.MAX];
    private float[] projection = new float[16], view = new float[16], model = new float[16], mvp = new float[16];

    @Override
    protected boolean begin(GL gl) {

        GL3 gl3 = (GL3) gl;

        viewport[Texture.RGB8] = new Vec4i(0, 0, windowSize.x >> 1, windowSize.y >> 1);
        viewport[Texture.R] = new Vec4i(windowSize.x >> 1, 0, windowSize.x >> 1, windowSize.y >> 1);
        viewport[Texture.G] = new Vec4i(windowSize.x >> 1, windowSize.y >> 1,
                windowSize.x >> 1, windowSize.y >> 1);
        viewport[Texture.B] = new Vec4i(0, windowSize.y >> 1, windowSize.x >> 1, windowSize.y >> 1);

        boolean validated = true;

        if (validated) {
            validated = initBlend(gl3);
        }
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
            validated = initSampler(gl3);
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

        ShaderCode[] shaderCodes = new ShaderCode[Shader.MAX];

        shaderCodes[Shader.VERT] = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
        shaderCodes[Shader.FRAG] = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            shaderProgram.add(shaderCodes[Shader.VERT]);
            shaderProgram.add(shaderCodes[Shader.FRAG]);

            shaderProgram.init(gl3);

            programNameSingle = shaderProgram.program();

            shaderProgram.link(gl3, System.out);
        }

        if (validated) {

            uniformMvpSingle = gl3.glGetUniformLocation(programNameSingle, "mvp");
            uniformDiffuseSingle = gl3.glGetUniformLocation(programNameSingle, "diffuse");
        }

        return validated & checkError(gl3, "initProgram");
    }

    private boolean initBuffer(GL3 gl3) {

        gl3.glGenBuffers(1, bufferName, 0);

        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[0]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexCount * 4);
        for (Vertex vertex : vertexData) {
            vertexBuffer.put(vertex.toFloatArray());
        }
        vertexBuffer.rewind();
        gl3.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        return checkError(gl3, "initBuffer");
    }

    private boolean initSampler(GL3 gl3) {

        gl3.glGenSamplers(1, samplerName, 0);
        gl3.glSamplerParameteri(samplerName[0], GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        gl3.glSamplerParameteri(samplerName[0], GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        gl3.glSamplerParameteri(samplerName[0], GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        gl3.glSamplerParameteri(samplerName[0], GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        gl3.glSamplerParameteri(samplerName[0], GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        gl3.glSamplerParameterfv(samplerName[0], GL_TEXTURE_BORDER_COLOR, new float[]{0.0f, 0.0f, 0.0f, 0.0f}, 0);
        gl3.glSamplerParameterf(samplerName[0], GL_TEXTURE_MIN_LOD, -1000.f);
        gl3.glSamplerParameterf(samplerName[0], GL_TEXTURE_MAX_LOD, 1000.f);
        gl3.glSamplerParameterf(samplerName[0], GL_TEXTURE_LOD_BIAS, 0.0f);
        gl3.glSamplerParameteri(samplerName[0], GL_TEXTURE_COMPARE_MODE, GL_NONE);
        gl3.glSamplerParameteri(samplerName[0], GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);

        return checkError(gl3, "initSampler");
    }

    private boolean initTexture(GL3 gl3) {

        try {
            jgli.Texture2d texture = new Texture2d(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE));
            jgli.Gl.Format format = jgli.Gl.translate(texture.format());

            gl3.glActiveTexture(GL_TEXTURE0);
            gl3.glGenTextures(Texture.MAX, texture2dName, 0);

            gl3.glBindTexture(GL_TEXTURE_2D, texture2dName[Texture.RGB8]);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            gl3.glTexImage2D(GL_TEXTURE_2D, 0,
                    format.internal.value,
                    texture.dimensions()[0], texture.dimensions()[1],
                    0,
                    format.external.value, format.type.value,
                    texture.data());

            gl3.glBindTexture(GL_TEXTURE_2D, texture2dName[Texture.R]);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_R, GL_RED);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_G, GL_ZERO);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_B, GL_ZERO);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_A, GL_ZERO);

            gl3.glBindTexture(GL_TEXTURE_2D, texture2dName[Texture.G]);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_R, GL_ZERO);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_G, GL_RED);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_B, GL_ZERO);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_A, GL_ZERO);

            gl3.glBindTexture(GL_TEXTURE_2D, texture2dName[Texture.B]);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_R, GL_ZERO);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_G, GL_ZERO);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_B, GL_RED);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_A, GL_ZERO);

            for (int i = Texture.R; i <= Texture.B; ++i) {
                gl3.glBindTexture(GL_TEXTURE_2D, texture2dName[i]);
                gl3.glTexImage2D(GL_TEXTURE_2D, 0,
                        GL_R8,
                        texture.dimensions()[0], texture.dimensions()[1],
                        0,
                        GL_RGB, GL_UNSIGNED_BYTE,
                        null);
            }

        } catch (IOException ex) {
            Logger.getLogger(Gl_330_blend_rtt.class.getName()).log(Level.SEVERE, null, ex);
        }
        return checkError(gl3, "initTexture");
    }

    private boolean initFramebuffer(GL3 gl3) {

        gl3.glGenFramebuffers(1, framebufferName, 0);
        gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[0]);

        for (int i = Texture.R; i <= Texture.B; ++i) {
            gl3.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + (i - Texture.R), texture2dName[i], 0);
        }

        int[] drawBuffers = new int[3];
        drawBuffers[0] = GL_COLOR_ATTACHMENT0;
        drawBuffers[1] = GL_COLOR_ATTACHMENT1;
        drawBuffers[2] = GL_COLOR_ATTACHMENT2;

        gl3.glDrawBuffers(3, drawBuffers, 0);

        if (!isFramebufferComplete(gl3, framebufferName[0])) {
            return false;
        }

        return checkError(gl3, "initFramebuffer");
    }

    private boolean initVertexArray(GL3 gl3) {

        gl3.glGenVertexArrays(1, vertexArrayName, 0);
        gl3.glBindVertexArray(vertexArrayName[0]);
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[0]);
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, Vertex.SIZEOF, 0);
            gl3.glVertexAttribPointer(Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, Vertex.SIZEOF, 2 * Float.BYTES);
            gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl3.glEnableVertexAttribArray(Semantic.Attr.TEXCOORD);
        }
        gl3.glBindVertexArray(0);

        return checkError(gl3, "initVertexArray");
    }

    private boolean initBlend(GL3 gl3) {

        gl3.glBlendEquationSeparate(GL_FUNC_ADD, GL_FUNC_ADD);
        gl3.glBlendFuncSeparate(GL_ONE, GL_ONE, GL_ONE, GL_ONE);

        gl3.glEnablei(GL_BLEND, 0);
        gl3.glEnablei(GL_BLEND, 1);
        gl3.glEnablei(GL_BLEND, 2);
        gl3.glEnablei(GL_BLEND, 3);

        gl3.glColorMaski(0, true, true, true, false);
        gl3.glColorMaski(1, true, false, false, false);
        gl3.glColorMaski(2, true, false, false, false);
        gl3.glColorMaski(3, true, false, false, false);

        return checkError(gl3, "initBlend");
    }

    @Override
    protected boolean render(GL gl) {

        GL3 gl3 = (GL3) gl;

        // Pass 1
        {
            gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[0]);
            gl3.glViewport(0, 0, windowSize.x >> 1, windowSize.y >> 1);
            gl3.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, 0);
            gl3.glClearBufferfv(GL_COLOR, 1, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, 0);
            gl3.glClearBufferfv(GL_COLOR, 2, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, 0);
            gl3.glClearBufferfv(GL_COLOR, 3, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, 0);
        }

        // Pass 2
        gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        gl3.glClearBufferfv(GL_COLOR, 0, new float[]{0.0f, 0.0f, 0.0f, 0.0f}, 0);

        gl3.glUseProgram(programNameSingle);

        {
            FloatUtil.makeOrtho(projection, 0, true, -2.0f, 2.0f, -1.5f, 1.5f, -1.0f, 1.0f);
            FloatUtil.makeIdentity(view);
            FloatUtil.makeIdentity(model);
            FloatUtil.multMatrix(projection, view, mvp);
            FloatUtil.multMatrix(mvp, model);

            gl3.glUniformMatrix4fv(uniformMvpSingle, 1, false, mvp, 0);
            gl3.glUniform1i(uniformDiffuseSingle, 0);
        }

        for (int i = 0; i < Texture.MAX; ++i) {
            gl3.glViewport(viewport[i].x, viewport[i].y, viewport[i].z, viewport[i].w);

            gl3.glActiveTexture(GL_TEXTURE0);
            gl3.glBindTexture(GL_TEXTURE_2D, texture2dName[i]);
            gl3.glBindSampler(0, samplerName[0]);
            gl3.glBindVertexArray(vertexArrayName[0]);

            gl3.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 1);
        }

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;

        gl3.glDeleteBuffers(1, bufferName, 0);
        gl3.glDeleteProgram(programNameSingle);
        gl3.glDeleteTextures(Texture.MAX, texture2dName, 0);
        gl3.glDeleteFramebuffers(1, framebufferName, 0);
        gl3.glDeleteSamplers(1, samplerName, 0);

        return checkError(gl3, "end");
    }
}
