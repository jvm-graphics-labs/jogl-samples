/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.es_300;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2GL3.*;
import com.jogamp.opengl.GL3ES3;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import core.glm;
import dev.Mat4;
import dev.Vec2;
import framework.BufferUtils;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import glf.Vertex_v2fv2f;
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
public class Es_300_fbo_srgb extends Test {

    public static void main(String[] args) {
        Es_300_fbo_srgb es_300_fbo_srgb = new Es_300_fbo_srgb();
    }

    public Es_300_fbo_srgb() {
        super("es-300-fbo-srgb", Profile.ES, 3, 0);
    }

    private final String SHADERS_SOURCE_TEXTURE = "fbo-srgb";
    private final String SHADERS_SOURCE_SPASH = "fbo-srgb-blit";
    private final String SHADERS_ROOT = "src/data/es_300";
    private final String TEXTURE_DIFFUSE = "kueken7_rgba8_srgb.dds";

    private int vertexCount = 4;
    private int vertexSize = vertexCount * Vertex_v2fv2f.SIZEOF;
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

    private class Buffer {

        private static final int VERTEX = 0;
        private static final int ELEMENT = 1;
        private static final int TRANSFORM = 2;
        private static final int MAX = 3;
    }

    private class Texture {

        private static final int DIFFUSE = 0;
        private static final int COLORBUFFER = 1;
        private static final int RENDERBUFFER = 2;
        private static final int MAX = 3;
    }

    private class Program {

        private static final int TEXTURE = 0;
        private static final int SPLASH = 1;
        private static final int MAX = 2;
    }

    private class Shader {

        private static final int VERT_TEXTURE = 0;
        private static final int FRAG_TEXTURE = 1;
        private static final int VERT_SPLASH = 2;
        private static final int FRAG_SPLASH = 3;
        private static final int MAX = 4;
    }

    private int[] framebufferName = {0}, programName = new int[Program.MAX], vertexArrayName = new int[Program.MAX],
            bufferName = new int[Buffer.MAX], textureName = new int[Texture.MAX], uniformDiffuse = new int[Program.MAX];
    private int framebufferScale = 2, uniformTransform;

    @Override
    protected boolean begin(GL gl) {

        GL3ES3 gl3es3 = (GL3ES3) gl;

        boolean validated = true;

        if (validated) {
            validated = initProgram(gl3es3);
        }
        if (validated) {
            validated = initBuffer(gl3es3);
        }
        if (validated) {
            validated = initVertexArray(gl3es3);
        }
        if (validated) {
            validated = initTexture(gl3es3);
        }
        if (validated) {
            validated = initFramebuffer(gl3es3);
        }

        return validated;
    }

    private boolean initProgram(GL3ES3 gl3es3) {

        boolean validated = true;

        ShaderCode[] shaderCodes = new ShaderCode[Shader.MAX];

        if (validated) {

            shaderCodes[Shader.VERT_TEXTURE] = ShaderCode.create(gl3es3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_TEXTURE, "vert", null, true);
            shaderCodes[Shader.FRAG_TEXTURE] = ShaderCode.create(gl3es3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_TEXTURE, "frag", null, true);

            ShaderProgram shaderProgram = new ShaderProgram();
            shaderProgram.add(shaderCodes[Shader.VERT_TEXTURE]);
            shaderProgram.add(shaderCodes[Shader.FRAG_TEXTURE]);

            shaderProgram.init(gl3es3);

            programName[Program.TEXTURE] = shaderProgram.program();

            shaderProgram.link(gl3es3, System.out);
        }

        if (validated) {

            shaderCodes[Shader.VERT_SPLASH] = ShaderCode.create(gl3es3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_SPASH, "vert", null, true);
            shaderCodes[Shader.FRAG_SPLASH] = ShaderCode.create(gl3es3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_SPASH, "frag", null, true);

            ShaderProgram shaderProgram = new ShaderProgram();
            shaderProgram.add(shaderCodes[Shader.VERT_SPLASH]);
            shaderProgram.add(shaderCodes[Shader.FRAG_SPLASH]);

            shaderProgram.init(gl3es3);

            programName[Program.SPLASH] = shaderProgram.program();

            shaderProgram.link(gl3es3, System.out);
        }
        if (validated) {

            uniformTransform = gl3es3.glGetUniformBlockIndex(programName[Program.TEXTURE], "transform");
            uniformDiffuse[Program.TEXTURE]
                    = gl3es3.glGetUniformLocation(programName[Program.TEXTURE], "Diffuse");
            uniformDiffuse[Program.SPLASH]
                    = gl3es3.glGetUniformLocation(programName[Program.SPLASH], "Diffuse");

            gl3es3.glUseProgram(programName[Program.TEXTURE]);
            gl3es3.glUniform1i(uniformDiffuse[Program.TEXTURE], 0);
            gl3es3.glUniformBlockBinding(programName[Program.TEXTURE], uniformTransform, Semantic.Uniform.TRANSFORM0);

            gl3es3.glUseProgram(programName[Program.SPLASH]);
            gl3es3.glUniform1i(uniformDiffuse[Program.SPLASH], 0);
        }

        return validated & checkError(gl3es3, "initProgram");
    }

    private boolean initBuffer(GL3ES3 gl3es3) {

        gl3es3.glGenBuffers(Buffer.MAX, bufferName, 0);

        gl3es3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT]);
        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        gl3es3.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(elementBuffer);
        gl3es3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl3es3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl3es3.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(vertexBuffer);
        gl3es3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        int[] uniformBufferOffset = {0};
        gl3es3.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset, 0);
        int uniformBlockSize = Math.max(Mat4.SIZEOF, uniformBufferOffset[0]);

        gl3es3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
        gl3es3.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_DYNAMIC_DRAW);
        gl3es3.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return true;
    }

    private boolean initTexture(GL3ES3 gl3es3) {

        boolean validated = true;

        try {

            jgli.Texture2d texture = new Texture2d(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE));
            assert (!texture.empty());

            gl3es3.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

            gl3es3.glGenTextures(Texture.MAX, textureName, 0);

            gl3es3.glActiveTexture(GL_TEXTURE0);
            gl3es3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.DIFFUSE]);
            gl3es3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl3es3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, texture.levels() - 1);
            gl3es3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            gl3es3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            gl3es3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            gl3es3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            int[] swizzle = {GL_RED, GL_GREEN, GL_BLUE, GL_ALPHA};
            gl3es3.glTexParameteriv(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_RGBA, swizzle, 0);

            jgli.Gl.Format format = jgli.Gl.translate(texture.format());
            for (int level = 0; level < texture.levels(); ++level) {

                gl3es3.glTexImage2D(GL_TEXTURE_2D, level,
                        format.internal.value,
                        texture.dimensions(level)[0], texture.dimensions(level)[1],
                        0,
                        format.external.value, format.type.value,
                        texture.data(level));
            }

            gl3es3.glActiveTexture(GL_TEXTURE0);
            gl3es3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.COLORBUFFER]);
            gl3es3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl3es3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
            gl3es3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            gl3es3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            gl3es3.glTexStorage2D(GL_TEXTURE_2D, 1,
                    //                    GL_SRGB8_ALPHA8,
                    GL_RGBA8,
                    windowSize.x * framebufferScale, windowSize.y * framebufferScale);

            gl3es3.glActiveTexture(GL_TEXTURE0);
            gl3es3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.RENDERBUFFER]);
            gl3es3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl3es3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
            gl3es3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            gl3es3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            gl3es3.glTexStorage2D(GL_TEXTURE_2D, 1, GL_DEPTH_COMPONENT24,
                    windowSize.x * framebufferScale, windowSize.y * framebufferScale);

            gl3es3.glPixelStorei(GL_UNPACK_ALIGNMENT, 4);

        } catch (IOException ex) {
            Logger.getLogger(Es_300_fbo_srgb.class.getName()).log(Level.SEVERE, null, ex);
        }
        return validated;
    }

    private boolean initVertexArray(GL3ES3 gl3es3) {

        gl3es3.glGenVertexArrays(Program.MAX, vertexArrayName, 0);
        gl3es3.glBindVertexArray(vertexArrayName[Program.TEXTURE]);
        {
            gl3es3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
            gl3es3.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, Vertex_v2fv2f.SIZEOF, 0);
            gl3es3.glVertexAttribPointer(Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, Vertex_v2fv2f.SIZEOF, Vec2.SIZEOF);
            gl3es3.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl3es3.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl3es3.glEnableVertexAttribArray(Semantic.Attr.TEXCOORD);

            gl3es3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT]);
        }
        gl3es3.glBindVertexArray(0);

        gl3es3.glBindVertexArray(vertexArrayName[Program.SPLASH]);
        gl3es3.glBindVertexArray(0);

        return true;
    }

    private boolean initFramebuffer(GL3ES3 gl3es3) {

        gl3es3.glGenFramebuffers(1, framebufferName, 0);
        gl3es3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[0]);
        gl3es3.glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D,
                textureName[Texture.COLORBUFFER], 0);
        gl3es3.glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D,
                textureName[Texture.RENDERBUFFER], 0);

        if (!isFramebufferComplete(gl3es3, framebufferName[0])) {
            return false;
        }

        gl3es3.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        int srgb = GL_SRGB;
        int linear = GL_LINEAR;
        int[] encoding = {0};
        gl3es3.glGetFramebufferAttachmentParameteriv(GL_DRAW_FRAMEBUFFER, GL_BACK_LEFT,
                GL_FRAMEBUFFER_ATTACHMENT_COLOR_ENCODING, encoding, 0);
        //if (Encoding != GL_SRGB)

        gl3es3.glEnable(GL_FRAMEBUFFER_SRGB);

        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL3ES3 gl3es3 = (GL3ES3) gl;

        {
            gl3es3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
            ByteBuffer pointer = gl3es3.glMapBufferRange(GL_UNIFORM_BUFFER,
                    0, Mat4.SIZEOF, GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            //glm::mat4 Projection = glm::perspectiveFov(glm::pi<float>() * 0.25f, 640.f, 480.f, 0.1f, 100.0f);
            Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, (float) windowSize.x / windowSize.y, 0.1f, 100.0f);

            pointer.asFloatBuffer().put(projection.mul(viewMat4()).toFA_());

            // Make sure the uniform buffer is uploaded
            gl3es3.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        // Render a textured quad to a sRGB framebuffer object.
        {
            gl3es3.glViewport(0, 0, windowSize.x * framebufferScale, windowSize.y * framebufferScale);
            gl3es3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[0]);

            float[] depth = {1.0f};
            gl3es3.glClearBufferfv(GL_DEPTH, 0, depth, 0);
            gl3es3.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 0.5f, 0.0f, 1.0f}, 0);

            gl3es3.glUseProgram(programName[Program.TEXTURE]);

            gl3es3.glActiveTexture(GL_TEXTURE0);
            gl3es3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.DIFFUSE]);
            gl3es3.glBindVertexArray(vertexArrayName[Program.TEXTURE]);
            gl3es3.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.TRANSFORM]);

            gl3es3.glDrawElementsInstanced(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1);
        }

        // Blit the sRGB framebuffer to the default framebuffer back buffer.
        {
            gl3es3.glDisable(GL_DEPTH_TEST);

            gl3es3.glViewport(0, 0, windowSize.x, windowSize.y);
            gl3es3.glBindFramebuffer(GL_FRAMEBUFFER, 0);

            gl3es3.glUseProgram(programName[Program.SPLASH]);

            gl3es3.glActiveTexture(GL_TEXTURE0);
            gl3es3.glBindVertexArray(vertexArrayName[Program.SPLASH]);
            gl3es3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.COLORBUFFER]);

            gl3es3.glDrawArraysInstanced(GL_TRIANGLES, 0, 3, 1);
        }

        return true;
    }
}
