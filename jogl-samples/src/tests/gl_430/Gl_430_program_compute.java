/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_430;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL3ES3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
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
import jglm.Vec2;
import jglm.Vec2i;

/**
 *
 * @author elect
 */
public class Gl_430_program_compute extends Test {

    public static void main(String[] args) {
        Gl_430_program_compute gl_430_program_compute = new Gl_430_program_compute();
    }

    public Gl_430_program_compute() {
        super("gl-430-program-compute", Profile.CORE, 4, 3, new Vec2i(640, 480),
                new Vec2(-(float) Math.PI * 0.2f, -(float) Math.PI * 0.2f));
    }

    private final String SHADERS_SOURCE = "program-compute";
    private final String SHADERS_ROOT = "src/data/gl_430";
    private final String TEXTURE_DIFFUSE = "kueken7_rgba8_srgb.dds";

    private int vertexCount = 8;
    private int vertexSize = vertexCount * 3 * 4 * Float.BYTES;
    private float[] vertexData = {
        -1.0f, -1.0f, 0.0f, 1.0f,/**/ 0.0f, 1.0f, 0.0f, 0.0f,/**/ 1.0f, 0.0f, 0.0f, 1.0f,
        +1.0f, -1.0f, 0.0f, 1.0f,/**/ 1.0f, 1.0f, 0.0f, 0.0f,/**/ 1.0f, 1.0f, 0.0f, 1.0f,
        +1.0f, +1.0f, 0.0f, 1.0f,/**/ 1.0f, 0.0f, 0.0f, 0.0f,/**/ 0.0f, 1.0f, 0.0f, 1.0f,
        -1.0f, +1.0f, 0.0f, 1.0f,/**/ 0.0f, 0.0f, 0.0f, 0.0f,/**/ 0.0f, 0.0f, 1.0f, 1.0f,
        -1.0f, -1.0f, 0.0f, 1.0f,/**/ 0.0f, 1.0f, 0.0f, 0.0f,/**/ 1.0f, 0.5f, 0.5f, 1.0f,
        +1.0f, -1.0f, 0.0f, 1.0f,/**/ 1.0f, 1.0f, 0.0f, 0.0f,/**/ 1.0f, 1.0f, 0.5f, 1.0f,
        +1.0f, +1.0f, 0.0f, 1.0f,/**/ 1.0f, 0.0f, 0.0f, 0.0f,/**/ 0.5f, 1.0f, 0.0f, 1.0f,
        -1.0f, +1.0f, 0.0f, 1.0f,/**/ 0.0f, 0.0f, 0.0f, 0.0f,/**/ 0.5f, 0.5f, 1.0f, 1.0f};

    private int elementCount = 6;
    private int elementSize = elementCount * Short.BYTES;
    private short[] elementData = {
        0, 1, 2,
        2, 3, 0};

    private enum Program {
        GRAPHICS,
        COMPUTE,
        MAX
    }

    private enum Buffer {
        ELEMENT,
        INPUT,
        OUTPUT,
        TRANSFORM,
        MAX
    }

    private enum Semantics {
        INPUT,
        OUTPUT
    }

    private int[] textureName = {0}, vertexArrayName = {0}, pipelineName = new int[Program.MAX.ordinal()],
            programName = new int[Program.MAX.ordinal()], bufferName = new int[Buffer.MAX.ordinal()];
    private float[] projection = new float[16], model = new float[16];

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;
        validated = validated && checkExtension(gl4, "GL_ARB_compute_shader");

        logImplementationDependentLimit(gl4, GL_MAX_COMPUTE_UNIFORM_BLOCKS,
                "GL_MAX_COMPUTE_UNIFORM_BLOCKS");
        logImplementationDependentLimit(gl4, GL_MAX_COMPUTE_TEXTURE_IMAGE_UNITS,
                "GL_MAX_COMPUTE_TEXTURE_IMAGE_UNITS");
        logImplementationDependentLimit(gl4, GL_MAX_COMPUTE_IMAGE_UNIFORMS,
                "GL_MAX_COMPUTE_IMAGE_UNIFORMS");
        logImplementationDependentLimit(gl4, GL_MAX_COMPUTE_SHARED_MEMORY_SIZE,
                "GL_MAX_COMPUTE_SHARED_MEMORY_SIZE");
        logImplementationDependentLimit(gl4, GL_MAX_COMPUTE_UNIFORM_COMPONENTS,
                "GL_MAX_COMPUTE_UNIFORM_COMPONENTS");
        logImplementationDependentLimit(gl4, GL_MAX_COMPUTE_ATOMIC_COUNTER_BUFFERS,
                "GL_MAX_COMPUTE_ATOMIC_COUNTER_BUFFERS");
        logImplementationDependentLimit(gl4, GL_MAX_COMPUTE_ATOMIC_COUNTERS,
                "GL_MAX_COMPUTE_ATOMIC_COUNTERS");
        logImplementationDependentLimit(gl4, GL_MAX_COMBINED_COMPUTE_UNIFORM_COMPONENTS,
                "GL_MAX_COMBINED_COMPUTE_UNIFORM_COMPONENTS");
        logImplementationDependentLimit(gl4, GL_MAX_COMPUTE_WORK_GROUP_INVOCATIONS,
                "GL_MAX_COMPUTE_WORK_GROUP_INVOCATIONS");

        //this->logImplementationDependentLimit(GL_MAX_COMPUTE_WORK_GROUP_COUNT, "GL_MAX_COMPUTE_WORK_GROUP_COUNT");
        //this->logImplementationDependentLimit(GL_MAX_COMPUTE_WORK_GROUP_SIZE, "GL_MAX_COMPUTE_WORK_GROUP_SIZE");
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

        return validated;
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);
            ShaderCode compShaderCode = ShaderCode.create(gl4, GL_COMPUTE_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "comp", null, true);

            shaderProgram.init(gl4);
            programName[Program.GRAPHICS.ordinal()] = shaderProgram.program();

            gl4.glProgramParameteri(programName[Program.GRAPHICS.ordinal()], GL_PROGRAM_SEPARABLE, GL_TRUE);

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);
            shaderProgram.link(gl4, System.out);

            shaderProgram = new ShaderProgram();
            shaderProgram.init(gl4);
            programName[Program.COMPUTE.ordinal()] = shaderProgram.program();
            gl4.glProgramParameteri(programName[Program.COMPUTE.ordinal()], GL_PROGRAM_SEPARABLE, GL_TRUE);
            shaderProgram.add(compShaderCode);
            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            gl4.glGenProgramPipelines(Program.MAX.ordinal(), pipelineName, 0);
            gl4.glUseProgramStages(pipelineName[Program.GRAPHICS.ordinal()], GL_VERTEX_SHADER_BIT
                    | GL_FRAGMENT_SHADER_BIT, programName[Program.GRAPHICS.ordinal()]);
            gl4.glUseProgramStages(pipelineName[Program.COMPUTE.ordinal()], GL_COMPUTE_SHADER_BIT,
                    programName[Program.COMPUTE.ordinal()]);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        boolean validated = true;

        gl4.glGenBuffers(Buffer.MAX.ordinal(), bufferName, 0);

        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT.ordinal()]);
        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(elementBuffer);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.INPUT.ordinal()]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl4.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer.rewind(), GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.OUTPUT.ordinal()]);
        vertexBuffer.put(vertexData);
        gl4.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer.rewind(), GL_STATIC_COPY);
        BufferUtils.destroyDirectBuffer(vertexBuffer);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        int[] uniformBufferOffset = {0};
        gl4.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset, 0);
        int uniformBlockSize = Math.max(projection.length * Float.BYTES, uniformBufferOffset[0]);

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM.ordinal()]);
        gl4.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_DYNAMIC_DRAW);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return validated;
    }

    private boolean initTexture(GL4 gl4) {

        try {
            jgli.Texture2d texture = new Texture2d(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE));
            assert (!texture.empty());
            jgli.Gl.Format format = jgli.Gl.translate(texture.format());

            gl4.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

            gl4.glGenTextures(1, textureName, 0);
            gl4.glActiveTexture(GL_TEXTURE0);
            gl4.glBindTexture(GL_TEXTURE_2D, textureName[0]);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_R, GL_RED);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_G, GL_GREEN);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_B, GL_BLUE);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_A, GL_ALPHA);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, texture.levels() - 1);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            gl4.glTexStorage2D(GL_TEXTURE_2D, texture.levels(), format.internal.value,
                    texture.dimensions()[0], texture.dimensions()[1]);
            for (int level = 0; level < texture.levels(); ++level) {
                gl4.glTexSubImage2D(GL_TEXTURE_2D, level,
                        0, 0,
                        texture.dimensions(level)[0], texture.dimensions(level)[1],
                        format.external.value, format.type.value,
                        texture.data(level));
            }

            gl4.glPixelStorei(GL_UNPACK_ALIGNMENT, 4);

        } catch (IOException ex) {
            Logger.getLogger(Gl_430_program_compute.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(1, vertexArrayName, 0);
        gl4.glBindVertexArray(vertexArrayName[0]);
        {
            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT.ordinal()]);
        }
        gl4.glBindVertexArray(0);

        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        {
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM.ordinal()]);
            ByteBuffer pointer = gl4.glMapBufferRange(GL_UNIFORM_BUFFER, 0, projection.length * Float.BYTES,
                    GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            FloatUtil.makePerspective(projection, 0, true, (float) Math.PI * 0.25f,
                    (float) windowSize.x / windowSize.y, 0.1f, 100.0f);
            FloatUtil.makeIdentity(model);
            FloatUtil.multMatrix(projection, view());
            FloatUtil.multMatrix(projection, model);

            pointer.asFloatBuffer().put(projection).rewind();

            // Make sure the uniform buffer is uploaded
            gl4.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        gl4.glBindProgramPipeline(pipelineName[Program.COMPUTE.ordinal()]);
        gl4.glBindBufferBase(GL_SHADER_STORAGE_BUFFER, Semantics.INPUT.ordinal(), bufferName[Buffer.INPUT.ordinal()]);
        gl4.glBindBufferBase(GL_SHADER_STORAGE_BUFFER, Semantics.OUTPUT.ordinal(), bufferName[Buffer.OUTPUT.ordinal()]);
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.TRANSFORM.ordinal()]);
        gl4.glDispatchCompute(vertexCount, 1, 1);

        gl4.glViewportIndexedf(0, 0, 0, windowSize.x, windowSize.y);
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, 0);

        gl4.glBindProgramPipeline(pipelineName[Program.GRAPHICS.ordinal()]);
        gl4.glActiveTexture(GL_TEXTURE0);
        gl4.glBindTexture(GL_TEXTURE_2D, textureName[0]);
        gl4.glBindVertexArray(vertexArrayName[0]);
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.TRANSFORM.ordinal()]);
        gl4.glBindBufferBase(GL_SHADER_STORAGE_BUFFER, Semantics.INPUT.ordinal(), bufferName[Buffer.OUTPUT.ordinal()]);

        gl4.glDrawElementsInstancedBaseVertexBaseInstance(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0, 0);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteProgramPipelines(Program.MAX.ordinal(), pipelineName, 0);
        gl4.glDeleteProgram(programName[Program.GRAPHICS.ordinal()]);
        gl4.glDeleteProgram(programName[Program.COMPUTE.ordinal()]);
        gl4.glDeleteBuffers(Buffer.MAX.ordinal(), bufferName, 0);
        gl4.glDeleteTextures(1, textureName, 0);
        gl4.glDeleteVertexArrays(1, vertexArrayName, 0);

        return true;
    }
}
