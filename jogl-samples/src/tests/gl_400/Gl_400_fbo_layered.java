/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_400;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_ALPHA;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_BACK;
import static com.jogamp.opengl.GL.GL_CLAMP_TO_EDGE;
import static com.jogamp.opengl.GL.GL_COLOR_ATTACHMENT0;
import static com.jogamp.opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_FRAMEBUFFER;
import static com.jogamp.opengl.GL.GL_LEQUAL;
import static com.jogamp.opengl.GL.GL_NEAREST;
import static com.jogamp.opengl.GL.GL_NONE;
import static com.jogamp.opengl.GL.GL_RGB;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_S;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_T;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNSIGNED_BYTE;
import static com.jogamp.opengl.GL.GL_UNSIGNED_SHORT;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_RED;
import static com.jogamp.opengl.GL2ES2.GL_TEXTURE_BORDER_COLOR;
import static com.jogamp.opengl.GL2ES2.GL_TEXTURE_COMPARE_FUNC;
import static com.jogamp.opengl.GL2ES2.GL_TEXTURE_COMPARE_MODE;
import static com.jogamp.opengl.GL2ES2.GL_TEXTURE_WRAP_R;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES3.GL_BLUE;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import static com.jogamp.opengl.GL2ES3.GL_GREEN;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_2D_ARRAY;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_BASE_LEVEL;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_MAX_LEVEL;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_MAX_LOD;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_MIN_LOD;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_SWIZZLE_A;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_SWIZZLE_B;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_SWIZZLE_G;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_SWIZZLE_R;
import static com.jogamp.opengl.GL2GL3.GL_TEXTURE_LOD_BIAS;
import static com.jogamp.opengl.GL3ES3.GL_GEOMETRY_SHADER;
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
import jglm.Vec4i;

/**
 *
 * @author GBarbieri
 */
public class Gl_400_fbo_layered extends Test {

    public static void main(String[] args) {
        Gl_400_fbo_layered gl_400_fbo_layered = new Gl_400_fbo_layered();
    }

    public Gl_400_fbo_layered() {
        super("gl-400-fbo-layered", Profile.CORE, 4, 0);
    }

    private final String SHADERS_SOURCE1 = "layer";
    private final String SHADERS_SOURCE2 = "rtt-array";
    private final String SHADERS_ROOT = "src/data/gl_400";

    private final int FRAMEBUFFER_SIZE = 2;

    private int vertexCount = 4;
    private int vertexSize = vertexCount * 2 * Float.BYTES;
    private float[] vertexData = {
        -1.0f, -1.0f,
        +1.0f, -1.0f,
        +1.0f, +1.0f,
        -1.0f, +1.0f,};

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
        IMAGE_2D,
        MAX
    };

    private int[] framebufferName = {0}, vertexArrayName = new int[Program.MAX.ordinal()],
            programName = new int[Program.MAX.ordinal()], bufferName = new int[Buffer.MAX.ordinal()],
            textureColorbufferName = {0}, samplerName = {0};
    private int uniformMvp, uniformDiffuse, uniformLayer;
    private Vec4i[] viewport = new Vec4i[4];
    private float[] projection = new float[16], view = new float[16], model = new float[16], mvp = new float[16];

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        Vec2i framebufferSize = new Vec2i(windowSize.x / FRAMEBUFFER_SIZE, windowSize.y / FRAMEBUFFER_SIZE);

        int border = 2;
        viewport[0] = new Vec4i(border, border, framebufferSize.x - 2 * border, framebufferSize.y - 2 * border);
        viewport[1] = new Vec4i((windowSize.x >> 1) + border, 1, framebufferSize.x - 2 * border,
                framebufferSize.y - 2 * border);
        viewport[2] = new Vec4i((windowSize.x >> 1) + border, (windowSize.y >> 1) + border,
                framebufferSize.x - 2 * border, framebufferSize.y - 2 * border);
        viewport[3] = new Vec4i(border, (windowSize.y >> 1) + border, framebufferSize.x - 2 * border,
                framebufferSize.y - 2 * border);

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

        return validated && checkError(gl4, "begin");
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE1, "vert", null, true);
            ShaderCode geomShaderCode = ShaderCode.create(gl4, GL_GEOMETRY_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE1, "geom", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE1, "frag", null, true);

            shaderProgram.init(gl4);

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(geomShaderCode);
            shaderProgram.add(fragShaderCode);

            programName[Program.LAYERING.ordinal()] = shaderProgram.program();

            shaderProgram.link(gl4, System.out);
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

            programName[Program.IMAGE_2D.ordinal()] = shaderProgram.program();

            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            uniformMvp = gl4.glGetUniformLocation(programName[Program.LAYERING.ordinal()], "mvp");
            uniformDiffuse = gl4.glGetUniformLocation(programName[Program.IMAGE_2D.ordinal()], "diffuse");
            uniformLayer = gl4.glGetUniformLocation(programName[Program.IMAGE_2D.ordinal()], "layer");
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
        gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAX_LEVEL, 0);
        gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SWIZZLE_R, GL_RED);
        gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SWIZZLE_G, GL_GREEN);
        gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SWIZZLE_B, GL_BLUE);
        gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SWIZZLE_A, GL_ALPHA);

        gl4.glTexImage3D(
                GL_TEXTURE_2D_ARRAY,
                0,
                GL_RGB,
                windowSize.x / FRAMEBUFFER_SIZE,
                windowSize.y / FRAMEBUFFER_SIZE,
                4, //depth
                0,
                GL_RGB,
                GL_UNSIGNED_BYTE,
                null);

        return checkError(gl4, "initTexture");
    }

    private boolean initFramebuffer(GL4 gl4) {

        int[] buffers = {GL_COLOR_ATTACHMENT0};
        gl4.glGenFramebuffers(1, framebufferName, 0);
        gl4.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[0]);
        gl4.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, textureColorbufferName[0], 0);
        gl4.glDrawBuffers(1, buffers, 0);
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

        gl4.glBindVertexArray(vertexArrayName[Program.IMAGE_2D.ordinal()]);
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);

            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT.ordinal()]);
        }
        gl4.glBindVertexArray(0);

        gl4.glBindVertexArray(vertexArrayName[Program.LAYERING.ordinal()]);
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);

            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT.ordinal()]);
        }
        gl4.glBindVertexArray(0);

        return checkError(gl4, "initVertexArray");
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

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        FloatUtil.makeOrtho(projection, 0, true, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f);
        FloatUtil.makeIdentity(view);
        FloatUtil.makeIdentity(model);
        FloatUtil.multMatrix(projection, view, mvp);
        FloatUtil.multMatrix(mvp, model);

        gl4.glViewport(0, 0, windowSize.x, windowSize.y);
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{0.0f, 0.0f, 0.0f, 0.0f}, 0);

        // Pass 1
        {
            gl4.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[0]);
            gl4.glViewport(0, 0, windowSize.x / FRAMEBUFFER_SIZE, windowSize.y / FRAMEBUFFER_SIZE);

            gl4.glUseProgram(programName[Program.LAYERING.ordinal()]);
            gl4.glUniformMatrix4fv(uniformMvp, 1, false, mvp, 0);

            gl4.glBindVertexArray(vertexArrayName[Program.LAYERING.ordinal()]);
            gl4.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0);
        }

        // Pass 2
        {
            gl4.glBindFramebuffer(GL_FRAMEBUFFER, 0);

            gl4.glUseProgram(programName[Program.IMAGE_2D.ordinal()]);
            gl4.glUniform1i(uniformDiffuse, 0);

            gl4.glActiveTexture(GL_TEXTURE0);
            gl4.glBindTexture(GL_TEXTURE_2D_ARRAY, textureColorbufferName[0]);
            gl4.glBindSampler(0, samplerName[0]);

            gl4.glBindVertexArray(vertexArrayName[Program.IMAGE_2D.ordinal()]);

            for (int i = 0; i < 4; ++i) {
                gl4.glUniform1i(uniformLayer, i);
                gl4.glViewport(viewport[i].x, viewport[i].y, viewport[i].z, viewport[i].w);
                gl4.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0);
            }
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
        gl4.glDeleteProgram(programName[Program.IMAGE_2D.ordinal()]);
        gl4.glDeleteProgram(programName[Program.LAYERING.ordinal()]);

        return checkError(gl4, "end");
    }
}
