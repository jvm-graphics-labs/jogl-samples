/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_430;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL3ES3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import core.glm;
import dev.Mat4;
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

/**
 *
 * @author GBarbieri
 */
public class Gl_430_program_compute_image extends Test {

    public static void main(String[] args) {
        Gl_430_program_compute_image gl_430_program_compute_image = new Gl_430_program_compute_image();
    }

    public Gl_430_program_compute_image() {
        super("gl-430-program-compute-image", Profile.CORE, 4, 3);
    }

    private final String SHADERS_SOURCE = "program-compute-image";
    private final String SHADERS_ROOT = "src/data/gl_430";
    private final String TEXTURE_DIFFUSE = "kueken7_rgba8_srgb.dds";

    private int vertexCount = 8;

    private int positionSize = vertexCount * 4 * Float.BYTES;
    private float[] positionData = {
        -1.0f, -1.0f, 0.0f, 1.0f,
        +1.0f, -1.0f, 0.0f, 1.0f,
        +1.0f, +1.0f, 0.0f, 1.0f,
        -1.0f, +1.0f, 0.0f, 1.0f,
        -1.0f, -1.0f, 0.0f, 1.0f,
        +1.0f, -1.0f, 0.0f, 1.0f,
        +1.0f, +1.0f, 0.0f, 1.0f,
        -1.0f, +1.0f, 0.0f, 1.0f};

    private int texCoordSize = vertexCount * 4 * Float.BYTES;
    private float[] texCoordData = {
        0.0f, 1.0f, 0.0f, 0.0f,
        1.0f, 1.0f, 0.0f, 0.0f,
        1.0f, 0.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 0.0f, 0.0f,
        0.0f, 1.0f, 0.0f, 0.0f,
        1.0f, 1.0f, 0.0f, 0.0f,
        1.0f, 0.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 0.0f, 0.0f};

    private int colorSize = vertexCount * 4 * Float.BYTES;
    private float[] colorData = {
        1.0f, 0.0f, 0.0f, 1.0f,
        1.0f, 1.0f, 0.0f, 1.0f,
        0.0f, 1.0f, 0.0f, 1.0f,
        0.0f, 0.0f, 1.0f, 1.0f,
        1.0f, 0.5f, 0.5f, 1.0f,
        1.0f, 1.0f, 0.5f, 1.0f,
        0.5f, 1.0f, 0.0f, 1.0f,
        0.5f, 0.5f, 1.0f, 1.0f};

    private int elementCount = 6;
    private int elementSize = elementCount * Short.BYTES;
    private short[] elementData = {
        0, 1, 2,
        2, 3, 0};

    private class Program {

        public static final int GRAPHICS = 0;
        public static final int COMPUTE = 1;
        public static final int MAX = 2;
    }

    private class Buffer {

        public static final int ELEMENT = 0;
        public static final int TRANSFORM = 1;
        public static final int POSITION_INPUT = 2;
        public static final int TEXCOORD_INPUT = 3;
        public static final int COLOR_INPUT = 4;
        public static final int POSITION_OUTPUT = 5;
        public static final int TEXCOORD_OUTPUT = 6;
        public static final int COLOR_OUTPUT = 7;
        public static final int MAX = 8;
    }

    private class Texture {

        public static final int DIFFUSE = 0;
        public static final int POSITION_INPUT = 1;
        public static final int TEXCOORD_INPUT = 2;
        public static final int COLOR_INPUT = 3;
        public static final int POSITION_OUTPUT = 4;
        public static final int TEXCOORD_OUTPUT = 5;
        public static final int COLOR_OUTPUT = 6;
        public static final int MAX = 7;
    }

    private class Image {

        public static final int POSITION_INPUT = 0;
        public static final int TEXCOORD_INPUT = 1;
        public static final int COLOR_INPUT = 2;
        public static final int POSITION_OUTPUT = 3;
        public static final int TEXCOORD_OUTPUT = 4;
        public static final int COLOR_OUTPUT = 5;
    }

    private class Semantics {

        public static final int INPUT = 0;
        public static final int OUTPUT = 1;
    }

    private int[] vertexArrayName = {0}, pipelineName = new int[Program.MAX], programName = new int[Program.MAX],
            bufferName = new int[Buffer.MAX], textureName = new int[Texture.MAX];

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        logImplementationDependentLimit(gl4, GL_MAX_COMPUTE_UNIFORM_BLOCKS, "GL_MAX_COMPUTE_UNIFORM_BLOCKS");
        logImplementationDependentLimit(gl4, GL_MAX_COMPUTE_TEXTURE_IMAGE_UNITS, "GL_MAX_COMPUTE_TEXTURE_IMAGE_UNITS");
        logImplementationDependentLimit(gl4, GL_MAX_COMPUTE_IMAGE_UNIFORMS, "GL_MAX_COMPUTE_IMAGE_UNIFORMS");
        logImplementationDependentLimit(gl4, GL_MAX_COMPUTE_SHARED_MEMORY_SIZE, "GL_MAX_COMPUTE_SHARED_MEMORY_SIZE");
        logImplementationDependentLimit(gl4, GL_MAX_COMPUTE_UNIFORM_COMPONENTS, "GL_MAX_COMPUTE_UNIFORM_COMPONENTS");
        logImplementationDependentLimit(gl4, GL_MAX_COMPUTE_ATOMIC_COUNTER_BUFFERS, "GL_MAX_COMPUTE_ATOMIC_COUNTER_BUFFERS");
        logImplementationDependentLimit(gl4, GL_MAX_COMPUTE_ATOMIC_COUNTERS, "GL_MAX_COMPUTE_ATOMIC_COUNTERS");
        logImplementationDependentLimit(gl4, GL_MAX_COMBINED_COMPUTE_UNIFORM_COMPONENTS, "GL_MAX_COMBINED_COMPUTE_UNIFORM_COMPONENTS");
        logImplementationDependentLimit(gl4, GL_MAX_COMPUTE_WORK_GROUP_INVOCATIONS, "GL_MAX_COMPUTE_WORK_GROUP_INVOCATIONS");

        logImplementationDependentLimit(gl4, GL_TEXTURE_BUFFER_OFFSET_ALIGNMENT, "GL_TEXTURE_BUFFER_OFFSET_ALIGNMENT");

        boolean validated = checkExtension(gl4, "GL_ARB_compute_shader");

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
            validated = initVertexArray(gl4);
        }

        return validated;
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        gl4.glGenProgramPipelines(Program.MAX, pipelineName, 0);

        // Create program
        if (validated) {

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);
            ShaderCode compShaderCode = ShaderCode.create(gl4, GL_COMPUTE_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "comp", null, true);

            if (validated) {

                ShaderProgram shaderProgram = new ShaderProgram();
                shaderProgram.init(gl4);

                programName[Program.GRAPHICS] = shaderProgram.program();

                gl4.glProgramParameteri(programName[Program.GRAPHICS], GL_PROGRAM_SEPARABLE, GL_TRUE);

                shaderProgram.add(vertShaderCode);
                shaderProgram.add(fragShaderCode);

                shaderProgram.link(gl4, System.out);
            }

            if (validated) {

                ShaderProgram shaderProgram = new ShaderProgram();
                shaderProgram.init(gl4);

                programName[Program.COMPUTE] = shaderProgram.program();

                gl4.glProgramParameteri(programName[Program.COMPUTE], GL_PROGRAM_SEPARABLE, GL_TRUE);

                shaderProgram.add(compShaderCode);

                shaderProgram.link(gl4, System.out);
            }
        }

        if (validated) {

            gl4.glUseProgramStages(pipelineName[Program.GRAPHICS], GL_VERTEX_SHADER_BIT
                    | GL_FRAGMENT_SHADER_BIT, programName[Program.GRAPHICS]);
            gl4.glUseProgramStages(pipelineName[Program.COMPUTE], GL_COMPUTE_SHADER_BIT,
                    programName[Program.COMPUTE]);
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

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.POSITION_INPUT]);
        gl4.glBufferData(GL_ARRAY_BUFFER, positionSize * 2, null, GL_STATIC_DRAW);
        FloatBuffer positionBuffer = GLBuffers.newDirectFloatBuffer(positionData);
        gl4.glBufferSubData(GL_ARRAY_BUFFER, 0, positionSize, positionBuffer);
        BufferUtils.destroyDirectBuffer(positionBuffer);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.TEXCOORD_INPUT]);
        FloatBuffer texCoordBuffer = GLBuffers.newDirectFloatBuffer(texCoordData);
        gl4.glBufferData(GL_ARRAY_BUFFER, texCoordSize * 2, texCoordBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(texCoordBuffer);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.COLOR_INPUT]);
        FloatBuffer colorBuffer = GLBuffers.newDirectFloatBuffer(colorData);
        gl4.glBufferData(GL_ARRAY_BUFFER, colorSize * 2, colorBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(colorBuffer);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.POSITION_OUTPUT]);
        gl4.glBufferData(GL_ARRAY_BUFFER, positionSize * 2, null, GL_STATIC_COPY);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.TEXCOORD_OUTPUT]);
        gl4.glBufferData(GL_ARRAY_BUFFER, texCoordSize * 2, null, GL_STATIC_COPY);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.COLOR_OUTPUT]);
        gl4.glBufferData(GL_ARRAY_BUFFER, colorSize * 2, null, GL_STATIC_COPY);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        int[] uniformBufferOffset = {0};
        gl4.glGetIntegerv(
                GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT,
                uniformBufferOffset, 0);
        int uniformBlockSize = Math.max(Mat4.SIZE, uniformBufferOffset[0]);

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
        gl4.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_DYNAMIC_DRAW);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return checkError(gl4, "initBuffer");
    }

    private boolean initTexture(GL4 gl4) {

        try {
            jgli.Texture2d texture = new Texture2d(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE));
            assert (!texture.empty());
            jgli.Gl.Format format = jgli.Gl.translate(texture.format());

            gl4.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

            gl4.glGenTextures(Texture.MAX, textureName, 0);
            gl4.glActiveTexture(GL_TEXTURE0);
            gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.DIFFUSE]);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_R, GL_RED);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_G, GL_GREEN);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_B, GL_BLUE);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_A, GL_ALPHA);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, texture.levels() - 1);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            gl4.glTexStorage2D(GL_TEXTURE_2D, texture.levels(), format.internal.value,
                    texture.dimensions(0)[0], texture.dimensions(0)[1]);
            for (int level = 0; level < texture.levels(); ++level) {
                gl4.glTexSubImage2D(GL_TEXTURE_2D, level,
                        0, 0,
                        texture.dimensions(level)[0], texture.dimensions(level)[1],
                        format.external.value, format.type.value,
                        texture.data(level));
            }

            gl4.glPixelStorei(GL_UNPACK_ALIGNMENT, 4);

            int[] textureBufferOffset = {0};
            gl4.glGetIntegerv(GL_TEXTURE_BUFFER_OFFSET_ALIGNMENT, textureBufferOffset, 0);
            int textureBufferRange = Math.max(positionSize, textureBufferOffset[0]);

            gl4.glBindTexture(GL_TEXTURE_BUFFER, textureName[Texture.POSITION_INPUT]);
            gl4.glTexBufferRange(GL_TEXTURE_BUFFER, GL_RGBA32F, bufferName[Buffer.POSITION_INPUT], 0,
                    textureBufferRange);
            gl4.glBindTexture(GL_TEXTURE_BUFFER, textureName[Texture.TEXCOORD_INPUT]);
            gl4.glTexBufferRange(GL_TEXTURE_BUFFER, GL_RGBA32F, bufferName[Buffer.TEXCOORD_INPUT], 0,
                    textureBufferRange);
            gl4.glBindTexture(GL_TEXTURE_BUFFER, textureName[Texture.COLOR_INPUT]);
            gl4.glTexBufferRange(GL_TEXTURE_BUFFER, GL_RGBA32F, bufferName[Buffer.COLOR_INPUT], 0,
                    textureBufferRange);
            gl4.glBindTexture(GL_TEXTURE_BUFFER, textureName[Texture.POSITION_OUTPUT]);
            gl4.glTexBufferRange(GL_TEXTURE_BUFFER, GL_RGBA32F, bufferName[Buffer.POSITION_OUTPUT], 0,
                    textureBufferRange);
            gl4.glBindTexture(GL_TEXTURE_BUFFER, textureName[Texture.TEXCOORD_OUTPUT]);
            gl4.glTexBufferRange(GL_TEXTURE_BUFFER, GL_RGBA32F, bufferName[Buffer.TEXCOORD_OUTPUT], 0,
                    textureBufferRange);
            gl4.glBindTexture(GL_TEXTURE_BUFFER, textureName[Texture.COLOR_OUTPUT]);
            gl4.glTexBufferRange(GL_TEXTURE_BUFFER, GL_RGBA32F, bufferName[Buffer.COLOR_OUTPUT], 0,
                    textureBufferRange);
            gl4.glBindTexture(GL_TEXTURE_BUFFER, 0);

        } catch (IOException ex) {
            Logger.getLogger(Gl_430_program_compute_image.class.getName()).log(Level.SEVERE, null, ex);
        }
        return checkError(gl4, "initTexture");
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(1, vertexArrayName, 0);
        gl4.glBindVertexArray(vertexArrayName[0]);
        {
            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT]);
        }
        gl4.glBindVertexArray(0);

        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        {
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
            ByteBuffer pointer = gl4.glMapBufferRange(
                    GL_UNIFORM_BUFFER, 0, Mat4.SIZE,
                    GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            Mat4 projection = glm.perspectiveFov_((float) Math.PI * 0.25f, windowSize.x, windowSize.y, 0.1f, 100.0f);
            Mat4 model = new Mat4(1.0f);

            pointer.asFloatBuffer().put(projection.mul(viewMat4()).mul(model).toFa_());

            // Make sure the uniform buffer is uploaded
            gl4.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        gl4.glBindProgramPipeline(pipelineName[Program.COMPUTE]);
        gl4.glBindImageTexture(Image.POSITION_INPUT, textureName[Texture.POSITION_INPUT],
                0, false, 0, GL_READ_ONLY, GL_RGBA32F);
        gl4.glBindImageTexture(Image.TEXCOORD_INPUT, textureName[Texture.TEXCOORD_INPUT],
                0, false, 0, GL_READ_ONLY, GL_RGBA32F);
        gl4.glBindImageTexture(Image.COLOR_INPUT, textureName[Texture.COLOR_INPUT],
                0, false, 0, GL_READ_ONLY, GL_RGBA32F);
        gl4.glBindImageTexture(Image.POSITION_OUTPUT, textureName[Texture.POSITION_OUTPUT],
                0, false, 0, GL_WRITE_ONLY, GL_RGBA32F);
        gl4.glBindImageTexture(Image.TEXCOORD_OUTPUT, textureName[Texture.TEXCOORD_OUTPUT],
                0, false, 0, GL_WRITE_ONLY, GL_RGBA32F);
        gl4.glBindImageTexture(Image.COLOR_OUTPUT, textureName[Texture.COLOR_OUTPUT],
                0, false, 0, GL_WRITE_ONLY, GL_RGBA32F);

        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.TRANSFORM]);
        gl4.glDispatchCompute(vertexCount, 1, 1);

        gl4.glViewportIndexedf(0, 0, 0, windowSize.x, windowSize.y);
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, 0);

        gl4.glBindProgramPipeline(pipelineName[Program.GRAPHICS]);
        gl4.glActiveTexture(GL_TEXTURE0 + Semantic.Sampler.DIFFUSE);
        gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.DIFFUSE]);
        gl4.glActiveTexture(GL_TEXTURE0 + Semantic.Sampler.POSITION);
        gl4.glBindTexture(GL_TEXTURE_BUFFER, textureName[Texture.POSITION_OUTPUT]);
        gl4.glActiveTexture(GL_TEXTURE0 + Semantic.Sampler.TEXCOORD);
        gl4.glBindTexture(GL_TEXTURE_BUFFER, textureName[Texture.TEXCOORD_OUTPUT]);
        gl4.glActiveTexture(GL_TEXTURE0 + Semantic.Sampler.COLOR);
        gl4.glBindTexture(GL_TEXTURE_BUFFER, textureName[Texture.COLOR_OUTPUT]);

        gl4.glBindVertexArray(vertexArrayName[0]);
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.TRANSFORM]);

        gl4.glDrawElementsInstancedBaseVertexBaseInstance(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0, 0);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteProgramPipelines(Program.MAX, pipelineName, 0);
        gl4.glDeleteProgram(programName[Program.GRAPHICS]);
        gl4.glDeleteProgram(programName[Program.COMPUTE]);
        gl4.glDeleteBuffers(Buffer.MAX, bufferName, 0);
        gl4.glDeleteTextures(Texture.MAX, textureName, 0);
        gl4.glDeleteVertexArrays(1, vertexArrayName, 0);

        return true;
    }
}
