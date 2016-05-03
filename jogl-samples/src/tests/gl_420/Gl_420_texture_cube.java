/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_420;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2GL3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import glm.glm;
import glm.mat._4.Mat4;
import framework.BufferUtils;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import jgli.TextureCube;
import glm.vec._2.Vec2;
import glm.vec._3.Vec3;
import java.nio.IntBuffer;

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
    private int vertexSize = vertexCount * Vec2.SIZE;
    private Vec2[] vertexData = {
        new Vec2(-1.0f, -1.0f).mul(4.0f),
        new Vec2(+1.0f, -1.0f).mul(4.0f),
        new Vec2(+1.0f, +1.0f).mul(4.0f),
        new Vec2(+1.0f, +1.0f).mul(4.0f),
        new Vec2(-1.0f, +1.0f).mul(4.0f),
        new Vec2(-1.0f, -1.0f).mul(4.0f)};

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int TRANSFORM = 1;
        public static final int MAX = 2;
    }

    private class Transform {

        public Mat4 mvp;
        public Mat4 mv;
        public Vec3 camera;

        public static final int SIZE = 2 * Mat4.SIZE + Vec3.SIZE;

        public Transform(Mat4 mvp, Mat4 mv, Vec3 camera) {
            this.mvp = mvp;
            this.mv = mv;
            this.camera = camera;
        }

        public ByteBuffer toDbb(ByteBuffer dbb) {
            mvp.toDbb(dbb, 0);
            mv.toDbb(dbb, Mat4.SIZE);
            camera.toDbb(dbb, Mat4.SIZE * 2);
            return dbb;
        }
    };

    private IntBuffer pipelineName = GLBuffers.newDirectIntBuffer(1), vertexArrayName = GLBuffers.newDirectIntBuffer(1),
            textureName = GLBuffers.newDirectIntBuffer(1), samplerName = GLBuffers.newDirectIntBuffer(1),
            bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX);
    private int programName;

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

        gl4.glGenProgramPipelines(1, pipelineName);

        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER, this.getClass(), SHADERS_ROOT, null,
                    SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER, this.getClass(), SHADERS_ROOT, null,
                    SHADERS_SOURCE, "frag", null, true);

            shaderProgram.init(gl4);
            programName = shaderProgram.program();
            gl4.glProgramParameteri(programName, GL_PROGRAM_SEPARABLE, GL_TRUE);

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);

            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            gl4.glUseProgramStages(pipelineName.get(0), GL_VERTEX_SHADER_BIT | GL_FRAGMENT_SHADER_BIT, programName);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        ByteBuffer vertexBuffer = GLBuffers.newDirectByteBuffer(vertexSize);
        IntBuffer uniformBufferOffset = GLBuffers.newDirectIntBuffer(1);

        gl4.glGenBuffers(Buffer.MAX, bufferName);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
        for (int i = 0; i < vertexCount; i++) {
            vertexData[i].toDbb(vertexBuffer, i * Vec2.SIZE);
        }
        gl4.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        gl4.glGetIntegerv(
                GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT,
                uniformBufferOffset);

        int uniformBlockSize = Math.max(Transform.SIZE, uniformBufferOffset.get(0));

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.TRANSFORM));
        gl4.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_DYNAMIC_DRAW);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        BufferUtils.destroyDirectBuffer(vertexBuffer);
        BufferUtils.destroyDirectBuffer(uniformBufferOffset);

        return true;
    }

    private boolean initSampler(GL4 gl4) {

        FloatBuffer borderColor = GLBuffers.newDirectFloatBuffer(new float[]{0.0f, 0.0f, 0.0f, 0.0f});

        gl4.glGenSamplers(1, samplerName);
        gl4.glSamplerParameteri(samplerName.get(0), GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_NEAREST);
        gl4.glSamplerParameteri(samplerName.get(0), GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        gl4.glSamplerParameteri(samplerName.get(0), GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        gl4.glSamplerParameteri(samplerName.get(0), GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        gl4.glSamplerParameteri(samplerName.get(0), GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        gl4.glSamplerParameterfv(samplerName.get(0), GL_TEXTURE_BORDER_COLOR, borderColor);
        gl4.glSamplerParameterf(samplerName.get(0), GL_TEXTURE_MIN_LOD, -1000.f);
        gl4.glSamplerParameterf(samplerName.get(0), GL_TEXTURE_MAX_LOD, 1000.f);
        gl4.glSamplerParameterf(samplerName.get(0), GL_TEXTURE_LOD_BIAS, 0.0f);
        gl4.glSamplerParameteri(samplerName.get(0), GL_TEXTURE_COMPARE_MODE, GL_NONE);
        gl4.glSamplerParameteri(samplerName.get(0), GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);
        gl4.glSamplerParameterf(samplerName.get(0), GL_TEXTURE_MAX_ANISOTROPY_EXT, 16.0f);

        BufferUtils.destroyDirectBuffer(borderColor);

        return true;
    }

    private boolean initTexture(GL4 gl4) {

        gl4.glActiveTexture(GL_TEXTURE0);
        gl4.glGenTextures(1, textureName);
        gl4.glBindTexture(GL_TEXTURE_CUBE_MAP_ARRAY, textureName.get(0));
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

        gl4.glGenVertexArrays(1, vertexArrayName);
        gl4.glBindVertexArray(vertexArrayName.get(0));
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
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
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.TRANSFORM));
            ByteBuffer pointer = gl4.glMapBufferRange(
                    GL_UNIFORM_BUFFER, 0, Transform.SIZE,
                    GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, (float) windowSize.x / windowSize.y, 0.1f, 1000.0f);
            Mat4 view = viewMat4();
            Mat4 model = new Mat4(1.0f);
            Mat4 mvp = projection.mul(view).mul(model);
            Mat4 mv = view.mul(model);

            Transform transform = new Transform(mvp, mv, new Vec3(0.0f, 0.0f, -cameraDistance()));

            transform.toDbb(pointer);

            // Make sure the uniform buffer is uploaded
            gl4.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        gl4.glViewportIndexedf(0, 0, 0, windowSize.x, windowSize.y);
        gl4.glClearBufferfv(GL_COLOR, 0, clearColor.put(0, 1.0f).put(1, 1.0f).put(2, 1.0f).put(3, 1.0f));

        gl4.glBindProgramPipeline(pipelineName.get(0));
        gl4.glActiveTexture(GL_TEXTURE0);
        gl4.glBindTexture(GL_TEXTURE_CUBE_MAP_ARRAY, textureName.get(0));
        gl4.glBindSampler(0, samplerName.get(0));
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName.get(Buffer.TRANSFORM));

        gl4.glBindVertexArray(vertexArrayName.get(0));
        gl4.glDrawArraysInstancedBaseInstance(GL_TRIANGLES, 0, vertexCount, 1, 0);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteBuffers(Buffer.MAX, bufferName);
        gl4.glDeleteProgram(programName);
        gl4.glDeleteTextures(1, textureName);
        gl4.glDeleteSamplers(1, samplerName);
        gl4.glDeleteVertexArrays(1, vertexArrayName);
        gl4.glDeleteProgramPipelines(1, pipelineName);

        BufferUtils.destroyDirectBuffer(bufferName);
        BufferUtils.destroyDirectBuffer(textureName);
        BufferUtils.destroyDirectBuffer(samplerName);
        BufferUtils.destroyDirectBuffer(vertexArrayName);
        BufferUtils.destroyDirectBuffer(pipelineName);

        return true;
    }
}
