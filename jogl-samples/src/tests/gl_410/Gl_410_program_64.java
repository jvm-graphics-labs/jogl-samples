/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_410;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2GL3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import dev.Vec3d;
import framework.BufferUtils;
import glm.glm;
import glm.mat._4.Mat4d;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.DoubleBuffer;
import java.nio.ShortBuffer;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author GBarbieri
 */
public class Gl_410_program_64 extends Test {

    public static void main(String[] args) {
        Gl_410_program_64 gl_410_program_64 = new Gl_410_program_64();
    }

    public Gl_410_program_64() {
        super("gl-410-program-64", Profile.CORE, 4, 1);
    }

    private final String SHADERS_SOURCE = "double";
    private final String SHADERS_ROOT = "src/data/gl_410";

    private int vertexCount = 4;
    private int positionSize = vertexCount * Vec3d.SIZE;
    private double[] positionData = {
        -1.0, -1.0, +0.0,
        +1.0, -1.0, +0.0,
        +1.0, +1.0, +0.0,
        -1.0, +1.0, +0.0};

    private int elementCount = 6;
    private int elementSize = elementCount * Short.BYTES;
    private short[] elementData = {
        0, 1, 2,
        0, 2, 3};

    private class Buffer {

        public static final int F64 = 0;
        public static final int ELEMENT = 1;
        public static final int MAX = 2;
    }

    private class Program {

        public static final int VERT = 0;
        public static final int FRAG = 1;
        public static final int MAX = 2;
    }

    private int[] pipelineName = {0}, programName = new int[Program.MAX], bufferName = new int[Buffer.MAX],
            vertexArrayName = {0};
    private int uniformMvp, uniformDiffuse;

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;

        if (validated) {
            validated = initProgram(gl4);
        }
        if (validated) {
            validated = initVertexBuffer(gl4);
        }
        if (validated) {
            validated = initVertexArray(gl4);
        }

        return validated;
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        try {

            if (validated) {

                String[] vertexSourceContent = new String[]{new Scanner(new File(SHADERS_ROOT + "/" + SHADERS_SOURCE
                    + ".vert")).useDelimiter("\\A").next()};
                programName[Program.VERT] = gl4.glCreateShaderProgramv(GL_VERTEX_SHADER, 1, vertexSourceContent);
            }

            if (validated) {

                String[] fragmentSourceContent = new String[]{new Scanner(new File(SHADERS_ROOT + "/" + SHADERS_SOURCE
                    + ".frag")).useDelimiter("\\A").next()};
                programName[Program.FRAG] = gl4.glCreateShaderProgramv(GL_FRAGMENT_SHADER, 1, fragmentSourceContent);
            }

            if (validated) {

                validated = validated && framework.Compiler.checkProgram(gl4, programName[Program.VERT]);
                validated = validated && framework.Compiler.checkProgram(gl4, programName[Program.FRAG]);
            }

            if (validated) {

                uniformMvp = gl4.glGetUniformLocation(programName[Program.VERT], "mvp");
                uniformDiffuse = gl4.glGetUniformLocation(programName[Program.FRAG], "diffuse");
            }

            if (validated) {

                gl4.glGenProgramPipelines(1, pipelineName, 0);
                gl4.glBindProgramPipeline(pipelineName[0]);
                gl4.glUseProgramStages(pipelineName[0], GL_VERTEX_SHADER_BIT, programName[Program.VERT]);
                gl4.glUseProgramStages(pipelineName[0], GL_FRAGMENT_SHADER_BIT, programName[Program.FRAG]);
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Gl_410_program_64.class.getName()).log(Level.SEVERE, null, ex);
        }

        return validated && checkError(gl4, "initProgram");
    }

    private boolean initVertexBuffer(GL4 gl4) {

        gl4.glGenBuffers(Buffer.MAX, bufferName, 0);

        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT]);
        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(elementBuffer);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.F64]);
        DoubleBuffer positionBuffer = GLBuffers.newDirectDoubleBuffer(positionData);
        gl4.glBufferData(GL_ARRAY_BUFFER, positionSize, positionBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(elementBuffer);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        return checkError(gl4, "initArrayBuffer");
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(1, vertexArrayName, 0);

        gl4.glBindVertexArray(vertexArrayName[0]);
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.F64]);
            gl4.glVertexAttribLPointer(Semantic.Attr.POSITION, 3, GL_DOUBLE, 3 * Double.BYTES, 0);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
        }
        gl4.glBindVertexArray(0);

        return checkError(gl4, "initVertexArray");
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        Mat4d projection = glm.perspective_(Math.PI * 0.25, (double) windowSize.x / windowSize.y, 0.1, 100.0);
        Mat4d view = new Mat4d(viewMat4());
        Mat4d model = new Mat4d(1.0);
        Mat4d mvp = projection.mul(view).mul(model);

        gl4.glProgramUniformMatrix4dv(programName[Program.VERT], uniformMvp, 1, false, mvp.toDa_(), 0);
        gl4.glProgramUniform4dv(programName[Program.FRAG], uniformDiffuse, 1, new double[]{1.0, 0.5, 0.0, 1.0}, 0);

        gl4.glViewportIndexedfv(0, new float[]{0, 0, windowSize.x, windowSize.y}, 0);
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{0.0f, 0.0f, 0.0f, 0.0f}, 0);

        gl4.glBindProgramPipeline(pipelineName[0]);

        gl4.glBindVertexArray(vertexArrayName[0]);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT]);
        gl4.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteBuffers(Buffer.MAX, bufferName, 0);
        gl4.glDeleteVertexArrays(1, vertexArrayName, 0);
        gl4.glDeleteProgram(programName[Program.VERT]);
        gl4.glDeleteProgram(programName[Program.FRAG]);
        gl4.glBindProgramPipeline(0);
        gl4.glDeleteProgramPipelines(1, pipelineName, 0);

        return true;
    }
}
