/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_400;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2GL3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.BufferUtils;
import glm.glm;
import glm.mat._4.Mat4;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import glm.vec._2.Vec2;
import glm.vec._3.Vec3;

/**
 *
 * @author GBarbieri
 */
public class Gl_400_texture_cube extends Test {

    public static void main(String[] args) {
        Gl_400_texture_cube gl_400_texture_cube = new Gl_400_texture_cube();
    }

    public Gl_400_texture_cube() {
        super("gl-400-texture-cube", Profile.CORE, 4, 0, new Vec2(Math.PI * 0.1f));
    }

    private final String SHADERS_SOURCE = "texture-cube";
    private final String SHADERS_ROOT = "src/data/gl_400";
    private final String TEXTURE_DIFFUSE_DDS = "cube_rgba8_unorm.dds";

    // With DDS textures, v texture coordinate are reversed, from top to bottom
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

    private class Transform {

        public Mat4 mvp;
        public Mat4 mv;
        public Vec3 camera;

        public static final int SIZEOF = 2 * Mat4.SIZE + Vec3.SIZE;

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

    private int programName;
    private int[] vertexArrayName = {0}, textureName = {0}, samplerName = {0}, bufferName = new int[Buffer.MAX];

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

        // Create program
        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            ShaderCode vertexShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragmentShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

            shaderProgram.init(gl4);

            shaderProgram.add(vertexShaderCode);
            shaderProgram.add(fragmentShaderCode);

            programName = shaderProgram.program();

            shaderProgram.link(gl4, System.out);

        }

        // Get variables locations
        if (validated) {

            gl4.glUseProgram(programName);
            gl4.glUniform1i(gl4.glGetUniformLocation(programName, "diffuse"), 0);
            gl4.glUseProgram(0);

            gl4.glUniformBlockBinding(programName, gl4.glGetUniformBlockIndex(programName, "Transform"),
                    Semantic.Uniform.TRANSFORM0);
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
        gl4.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset, 0);
        int uniformBlockSize = Math.max(Transform.SIZEOF, uniformBufferOffset[0]);

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
        gl4.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_DYNAMIC_DRAW);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return true;
    }

    private boolean initSampler(GL4 gl4) {

        gl4.glGenSamplers(1, samplerName, 0);
        gl4.glSamplerParameteri(samplerName[0], GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        gl4.glSamplerParameteri(samplerName[0], GL_TEXTURE_MAG_FILTER, GL_LINEAR);
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

        try {
            gl4.glActiveTexture(GL_TEXTURE0);
            gl4.glGenTextures(1, textureName, 0);
            gl4.glBindTexture(GL_TEXTURE_CUBE_MAP_ARRAY, textureName[0]);
            gl4.glTexParameteri(GL_TEXTURE_CUBE_MAP_ARRAY, GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTexParameteri(GL_TEXTURE_CUBE_MAP_ARRAY, GL_TEXTURE_MAX_LEVEL, 0);

            jgli.Texture texture = jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE_DDS);
            assert (!texture.empty());

            jgli.Gl.Format format = jgli.Gl.translate(texture.format());
            jgli.Gl.Target target = jgli.Gl.translate(texture.target());

            gl4.glTexImage3D(
                    target.value, 0,
                    format.internal.value,
                    texture.dimensions()[0], texture.dimensions()[1], texture.faces(),
                    0,
                    format.external.value, format.type.value,
                    texture.data());

        } catch (IOException ex) {
            Logger.getLogger(Gl_400_texture_cube.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(1, vertexArrayName, 0);
        gl4.glBindVertexArray(vertexArrayName[0]);
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
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
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
            ByteBuffer pointer = gl4.glMapBufferRange(
                    GL_UNIFORM_BUFFER, 0, Transform.SIZEOF,
                    GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, (float) windowSize.x / windowSize.y, 0.1f, 1000.0f);
            Mat4 view = viewMat4();
            Mat4 model = new Mat4(1.0f);
            Mat4 mvp = projection.mul(view).mul(model);
            Mat4 mv = view.mul(model);

            Transform transform = new Transform(mvp, mv, new Vec3(0.0f, 0.0f, -cameraDistance()));

            pointer.asFloatBuffer().put(transform.toFa_());

            // Make sure the uniform buffer is uploaded
            gl4.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        gl4.glViewport(0, 0, windowSize.x, windowSize.y);
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, 0);

        gl4.glUseProgram(programName);
        gl4.glActiveTexture(GL_TEXTURE0);
        gl4.glBindTexture(GL_TEXTURE_CUBE_MAP_ARRAY, textureName[0]);
        gl4.glBindSampler(0, samplerName[0]);
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.TRANSFORM]);
        gl4.glBindVertexArray(vertexArrayName[0]);

        gl4.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 1);

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
