/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_410;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2GL3.GL_LINE;
import static com.jogamp.opengl.GL3ES3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.nio.FloatBuffer;

/**
 *
 * @author GBarbieri
 */
public class Gl_410_primitive_tessellation5 extends Test {

    public static void main(String[] args) {
        Gl_410_primitive_tessellation5 gl_410_primitive_tessellation5 = new Gl_410_primitive_tessellation5();
    }

    public Gl_410_primitive_tessellation5() {
        super("gl-410-primitive-tessellation5", Profile.CORE, 4, 1);
    }

    private final String SHADERS_SOURCE = "tess";
    private final String SHADERS_ROOT = "src/data/gl_410";

    private int vertexCount = 4;
    private int vertexSize = vertexCount * (2 + 4) * Float.BYTES;
    private float[] vertexData = {
        -1.0f, -1.0f, 1.0f, 0.0f, 0.0f, 1.0f,
        +1.0f, -1.0f, 1.0f, 1.0f, 0.0f, 1.0f,
        +1.0f, +1.0f, 0.0f, 1.0f, 0.0f, 1.0f,
        -1.0f, +1.0f, 0.0f, 0.0f, 1.0f, 1.0f};

    private enum Program {
        VERT,
        CONT,
        EVAL,
        GEOM,
        FRAG,
        MAX
    }

    private int[] pipelineName = {0}, programName = new int[Program.MAX.ordinal()], arrayBufferName = {0},
            vertexArrayName = {0};
    private int uniformMvp;
    private float[] projection = new float[16], model = new float[16], mvp = new float[16];

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;

        if (validated) {
            validated = initProgram(gl4);
        }
        if (validated) {
            validated = initArrayBuffer(gl4);
        }
        if (validated) {
            validated = initVertexArray(gl4);
        }

        return validated && checkError(gl4, "begin");
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        gl4.glGenProgramPipelines(1, pipelineName, 0);
        gl4.glBindProgramPipeline(pipelineName[0]);
        gl4.glBindProgramPipeline(0);

        // Create program
        if (validated) {

            ShaderProgram[] shaderPrograms = new ShaderProgram[Program.MAX.ordinal()];

            ShaderCode[] shaderCodes = new ShaderCode[]{
                ShaderCode.create(gl4, GL_VERTEX_SHADER, this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE,
                "vert", null, true),
                ShaderCode.create(gl4, GL_TESS_CONTROL_SHADER, this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE,
                "cont", null, true),
                ShaderCode.create(gl4, GL_TESS_EVALUATION_SHADER, this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE,
                "eval", null, true),
                ShaderCode.create(gl4, GL_GEOMETRY_SHADER, this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE,
                "geom", null, true),
                ShaderCode.create(gl4, GL_FRAGMENT_SHADER, this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE,
                "frag", null, true)};

            for (int i = 0; i < Program.MAX.ordinal(); i++) {

                shaderPrograms[i] = new ShaderProgram();
                shaderPrograms[i].init(gl4);
                shaderPrograms[i].add(shaderCodes[i]);
                programName[i] = shaderPrograms[i].program();
                gl4.glProgramParameteri(programName[i], GL_PROGRAM_SEPARABLE, GL_TRUE);
                shaderPrograms[i].link(gl4, System.out);
            }
        }

        if (validated) {

            gl4.glUseProgramStages(pipelineName[0], GL_VERTEX_SHADER_BIT, programName[Program.VERT.ordinal()]);
            gl4.glUseProgramStages(pipelineName[0], GL_TESS_CONTROL_SHADER_BIT, programName[Program.CONT.ordinal()]);
            gl4.glUseProgramStages(pipelineName[0], GL_TESS_EVALUATION_SHADER_BIT, programName[Program.EVAL.ordinal()]);
            gl4.glUseProgramStages(pipelineName[0], GL_GEOMETRY_SHADER_BIT, programName[Program.GEOM.ordinal()]);
            gl4.glUseProgramStages(pipelineName[0], GL_FRAGMENT_SHADER_BIT, programName[Program.FRAG.ordinal()]);

        }

        // Get variables locations
        if (validated) {

            uniformMvp = gl4.glGetUniformLocation(programName[Program.VERT.ordinal()], "mvp");
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(1, vertexArrayName, 0);
        gl4.glBindVertexArray(vertexArrayName[0]);
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, arrayBufferName[0]);
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, (2 + 4) * Float.BYTES, 0);
            gl4.glVertexAttribPointer(Semantic.Attr.COLOR, 4, GL_FLOAT, false, (2 + 4) * Float.BYTES, 2 * Float.BYTES);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl4.glEnableVertexAttribArray(Semantic.Attr.COLOR);
        }
        gl4.glBindVertexArray(0);

        return checkError(gl4, "initVertexArray");
    }

    private boolean initArrayBuffer(GL4 gl4) {

        // Generate a buffer object
        gl4.glGenBuffers(1, arrayBufferName, 0);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, arrayBufferName[0]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl4.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        return checkError(gl4, "initArrayBuffer");
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
        gl4.glDrawArraysInstancedBaseInstance(GL_PATCHES, 0, vertexCount, 1, 0);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteVertexArrays(1, vertexArrayName, 0);
        gl4.glDeleteBuffers(1, arrayBufferName, 0);
        for (int i = 0; i < Program.MAX.ordinal(); ++i) {
            gl4.glDeleteProgram(programName[i]);
        }

        return checkError(gl4, "end");
    }
}
