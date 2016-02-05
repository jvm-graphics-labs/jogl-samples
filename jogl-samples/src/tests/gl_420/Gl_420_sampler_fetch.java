/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_420;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2ES3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import core.glm;
import dev.Mat4;
import dev.Vec3;
import framework.BufferUtils;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import jgli.Texture2d;
import jglm.Vec2i;

/**
 *
 * @author GBarbieri
 */
public class Gl_420_sampler_fetch extends Test {

    public static void main(String[] args) {
        Gl_420_sampler_fetch gl_420_sampler_fetch = new Gl_420_sampler_fetch();
    }

    public Gl_420_sampler_fetch() {
        super("gl-420-sampler-fetch", Profile.CORE, 4, 2);
    }

    private final String SHADER_SOURCE = "sampler-fetch";
    private final String SHADER_LIBRARY = "sampler-library";
    private final String SHADER_SOURCE_PROG = "sampler-fetch";
    private final String SHADER_SOURCE_FUNC = "sampler-fetch-builtin";
    private final String SHADERS_ROOT = "src/data/gl_420";
    private final String TEXTURE_DIFFUSE = "kueken7_rgba8_srgb.dds";

    private final float FRAMEBUFFER_SCALE = 0.125f;

    private int vertexCount = 4;
    private int vertexSize = vertexCount * 2 * 2 * Float.BYTES;
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

        public static final int VERTEX = 0;
        public static final int ELEMENT = 1;
        public static final int TRANSFORM = 2;
        public static final int MAX = 3;
    }

    private class Program {

        public static final int FUNC = 0;
        public static final int PROG = 1;
        public static final int MAX = 2;
    }

    private class Texture {

        public static final int DIFFUSE = 0;
        public static final int COLORBUFFER = 1;
        public static final int MAX = 2;
    }

    private int[] vertexArrayName = {0}, bufferName = new int[Buffer.MAX], pipelineName = new int[Program.MAX],
            programName = new int[Program.MAX], textureName = new int[Texture.MAX], framebufferName = {0};

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
            validated = initTexture(gl4);
        }
        if (validated) {
            validated = initFramebuffer(gl4);
        }
        if (validated) {
            validated = initVertexArray(gl4);
        }

        return validated;
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        // Create program
        if (validated) {

            ShaderProgram[] shaderPrograms = new ShaderProgram[Program.MAX];
            shaderPrograms[Program.PROG] = new ShaderProgram();
            shaderPrograms[Program.FUNC] = new ShaderProgram();

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADER_SOURCE, "vert", null, true);
            ShaderCode fragShaderCodeFunc = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADER_SOURCE_FUNC, "frag", null, true);
            ShaderCode fragShaderCodeProg = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADER_SOURCE_PROG, "frag", null, true);
            ShaderCode libShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADER_LIBRARY, "frag", null, true);

            shaderPrograms[Program.PROG].init(gl4);
            programName[Program.PROG] = shaderPrograms[Program.PROG].program();

            gl4.glProgramParameteri(programName[Program.PROG], GL_PROGRAM_SEPARABLE, GL_TRUE);

            shaderPrograms[Program.PROG].add(vertShaderCode);
            shaderPrograms[Program.PROG].add(libShaderCode);
            shaderPrograms[Program.PROG].add(fragShaderCodeProg);
            shaderPrograms[Program.PROG].link(gl4, System.out);

            shaderPrograms[Program.FUNC].init(gl4);
            programName[Program.FUNC] = shaderPrograms[Program.FUNC].program();

            gl4.glProgramParameteri(programName[Program.FUNC], GL_PROGRAM_SEPARABLE, GL_TRUE);

            shaderPrograms[Program.FUNC].add(vertShaderCode);
            shaderPrograms[Program.FUNC].add(fragShaderCodeFunc);
            shaderPrograms[Program.FUNC].link(gl4, System.out);
        }

        if (validated) {

            gl4.glGenProgramPipelines(Program.MAX, pipelineName, 0);
            gl4.glUseProgramStages(pipelineName[Program.PROG],
                    GL_VERTEX_SHADER_BIT | GL_FRAGMENT_SHADER_BIT, programName[Program.PROG]);
            gl4.glUseProgramStages(pipelineName[Program.FUNC],
                    GL_VERTEX_SHADER_BIT | GL_FRAGMENT_SHADER_BIT, programName[Program.FUNC]);
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

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
        gl4.glBufferData(GL_UNIFORM_BUFFER, Mat4.SIZE, null, GL_DYNAMIC_DRAW);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return true;
    }

    private boolean initTexture(GL4 gl4) {

        try {
            jgli.Texture2d texture = new Texture2d(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE));
            assert (!texture.empty());
            jgli.Gl.Format format = jgli.Gl.translate(texture.format());

            gl4.glGenTextures(Texture.MAX, textureName, 0);

            gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.DIFFUSE]);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 4);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, texture.levels() - 1);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            gl4.glTexStorage2D(GL_TEXTURE_2D, texture.levels(), format.internal.value,
                    texture.dimensions(0)[0], texture.dimensions(0)[1]);

            for (int level = 0; level < texture.levels(); ++level) {
                gl4.glTexSubImage2D(GL_TEXTURE_2D, level,
                        0, 0,
                        texture.dimensions(level)[0], texture.dimensions(level)[1],
                        format.external.value, format.type.value,
                        texture.data(level));
            }

            if (texture.levels() == 1) {
                gl4.glGenerateMipmap(GL_TEXTURE_2D);
            }

            gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.COLORBUFFER]);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            gl4.glTexStorage2D(GL_TEXTURE_2D, 1, GL_RGBA8, (int) (windowSize.x * FRAMEBUFFER_SCALE),
                    (int) (windowSize.y * FRAMEBUFFER_SCALE));

        } catch (IOException ex) {
            Logger.getLogger(Gl_420_sampler_fetch.class.getName()).log(Level.SEVERE, null, ex);
        }
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
        }
        gl4.glBindVertexArray(0);

        return true;
    }

    private boolean initFramebuffer(GL4 gl4) {

        gl4.glGenFramebuffers(1, framebufferName, 0);
        gl4.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[0]);
        gl4.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, textureName[Texture.COLORBUFFER], 0);
        gl4.glDrawBuffer(GL_COLOR_ATTACHMENT0);

        if (!isFramebufferComplete(gl4, framebufferName[0])) {
            return false;
        }

        gl4.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        Vec2i framebufferSize = new Vec2i((int) (windowSize.x * FRAMEBUFFER_SCALE),
                (int) (windowSize.y * FRAMEBUFFER_SCALE));

        {
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
            ByteBuffer pointer = gl4.glMapBufferRange(GL_UNIFORM_BUFFER, 0, Mat4.SIZE,
                    GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, windowSize.x * 0.5f / windowSize.y, 0.1f, 1000.0f);
            Mat4 model = new Mat4(1.0f);

            pointer.asFloatBuffer().put(projection.mul(viewMat4()).mul(model.scale(new Vec3(4.f))).toFa_());

            // Make sure the uniform buffer is uploaded
            gl4.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        gl4.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[0]);
        gl4.glViewportIndexedf(0, 0, 0, framebufferSize.x, framebufferSize.y);
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 0.5f, 0.0f, 1.0f}, 0);

        gl4.glActiveTexture(GL_TEXTURE0);
        gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.DIFFUSE]);
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.TRANSFORM]);
        gl4.glBindVertexArray(vertexArrayName[0]);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT]);

        gl4.glBindProgramPipeline(pipelineName[Program.PROG]);
        gl4.glViewportIndexedf(0, framebufferSize.x * 0.5f * 0.0f, 0, framebufferSize.x * 0.5f, framebufferSize.y);
        gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        gl4.glDrawElementsInstancedBaseVertexBaseInstance(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0, 0);

        gl4.glBindProgramPipeline(pipelineName[Program.FUNC]);
        gl4.glViewportIndexedf(0, framebufferSize.x * 0.5f * 1.0f, 0, framebufferSize.x * 0.5f, framebufferSize.y);
        gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        gl4.glDrawElementsInstancedBaseVertexBaseInstance(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0, 0);

        gl4.glBindFramebuffer(GL_READ_FRAMEBUFFER, framebufferName[0]);
        gl4.glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
        gl4.glBlitFramebuffer(
                0, 0, framebufferSize.x, framebufferSize.y,
                0, 0, windowSize.x, windowSize.y,
                GL_COLOR_BUFFER_BIT, GL_NEAREST);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteBuffers(Buffer.MAX, bufferName, 0);
        gl4.glDeleteTextures(Texture.MAX, textureName, 0);
        gl4.glDeleteVertexArrays(1, vertexArrayName, 0);
        gl4.glDeleteProgram(programName[Program.FUNC]);
        gl4.glDeleteProgram(programName[Program.PROG]);
        gl4.glDeleteProgramPipelines(Program.MAX, pipelineName, 0);

        return true;
    }
}
