/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_500;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2ES3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.BufferUtils;
import framework.Caps;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import glf.Vertex_v2fv2f;
import glm.glm;
import glm.mat._4.Mat4;
import glm.vec._2.Vec2;
import glm.vec._2.i.Vec2i;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 *
 * @author GBarbieri
 */
public class Gl_500_shader_group_nv extends Test {

    public static void main(String[] args) {
        Gl_500_shader_group_nv gl_500_shader_group_nv = new Gl_500_shader_group_nv();
    }

    public Gl_500_shader_group_nv() {
        super("gl-500-shader-group-nv", Profile.CORE, 4, 5, new Vec2(Math.PI * 0.2f));
    }

    private final String SHADERS_SOURCE_TEXTURE = "shader-group-nv";
    private final String SHADERS_SOURCE_BLIT = "shader-group-blit-nv";
    private final String SHADERS_ROOT = "src/data/gl_500";
    private final float FRAMEBUFFER_SCALE = 1.0f / 8.0f;

    private int vertexCount = 4;
    private int vertexSize = vertexCount * glf.Vertex_v2fv2f.SIZE;
    private float[] vertexData = {
        -1.0f, -1.0f,/**/ 0.0f, 1.0f,
        +1.0f, -1.0f,/**/ 1.0f, 1.0f,
        +1.0f, +1.0f,/**/ 1.0f, 0.0f,
        -1.0f, +1.0f,/**/ 0.0f, 0.0f};

    private int elementCount = 6;
    private int elementSize = elementCount * Short.BYTES;
    private short[] elementData = {
        0, 1, 2,
        2, 3, 0};

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int ELEMENT = 1;
        public static final int TRANSFORM = 2;
        public static final int MAX = 3;
    }

    private class Texture {

        public static final int COLORBUFFER = 0;
        public static final int RENDERBUFFER = 1;
        public static final int INVOCATION_COUNT = 2;
        public static final int MAX = 3;
    }

    private class Pipeline {

        public static final int TEXTURE = 0;
        public static final int SPLASH = 1;
        public static final int MAX = 2;
    }

    private IntBuffer framebufferName = GLBuffers.newDirectIntBuffer(1),
            vertexArrayName = GLBuffers.newDirectIntBuffer(Pipeline.MAX),
            bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX),
            textureName = GLBuffers.newDirectIntBuffer(Texture.MAX),
            pipelineName = GLBuffers.newDirectIntBuffer(Pipeline.MAX);
    private int[] programName = new int[Pipeline.MAX];
    /**
     * https://jogamp.org/bugzilla/show_bug.cgi?id=1287
     */
    private boolean bug1287 = true;
    private ByteBuffer clearColorBuffer = GLBuffers.newDirectByteBuffer(
            new byte[]{(byte) 255, (byte) 255, (byte) 255, (byte) 255});
    private FloatBuffer clearRenderBuffer = GLBuffers.newDirectFloatBuffer(new float[]{1.0f});
    private IntBuffer clearInvocationCount = GLBuffers.newDirectIntBuffer(new int[]{0});

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = gl4.isExtensionAvailable("GL_NV_shader_thread_group");

        Caps caps = new Caps(gl4, Profile.CORE);

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

        if (validated) {

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_TEXTURE, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_TEXTURE, "frag", null, true);

            ShaderProgram shaderProgram = new ShaderProgram();
            shaderProgram.init(gl4);

            programName[Pipeline.TEXTURE] = shaderProgram.program();

            gl4.glProgramParameteri(programName[Pipeline.TEXTURE], GL_PROGRAM_SEPARABLE, GL_TRUE);

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);
            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_BLIT, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_BLIT, "frag", null, true);

            ShaderProgram shaderProgram = new ShaderProgram();
            shaderProgram.init(gl4);

            programName[Pipeline.SPLASH] = shaderProgram.program();

            gl4.glProgramParameteri(programName[Pipeline.SPLASH], GL_PROGRAM_SEPARABLE, GL_TRUE);

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);
            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            gl4.glGenProgramPipelines(Pipeline.MAX, pipelineName);
            gl4.glUseProgramStages(pipelineName.get(Pipeline.TEXTURE), GL_VERTEX_SHADER_BIT | GL_FRAGMENT_SHADER_BIT,
                    programName[Pipeline.TEXTURE]);
            gl4.glUseProgramStages(pipelineName.get(Pipeline.SPLASH), GL_VERTEX_SHADER_BIT | GL_FRAGMENT_SHADER_BIT,
                    programName[Pipeline.SPLASH]);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        IntBuffer uniformBufferOffset = GLBuffers.newDirectIntBuffer(1);
        gl4.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset);
        int uniformBlockSize = Math.max(Mat4.SIZE, uniformBufferOffset.get(0));
        BufferUtils.destroyDirectBuffer(uniformBufferOffset);

        gl4.glCreateBuffers(Buffer.MAX, bufferName);

        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);

        if (!bug1287) {

            gl4.glNamedBufferStorage(bufferName.get(Buffer.ELEMENT), elementSize, elementBuffer, 0);
            gl4.glNamedBufferStorage(bufferName.get(Buffer.VERTEX), vertexSize, vertexBuffer, 0);
            gl4.glNamedBufferStorage(bufferName.get(Buffer.TRANSFORM), uniformBlockSize, null, GL_MAP_WRITE_BIT);

        } else {

            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT));
            gl4.glBufferStorage(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, 0);
            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
            gl4.glBufferStorage(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, 0);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.TRANSFORM));
            gl4.glBufferStorage(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_MAP_WRITE_BIT);
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }

        return true;
    }

    private boolean initTexture(GL4 gl4) {

        gl4.glCreateTextures(GL_TEXTURE_2D, Texture.MAX, textureName);

        Vec2i framebufferSize = new Vec2i(windowSize).mul(FRAMEBUFFER_SCALE);

        gl4.glTextureParameteri(textureName.get(Texture.COLORBUFFER), GL_TEXTURE_BASE_LEVEL, 0);
        gl4.glTextureParameteri(textureName.get(Texture.COLORBUFFER), GL_TEXTURE_MAX_LEVEL, 0);
        gl4.glTextureParameteri(textureName.get(Texture.COLORBUFFER), GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        gl4.glTextureParameteri(textureName.get(Texture.COLORBUFFER), GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        gl4.glTextureParameteri(textureName.get(Texture.COLORBUFFER), GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        gl4.glTextureParameteri(textureName.get(Texture.COLORBUFFER), GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        gl4.glTextureStorage2D(textureName.get(Texture.COLORBUFFER), 1, GL_RGBA8, framebufferSize.x,
                framebufferSize.y);

        gl4.glTextureParameteri(textureName.get(Texture.RENDERBUFFER), GL_TEXTURE_BASE_LEVEL, 0);
        gl4.glTextureParameteri(textureName.get(Texture.RENDERBUFFER), GL_TEXTURE_MAX_LEVEL, 0);
        gl4.glTextureStorage2D(textureName.get(Texture.RENDERBUFFER), 1, GL_DEPTH_COMPONENT24, framebufferSize.x,
                framebufferSize.y);

        gl4.glTextureParameteri(textureName.get(Texture.INVOCATION_COUNT), GL_TEXTURE_BASE_LEVEL, 0);
        gl4.glTextureParameteri(textureName.get(Texture.INVOCATION_COUNT), GL_TEXTURE_MAX_LEVEL, 0);
        gl4.glTextureStorage2D(textureName.get(Texture.INVOCATION_COUNT), 1, GL_R32UI, framebufferSize.x,
                framebufferSize.y);

        return true;
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glCreateVertexArrays(Pipeline.MAX, vertexArrayName);

        gl4.glVertexArrayAttribBinding(vertexArrayName.get(Pipeline.TEXTURE), Semantic.Attr.POSITION, 0);
        gl4.glVertexArrayAttribFormat(vertexArrayName.get(Pipeline.TEXTURE), Semantic.Attr.POSITION, 2, GL_FLOAT, false,
                0);
        gl4.glEnableVertexArrayAttrib(vertexArrayName.get(Pipeline.TEXTURE), Semantic.Attr.POSITION);

        gl4.glVertexArrayAttribBinding(vertexArrayName.get(Pipeline.TEXTURE), Semantic.Attr.TEXCOORD, 0);
        gl4.glVertexArrayAttribFormat(vertexArrayName.get(Pipeline.TEXTURE), Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false,
                Vec2.SIZE);
        gl4.glEnableVertexArrayAttrib(vertexArrayName.get(Pipeline.TEXTURE), Semantic.Attr.TEXCOORD);

        gl4.glVertexArrayElementBuffer(vertexArrayName.get(Pipeline.TEXTURE), bufferName.get(Buffer.ELEMENT));
        gl4.glVertexArrayVertexBuffer(vertexArrayName.get(Pipeline.TEXTURE), 0, bufferName.get(Buffer.VERTEX), 0,
                Vertex_v2fv2f.SIZE);

        return true;
    }

    private boolean initFramebuffer(GL4 gl4) {

        gl4.glCreateFramebuffers(1, framebufferName);
        gl4.glNamedFramebufferTexture(framebufferName.get(0), GL_COLOR_ATTACHMENT0, textureName.get(Texture.COLORBUFFER),
                0);
        gl4.glNamedFramebufferTexture(framebufferName.get(0), GL_DEPTH_ATTACHMENT, textureName.get(Texture.RENDERBUFFER),
                0);

        return gl4.glCheckNamedFramebufferStatus(framebufferName.get(0), GL_FRAMEBUFFER) == GL_FRAMEBUFFER_COMPLETE;
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        {
            ByteBuffer pointer = gl4.glMapNamedBufferRange(bufferName.get(Buffer.TRANSFORM),
                    0, Mat4.SIZE, GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, (float) windowSize.x / windowSize.y, 0.1f, 100.0f);
            Mat4 model = new Mat4(1.0f);

            pointer.asFloatBuffer().put(projection.mul(viewMat4()).mul(model).toFa_());

            // Make sure the uniform buffer is uploaded
            gl4.glUnmapNamedBuffer(bufferName.get(Buffer.TRANSFORM));
        }

        gl4.glEnable(GL_DEPTH_TEST);
        gl4.glDepthFunc(GL_LESS);

        gl4.glClearTexImage(textureName.get(Texture.COLORBUFFER), 0, GL_RGBA, GL_UNSIGNED_BYTE, clearColorBuffer);
        gl4.glClearTexImage(textureName.get(Texture.RENDERBUFFER), 0, GL_DEPTH_COMPONENT, GL_FLOAT, clearRenderBuffer);
        gl4.glClearTexImage(textureName.get(Texture.INVOCATION_COUNT), 0, GL_RED_INTEGER, GL_UNSIGNED_INT,
                clearInvocationCount);

        gl4.glViewportIndexedf(0, 0, 0, windowSize.x * FRAMEBUFFER_SCALE, windowSize.y * FRAMEBUFFER_SCALE);

        gl4.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName.get(0));
        gl4.glBindProgramPipeline(pipelineName.get(Pipeline.TEXTURE));
        gl4.glBindVertexArray(vertexArrayName.get(Pipeline.TEXTURE));
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName.get(Buffer.TRANSFORM));
        gl4.glBindImageTexture(0, textureName.get(Texture.INVOCATION_COUNT), 0, false, 0, GL_WRITE_ONLY, GL_R32UI);

        gl4.glDrawElementsInstancedBaseVertexBaseInstance(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 5, 0, 0);

        gl4.glDisable(GL_DEPTH_TEST);

        gl4.glViewportIndexedf(0, 0, 0, windowSize.x, windowSize.y);

        gl4.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        gl4.glBindProgramPipeline(pipelineName.get(Pipeline.SPLASH));
        gl4.glBindVertexArray(vertexArrayName.get(Pipeline.SPLASH));
        gl4.glBindImageTexture(0, textureName.get(Texture.INVOCATION_COUNT), 0, false, 0, GL_READ_ONLY, GL_R32UI);

        gl4.glDrawArraysInstancedBaseInstance(GL_TRIANGLES, 0, 3, 1, 0);

        return true;
    }
}
