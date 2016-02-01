/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_430;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2ES3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import jglm.Vec2i;

/**
 *
 * @author elect
 */
public class Gl_430_image_store extends Test {

    public static void main(String[] args) {
        Gl_430_image_store gl_430_image_store = new Gl_430_image_store();
    }

    public Gl_430_image_store() {
        super("gl-430-image-store", Profile.CORE, 4, 3);
    }

    private final String SHADERS_SOURCE_SAVE = "image-store-write";
    private final String SHADERS_SOURCE_READ = "image-store-read";
    private final String SHADERS_ROOT = "src/data/gl_430";

    private enum Pipeline {
        READ,
        SAVE,
        MAX
    }

    private int[] vertexArrayName = {0}, textureName = {0}, pipelineName = new int[Pipeline.MAX.ordinal()],
            programName = new int[Pipeline.MAX.ordinal()];
    private Vec2i imageSize = new Vec2i();

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;
        validated = validated && checkExtension(gl4, "GL_ARB_shader_image_size");

        logImplementationDependentLimit(gl4, GL_MAX_TEXTURE_IMAGE_UNITS, "GL_MAX_TEXTURE_IMAGE_UNITS");

        if (validated) {
            validated = initTexture(gl4);
        }
        if (validated) {
            validated = initProgram(gl4);
        }
        if (validated) {
            validated = initVertexArray(gl4);
        }

        return validated;
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        // Create program
        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_READ, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_READ, "frag", null, true);

            shaderProgram.init(gl4);
            programName[Pipeline.READ.ordinal()] = shaderProgram.program();
            gl4.glProgramParameteri(programName[Pipeline.READ.ordinal()], GL_PROGRAM_SEPARABLE, GL_TRUE);
            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);
            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_SAVE, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_SAVE, "frag", null, true);

            shaderProgram.init(gl4);
            programName[Pipeline.SAVE.ordinal()] = shaderProgram.program();
            gl4.glProgramParameteri(programName[Pipeline.SAVE.ordinal()], GL_PROGRAM_SEPARABLE, GL_TRUE);
            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);
            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            gl4.glGenProgramPipelines(Pipeline.MAX.ordinal(), pipelineName, 0);
            gl4.glUseProgramStages(pipelineName[Pipeline.READ.ordinal()], GL_VERTEX_SHADER_BIT
                    | GL_FRAGMENT_SHADER_BIT, programName[Pipeline.READ.ordinal()]);
            gl4.glUseProgramStages(pipelineName[Pipeline.SAVE.ordinal()], GL_VERTEX_SHADER_BIT
                    | GL_FRAGMENT_SHADER_BIT, programName[Pipeline.SAVE.ordinal()]);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initTexture(GL4 gl4) {

        gl4.glGenTextures(1, textureName, 0);
        gl4.glActiveTexture(GL_TEXTURE0);
        gl4.glBindTexture(GL_TEXTURE_2D, textureName[0]);
        gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
        gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 1);
        gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_R, GL_RED);
        gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_G, GL_GREEN);
        gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_B, GL_BLUE);
        gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_A, GL_ALPHA);
        gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        gl4.glTexStorage2D(GL_TEXTURE_2D, 1, GL_RGBA8, windowSize.x, windowSize.y);

        return true;
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(1, vertexArrayName, 0);
        gl4.glBindVertexArray(vertexArrayName[0]);
        gl4.glBindVertexArray(0);

        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glViewportIndexedf(0, 0, 0, windowSize.x, windowSize.y);
        gl4.glDrawBuffer(GL_BACK);
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{0.0f, 0.5f, 1.0f, 1.0f}, 0);

        // Renderer to image
        {
            gl4.glDrawBuffer(GL_NONE);

            gl4.glBindProgramPipeline(pipelineName[Pipeline.SAVE.ordinal()]);
            gl4.glBindImageTexture(Semantic.Image.DIFFUSE, textureName[0], 0,
                    false, 0, GL_WRITE_ONLY, GL_RGBA8);
            gl4.glBindVertexArray(vertexArrayName[0]);
            gl4.glDrawArraysInstancedBaseInstance(GL_TRIANGLES, 0, 3, 1, 0);
        }

        // Read from image
        {
            gl4.glDrawBuffer(GL_BACK);

            gl4.glBindProgramPipeline(pipelineName[Pipeline.READ.ordinal()]);
            gl4.glBindImageTexture(Semantic.Image.DIFFUSE, textureName[0], 0,
                    false, 0, GL_READ_ONLY, GL_RGBA8);
            gl4.glBindVertexArray(vertexArrayName[0]);
            gl4.glDrawArraysInstancedBaseInstance(GL_TRIANGLES, 0, 3, 1, 0);
        }

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteTextures(1, textureName, 0);
        gl4.glDeleteVertexArrays(1, vertexArrayName, 0);
        gl4.glDeleteProgram(programName[Pipeline.READ.ordinal()]);
        gl4.glDeleteProgram(programName[Pipeline.SAVE.ordinal()]);
        gl4.glDeleteProgramPipelines(Pipeline.MAX.ordinal(), pipelineName, 0);

        return true;
    }
}
