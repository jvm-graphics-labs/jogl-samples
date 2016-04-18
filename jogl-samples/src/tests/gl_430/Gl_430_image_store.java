/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_430;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2ES3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import framework.BufferUtils;
import java.nio.IntBuffer;

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

    private class Pipeline {

        public static final int READ = 0;
        public static final int SAVE = 1;
        public static final int MAX = 2;
    }

    private IntBuffer vertexArrayName = GLBuffers.newDirectIntBuffer(1), textureName = GLBuffers.newDirectIntBuffer(1),
            pipelineName = GLBuffers.newDirectIntBuffer(Pipeline.MAX);
    private int[] programName = new int[Pipeline.MAX];

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;
        validated = validated && gl4.isExtensionAvailable("GL_ARB_shader_image_size");

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
            programName[Pipeline.READ] = shaderProgram.program();
            gl4.glProgramParameteri(programName[Pipeline.READ], GL_PROGRAM_SEPARABLE, GL_TRUE);
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
            programName[Pipeline.SAVE] = shaderProgram.program();
            gl4.glProgramParameteri(programName[Pipeline.SAVE], GL_PROGRAM_SEPARABLE, GL_TRUE);
            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);
            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            gl4.glGenProgramPipelines(Pipeline.MAX, pipelineName);
            gl4.glUseProgramStages(pipelineName.get(Pipeline.READ), GL_VERTEX_SHADER_BIT | GL_FRAGMENT_SHADER_BIT,
                    programName[Pipeline.READ]);
            gl4.glUseProgramStages(pipelineName.get(Pipeline.SAVE), GL_VERTEX_SHADER_BIT | GL_FRAGMENT_SHADER_BIT,
                    programName[Pipeline.SAVE]);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initTexture(GL4 gl4) {

        gl4.glGenTextures(1, textureName);
        gl4.glActiveTexture(GL_TEXTURE0);
        gl4.glBindTexture(GL_TEXTURE_2D, textureName.get(0));
        gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
        gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
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

        gl4.glGenVertexArrays(1, vertexArrayName);
        gl4.glBindVertexArray(vertexArrayName.get(0));
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

            gl4.glBindProgramPipeline(pipelineName.get(Pipeline.SAVE));
            gl4.glBindImageTexture(Semantic.Image.DIFFUSE, textureName.get(0), 0, false, 0, GL_WRITE_ONLY, GL_RGBA8);
            gl4.glBindVertexArray(vertexArrayName.get(0));
            gl4.glDrawArraysInstancedBaseInstance(GL_TRIANGLES, 0, 3, 1, 0);
        }

        // Read from image
        {
            gl4.glDrawBuffer(GL_BACK);

            gl4.glBindProgramPipeline(pipelineName.get(Pipeline.READ));
            gl4.glBindImageTexture(Semantic.Image.DIFFUSE, textureName.get(0), 0, false, 0, GL_READ_ONLY, GL_RGBA8);
            gl4.glBindVertexArray(vertexArrayName.get(0));
            gl4.glDrawArraysInstancedBaseInstance(GL_TRIANGLES, 0, 3, 1, 0);
        }

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteTextures(1, textureName);
        BufferUtils.destroyDirectBuffer(textureName);
        gl4.glDeleteVertexArrays(1, vertexArrayName);
        BufferUtils.destroyDirectBuffer(vertexArrayName);
        gl4.glDeleteProgram(programName[Pipeline.READ]);
        gl4.glDeleteProgram(programName[Pipeline.SAVE]);
        gl4.glDeleteProgramPipelines(Pipeline.MAX, pipelineName);
        BufferUtils.destroyDirectBuffer(pipelineName);

        return true;
    }
}
