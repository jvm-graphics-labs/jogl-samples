/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_400;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2ES3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.BufferUtils;
import glm.glm;
import glm.mat._4.Mat4;
import glm.vec._3.Vec3;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import glf.Vertex_v2fv2f;
import glm.vec._2.Vec2;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import jglm.Vec4i;

/**
 *
 * @author GBarbieri
 */
public class Gl_400_blend_rtt extends Test {

    public static void main(String[] args) {
        Gl_400_blend_rtt gl_400_blend_rtt = new Gl_400_blend_rtt();
    }

    public Gl_400_blend_rtt() {
        super("gl-400-blend-rtt", Profile.CORE, 4, 0);
    }

    private final String SHADERS_SOURCE1 = "blend-rtt";
    private final String SHADERS_SOURCE2 = "blend-rtt-blit";
    private final String SHADERS_ROOT = "src/data/gl_400";

    private int vertexCount = 4;
    private int vertexSize = vertexCount * Vertex_v2fv2f.SIZE;
    private float[] vertexData = {
        -1.0f, -1.0f,/**/ 0.0f, 0.0f,
        +1.0f, -1.0f,/**/ 1.0f, 0.0f,
        +1.0f, +1.0f,/**/ 1.0f, 1.0f,
        -1.0f, +1.0f,/**/ 0.0f, 1.0f};

    private int elementCount = 6;
    private int elementSize = elementCount * Short.BYTES;
    private short[] elementData = {
        0, 1, 2,
        2, 3, 0
    };

    private class Texture {

        public static final int R = 0;
        public static final int G = 1;
        public static final int B = 2;
        public static final int MAX = 3;
    };

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int ELEMENT = 1;
        public static final int MAX = 2;
    }

    private class Program {

        public static final int COLORBUFFERS = 0;
        public static final int BLIT = 1;
        public static final int MAX = 2;
    }

    private int uniformMvpSingle, uniformDiffuseSingle, uniformMvpMultiple;
    private int[] framebufferName = {0}, vertexArrayName = {0}, bufferName = new int[Buffer.MAX],
            textureName = new int[Texture.MAX], programName = new int[Program.MAX];
    private Vec4i[] viewport = new Vec4i[Texture.MAX];

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        viewport[Texture.R] = new Vec4i(windowSize.x >> 1, 0, windowSize.x >> 1, windowSize.y >> 1);
        viewport[Texture.G] = new Vec4i(windowSize.x >> 1, windowSize.y >> 1, windowSize.x >> 1, windowSize.y >> 1);
        viewport[Texture.B] = new Vec4i(0, windowSize.y >> 1, windowSize.x >> 1, windowSize.y >> 1);

        boolean validated = true;

        if (validated) {
            validated = initBlend(gl4);
        }
        if (validated) {
            validated = initProgram(gl4);
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

        return validated;
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE1, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE1, "frag", null, true);

            shaderProgram.init(gl4);

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);

            programName[Program.COLORBUFFERS] = shaderProgram.program();

            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            uniformMvpMultiple = gl4.glGetUniformLocation(programName[Program.COLORBUFFERS], "mvp");
        }

        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE2, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE2, "frag", null, true);

            shaderProgram.init(gl4);

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);

            programName[Program.BLIT] = shaderProgram.program();

            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            uniformMvpSingle = gl4.glGetUniformLocation(programName[Program.BLIT], "mvp");
            uniformDiffuseSingle = gl4.glGetUniformLocation(programName[Program.BLIT], "diffuse");
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        gl4.glGenBuffers(Buffer.MAX, bufferName, 0);

        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT]);
        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(elementBuffer);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl4.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(vertexBuffer);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        return true;
    }

    private boolean initTexture(GL4 gl4) {

        gl4.glActiveTexture(GL_TEXTURE0);
        gl4.glGenTextures(Texture.MAX, textureName, 0);

        for (int i = Texture.R; i <= Texture.B; ++i) {
            gl4.glBindTexture(GL_TEXTURE_2D, textureName[i]);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 1000);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_R, GL_RED);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_G, GL_GREEN);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_B, GL_BLUE);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_A, GL_ALPHA);

            gl4.glTexImage2D(GL_TEXTURE_2D, 0,
                    GL_RGBA8,
                    320, 240,
                    0,
                    GL_RGBA, GL_UNSIGNED_BYTE,
                    null);
        }

        return true;
    }

    private boolean initFramebuffer(GL4 gl4) {

        gl4.glGenFramebuffers(1, framebufferName, 0);
        gl4.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[0]);

        gl4.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, textureName[0], 0);
        gl4.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, textureName[1], 0);
        gl4.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT2, textureName[2], 0);

        int[] drawBuffers = new int[4];
        drawBuffers[0] = GL_NONE;
        drawBuffers[1] = GL_COLOR_ATTACHMENT0;
        drawBuffers[2] = GL_COLOR_ATTACHMENT1;
        drawBuffers[3] = GL_COLOR_ATTACHMENT2;

        gl4.glDrawBuffers(4, drawBuffers, 0);

        if (!isFramebufferComplete(gl4, framebufferName[0])) {
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
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, Vertex_v2fv2f.SIZE, 0);
            gl4.glVertexAttribPointer(Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, Vertex_v2fv2f.SIZE, Vec2.SIZE);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl4.glEnableVertexAttribArray(Semantic.Attr.TEXCOORD);

            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT]);
        }
        gl4.glBindVertexArray(0);

        return true;
    }

    private boolean initBlend(GL4 gl4) {

        gl4.glEnable(GL_SAMPLE_MASK);
        gl4.glSampleMaski(0, 0xFF);

        gl4.glEnablei(GL_BLEND, 0);
        gl4.glColorMaski(0, true, true, true, true);
        gl4.glBlendEquationSeparatei(0, GL_FUNC_ADD, GL_FUNC_ADD);
        gl4.glBlendFuncSeparatei(0, GL_SRC_ALPHA, GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        gl4.glEnablei(GL_BLEND, 1);
        gl4.glColorMaski(1, true, false, false, true);
        gl4.glBlendEquationSeparatei(1, GL_FUNC_ADD, GL_FUNC_ADD);
        gl4.glBlendFuncSeparatei(1, GL_SRC_ALPHA, GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        gl4.glEnablei(GL_BLEND, 2);
        gl4.glColorMaski(2, false, true, false, true);
        gl4.glBlendEquationSeparatei(2, GL_FUNC_ADD, GL_FUNC_ADD);
        gl4.glBlendFuncSeparatei(2, GL_SRC_ALPHA, GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        gl4.glEnablei(GL_BLEND, 3);
        gl4.glColorMaski(3, false, false, true, true);
        gl4.glBlendEquationSeparatei(3, GL_FUNC_ADD, GL_FUNC_ADD);
        gl4.glBlendFuncSeparatei(3, GL_SRC_ALPHA, GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        // Pass 1: Compute the MVP (Model View Projection matrix)
        Mat4 projection = glm.ortho_(-1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f);
        Mat4 viewTranslate = new Mat4(1.0f).translate(new Vec3(0.0f, 0.0f, 0.0f));
        Mat4 view = viewTranslate;
        Mat4 model = new Mat4(1.0f);
        Mat4 mvp = projection.mul(view).mul(model);

        gl4.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[0]);
        gl4.glViewport(0, 0, windowSize.x, windowSize.y);

        gl4.glUseProgram(programName[Program.COLORBUFFERS]);
        gl4.glUniformMatrix4fv(uniformMvpMultiple, 1, false, mvp.toFa_(), 0);

        gl4.glBindVertexArray(vertexArrayName[0]);
        gl4.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0);

        // Pass 2
        gl4.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{0.5f, 0.5f, 0.5f, 0.5f}, 0);

        gl4.glUseProgram(programName[Program.BLIT]);
        gl4.glUniform1i(uniformDiffuseSingle, 0);

        {
            projection.ortho(-1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f);
            view.identity();
            model.identity();
            mvp = projection.mul(view).mul(model);
            gl4.glUniformMatrix4fv(uniformMvpSingle, 1, false, mvp.toFa_(), 0);
        }

        for (int i = 0; i < Texture.MAX; ++i) {
            
            gl4.glViewport(viewport[i].x, viewport[i].y, viewport[i].z, viewport[i].w);

            gl4.glActiveTexture(GL_TEXTURE0);
            gl4.glBindTexture(GL_TEXTURE_2D, textureName[i]);

            gl4.glBindVertexArray(vertexArrayName[0]);
            gl4.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0);
        }

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteBuffers(Buffer.MAX, bufferName, 0);
        gl4.glDeleteTextures(Texture.MAX, textureName, 0);
        gl4.glDeleteProgram(programName[Program.BLIT]);
        gl4.glDeleteProgram(programName[Program.COLORBUFFERS]);
        gl4.glDeleteFramebuffers(1, framebufferName, 0);

        return checkError(gl4, "end");
    }
}
