/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_430;

import static com.jogamp.opengl.GL.GL_TRUE;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER_BIT;
import static com.jogamp.opengl.GL2ES2.GL_PROGRAM_SEPARABLE;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER_BIT;
import static com.jogamp.opengl.GL3ES3.GL_COMPUTE_SHADER;
import static com.jogamp.opengl.GL3ES3.GL_COMPUTE_SHADER_BIT;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.Profile;
import framework.Test;
import glm.vec._2.Vec2;
import glm.vec._2.i.Vec2i;
import java.nio.IntBuffer;

/**
 *
 * @author GBarbieri
 */
public class Gl_430_program_compute_variable_group_size extends Test {

    public static void main(String[] args) {
        Gl_430_program_compute_variable_group_size gl_430_program_compute_variable_group_size
                = new Gl_430_program_compute_variable_group_size();
    }

    public Gl_430_program_compute_variable_group_size() {
        super("gl-430-program-compute-variable-group-size", Profile.CORE, 4, 3);
    }

    private final String SHADERS_SOURCE = "program-compute-variable-group-size";
    private final String SHADERS_ROOT = "src/data/gl_430";
    private final String TEXTURE_DIFFUSE = "kueken7_rgba8_srgb.dds";

    private class Program {

        public static final int GRAPHICS = 0;
        public static final int COMPUTE = 1;
        public static final int MAX = 2;
    }

    private IntBuffer pipelineName = GLBuffers.newDirectIntBuffer(Program.MAX);
    private int[] programName = new int[Program.MAX];

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        gl4.glGenProgramPipelines(Program.MAX, pipelineName);

        // Create program
        if (validated) {

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);
            ShaderCode compShaderCode = ShaderCode.create(gl4, GL_COMPUTE_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "comp", null, true);

            if (validated) {

                ShaderProgram shaderProgram = new ShaderProgram();
                shaderProgram.init(gl4);

                programName[Program.GRAPHICS] = shaderProgram.program();

                gl4.glProgramParameteri(programName[Program.GRAPHICS], GL_PROGRAM_SEPARABLE, GL_TRUE);

                shaderProgram.add(vertShaderCode);
                shaderProgram.add(fragShaderCode);

                shaderProgram.link(gl4, System.out);
            }

            if (validated) {

                ShaderProgram shaderProgram = new ShaderProgram();
                shaderProgram.init(gl4);

                programName[Program.COMPUTE] = shaderProgram.program();

                gl4.glProgramParameteri(programName[Program.COMPUTE], GL_PROGRAM_SEPARABLE, GL_TRUE);

                shaderProgram.add(compShaderCode);

                shaderProgram.link(gl4, System.out);
            }
        }

        if (validated) {

            gl4.glUseProgramStages(pipelineName.get(Program.GRAPHICS), GL_VERTEX_SHADER_BIT | GL_FRAGMENT_SHADER_BIT,
                    programName[Program.GRAPHICS]);
            gl4.glUseProgramStages(pipelineName.get(Program.COMPUTE), GL_COMPUTE_SHADER_BIT, programName[Program.COMPUTE]);
        }

        return validated & checkError(gl4, "initProgram");
    }
}
