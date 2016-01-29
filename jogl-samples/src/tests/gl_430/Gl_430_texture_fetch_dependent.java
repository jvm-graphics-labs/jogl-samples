/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_430;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_ALPHA;
import static com.jogamp.opengl.GL.GL_FRAMEBUFFER;
import static com.jogamp.opengl.GL.GL_NEAREST;
import static com.jogamp.opengl.GL.GL_RGBA;
import static com.jogamp.opengl.GL.GL_RGBA8;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE1;
import static com.jogamp.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_TRUE;
import static com.jogamp.opengl.GL.GL_UNSIGNED_BYTE;
import static com.jogamp.opengl.GL.GL_UNSIGNED_INT;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER_BIT;
import static com.jogamp.opengl.GL2ES2.GL_PROGRAM_SEPARABLE;
import static com.jogamp.opengl.GL2ES2.GL_RED;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER_BIT;
import static com.jogamp.opengl.GL2ES3.GL_BLUE;
import static com.jogamp.opengl.GL2ES3.GL_GREEN;
import static com.jogamp.opengl.GL2ES3.GL_R32UI;
import static com.jogamp.opengl.GL2ES3.GL_RED_INTEGER;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_2D_ARRAY;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_BASE_LEVEL;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_MAX_LEVEL;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_SWIZZLE_A;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_SWIZZLE_B;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_SWIZZLE_G;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_SWIZZLE_R;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import core.glm;
import framework.BufferUtils;
import framework.Profile;
import framework.Test;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import jglm.Vec2i;

/**
 *
 * @author GBarbieri
 */
public class Gl_430_texture_fetch_dependent extends Test {

    public static void main(String[] args) {
        Gl_430_texture_fetch_dependent gl_430_texture_fetch_dependent = new Gl_430_texture_fetch_dependent();
    }

    public Gl_430_texture_fetch_dependent() {
        super("gl-430-texture-fetch-dependent", Profile.CORE, 4, 3, new Vec2i(1280, 720));
    }

    private final String SHADERS_SOURCE = "texture-fetch-dependent";
    private final String SHADERS_ROOT = "src/data/gl_430";

    private enum Texture {
        DIFFUSE,
        INDIRECTION,
        MAX
    }

    private int[] pipelineName = {0}, vertexArrayName = {0}, textureName = new int[Texture.MAX.ordinal()];
    private int programName;

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

        return validated;
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        // Create program
        if (validated) {

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

            ShaderProgram shaderProgram = new ShaderProgram();
            shaderProgram.init(gl4);

            programName = shaderProgram.program();

            gl4.glProgramParameteri(programName, GL_PROGRAM_SEPARABLE, GL_TRUE);

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);

            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            gl4.glGenProgramPipelines(1, pipelineName, 0);
            gl4.glUseProgramStages(pipelineName[0], GL_VERTEX_SHADER_BIT | GL_FRAGMENT_SHADER_BIT, programName);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initTexture(GL4 gl4) {

        gl4.glGenTextures(Texture.MAX.ordinal(), textureName, 0);

        {
            gl4.glBindTexture(GL_TEXTURE_2D_ARRAY, textureName[Texture.DIFFUSE.ordinal()]);
            gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SWIZZLE_R, GL_RED);
            gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SWIZZLE_G, GL_GREEN);
            gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SWIZZLE_B, GL_BLUE);
            gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SWIZZLE_A, GL_ALPHA);
            gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAX_LEVEL, 0);
            gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

            byte[] colors = new byte[4 * 2048];
            for (int colorIndex = 0; colorIndex < colors.length / 4; ++colorIndex) {
                colors[colorIndex * 4 + 0] = (byte) (glm.linearRand(0, 1) * 255f);
                colors[colorIndex * 4 + 1] = (byte) (glm.linearRand(0, 1) * 255f);
                colors[colorIndex * 4 + 2] = (byte) (glm.linearRand(0, 1) * 255f);
                colors[colorIndex * 4 + 3] = (byte) 1;
            }
            gl4.glTexStorage3D(GL_TEXTURE_2D_ARRAY, 1, GL_RGBA8, 1, 1, colors.length / 4 * Byte.BYTES);
            ByteBuffer colorsBuffer = GLBuffers.newDirectByteBuffer(colors);
            gl4.glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0,
                    0, 0, 0,
                    1, 1, 2048,
                    GL_RGBA, GL_UNSIGNED_BYTE, colorsBuffer);
            BufferUtils.destroyDirectBuffer(colorsBuffer);
        }

        {
            gl4.glBindTexture(GL_TEXTURE_2D_ARRAY, textureName[Texture.INDIRECTION.ordinal()]);
            gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SWIZZLE_R, GL_RED);
            gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SWIZZLE_G, GL_GREEN);
            gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SWIZZLE_B, GL_BLUE);
            gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SWIZZLE_A, GL_ALPHA);
            gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAX_LEVEL, 0);
            gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

            int[] data = new int[windowSize.x * windowSize.y];
            for (int index = 0; index < data.length; ++index) {
                //Data[Index] = glm::u32vec1(glm::linearRand(glm::vec1(0), glm::vec1(1)) * 255.0f);
                long signedInt = (long) (glm.linearRand(0, 1) * data.length - 1);
                data[index] = Integer.parseUnsignedInt("" + signedInt);
            }
            gl4.glTexStorage3D(GL_TEXTURE_2D_ARRAY, 1, GL_R32UI, windowSize.x, windowSize.y, 1);
            IntBuffer dataBuffer = GLBuffers.newDirectIntBuffer(data);
            gl4.glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0,
                    0, 0, 0,
                    windowSize.x, windowSize.y, 1,
                    GL_RED_INTEGER, GL_UNSIGNED_INT, dataBuffer);
            BufferUtils.destroyDirectBuffer(dataBuffer);
        }

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

        gl4.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        gl4.glBindProgramPipeline(pipelineName[0]);
        gl4.glBindVertexArray(vertexArrayName[0]);
        gl4.glActiveTexture(GL_TEXTURE0);
        gl4.glBindTexture(GL_TEXTURE_2D_ARRAY, textureName[Texture.DIFFUSE.ordinal()]);
        gl4.glActiveTexture(GL_TEXTURE1);
        gl4.glBindTexture(GL_TEXTURE_2D_ARRAY, textureName[Texture.INDIRECTION.ordinal()]);

        gl4.glDrawArraysInstancedBaseInstance(GL_TRIANGLES, 0, 3, 1, 0);
        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteProgram(programName);
        gl4.glDeleteProgramPipelines(1, pipelineName, 0);
        gl4.glDeleteVertexArrays(1, vertexArrayName, 0);
        gl4.glDeleteTextures(Texture.MAX.ordinal(), textureName, 0);

        return true;
    }
}
