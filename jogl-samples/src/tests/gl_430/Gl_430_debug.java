/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_430;

import static com.jogamp.opengl.GL2ES3.*;
import com.jogamp.opengl.GL4;
import static com.jogamp.opengl.GLES2.GL_VERTEX_ARRAY;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.BufferUtils;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.io.IOException;
import static java.lang.Boolean.FALSE;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import jgli.Texture2d;

/**
 *
 * @author GBarbieri
 */
public class Gl_430_debug extends Test {

    public static void main(String[] args) {
        Gl_430_debug gl_430_debug = new Gl_430_debug();
    }

    public Gl_430_debug() {
        super("gl-430-debug", Profile.CORE, 4, 3);
    }

    private final String SHADERS_SOURCE = "debug";
    private final String SHADERS_ROOT = "src/data/gl_430";
    private final String TEXTURE_DIFFUSE = "kueken7_rgb8_unorm.dds";

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

    private class Program {

        public static final int VERTEX = 0;
        public static final int FRAGMENT = 1;
        public static final int MAX = 2;
    }

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int ELEMENT = 1;
        public static final int TRANSFORM = 2;
        public static final int MAX = 3;
    }

    private int[] pipelineName = {0}, vertexArrayName = {0}, textureName = {0},
            programName = new int[Program.MAX], bufferName = new int[Buffer.MAX];
    private float[] projection = new float[16], model = new float[16];

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        gl4.glGenProgramPipelines(1, pipelineName, 0);
        gl4.glObjectLabel(GL_PROGRAM_PIPELINE, pipelineName[0], -1, "Pipeline Program Object".getBytes(), 0);

        // Create program
        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

            shaderProgram.init(gl4);
            programName[Program.VERTEX] = shaderProgram.program();

            gl4.glObjectLabel(GL_PROGRAM, programName[Program.VERTEX], -1,
                    "Vertex Program object".getBytes(), 0);

            gl4.glProgramParameteri(programName[Program.VERTEX], GL_PROGRAM_SEPARABLE, GL_TRUE);

            shaderProgram.add(vertShaderCode);

            shaderProgram.link(gl4, System.out);

            shaderProgram = new ShaderProgram();

            shaderProgram.init(gl4);
            programName[Program.FRAGMENT] = shaderProgram.program();

            gl4.glObjectLabel(GL_PROGRAM, programName[Program.FRAGMENT], -1,
                    "Fragment Program object".getBytes(), 0);

            gl4.glProgramParameteri(programName[Program.FRAGMENT], GL_PROGRAM_SEPARABLE, GL_TRUE);

            shaderProgram.add(fragShaderCode);

            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            gl4.glUseProgramStages(pipelineName[0], GL_VERTEX_SHADER_BIT, programName[Program.VERTEX]);
            gl4.glUseProgramStages(pipelineName[0], GL_FRAGMENT_SHADER_BIT, programName[Program.FRAGMENT]);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        boolean validated = true;

        gl4.glGenBuffers(Buffer.MAX, bufferName, 0);

        gl4.glObjectLabel(GL_BUFFER, bufferName[Buffer.ELEMENT], -1,
                "Element Array Buffer object".getBytes(), 0);

        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT]);
        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(elementBuffer);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl4.glObjectLabel(GL_BUFFER, bufferName[Buffer.VERTEX], -1,
                "Array Buffer object".getBytes(), 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl4.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(vertexBuffer);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        int[] uniformBufferOffset = {0};
        gl4.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset, 0);
        int uniformBlockSize = Math.max(projection.length * Float.BYTES, uniformBufferOffset[0]);

        gl4.glObjectLabel(GL_BUFFER, bufferName[Buffer.TRANSFORM], -1,
                "Uniform Buffer object".getBytes(), 0);

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
        gl4.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_DYNAMIC_DRAW);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return validated;
    }

    private boolean initTexture(GL4 gl4) {

        boolean validated = true;

        try {
            jgli.Texture2d texture = new Texture2d(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE));

            gl4.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

            gl4.glGenTextures(1, textureName, 0);
            gl4.glActiveTexture(GL_TEXTURE0);
            gl4.glBindTexture(GL_TEXTURE_2D, textureName[0]);

            gl4.glObjectLabel(GL_TEXTURE, textureName[0], -1, "Texture object".getBytes(), 0);

            gl4.glDebugMessageInsert(GL_DEBUG_SOURCE_APPLICATION, GL_DEBUG_TYPE_MARKER,
                    1, GL_DEBUG_SEVERITY_NOTIFICATION, -1, "Throwing an error on glTexParameteri");

            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_R, GL_RED);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_G, GL_GREEN);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_B, GL_BLUE);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_A, GL_LINEAR); // Generates an error GL_LINEAR instead of GL_ALPHA

            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, texture.levels() - 1);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

            gl4.glTexStorage2D(GL_TEXTURE_2D, texture.levels(), GL_RGBA8,
                    texture.dimensions(0)[0], texture.dimensions(0)[1]);

            for (int level = 0; level < texture.levels(); ++level) {
                gl4.glTexSubImage2D(
                        GL_TEXTURE_2D,
                        level,
                        0, 0,
                        texture.dimensions(level)[0],
                        texture.dimensions(level)[1],
                        GL_BGR, GL_UNSIGNED_BYTE,
                        texture.data(level));
            }

            gl4.glPixelStorei(GL_UNPACK_ALIGNMENT, 4);

        } catch (IOException ex) {
            Logger.getLogger(Gl_430_debug.class.getName()).log(Level.SEVERE, null, ex);
        }
        return validated;
    }

    private boolean initVertexArray(GL4 gl4) {

        boolean validated = true;

        gl4.glGenVertexArrays(1, vertexArrayName, 0);
        gl4.glBindVertexArray(vertexArrayName[0]);
        {
            gl4.glObjectLabel(GL_VERTEX_ARRAY, vertexArrayName[0], -1, "Vertex array object".getBytes(), 0);

            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 2 * 2 * Float.BYTES, 0);
            gl4.glVertexAttribPointer(Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, 2 * 2 * Float.BYTES, 2 * Float.BYTES);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl4.glEnableVertexAttribArray(Semantic.Attr.TEXCOORD);

            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT]);
        }
        gl4.glBindVertexArray(0);

        return validated;
    }

    private boolean initDebug(GL4 gl4) {

        boolean validated = true;

        gl4.glEnable(GL_DEBUG_OUTPUT);
        gl4.glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS);
        gl4.glDebugMessageControl(GL_DONT_CARE, GL_DONT_CARE, GL_DONT_CARE, 0, null, true);
//		gl4.glDebugMessageCallback(&test::debugOutput, this);

        gl4.glPushDebugGroup(GL_DEBUG_SOURCE_APPLICATION, 1, -1, "Message test: Begin".getBytes(), 0);

        int[] messageId = {4};
        gl4.glDebugMessageControl(GL_DONT_CARE, GL_DONT_CARE, GL_DONT_CARE, 0, null, FALSE);
        gl4.glDebugMessageControl(GL_DEBUG_SOURCE_APPLICATION, GL_DEBUG_TYPE_OTHER, GL_DONT_CARE, 0, null, true);
        gl4.glDebugMessageControl(GL_DEBUG_SOURCE_APPLICATION, GL_DEBUG_TYPE_OTHER, GL_DONT_CARE, 1, messageId, 0, false);
        String message1 = "Message 1";
        gl4.glDebugMessageInsert(
                GL_DEBUG_SOURCE_APPLICATION,
                GL_DEBUG_TYPE_OTHER, 1,
                GL_DEBUG_SEVERITY_MEDIUM,
                message1.length(), message1);
        String message2 = "Message 2";
        gl4.glDebugMessageInsert(
                GL_DEBUG_SOURCE_THIRD_PARTY,
                GL_DEBUG_TYPE_OTHER, 2,
                GL_DEBUG_SEVERITY_MEDIUM,
                message2.length(), message2);
        gl4.glDebugMessageInsert(
                GL_DEBUG_SOURCE_APPLICATION,
                GL_DEBUG_TYPE_OTHER, 2,
                GL_DEBUG_SEVERITY_MEDIUM,
                -1, "Message 3");
        gl4.glDebugMessageInsert(
                GL_DEBUG_SOURCE_APPLICATION,
                GL_DEBUG_TYPE_OTHER, messageId[0],
                GL_DEBUG_SEVERITY_MEDIUM,
                -1, "Message 4");

        gl4.glPopDebugGroup();

        return validated;
    }
}
