/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_440;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL3ES3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import core.glm;
import dev.Mat4;
import dev.Vec3;
import dev.Vec4;
import framework.BufferUtils;
import framework.DrawElementsIndirectCommand;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
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
public class Gl_440_multi_draw_indirect_id_arb extends Test {

    public static void main(String[] args) {
        Gl_440_multi_draw_indirect_id_arb gl_440_multi_draw_indirect_id_arb = new Gl_440_multi_draw_indirect_id_arb();
    }

    public Gl_440_multi_draw_indirect_id_arb() {
        super("gl-440-multi-draw-indirect-id-arb", Profile.CORE, 4, 4, new Vec2i(640, 480),
                new Vec2(-(float) Math.PI * 0.2f, (float) Math.PI * 0.2f));
    }

    private final String SAMPLE_SHADERS = "multi-draw-indirect-id";
    private final String SHADERS_ROOT = "src/data/gl_440";
    private final String TEXTURE_DIFFUSE = "kueken1-bgr8.dds";

    private int elementCount = 15;
    private int elementSize = elementCount * Short.BYTES;
    private short[] elementData = {
        0, 1, 2,
        0, 2, 3,
        0, 1, 2,
        0, 1, 2,
        0, 2, 3};

    private int vertexCount = 11;
    private int vertexSize = vertexCount * 2 * dev.Vec2.SIZEOF;
    private float[] vertexData = {
        -1.0f, -1.0f,/**/ 0.0f, 1.0f,
        +1.0f, -1.0f,/**/ 1.0f, 1.0f,
        +1.0f, +1.0f,/**/ 1.0f, 0.0f,
        -1.0f, +1.0f,/**/ 0.0f, 0.0f,
        -0.5f, -1.0f,/**/ 0.0f, 1.0f,
        +1.5f, -1.0f,/**/ 1.0f, 1.0f,
        +0.5f, +1.0f,/**/ 1.0f, 0.0f,
        -0.5f, -1.0f,/**/ 0.0f, 1.0f,
        +0.5f, -1.0f,/**/ 1.0f, 1.0f,
        +1.5f, +1.0f,/**/ 1.0f, 0.0f,
        -1.5f, +1.0f,/**/ 0.0f, 0.0f};

    private int drawDataCount = 3;
    private int drawSize = drawDataCount * Integer.BYTES;
    private int[] drawIDData = {
        0, 1, 2};

    private int indirectBufferCount = 3;

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int ELEMENT = 1;
        public static final int TRANSFORM = 2;
        public static final int INDIRECT = 3;
        public static final int VERTEX_INDIRECTION = 4;
        public static final int MAX = 5;
    }

    private class Texture {

        public static final int A = 0;
        public static final int B = 1;
        public static final int C = 2;
        public static final int MAX = 3;
    }

    private int[] bufferName = new int[Buffer.MAX], textureName = new int[Texture.MAX], vertexArrayName = {0},
            pipelineName = {0}, uniformArrayStride = {0}, drawOffset = new int[indirectBufferCount],
            drawCount = new int[indirectBufferCount];
    private int programName;
    private Vec4[] viewport = new Vec4[indirectBufferCount];

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;
        validated = validated && checkExtension(gl4, "GL_ARB_multi_draw_indirect");
        validated = validated && checkExtension(gl4, "GL_ARB_shader_draw_parameters");
        validated = validated && checkExtension(gl4, "GL_ARB_shader_storage_buffer_object");
        validated = validated && checkExtension(gl4, "GL_ARB_buffer_storage");

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

        viewport[0] = new Vec4((float) windowSize.x / 3.0f * 0.0f, 0, (float) windowSize.x / 3, windowSize.y);
        viewport[1] = new Vec4((float) windowSize.x / 3.0f * 1.0f, 0, (float) windowSize.x / 3, windowSize.y);
        viewport[2] = new Vec4((float) windowSize.x / 3.0f * 2.0f, 0, (float) windowSize.x / 3, windowSize.y);

        gl4.glEnable(GL_DEPTH_TEST);
        gl4.glProvokingVertex(GL_FIRST_VERTEX_CONVENTION);

        return validated;
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                this.getClass(), SHADERS_ROOT, null, SAMPLE_SHADERS, "vert", null, true);
        ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                this.getClass(), SHADERS_ROOT, null, SAMPLE_SHADERS, "frag", null, true);

        ShaderProgram shaderProgram = new ShaderProgram();
        shaderProgram.init(gl4);

        programName = shaderProgram.program();

        gl4.glProgramParameteri(programName, GL_PROGRAM_SEPARABLE, GL_TRUE);

        shaderProgram.add(vertShaderCode);
        shaderProgram.add(fragShaderCode);

        int[] activeUniform = {0};
        gl4.glGetProgramiv(programName, GL_ACTIVE_UNIFORMS, activeUniform, 0);

        for (int i = 0; i < activeUniform[0]; ++i) {

            byte[] name = new byte[128];
            int[] length = {0};

            gl4.glGetActiveUniformName(programName, i, name.length, length, 0, name, 0);

            String stringName = new String(name).trim();

            if (stringName.equals("transform.MVP[0]")) {
                int[] uniformIndices = {i};
                gl4.glGetActiveUniformsiv(programName, 1, uniformIndices, 0, GL_UNIFORM_ARRAY_STRIDE, uniformArrayStride, 0);
            }
        }

        int[] activeShaderStorageBuffer = {0};
        gl4.glGetProgramInterfaceiv(programName, GL_BUFFER_VARIABLE, GL_ACTIVE_RESOURCES, activeShaderStorageBuffer, 0);

        for (int i = 0; i < activeShaderStorageBuffer[0]; ++i) {
            byte[] name = new byte[128];
            int[] length = {0};

            gl4.glGetProgramResourceName(programName, GL_BUFFER_VARIABLE, i, name.length, length, 0, name, 0);

            String stringName = new String(name);

            int[] propsTopLevel = {GL_TOP_LEVEL_ARRAY_STRIDE};
            int[] paramsTopLevel = {0};
            gl4.glGetProgramResourceiv(programName, GL_BUFFER_VARIABLE, i, 1, propsTopLevel, 0,
                    4, null, 0, paramsTopLevel, 0);

            int[] propsArrayStride = {GL_ARRAY_STRIDE};
            int[] paramsArrayStride = {0};
            gl4.glGetProgramResourceiv(programName, GL_BUFFER_VARIABLE, i, 1, propsArrayStride, 0,
                    4, null, 0, paramsArrayStride, 0);

            System.out.printf("%d", paramsArrayStride[0]);
        }

        if (validated) {

            gl4.glGenProgramPipelines(1, pipelineName, 0);
            gl4.glUseProgramStages(pipelineName[0], GL_VERTEX_SHADER_BIT | GL_FRAGMENT_SHADER_BIT, programName);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        gl4.glGenBuffers(Buffer.MAX, bufferName, 0);

        gl4.glBindBuffer(GL_SHADER_STORAGE_BUFFER, bufferName[Buffer.VERTEX]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl4.glBufferData(GL_SHADER_STORAGE_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(vertexBuffer);
        gl4.glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);

        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT]);
        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(elementBuffer);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        int[] vertexIndirection = {0, 1, 2};
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.VERTEX_INDIRECTION]);
        IntBuffer vertexIndirectionBuffer = GLBuffers.newDirectIntBuffer(vertexIndirection);
        gl4.glBufferData(GL_UNIFORM_BUFFER, Integer.BYTES * 3, vertexIndirectionBuffer, GL_DYNAMIC_DRAW);
        BufferUtils.destroyDirectBuffer(vertexIndirectionBuffer);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        int padding = Math.max(Mat4.SIZEOF, uniformArrayStride[0]);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
        gl4.glBufferData(GL_UNIFORM_BUFFER, padding * 3, null, GL_DYNAMIC_DRAW);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        DrawElementsIndirectCommand[] commands = new DrawElementsIndirectCommand[6];
        commands[0] = new DrawElementsIndirectCommand(elementCount, 1, 0, 0, 0);
        commands[1] = new DrawElementsIndirectCommand(elementCount >> 1, 1, 6, 4, 1);
        commands[2] = new DrawElementsIndirectCommand(elementCount, 1, 9, 7, 2);
        commands[3] = new DrawElementsIndirectCommand(elementCount, 1, 0, 0, 0);
        commands[4] = new DrawElementsIndirectCommand(elementCount >> 1, 1, 6, 4, 1);
        commands[5] = new DrawElementsIndirectCommand(elementCount, 1, 9, 7, 2);

        drawCount[0] = 3;
        drawCount[1] = 2;
        drawCount[2] = 1;
        drawOffset[0] = 0;
        drawOffset[1] = 1;
        drawOffset[2] = 3;

        IntBuffer commandsBuffer = GLBuffers.newDirectIntBuffer(5 * commands.length);
        for (DrawElementsIndirectCommand command : commands) {
            commandsBuffer.put(command.toIntArray());
        }
        gl4.glBindBuffer(GL_DRAW_INDIRECT_BUFFER, bufferName[Buffer.INDIRECT]);
        gl4.glBufferData(GL_DRAW_INDIRECT_BUFFER, commandsBuffer.capacity() * Integer.BYTES, commandsBuffer.rewind(), GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(commandsBuffer);
        gl4.glBindBuffer(GL_DRAW_INDIRECT_BUFFER, 0);

        return true;
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

    private boolean initTexture(GL4 gl4) {

        boolean validated = true;

        try {

            jgli.Texture2d texture = new Texture2d(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE));
            assert (!texture.empty());

            gl4.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

            gl4.glGenTextures(Texture.MAX, textureName, 0);
            gl4.glActiveTexture(GL_TEXTURE0);
            gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.A]);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_R, GL_RED);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_G, GL_NONE);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_B, GL_NONE);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_A, GL_ALPHA);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
            //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, GLint(Texture.levels() - 1));
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            gl4.glTexStorage2D(GL_TEXTURE_2D, texture.levels(), GL_RGBA8, texture.dimensions()[0], texture.dimensions()[1]);
            for (int level = 0; level < texture.levels(); ++level) {
                gl4.glTexSubImage2D(GL_TEXTURE_2D, level,
                        0, 0,
                        texture.dimensions(level)[0], texture.dimensions(level)[1],
                        GL_BGR, GL_UNSIGNED_BYTE,
                        texture.data(level));
            }

            ///////////////////////////////////////////
            gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.B]);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_R, GL_NONE);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_G, GL_GREEN);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_B, GL_NONE);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_A, GL_ALPHA);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
            //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, GLint(Texture.levels() - 1));
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            gl4.glTexStorage2D(GL_TEXTURE_2D, texture.levels(), GL_RGBA8, texture.dimensions()[0], texture.dimensions()[1]);
            for (int level = 0; level < texture.levels(); ++level) {
                gl4.glTexSubImage2D(GL_TEXTURE_2D, level,
                        0, 0,
                        texture.dimensions(level)[0], texture.dimensions(level)[1],
                        GL_BGR, GL_UNSIGNED_BYTE,
                        texture.data(level));
            }

            ///////////////////////////////////////////
            gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.C]);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_R, GL_NONE);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_G, GL_NONE);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_B, GL_BLUE);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_A, GL_ALPHA);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
            //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, GLint(Texture.levels() - 1));
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            gl4.glTexStorage2D(GL_TEXTURE_2D, texture.levels(), GL_RGBA8, texture.dimensions()[0], texture.dimensions()[1]);
            for (int level = 0; level < texture.levels(); ++level) {
                gl4.glTexSubImage2D(GL_TEXTURE_2D, level,
                        0, 0,
                        texture.dimensions(level)[0], texture.dimensions(level)[1],
                        GL_BGR, GL_UNSIGNED_BYTE,
                        texture.data(level));
            }

            gl4.glBindTexture(GL_TEXTURE_2D, 0);
            gl4.glPixelStorei(GL_UNPACK_ALIGNMENT, 4);

        } catch (IOException ex) {
            Logger.getLogger(Gl_440_multi_draw_indirect_id_arb.class.getName()).log(Level.SEVERE, null, ex);
        }
        return validated;
    }

    private void validate(GL4 gl4) {

        int[] status = {0};
        int[] lengthMax = {0};
        gl4.glValidateProgramPipeline(pipelineName[0]);
        gl4.glGetProgramPipelineiv(pipelineName[0], GL_VALIDATE_STATUS, status, 0);
        gl4.glGetProgramPipelineiv(pipelineName[0], GL_INFO_LOG_LENGTH, lengthMax, 0);

        int[] lengthQuery = {0};
        byte[] infoLog = new byte[lengthMax[0] + 1];
        gl4.glGetProgramPipelineInfoLog(pipelineName[0], infoLog.length, lengthQuery, 0, infoLog, 0);

        gl4.glDebugMessageInsert(
                GL_DEBUG_SOURCE_APPLICATION,
                GL_DEBUG_TYPE_OTHER, 76,
                GL_DEBUG_SEVERITY_LOW,
                lengthQuery[0],
                new String(infoLog).trim());
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        float[] depth = {1.0f};
        gl4.glClearBufferfv(GL_DEPTH, 0, depth, 0);
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, 0);

        {
            int padding = Math.max(Mat4.SIZEOF, uniformArrayStride[0]);

            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
            ByteBuffer pointer = gl4.glMapBufferRange(GL_UNIFORM_BUFFER, 0, padding * 3, GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, (float) windowSize.x / 3.0f / windowSize.y, 0.1f, 100.0f);
            Mat4 view = viewMat4();
            Mat4 model = new Mat4(1.0f);

            pointer.position(padding * 0);
            pointer.asFloatBuffer().put(projection.mul(view).translate(new Mat4(1.0f), new Vec3(0.0f, 0.0f, 0.5f)).toFA_());
            // now projection contains projection * view
            pointer.position(padding * 1);
            pointer.asFloatBuffer().put(projection.translate(new Mat4(1.0f), new Vec3(0.0f, 0.0f, 0.0f)).toFA_());
            pointer.position(padding * 2);
            pointer.asFloatBuffer().put(projection.translate(new Mat4(1.0f), new Vec3(0.0f, 0.0f, -0.5f)).toFA_());
            gl4.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        gl4.glActiveTexture(GL_TEXTURE0);
        gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.A]);
        gl4.glActiveTexture(GL_TEXTURE1);
        gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.B]);
        gl4.glActiveTexture(GL_TEXTURE2);
        gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.C]);

        gl4.glBindProgramPipeline(pipelineName[0]);
        gl4.glBindVertexArray(vertexArrayName[0]);
        gl4.glBindBufferBase(GL_SHADER_STORAGE_BUFFER, Semantic.Storage.VERTEX, bufferName[Buffer.VERTEX]);
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.TRANSFORM]);
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.INDIRECTION, bufferName[Buffer.VERTEX_INDIRECTION]);

        gl4.glBindBuffer(GL_DRAW_INDIRECT_BUFFER, bufferName[Buffer.INDIRECT]);

        validate(gl4);

        for (int i = 0; i < indirectBufferCount; ++i) {
            gl4.glViewportIndexedfv(0, viewport[i].toFA_(), 0);
            gl4.glMultiDrawElementsIndirect(GL_TRIANGLES, GL_UNSIGNED_SHORT, null, drawCount[i], 
                    DrawElementsIndirectCommand.SIZEOF);
        }

        return true;
    }
}
