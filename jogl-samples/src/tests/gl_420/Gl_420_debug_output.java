/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_420;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2ES3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.GlDebugOutput;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import jglm.Vec2;

/**
 *
 * @author GBarbieri
 */
public class Gl_420_debug_output extends Test {

    public static void main(String[] args) {
        Gl_420_debug_output gl_420_debug_output = new Gl_420_debug_output();
    }

    public Gl_420_debug_output() {
        /**
         * It's mandatory to enable glDebugOutput before GLContext creation!
         * Here through the boolean true.
         */
        super("gl-420-debug-output", Profile.CORE, 4, 2, new Vec2(0.25f, 0.25f), true);
    }

    private final String SHADERS_SOURCE = "debug-output";
    private final String SHADERS_ROOT = "src/data/gl_420";

    private final boolean useJoglUtils = true;

    private int vertexCount = 4;
    private int positionSize = vertexCount * 2 * Float.BYTES;
    private float[] positionData = {
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
        ELEMENT,
        VERTEX,
        TRANSFORM,
        MAX
    }

    private int[] bufferName = new int[Buffer.MAX.ordinal()], pipelineName = {0}, vertexArrayName = {0};
    private int programName;
    private float[] projection = new float[16], model = new float[16];

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;

        if (validated && checkExtension(gl4, "GL_ARB_debug_output")) {
            validated = initDebugOutput(gl4);
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

//        glWindow.getContext().addGLDebugListener(this);
        return validated;
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

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

            gl4.glGenProgramPipelines(1, pipelineName, 0);
            gl4.glBindProgramPipeline(pipelineName[0]);
            gl4.glUseProgramStages(pipelineName[0], GL_VERTEX_SHADER_BIT | GL_FRAGMENT_SHADER_BIT, programName);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(1, vertexArrayName, 0);
        gl4.glBindVertexArray(vertexArrayName[0]);
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 0, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);

            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT.ordinal()]);
        }
        gl4.glBindVertexArray(0);

        return true;
    }

    private boolean initBuffer(GL4 gl4) {

        gl4.glGenBuffers(Buffer.MAX.ordinal(), bufferName, 0);

        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT.ordinal()]);
        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);
        FloatBuffer positionBuffer = GLBuffers.newDirectFloatBuffer(positionData);
        gl4.glBufferData(GL_ARRAY_BUFFER, positionSize, positionBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM.ordinal()]);
        gl4.glBufferData(GL_UNIFORM_BUFFER, 16 * Float.BYTES, null, GL_DYNAMIC_DRAW);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return true;
    }

    private boolean initDebugOutput(GL4 gl4) {

        if (useJoglUtils) {
            glWindow.getContext().enableGLDebugMessage(true);
            glWindow.getContext().setGLDebugSynchronous(true);
        } else {
//        gl4.glDisable(GL_DEBUG_OUTPUT);
            gl4.glEnable(GL_DEBUG_OUTPUT);
            gl4.glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS);
        }
        // from the constructor.
        System.out.println("isGLDebugEnabled " + glWindow.getContext().isGLDebugEnabled());
        // from enableGLDebugMessage()
        System.out.println("isGLDebugMessageEnabled " + glWindow.getContext().isGLDebugMessageEnabled());
        // from setGLDebugSynchronous
        System.out.println("isGLDebugSynchronous " + glWindow.getContext().isGLDebugSynchronous());

        glWindow.getContext().addGLDebugListener(new GlDebugOutput());

        if (useJoglUtils) {
            glWindow.getContext().glDebugMessageControl(GL_DONT_CARE, GL_DONT_CARE, GL_DONT_CARE, 0, null, 0, true);
        } else {
            gl4.glDebugMessageControl(GL_DONT_CARE, GL_DONT_CARE, GL_DONT_CARE, 0, null, 0, true);
        }

        int[] messageId = {4};
        if (useJoglUtils) {
            glWindow.getContext().glDebugMessageControl(GL_DEBUG_SOURCE_APPLICATION, GL_DEBUG_TYPE_OTHER,
                    GL_DONT_CARE, 0, null, 0, true);
            glWindow.getContext().glDebugMessageControl(GL_DEBUG_SOURCE_APPLICATION, GL_DEBUG_TYPE_OTHER,
                    GL_DONT_CARE, 1, messageId, 0, false);
        } else {
            gl4.glDebugMessageControl(GL_DEBUG_SOURCE_APPLICATION, GL_DEBUG_TYPE_OTHER, GL_DONT_CARE, 0, null, 0, true);
            gl4.glDebugMessageControl(GL_DEBUG_SOURCE_APPLICATION, GL_DEBUG_TYPE_OTHER,
                    GL_DONT_CARE, 1, messageId, 0, false);
        }

        String message1 = "Message 1";
        if (useJoglUtils) {
            glWindow.getContext().glDebugMessageInsert(
                    GL_DEBUG_SOURCE_APPLICATION,
                    GL_DEBUG_TYPE_OTHER, 1,
                    GL_DEBUG_SEVERITY_MEDIUM,
                    message1);
        } else {
            gl4.glDebugMessageInsert(
                    GL_DEBUG_SOURCE_APPLICATION,
                    GL_DEBUG_TYPE_OTHER, 1,
                    GL_DEBUG_SEVERITY_MEDIUM,
                    message1.length(), message1);
        }
        String message2 = "Message 2";
        if (useJoglUtils) {
            glWindow.getContext().glDebugMessageInsert(
                    GL_DEBUG_SOURCE_THIRD_PARTY,
                    GL_DEBUG_TYPE_OTHER, 2,
                    GL_DEBUG_SEVERITY_MEDIUM,
                    message2);
        } else {
            gl4.glDebugMessageInsert(
                    GL_DEBUG_SOURCE_THIRD_PARTY,
                    GL_DEBUG_TYPE_OTHER, 2,
                    GL_DEBUG_SEVERITY_MEDIUM,
                    message2.length(), message2);
        }
        if (useJoglUtils) {
            glWindow.getContext().glDebugMessageInsert(
                    GL_DEBUG_SOURCE_APPLICATION,
                    GL_DEBUG_TYPE_OTHER, 2,
                    GL_DEBUG_SEVERITY_MEDIUM,
                    "Message 3");
        } else {
            gl4.glDebugMessageInsert(
                    GL_DEBUG_SOURCE_APPLICATION,
                    GL_DEBUG_TYPE_OTHER, 2,
                    GL_DEBUG_SEVERITY_MEDIUM,
                    -1, "Message 3");
        }
        if (useJoglUtils) {
            glWindow.getContext().glDebugMessageInsert(
                    GL_DEBUG_SOURCE_APPLICATION,
                    GL_DEBUG_TYPE_OTHER, messageId[0],
                    GL_DEBUG_SEVERITY_MEDIUM,
                    "Message 4");
        } else {
            gl4.glDebugMessageInsert(
                    GL_DEBUG_SOURCE_APPLICATION,
                    GL_DEBUG_TYPE_OTHER, messageId[0],
                    GL_DEBUG_SEVERITY_MEDIUM,
                    -1, "Message 4");
        }

        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;
        {
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM.ordinal()]);

            ByteBuffer pointer = gl4.glMapBufferRange(GL_UNIFORM_BUFFER,
                    0, 16 * Float.BYTES, GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            FloatUtil.makePerspective(projection, 0, true, (float) Math.PI * 0.25f, 4.0f / 3.0f, 0.1f, 100.0f);
            FloatUtil.makeIdentity(model);
            FloatUtil.multMatrix(projection, view());
            FloatUtil.multMatrix(projection, model);

            for (float f : projection) {
                pointer.putFloat(f);
            }
            pointer.rewind();

            gl4.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        gl4.glViewportIndexedf(0, 0, 0, windowSize.x, windowSize.y);
        boolean fail = true;
        gl4.glClearBufferfv(fail ? GL_UNIFORM_BUFFER : GL_COLOR, 0, new float[]{0.0f, 0.5f, 1.0f, 1.0f}, 0);

        gl4.glBindProgramPipeline(pipelineName[0]);
        gl4.glBindVertexArray(vertexArrayName[0]);
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.TRANSFORM.ordinal()]);

        gl4.glDrawElementsInstancedBaseVertexBaseInstance(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0, 0);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;

        gl4.glDeleteProgramPipelines(1, pipelineName, 0);
        gl4.glDeleteVertexArrays(1, vertexArrayName, 0);
        gl4.glDeleteBuffers(Buffer.MAX.ordinal(), bufferName, 0);
        gl4.glDeleteProgram(programName);

        return validated;
    }
}
