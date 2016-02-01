/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_410;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2GL3.GL_TEXTURE_LOD_BIAS;
import static com.jogamp.opengl.GL3ES3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import jglm.Vec2i;

/**
 *
 * @author GBarbieri
 */
public class Gl_410_fbo_layered extends Test {

    public static void main(String[] args) {
        Gl_410_fbo_layered gl_410_fbo_layered = new Gl_410_fbo_layered();
    }

    public Gl_410_fbo_layered() {
        super("gl-410-fbo-layered", Profile.CORE, 4, 1);
    }

    private final String SHADERS_SOURCE1 = "layer";
    private final String SHADERS_SOURCE2 = "viewport";
    private final String SHADERS_ROOT = "src/data/gl_410";

    private Vec2i FRAMEBUFFER_SIZE = new Vec2i(640, 480);

    private int vertexCount = 4;
    private int vertexSize = vertexCount * 2 * 2 * Float.BYTES;
    private float[] vertexData = {
        -1.0f, -1.0f, 0.0f, 0.0f,
        +1.0f, -1.0f, 1.0f, 0.0f,
        +1.0f, +1.0f, 1.0f, 1.0f,
        -1.0f, +1.0f, 0.0f, 1.0f};

    private int elementCount = 6;
    private int elementSize = elementCount * Short.BYTES;
    private short[] elementData = {
        0, 1, 2,
        2, 3, 0};

    private enum Buffer {
        VERTEX,
        ELEMENT,
        MAX
    };

    private enum Program {
        LAYERING,
        VIEWPORT,
        MAX
    };

    private int[] framebufferName = {0}, vertexArrayName = new int[Program.MAX.ordinal()],
            programName = new int[Program.MAX.ordinal()], uniformMvp = new int[Program.MAX.ordinal()],
            samplerName = {0}, bufferName = new int[Buffer.MAX.ordinal()], textureColorbufferName = {0};
    private int uniformDiffuse;
    private float[] projection = new float[16], view = new float[16], model = new float[16], mvp = new float[16];

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;

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
        if (validated) {
            validated = initSampler(gl4);
        }

        return validated;
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        // Create program
        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            ShaderCode vertexShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE1, "vert", null, true);
            ShaderCode geometryShaderCode = ShaderCode.create(gl4, GL_GEOMETRY_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE1, "geom", null, true);
            ShaderCode fragmentShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE1, "frag", null, true);

            shaderProgram.init(gl4);

            shaderProgram.add(vertexShaderCode);
            shaderProgram.add(geometryShaderCode);
            shaderProgram.add(fragmentShaderCode);

            programName[Program.LAYERING.ordinal()] = shaderProgram.program();

            shaderProgram.link(gl4, System.out);
        }

        // Create program
        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            ShaderCode vertexShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE2, "vert", null, true);
            ShaderCode geometryShaderCode = ShaderCode.create(gl4, GL_GEOMETRY_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE2, "geom", null, true);
            ShaderCode fragmentShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE2, "frag", null, true);

            shaderProgram.init(gl4);

            shaderProgram.add(vertexShaderCode);
            shaderProgram.add(geometryShaderCode);
            shaderProgram.add(fragmentShaderCode);

            programName[Program.VIEWPORT.ordinal()] = shaderProgram.program();

            shaderProgram.link(gl4, System.out);

        }

        // Get variables locations
        if (validated) {

            for (int i = 0; i < Program.MAX.ordinal(); ++i) {
                uniformMvp[i] = gl4.glGetUniformLocation(programName[i], "mvp");
            }
            uniformDiffuse = gl4.glGetUniformLocation(programName[Program.VIEWPORT.ordinal()], "diffuse");
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        gl4.glGenBuffers(Buffer.MAX.ordinal(), bufferName, 0);

        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT.ordinal()]);
        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl4.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        return checkError(gl4, "initBuffer");
    }

    private boolean initTexture(GL4 gl4) {

        gl4.glGenTextures(1, textureColorbufferName, 0);

        gl4.glActiveTexture(GL_TEXTURE0);
        gl4.glBindTexture(GL_TEXTURE_2D_ARRAY, textureColorbufferName[0]);
        gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_BASE_LEVEL, 0);
        gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAX_LEVEL, 1000);
        gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SWIZZLE_R, GL_RED);
        gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SWIZZLE_G, GL_GREEN);
        gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SWIZZLE_B, GL_BLUE);
        gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SWIZZLE_A, GL_ALPHA);

        gl4.glTexImage3D(
                GL_TEXTURE_2D_ARRAY,
                0,
                GL_RGB,
                FRAMEBUFFER_SIZE.x,
                FRAMEBUFFER_SIZE.y,
                4, //depth
                0,
                GL_RGB,
                GL_UNSIGNED_BYTE,
                null);

        return checkError(gl4, "initTexture");
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

        return checkError(gl4, "initSampler");
    }

    private boolean initFramebuffer(GL4 gl4) {

        int[] buffersRender = {GL_COLOR_ATTACHMENT0};
        gl4.glGenFramebuffers(1, framebufferName, 0);
        gl4.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[0]);
        gl4.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, textureColorbufferName[0], 0);
        gl4.glDrawBuffers(1, buffersRender, 0);
        if (!isFramebufferComplete(gl4, framebufferName[0])) {
            return false;
        }

        gl4.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        gl4.glDrawBuffer(GL_BACK);
        if (!isFramebufferComplete(gl4, 0)) {
            return false;
        }

        return checkError(gl4, "initFramebuffer");
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(Program.MAX.ordinal(), vertexArrayName, 0);

        gl4.glBindVertexArray(vertexArrayName[Program.VIEWPORT.ordinal()]);
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 2 * 2 * Float.BYTES, 0);
            gl4.glVertexAttribPointer(Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, 2 * 2 * Float.BYTES, 2 * Float.BYTES);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl4.glEnableVertexAttribArray(Semantic.Attr.TEXCOORD);

            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT.ordinal()]);
        }
        gl4.glBindVertexArray(0);

        gl4.glBindVertexArray(vertexArrayName[Program.LAYERING.ordinal()]);
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 2 * 2 * Float.BYTES, 0);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);

            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT.ordinal()]);
        }
        gl4.glBindVertexArray(0);

        return checkError(gl4, "initVertexArray");
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        FloatUtil.makeOrtho(projection, 0, true, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f);
        FloatUtil.makeIdentity(view);
        FloatUtil.makeIdentity(model);
        FloatUtil.multMatrix(projection, view, mvp);
        FloatUtil.multMatrix(mvp, model);

        gl4.glProgramUniformMatrix4fv(programName[Program.LAYERING.ordinal()],
                uniformMvp[Program.LAYERING.ordinal()], 1, false, mvp, 0);
        gl4.glProgramUniformMatrix4fv(programName[Program.VIEWPORT.ordinal()],
                uniformMvp[Program.VIEWPORT.ordinal()], 1, false, mvp, 0);
        gl4.glProgramUniform1i(programName[Program.VIEWPORT.ordinal()], uniformDiffuse, 0);

        // Pass 1
        {
            gl4.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[0]);
            gl4.glViewportIndexedfv(0, new float[]{0, 0, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y}, 0);

            gl4.glUseProgram(programName[Program.LAYERING.ordinal()]);

            gl4.glBindVertexArray(vertexArrayName[Program.LAYERING.ordinal()]);
            gl4.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0);
        }

        // Pass 2
        {
            int border = 2;

            gl4.glBindFramebuffer(GL_FRAMEBUFFER, 0);

            gl4.glClearBufferfv(GL_COLOR, 0, new float[]{0, 0, 0, 0}, 0);

            gl4.glViewportIndexedfv(0, new float[]{border, border, windowSize.x * 0.5f - 2.0f * border,
                windowSize.y * 0.5f - 2.0f * border}, 0);
            gl4.glViewportIndexedfv(1, new float[]{windowSize.x * 0.5f + border, border,
                windowSize.x * 0.5f - 2.0f * border, windowSize.y * 0.5f - 2.0f * border}, 0);
            gl4.glViewportIndexedfv(2, new float[]{windowSize.x * 0.5f + border, windowSize.y * 0.5f + 1,
                windowSize.x * 0.5f - 2.0f * border, windowSize.y * 0.5f - 2.0f * border}, 0);
            gl4.glViewportIndexedfv(3, new float[]{border, windowSize.y * 0.5f + border,
                windowSize.x * 0.5f - 2.0f * border, windowSize.y * 0.5f - 2.0f * border}, 0);

            gl4.glUseProgram(programName[Program.VIEWPORT.ordinal()]);

            gl4.glActiveTexture(GL_TEXTURE0);
            gl4.glBindTexture(GL_TEXTURE_2D_ARRAY, textureColorbufferName[0]);
            gl4.glBindSampler(0, samplerName[0]);

            gl4.glBindVertexArray(vertexArrayName[Program.VIEWPORT.ordinal()]);
            gl4.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0);
        }

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteVertexArrays(Program.MAX.ordinal(), vertexArrayName, 0);
        gl4.glDeleteBuffers(Buffer.MAX.ordinal(), bufferName, 0);
        gl4.glDeleteTextures(1, textureColorbufferName, 0);
        gl4.glDeleteFramebuffers(1, framebufferName, 0);
        gl4.glDeleteProgram(programName[Program.VIEWPORT.ordinal()]);
        gl4.glDeleteProgram(programName[Program.LAYERING.ordinal()]);
        gl4.glDeleteSamplers(1, samplerName, 0);

        return true;
    }
}
