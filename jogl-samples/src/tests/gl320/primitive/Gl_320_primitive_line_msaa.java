/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl320.primitive;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_COLOR_ATTACHMENT0;
import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DRAW_FRAMEBUFFER;
import static com.jogamp.opengl.GL.GL_DYNAMIC_DRAW;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_FRAMEBUFFER;
import static com.jogamp.opengl.GL.GL_LINE_LOOP;
import static com.jogamp.opengl.GL.GL_MAP_INVALIDATE_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_MAP_WRITE_BIT;
import static com.jogamp.opengl.GL.GL_NEAREST;
import static com.jogamp.opengl.GL.GL_READ_FRAMEBUFFER;
import static com.jogamp.opengl.GL.GL_RGBA;
import static com.jogamp.opengl.GL.GL_RGBA8;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNSIGNED_BYTE;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_TEXTURE_2D_MULTISAMPLE;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_BASE_LEVEL;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_MAX_LEVEL;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.Glm;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

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

    private enum Buffer {
        VERTEX,
        TRANSFORM,
        MAX
    }

    private enum Texture {
        COLORBUFFER,
        RENDERBUFFER,
        MAX
    }

    private enum Program {
        TEXTURE,
        SPLASH,
        MAX
    }

    private enum Shader {
        VERT_TEXTURE,
        FRAG_TEXTURE,
        VERT_SPLASH,
        FRAG_SPLASH,
        MAX
    }

    private int framebufferScale = 3, vertexCount, uniformTransform;
    private int[] programName = new int[Program.MAX.ordinal()], vertexArrayName = new int[Program.MAX.ordinal()],
            bufferName = new int[Buffer.MAX.ordinal()], textureName = new int[Texture.MAX.ordinal()],
            uniformDiffuse = new int[Program.MAX.ordinal()], framebufferName = new int[Texture.MAX.ordinal()];
    private float[] projection = new float[16], model = new float[16];

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

        ShaderCode[] shaderCode = new ShaderCode[Shader.MAX.ordinal()];

        if (validated) {

            shaderCode[Shader.VERT_TEXTURE.ordinal()] = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_TEXTURE, "vert", null, true);
            shaderCode[Shader.FRAG_TEXTURE.ordinal()] = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_TEXTURE, "frag", null, true);

            ShaderProgram shaderProgram = new ShaderProgram();
            shaderProgram.add(shaderCode[Shader.VERT_TEXTURE.ordinal()]);
            shaderProgram.add(shaderCode[Shader.FRAG_TEXTURE.ordinal()]);

            shaderProgram.init(gl3);

            programName[Program.TEXTURE.ordinal()] = shaderProgram.program();

            gl3.glBindAttribLocation(programName[Program.TEXTURE.ordinal()], Semantic.Attr.POSITION, "position");
            gl3.glBindAttribLocation(programName[Program.TEXTURE.ordinal()], Semantic.Attr.TEXCOORD, "color");
            gl3.glBindFragDataLocation(programName[Program.TEXTURE.ordinal()], Semantic.Frag.COLOR, "color");

            shaderProgram.link(gl3, System.out);
        }
        if (validated) {

            shaderCode[Shader.VERT_SPLASH.ordinal()] = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_SPLASH, "vert", null, true);
            shaderCode[Shader.FRAG_SPLASH.ordinal()] = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_SPLASH, "frag", null, true);

            ShaderProgram shaderProgram = new ShaderProgram();
            shaderProgram.add(shaderCode[Shader.VERT_SPLASH.ordinal()]);
            shaderProgram.add(shaderCode[Shader.FRAG_SPLASH.ordinal()]);

            shaderProgram.init(gl3);

            programName[Program.SPLASH.ordinal()] = shaderProgram.program();

            gl3.glBindFragDataLocation(programName[Program.SPLASH.ordinal()], Semantic.Frag.COLOR, "color");

            shaderProgram.link(gl3, System.out);
        }
        if (validated) {

            uniformTransform = gl3.glGetUniformBlockIndex(programName[Program.TEXTURE.ordinal()], "Transform");
            uniformDiffuse[Program.TEXTURE.ordinal()]
                    = gl3.glGetUniformLocation(programName[Program.TEXTURE.ordinal()], "diffuse");
            uniformDiffuse[Program.SPLASH.ordinal()]
                    = gl3.glGetUniformLocation(programName[Program.SPLASH.ordinal()], "diffuse");

            gl3.glUseProgram(programName[Program.TEXTURE.ordinal()]);
            gl3.glUniform1i(uniformDiffuse[Program.TEXTURE.ordinal()], 0);
            gl3.glUniformBlockBinding(programName[Program.TEXTURE.ordinal()], uniformTransform,
                    Semantic.Uniform.TRANSFORM0);

            gl3.glUseProgram(programName[Program.SPLASH.ordinal()]);
            gl3.glUniform1i(uniformDiffuse[Program.SPLASH.ordinal()], 0);
        }

        return validated & checkError(gl3, "initProgram");
    }

    private boolean initBuffer(GL3 gl3) {

        gl3.glGenBuffers(Buffer.MAX.ordinal(), bufferName, 0);

        float[] data = new float[2 * 36];
        for (int i = 0; i < 36; ++i) {
            float angle = (float) Math.PI * 2.0f * (float) i / (data.length / 2);
            data[i * 2 + 0] = Glm.normalize(new float[]{(float) Math.sin(angle), (float) Math.cos(angle)})[0];
            data[i * 2 + 1] = Glm.normalize(new float[]{(float) Math.sin(angle), (float) Math.cos(angle)})[1];
        }
        vertexCount = 18;//static_cast<GLsizei>(Data.size() - 8);

        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(data);
        gl3.glBufferData(GL_ARRAY_BUFFER, data.length * Float.BYTES, vertexBuffer, GL_STATIC_DRAW);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        int[] uniformBufferOffset = {0};
        gl3.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset, 0);
        int uniformBlockSize = Math.max(16 * Float.BYTES, uniformBufferOffset[0]);

        gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM.ordinal()]);
        gl3.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_DYNAMIC_DRAW);
        gl3.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return true;
    }

    private boolean initTexture(GL3 gl3) {

        boolean validated = true;

        gl3.glGenTextures(Texture.MAX.ordinal(), textureName, 0);

        gl3.glActiveTexture(GL_TEXTURE0);
        gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.COLORBUFFER.ordinal()]);
        gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
        gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
        gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        gl3.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, windowSize.x >> framebufferScale,
                windowSize.y >> framebufferScale, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);

        gl3.glActiveTexture(GL_TEXTURE0);
        gl3.glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, textureName[Texture.RENDERBUFFER.ordinal()]);
        //glTexParameteri(GL_TEXTURE_2D_MULTISAMPLE, GL_TEXTURE_BASE_LEVEL, 0);
        //glTexParameteri(GL_TEXTURE_2D_MULTISAMPLE, GL_TEXTURE_MAX_LEVEL, 0);
        gl3.glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, 8, GL_RGBA8, windowSize.x >> framebufferScale,
                windowSize.y >> framebufferScale, true);

        return validated;
    }

    private boolean initVertexArray(GL3 gl3) {

        gl3.glGenVertexArrays(Program.MAX.ordinal(), vertexArrayName, 0);
        gl3.glBindVertexArray(vertexArrayName[Program.TEXTURE.ordinal()]);
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 2 * 2 * Float.BYTES, 0);
            gl3.glVertexAttribPointer(Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, 2 * 2 * Float.BYTES, 2 * Float.BYTES);
            gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl3.glEnableVertexAttribArray(Semantic.Attr.TEXCOORD);
        }
        gl3.glBindVertexArray(0);

        gl3.glBindVertexArray(vertexArrayName[Program.SPLASH.ordinal()]);
        gl3.glBindVertexArray(0);

        return true;
    }

    private boolean initFramebuffer(GL3 gl3) {

        gl3.glGenFramebuffers(Texture.MAX.ordinal(), framebufferName, 0);

        gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[Texture.RENDERBUFFER.ordinal()]);
        gl3.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, textureName[Texture.RENDERBUFFER.ordinal()], 0);

        gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[Texture.COLORBUFFER.ordinal()]);
        gl3.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, textureName[Texture.COLORBUFFER.ordinal()], 0);

        if (!isFramebufferComplete(gl3, framebufferName[Texture.RENDERBUFFER.ordinal()])) {
            return false;
        }
        if (!isFramebufferComplete(gl3, framebufferName[Texture.COLORBUFFER.ordinal()])) {
            return false;
        }

        gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL3 gl3 = (GL3) gl;

        {
            gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM.ordinal()]);
            ByteBuffer pointer = gl3.glMapBufferRange(
                    GL_UNIFORM_BUFFER, 0, 16 * Float.BYTES,
                    GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            FloatUtil.makePerspective(projection, 0, true, (float) Math.PI * 0.25f,
                    (float) windowSize.x / windowSize.y, 0.1f, 100.0f);
            FloatUtil.makeIdentity(model);

            FloatUtil.multMatrix(projection, view());
            FloatUtil.multMatrix(projection, model);

            for (float f : projection) {
                pointer.putFloat(f);
            }

            // Make sure the uniform buffer is uploaded
            gl3.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        gl3.glViewport(0, 0, windowSize.x >> framebufferScale, windowSize.y >> framebufferScale);
        gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[Texture.RENDERBUFFER.ordinal()]);
        gl3.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, 0);

        gl3.glUseProgram(programName[Program.TEXTURE.ordinal()]);

        gl3.glBindVertexArray(vertexArrayName[Program.TEXTURE.ordinal()]);
        gl3.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.TRANSFORM.ordinal()]);

        gl3.glDrawArraysInstanced(GL_LINE_LOOP, 0, vertexCount, 1);

        gl3.glBindFramebuffer(GL_READ_FRAMEBUFFER, framebufferName[Texture.RENDERBUFFER.ordinal()]);
        gl3.glBindFramebuffer(GL_DRAW_FRAMEBUFFER, framebufferName[Texture.COLORBUFFER.ordinal()]);
        gl3.glBlitFramebuffer(0, 0, windowSize.x >> framebufferScale, windowSize.y >> framebufferScale, 0, 0,
                windowSize.x >> framebufferScale, windowSize.y >> framebufferScale, GL_COLOR_BUFFER_BIT, GL_NEAREST);

        gl3.glViewport(0, 0, windowSize.x, windowSize.y);
        gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);

        gl3.glUseProgram(programName[Program.SPLASH.ordinal()]);

        gl3.glActiveTexture(GL_TEXTURE0);
        gl3.glBindVertexArray(vertexArrayName[Program.SPLASH.ordinal()]);
        gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.COLORBUFFER.ordinal()]);

        gl3.glDrawArraysInstanced(GL_TRIANGLES, 0, 3, 1);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;

        gl3.glDeleteFramebuffers(Texture.MAX.ordinal(), framebufferName, 0);
        gl3.glDeleteProgram(programName[Program.SPLASH.ordinal()]);
        gl3.glDeleteProgram(programName[Program.TEXTURE.ordinal()]);

        gl3.glDeleteBuffers(Buffer.MAX.ordinal(), bufferName, 0);
        gl3.glDeleteTextures(Texture.MAX.ordinal(), textureName, 0);
        gl3.glDeleteVertexArrays(Program.MAX.ordinal(), vertexArrayName, 0);

        return true;
    }
}
