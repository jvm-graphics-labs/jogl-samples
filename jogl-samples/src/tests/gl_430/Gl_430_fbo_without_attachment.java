/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_430;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2GL3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jgli.Texture2d;

/**
 *
 * @author GBarbieri
 */
public class Gl_430_fbo_without_attachment extends Test {

    public static void main(String[] args) {
        Gl_430_fbo_without_attachment gl_430_fbo_without_attachment = new Gl_430_fbo_without_attachment();
    }

    public Gl_430_fbo_without_attachment() {
        super("gl-430-fbo-without-attachment", Profile.CORE, 4, 3);
    }

    private final String SHADERS_SOURCE_RENDER = "fbo-render";
    private final String SHADERS_SOURCE_SPLASH = "fbo-splash";
    private final String SHADERS_ROOT = "src/data/gl_430";
    private final String TEXTURE_DIFFUSE = "kueken7_rgba8_srgb.dds";

    private int vertexCount = 4;

    private class Texture {

        public static final int DIFFUSE = 0;
        public static final int COLORBUFFER = 1;
        public static final int MAX = 2;
    }

    private class Pipeline {

        public static final int RENDER = 0;
        public static final int SPLASH = 1;
        public static final int MAX = 2;
    }

    private int[] vertexArrayName = {0}, framebufferName = {0}, samplerName = {0}, pipelineName = new int[Pipeline.MAX],
            programName = new int[Pipeline.MAX], textureName = new int[Texture.MAX];

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;
        validated = validated && checkExtension(gl4, "GL_ARB_framebuffer_no_attachments");

        if (validated) {
            validated = initProgram(gl4);
        }
        if (validated) {
            validated = initVertexArray(gl4);
        }
        if (validated) {
            validated = initTexture(gl4);
        }
        if (validated) {
            validated = initSampler(gl4);
        }
        if (validated) {
            validated = initFramebuffer(gl4);
        }

        return validated;
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        // Create program
        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_RENDER, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_RENDER, "frag", null, true);

            shaderProgram.init(gl4);
            programName[Pipeline.RENDER] = shaderProgram.program();
            gl4.glProgramParameteri(programName[Pipeline.RENDER], GL_PROGRAM_SEPARABLE, GL_TRUE);
            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);
            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_SPLASH, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_SPLASH, "frag", null, true);

            shaderProgram.init(gl4);
            programName[Pipeline.SPLASH] = shaderProgram.program();
            gl4.glProgramParameteri(programName[Pipeline.SPLASH], GL_PROGRAM_SEPARABLE, GL_TRUE);
            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);
            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            gl4.glGenProgramPipelines(Pipeline.MAX, pipelineName, 0);
            gl4.glUseProgramStages(pipelineName[Pipeline.RENDER], GL_VERTEX_SHADER_BIT
                    | GL_FRAGMENT_SHADER_BIT, programName[Pipeline.RENDER]);
            gl4.glUseProgramStages(pipelineName[Pipeline.SPLASH], GL_VERTEX_SHADER_BIT
                    | GL_FRAGMENT_SHADER_BIT, programName[Pipeline.SPLASH]);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initSampler(GL4 gl4) {

        gl4.glGenSamplers(1, samplerName, 0);
        gl4.glSamplerParameteri(samplerName[0], GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_NEAREST);
        gl4.glSamplerParameteri(samplerName[0], GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        gl4.glSamplerParameteri(samplerName[0], GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        gl4.glSamplerParameteri(samplerName[0], GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        gl4.glSamplerParameteri(samplerName[0], GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        gl4.glSamplerParameterfv(samplerName[0], GL_TEXTURE_BORDER_COLOR, new float[]{0.0f, 0.0f, 0.0f, 0.0f}, 0);
        gl4.glSamplerParameterf(samplerName[0], GL_TEXTURE_MIN_LOD, -1000.f);
        gl4.glSamplerParameterf(samplerName[0], GL_TEXTURE_MAX_LOD, 1000.f);
        gl4.glSamplerParameterf(samplerName[0], GL_TEXTURE_LOD_BIAS, 0.0f);
        gl4.glSamplerParameteri(samplerName[0], GL_TEXTURE_COMPARE_MODE, GL_NONE);
        gl4.glSamplerParameteri(samplerName[0], GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);

        return true;
    }

    private boolean initTexture(GL4 gl4) {

        try {
            jgli.Texture2d texture = new Texture2d(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE));
            assert (!texture.empty());
            jgli.Gl.Format format = jgli.Gl.translate(texture.format());
            jgli.Gl.Swizzles swizzles = jgli.Gl.translate(texture.swizzles());

            gl4.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

            gl4.glGenTextures(Texture.MAX, textureName, 0);

            gl4.glActiveTexture(GL_TEXTURE0);
            gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.DIFFUSE]);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, texture.levels() - 1);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_R, swizzles.r.value);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_G, swizzles.g.value);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_B, swizzles.b.value);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_A, swizzles.a.value);
            gl4.glTexStorage2D(GL_TEXTURE_2D, texture.levels(),
                    format.internal.value, texture.dimensions()[0], texture.dimensions()[1]);

            for (int level = 0; level < texture.levels(); ++level) {
                gl4.glTexSubImage2D(GL_TEXTURE_2D, level,
                        0, 0,
                        texture.dimensions(level)[0], texture.dimensions(level)[1],
                        format.external.value, format.type.value,
                        texture.data(level));
            }

            gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.COLORBUFFER]);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
            gl4.glTexStorage2D(GL_TEXTURE_2D, 1, GL_RGBA8, windowSize.x, windowSize.y);

            gl4.glPixelStorei(GL_UNPACK_ALIGNMENT, 4);

        } catch (IOException ex) {
            Logger.getLogger(Gl_430_fbo_without_attachment.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(1, vertexArrayName, 0);
        gl4.glBindVertexArray(vertexArrayName[0]);
        gl4.glBindVertexArray(0);

        return true;
    }

    private boolean initFramebuffer(GL4 gl4) {

        boolean validated = true;

        gl4.glGenFramebuffers(1, framebufferName, 0);
        gl4.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[0]);
        gl4.glFramebufferParameteri(GL_FRAMEBUFFER, GL_FRAMEBUFFER_DEFAULT_WIDTH, windowSize.x);
        gl4.glFramebufferParameteri(GL_FRAMEBUFFER, GL_FRAMEBUFFER_DEFAULT_HEIGHT, windowSize.y);
        gl4.glFramebufferParameteri(GL_FRAMEBUFFER, GL_FRAMEBUFFER_DEFAULT_LAYERS, 1);
        gl4.glFramebufferParameteri(GL_FRAMEBUFFER, GL_FRAMEBUFFER_DEFAULT_SAMPLES, 1);
        gl4.glFramebufferParameteri(GL_FRAMEBUFFER, GL_FRAMEBUFFER_DEFAULT_FIXED_SAMPLE_LOCATIONS, GL_TRUE);
        if (!isFramebufferComplete(gl4, framebufferName[0])) {
            return false;
        }

        gl4.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        return validated;
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glViewportIndexedf(0, 0, 0, windowSize.x, windowSize.y);

        gl4.glBindVertexArray(vertexArrayName[0]);
        gl4.glBindSampler(0, samplerName[0]);

        // Render
        gl4.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[0]);
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 0.5f, 0.0f, 1.0f}, 0);

        gl4.glBindProgramPipeline(pipelineName[Pipeline.RENDER]);
        gl4.glActiveTexture(GL_TEXTURE0);
        gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.DIFFUSE]);
        gl4.glBindImageTexture(Semantic.Image.DIFFUSE, textureName[Texture.COLORBUFFER], 0, false,
                0, GL_WRITE_ONLY, GL_RGBA8);

        gl4.glDrawArraysInstancedBaseInstance(GL_TRIANGLES, 0, 3, 1, 0);

        // Splash
        gl4.glBindFramebuffer(GL_FRAMEBUFFER, 0);

        gl4.glBindProgramPipeline(pipelineName[Pipeline.SPLASH]);
        gl4.glActiveTexture(GL_TEXTURE0);
        gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.COLORBUFFER]);

        gl4.glDrawArraysInstancedBaseInstance(GL_TRIANGLES, 0, 3, 1, 0);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteSamplers(1, samplerName, 0);
        gl4.glDeleteProgramPipelines(Pipeline.MAX, pipelineName, 0);
        gl4.glDeleteProgram(programName[Pipeline.SPLASH]);
        gl4.glDeleteProgram(programName[Pipeline.RENDER]);
        gl4.glDeleteFramebuffers(1, framebufferName, 0);
        gl4.glDeleteTextures(Texture.MAX, textureName, 0);
        gl4.glDeleteVertexArrays(1, vertexArrayName, 0);

        return true;
    }
}
