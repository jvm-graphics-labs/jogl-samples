/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_430;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_ALPHA;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_DYNAMIC_DRAW;
import static com.jogamp.opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_LINEAR;
import static com.jogamp.opengl.GL.GL_LINEAR_MIPMAP_LINEAR;
import static com.jogamp.opengl.GL.GL_MAP_INVALIDATE_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_MAP_WRITE_BIT;
import static com.jogamp.opengl.GL.GL_NONE;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE1;
import static com.jogamp.opengl.GL.GL_TEXTURE2;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_TRUE;
import static com.jogamp.opengl.GL.GL_UNPACK_ALIGNMENT;
import static com.jogamp.opengl.GL.GL_UNSIGNED_INT;
import static com.jogamp.opengl.GL.GL_UNSIGNED_SHORT;
import static com.jogamp.opengl.GL2ES2.GL_ACTIVE_UNIFORMS;
import static com.jogamp.opengl.GL2ES2.GL_DEBUG_SEVERITY_LOW;
import static com.jogamp.opengl.GL2ES2.GL_DEBUG_SOURCE_APPLICATION;
import static com.jogamp.opengl.GL2ES2.GL_DEBUG_TYPE_OTHER;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER_BIT;
import static com.jogamp.opengl.GL2ES2.GL_INFO_LOG_LENGTH;
import static com.jogamp.opengl.GL2ES2.GL_PROGRAM_SEPARABLE;
import static com.jogamp.opengl.GL2ES2.GL_RED;
import static com.jogamp.opengl.GL2ES2.GL_VALIDATE_STATUS;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER_BIT;
import static com.jogamp.opengl.GL2ES3.GL_BLUE;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import static com.jogamp.opengl.GL2ES3.GL_DEPTH;
import static com.jogamp.opengl.GL2ES3.GL_FIRST_VERTEX_CONVENTION;
import static com.jogamp.opengl.GL2ES3.GL_GREEN;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_BASE_LEVEL;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_MAX_LEVEL;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_SWIZZLE_A;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_SWIZZLE_B;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_SWIZZLE_G;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_SWIZZLE_R;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_ARRAY_STRIDE;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER;
import static com.jogamp.opengl.GL3ES3.GL_DRAW_INDIRECT_BUFFER;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.BufferUtils;
import framework.DrawElementsIndirectCommand;
import framework.GlDebugOutput;
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
import jglm.Vec4i;

/**
 *
 * @author GBarbieri
 */
public class Gl_430_multi_draw_indirect extends Test {

    public static void main(String[] args) {
        Gl_430_multi_draw_indirect gl_430_multi_draw_indirect = new Gl_430_multi_draw_indirect();
    }

    public Gl_430_multi_draw_indirect() {
        super("gl-430-multi-draw-indirect", Profile.CORE, 4, 3, new Vec2i(640, 480),
                new Vec2(-(float) Math.PI * 0.2f, (float) Math.PI * 0.2f));
    }

    private final String SHADERS_SOURCE = "multi-draw-indirect";
    private final String SHADERS_ROOT = "src/data/gl_430";
    private final String TEXTURE_DIFFUSE = "kueken7_rgba8_srgb.dds";

    private int elementCount = 15;
    private int elementSize = elementCount * Short.BYTES;
    private short[] elementData = {
        0, 1, 2,
        0, 2, 3,
        0, 1, 2,
        0, 1, 2,
        0, 2, 3};

    private int vertexCount = 11;
    private int vertexSize = vertexCount * 2 * 2 * Float.BYTES;
    private float[] vertexData = {
        -1.0f, -1.0f,/**/ 0.0f, 1.0f,
        +1.0f, -1.0f,/**/ 1.0f, 1.0f,
        +1.0f, +1.0f,/**/ 1.0f, 0.0f,
        -1.0f, +1.0f,/**/ 0.0f, 0.0f,
        //        
        -0.5f, -1.0f,/**/ 0.0f, 1.0f,
        +1.5f, -1.0f,/**/ 1.0f, 1.0f,
        +0.5f, +1.0f,/**/ 1.0f, 0.0f,
        //        
        -0.5f, -1.0f,/**/ 0.0f, 1.0f,
        +0.5f, -1.0f,/**/ 1.0f, 1.0f,
        +1.5f, +1.0f,/**/ 1.0f, 0.0f,
        -1.5f, +1.0f,/**/ 0.0f, 0.0f};

    private int drawDataCount = 3;
    private int drawSize = drawDataCount * Integer.BYTES;
    private int[] drawIdData = {
        0, 1, 2};

    private int indirectBufferCount = 3;

    private enum Buffer {
        VERTEX,
        ELEMENT,
        DRAW_ID,
        TRANSFORM,
        INDIRECT,
        VERTEX_INDIRECTION,
        MAX
    }

    private enum Texture {
        A,
        B,
        C,
        MAX
    }

    private int[] vertexArrayName = {0}, pipelineName = {0}, bufferName = new int[Buffer.MAX.ordinal()],
            textureName = new int[Texture.MAX.ordinal()], drawOffset = new int[indirectBufferCount],
            drawCount = new int[indirectBufferCount];
    private int programName, uniformArrayStrideMat = 256, uniformArrayStrideInt = 256;
    private Vec4i[] viewport = new Vec4i[indirectBufferCount];
    private float[] projection = new float[16], model = new float[16], view = new float[16];

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = checkExtension(gl4, "GL_ARB_multi_draw_indirect");

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

        viewport[0] = new Vec4i((int) (windowSize.x / 3.0f * 0.0f), 0, windowSize.x / 3, windowSize.y);
        viewport[1] = new Vec4i((int) (windowSize.x / 3.0f * 1.0f), 0, windowSize.x / 3, windowSize.y);
        viewport[2] = new Vec4i((int) (windowSize.x / 3.0f * 2.0f), 0, windowSize.x / 3, windowSize.y);

        gl4.glEnable(GL_DEPTH_TEST);
        gl4.glProvokingVertex(GL_FIRST_VERTEX_CONVENTION);

        return validated;
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        gl4.glGenProgramPipelines(1, pipelineName, 0);

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

        int[] activeUniform = {0};
        gl4.glGetProgramiv(programName, GL_ACTIVE_UNIFORMS, activeUniform, 0);

        for (int i = 0; i < activeUniform[0]; ++i) {

            byte[] name = new byte[128];
            int[] length = {0};

            gl4.glGetActiveUniformName(programName, i, name.length, length, 0, name, 0);

            String stringName = new String(name).trim();

            int[] uniformIndices = {i};
            int[] params = {0};

            if (stringName.equals("transform.MVP[0]")) {
                gl4.glGetActiveUniformsiv(programName, 1, uniformIndices, 0, GL_UNIFORM_ARRAY_STRIDE, params, 0);
                uniformArrayStrideMat = params[0];
            }

            if (stringName.equals("indirection.Transform[0]")) {
                gl4.glGetActiveUniformsiv(programName, 1, uniformIndices, 0, GL_UNIFORM_ARRAY_STRIDE, params, 0);
                uniformArrayStrideInt = params[0];
            }
        }

        if (validated) {

            gl4.glGenProgramPipelines(1, pipelineName, 0);
            gl4.glUseProgramStages(pipelineName[0], GL_VERTEX_SHADER_BIT | GL_FRAGMENT_SHADER_BIT, programName);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        gl4.glGenBuffers(Buffer.MAX.ordinal(), bufferName, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl4.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
//        BufferUtils.destroyDirectBuffer(vertexBuffer);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.DRAW_ID.ordinal()]);
        IntBuffer drawIdBuffer = GLBuffers.newDirectIntBuffer(drawIdData);
        gl4.glBufferData(GL_ARRAY_BUFFER, drawSize, drawIdBuffer, GL_STATIC_DRAW);
//        BufferUtils.destroyDirectBuffer(drawIdBuffer);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT.ordinal()]);
        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
//        BufferUtils.destroyDirectBuffer(elementBuffer);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        int[] vertexIndirection = {0, 1, 2};
        int paddingInt = Math.max(Integer.BYTES, uniformArrayStrideInt);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.VERTEX_INDIRECTION.ordinal()]);
        gl4.glBufferData(GL_UNIFORM_BUFFER, paddingInt * 3, null, GL_DYNAMIC_DRAW);
        IntBuffer paddingIntBuffer = GLBuffers.newDirectIntBuffer(1);
        paddingIntBuffer.put(vertexIndirection[0]).rewind();
        gl4.glBufferSubData(GL_UNIFORM_BUFFER, paddingInt * 0, paddingInt, paddingIntBuffer);
        paddingIntBuffer.put(vertexIndirection[1]).rewind();
        gl4.glBufferSubData(GL_UNIFORM_BUFFER, paddingInt * 1, paddingInt, paddingIntBuffer);
        paddingIntBuffer.put(vertexIndirection[2]).rewind();
        gl4.glBufferSubData(GL_UNIFORM_BUFFER, paddingInt * 2, paddingInt, paddingIntBuffer);
//        BufferUtils.destroyDirectBuffer(paddingIntBuffer);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        int paddingMat = Math.max(projection.length * Float.BYTES, uniformArrayStrideMat);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM.ordinal()]);
        gl4.glBufferData(GL_UNIFORM_BUFFER, paddingMat * 3, null, GL_DYNAMIC_DRAW);
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

        gl4.glBindBuffer(GL_DRAW_INDIRECT_BUFFER, bufferName[Buffer.INDIRECT.ordinal()]);
        IntBuffer commandsBuffer = GLBuffers.newDirectIntBuffer(5 * commands.length);
        for (DrawElementsIndirectCommand command : commands) {
            commandsBuffer.put(command.toIntArray());
        }
        gl4.glBufferData(GL_DRAW_INDIRECT_BUFFER, commandsBuffer.capacity() * Integer.BYTES,
                commandsBuffer.rewind(), GL_STATIC_DRAW);
//        BufferUtils.destroyDirectBuffer(commandsBuffer);
        gl4.glBindBuffer(GL_DRAW_INDIRECT_BUFFER, 0);

        return true;
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(1, vertexArrayName, 0);
        gl4.glBindVertexArray(vertexArrayName[0]);
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 2 * 2 * Float.BYTES, 0);
            gl4.glVertexAttribPointer(Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, 2 * 2 * Float.BYTES, 2 * Float.BYTES);

            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.DRAW_ID.ordinal()]);
            gl4.glVertexAttribIPointer(Semantic.Attr.DRAW_ID, 1, GL_UNSIGNED_INT, Integer.BYTES, 0);
            gl4.glVertexAttribDivisor(Semantic.Attr.DRAW_ID, 1);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl4.glEnableVertexAttribArray(Semantic.Attr.TEXCOORD);
            gl4.glEnableVertexAttribArray(Semantic.Attr.DRAW_ID);

            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT.ordinal()]);
        }
        gl4.glBindVertexArray(0);

        return true;
    }

    private boolean initTexture(GL4 gl4) {

        try {
            jgli.Texture2d texture = new Texture2d(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE));
            assert (!texture.empty());
            jgli.Gl.Format format = jgli.Gl.translate(texture.format());

            gl4.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

            gl4.glGenTextures(Texture.MAX.ordinal(), textureName, 0);
            gl4.glActiveTexture(GL_TEXTURE0);
            gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.A.ordinal()]);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_R, GL_RED);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_G, GL_NONE);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_B, GL_NONE);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_A, GL_ALPHA);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
            //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, GLint(Texture.levels() - 1));
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

            ///////////////////////////////////////////
            gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.B.ordinal()]);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_R, GL_NONE);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_G, GL_GREEN);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_B, GL_NONE);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_A, GL_ALPHA);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
            //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, GLint(Texture.levels() - 1));
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

            ///////////////////////////////////////////
            gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.C.ordinal()]);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_R, GL_NONE);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_G, GL_NONE);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_B, GL_BLUE);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_A, GL_ALPHA);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
            //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, GLint(Texture.levels() - 1));
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

            gl4.glBindTexture(GL_TEXTURE_2D, 0);
            gl4.glPixelStorei(GL_UNPACK_ALIGNMENT, 4);

        } catch (IOException ex) {
            Logger.getLogger(Gl_430_multi_draw_indirect.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    private void validate(GL4 gl4) {

        int[] status = {0};
        gl4.glValidateProgramPipeline(pipelineName[0]);
        gl4.glGetProgramPipelineiv(pipelineName[0], GL_VALIDATE_STATUS, status, 0);

        if (status[0] != GL_TRUE) {
            int[] lengthMax = {0};
            gl4.glGetProgramPipelineiv(pipelineName[0], GL_INFO_LOG_LENGTH, lengthMax, 0);

            int[] lengthQuery = {0};
            byte[] infoLog = new byte[lengthMax[0] + 1];
            gl4.glGetProgramPipelineInfoLog(pipelineName[0], infoLog.length,
                    lengthQuery, 0,
                    infoLog, 0);

            gl4.glDebugMessageInsert(
                    GL_DEBUG_SOURCE_APPLICATION,
                    GL_DEBUG_TYPE_OTHER, 76,
                    GL_DEBUG_SEVERITY_LOW,
                    lengthQuery[0],
                    new String(infoLog).trim());
        }
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        float[] depth = {1.0f};
        gl4.glClearBufferfv(GL_DEPTH, 0, depth, 0);
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, 0);

        {
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM.ordinal()]);
            ByteBuffer pointer = gl4.glMapBufferRange(GL_UNIFORM_BUFFER, 0,
                    projection.length * Float.BYTES * 3, GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            FloatUtil.makePerspective(projection, 0, true, (float) Math.PI * 0.25f,
                    windowSize.x / 3.0f / windowSize.y, 0.1f, 100.0f);
            view = view();
            FloatUtil.makeIdentity(model);

            FloatUtil.multMatrix(projection, view);

            FloatUtil.makeTranslation(model, true, 0.0f, 0.0f, 0.5f);
            FloatUtil.multMatrix(projection, model, model);
            FloatBuffer pointerF = pointer.asFloatBuffer().put(model);

            FloatUtil.makeTranslation(model, true, 0.0f, 0.0f, 0.0f);
            FloatUtil.multMatrix(projection, model, model);
            pointerF.put(model);

            FloatUtil.makeTranslation(model, true, 0.0f, 0.0f, -0.5f);
            FloatUtil.multMatrix(projection, model, model);
            pointerF.put(model).rewind();

            gl4.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        gl4.glActiveTexture(GL_TEXTURE0);
        gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.A.ordinal()]);
        gl4.glActiveTexture(GL_TEXTURE1);
        gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.B.ordinal()]);
        gl4.glActiveTexture(GL_TEXTURE2);
        gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.C.ordinal()]);

        gl4.glBindProgramPipeline(pipelineName[0]);
        gl4.glBindVertexArray(vertexArrayName[0]);
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.TRANSFORM.ordinal()]);
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.INDIRECTION, bufferName[Buffer.VERTEX_INDIRECTION.ordinal()]);

        gl4.glBindBuffer(GL_DRAW_INDIRECT_BUFFER, bufferName[Buffer.INDIRECT.ordinal()]);

        validate(gl4);

        for (int i = 0; i < indirectBufferCount; ++i) {

            gl4.glViewportIndexedfv(0, viewport[i].toFloatArray(), 0);
//            IntBuffer buffer = GLBuffers.newDirectIntBuffer(1);
//            IntBuffer buffer = GLBuffers.newDirectIntBuffer(new int[]{5 * Integer.BYTES * drawOffset[i]});
//            buffer.put(5 * Integer.BYTES * drawOffset[i]).rewind();
//            buffer.put(0).rewind();
            gl4.glMultiDrawElementsIndirect(GL_TRIANGLES, GL_UNSIGNED_SHORT, null, drawCount[i], 5 * Integer.BYTES);
        }

        return true;
    }
}
