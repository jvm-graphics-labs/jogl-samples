/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_500;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import glm.glm;
import glm.mat._4.Mat4;
import glm.vec._2.Vec2;
import glm.vec._3.Vec3;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import jgli.TextureCube;

/**
 *
 * @author GBarbieri
 */
public class Gl_500_texture_cube_arb extends Test {

    public static void main(String[] args) {
        Gl_500_texture_cube_arb gl_500_texture_cube_arb = new Gl_500_texture_cube_arb();
    }

    public Gl_500_texture_cube_arb() {
        super("gl-500-texture-cube-arb", Profile.CORE, 4, 5, new Vec2(Math.PI * 0.1f));
    }

    private final String SHADERS_SOURCE = "texture-cube";
    private final String SHADERS_ROOT = "src/data/gl_500";

    private int vertexCount = 6;
    private int vertexSize = vertexCount * Vec2.SIZE;
    private float[] vertexData = {
        -1.0f, -1.0f,
        +1.0f, -1.0f,
        +1.0f, +1.0f,
        +1.0f, +1.0f,
        -1.0f, +1.0f,
        -1.0f, -1.0f};

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int TRANSFORM = 1;
        public static final int MAX = 2;
    }

    private class Sampler {

        public static final int SEAMLESS = 0;
        public static final int NON_SEAMLESS = 1;
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

        public float[] toFa_() {
            return new float[]{
                mvp.m00, mvp.m01, mvp.m02, mvp.m03, mvp.m10, mvp.m11, mvp.m12, mvp.m13,
                mvp.m20, mvp.m21, mvp.m22, mvp.m23, mvp.m30, mvp.m31, mvp.m32, mvp.m33,
                mv.m00, mv.m01, mv.m02, mv.m03, mv.m10, mv.m11, mv.m12, mv.m13,
                mv.m20, mv.m21, mv.m22, mv.m23, mv.m30, mv.m31, mv.m32, mv.m33,
                camera.x, camera.y, camera.z};
        }
    };

    private IntBuffer bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX),
            samplerName = GLBuffers.newDirectIntBuffer(Sampler.MAX), pipelineName = GLBuffers.newDirectIntBuffer(1),
            vertexArrayName = GLBuffers.newDirectIntBuffer(1), textureName = GLBuffers.newDirectIntBuffer(1);
    private int programName;
    private ByteBuffer transformPointer;
    private FloatBuffer black = GLBuffers.newDirectFloatBuffer(new float[]{0.0f, 0.0f, 0.0f, 0.0f});
    private FloatBuffer white = GLBuffers.newDirectFloatBuffer(new float[]{1.0f, 1.0f, 1.0f, 1.0f});

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = checkExtension(gl4, "GL_ARB_seamless_cubemap_per_texture");
        validated = validated && checkExtension(gl4, "GL_ARB_shader_storage_buffer_object");
        validated = validated && checkExtension(gl4, "GL_ARB_buffer_storage");
        validated = validated && checkExtension(gl4, "GL_ARB_multi_bind");

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
        
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.TRANSFORM));
        transformPointer = gl4.glMapBufferRange(GL_UNIFORM_BUFFER,
                0, Transform.SIZE,
                GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

        return validated;
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

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

            gl4.glGenProgramPipelines(1, pipelineName);
            gl4.glUseProgramStages(pipelineName.get(0), GL_VERTEX_SHADER_BIT | GL_FRAGMENT_SHADER_BIT, programName);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        gl4.glGenBuffers(Buffer.MAX, bufferName);

        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);

        gl4.glBindBuffer(GL_SHADER_STORAGE_BUFFER, bufferName.get(Buffer.VERTEX));
        gl4.glBufferStorage(GL_SHADER_STORAGE_BUFFER, vertexSize, vertexBuffer, 0);
        gl4.glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);

        IntBuffer uniformBufferOffset = GLBuffers.newDirectIntBuffer(1);
        gl4.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset);
        int uniformBlockSize = Math.max(Transform.SIZE, uniformBufferOffset.get(0));

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.TRANSFORM));
        gl4.glBufferStorage(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT
                | GL_MAP_COHERENT_BIT);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return true;
    }

    private boolean initSampler(GL4 gl4) {

        gl4.glGenSamplers(Sampler.MAX, samplerName);

        gl4.glSamplerParameteri(samplerName.get(Sampler.SEAMLESS), GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        gl4.glSamplerParameteri(samplerName.get(Sampler.SEAMLESS), GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        gl4.glSamplerParameteri(samplerName.get(Sampler.SEAMLESS), GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        gl4.glSamplerParameteri(samplerName.get(Sampler.SEAMLESS), GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        gl4.glSamplerParameteri(samplerName.get(Sampler.SEAMLESS), GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        gl4.glSamplerParameterfv(samplerName.get(Sampler.SEAMLESS), GL_TEXTURE_BORDER_COLOR, black);
        gl4.glSamplerParameterf(samplerName.get(Sampler.SEAMLESS), GL_TEXTURE_MIN_LOD, -1000.f);
        gl4.glSamplerParameterf(samplerName.get(Sampler.SEAMLESS), GL_TEXTURE_MAX_LOD, 1000.f);
        gl4.glSamplerParameterf(samplerName.get(Sampler.SEAMLESS), GL_TEXTURE_LOD_BIAS, 0.0f);
        gl4.glSamplerParameteri(samplerName.get(Sampler.SEAMLESS), GL_TEXTURE_COMPARE_MODE, GL_NONE);
        gl4.glSamplerParameteri(samplerName.get(Sampler.SEAMLESS), GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);
        gl4.glSamplerParameterf(samplerName.get(Sampler.SEAMLESS), GL_TEXTURE_MAX_ANISOTROPY_EXT, 1.0f);
        gl4.glSamplerParameteri(samplerName.get(Sampler.SEAMLESS), GL_TEXTURE_CUBE_MAP_SEAMLESS, GL_TRUE);

        gl4.glSamplerParameteri(samplerName.get(Sampler.NON_SEAMLESS), GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        gl4.glSamplerParameteri(samplerName.get(Sampler.NON_SEAMLESS), GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        gl4.glSamplerParameteri(samplerName.get(Sampler.NON_SEAMLESS), GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        gl4.glSamplerParameteri(samplerName.get(Sampler.NON_SEAMLESS), GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        gl4.glSamplerParameteri(samplerName.get(Sampler.NON_SEAMLESS), GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        gl4.glSamplerParameterfv(samplerName.get(Sampler.NON_SEAMLESS), GL_TEXTURE_BORDER_COLOR, black);
        gl4.glSamplerParameterf(samplerName.get(Sampler.NON_SEAMLESS), GL_TEXTURE_MIN_LOD, -1000.f);
        gl4.glSamplerParameterf(samplerName.get(Sampler.NON_SEAMLESS), GL_TEXTURE_MAX_LOD, 1000.f);
        gl4.glSamplerParameterf(samplerName.get(Sampler.NON_SEAMLESS), GL_TEXTURE_LOD_BIAS, 0.0f);
        gl4.glSamplerParameteri(samplerName.get(Sampler.NON_SEAMLESS), GL_TEXTURE_COMPARE_MODE, GL_NONE);
        gl4.glSamplerParameteri(samplerName.get(Sampler.NON_SEAMLESS), GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);
        gl4.glSamplerParameterf(samplerName.get(Sampler.NON_SEAMLESS), GL_TEXTURE_MAX_ANISOTROPY_EXT, 1.0f);
        gl4.glSamplerParameteri(samplerName.get(Sampler.NON_SEAMLESS), GL_TEXTURE_CUBE_MAP_SEAMLESS, GL_FALSE);

        return true;
    }

    private boolean initTexture(GL4 gl4) {

        jgli.TextureCube texture = new TextureCube(jgli.Format.FORMAT_RGBA8_UNORM_PACK8, new int[]{2, 2}, 1);
        assert (!texture.empty());

        jgli.Gl.Format format = jgli.Gl.translate(texture.format());

        texture.clearFace(0, new byte[]{(byte) 255, (byte) 0, (byte) 0, (byte) 255});

        texture.clearFace(1, new byte[]{(byte) 255, (byte) 128, (byte) 0, (byte) 255});
        texture.clearFace(2, new byte[]{(byte) 255, (byte) 255, (byte) 0, (byte) 255});
        texture.clearFace(3, new byte[]{(byte) 0, (byte) 255, (byte) 0, (byte) 255});
        texture.clearFace(4, new byte[]{(byte) 0, (byte) 255, (byte) 255, (byte) 255});
        texture.clearFace(5, new byte[]{(byte) 0, (byte) 0, (byte) 255, (byte) 255});

        gl4.glGenTextures(1, textureName);
        gl4.glBindTexture(GL_TEXTURE_CUBE_MAP_ARRAY, textureName.get(0));
        gl4.glTexParameteri(GL_TEXTURE_CUBE_MAP_ARRAY, GL_TEXTURE_BASE_LEVEL, 0);
        gl4.glTexParameteri(GL_TEXTURE_CUBE_MAP_ARRAY, GL_TEXTURE_MAX_LEVEL, 0);

        gl4.glTexStorage3D(GL_TEXTURE_CUBE_MAP_ARRAY, texture.levels(),
                format.internal.value, texture.dimensions()[0], texture.dimensions()[1], texture.faces());

        gl4.glTexSubImage3D(GL_TEXTURE_CUBE_MAP_ARRAY, 0,
                0, 0, 0,
                texture.dimensions()[0],
                texture.dimensions()[1],
                texture.faces(),
                format.external.value, format.type.value,
                texture.data());

        return true;
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(1, vertexArrayName);
        gl4.glBindVertexArray(vertexArrayName.get(0));
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, Vec2.SIZE, 0);
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
            Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, windowSize.x * 0.5f / windowSize.y, 0.1f, 1000.0f);
            Mat4 view = viewMat4();
            Mat4 model = new Mat4(1.0f);
            Mat4 mvp = projection.mul(view).mul(model);
            Mat4 mv = view.mul(model);

            Transform t = new Transform(mvp, mv, new Vec3(0.0f, 0.0f, -cameraDistance()));

            transformPointer.asFloatBuffer().put(t.toFa_());
        }

        gl4.glClearBufferfv(GL_COLOR, 0, white);

        gl4.glBindProgramPipeline(pipelineName.get(0));
        gl4.glBindTextures(Semantic.Sampler.DIFFUSE, 1, textureName);
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName.get(Buffer.TRANSFORM));
        gl4.glBindVertexArray(vertexArrayName.get(0));

        // Left side: seamless cubemap filtering
        gl4.glViewportIndexedf(0, 0, 0, windowSize.x * 0.5f, windowSize.y);
        samplerName.position(Sampler.SEAMLESS);
        gl4.glBindSamplers(Semantic.Sampler.DIFFUSE, 1, samplerName);

        gl4.glDrawArraysInstancedBaseInstance(GL_TRIANGLES, 0, vertexCount, 1, 0);
        
        // Right side: per face cubemap filtering
        gl4.glViewportIndexedf(0, windowSize.x * 0.5f, 0, windowSize.x * 0.5f, windowSize.y);
        samplerName.position(Sampler.NON_SEAMLESS);
        gl4.glBindSamplers(Semantic.Sampler.DIFFUSE, 1, samplerName);

        samplerName.rewind();

        gl4.glDrawArraysInstancedBaseInstance(GL_TRIANGLES, 0, vertexCount, 1, 0);

        return true;
    }
}
