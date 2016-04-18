/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_400;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2GL3.GL_TEXTURE_LOD_BIAS;
import static com.jogamp.opengl.GL3ES3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.BufferUtils;
import glm.glm;
import glm.mat._4.Mat4;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import glm.vec._2.Vec2;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
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
    private int vertexSize = vertexCount * Vec2.SIZE;
    private float[] vertexData = {
        -1.0f, -1.0f,
        +1.0f, -1.0f,
        +1.0f, +1.0f,
        -1.0f, +1.0f};

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

    private class Program {

        public static final int LAYERING = 0;
        public static final int IMAGE_2D = 1;
        public static final int MAX = 2;
    };

    private IntBuffer framebufferName = GLBuffers.newDirectIntBuffer(1),
            vertexArrayName = GLBuffers.newDirectIntBuffer(Program.MAX),
            bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX),
            textureColorbufferName = GLBuffers.newDirectIntBuffer(1), samplerName = GLBuffers.newDirectIntBuffer(1);
    private int[] programName = new int[Program.MAX];
    private int uniformMvp, uniformDiffuse, uniformLayer;
    private Vec4i[] viewport = new Vec4i[4];

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

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER, this.getClass(), SHADERS_ROOT, null,
                    SHADERS_SOURCE1, "vert", null, true);
            ShaderCode geomShaderCode = ShaderCode.create(gl4, GL_GEOMETRY_SHADER, this.getClass(), SHADERS_ROOT, null,
                    SHADERS_SOURCE1, "geom", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER, this.getClass(), SHADERS_ROOT, null,
                    SHADERS_SOURCE1, "frag", null, true);

            shaderProgram.init(gl4);

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(geomShaderCode);
            shaderProgram.add(fragShaderCode);

            programName[Program.LAYERING] = shaderProgram.program();

            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER, this.getClass(), SHADERS_ROOT, null,
                    SHADERS_SOURCE2, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER, this.getClass(), SHADERS_ROOT, null,
                    SHADERS_SOURCE2, "frag", null, true);

            shaderProgram.init(gl4);

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);

            programName[Program.IMAGE_2D] = shaderProgram.program();

            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            uniformMvp = gl4.glGetUniformLocation(programName[Program.LAYERING], "mvp");
            uniformDiffuse = gl4.glGetUniformLocation(programName[Program.IMAGE_2D], "diffuse");
            uniformLayer = gl4.glGetUniformLocation(programName[Program.IMAGE_2D], "layer");
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);

        gl4.glGenBuffers(Buffer.MAX, bufferName);

        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT));
        gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
        gl4.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        BufferUtils.destroyDirectBuffer(elementBuffer);
        BufferUtils.destroyDirectBuffer(vertexBuffer);

        return checkError(gl4, "initBuffer");
    }

    private boolean initTexture(GL4 gl4) {

        gl4.glGenTextures(1, textureColorbufferName);

        gl4.glActiveTexture(GL_TEXTURE0);
        gl4.glBindTexture(GL_TEXTURE_2D_ARRAY, textureColorbufferName.get(0));
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

        IntBuffer buffers = GLBuffers.newDirectIntBuffer(new int[]{GL_COLOR_ATTACHMENT0});
        gl4.glGenFramebuffers(1, framebufferName);
        gl4.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName.get(0));
        gl4.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, textureColorbufferName.get(0), 0);
        gl4.glDrawBuffers(1, buffers);
        if (!isFramebufferComplete(gl4, framebufferName.get(0))) {
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

        gl4.glGenVertexArrays(Program.MAX, vertexArrayName);

        gl4.glBindVertexArray(vertexArrayName.get(Program.IMAGE_2D));
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, Vec2.SIZE, 0);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);

            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT));
        }
        gl4.glBindVertexArray(0);

        gl4.glBindVertexArray(vertexArrayName.get(Program.LAYERING));
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, Vec2.SIZE, 0);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);

            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT));
        }
        gl4.glBindVertexArray(0);

        return checkError(gl4, "initVertexArray");
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

        BufferUtils.destroyDirectBuffer(borderColor);

        return checkError(gl4, "initSampler");
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        Mat4 projection = glm.ortho_(-1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f);
        Mat4 view = new Mat4(1.0f);
        Mat4 model = new Mat4(1.0f);
        Mat4 mvp = projection.mul(view).mul(model);

        gl4.glViewport(0, 0, windowSize.x, windowSize.y);
        gl4.glClearBufferfv(GL_COLOR, 0, clearColor.put(0, 0).put(1, 0).put(2, 0).put(3, 0));

        // Pass 1
        {
            gl4.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName.get(0));
            gl4.glViewport(0, 0, windowSize.x / FRAMEBUFFER_SIZE, windowSize.y / FRAMEBUFFER_SIZE);

            gl4.glUseProgram(programName[Program.LAYERING]);
            gl4.glUniformMatrix4fv(uniformMvp, 1, false, mvp.toFa_(), 0);

            gl4.glBindVertexArray(vertexArrayName.get(Program.LAYERING));
            gl4.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0);
        }

        // Pass 2
        {
            gl4.glBindFramebuffer(GL_FRAMEBUFFER, 0);

            gl4.glUseProgram(programName[Program.IMAGE_2D]);
            gl4.glUniform1i(uniformDiffuse, 0);

            gl4.glActiveTexture(GL_TEXTURE0);
            gl4.glBindTexture(GL_TEXTURE_2D_ARRAY, textureColorbufferName.get(0));
            gl4.glBindSampler(0, samplerName.get(0));

            gl4.glBindVertexArray(vertexArrayName.get(Program.IMAGE_2D));

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

        gl4.glDeleteVertexArrays(Program.MAX, vertexArrayName);
        gl4.glDeleteBuffers(Buffer.MAX, bufferName);
        gl4.glDeleteTextures(1, textureColorbufferName);
        gl4.glDeleteFramebuffers(1, framebufferName);
        gl4.glDeleteSamplers(1, samplerName);
        gl4.glDeleteProgram(programName[Program.IMAGE_2D]);
        gl4.glDeleteProgram(programName[Program.LAYERING]);

        BufferUtils.destroyDirectBuffer(vertexArrayName);
        BufferUtils.destroyDirectBuffer(bufferName);
        BufferUtils.destroyDirectBuffer(textureColorbufferName);
        BufferUtils.destroyDirectBuffer(framebufferName);
        BufferUtils.destroyDirectBuffer(samplerName);

        return checkError(gl4, "end");
    }
}
