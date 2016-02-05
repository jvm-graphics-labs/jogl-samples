/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_420;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2GL3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.BufferUtils;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import jgli.TextureCube;
import dev.Vec2;

/**
 *
 * @author GBarbieri
 */
public class Gl_420_texture_cube extends Test {

    public static void main(String[] args) {
        Gl_420_texture_cube gl_420_texture_cube = new Gl_420_texture_cube();
    }

    public Gl_420_texture_cube() {
        super("gl-420-texture-cube", Profile.CORE, 4, 2, new Vec2(0.0f, -(float) Math.PI * 0.45f));
    }

    private final String SHADERS_SOURCE = "texture-cube";
    private final String SHADERS_ROOT = "src/data/gl_420";

    // With DDS textures, v texture coordinate are reversed, from top to bottom
    private int vertexCount = 6;
    private int vertexSize = vertexCount * 2 * Float.BYTES;
    private float[] vertexData = {
        -1.0f * 4.0f, -1.0f * 4.0f,
        +1.0f * 4.0f, -1.0f * 4.0f,
        +1.0f * 4.0f, +1.0f * 4.0f,
        +1.0f * 4.0f, +1.0f * 4.0f,
        -1.0f * 4.0f, +1.0f * 4.0f,
        -1.0f * 4.0f, -1.0f * 4.0f};

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int TRANSFORM = 1;
        public static final int MAX = 2;
    }

    private class Transform {

        public static final int SIZEOF = (2 * 16 + 3) * Float.BYTES;
    }

    private int[] pipelineName = {0}, vertexArrayName = {0}, textureName = {0}, samplerName = {0},
            bufferName = new int[Buffer.MAX];
    private int programName;
    private float[] projection = new float[16], view = new float[16], model = new float[16],
            mvp = new float[16], mv = new float[16];

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;

        if (validated) {
            validated = initProgram(gl4);
        }
        if (validated) {
            validated = initBuffer(gl4);
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

        return validated;
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        gl4.glGenProgramPipelines(1, pipelineName, 0);

        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

            shaderProgram.init(gl4);
            programName = shaderProgram.program();
            gl4.glProgramParameteri(programName, GL_PROGRAM_SEPARABLE, GL_TRUE);

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);

            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            gl4.glUseProgramStages(pipelineName[0], GL_VERTEX_SHADER_BIT | GL_FRAGMENT_SHADER_BIT, programName);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        gl4.glGenBuffers(Buffer.MAX, bufferName, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl4.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(vertexBuffer);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        int[] uniformBufferOffset = {0};

        gl4.glGetIntegerv(
                GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT,
                uniformBufferOffset, 0);

        int uniformBlockSize = Math.max(Transform.SIZEOF, uniformBufferOffset[0]);

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
        gl4.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_DYNAMIC_DRAW);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return true;
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
        gl4.glSamplerParameterf(samplerName[0], GL_TEXTURE_MAX_ANISOTROPY_EXT, 16.0f);

        return true;
    }

    private boolean initTexture(GL4 gl4) {

        gl4.glActiveTexture(GL_TEXTURE0);
        gl4.glGenTextures(1, textureName, 0);
        gl4.glBindTexture(GL_TEXTURE_CUBE_MAP_ARRAY, textureName[0]);
        gl4.glTexParameteri(GL_TEXTURE_CUBE_MAP_ARRAY, GL_TEXTURE_BASE_LEVEL, 0);
        gl4.glTexParameteri(GL_TEXTURE_CUBE_MAP_ARRAY, GL_TEXTURE_MAX_LEVEL, 2);

        jgli.TextureCube textureA = new TextureCube(jgli.Format.FORMAT_RGBA8_UNORM_PACK32, new int[]{512, 512}, 1);
        assert (!textureA.empty());
        textureA.clear(0, 0, 0, new byte[]{(byte) 255, (byte) 0, (byte) 0, (byte) 255});
        textureA.clear(0, 1, 0, new byte[]{(byte) 255, (byte) 128, (byte) 0, (byte) 255});
        textureA.clear(0, 2, 0, new byte[]{(byte) 255, (byte) 255, (byte) 0, (byte) 255});
        textureA.clear(0, 3, 0, new byte[]{(byte) 0, (byte) 255, (byte) 0, (byte) 255});
        textureA.clear(0, 4, 0, new byte[]{(byte) 0, (byte) 255, (byte) 255, (byte) 255});
        textureA.clear(0, 5, 0, new byte[]{(byte) 0, (byte) 0, (byte) 255, (byte) 255});

        jgli.TextureCube textureB = new TextureCube(jgli.Format.FORMAT_RGBA8_UNORM_PACK32, new int[]{256, 256}, 1);
        assert (!textureB.empty());
        textureB.clear(0, 0, 0, new byte[]{(byte) 255, (byte) 128, (byte) 128, (byte) 255});
        textureB.clear(0, 1, 0, new byte[]{(byte) 255, (byte) 192, (byte) 128, (byte) 255});
        textureB.clear(0, 2, 0, new byte[]{(byte) 255, (byte) 255, (byte) 128, (byte) 255});
        textureB.clear(0, 3, 0, new byte[]{(byte) 128, (byte) 255, (byte) 128, (byte) 255});
        textureB.clear(0, 4, 0, new byte[]{(byte) 128, (byte) 255, (byte) 255, (byte) 255});
        textureB.clear(0, 5, 0, new byte[]{(byte) 128, (byte) 128, (byte) 255, (byte) 255});

        jgli.TextureCube textureC = new TextureCube(jgli.Format.FORMAT_RGBA8_UNORM_PACK32, new int[]{128, 128}, 1);
        assert (!textureC.empty());
        textureC.clear(0, 0, 0, new byte[]{(byte) 255, (byte) 192, (byte) 192, (byte) 255});
        textureC.clear(0, 1, 0, new byte[]{(byte) 255, (byte) 224, (byte) 192, (byte) 255});
        textureC.clear(0, 2, 0, new byte[]{(byte) 255, (byte) 255, (byte) 192, (byte) 255});
        textureC.clear(0, 3, 0, new byte[]{(byte) 192, (byte) 255, (byte) 192, (byte) 255});
        textureC.clear(0, 4, 0, new byte[]{(byte) 192, (byte) 255, (byte) 255, (byte) 255});
        textureC.clear(0, 5, 0, new byte[]{(byte) 192, (byte) 192, (byte) 255, (byte) 255});

        gl4.glTexStorage3D(GL_TEXTURE_CUBE_MAP_ARRAY, 3,
                GL_RGBA8, textureA.dimensions()[0], textureA.dimensions()[1], textureA.faces());

        gl4.glTexSubImage3D(GL_TEXTURE_CUBE_MAP_ARRAY, 0,
                0, 0, 0,
                textureA.dimensions()[0],
                textureA.dimensions()[1],
                textureA.faces(),
                GL_RGBA, GL_UNSIGNED_BYTE,
                textureA.data());

        gl4.glTexSubImage3D(GL_TEXTURE_CUBE_MAP_ARRAY, 1,
                0, 0, 0,
                textureB.dimensions()[0],
                textureB.dimensions()[1],
                textureB.faces(),
                GL_RGBA, GL_UNSIGNED_BYTE,
                textureB.data());

        gl4.glTexSubImage3D(GL_TEXTURE_CUBE_MAP_ARRAY, 2,
                0, 0, 0,
                textureC.dimensions()[0],
                textureC.dimensions()[1],
                textureC.faces(),
                GL_RGBA, GL_UNSIGNED_BYTE,
                textureC.data());

        //glGenerateMipmap(GL_TEXTURE_CUBE_MAP_ARRAY);
        return true;
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(1, vertexArrayName, 0);
        gl4.glBindVertexArray(vertexArrayName[0]);
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
        }
        gl4.glBindVertexArray(0);

        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        {
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
            ByteBuffer pointer = gl4.glMapBufferRange(
                    GL_UNIFORM_BUFFER, 0, Transform.SIZEOF,
                    GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            FloatUtil.makePerspective(projection, 0, true, (float) Math.PI * 0.25f,
                    (float) windowSize.x / windowSize.y, 0.1f, 1000.0f);
            view = view();
            FloatUtil.makeIdentity(model);
            FloatUtil.multMatrix(projection, view, mvp);
            FloatUtil.multMatrix(mvp, model);
            FloatUtil.multMatrix(view, model, mv);

            for (float f : mvp) {
                pointer.putFloat(f);
            }
            for (float f : mv) {
                pointer.putFloat(f);
            }
            pointer.putFloat(0.0f);
            pointer.putFloat(0.0f);
            pointer.putFloat(-cameraDistance());
            pointer.rewind();

            // Make sure the uniform buffer is uploaded
            gl4.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        gl4.glViewportIndexedf(0, 0, 0, windowSize.x, windowSize.y);
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, 0);

        gl4.glBindProgramPipeline(pipelineName[0]);
        gl4.glActiveTexture(GL_TEXTURE0);
        gl4.glBindTexture(GL_TEXTURE_CUBE_MAP_ARRAY, textureName[0]);
        gl4.glBindSampler(0, samplerName[0]);
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.TRANSFORM]);

        gl4.glBindVertexArray(vertexArrayName[0]);
        gl4.glDrawArraysInstancedBaseInstance(GL_TRIANGLES, 0, vertexCount, 1, 0);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteBuffers(Buffer.MAX, bufferName, 0);
        gl4.glDeleteProgram(programName);
        gl4.glDeleteTextures(1, textureName, 0);
        gl4.glDeleteSamplers(1, samplerName, 0);
        gl4.glDeleteVertexArrays(1, vertexArrayName, 0);

        return true;
    }
}
