/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_320.primitive;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2ES3.*;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.BufferUtils;
import glm.glm;
import glm.mat._4.Mat4;
import framework.Glm;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import glf.Vertex_v2fv2f;
import glm.vec._2.Vec2;
import glm.vec._2.i.Vec2i;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 *
 * @author GBarbieri
 */
public class Gl_320_primitive_line_msaa extends Test {

    public static void main(String[] args) {
        Gl_320_primitive_line_msaa gl_320_primitive_line_msaa = new Gl_320_primitive_line_msaa();
    }

    public Gl_320_primitive_line_msaa() {
        super("gl-320-primitive-line-msaa", Profile.CORE, 3, 2);
    }

    private final String SHADERS_SOURCE_TEXTURE = "primitive-line-msaa-render";
    private final String SHADERS_SOURCE_SPLASH = "primitive-line-msaa-splash";
    private final String SHADERS_ROOT = "src/data/gl_320/primitive";

    private int elementCount = 6;
    private int elementSize = elementCount * Short.BYTES;
    private short[] elementData = {
        0, 1, 2,
        2, 3, 0};

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int TRANSFORM = 1;
        public static final int MAX = 2;
    }

    private class Texture {

        public static final int COLORBUFFER = 0;
        public static final int RENDERBUFFER = 1;
        public static final int MAX = 2;
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

    private int framebufferScale = 3, vertexCount, uniformTransform;
    private int[] programName = new int[Program.MAX], uniformDiffuse = new int[Program.MAX];
    private IntBuffer vertexArrayName = GLBuffers.newDirectIntBuffer(Program.MAX),
            bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX),
            textureName = GLBuffers.newDirectIntBuffer(Texture.MAX),
            framebufferName = GLBuffers.newDirectIntBuffer(Texture.MAX);
    private FloatBuffer clearColor = GLBuffers.newDirectFloatBuffer(new float[]{1.0f, 1.0f, 1.0f, 1.0f});

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
            validated = initVertexArray(gl3);
        }
        if (validated) {
            validated = initTexture(gl3);
        }
        if (validated) {
            validated = initFramebuffer(gl3);
        }

        return validated;
    }

    private boolean initProgram(GL3 gl3) {

        boolean validated = true;

        ShaderCode[] shaderCode = new ShaderCode[Shader.MAX];

        if (validated) {

            shaderCode[Shader.VERT_TEXTURE] = ShaderCode.create(gl3, GL_VERTEX_SHADER, this.getClass(), SHADERS_ROOT,
                    null, SHADERS_SOURCE_TEXTURE, "vert", null, true);
            shaderCode[Shader.FRAG_TEXTURE] = ShaderCode.create(gl3, GL_FRAGMENT_SHADER, this.getClass(), SHADERS_ROOT,
                    null, SHADERS_SOURCE_TEXTURE, "frag", null, true);

            ShaderProgram shaderProgram = new ShaderProgram();
            shaderProgram.add(shaderCode[Shader.VERT_TEXTURE]);
            shaderProgram.add(shaderCode[Shader.FRAG_TEXTURE]);

            shaderProgram.init(gl3);

            programName[Program.TEXTURE] = shaderProgram.program();

            gl3.glBindAttribLocation(programName[Program.TEXTURE], Semantic.Attr.POSITION, "position");
            gl3.glBindAttribLocation(programName[Program.TEXTURE], Semantic.Attr.TEXCOORD, "color");
            gl3.glBindFragDataLocation(programName[Program.TEXTURE], Semantic.Frag.COLOR, "color");

            shaderProgram.link(gl3, System.out);
        }
        if (validated) {

            shaderCode[Shader.VERT_SPLASH] = ShaderCode.create(gl3, GL_VERTEX_SHADER, this.getClass(), SHADERS_ROOT,
                    null, SHADERS_SOURCE_SPLASH, "vert", null, true);
            shaderCode[Shader.FRAG_SPLASH] = ShaderCode.create(gl3, GL_FRAGMENT_SHADER, this.getClass(), SHADERS_ROOT,
                    null, SHADERS_SOURCE_SPLASH, "frag", null, true);

            ShaderProgram shaderProgram = new ShaderProgram();
            shaderProgram.add(shaderCode[Shader.VERT_SPLASH]);
            shaderProgram.add(shaderCode[Shader.FRAG_SPLASH]);

            shaderProgram.init(gl3);

            programName[Program.SPLASH] = shaderProgram.program();

            gl3.glBindFragDataLocation(programName[Program.SPLASH], Semantic.Frag.COLOR, "color");

            shaderProgram.link(gl3, System.out);
        }
        if (validated) {

            uniformTransform = gl3.glGetUniformBlockIndex(programName[Program.TEXTURE], "Transform");
            uniformDiffuse[Program.TEXTURE] = gl3.glGetUniformLocation(programName[Program.TEXTURE], "diffuse");
            uniformDiffuse[Program.SPLASH] = gl3.glGetUniformLocation(programName[Program.SPLASH], "diffuse");

            gl3.glUseProgram(programName[Program.TEXTURE]);
            gl3.glUniform1i(uniformDiffuse[Program.TEXTURE], 0);
            gl3.glUniformBlockBinding(programName[Program.TEXTURE], uniformTransform, Semantic.Uniform.TRANSFORM0);

            gl3.glUseProgram(programName[Program.SPLASH]);
            gl3.glUniform1i(uniformDiffuse[Program.SPLASH], 0);
        }

        return validated & checkError(gl3, "initProgram");
    }

    private boolean initBuffer(GL3 gl3) {

        gl3.glGenBuffers(Buffer.MAX, bufferName);

        float[] data = new float[2 * 36];
        for (int i = 0; i < 36; ++i) {
            float angle = (float) Math.PI * 2.0f * i / (data.length / 2);
            data[i * 2 + 0] = Glm.normalize(new float[]{(float) Math.sin(angle), (float) Math.cos(angle)})[0];
            data[i * 2 + 1] = Glm.normalize(new float[]{(float) Math.sin(angle), (float) Math.cos(angle)})[1];
        }
        vertexCount = 18;//static_cast<GLsizei>(Data.size() - 8);

        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(data);
        IntBuffer uniformBufferOffset = GLBuffers.newDirectIntBuffer(1);

        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
        gl3.glBufferData(GL_ARRAY_BUFFER, data.length * Float.BYTES, vertexBuffer, GL_STATIC_DRAW);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        gl3.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset);
        int uniformBlockSize = Math.max(Mat4.SIZE, uniformBufferOffset.get(0));

        gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.TRANSFORM));
        gl3.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_DYNAMIC_DRAW);
        gl3.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        BufferUtils.destroyDirectBuffer(vertexBuffer);
        BufferUtils.destroyDirectBuffer(uniformBufferOffset);

        return true;
    }

    private boolean initTexture(GL3 gl3) {

        boolean validated = true;

//        Vec2i windowSize_ = windowSize.shiftR(framebufferScale);
        gl3.glGenTextures(Texture.MAX, textureName);

        gl3.glActiveTexture(GL_TEXTURE0);
        gl3.glBindTexture(GL_TEXTURE_2D, textureName.get(Texture.COLORBUFFER));
        gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
        gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
        gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        gl3.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, windowSize.x >> framebufferScale,
                windowSize.y >> framebufferScale, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);

        gl3.glActiveTexture(GL_TEXTURE0);
        gl3.glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, textureName.get(Texture.RENDERBUFFER));
        //glTexParameteri(GL_TEXTURE_2D_MULTISAMPLE, GL_TEXTURE_BASE_LEVEL, 0);
        //glTexParameteri(GL_TEXTURE_2D_MULTISAMPLE, GL_TEXTURE_MAX_LEVEL, 0);
        gl3.glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, 8, GL_RGBA8, windowSize.x >> framebufferScale,
                windowSize.y >> framebufferScale, true);

        return validated;
    }

    private boolean initVertexArray(GL3 gl3) {

        gl3.glGenVertexArrays(Program.MAX, vertexArrayName);
        gl3.glBindVertexArray(vertexArrayName.get(Program.TEXTURE));
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, Vertex_v2fv2f.SIZE, 0);
            gl3.glVertexAttribPointer(Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, Vertex_v2fv2f.SIZE, Vec2.SIZE);
            gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl3.glEnableVertexAttribArray(Semantic.Attr.TEXCOORD);
        }
        gl3.glBindVertexArray(0);

        gl3.glBindVertexArray(vertexArrayName.get(Program.SPLASH));
        gl3.glBindVertexArray(0);

        return true;
    }

    private boolean initFramebuffer(GL3 gl3) {

        gl3.glGenFramebuffers(Texture.MAX, framebufferName);

        gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName.get(Texture.RENDERBUFFER));
        gl3.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, textureName.get(Texture.RENDERBUFFER), 0);

        gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName.get(Texture.COLORBUFFER));
        gl3.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, textureName.get(Texture.COLORBUFFER), 0);

        if (!isFramebufferComplete(gl3, framebufferName.get(Texture.RENDERBUFFER))) {
            return false;
        }
        if (!isFramebufferComplete(gl3, framebufferName.get(Texture.COLORBUFFER))) {
            return false;
        }

        gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL3 gl3 = (GL3) gl;

        {
            gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.TRANSFORM));
            ByteBuffer pointer = gl3.glMapBufferRange(
                    GL_UNIFORM_BUFFER, 0, Mat4.SIZE,
                    GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, (float) windowSize.x / windowSize.y, 0.1f, 100.0f);
            Mat4 model = new Mat4(1.0f);

            pointer.asFloatBuffer().put(projection.mul(viewMat4()).mul(model).toFa_());

            // Make sure the uniform buffer is uploaded
            gl3.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        gl3.glViewport(0, 0, windowSize.x >> framebufferScale, windowSize.y >> framebufferScale);
        gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName.get(Texture.RENDERBUFFER));
        gl3.glClearBufferfv(GL_COLOR, 0, clearColor);

        gl3.glUseProgram(programName[Program.TEXTURE]);

        gl3.glBindVertexArray(vertexArrayName.get(Program.TEXTURE));
        gl3.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName.get(Buffer.TRANSFORM));

        gl3.glDrawArraysInstanced(GL_LINE_LOOP, 0, vertexCount, 1);

        gl3.glBindFramebuffer(GL_READ_FRAMEBUFFER, framebufferName.get(Texture.RENDERBUFFER));
        gl3.glBindFramebuffer(GL_DRAW_FRAMEBUFFER, framebufferName.get(Texture.COLORBUFFER));
        gl3.glBlitFramebuffer(0, 0, windowSize.x >> framebufferScale, windowSize.y >> framebufferScale, 0, 0,
                windowSize.x >> framebufferScale, windowSize.y >> framebufferScale, GL_COLOR_BUFFER_BIT, GL_NEAREST);

        gl3.glViewport(0, 0, windowSize.x, windowSize.y);
        gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);

        gl3.glUseProgram(programName[Program.SPLASH]);

        gl3.glActiveTexture(GL_TEXTURE0);
        gl3.glBindVertexArray(vertexArrayName.get(Program.SPLASH));
        gl3.glBindTexture(GL_TEXTURE_2D, textureName.get(Texture.COLORBUFFER));

        gl3.glDrawArraysInstanced(GL_TRIANGLES, 0, 3, 1);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;

        gl3.glDeleteFramebuffers(Texture.MAX, framebufferName);
        gl3.glDeleteProgram(programName[Program.SPLASH]);
        gl3.glDeleteProgram(programName[Program.TEXTURE]);

        gl3.glDeleteBuffers(Buffer.MAX, bufferName);
        gl3.glDeleteTextures(Texture.MAX, textureName);
        gl3.glDeleteVertexArrays(Program.MAX, vertexArrayName);

        BufferUtils.destroyDirectBuffer(framebufferName);
        BufferUtils.destroyDirectBuffer(bufferName);
        BufferUtils.destroyDirectBuffer(textureName);
        BufferUtils.destroyDirectBuffer(vertexArrayName);

        BufferUtils.destroyDirectBuffer(clearColor);

        return true;
    }
}
