/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_420;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import framework.VertexAttrib;
import java.nio.FloatBuffer;

/**
 *
 * @author GBarbieri
 */
public class Gl_420_interface_matching extends Test {

    public static void main(String[] args) {
        Gl_420_interface_matching gl_420_interface_matching = new Gl_420_interface_matching();
    }

    public Gl_420_interface_matching() {
        super("gl-420-interface-matching", Profile.CORE, 4, 2);
    }

    private final String SHADERS_SOURCE = "interface-matching";
    private final String SHADERS_ROOT = "src/data/gl_420";

    private int vertexCount = 4;
    private int vertexSize = vertexCount * (2 + 4) * Float.BYTES;
    private float[] vertexData = {
        -1.0f, -1.0f, 1.0f, 0.0f, 0.0f, 1.0f,
        +1.0f, -1.0f, 1.0f, 1.0f, 0.0f, 1.0f,
        +1.0f, +1.0f, 0.0f, 1.0f, 0.0f, 1.0f,
        -1.0f, +1.0f, 0.0f, 0.0f, 1.0f, 1.0f};

    private enum Program {
        VERT,
        FRAG,
        MAX
    }

    private int[] pipelineName = {0}, vertexArrayName = {0}, bufferName = {0}, programName = new int[Program.MAX.ordinal()];
    private int uniformMvp;
    private float[] projection = new float[16], model = new float[16], mvp = new float[16];

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;

        if (validated) {
            validated = initMax(gl4);
        };
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

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        gl4.glGenProgramPipelines(1, pipelineName, 0);

        // Create program
        if (validated) {

            ShaderProgram[] shaderPrograms = new ShaderProgram[Program.MAX.ordinal()];
            for (int i = 0; i < Program.MAX.ordinal(); i++) {
                shaderPrograms[i] = new ShaderProgram();
            }

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

            shaderPrograms[Program.VERT.ordinal()].init(gl4);
            shaderPrograms[Program.FRAG.ordinal()].init(gl4);
            programName[Program.VERT.ordinal()] = shaderPrograms[Program.VERT.ordinal()].program();
            programName[Program.FRAG.ordinal()] = shaderPrograms[Program.FRAG.ordinal()].program();

            gl4.glProgramParameteri(programName[Program.VERT.ordinal()], GL_PROGRAM_SEPARABLE, GL_TRUE);
            gl4.glProgramParameteri(programName[Program.FRAG.ordinal()], GL_PROGRAM_SEPARABLE, GL_TRUE);

            shaderPrograms[Program.VERT.ordinal()].add(vertShaderCode);
            shaderPrograms[Program.VERT.ordinal()].add(contShaderCode);
            shaderPrograms[Program.VERT.ordinal()].add(evalShaderCode);
            shaderPrograms[Program.VERT.ordinal()].add(geomShaderCode);
            shaderPrograms[Program.VERT.ordinal()].link(gl4, System.out);

            shaderPrograms[Program.FRAG.ordinal()].add(fragShaderCode);
            shaderPrograms[Program.FRAG.ordinal()].link(gl4, System.out);
        }

        if (validated) {

            gl4.glUseProgramStages(pipelineName[0], GL_VERTEX_SHADER_BIT | GL_TESS_CONTROL_SHADER_BIT
                    | GL_TESS_EVALUATION_SHADER_BIT | GL_GEOMETRY_SHADER_BIT, programName[Program.VERT.ordinal()]);
            gl4.glUseProgramStages(pipelineName[0], GL_FRAGMENT_SHADER_BIT, programName[Program.FRAG.ordinal()]);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(1, vertexArrayName, 0);
        gl4.glBindVertexArray(vertexArrayName[0]);
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[0]);
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION + 0, 2, GL_FLOAT, false, (2 + 4) * Float.BYTES, 0);
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION + 1, 2, GL_FLOAT, false, (2 + 4) * Float.BYTES, 0);
            gl4.glVertexAttribPointer(Semantic.Attr.COLOR, 4, GL_FLOAT, false, (2 + 4) * Float.BYTES, 2 * Float.BYTES);
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
        valid[Semantic.Attr.POSITION + 0] = new VertexAttrib(GL_TRUE, 0, 2, (2 + 4) * Float.BYTES,
                GL_FLOAT, false, GL_FALSE, GL_FALSE, 0, 0);
        valid[Semantic.Attr.POSITION + 1] = new VertexAttrib(GL_TRUE, 0, 2, (2 + 4) * Float.BYTES,
                GL_FLOAT, false, GL_FALSE, GL_FALSE, 0, 0);
        valid[Semantic.Attr.COLOR] = new VertexAttrib(GL_TRUE, 0, 4, (2 + 4) * Float.BYTES,
                GL_FLOAT, false, GL_FALSE, GL_FALSE, 0, 2 * Float.BYTES);
        validate(gl4, vertexArrayName[0], valid);

        return true;
    }

    private boolean initBuffer(GL4 gl4) {

        gl4.glGenBuffers(1, bufferName, 0);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[0]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl4.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

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

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

        FloatUtil.makePerspective(projection, 0, true, (float) Math.PI * 0.25f,
                (float) windowSize.x / windowSize.y, 0.1f, 100.0f);
        FloatUtil.makeIdentity(model);
        FloatUtil.multMatrix(projection, view(), mvp);
        FloatUtil.multMatrix(mvp, model);

        gl4.glProgramUniformMatrix4fv(programName[Program.VERT.ordinal()], uniformMvp, 1, false, mvp, 0);

        gl4.glViewportIndexedfv(0, new float[]{0, 0, windowSize.x, windowSize.y}, 0);
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{0.0f, 0.0f, 0.0f, 0.0f}, 0);

        gl4.glBindProgramPipeline(pipelineName[0]);

        gl4.glBindVertexArray(vertexArrayName[0]);
        gl4.glPatchParameteri(GL_PATCH_VERTICES, vertexCount);

        assert (!validate(gl4, programName[Program.VERT.ordinal()]));
        gl4.glDrawArraysInstancedBaseInstance(GL_PATCHES, 0, vertexCount, 1, 0);

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
//			byte[] nameSwap=new byte[activeAttributeMaxLength[0]];
//			std::swap(attribName, nameSwap);

            int attribLocation = gl4.glGetAttribLocation(programName, nameString);

            VertexAttrib vertexAttrib = new VertexAttrib();
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

//			gl4.glGetVertexAttribPointerv(attribLocation, GL_VERTEX_ATTRIB_ARRAY_POINTER, vertexAttrib.Pointer);
            if (GL_VERTEX_ATTRIB_ARRAY_INTEGER == GL_TRUE) {
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
            } else if (vertexAttrib.long_ == GL_TRUE) // OpenGL Spec bug 
            {
                if (vertexAttrib.type == GL_DOUBLE
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
                        || vertexAttrib.type == GL_DOUBLE_MAT4x3) {
                    if (vertexAttrib.type != GL_DOUBLE) {
                        return true;
                    }
                } else// if((VertexAttrib.Normalized == GL_TRUE) || (GL_VERTEX_ATTRIB_ARRAY_FLOAT == GL_TRUE))
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
}
