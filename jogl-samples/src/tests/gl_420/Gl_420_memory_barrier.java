/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_420;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_ALPHA;
import static com.jogamp.opengl.GL.GL_CLAMP_TO_EDGE;
import static com.jogamp.opengl.GL.GL_COLOR_ATTACHMENT0;
import static com.jogamp.opengl.GL.GL_COMPRESSED_RGB_S3TC_DXT1_EXT;
import static com.jogamp.opengl.GL.GL_FRAMEBUFFER;
import static com.jogamp.opengl.GL.GL_NEAREST;
import static com.jogamp.opengl.GL.GL_RGBA8;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_S;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_T;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_TRUE;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER_BIT;
import static com.jogamp.opengl.GL2ES2.GL_PROGRAM_SEPARABLE;
import static com.jogamp.opengl.GL2ES2.GL_RED;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER_BIT;
import static com.jogamp.opengl.GL2ES3.GL_BLUE;
import static com.jogamp.opengl.GL2ES3.GL_GREEN;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_BASE_LEVEL;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_FETCH_BARRIER_BIT;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_MAX_LEVEL;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_SWIZZLE_A;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_SWIZZLE_B;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_SWIZZLE_G;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_SWIZZLE_R;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_UPDATE_BARRIER_BIT;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.Profile;
import framework.Test;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jgli.Texture2d;
import jglm.Vec2i;

/**
 *
 * @author GBarbieri
 */
public class Gl_420_memory_barrier extends Test {

    public static void main(String[] args) {
        Gl_420_memory_barrier gl_420_memory_barrier = new Gl_420_memory_barrier();
    }

    public Gl_420_memory_barrier() {
        super("gl-420-memory-barrier", Profile.CORE, 4, 2);
    }

    private final String SHADERS_SOURCE_UPDATE = "memory-barrier-update";
    private final String SHADERS_SOURCE_BLIT = "memory-barrier-blit";
    private final String SHADERS_ROOT = "src/data/gl_420";
    private final String TEXTURE_DIFFUSE = "kueken7_rgb_dxt1_unorm.dds";

    private int vertexCount = 3;

    private enum Program {
        UPDATE,
        BLIT,
        MAX
    }

    private enum Pipeline {
        UPDATE,
        BLIT,
        MAX
    }

    private enum Texture {
        DIFFUSE,
        COLORBUFFER,
        MAX
    }

    private int[] vertexArrayName = {0}, framebufferName = {0}, samplerName = {0},
            pipelineName = new int[Pipeline.MAX.ordinal()], programName = new int[Program.MAX.ordinal()],
            textureName = new int[Texture.MAX.ordinal()];
    private Vec2i frameBufferSize = new Vec2i();
    private int frameIndex = 0;

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;

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
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_UPDATE, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_UPDATE, "frag", null, true);

            shaderProgram.init(gl4);
            programName[Program.UPDATE.ordinal()] = shaderProgram.program();

            gl4.glProgramParameteri(programName[Program.UPDATE.ordinal()], GL_PROGRAM_SEPARABLE, GL_TRUE);

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);
            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_BLIT, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_BLIT, "frag", null, true);

            shaderProgram.init(gl4);
            programName[Program.BLIT.ordinal()] = shaderProgram.program();

            gl4.glProgramParameteri(programName[Program.BLIT.ordinal()], GL_PROGRAM_SEPARABLE, GL_TRUE);

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);
            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            gl4.glGenProgramPipelines(Pipeline.MAX.ordinal(), pipelineName, 0);
            gl4.glUseProgramStages(pipelineName[Pipeline.UPDATE.ordinal()], GL_VERTEX_SHADER_BIT
                    | GL_FRAGMENT_SHADER_BIT, programName[Program.UPDATE.ordinal()]);
            gl4.glUseProgramStages(pipelineName[Pipeline.BLIT.ordinal()], GL_VERTEX_SHADER_BIT
                    | GL_FRAGMENT_SHADER_BIT, programName[Program.BLIT.ordinal()]);
        }

        return validated & checkError(gl4, "initProgram - stage");
    }

    private boolean initSampler(GL4 gl4) {

        boolean validated = true;

        gl4.glGenSamplers(1, samplerName, 0);
        gl4.glSamplerParameteri(samplerName[0], GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        gl4.glSamplerParameteri(samplerName[0], GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        gl4.glSamplerParameteri(samplerName[0], GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        gl4.glSamplerParameteri(samplerName[0], GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        return validated;
    }

    private boolean initTexture(GL4 gl4) {

        boolean validated = true;

        try {

            jgli.Texture2d texture = new Texture2d(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE));
            frameBufferSize.x = texture.dimensions()[0];
            frameBufferSize.y = texture.dimensions()[1];

            gl4.glGenTextures(Texture.MAX.ordinal(), textureName, 0);

            gl4.glActiveTexture(GL_TEXTURE0);
            gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.DIFFUSE.ordinal()]);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, texture.levels() - 1);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_R, GL_RED);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_G, GL_GREEN);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_B, GL_BLUE);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_A, GL_ALPHA);
            gl4.glTexStorage2D(GL_TEXTURE_2D, texture.levels(), GL_COMPRESSED_RGB_S3TC_DXT1_EXT,
                    texture.dimensions()[0], texture.dimensions()[1]);

            for (int level = 0; level < texture.levels(); ++level) {
                gl4.glCompressedTexSubImage2D(GL_TEXTURE_2D,
                        level,
                        0, 0,
                        texture.dimensions(level)[0],
                        texture.dimensions(level)[1],
                        GL_COMPRESSED_RGB_S3TC_DXT1_EXT,
                        texture.size(level),
                        texture.data(0, 0, level));
            }

            gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.COLORBUFFER.ordinal()]);
            gl4.glTexStorage2D(GL_TEXTURE_2D, 1, GL_RGBA8, frameBufferSize.x, frameBufferSize.y);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_R, GL_RED);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_G, GL_GREEN);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_B, GL_BLUE);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_A, GL_ALPHA);

        } catch (IOException ex) {
            Logger.getLogger(Gl_420_memory_barrier.class.getName()).log(Level.SEVERE, null, ex);
        }
        return validated;
    }

    private boolean initFramebuffer(GL4 gl4) {

        boolean validated = true;

        gl4.glGenFramebuffers(1, framebufferName, 0);
        gl4.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[0]);
        gl4.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, textureName[Texture.COLORBUFFER.ordinal()], 0);

        validated = validated && (isFramebufferComplete(gl4, framebufferName[0]));

        gl4.glBindFramebuffer(GL_FRAMEBUFFER, 0);

        return validated;
    }

    private boolean initVertexArray(GL4 gl4) {

        boolean validated = true;

        gl4.glGenVertexArrays(1, vertexArrayName, 0);
        gl4.glBindVertexArray(vertexArrayName[0]);
        gl4.glBindVertexArray(0);

        return validated;
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        // Bind shared objects
        gl4.glViewportIndexedf(0, 0, 0, windowSize.x, windowSize.y);
        gl4.glBindSampler(0, samplerName[0]);
        gl4.glBindVertexArray(vertexArrayName[0]);

        // Update a colorbuffer bound as a framebuffer attachement and as a texture
        gl4.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[0]);
        gl4.glBindProgramPipeline(pipelineName[Pipeline.UPDATE.ordinal()]);

        gl4.glActiveTexture(GL_TEXTURE0);
        gl4.glBindTexture(GL_TEXTURE_2D, frameIndex != 0 ? textureName[Texture.COLORBUFFER.ordinal()]
                : textureName[Texture.DIFFUSE.ordinal()]);

        gl4.glMemoryBarrier(GL_TEXTURE_UPDATE_BARRIER_BIT | GL_TEXTURE_FETCH_BARRIER_BIT);
        gl4.glDrawArraysInstancedBaseInstance(GL_TRIANGLES, 0, vertexCount, 1, 0);

        // Blit to framebuffer
        gl4.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        gl4.glBindProgramPipeline(pipelineName[Pipeline.BLIT.ordinal()]);

        gl4.glActiveTexture(GL_TEXTURE0);
        gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.COLORBUFFER.ordinal()]);

        gl4.glMemoryBarrier(GL_TEXTURE_UPDATE_BARRIER_BIT);
        gl4.glDrawArraysInstancedBaseInstance(GL_TRIANGLES, 0, vertexCount, 1, 0);

        frameIndex = (frameIndex + 1) % 256;

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteTextures(Texture.MAX.ordinal(), textureName, 0);
        gl4.glDeleteFramebuffers(1, framebufferName, 0);
        gl4.glDeleteProgram(programName[Program.BLIT.ordinal()]);
        gl4.glDeleteProgram(programName[Program.UPDATE.ordinal()]);
        gl4.glDeleteVertexArrays(1, vertexArrayName, 0);
        gl4.glDeleteSamplers(1, samplerName, 0);
        gl4.glDeleteProgramPipelines(Pipeline.MAX.ordinal(), pipelineName, 0);

        return true;
    }
}
