/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_440;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import glm.glm;
import glm.mat._4.Mat4;
import glm.vec._2.Vec2;
import dev.Vec4d;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import framework.VertexAttrib;
import glf.Vertex_v2fc4d;
import java.nio.ByteBuffer;

/**
 *
 * @author elect
 */
public class Gl_440_interface_matching extends Test {

    public static void main(String[] args) {
        Gl_440_interface_matching gl_440_interface_matching = new Gl_440_interface_matching();
    }

    public Gl_440_interface_matching() {
        super("gl-440-interface-macthing", Profile.CORE, 4, 4);
    }

    private final String SAMPLE_SHADERS = "interface-matching";
    private final String SHADERS_ROOT = "src/data/gl_440";

    private int vertexCount = 4;
    private int vertexSize = vertexCount * Vertex_v2fc4d.SIZE;
    private Vertex_v2fc4d[] vertexData = {
        new Vertex_v2fc4d(new Vec2(-1.0f, -1.0f), new Vec4d(1.0f, 0.0f, 0.0f, 1.0f)),
        new Vertex_v2fc4d(new Vec2(+1.0f, -1.0f), new Vec4d(1.0f, 1.0f, 0.0f, 1.0f)),
        new Vertex_v2fc4d(new Vec2(+1.0f, +1.0f), new Vec4d(0.0f, 1.0f, 0.0f, 1.0f)),
        new Vertex_v2fc4d(new Vec2(-1.0f, +1.0f), new Vec4d(0.0f, 0.0f, 1.0f, 1.0f))};

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

    private int[] pipelineName = {0}, vertexArrayName = {0}, programName = new int[Program.MAX],
            bufferName = new int[Buffer.MAX];

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
                    new String(infoLog).trim());
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
            int[] params = new int[3];
            int[] length = {0};
            gl4.glGetProgramResourceiv(programName, GL_PROGRAM_INPUT, i, 3,
                    props, 0, 3, length, 0, params, 0);

            VertexAttrib vertexAttrib = new VertexAttrib();
            gl4.glGetActiveAttrib(programName,
                    i,
                    activeAttributeMaxLength[0],
                    attribLength, 0,
                    attribSize, 0,
                    attribType, 0,
                    attribName, 0);

            String nameString = new String(attribName).trim();

            int attribLocation = gl4.glGetAttribLocation(programName, nameString);

            int[] value = {0};
            gl4.glGetVertexAttribiv(attribLocation, GL_VERTEX_ATTRIB_ARRAY_ENABLED, value, 0);
            vertexAttrib.enabled = value[0];
            gl4.glGetVertexAttribiv(attribLocation, GL_VERTEX_ATTRIB_ARRAY_BUFFER_BINDING, value, 0);
            vertexAttrib.binding = value[0];
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
//            gl4.glGetVertexAttribPointerv(attribLocation, GL_VERTEX_ATTRIB_ARRAY_POINTER, &vertexAttrib.Pointer);

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
            System.out.println(
                    "glGetActiveAttrib(\n\t" + i + ", \n\t" + attribLocation + ", \n\t" + attribLength[0]
                    + ", \n\t" + attribSize[0] + ", \n\t" + attribType[0] + ", \n\t+" + nameString + ")\n");
        }

        return error;
    }

    private boolean initBuffer(GL4 gl4) {

        gl4.glGenBuffers(Buffer.MAX, bufferName, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
        ByteBuffer vertexBuffer = GLBuffers.newDirectByteBuffer(vertexSize);
        for (int i = 0; i < vertexCount; i++) {
            vertexData[i].toBB(vertexBuffer, i);
        }
        vertexBuffer.rewind();
        gl4.glBufferStorage(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, 0);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        int[] uniformBufferOffset = {0};

        gl4.glGetIntegerv(
                GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT,
                uniformBufferOffset, 0);

        int uniformBlockSize = Math.max(Mat4.SIZE, uniformBufferOffset[0]);

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
        gl4.glBufferStorage(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return true;
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        // Create program
        if (validated) {

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SAMPLE_SHADERS, "vert", null, true);
            ShaderCode contShaderCode = ShaderCode.create(gl4, GL_TESS_CONTROL_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SAMPLE_SHADERS, "cont", null, true);
            ShaderCode evalShaderCode = ShaderCode.create(gl4, GL_TESS_EVALUATION_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SAMPLE_SHADERS, "eval", null, true);
            ShaderCode geomShaderCode = ShaderCode.create(gl4, GL_GEOMETRY_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SAMPLE_SHADERS, "geom", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SAMPLE_SHADERS, "frag", null, true);

            ShaderProgram[] shaderPrograms = new ShaderProgram[]{new ShaderProgram(), new ShaderProgram()};
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

            gl4.glGenProgramPipelines(1, pipelineName, 0);
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
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION + 0, 2, GL_FLOAT, false, Vertex_v2fc4d.SIZE, 0);
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION + 1, 2, GL_FLOAT, false, Vertex_v2fc4d.SIZE, 0);
            gl4.glVertexAttribLPointer(Semantic.Attr.COLOR, 4, GL_DOUBLE, Vertex_v2fc4d.SIZE, Vec2.SIZE);
            //glVertexAttribLPointer(semantic::attr::COLOR, 4, GL_DOUBLE, sizeof(glf::vertex_v2fc4d), BUFFER_OFFSET(sizeof(glm::vec2)));
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION + 0);
            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION + 1);
            gl4.glEnableVertexAttribArray(Semantic.Attr.COLOR);
        }
        gl4.glBindVertexArray(0);

        /*
		std::vector<glf::vertexattrib> Valid(16); 
		Valid[semantic::attr::POSITION + 0] = glf::vertexattrib(GL_TRUE, 2, sizeof(glf::vertex_v2fc4d), GL_FLOAT, GL_FALSE, GL_FALSE, GL_FALSE, 0, NULL);
		Valid[semantic::attr::POSITION + 1] = glf::vertexattrib(GL_TRUE, 2, sizeof(glf::vertex_v2fc4d), GL_FLOAT, GL_FALSE, GL_FALSE, GL_FALSE, 0, NULL);
		Valid[semantic::attr::COLOR] = glf::vertexattrib(GL_TRUE, 4, sizeof(glf::vertex_v2fc4d), GL_DOUBLE, GL_FALSE, GL_FALSE, GL_FALSE, 0, BUFFER_OFFSET(sizeof(glm::vec2)));

		// TODO
		//glf::validateVAO(VertexArrayName, Valid);
         */
        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

        {
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
            ByteBuffer pointer = gl4.glMapBufferRange(GL_UNIFORM_BUFFER, 0, Mat4.SIZE,
                    GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, (float) windowSize.x / windowSize.y, 0.1f, 100.0f);
            pointer.asFloatBuffer().put(projection.mul(viewMat4()).mul(new Mat4(1.0f)).toFa_());

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
}
