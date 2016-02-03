/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_450;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import core.glm;
import dev.Mat4;
import dev.Vec2;
import dev.Vec2i;
import dev.Vec3;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import jgli.Texture2d;

/**
 *
 * @author GBarbieri
 */
public class Gl_450_fbo_multisample_explicit extends Test {

    public static void main(String[] args) {
        Gl_450_fbo_multisample_explicit gl_450_fbo_multisample_explicit = new Gl_450_fbo_multisample_explicit();
    }

    public Gl_450_fbo_multisample_explicit() {
        super("gl-450-fbo-multisample-explicit", Profile.CORE, 4, 5,
                new jglm.Vec2((float) Math.PI * 0.2f, (float) Math.PI * 0.2f));
    }

    private final String VERT_SHADER_SOURCE = "fbo-multisample-explicit";
    private final String[] FRAG_SHADERS_SOURCE = {
        "fbo-multisample-explicit-texture",
        "fbo-multisample-explicit-box",
        "fbo-multisample-explicit-near"
    };
    private final String SHADERS_ROOT = "src/data/gl_450";
    private final String TEXTURE_DIFFUSE = "kueken7_rgba8_srgb.dds";
    private final Vec2i FRAMEBUFFER_SIZE = new Vec2i(160, 120);

    // With DDS textures, v texture coordinate are reversed, from top to bottom
    private int vertexCount = 6;
    private int vertexSize = vertexCount * glf.Vertex_v2fv2f.SIZE;
    private float[] vertexData = {
        -4.0f, -3.0f,/**/ 0.0f, 1.0f,
        +4.0f, -3.0f,/**/ 1.0f, 1.0f,
        +4.0f, +3.0f,/**/ 1.0f, 0.0f,
        +4.0f, +3.0f,/**/ 1.0f, 0.0f,
        -4.0f, +3.0f,/**/ 0.0f, 0.0f,
        -4.0f, -3.0f,/**/ 0.0f, 1.0f};

    private class Program {

        public static final int THROUGH = 0;
        public static final int RESOLVE_BOX = 1;
        public static final int RESOLVE_NEAR = 2;
        public static final int MAX = 3;
    }

    private class Texture {

        public static final int COLORBUFFER = 0;
        public static final int MULTISAMPLE_DEPTHBUFFER = 1;
        public static final int MULTISAMPLE_COLORBUFFER = 2;
        public static final int DIFFUSE = 3;
        public static final int MAX = 4;
    }

    private class Shader {

        public static final int VERT = 0;
        public static final int FRAG_TEXTURE = 1;
        public static final int FRAG_BOX = 2;
        public static final int FRAG_NEAR = 3;
        public static final int MAX = 4;
    }

    private class Framebuffer {

        public static final int RENDER = 0;
        public static final int RESOLVE = 1;
        public static final int MAX = 2;
    }

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int TRANSFORM = 1;
        public static final int MAX = 2;
    }

    private int[] framebufferName = new int[Framebuffer.MAX], pipelineName = new int[Program.MAX],
            programName = new int[Program.MAX], textureName = new int[Texture.MAX], bufferName = new int[Buffer.MAX],
            vertexArrayName = {0};
    private int uniformBlockSize;
    private ByteBuffer uniformPointer;

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = checkExtension(gl4, "GL_ARB_shader_texture_image_samples");

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
            validated = initFramebuffer(gl4);
        }

        return validated;
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        ShaderCode[] shaderCodes = new ShaderCode[Shader.MAX];

        shaderCodes[Shader.VERT] = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                this.getClass(), SHADERS_ROOT, null, VERT_SHADER_SOURCE, "vert", null, true);

        for (int i = 0; i < Program.MAX; i++) {

            shaderCodes[Shader.FRAG_TEXTURE + i] = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, FRAG_SHADERS_SOURCE[i], "frag", null, true);

            ShaderProgram shaderProgram = new ShaderProgram();
            shaderProgram.init(gl4);

            programName[i] = shaderProgram.program();

            gl4.glProgramParameteri(programName[i], GL_PROGRAM_SEPARABLE, GL_TRUE);

            shaderProgram.add(shaderCodes[Shader.VERT]);
            shaderProgram.add(shaderCodes[Shader.FRAG_TEXTURE + i]);
            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            gl4.glGenProgramPipelines(Program.MAX, pipelineName, 0);
            for (int i = 0; i < pipelineName.length; i++) {
                gl4.glUseProgramStages(pipelineName[i], GL_VERTEX_SHADER_BIT | GL_FRAGMENT_SHADER_BIT, programName[i]);
            }
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        int[] uniformBufferOffset = {0};
        gl4.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset, 0);
        uniformBlockSize = Math.max(Mat4.SIZE, uniformBufferOffset[0]);

        gl4.glCreateBuffers(Buffer.MAX, bufferName, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl4.glBufferStorage(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, 0);

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
        gl4.glBufferStorage(GL_UNIFORM_BUFFER, uniformBlockSize * 2, null,
                GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT);

        uniformPointer = gl4.glMapBufferRange(GL_UNIFORM_BUFFER, 0, uniformBlockSize * 2,
                GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return true;
    }

    private boolean initTexture(GL4 gl4) {

        try {
            jgli.Texture2d texture = new Texture2d(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE));

            gl4.glGenTextures(Texture.MAX, textureName, 0);
            gl4.glActiveTexture(GL_TEXTURE0);
            gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.DIFFUSE]);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

            jgli.Gl.Format format = jgli.Gl.translate(texture.format());
            for (int level = 0; level < texture.levels(); ++level) {
                gl4.glTexImage2D(GL_TEXTURE_2D, level,
                        format.internal.value,
                        texture.dimensions(level)[0], texture.dimensions(level)[1],
                        0,
                        format.external.value, format.type.value,
                        texture.data(level));
            }

            gl4.glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, textureName[Texture.MULTISAMPLE_COLORBUFFER]);
            gl4.glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, 4, GL_RGBA8, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y,
                    true);
            gl4.glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, textureName[Texture.MULTISAMPLE_DEPTHBUFFER]);
            gl4.glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, 4, GL_DEPTH_COMPONENT24, FRAMEBUFFER_SIZE.x,
                    FRAMEBUFFER_SIZE.y, true);
            gl4.glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, 0);

            gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.COLORBUFFER]);
            gl4.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y, 0, GL_RGBA,
                    GL_UNSIGNED_BYTE, null);
            gl4.glBindTexture(GL_TEXTURE_2D, 0);

        } catch (IOException ex) {
            Logger.getLogger(Gl_450_fbo_multisample_explicit.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    private boolean initFramebuffer(GL4 gl4) {

        gl4.glGenFramebuffers(Framebuffer.MAX, framebufferName, 0);

        gl4.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[Framebuffer.RENDER]);
        gl4.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, textureName[Texture.MULTISAMPLE_COLORBUFFER], 0);
        gl4.glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, textureName[Texture.MULTISAMPLE_DEPTHBUFFER], 0);

        gl4.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[Framebuffer.RESOLVE]);
        gl4.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, textureName[Texture.COLORBUFFER], 0);

        if (!isFramebufferComplete(gl4, framebufferName[Framebuffer.RENDER])) {
            return false;
        }
        if (!isFramebufferComplete(gl4, framebufferName[Framebuffer.RESOLVE])) {
            return false;
        }

        gl4.glBindFramebuffer(GL_FRAMEBUFFER, 0);

        return true;
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(1, vertexArrayName, 0);
        gl4.glBindVertexArray(vertexArrayName[0]);
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, glf.Vertex_v2fv2f.SIZE, 0);
            gl4.glVertexAttribPointer(Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, glf.Vertex_v2fv2f.SIZE, Vec2.SIZE);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl4.glEnableVertexAttribArray(Semantic.Attr.TEXCOORD);
        }
        gl4.glBindVertexArray(0);

        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        {
            Mat4 perspective = glm.perspective_((float) Math.PI * 0.25f, (float) FRAMEBUFFER_SIZE.x / FRAMEBUFFER_SIZE.y,
                    0.1f, 100.0f);
            Mat4 model = new Mat4(1.0f).scale(new Vec3(0.3f));
            Mat4 mvp = perspective.mul(viewMat4()).mul(model);
            uniformPointer.position(uniformBlockSize * 0);
            uniformPointer.asFloatBuffer().put(mvp.toFA_());
        }

        {
            Mat4 perspective = glm.ortho_(-4.0f, 4.0f, -3.0f, 3.0f, 0.0f, 100.0f);
            Mat4 viewFlip = new Mat4(1.0f).scale(new Vec3(1.0f, -1.0f, 1.0f));
            Mat4 view = viewFlip.translate(new Vec3(0.0f, 0.0f, -cameraDistance() * 2.0f));
            Mat4 model = new Mat4(1.0f);
            Mat4 mvp = perspective.mul(view).mul(model);
            uniformPointer.position(uniformBlockSize * 1);
            uniformPointer.asFloatBuffer().put(mvp.toFA_());
        }
        uniformPointer.rewind();

        // Clear the framebuffer
        gl4.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        int[] buffer = {GL_BACK};
        gl4.glDrawBuffers(1, buffer, 0);
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{0.0f, 0.5f, 1.0f, 1.0f}, 0);

        // Pass 1
        // Render the scene in a multisampled framebuffer
        gl4.glEnable(GL_MULTISAMPLE);
        renderFBO(gl4, framebufferName[Framebuffer.RENDER]);
        gl4.glDisable(GL_MULTISAMPLE);

        // Pass 2
        // Resolved and render the colorbuffer from the multisampled framebuffer
        resolveMultisampling(gl4);

        return true;
    }

    private void renderFBO(GL4 gl4, int framebuffer) {

        gl4.glEnable(GL_DEPTH_TEST);

        gl4.glBindProgramPipeline(pipelineName[Program.THROUGH]);
        gl4.glBindBufferRange(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.TRANSFORM], 0,
                uniformBlockSize);

        gl4.glViewport(0, 0, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y);

        gl4.glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);
        float[] depth = {1.0f};
        gl4.glClearBufferfv(GL_DEPTH, 0, depth, 0);
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 0.5f, 0.0f, 1.0f}, 0);

        gl4.glBindTextures(0, 1, textureName, Texture.DIFFUSE);
        gl4.glBindVertexArray(vertexArrayName[0]);

        gl4.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 5);

        gl4.glDisable(GL_DEPTH_TEST);
    }

    private void resolveMultisampling(GL4 gl4) {

        gl4.glViewport(0, 0, windowSize.x, windowSize.y);
        gl4.glBindFramebuffer(GL_FRAMEBUFFER, 0);

        gl4.glBindTextures(0, 1, textureName, Texture.MULTISAMPLE_COLORBUFFER);
        gl4.glBindVertexArray(vertexArrayName[0]);
        gl4.glBindBufferRange(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.TRANSFORM],
                uniformBlockSize, uniformBlockSize);

        gl4.glEnable(GL_SCISSOR_TEST);

        // Box
        {
            gl4.glScissor(1, 1, windowSize.x / 2 - 2, windowSize.y - 2);

            gl4.glBindProgramPipeline(pipelineName[Program.RESOLVE_BOX]);
            gl4.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 5);
        }

        // Near
        {
            gl4.glScissor(windowSize.x / 2 + 1, 1, windowSize.x / 2 - 2, windowSize.y - 2);

            gl4.glBindProgramPipeline(pipelineName[Program.RESOLVE_NEAR]);
            gl4.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 5);
        }

        gl4.glDisable(GL_SCISSOR_TEST);
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        for (int i = 0; i < Program.MAX; ++i) {
            gl4.glDeleteProgram(programName[i]);
        }

        gl4.glDeleteProgramPipelines(Program.MAX, pipelineName, 0);
        gl4.glDeleteBuffers(Buffer.MAX, bufferName, 0);
        gl4.glDeleteTextures(Texture.MAX, textureName, 0);
        gl4.glDeleteFramebuffers(Framebuffer.MAX, framebufferName, 0);
        gl4.glDeleteVertexArrays(1, vertexArrayName, 0);

        return true;
    }
}
