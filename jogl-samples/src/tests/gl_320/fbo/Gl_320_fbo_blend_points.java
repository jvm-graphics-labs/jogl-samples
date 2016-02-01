/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_320.fbo;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2ES1.GL_POINT_SPRITE;
import com.jogamp.opengl.GL2GL3;
import static com.jogamp.opengl.GL2GL3.*;
import com.jogamp.opengl.GL3;
import static com.jogamp.opengl.GL3.GL_PROGRAM_POINT_SIZE;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.BufferUtils;
import framework.Glm;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 *
 * @author GBarbieri
 */
public class Gl_320_fbo_blend_points extends Test {

    public static void main(String[] args) {
        Gl_320_fbo_blend_points gl_320_fbo_blend_points = new Gl_320_fbo_blend_points();
    }

    public Gl_320_fbo_blend_points() {
        super("gl-320-fbo-blend-points", Profile.CORE, 3, 2);
    }

    private final String SHADERS_SOURCE_RENDER = "fbo-blend-points";
    private final String SHADERS_SOURCE_SPLASH = "fbo-blend-points-blit";
    private final String SHADERS_ROOT = "src/data/gl_320/fbo";

    private int vertexCount = 8;
    private int vertexSize = vertexCount * 2 * 4 * Float.BYTES;
    private float scale = 0.2f;
    private float[] vertexData = new float[vertexCount * 2 * 4];
    private float[][] vertexV2f = {
        {-1.0f, -1.0f},
        {+1.0f, -1.0f},
        {+1.0f, +1.0f},
        {-1.0f, +1.0f},
        {+1.0f, +0.0f},
        {+0.0f, +1.0f},
        {-1.0f, +0.0f},
        {+0.0f, -1.0f}};
    private float[][] vertexV4f = {
        {1.0f, 0.5f, 0.0f, 1.0f},
        {0.0f, 1.0f, 0.5f, 1.0f},
        {0.5f, 0.0f, 1.0f, 1.0f},
        {1.0f, 0.0f, 0.5f, 1.0f},
        {0.5f, 1.0f, 0.0f, 1.0f},
        {0.0f, 0.5f, 1.0f, 1.0f},
        {0.4f, 0.6f, 0.5f, 1.0f},
        {0.5f, 0.4f, 0.6f, 1.0f}};

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int TRANSFORM = 1;
        public static final int MAX = 2;
    }

    private class Texture {

        public static final int COLORBUFFER = 0;
        public static final int MAX = 1;
    }

    private class Program {

        public static final int RENDER = 0;
        public static final int SPLASH = 1;
        public static final int MAX = 2;
    }

    private class Shader {

        public static final int VERT_TEXTURE = 0;
        public static final int FRAG_TEXTURE = 1;
        public static final int VERT_SPLASH = 2;
        public static final int FRAG_SPLASH = 3;
        public static final int MAX = 4;
    }

    private int[] programName = new int[Program.MAX], bufferName = new int[Buffer.MAX],
            textureName = new int[Texture.MAX], vertexArrayName = new int[Program.MAX],
            framebufferName = new int[1];
    private int uniformTransform, uniformDiffuse, framebufferScale = 2;
    private float[] projection = new float[16], model = new float[16];

    @Override
    protected boolean begin(GL gl) {

        GL3 gl3 = (GL3) gl;

        boolean validated = true;

        gl3.glEnable(GL_PROGRAM_POINT_SIZE);
        gl3.glEnable(GL_POINT_SPRITE);
        gl3.glPointParameteri(GL_POINT_SPRITE_COORD_ORIGIN, GL_LOWER_LEFT);

        float[] pointSizeProperties = new float[3];
        gl3.glGetFloatv(GL2GL3.GL_POINT_SIZE_RANGE, pointSizeProperties, 0);
        gl3.glGetFloatv(GL2GL3.GL_POINT_SIZE_GRANULARITY, pointSizeProperties, 2);
        System.out.println("pointSizeRange: (" + pointSizeProperties[0] + ", " + pointSizeProperties[1] + ") "
                + "granularity: " + pointSizeProperties[2]);

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

        return validated;
    }

    private boolean initProgram(GL3 gl3) {

        boolean validated = true;

        ShaderCode[] shaderCode = new ShaderCode[Shader.MAX];

        if (validated) {

            shaderCode[Shader.VERT_TEXTURE] = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_RENDER, "vert", null, true);
            shaderCode[Shader.FRAG_TEXTURE] = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_RENDER, "frag", null, true);

            ShaderProgram shaderProgram = new ShaderProgram();
            shaderProgram.add(shaderCode[Shader.VERT_TEXTURE]);
            shaderProgram.add(shaderCode[Shader.FRAG_TEXTURE]);

            shaderProgram.init(gl3);

            programName[Program.RENDER] = shaderProgram.program();

            gl3.glBindAttribLocation(programName[Program.RENDER], Semantic.Attr.POSITION, "position");
            gl3.glBindAttribLocation(programName[Program.RENDER], Semantic.Attr.COLOR, "color");
            gl3.glBindFragDataLocation(programName[Program.RENDER], Semantic.Frag.COLOR, "color");

            shaderProgram.link(gl3, System.out);
        }
        if (validated) {

            shaderCode[Shader.VERT_SPLASH] = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_SPLASH, "vert", null, true);
            shaderCode[Shader.FRAG_SPLASH] = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_SPLASH, "frag", null, true);

            ShaderProgram shaderProgram = new ShaderProgram();
            shaderProgram.add(shaderCode[Shader.VERT_SPLASH]);
            shaderProgram.add(shaderCode[Shader.FRAG_SPLASH]);

            shaderProgram.init(gl3);

            programName[Program.SPLASH] = shaderProgram.program();

            gl3.glBindFragDataLocation(programName[Program.SPLASH], Semantic.Frag.COLOR, "color");

            shaderProgram.link(gl3, System.out);
        }
        if (validated) {

            uniformTransform = gl3.glGetUniformBlockIndex(programName[Program.RENDER], "Transform");
            uniformDiffuse = gl3.glGetUniformLocation(programName[Program.SPLASH], "diffuse");

            gl3.glUseProgram(programName[Program.RENDER]);
            gl3.glUniformBlockBinding(programName[Program.RENDER], uniformTransform,
                    Semantic.Uniform.TRANSFORM0);

            gl3.glUseProgram(programName[Program.SPLASH]);
            gl3.glUniform1i(uniformDiffuse, 0);
        }

        return validated & checkError(gl3, "initProgram");
    }

    private boolean initBuffer(GL3 gl3) {

        gl3.glGenBuffers(Buffer.MAX, bufferName, 0);

        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
        for (int vertex = 0; vertex < vertexCount; vertex++) {
            float[] normalized = Glm.normalize(vertexV2f[vertex]);
            for (int i = 0; i < normalized.length; i++) {
                vertexData[vertex * 2 * 4 + i] = normalized[i] * scale;
                vertexData[vertex * 2 * 4 + 2 + i] = i;
                vertexData[vertex * 2 * 4 + 4 + i] = vertexV4f[vertex][i];
                vertexData[vertex * 2 * 4 + 4 + 2 + i] = vertexV4f[vertex][2 + i];
            }
        }
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl3.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(vertexBuffer);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        int[] uniformBufferOffset = {0};
        gl3.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset, 0);
        int uniformBlockSize = Math.max(16 * Float.BYTES, uniformBufferOffset[0]);

        gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
        gl3.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_DYNAMIC_DRAW);
        gl3.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return true;
    }

    private boolean initTexture(GL3 gl3) {

        boolean validated = true;

        gl3.glGenTextures(Texture.MAX, textureName, 0);
        gl3.glActiveTexture(GL_TEXTURE0);
        gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.COLORBUFFER]);
        gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
        gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
        gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        gl3.glTexImage2D(GL_TEXTURE_2D, 0, GL_SRGB8, windowSize.x * framebufferScale,
                windowSize.y * framebufferScale, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);

        return validated;
    }

    private boolean initVertexArray(GL3 gl3) {

        gl3.glGenVertexArrays(Program.MAX, vertexArrayName, 0);
        gl3.glBindVertexArray(vertexArrayName[Program.RENDER]);
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 4, GL_FLOAT, false, 2 * 4 * Float.BYTES, 0);
            gl3.glVertexAttribPointer(Semantic.Attr.COLOR, 4, GL_FLOAT, false, 2 * 4 * Float.BYTES, 4 * Float.BYTES);
            gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl3.glEnableVertexAttribArray(Semantic.Attr.COLOR);
        }
        gl3.glBindVertexArray(0);

        gl3.glBindVertexArray(vertexArrayName[Program.SPLASH]);
        gl3.glBindVertexArray(0);

        return true;
    }

    private boolean initFramebuffer(GL3 gl3) {

        gl3.glGenFramebuffers(1, framebufferName, 0);
        gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[0]);
        gl3.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, textureName[Texture.COLORBUFFER], 0);

        if (!isFramebufferComplete(gl3, framebufferName[0])) {
            return false;
        }

        gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL3 gl3 = (GL3) gl;

        {
            gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
            ByteBuffer pointer = gl3.glMapBufferRange(GL_UNIFORM_BUFFER,
                    0, 16 * Float.BYTES, GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            FloatUtil.makePerspective(projection, 0, true, (float) Math.PI * 0.25f,
                    (float) windowSize.x / windowSize.y, 0.1f, 100.0f);
            FloatUtil.makeIdentity(model);

            FloatUtil.multMatrix(projection, view());
            FloatUtil.multMatrix(projection, model);

            pointer.asFloatBuffer().put(projection).rewind();

            // Make sure the uniform buffer is uploaded
            gl3.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        {
            gl3.glViewport(0, 0, windowSize.x * framebufferScale, windowSize.y * framebufferScale);

            gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[0]);
            gl3.glClearBufferfv(GL_COLOR, 0, new float[]{0.0f, 0.0f, 0.0f, 1.0f}, 0);
            gl3.glEnable(GL_FRAMEBUFFER_SRGB);

            gl3.glEnable(GL_BLEND);
            gl3.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

            gl3.glUseProgram(programName[Program.RENDER]);

            gl3.glBindVertexArray(vertexArrayName[Program.RENDER]);
            gl3.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.TRANSFORM]);

            gl3.glDrawArraysInstanced(GL_POINTS, 0, 8, 1);

            gl3.glDisable(GL_BLEND);
        }

        {
            gl3.glViewport(0, 0, windowSize.x, windowSize.y);

            gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);
            gl3.glDisable(GL_FRAMEBUFFER_SRGB);

            gl3.glUseProgram(programName[Program.SPLASH]);

            gl3.glActiveTexture(GL_TEXTURE0);
            gl3.glBindVertexArray(vertexArrayName[Program.SPLASH]);
            gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.COLORBUFFER]);

            gl3.glDrawArraysInstanced(GL_TRIANGLES, 0, 3, 1);
        }

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;

        gl3.glDeleteFramebuffers(1, framebufferName, 0);
        gl3.glDeleteProgram(programName[Program.SPLASH]);
        gl3.glDeleteProgram(programName[Program.RENDER]);

        gl3.glDeleteBuffers(Buffer.MAX, bufferName, 0);
        gl3.glDeleteTextures(Texture.MAX, textureName, 0);
        gl3.glDeleteVertexArrays(Program.MAX, vertexArrayName, 0);

        return true;
    }
}
