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
import glm.glm;
import glm.mat._4.Mat4;
import framework.BufferUtils;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import glm.vec._2.Vec2;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import jglm.Vec2i;

/**
 *
 * @author GBarbieri
 */
public class Gl_430_fbo_invalidate extends Test {

    public static void main(String[] args) {
        Gl_430_fbo_invalidate gl_430_fbo_invalidate = new Gl_430_fbo_invalidate();
    }

    public Gl_430_fbo_invalidate() {
        super("gl-430-fbo-invalidate", Profile.CORE, 4, 3);
    }

    private final String SHADERS_SOURCE_AA = "fbo-invalidate-render";
    private final String SHADERS_SOURCE_SPLASH = "fbo-invalidate-splash";
    private final String SHADERS_ROOT = "src/data/gl_430";

    private Vec2i FRAMEBUFFER_SIZE = new Vec2i(80, 60);
    private float[] vertexData;

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int TRANSFORM = 1;
        public static final int MAX = 2;
    }

    private class Texture {

        public static final int MULTISAMPLE = 0;
        public static final int COLOR = 1;
        public static final int MAX = 2;
    }

    private class Framebuffer {

        public static final int MULTISAMPLE = 0;
        public static final int COLOR = 1;
        public static final int MAX = 2;
    }

    private class Pipeline {

        public static final int MULTISAMPLE = 0;
        public static final int SPLASH = 1;
        public static final int MAX = 2;
    }

    private IntBuffer pipelineName = GLBuffers.newDirectIntBuffer(Pipeline.MAX),
            programName = GLBuffers.newDirectIntBuffer(Pipeline.MAX), 
            vertexArrayName = GLBuffers.newDirectIntBuffer(Pipeline.MAX), 
            framebufferName = GLBuffers.newDirectIntBuffer(Framebuffer.MAX),
            bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX), 
            textureName = GLBuffers.newDirectIntBuffer(Texture.MAX);

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;
        validated = validated && checkExtension(gl4, "GL_ARB_invalidate_subdata");

        if (validated) {
            validated = initState(gl4);
        }
        if (validated) {
            validated = initProgram(gl4);
        }
        if (validated) {
            validated = initBuffer(gl4);
        }
        if (validated) {
            validated = initTexture(gl4);
        }
        if (validated) {
            validated = initFramebuffer(gl4);
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
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_AA, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_AA, "frag", null, true);

            shaderProgram.init(gl4);
            programName.put(Pipeline.MULTISAMPLE, shaderProgram.program());
            gl4.glProgramParameteri(programName.get(Pipeline.MULTISAMPLE), GL_PROGRAM_SEPARABLE, GL_TRUE);
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
            programName.put(Pipeline.SPLASH, shaderProgram.program());
            gl4.glProgramParameteri(programName.get(Pipeline.SPLASH), GL_PROGRAM_SEPARABLE, GL_TRUE);
            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);
            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            gl4.glGenProgramPipelines(Pipeline.MAX, pipelineName);
            gl4.glUseProgramStages(pipelineName.get(Pipeline.MULTISAMPLE), GL_VERTEX_SHADER_BIT
                    | GL_FRAGMENT_SHADER_BIT, programName.get(Pipeline.MULTISAMPLE));
            gl4.glUseProgramStages(pipelineName.get(Pipeline.SPLASH), GL_VERTEX_SHADER_BIT
                    | GL_FRAGMENT_SHADER_BIT, programName.get(Pipeline.SPLASH));
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        int count = 360;
        float step = 360.f / count;

        vertexData = new float[2 * count];
        for (int i = 0; i < count; ++i) {
            vertexData[i * 2 + 0] = (float) Math.sin(Math.toRadians(step * i));
            vertexData[i * 2 + 1] = (float) Math.cos(Math.toRadians(step * i));
        }

        gl4.glGenBuffers(Buffer.MAX, bufferName);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl4.glBufferData(GL_ARRAY_BUFFER, vertexData.length * Float.BYTES, vertexBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(vertexBuffer);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        int[] uniformBufferOffset = {0};
        gl4.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset, 0);
        int uniformBlockSize = Math.max(Mat4.SIZE, uniformBufferOffset[0]);

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.TRANSFORM));
        gl4.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_DYNAMIC_DRAW);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return true;
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(Pipeline.MAX, vertexArrayName);

        gl4.glBindVertexArray(vertexArrayName.get(Pipeline.MULTISAMPLE));
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, Vec2.SIZE, 0);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
        }
        gl4.glBindVertexArray(0);

        gl4.glBindVertexArray(vertexArrayName.get(Pipeline.SPLASH));
        gl4.glBindVertexArray(0);

        return true;
    }

    private boolean initFramebuffer(GL4 gl4) {

        gl4.glGenFramebuffers(Framebuffer.MAX, framebufferName);

        gl4.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName.get(Framebuffer.MULTISAMPLE));
        gl4.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, textureName.get(Texture.MULTISAMPLE), 0);
        if (!isFramebufferComplete(gl4, framebufferName.get(Framebuffer.MULTISAMPLE))) {
            return false;
        }
        gl4.glBindFramebuffer(GL_FRAMEBUFFER, 0);

        gl4.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName.get(Framebuffer.COLOR));
        gl4.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, textureName.get(Texture.COLOR), 0);
        if (!isFramebufferComplete(gl4, framebufferName.get(Framebuffer.COLOR))) {
            return false;
        }
        gl4.glBindFramebuffer(GL_FRAMEBUFFER, 0);

        return true;
    }

    private boolean initTexture(GL4 gl4) {

        gl4.glGenTextures(Texture.MAX, textureName);

        {
            gl4.glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, textureName.get(Texture.MULTISAMPLE));
            gl4.glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, 8, GL_RGBA8,
                    FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y, true);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            gl4.glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, 0);
        }

        {
            gl4.glBindTexture(GL_TEXTURE_2D, textureName.get(Texture.COLOR));
            gl4.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y, 0,
                    GL_RGBA, GL_UNSIGNED_BYTE, null);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            gl4.glBindTexture(GL_TEXTURE_2D, 0);
        }

        return true;
    }

    private boolean initState(GL4 gl4) {

        gl4.glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);

        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        {
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.TRANSFORM));
            ByteBuffer pointer = gl4.glMapBufferRange(
                    GL_UNIFORM_BUFFER, 0, Mat4.SIZE,
                    GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            Mat4 projection = glm.perspectiveFov_((float) Math.PI * 0.25f, windowSize.x, windowSize.y, 0.1f, 100.0f);
            Mat4 model = new Mat4(1.0f);

            pointer.asFloatBuffer().put(projection.mul(viewMat4()).mul(model).toFa_());

            // Make sure the uniform buffer is uploaded
            gl4.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        //////////////////////////////
        // Render multisampled texture
        gl4.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName.get(Framebuffer.MULTISAMPLE));

        gl4.glViewportIndexedf(0, 0, 0, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y);
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 0.5f, 0.0f, 1.0f}, 0);

        gl4.glBindProgramPipeline(pipelineName.get(Pipeline.MULTISAMPLE));
        gl4.glBindVertexArray(vertexArrayName.get(Pipeline.MULTISAMPLE));
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName.get(Buffer.TRANSFORM));

        gl4.glEnable(GL_MULTISAMPLE);
        {
            gl4.glDrawArraysInstancedBaseInstance(GL_LINE_LOOP, 0, vertexData.length / 2, 3, 0);
        }
        gl4.glDisable(GL_MULTISAMPLE);

        //////////////////////////
        // Resolving multisampling
        gl4.glBindFramebuffer(GL_READ_FRAMEBUFFER, framebufferName.get(Framebuffer.MULTISAMPLE));
        gl4.glBindFramebuffer(GL_DRAW_FRAMEBUFFER, framebufferName.get(Framebuffer.COLOR));
        gl4.glBlitFramebuffer(
                0, 0, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y,
                0, 0, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y,
                GL_COLOR_BUFFER_BIT, GL_NEAREST);

        int[] attachments = {GL_COLOR_ATTACHMENT0};
        gl4.glInvalidateFramebuffer(GL_READ_FRAMEBUFFER, 1, attachments, 0);

        gl4.glBindFramebuffer(GL_FRAMEBUFFER, 0);

        //////////////////////////////////////
        // Render resolved multisample texture
        gl4.glViewportIndexedf(0, 0, 0, windowSize.x, windowSize.y);
        gl4.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        //glDisable(GL_MULTISAMPLE);
        gl4.glActiveTexture(GL_TEXTURE0);
        //glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, TextureName[texture::MULTISAMPLE]);
        gl4.glBindTexture(GL_TEXTURE_2D, textureName.get(Texture.COLOR));
        gl4.glBindVertexArray(vertexArrayName.get(Pipeline.SPLASH));
        gl4.glBindProgramPipeline(pipelineName.get(Pipeline.SPLASH));

        gl4.glDrawArraysInstancedBaseInstance(GL_TRIANGLES, 0, 6, 1, 0);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteBuffers(Texture.MAX, textureName);
        BufferUtils.destroyDirectBuffer(textureName);
        gl4.glDeleteProgramPipelines(Pipeline.MAX, pipelineName);
        BufferUtils.destroyDirectBuffer(pipelineName);
        gl4.glDeleteProgram(programName.get(Pipeline.MULTISAMPLE));
        gl4.glDeleteProgram(programName.get(Pipeline.SPLASH));
        BufferUtils.destroyDirectBuffer(programName);
        gl4.glDeleteBuffers(Buffer.MAX, bufferName);
        BufferUtils.destroyDirectBuffer(bufferName);
        gl4.glDeleteVertexArrays(Pipeline.MAX, vertexArrayName);
        BufferUtils.destroyDirectBuffer(vertexArrayName);

        return true;
    }
}
