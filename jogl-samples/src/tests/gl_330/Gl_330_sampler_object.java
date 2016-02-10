/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_330;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2GL3.*;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.BufferUtils;
import glm.glm;
import glm.mat._4.Mat4;
import glm.vec._3.Vec3;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import glm.vec._2.Vec2;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import jgli.Texture2d;

/**
 *
 * @author GBarbieri
 */
public class Gl_330_sampler_object extends Test {

    public static void main(String[] args) {
        Gl_330_sampler_object gl_330_sampler_object = new Gl_330_sampler_object();
    }

    public Gl_330_sampler_object() {
        super("gl-330-sampler-object", Profile.CORE, 3, 3);
    }

    private final String SHADERS_SOURCE = "sampler-object";
    private final String SHADERS_ROOT = "src/data/gl_330";
    private final String TEXTURE_DIFFUSE = "kueken7_rgba8_srgb.dds";

    // With DDS textures, v texture coordinate are reversed, from top to bottom
    private int vertexCount = 6;
    private int vertexSize = vertexCount * 2 * Vec2.SIZE;
    private float[] vertexData = {
        -1.0f, -1.0f,/**/ 0.0f, 1.0f,
        +1.0f, -1.0f,/**/ 1.0f, 1.0f,
        +1.0f, +1.0f,/**/ 1.0f, 0.0f,
        +1.0f, +1.0f,/**/ 1.0f, 0.0f,
        -1.0f, +1.0f,/**/ 0.0f, 0.0f,
        -1.0f, -1.0f,/**/ 0.0f, 1.0f};

    private int[] vertexArrayName = {0}, bufferName = {0}, textureName = {0}, samplerAname = {0}, samplerBname = {0};
    private int programName, uniformMvp, uniformDiffuseA, uniformDiffuseB;

    @Override
    protected boolean begin(GL gl) {

        GL3 gl3 = (GL3) gl;

        boolean validated = true;

        if (validated) {
            validated = initProgram(gl3);
        }
        if (validated) {
            validated = initBuffer(gl3);
        }
        if (validated) {
            validated = initTexture(gl3);
        }
        if (validated) {
            validated = initSampler(gl3);
        }
        if (validated) {
            validated = initVertexArray(gl3);
        }

        return validated;
    }

    private boolean initProgram(GL3 gl3) {

        boolean validated = true;

        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            ShaderCode vertShaderCode = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

            shaderProgram.init(gl3);

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);

            programName = shaderProgram.program();

            shaderProgram.link(gl3, System.out);
        }

        // Get variables locations
        if (validated) {

            uniformMvp = gl3.glGetUniformLocation(programName, "mvp");
            uniformDiffuseA = gl3.glGetUniformLocation(programName, "material.diffuse[0]");
            uniformDiffuseB = gl3.glGetUniformLocation(programName, "material.diffuse[1]");
        }

        return validated & checkError(gl3, "initProgram");
    }

    private boolean initBuffer(GL3 gl3) {

        gl3.glGenBuffers(1, bufferName, 0);

        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[0]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl3.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(vertexBuffer);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        return true;
    }

    private boolean initSampler(GL3 gl3) {

        gl3.glGenSamplers(1, samplerAname, 0);
        gl3.glSamplerParameteri(samplerAname[0], GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_NEAREST);
        gl3.glSamplerParameteri(samplerAname[0], GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        gl3.glSamplerParameteri(samplerAname[0], GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        gl3.glSamplerParameteri(samplerAname[0], GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        gl3.glSamplerParameteri(samplerAname[0], GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        gl3.glSamplerParameterfv(samplerAname[0], GL_TEXTURE_BORDER_COLOR, new float[]{0.0f, 0.0f, 0.0f, 0.0f}, 0);
        gl3.glSamplerParameterf(samplerAname[0], GL_TEXTURE_MIN_LOD, -1000.f);
        gl3.glSamplerParameterf(samplerAname[0], GL_TEXTURE_MAX_LOD, 1000.f);
        gl3.glSamplerParameterf(samplerAname[0], GL_TEXTURE_LOD_BIAS, 3.0f);
        gl3.glSamplerParameteri(samplerAname[0], GL_TEXTURE_COMPARE_MODE, GL_NONE);
        gl3.glSamplerParameteri(samplerAname[0], GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);

        gl3.glGenSamplers(1, samplerBname, 0);
        gl3.glSamplerParameteri(samplerBname[0], GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        gl3.glSamplerParameteri(samplerBname[0], GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        gl3.glSamplerParameteri(samplerBname[0], GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        gl3.glSamplerParameteri(samplerBname[0], GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        gl3.glSamplerParameteri(samplerBname[0], GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        gl3.glSamplerParameterfv(samplerBname[0], GL_TEXTURE_BORDER_COLOR, new float[]{0.0f, 0.0f, 0.0f, 0.0f}, 0);
        gl3.glSamplerParameterf(samplerBname[0], GL_TEXTURE_MIN_LOD, -1000.f);
        gl3.glSamplerParameterf(samplerBname[0], GL_TEXTURE_MAX_LOD, 1000.f);
        gl3.glSamplerParameterf(samplerBname[0], GL_TEXTURE_LOD_BIAS, 3.0f);
        gl3.glSamplerParameteri(samplerBname[0], GL_TEXTURE_COMPARE_MODE, GL_NONE);
        gl3.glSamplerParameteri(samplerBname[0], GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);

        return true;
    }

    private boolean initTexture(GL3 gl3) {

        try {
            jgli.Texture2d texture = new Texture2d(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE));
            jgli.Gl.Format format = jgli.Gl.translate(texture.format());

            gl3.glGenTextures(1, textureName, 0);
            gl3.glActiveTexture(GL_TEXTURE0);
            gl3.glBindTexture(GL_TEXTURE_2D, textureName[0]);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, texture.levels() - 1);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_R, GL_RED);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_G, GL_GREEN);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_B, GL_BLUE);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_A, GL_ALPHA);

            for (int level = 0; level < texture.levels(); ++level) {
                gl3.glTexImage2D(GL_TEXTURE_2D, level,
                        format.internal.value,
                        texture.dimensions(level)[0], texture.dimensions(level)[1],
                        0,
                        format.external.value, format.type.value,
                        texture.data(level));
            }

            gl3.glActiveTexture(GL_TEXTURE0);
            gl3.glBindTexture(GL_TEXTURE_2D, 0);

        } catch (IOException ex) {
            Logger.getLogger(Gl_330_sampler_object.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    private boolean initVertexArray(GL3 gl3) {

        gl3.glGenVertexArrays(1, vertexArrayName, 0);
        gl3.glBindVertexArray(vertexArrayName[0]);
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[0]);
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 2 * Vec2.SIZE, 0);
            gl3.glVertexAttribPointer(Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, 2 * Vec2.SIZE, Vec2.SIZE);
            gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl3.glEnableVertexAttribArray(Semantic.Attr.TEXCOORD);
        }
        gl3.glBindVertexArray(0);

        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL3 gl3 = (GL3) gl;

        Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, 4.0f / 3.0f, 0.1f, 1000.0f);
        Mat4 model = new Mat4(1.0f).scale(new Vec3(3.0f));
        Mat4 mvp = projection.mul(viewMat4()).mul(model);

        gl3.glViewport(0, 0, windowSize.x, windowSize.y);
        gl3.glClearBufferfv(GL_COLOR, 0, new float[]{0.0f, 0.5f, 1.0f, 1.0f}, 0);

        // Bind the program for use
        gl3.glUseProgram(programName);
        gl3.glUniformMatrix4fv(uniformMvp, 1, false, mvp.toFa_(), 0);
        gl3.glUniform1i(uniformDiffuseA, 0);
        gl3.glUniform1i(uniformDiffuseB, 1);

        gl3.glActiveTexture(GL_TEXTURE0);
        gl3.glBindTexture(GL_TEXTURE_2D, textureName[0]);
        gl3.glBindSampler(0, samplerBname[0]);

        gl3.glActiveTexture(GL_TEXTURE1);
        gl3.glBindTexture(GL_TEXTURE_2D, textureName[0]);
        gl3.glBindSampler(1, samplerAname[0]);

        gl3.glBindVertexArray(vertexArrayName[0]);

        {
            System.out.println("Validate");

            gl3.glValidateProgram(programName);

            int[] result = {GL_FALSE};
            gl3.glGetProgramiv(programName, GL_VALIDATE_STATUS, result, 0);

            int[] infoLogLength = {0};
            gl3.glGetProgramiv(programName, GL_INFO_LOG_LENGTH, infoLogLength, 0);
            byte[] buffer = new byte[Math.max(infoLogLength[0], 1)];
            gl3.glGetProgramInfoLog(programName, infoLogLength[0], infoLogLength, 0, buffer, 0);
            System.out.println(new String(buffer));
        }

        gl3.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 1);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;

        gl3.glDeleteBuffers(1, bufferName, 0);
        gl3.glDeleteProgram(programName);
        gl3.glDeleteTextures(1, textureName, 0);
        gl3.glDeleteSamplers(1, samplerAname, 0);
        gl3.glDeleteSamplers(1, samplerBname, 0);
        gl3.glDeleteVertexArrays(1, vertexArrayName, 0);

        return true;
    }
}
