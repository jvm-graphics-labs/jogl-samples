/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_420;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_DONT_CARE;
import static com.jogamp.opengl.GL2ES2.GL_DEBUG_SEVERITY_LOW;
import static com.jogamp.opengl.GL2ES2.GL_DEBUG_SOURCE_APPLICATION;
import static com.jogamp.opengl.GL3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.BufferUtils;
import glm.glm;
import glm.mat._4.Mat4;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import framework.VertexAttrib;
import glf.Vertex_v2fc4f;
import glm.vec._2.Vec2;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

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
    private int vertexSize = vertexCount * Vertex_v2fc4f.SIZE;
    private float[] vertexData = {
        -1.0f, -1.0f,/**/ 1.0f, 0.0f, 0.0f, 1.0f,
        +1.0f, -1.0f,/**/ 1.0f, 1.0f, 0.0f, 1.0f,
        +1.0f, +1.0f,/**/ 0.0f, 1.0f, 0.0f, 1.0f,
        -1.0f, +1.0f,/**/ 0.0f, 0.0f, 1.0f, 1.0f};

    private class Program {

        public static final int VERT = 0;
        public static final int FRAG = 1;
        public static final int MAX = 2;
    }

    private IntBuffer pipelineName = GLBuffers.newDirectIntBuffer(1), vertexArrayName = GLBuffers.newDirectIntBuffer(1),
            bufferName = GLBuffers.newDirectIntBuffer(1);
    private int[] programName = new int[Program.MAX];
    private int uniformMvp;

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

        gl4.glGenProgramPipelines(1, pipelineName);

        // Create program
        if (validated) {

            ShaderProgram[] shaderPrograms = new ShaderProgram[Program.MAX];
            for (int i = 0; i < Program.MAX; i++) {
                shaderPrograms[i] = new ShaderProgram();
            }

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER, this.getClass(), SHADERS_ROOT,
                    null, SHADERS_SOURCE, "vert", null, true);
            ShaderCode contShaderCode = ShaderCode.create(gl4, GL_TESS_CONTROL_SHADER, this.getClass(), SHADERS_ROOT,
                    null, SHADERS_SOURCE, "cont", null, true);
            ShaderCode evalShaderCode = ShaderCode.create(gl4, GL_TESS_EVALUATION_SHADER, this.getClass(), SHADERS_ROOT,
                    null, SHADERS_SOURCE, "eval", null, true);
            ShaderCode geomShaderCode = ShaderCode.create(gl4, GL_GEOMETRY_SHADER, this.getClass(), SHADERS_ROOT,
                    null, SHADERS_SOURCE, "geom", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER, this.getClass(), SHADERS_ROOT,
                    null, SHADERS_SOURCE, "frag", null, true);

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

            gl4.glUseProgramStages(pipelineName.get(0), GL_VERTEX_SHADER_BIT | GL_TESS_CONTROL_SHADER_BIT
                    | GL_TESS_EVALUATION_SHADER_BIT | GL_GEOMETRY_SHADER_BIT, programName[Program.VERT]);
            gl4.glUseProgramStages(pipelineName.get(0), GL_FRAGMENT_SHADER_BIT, programName[Program.FRAG]);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(1, vertexArrayName);
        gl4.glBindVertexArray(vertexArrayName.get(0));
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(0));
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION + 0, 2, GL_FLOAT, false, Vertex_v2fc4f.SIZE, 0);
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION + 1, 2, GL_FLOAT, false, Vertex_v2fc4f.SIZE, 0);
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
        valid[Semantic.Attr.POSITION + 0] = new VertexAttrib(GL_TRUE, 0, 2, Vertex_v2fc4f.SIZE, GL_FLOAT, false,
                GL_FALSE, GL_FALSE, 0, 0);
        valid[Semantic.Attr.POSITION + 1] = new VertexAttrib(GL_TRUE, 0, 2, Vertex_v2fc4f.SIZE, GL_FLOAT, false,
                GL_FALSE, GL_FALSE, 0, 0);
        valid[Semantic.Attr.COLOR] = new VertexAttrib(GL_TRUE, 0, 4, Vertex_v2fc4f.SIZE, GL_FLOAT, false, GL_FALSE,
                GL_FALSE, 0, Vec2.SIZE);
        validate(gl4, vertexArrayName.get(0), valid);

        return true;
    }

    private boolean initBuffer(GL4 gl4) {

        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);

        gl4.glGenBuffers(1, bufferName);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(0));
        gl4.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        BufferUtils.destroyDirectBuffer(vertexBuffer);

        return true;
    }

    private boolean initMax(GL4 gl4) {

        final int maxVertexAttribs = 0, maxVertexOutput = 1, maxControlInput = 2, maxControlOutput = 3,
                maxControlTotalOutput = 4, maxEvaluationInput = 5, maxEvaluationOutput = 6, maxGeometryInput = 7,
                maxGeometryOutput = 8, maxFragmentInput = 9, max = 10;
        IntBuffer data = GLBuffers.newDirectIntBuffer(max);

        data.position(maxVertexAttribs);
        gl4.glGetIntegerv(GL_MAX_VERTEX_ATTRIBS, data);
        //GL_MAX_DRAW_BUFFERS 8
        //GL_MAX_COLOR_ATTACHMENTS 8
        data.position(maxVertexOutput);
        gl4.glGetIntegerv(GL_MAX_VERTEX_OUTPUT_COMPONENTS, data);
        data.position(maxControlInput);
        gl4.glGetIntegerv(GL_MAX_TESS_CONTROL_INPUT_COMPONENTS, data);
        data.position(maxControlOutput);
        gl4.glGetIntegerv(GL_MAX_TESS_CONTROL_OUTPUT_COMPONENTS, data);
        data.position(maxControlTotalOutput);
        gl4.glGetIntegerv(GL_MAX_TESS_CONTROL_TOTAL_OUTPUT_COMPONENTS, data);
        data.position(maxEvaluationInput);
        gl4.glGetIntegerv(GL_MAX_TESS_EVALUATION_INPUT_COMPONENTS, data);
        data.position(maxEvaluationOutput);
        gl4.glGetIntegerv(GL_MAX_TESS_EVALUATION_OUTPUT_COMPONENTS, data);
        data.position(maxGeometryInput);
        gl4.glGetIntegerv(GL_MAX_GEOMETRY_INPUT_COMPONENTS, data);
        data.position(maxGeometryOutput);
        gl4.glGetIntegerv(GL_MAX_GEOMETRY_OUTPUT_COMPONENTS, data);
        data.position(maxFragmentInput);
        gl4.glGetIntegerv(GL_MAX_FRAGMENT_INPUT_COMPONENTS, data);

        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

        Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, (float) windowSize.x / windowSize.y, 0.1f, 100.0f);
        Mat4 model = new Mat4(1.0f);
        Mat4 mvp = projection.mul(viewMat4()).mul(model);

        gl4.glProgramUniformMatrix4fv(programName[Program.VERT], uniformMvp, 1, false, mvp.toFa_(), 0);

        gl4.glViewportIndexedfv(0, viewport.put(0, 0).put(1, 0).put(2, windowSize.x).put(3, windowSize.y));
        gl4.glClearBufferfv(GL_COLOR, 0, clearColor.put(0, 0).put(1, 0).put(2, 0).put(3, 0));

        gl4.glBindProgramPipeline(pipelineName.get(0));

        gl4.glBindVertexArray(vertexArrayName.get(0));
        gl4.glPatchParameteri(GL_PATCH_VERTICES, vertexCount);

        assert (!validate(gl4, programName[Program.VERT]));
        gl4.glDrawArraysInstancedBaseInstance(GL_PATCHES, 0, vertexCount, 1, 0);

        return true;
    }

    private boolean validate(GL4 gl4, int programName) {

        boolean error = false;

        // Pipeline object validation
        {
            IntBuffer status = GLBuffers.newDirectIntBuffer(1);
            IntBuffer lengthMax = GLBuffers.newDirectIntBuffer(1);
            gl4.glValidateProgramPipeline(pipelineName.get(0));
            gl4.glGetProgramPipelineiv(pipelineName.get(0), GL_VALIDATE_STATUS, status);
            gl4.glGetProgramPipelineiv(pipelineName.get(0), GL_INFO_LOG_LENGTH, lengthMax);

            IntBuffer lengthQuery = GLBuffers.newDirectIntBuffer(1);
            ByteBuffer infoLog = GLBuffers.newDirectByteBuffer(lengthMax.get(0) + 1);
            gl4.glGetProgramPipelineInfoLog(pipelineName.get(0), infoLog.capacity(), lengthQuery, infoLog);

            byte[] buf = new byte[infoLog.capacity()];
            infoLog.get(buf);

            gl4.glDebugMessageInsert(
                    GL_DEBUG_SOURCE_APPLICATION,
                    GL_DEBUG_TYPE_OTHER, 76,
                    GL_DEBUG_SEVERITY_LOW,
                    lengthQuery.get(0),
                    new String(buf));
        }

        IntBuffer activeAttributeMaxLength = GLBuffers.newDirectIntBuffer(1);
        IntBuffer activeAttribute = GLBuffers.newDirectIntBuffer(1);
        gl4.glGetProgramiv(programName, GL_ACTIVE_ATTRIBUTE_MAX_LENGTH, activeAttributeMaxLength);
        gl4.glGetProgramiv(programName, GL_ACTIVE_ATTRIBUTES, activeAttribute);

        IntBuffer attribLength = GLBuffers.newDirectIntBuffer(1);
        IntBuffer attribSize = GLBuffers.newDirectIntBuffer(1);
        IntBuffer attribType = GLBuffers.newDirectIntBuffer(1);
        ByteBuffer attribName = GLBuffers.newDirectByteBuffer(activeAttributeMaxLength.get(0));

        for (int i = 0; i < activeAttribute.get(0); ++i) {

            gl4.glGetActiveAttrib(programName,
                    i,
                    activeAttributeMaxLength.get(0),
                    attribLength,
                    attribSize,
                    attribType,
                    attribName);

            byte[] nameByte = new byte[attribName.capacity()];
            attribName.get(nameByte).position(0);
            String nameString = new String(nameByte);
            // remove spaces at the end
            nameString = nameString.trim();

            int attribLocation = gl4.glGetAttribLocation(programName, nameString);

            VertexAttrib vertexAttrib = new VertexAttrib();
            IntBuffer value = GLBuffers.newDirectIntBuffer(1);
            gl4.glGetVertexAttribiv(attribLocation, GL_VERTEX_ATTRIB_ARRAY_ENABLED, value);
            vertexAttrib.enabled = value.get(0);
            //glGetVertexAttribiv(AttribLocation, GL_VERTEX_ATTRIB_ARRAY_BUFFER_BINDING, &VertexAttrib.Binding);
            gl4.glGetVertexAttribiv(attribLocation, GL_VERTEX_ATTRIB_ARRAY_SIZE, value);
            vertexAttrib.size = value.get(0);
            gl4.glGetVertexAttribiv(attribLocation, GL_VERTEX_ATTRIB_ARRAY_STRIDE, value);
            vertexAttrib.stride = value.get(0);
            gl4.glGetVertexAttribiv(attribLocation, GL_VERTEX_ATTRIB_ARRAY_TYPE, value);
            vertexAttrib.type = value.get(0);
            gl4.glGetVertexAttribiv(attribLocation, GL_VERTEX_ATTRIB_ARRAY_NORMALIZED, value);
            vertexAttrib.normalized = value.get(0) == GL_TRUE;
            gl4.glGetVertexAttribiv(attribLocation, GL_VERTEX_ATTRIB_ARRAY_INTEGER, value);
            vertexAttrib.integer = value.get(0);
            gl4.glGetVertexAttribiv(attribLocation, GL_VERTEX_ATTRIB_ARRAY_DIVISOR, value);
            vertexAttrib.divisor = value.get(0);

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
            }

            System.out.println("glGetActiveAttrib(" + i + ", " + attribLocation + ", " + attribLength.get(0)
                    + ", " + attribSize.get(0) + ", " + attribType.get(0) + ", " + nameString + ")");
        }

        return error;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteVertexArrays(1, vertexArrayName);
        gl4.glDeleteBuffers(1, bufferName);
        for (int i = 0; i < Program.MAX; i++) {
            gl4.glDeleteProgram(programName[i]);
        }

        return true;
    }
}
