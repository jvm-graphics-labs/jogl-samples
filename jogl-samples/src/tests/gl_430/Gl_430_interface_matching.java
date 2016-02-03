/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_430;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL3.*;
import com.jogamp.opengl.GL4;
import static com.jogamp.opengl.GL4.GL_VERTEX_ATTRIB_ARRAY_LONG;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.BufferUtils;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import framework.VertexAttrib;
import java.nio.ByteBuffer;

/**
 *
 * @author GBarbieri
 */
public class Gl_430_interface_matching extends Test {

    public static void main(String[] args) {
        Gl_430_interface_matching gl_430_interface_matching = new Gl_430_interface_matching();
    }

    public Gl_430_interface_matching() {
        super("gl-430-interface-matching", Profile.CORE, 4, 3);
    }

    private final String SHADERS_SOURCE = "interface-matching";
    private final String SHADERS_ROOT = "src/data/gl_430";

    private int vertexCount = 4;
    private int vertexSize = vertexCount * (2 * Float.BYTES + 4 * Double.BYTES);
    private float[] vertexV3fData = {
        -1.0f, -1.0f,
        +1.0f, -1.0f,
        +1.0f, +1.0f,
        -1.0f, +1.0f};
    private double[] vertexC4dData = {
        1.0, 0.0, 0.0, 1.0,
        1.0, 1.0, 0.0, 1.0,
        0.0, 1.0, 0.0, 1.0,
        0.0, 0.0, 1.0, 1.0};

    private class Program {

        public static final int VERT = 0;
        public static final int FRAG = 1;
        public static final int MAX = 2;
    }

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int ELEMENT = 1;
        public static final int TRANSFORM = 2;
        public static final int MAX = 3;
    }

    private int[] pipelineName = {0}, vertexArrayName = {0}, bufferName = new int[Buffer.MAX],
            programName = new int[Program.MAX];
    private float[] projection = new float[16], model = new float[16], mvp = new float[16];

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;
        validated = validated && checkExtension(gl4, "GL_ARB_arrays_of_arrays");
        validated = validated && checkExtension(gl4, "GL_ARB_program_interface_query");

        if (validated) {
            validated = initMax(gl4);
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

        return validated;
    }

    private boolean initBuffer(GL4 gl4) {

        gl4.glGenBuffers(Buffer.MAX, bufferName, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
        ByteBuffer vertexBuffer = GLBuffers.newDirectByteBuffer(vertexSize);
        int size = 2 * Float.BYTES + 4 * Double.BYTES;
        for (int i = 0; i < vertexCount; i++) {
            vertexBuffer.putFloat(i * size + 0 * Float.BYTES, vertexV3fData[i * 2 + 0])
                    .putFloat(i * size + 1 * Float.BYTES, vertexV3fData[i * 2 + 1]);
            vertexBuffer.putDouble(i * size + 2 * Float.BYTES + 0 * Double.BYTES, vertexC4dData[i * 4 + 0]).
                    putDouble(i * size + 2 * Float.BYTES + 1 * Double.BYTES, vertexC4dData[i * 4 + 1]).
                    putDouble(i * size + 2 * Float.BYTES + 2 * Double.BYTES, vertexC4dData[i * 4 + 2]).
                    putDouble(i * size + 2 * Float.BYTES + 3 * Double.BYTES, vertexC4dData[i * 4 + 3]);
        }
        gl4.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer.rewind(), GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(vertexBuffer);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        int[] uniformBufferOffset = {0};
        gl4.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset, 0);
        int uniformBlockSize = Math.max(mvp.length * Float.BYTES, uniformBufferOffset[0]);

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
        gl4.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_DYNAMIC_DRAW);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return true;
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        gl4.glGenProgramPipelines(1, pipelineName, 0);

        // Create program
        if (validated) {

            ShaderProgram[] shaderPrograms = new ShaderProgram[]{
                new ShaderProgram(), new ShaderProgram()};

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            ShaderCode contShaderCode = ShaderCode.create(gl4, GL_TESS_CONTROL_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "cont", null, true);
            ShaderCode evalShaderCode = ShaderCode.create(gl4, GL_TESS_EVALUATION_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "eval", null, true);
            ShaderCode geomShaderCode = ShaderCode.create(gl4, GL_GEOMETRY_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "geom", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

            shaderPrograms[Program.VERT].init(gl4);
            shaderPrograms[Program.FRAG].init(gl4);

            programName[Program.VERT] = shaderPrograms[Program.VERT].program();
            programName[Program.FRAG] = shaderPrograms[Program.FRAG].program();

            gl4.glProgramParameteri(programName[Program.VERT], GL_PROGRAM_SEPARABLE, GL_TRUE);
            gl4.glProgramParameteri(programName[Program.FRAG], GL_PROGRAM_SEPARABLE, GL_TRUE);

            shaderPrograms[Program.VERT].add(vertShaderCode);
            shaderPrograms[Program.VERT].add(contShaderCode);
            shaderPrograms[Program.VERT].add(evalShaderCode);
            shaderPrograms[Program.VERT].add(geomShaderCode);
            shaderPrograms[Program.VERT].link(gl4, System.out);

            shaderPrograms[Program.FRAG].add(fragShaderCode);
            shaderPrograms[Program.FRAG].link(gl4, System.out);

        }

        if (validated) {

            gl4.glUseProgramStages(pipelineName[0], GL_VERTEX_SHADER_BIT | GL_TESS_CONTROL_SHADER_BIT
                    | GL_TESS_EVALUATION_SHADER_BIT | GL_GEOMETRY_SHADER_BIT, programName[Program.VERT]);
            gl4.glUseProgramStages(pipelineName[0], GL_FRAGMENT_SHADER_BIT, programName[Program.FRAG]);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(1, vertexArrayName, 0);
        gl4.glBindVertexArray(vertexArrayName[0]);
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION + 0, 2, GL_FLOAT, false,
                    2 * Float.BYTES + 4 * Double.BYTES, 0);
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION + 1, 2, GL_FLOAT, false,
                    2 * Float.BYTES + 4 * Double.BYTES, 0);
            gl4.glVertexAttribLPointer(Semantic.Attr.COLOR, 4, GL_DOUBLE,
                    2 * Float.BYTES + 4 * Double.BYTES, 2 * Float.BYTES);
            //glVertexAttribLPointer(semantic::attr::COLOR, 4, GL_DOUBLE, (GLint)sizeof(glf::vertex_v2fc4d), BUFFER_OFFSET(sizeof(glm::vec2)));
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION + 0);
            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION + 1);
            gl4.glEnableVertexAttribArray(Semantic.Attr.COLOR);
        }
        gl4.glBindVertexArray(0);

        VertexAttrib[] valid = new VertexAttrib[16];
        for (int i = 0; i < valid.length; i++) {
            valid[i] = new VertexAttrib();
        }
        valid[Semantic.Attr.POSITION + 0] = new VertexAttrib(GL_TRUE, 2, 2 * Float.BYTES + 4 * Double.BYTES,
                GL_FLOAT, GL_FALSE, false, GL_FALSE, 0, 0, 0);
        valid[Semantic.Attr.POSITION + 1] = new VertexAttrib(GL_TRUE, 2, 2 * Float.BYTES + 4 * Double.BYTES,
                GL_FLOAT, GL_FALSE, false, GL_FALSE, 0, 0, 0);
        valid[Semantic.Attr.COLOR] = new VertexAttrib(GL_TRUE, 4, 2 * Float.BYTES + 4 * Double.BYTES,
                GL_FLOAT, GL_FALSE, false, GL_FALSE, 0, 0, 0);
        //Valid[semantic::attr::COLOR]        = vertexattrib(GL_TRUE, 4, (GLint)sizeof(glf::vertex_v2fc4d), GL_DOUBLE, GL_FALSE, GL_FALSE, GL_FALSE, 0, BUFFER_OFFSET(sizeof(glm::vec2)),NULL);

        // TODO
        //glf::validateVAO(VertexArrayName, Valid);
        return true;
    }

    private boolean initMax(GL4 gl4) {

        int[] maxVertexAttribs = {0};
        gl4.glGetIntegerv(GL_MAX_VERTEX_ATTRIBS, maxVertexAttribs, 0);
        //GL_MAX_DRAW_BUFFERS 8
        //GL_MAX_COLOR_ATTACHMENTS 8
        int[] maxVertexOutput = {0};
        gl4.glGetIntegerv(GL_MAX_VERTEX_OUTPUT_COMPONENTS, maxVertexOutput, 0);
        int[] maxControlInput = {0};
        gl4.glGetIntegerv(GL_MAX_TESS_CONTROL_INPUT_COMPONENTS, maxControlInput, 0);
        int[] maxControlOutput = {0};
        gl4.glGetIntegerv(GL_MAX_TESS_CONTROL_OUTPUT_COMPONENTS, maxControlOutput, 0);
        int[] maxControlTotalOutput = {0};
        gl4.glGetIntegerv(GL_MAX_TESS_CONTROL_TOTAL_OUTPUT_COMPONENTS, maxControlTotalOutput, 0);
        int[] maxEvaluationInput = {0};
        gl4.glGetIntegerv(GL_MAX_TESS_EVALUATION_INPUT_COMPONENTS, maxEvaluationInput, 0);
        int[] maxEvaluationOutput = {0};
        gl4.glGetIntegerv(GL_MAX_TESS_EVALUATION_OUTPUT_COMPONENTS, maxEvaluationOutput, 0);
        int[] maxGeometryInput = {0};
        gl4.glGetIntegerv(GL_MAX_GEOMETRY_INPUT_COMPONENTS, maxGeometryInput, 0);
        int[] maxGeometryOutput = {0};
        gl4.glGetIntegerv(GL_MAX_GEOMETRY_OUTPUT_COMPONENTS, maxGeometryOutput, 0);
        int[] maxFragmentInput = {0};
        gl4.glGetIntegerv(GL_MAX_FRAGMENT_INPUT_COMPONENTS, maxFragmentInput, 0);

        return true;
    }

    private boolean validate(GL4 gl4, int programName) {

        boolean error = false;

        // Pipeline object validation
        {
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
                    new String(infoLog));
        }

        int[] activeAttributeMaxLength = {0};
        int[] activeAttribute = {0};
        gl4.glGetProgramiv(programName, GL_ACTIVE_ATTRIBUTE_MAX_LENGTH, activeAttributeMaxLength, 0);
        gl4.glGetProgramiv(programName, GL_ACTIVE_ATTRIBUTES, activeAttribute, 0);

        int[] attribLength = {0};
        int[] attribSize = {0};
        int[] attribType = {0};
        byte[] attribName = new byte[activeAttributeMaxLength[0]];

        for (int i = 0; i < activeAttribute[0]; ++i) {

            int[] props = {GL_TYPE, GL_ARRAY_SIZE, GL_LOCATION};
            int[] params = new int[props.length];
            int[] length = {0};
            gl4.glGetProgramResourceiv(programName, GL_PROGRAM_INPUT, i, 3,
                    props, 0,
                    3,
                    length, 0,
                    params, 0);

            VertexAttrib vertexAttrib = new VertexAttrib();
            gl4.glGetActiveAttrib(programName,
                    i,
                    activeAttributeMaxLength[0],
                    attribLength, 0,
                    attribSize, 0,
                    attribType, 0,
                    attribName, 0);

            String nameString;
            nameString = new String(attribName);
            // remove spaces at the end
            nameString = nameString.trim();

            int attribLocation = gl4.glGetAttribLocation(programName, nameString);

            int[] value = {0};
            gl4.glGetVertexAttribiv(attribLocation, GL_VERTEX_ATTRIB_ARRAY_ENABLED, value, 0);
            vertexAttrib.enabled = value[0];
            //glGetVertexAttribiv(AttribLocation, GL_VERTEX_ATTRIB_ARRAY_BUFFER_BINDING, &VertexAttrib.Binding);
            gl4.glGetVertexAttribiv(attribLocation, GL_VERTEX_ATTRIB_ARRAY_SIZE, value, 0);
            vertexAttrib.size = value[0];
            gl4.glGetVertexAttribiv(attribLocation, GL_VERTEX_ATTRIB_ARRAY_STRIDE, value, 0);
            vertexAttrib.stride = value[0];
            gl4.glGetVertexAttribiv(attribLocation, GL_VERTEX_ATTRIB_ARRAY_TYPE, value, 0);
            vertexAttrib.type = value[0];
            gl4.glGetVertexAttribiv(attribLocation, GL_VERTEX_ATTRIB_ARRAY_NORMALIZED, value, 0);
            vertexAttrib.normalized = value[0] == GL_TRUE;
            gl4.glGetVertexAttribiv(attribLocation, GL_VERTEX_ATTRIB_ARRAY_INTEGER, value, 0);
            vertexAttrib.integer = value[0];
            gl4.glGetVertexAttribiv(attribLocation, GL_VERTEX_ATTRIB_ARRAY_DIVISOR, value, 0);
            vertexAttrib.divisor = value[0];
            gl4.glGetVertexAttribiv(attribLocation, GL_VERTEX_ATTRIB_ARRAY_LONG, value, 0);
            vertexAttrib.long_ = value[0];
//            gl4.glGetVertexAttribPointerv(attribLocation, GL_VERTEX_ATTRIB_ARRAY_POINTER,  & vertexAttrib.Pointer);

            if (vertexAttrib.integer == GL_TRUE) {
                if (!(vertexAttrib.type == GL_INT
                        || vertexAttrib.type == GL_INT_VEC2
                        || vertexAttrib.type == GL_INT_VEC3
                        || vertexAttrib.type == GL_INT_VEC4
                        || vertexAttrib.type == GL_UNSIGNED_INT
                        || vertexAttrib.type == GL_UNSIGNED_INT_VEC2
                        || vertexAttrib.type == GL_UNSIGNED_INT_VEC3
                        || vertexAttrib.type == GL_UNSIGNED_INT_VEC4)) {
                    return true;
                }

                if (!(vertexAttrib.type == GL_BYTE
                        || vertexAttrib.type == GL_UNSIGNED_BYTE
                        || vertexAttrib.type == GL_SHORT
                        || vertexAttrib.type == GL_UNSIGNED_SHORT
                        || vertexAttrib.type == GL_INT
                        || vertexAttrib.type == GL_UNSIGNED_INT)) {
                    return true;
                }

                //if(AttribSize > 1)
                //GL_BYTE, GL_UNSIGNED_BYTE, GL_SHORT, GL_UNSIGNED_SHORT, GL_INT, GL_UNSIGNED_INT, GL_FLOAT, and GL_DOUBLE
            } else if (vertexAttrib.long_ == GL_TRUE) {
                if (!(vertexAttrib.type == GL_DOUBLE
                        || vertexAttrib.type == GL_DOUBLE_VEC2
                        || vertexAttrib.type == GL_DOUBLE_VEC3
                        || vertexAttrib.type == GL_DOUBLE_VEC4
                        || vertexAttrib.type == GL_DOUBLE_MAT2
                        || vertexAttrib.type == GL_DOUBLE_MAT3
                        || vertexAttrib.type == GL_DOUBLE_MAT4
                        || vertexAttrib.type == GL_DOUBLE_MAT2x3
                        || vertexAttrib.type == GL_DOUBLE_MAT2x4
                        || vertexAttrib.type == GL_DOUBLE_MAT3x2
                        || vertexAttrib.type == GL_DOUBLE_MAT3x4
                        || vertexAttrib.type == GL_DOUBLE_MAT4x2
                        || vertexAttrib.type == GL_DOUBLE_MAT4x3)) {
                    return true;
                }
            } else// if((VertexAttrib.Normalized == GL_TRUE) || (GL_VERTEX_ATTRIB_ARRAY_FLOAT == GL_TRUE))
            {
                if (!(vertexAttrib.type == GL_FLOAT
                        || vertexAttrib.type == GL_FLOAT_VEC2
                        || vertexAttrib.type == GL_FLOAT_VEC3
                        || vertexAttrib.type == GL_FLOAT_VEC4
                        || vertexAttrib.type == GL_FLOAT_MAT2
                        || vertexAttrib.type == GL_FLOAT_MAT3
                        || vertexAttrib.type == GL_FLOAT_MAT4
                        || vertexAttrib.type == GL_FLOAT_MAT2x3
                        || vertexAttrib.type == GL_FLOAT_MAT2x4
                        || vertexAttrib.type == GL_FLOAT_MAT3x2
                        || vertexAttrib.type == GL_FLOAT_MAT3x4
                        || vertexAttrib.type == GL_FLOAT_MAT4x2
                        || vertexAttrib.type == GL_FLOAT_MAT4x3)) {
                    return true;
                } // It could be any vertex array attribute type
            }
            System.out.println("glGetActiveAttrib(" + i + ", " + attribLocation + ", " + attribLength[0]
                    + ", " + attribSize[0] + ", " + attribType[0] + ", " + nameString + ")");
        }

        return error;
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

        {
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
            ByteBuffer pointer = gl4.glMapBufferRange(
                    GL_UNIFORM_BUFFER, 0, mvp.length * Float.BYTES,
                    GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            FloatUtil.makePerspective(projection, 0, true, (float) Math.PI * 0.25f, 4.0f / 3.0f, 0.1f, 100.0f);
            FloatUtil.makeIdentity(model);
            FloatUtil.multMatrix(projection, view(), mvp);
            FloatUtil.multMatrix(mvp, model);

            pointer.asFloatBuffer().put(mvp).rewind();

            // Make sure the uniform buffer is uploaded
            gl4.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        gl4.glViewportIndexedfv(0, new float[]{0, 0, windowSize.x, windowSize.y}, 0);
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{0.0f, 0.0f, 0.0f, 0.0f}, 0);

        gl4.glBindProgramPipeline(pipelineName[0]);
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.TRANSFORM]);
        gl4.glBindVertexArray(vertexArrayName[0]);
        gl4.glPatchParameteri(GL_PATCH_VERTICES, vertexCount);

        assert (!validate(gl4, programName[Program.VERT]));
        gl4.glDrawArraysInstancedBaseInstance(GL_PATCHES, 0, vertexCount, 1, 0);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteVertexArrays(1, vertexArrayName, 0);
        gl4.glDeleteBuffers(Buffer.MAX, bufferName, 0);
        for (int i = 0; i < Program.MAX; ++i) {
            gl4.glDeleteProgram(programName[i]);
        }
        gl4.glDeleteProgramPipelines(1, pipelineName, 0);

        return true;
    }
}
