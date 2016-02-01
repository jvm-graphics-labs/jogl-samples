/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_430;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2ES3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.BufferUtils;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import jglm.Vec2;

/**
 *
 * @author GBarbieri
 */
public class Gl_430_atomic_counter extends Test {

    public static void main(String[] args) {
        Gl_430_atomic_counter gl_430_atomic_counter = new Gl_430_atomic_counter();
    }

    public Gl_430_atomic_counter() {
        super("gl-430-atomic-counter", Profile.CORE, 4, 3,
                new Vec2((float) Math.PI * 0.2f, (float) Math.PI * 0.2f));
    }

    private final String SHADERS_SOURCE = "atomic-counter";
    private final String SHADERS_ROOT = "src/data/gl_430";

    private int vertexCount = 4;
    private int vertexSize = vertexCount * 4 * Float.BYTES;
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

    private enum Buffer {
        VERTEX,
        ELEMENT,
        TRANSFORM,
        ATOMIC_COUNTER,
        MAX
    }

    private int[] pipelineName = {0}, vertexArrayName = {0}, bufferName = new int[Buffer.MAX.ordinal()];
    private int programName;
    private float[] projection = new float[16], model = new float[16], mvp = new float[16];

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;
        validated = validated && checkExtension(gl4, "GL_ARB_clear_buffer_object");

        if (validated) {
            validated = initBuffer(gl4);
        }
        if (validated) {
            validated = initProgram(gl4);
        }
        if (validated) {
            validated = initVertexArray(gl4);
        }

        return validated;
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        gl4.glGenProgramPipelines(1, pipelineName, 0);

        // Create program
        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

            shaderProgram.init(gl4);
            programName = shaderProgram.program();
            gl4.glProgramParameteri(programName, GL_PROGRAM_SEPARABLE, GL_TRUE);
            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);
            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            gl4.glUseProgramStages(pipelineName[0], GL_VERTEX_SHADER_BIT | GL_FRAGMENT_SHADER_BIT, programName);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        boolean validated = true;

        int[] maxVertexAtomicCounterBuffers = {0};
        int[] maxControlAtomicCounterBuffers = {0};
        int[] maxEvaluationAtomicCounterBuffers = {0};
        int[] maxGeometryAtomicCounterBuffers = {0};
        int[] maxFragmentAtomicCounterBuffers = {0};
        int[] maxCombinedAtomicCounterBuffers = {0};

        gl4.glGetIntegerv(GL_MAX_VERTEX_ATOMIC_COUNTER_BUFFERS, maxVertexAtomicCounterBuffers, 0);
        gl4.glGetIntegerv(GL_MAX_TESS_CONTROL_ATOMIC_COUNTER_BUFFERS, maxControlAtomicCounterBuffers, 0);
        gl4.glGetIntegerv(GL_MAX_TESS_EVALUATION_ATOMIC_COUNTER_BUFFERS, maxEvaluationAtomicCounterBuffers, 0);
        gl4.glGetIntegerv(GL_MAX_GEOMETRY_ATOMIC_COUNTER_BUFFERS, maxGeometryAtomicCounterBuffers, 0);
        gl4.glGetIntegerv(GL_MAX_FRAGMENT_ATOMIC_COUNTER_BUFFERS, maxFragmentAtomicCounterBuffers, 0);
        gl4.glGetIntegerv(GL_MAX_COMBINED_ATOMIC_COUNTER_BUFFERS, maxCombinedAtomicCounterBuffers, 0);

        gl4.glGenBuffers(Buffer.MAX.ordinal(), bufferName, 0);

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM.ordinal()]);
        gl4.glBufferData(GL_UNIFORM_BUFFER, mvp.length * Float.BYTES, null, GL_DYNAMIC_DRAW);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT.ordinal()]);
        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(elementBuffer);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl4.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(vertexBuffer);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ATOMIC_COUNTER_BUFFER, bufferName[Buffer.ATOMIC_COUNTER.ordinal()]);
        gl4.glBufferData(GL_ATOMIC_COUNTER_BUFFER, Integer.BYTES, null, GL_DYNAMIC_COPY);
        gl4.glBindBuffer(GL_ATOMIC_COUNTER_BUFFER, 0);

        return validated;
    }

    private boolean initVertexArray(GL4 gl4) {

        boolean validated = true;

        gl4.glGenVertexArrays(1, vertexArrayName, 0);
        gl4.glBindVertexArray(vertexArrayName[0]);
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

        return validated;
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        // Setup blending
        gl4.glEnable(GL_BLEND);
        gl4.glBlendEquation(GL_FUNC_ADD);
        gl4.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        {
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM.ordinal()]);
            ByteBuffer pointer = gl4.glMapBufferRange(
                    GL_UNIFORM_BUFFER, 0, mvp.length * Float.BYTES,
                    GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            FloatUtil.makePerspective(projection, 0, true, (float) Math.PI * 0.25f,
                    (float) windowSize.x / windowSize.y, 0.1f, 100.0f);
            FloatUtil.makeIdentity(model);
            FloatUtil.multMatrix(projection, view(), mvp);
            FloatUtil.multMatrix(mvp, model);

            for (float f : mvp) {
                pointer.putFloat(f);
            }
            pointer.rewind();

            gl4.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        IntBuffer data = IntBuffer.wrap(new int[]{0});
        gl4.glBindBuffer(GL_ATOMIC_COUNTER_BUFFER, bufferName[Buffer.ATOMIC_COUNTER.ordinal()]);
        gl4.glClearBufferSubData(GL_ATOMIC_COUNTER_BUFFER, GL_R8UI, 0, Integer.BYTES, GL_RGBA, GL_UNSIGNED_INT, data);

        gl4.glViewportIndexedf(0, 0, 0, windowSize.x, windowSize.y);
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{0.0f, 0.0f, 0.0f, 1.0f}, 0);

        gl4.glBindProgramPipeline(pipelineName[0]);
        gl4.glBindVertexArray(vertexArrayName[0]);
        gl4.glBindBufferBase(GL_ATOMIC_COUNTER_BUFFER, 0, bufferName[Buffer.ATOMIC_COUNTER.ordinal()]);
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.TRANSFORM.ordinal()]);

        gl4.glDrawElementsInstancedBaseVertexBaseInstance(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 5, 0, 0);

        return true;
    }
}
