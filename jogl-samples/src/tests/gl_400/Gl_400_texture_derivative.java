/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_400;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL3ES3.*;
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
import glf.Vertex_v2fv2f;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import jgli.Texture2d;
import glm.vec._2.Vec2;
import java.nio.IntBuffer;
import jglm.Vec2i;

/**
 *
 * @author GBarbieri
 */
public class Gl_400_texture_derivative extends Test {

    public static void main(String[] args) {
        Gl_400_texture_derivative gl_400_texture_derivative = new Gl_400_texture_derivative();
    }

    public Gl_400_texture_derivative() {
        super("gl-400-texture-derivative", Profile.CORE, 4, 0, new Vec2(Math.PI * 0.05f, -Math.PI * 0.45f));
    }

    private final String SHADERS_SOURCE_TEXTURE = "texture-derivative2";
    private final String SHADERS_SOURCE_SPLASH = "texture-derivative1";
    private final String SHADERS_ROOT = "src/data/gl_400";

    private final int FRAMEBUFFER_SIZE = 8;

    private int vertexCount = 4;
    private int vertexSize = vertexCount * Vertex_v2fv2f.SIZE;
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

        public static final int DIFFUSE = 0;
        public static final int COLORBUFFER = 1;
        public static final int RENDERBUFFER = 2;
        public static final int MAX = 3;
    }

    private class Program {

        public static final int TEXTURE = 0;
        public static final int SPLASH = 1;
        public static final int MAX = 2;
    }

    private class Shader {

        public static final int VERT_TEXTURE = 0;
        public static final int FRAG_TEXTURE = 1;
        public static final int VERT_SPLASH = 2;
        public static final int FRAG_SPLASH = 3;
        public static final int MAX = 4;
    }

    private int[] programName = new int[Program.MAX], uniformDiffuse = new int[Program.MAX];
    private IntBuffer framebufferName = GLBuffers.newDirectIntBuffer(1),
            vertexArrayName = GLBuffers.newDirectIntBuffer(Program.MAX),
            bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX),
            textureName = GLBuffers.newDirectIntBuffer(Texture.MAX);
    private int uniformTransform, uniformUseGrad, uniformFramebufferSize;

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;

        FloatBuffer data = GLBuffers.newDirectFloatBuffer(1);
        gl4.glGetFloatv(GL_MIN_FRAGMENT_INTERPOLATION_OFFSET, data);
        System.out.println("GL_MIN_FRAGMENT_INTERPOLATION_OFFSET: " + data.get(0));
        gl4.glGetFloatv(GL_MAX_FRAGMENT_INTERPOLATION_OFFSET, data);
        System.out.println("GL_MAX_FRAGMENT_INTERPOLATION_OFFSET: " + data.get(0));
        gl4.glGetFloatv(GL_FRAGMENT_INTERPOLATION_OFFSET_BITS, data);
        System.out.println("GL_FRAGMENT_INTERPOLATION_OFFSET_BITS: " + data.get(0));
        BufferUtils.destroyDirectBuffer(data);

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

        // Create program
        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            shaderCodes[Shader.VERT_TEXTURE] = ShaderCode.create(gl4, GL_VERTEX_SHADER, this.getClass(), SHADERS_ROOT,
                    null, SHADERS_SOURCE_TEXTURE, "vert", null, true);
            shaderCodes[Shader.FRAG_TEXTURE] = ShaderCode.create(gl4, GL_FRAGMENT_SHADER, this.getClass(), SHADERS_ROOT,
                    null, SHADERS_SOURCE_TEXTURE, "frag", null, true);

            shaderProgram.init(gl4);

            shaderProgram.add(shaderCodes[Shader.VERT_TEXTURE]);
            shaderProgram.add(shaderCodes[Shader.FRAG_TEXTURE]);

            programName[Program.TEXTURE] = shaderProgram.program();

            shaderProgram.link(gl4, System.out);

        }

        // Get variables locations
        if (validated) {

            uniformTransform = gl4.glGetUniformBlockIndex(programName[Program.TEXTURE], "transform");
            uniformDiffuse[Program.TEXTURE] = gl4.glGetUniformLocation(programName[Program.TEXTURE], "Diffuse");
            uniformUseGrad = gl4.glGetUniformLocation(programName[Program.TEXTURE], "UseGrad");
        }
        // Create program
        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            shaderCodes[Shader.VERT_SPLASH] = ShaderCode.create(gl4, GL_VERTEX_SHADER, this.getClass(), SHADERS_ROOT,
                    null, SHADERS_SOURCE_SPLASH, "vert", null, true);
            shaderCodes[Shader.FRAG_SPLASH] = ShaderCode.create(gl4, GL_FRAGMENT_SHADER, this.getClass(), SHADERS_ROOT,
                    null, SHADERS_SOURCE_SPLASH, "frag", null, true);

            shaderProgram.init(gl4);

            shaderProgram.add(shaderCodes[Shader.VERT_SPLASH]);
            shaderProgram.add(shaderCodes[Shader.FRAG_SPLASH]);

            programName[Program.SPLASH] = shaderProgram.program();

            shaderProgram.link(gl4, System.out);

        }

        // Get variables locations
        if (validated) {

            uniformDiffuse[Program.SPLASH] = gl4.glGetUniformLocation(programName[Program.SPLASH], "Diffuse");
            uniformFramebufferSize = gl4.glGetUniformLocation(programName[Program.SPLASH], "FramebufferSize");
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        IntBuffer uniformBufferOffset = GLBuffers.newDirectIntBuffer(1);

        gl4.glGenBuffers(Buffer.MAX, bufferName);

        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT));
        gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
        gl4.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        gl4.glGetIntegerv(
                GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT,
                uniformBufferOffset);

        int uniformBlockSize = Math.max(Mat4.SIZE, uniformBufferOffset.get(0));

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.TRANSFORM));
        gl4.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_DYNAMIC_DRAW);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        BufferUtils.destroyDirectBuffer(elementBuffer);
        BufferUtils.destroyDirectBuffer(vertexBuffer);
        BufferUtils.destroyDirectBuffer(uniformBufferOffset);

        return true;
    }

    private boolean initTexture(GL4 gl4) {

        boolean validated = true;

        int[] textureSize = {128, 128};
        jgli.Texture2d texture = new Texture2d(jgli.Format.FORMAT_RGBA8_UNORM_PACK32, textureSize);
        texture.clear(0, 0, 0, new byte[]{(byte) 255, (byte) 0, (byte) 0, (byte) 255});
        texture.clear(0, 0, 1, new byte[]{(byte) 255, (byte) 128, (byte) 0, (byte) 255});
        texture.clear(0, 0, 2, new byte[]{(byte) 0, (byte) 255, (byte) 0, (byte) 255});
        texture.clear(0, 0, 3, new byte[]{(byte) 0, (byte) 255, (byte) 0, (byte) 255});
        texture.clear(0, 0, 4, new byte[]{(byte) 0, (byte) 255, (byte) 255, (byte) 255});
        texture.clear(0, 0, 5, new byte[]{(byte) 0, (byte) 0, (byte) 255, (byte) 255});
        texture.clear(0, 0, 6, new byte[]{(byte) 255, (byte) 0, (byte) 255, (byte) 255});
        texture.clear(0, 0, 7, new byte[]{(byte) 255, (byte) 255, (byte) 255, (byte) 255});

        assert (!texture.empty());

        jgli.Gl.Format format = jgli.Gl.translate(texture.format());

        gl4.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        gl4.glGenTextures(Texture.MAX, textureName);

        gl4.glActiveTexture(GL_TEXTURE0);
        gl4.glBindTexture(GL_TEXTURE_2D, textureName.get(Texture.DIFFUSE));
        gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
        gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, texture.levels() - 2);
        gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_R, GL_BLUE);
        gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_G, GL_GREEN);
        gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_B, GL_RED);
        gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_A, GL_ALPHA);

        for (int level = 0; level < texture.levels(); ++level) {
            gl4.glTexImage2D(GL_TEXTURE_2D, level,
                    format.internal.value,
                    texture.dimensions(level)[0], texture.dimensions(level)[1],
                    0,
                    format.external.value, format.type.value,
                    texture.data(level));
        }

        gl4.glActiveTexture(GL_TEXTURE0);
        gl4.glBindTexture(GL_TEXTURE_2D, textureName.get(Texture.COLORBUFFER));
        gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
        gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
        gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        gl4.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, windowSize.x / FRAMEBUFFER_SIZE, windowSize.y / FRAMEBUFFER_SIZE, 0,
                GL_RGBA, GL_UNSIGNED_BYTE, null);

        gl4.glActiveTexture(GL_TEXTURE0);
        gl4.glBindTexture(GL_TEXTURE_2D, textureName.get(Texture.RENDERBUFFER));
        gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
        gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
        gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        gl4.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT24, windowSize.x / FRAMEBUFFER_SIZE,
                windowSize.y / FRAMEBUFFER_SIZE, 0, GL_DEPTH_COMPONENT, GL_FLOAT, null);

        gl4.glPixelStorei(GL_UNPACK_ALIGNMENT, 4);

        return validated;
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(Program.MAX, vertexArrayName);
        gl4.glBindVertexArray(vertexArrayName.get(Program.TEXTURE));
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, Vertex_v2fv2f.SIZE, 0);
            gl4.glVertexAttribPointer(Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, Vertex_v2fv2f.SIZE, Vec2.SIZE);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl4.glEnableVertexAttribArray(Semantic.Attr.TEXCOORD);

            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT));
        }
        gl4.glBindVertexArray(0);

        gl4.glBindVertexArray(vertexArrayName.get(Program.SPLASH));
        gl4.glBindVertexArray(0);

        return true;
    }

    private boolean initFramebuffer(GL4 gl4) {

        gl4.glGenFramebuffers(1, framebufferName);
        gl4.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName.get());
        gl4.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, textureName.get(Texture.COLORBUFFER), 0);
        gl4.glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, textureName.get(Texture.RENDERBUFFER), 0);

        if (!isFramebufferComplete(gl4, framebufferName.get(0))) {
            return false;
        }

        gl4.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        Vec2i framebufferSize = new Vec2i(windowSize.x / FRAMEBUFFER_SIZE, windowSize.y / FRAMEBUFFER_SIZE);

        // Update of the uniform buffer
        {
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.TRANSFORM));
            ByteBuffer pointer = gl4.glMapBufferRange(
                    GL_UNIFORM_BUFFER, 0, Mat4.SIZE,
                    GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, 4.0f / 3.0f, 0.1f, 100.0f);
            Mat4 model = new Mat4(1.0f);

            pointer.asFloatBuffer().put(projection.mul(viewMat4()).mul(model).toFa_());

            // Make sure the uniform buffer is uploaded
            gl4.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        gl4.glEnable(GL_DEPTH_TEST);
        gl4.glDepthFunc(GL_LESS);

        gl4.glViewport(0, 0, framebufferSize.x, framebufferSize.y);
        gl4.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName.get(0));
        
        gl4.glClearBufferfv(GL_DEPTH, 0, clearDepth.put(0,1));
        gl4.glClearBufferfv(GL_COLOR, 0, clearColor.put(0, 0).put(1, 0).put(2, 0).put(3, 1));

        gl4.glUseProgram(programName[Program.TEXTURE]);
        gl4.glUniform1i(uniformDiffuse[Program.TEXTURE], 0);
        gl4.glUniformBlockBinding(programName[Program.TEXTURE], uniformTransform,
                Semantic.Uniform.TRANSFORM0);

        gl4.glActiveTexture(GL_TEXTURE0);
        gl4.glBindTexture(GL_TEXTURE_2D, textureName.get(Texture.DIFFUSE));
        gl4.glBindVertexArray(vertexArrayName.get(Program.TEXTURE));
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName.get(Buffer.TRANSFORM));

        gl4.glEnable(GL_SCISSOR_TEST);
        {
            gl4.glScissor(0, 0, framebufferSize.x / 2, framebufferSize.y);
            gl4.glUniform1i(uniformUseGrad, 1);
            gl4.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 2, 0);

            gl4.glScissor(framebufferSize.x / 2, 0, framebufferSize.x / 2, framebufferSize.y);
            gl4.glUniform1i(uniformUseGrad, 0);
            gl4.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 2, 0);
        }
        gl4.glDisable(GL_SCISSOR_TEST);

        gl4.glDisable(GL_DEPTH_TEST);
        gl4.glViewport(0, 0, windowSize.x, windowSize.y);
        gl4.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        gl4.glUseProgram(programName[Program.SPLASH]);
        gl4.glUniform1i(uniformDiffuse[Program.SPLASH], 0);
        gl4.glUniform1f(uniformFramebufferSize, FRAMEBUFFER_SIZE);

        gl4.glActiveTexture(GL_TEXTURE0);
        gl4.glBindVertexArray(vertexArrayName.get(Program.SPLASH));
        gl4.glBindTexture(GL_TEXTURE_2D, textureName.get(Texture.COLORBUFFER));

        gl4.glDrawArraysInstanced(GL_TRIANGLES, 0, 3, 1);

        return true;
    }
}
