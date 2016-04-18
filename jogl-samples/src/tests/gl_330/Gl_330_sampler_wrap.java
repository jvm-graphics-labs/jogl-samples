/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_330;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2ES3.*;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.BufferUtils;
import glm.glm;
import glm.mat._4.Mat4;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import glf.Vertex_v2fv2f;
import glm.vec._2.Vec2;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import jgli.Texture2d;
import jglm.Vec4i;

/**
 *
 * @author GBarbieri
 */
public class Gl_330_sampler_wrap extends Test {

    public static void main(String[] args) {
        Gl_330_sampler_wrap gl_330_sampler_wrap = new Gl_330_sampler_wrap();
    }

    public Gl_330_sampler_wrap() {
        super("gl-330-sampler-wrap", Profile.CORE, 3, 3);
    }

    private final String SHADERS_SOURCE = "texture-2d";
    private final String SHADERS_ROOT = "src/data/gl_330";
    private final String TEXTURE_DIFFUSE = "kueken7_rgba_dxt5_unorm.dds";

    // With DDS textures, v texture coordinate are reversed, from top to bottom
    private int vertexCount = 6;
    private int vertexSize = vertexCount * Vertex_v2fv2f.SIZE;
    private float[] vertexData = {
        -1.0f, -1.0f,/**/ -2.0f, +2.0f,
        +1.0f, -1.0f,/**/ +2.0f, +2.0f,
        +1.0f, +1.0f,/**/ +2.0f, -2.0f,
        +1.0f, +1.0f,/**/ +2.0f, -2.0f,
        -1.0f, +1.0f,/**/ -2.0f, -2.0f,
        -1.0f, -1.0f,/**/ -2.0f, +2.0f};

    private class Viewport {

        public static final int V00 = 0;
        public static final int V10 = 1;
        public static final int V11 = 2;
        public static final int V01 = 3;
        public static final int MAX = 4;
    }

    private IntBuffer vertexArrayName = GLBuffers.newDirectIntBuffer(1), bufferName = GLBuffers.newDirectIntBuffer(1),
            texture2dName = GLBuffers.newDirectIntBuffer(1), samplerName = GLBuffers.newDirectIntBuffer(Viewport.MAX);
    private int programName, uniformMvp, uniformDiffuse;
    private Vec4i[] viewport = new Vec4i[Viewport.MAX];

    @Override
    protected boolean begin(GL gl) {

        GL3 gl3 = (GL3) gl;

        viewport[Viewport.V00] = new Vec4i(0, 0, windowSize.x >> 1, windowSize.y >> 1);
        viewport[Viewport.V10] = new Vec4i(windowSize.x >> 1, 0, windowSize.x >> 1, windowSize.y >> 1);
        viewport[Viewport.V11] = new Vec4i(windowSize.x >> 1, windowSize.y >> 1,
                windowSize.x >> 1, windowSize.y >> 1);
        viewport[Viewport.V01] = new Vec4i(0, windowSize.y >> 1, windowSize.x >> 1, windowSize.y >> 1);

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

        return validated && checkError(gl3, "begin");
    }

    private boolean initProgram(GL3 gl3) {

        boolean validated = true;

        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            ShaderCode vertShaderCode = ShaderCode.create(gl3, GL_VERTEX_SHADER, this.getClass(), SHADERS_ROOT, null,
                    SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl3, GL_FRAGMENT_SHADER, this.getClass(), SHADERS_ROOT, null,
                    SHADERS_SOURCE, "frag", null, true);

            shaderProgram.init(gl3);

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);

            programName = shaderProgram.program();

            shaderProgram.link(gl3, System.out);
        }

        // Get variables locations
        if (validated) {

            uniformMvp = gl3.glGetUniformLocation(programName, "mvp");
            uniformDiffuse = gl3.glGetUniformLocation(programName, "diffuse");
        }

        return validated & checkError(gl3, "initProgram");
    }

    private boolean initBuffer(GL3 gl3) {

        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);

        gl3.glGenBuffers(1, bufferName);

        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(0));
        gl3.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        BufferUtils.destroyDirectBuffer(vertexBuffer);

        return checkError(gl3, "initBuffer");
    }

    private boolean initSampler(GL3 gl3) {

        FloatBuffer borderColor = GLBuffers.newDirectFloatBuffer(new float[]{0.0f, 0.5f, 1.0f, 1.0f});

        gl3.glGenSamplers(Viewport.MAX, samplerName);

        for (int i = 0; i < Viewport.MAX; ++i) {
            gl3.glSamplerParameteri(samplerName.get(i), GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            gl3.glSamplerParameteri(samplerName.get(i), GL_TEXTURE_MAG_FILTER, GL_LINEAR);

            gl3.glSamplerParameterfv(samplerName.get(i), GL_TEXTURE_BORDER_COLOR, borderColor);
        }

        gl3.glSamplerParameteri(samplerName.get(Viewport.V00), GL_TEXTURE_WRAP_S, GL_MIRRORED_REPEAT);
        gl3.glSamplerParameteri(samplerName.get(Viewport.V10), GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
        gl3.glSamplerParameteri(samplerName.get(Viewport.V11), GL_TEXTURE_WRAP_S, GL_REPEAT);
        gl3.glSamplerParameteri(samplerName.get(Viewport.V01), GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);

        gl3.glSamplerParameteri(samplerName.get(Viewport.V00), GL_TEXTURE_WRAP_T, GL_MIRRORED_REPEAT);
        gl3.glSamplerParameteri(samplerName.get(Viewport.V10), GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
        gl3.glSamplerParameteri(samplerName.get(Viewport.V11), GL_TEXTURE_WRAP_T, GL_REPEAT);
        gl3.glSamplerParameteri(samplerName.get(Viewport.V01), GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        BufferUtils.destroyDirectBuffer(borderColor);

        return checkError(gl3, "initSampler");
    }

    private boolean initTexture(GL3 gl3) {

        try {
            gl3.glGenTextures(1, texture2dName);

            gl3.glActiveTexture(GL_TEXTURE0);
            gl3.glBindTexture(GL_TEXTURE_2D, texture2dName.get(0));

            jgli.Texture2d texture = new Texture2d(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE));
            for (int level = 0; level < texture.levels(); ++level) {
                gl3.glCompressedTexImage2D(
                        GL_TEXTURE_2D,
                        level,
                        GL_COMPRESSED_RGBA_S3TC_DXT5_EXT,
                        texture.dimensions(level)[0],
                        texture.dimensions(level)[1],
                        0,
                        texture.size(level),
                        texture.data(level));
            }

        } catch (IOException ex) {
            Logger.getLogger(Gl_330_sampler_wrap.class.getName()).log(Level.SEVERE, null, ex);
        }
        return checkError(gl3, "initTexture");
    }

    private boolean initVertexArray(GL3 gl3) {

        gl3.glGenVertexArrays(1, vertexArrayName);
        gl3.glBindVertexArray(vertexArrayName.get(0));
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(0));
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, Vertex_v2fv2f.SIZE, 0);
            gl3.glVertexAttribPointer(Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, Vertex_v2fv2f.SIZE, Vec2.SIZE);
            gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl3.glEnableVertexAttribArray(Semantic.Attr.TEXCOORD);
        }
        gl3.glBindVertexArray(0);

        return checkError(gl3, "initVertexArray");
    }

    @Override
    protected boolean render(GL gl) {

        GL3 gl3 = (GL3) gl;

        Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, (float) windowSize.x / windowSize.y, 0.1f, 1000.0f);
        Mat4 model = new Mat4(1.0f);
        Mat4 mvp = projection.mul(viewMat4()).mul(model);

        gl3.glClearBufferfv(GL_COLOR, 0, clearColor.put(0, 1).put(1, .5f).put(2, 0).put(3, 1));

        gl3.glUseProgram(programName);
        gl3.glUniformMatrix4fv(uniformMvp, 1, false, mvp.toFa_(), 0);
        gl3.glUniform1i(uniformDiffuse, 0);

        gl3.glActiveTexture(GL_TEXTURE0);
        gl3.glBindTexture(GL_TEXTURE_2D, texture2dName.get(0));

        gl3.glBindVertexArray(vertexArrayName.get(0));

        for (int Index = 0; Index < Viewport.MAX; ++Index) {
            gl3.glViewport(viewport[Index].x, viewport[Index].y, viewport[Index].z, viewport[Index].w);
            gl3.glBindSampler(0, samplerName.get(Index));
            gl3.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 1);
        }

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;

        gl3.glDeleteBuffers(1, bufferName);
        gl3.glDeleteProgram(programName);
        gl3.glDeleteTextures(1, texture2dName);
        gl3.glDeleteVertexArrays(1, vertexArrayName);
        gl3.glDeleteSamplers(1, samplerName);

        BufferUtils.destroyDirectBuffer(bufferName);
        BufferUtils.destroyDirectBuffer(texture2dName);
        BufferUtils.destroyDirectBuffer(vertexArrayName);
        BufferUtils.destroyDirectBuffer(samplerName);

        return checkError(gl3, "end");
    }
}
