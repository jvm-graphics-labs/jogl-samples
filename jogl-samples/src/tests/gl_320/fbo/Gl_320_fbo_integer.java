/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_320.fbo;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2ES3.*;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import glm.glm;
import glm.mat._4.Mat4;
import glm.vec._3.Vec3;
import framework.BufferUtils;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import jgli.Texture2d;

/**
 *
 * @author GBarbieri
 */
public class Gl_320_fbo_integer extends Test {

    public static void main(String[] args) {
        Gl_320_fbo_integer gl_320_fbo_integer = new Gl_320_fbo_integer();
    }

    public Gl_320_fbo_integer() {
        super("Gl-320-fbo-integer", Profile.CORE, 3, 2);
    }

    private final String SHADERS_SOURCE1 = "fbo-integer-render";
    private final String SHADERS_SOURCE2 = "fbo-integer-splash";
    private final String SHADERS_ROOT = "src/data/gl_320/fbo";
    private final String TEXTURE_DIFFUSE = "kueken7_rgb8_unorm.dds";

    private int framebufferSize = 4;

    // With DDS textures, v texture coordinate are reversed, from top to bottom
    private int vertexCount = 6;
    private int vertexSize = vertexCount * 2 * 2 * Float.BYTES;
    private float[] vertexData = {
        -1.0f, -1.0f, 0.0f, 1.0f,
        +1.0f, -1.0f, 1.0f, 1.0f,
        +1.0f, +1.0f, 1.0f, 0.0f,
        +1.0f, +1.0f, 1.0f, 0.0f,
        -1.0f, +1.0f, 0.0f, 0.0f,
        -1.0f, -1.0f, 0.0f, 1.0f};

    private class Texture {

        public static final int DIFFUSE = 0;
        public static final int RENDERBUFFER = 1;
        public static final int MAX = 2;
    }

    private class Program {

        public static final int RENDER = 0;
        public static final int SPLASH = 1;
        public static final int MAX = 2;
    }

    private class Shader {

        public static final int VERT1 = 0;
        public static final int FRAG1 = 1;
        public static final int VERT2 = 2;
        public static final int FRAG2 = 3;
        public static final int MAX = 4;
    }

    private int[] vertexArrayName = {0}, bufferName = {0}, textureName = new int[Texture.MAX], framebufferName = {0},
            programName = new int[Program.MAX], uniformMvp = new int[Program.MAX], uniformDiffuse = new int[Program.MAX];

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

        return validated && checkError(gl3, "begin");
    }

    private boolean initProgram(GL3 gl3) {

        boolean validated = true;

        ShaderCode[] shaderCodes = new ShaderCode[Shader.MAX];

        // Create program
        if (validated) {

            shaderCodes[Shader.VERT1] = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE1, "vert", null, true);
            shaderCodes[Shader.FRAG1] = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE1, "frag", null, true);

            ShaderProgram program = new ShaderProgram();
            program.add(shaderCodes[Shader.VERT1]);
            program.add(shaderCodes[Shader.FRAG1]);
            program.init(gl3);

            programName[Program.RENDER] = program.program();

            gl3.glBindAttribLocation(programName[Program.RENDER], Semantic.Attr.POSITION, "position");
            gl3.glBindAttribLocation(programName[Program.RENDER], Semantic.Attr.TEXCOORD, "texCoord");
            gl3.glBindFragDataLocation(programName[Program.RENDER], Semantic.Frag.COLOR, "color");

            program.link(gl3, System.out);
        }
        if (validated) {

            shaderCodes[Shader.VERT2] = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE2, "vert", null, true);
            shaderCodes[Shader.FRAG2] = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE2, "frag", null, true);

            ShaderProgram program = new ShaderProgram();
            program.add(shaderCodes[Shader.VERT2]);
            program.add(shaderCodes[Shader.FRAG2]);
            program.init(gl3);

            programName[Program.SPLASH] = program.program();

            gl3.glBindAttribLocation(programName[Program.SPLASH], Semantic.Attr.POSITION, "position");
            gl3.glBindAttribLocation(programName[Program.SPLASH], Semantic.Attr.TEXCOORD, "texCoord");
            gl3.glBindFragDataLocation(programName[Program.SPLASH], Semantic.Frag.COLOR, "color");

            program.link(gl3, System.out);
        }
        if (validated) {

            uniformMvp[Program.RENDER]
                    = gl3.glGetUniformLocation(programName[Program.RENDER], "mvp");
            uniformDiffuse[Program.RENDER]
                    = gl3.glGetUniformLocation(programName[Program.RENDER], "diffuse");
            uniformMvp[Program.SPLASH]
                    = gl3.glGetUniformLocation(programName[Program.SPLASH], "mvp");
            uniformDiffuse[Program.SPLASH]
                    = gl3.glGetUniformLocation(programName[Program.SPLASH], "diffuse");
        }
        return validated & checkError(gl3, "initProgram");
    }

    private boolean initBuffer(GL3 gl3) {

        gl3.glGenBuffers(1, bufferName, 0);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[0]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl3.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(vertexBuffer);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        return checkError(gl3, "initBuffer");
    }

    private boolean initTexture(GL3 gl3) {

        try {

            jgli.Texture2d texture = new Texture2d(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE));
            gl3.glGenTextures(Texture.MAX, textureName, 0);
            gl3.glActiveTexture(GL_TEXTURE0);
            gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.DIFFUSE]);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

            gl3.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

            jgli.Gl.Format format = jgli.Gl.translate(jgli.Format.FORMAT_RGB8_UINT_PACK8);

            for (int level = 0; level < texture.levels(); level++) {

                gl3.glTexImage2D(GL_TEXTURE_2D, level,
                        format.internal.value,
                        texture.dimensions(level)[0], texture.dimensions(level)[1],
                        0,
                        format.external.value, format.type.value,
                        texture.data(level));
            }
            gl3.glPixelStorei(GL_UNPACK_ALIGNMENT, 4);
            gl3.glBindTexture(GL_TEXTURE_2D, 0);

            gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.RENDERBUFFER]);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            gl3.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8UI, windowSize.x / framebufferSize,
                    windowSize.y / framebufferSize, 0, GL_RGBA_INTEGER, GL_UNSIGNED_BYTE, null);
            gl3.glBindTexture(GL_TEXTURE_2D, 0);

        } catch (IOException ex) {
            Logger.getLogger(Gl_320_fbo_integer_blit.class.getName()).log(Level.SEVERE, null, ex);
        }
        return checkError(gl3, "initTexture");
    }

    private boolean initFramebuffer(GL3 gl3) {

        gl3.glGenFramebuffers(1, framebufferName, 0);

        gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[0]);
        gl3.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, textureName[Texture.RENDERBUFFER], 0);
        if (!isFramebufferComplete(gl3, framebufferName[0])) {
            return false;
        }
        gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);

        return checkError(gl3, "initFramebuffer");
    }

    private boolean initVertexArray(GL3 gl3) {

        gl3.glGenVertexArrays(1, vertexArrayName, 0);
        gl3.glBindVertexArray(vertexArrayName[0]);
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[0]);
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 2 * 2 * Float.BYTES, 0);
            gl3.glVertexAttribPointer(Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, 2 * 2 * Float.BYTES, 2 * Float.BYTES);
            gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl3.glEnableVertexAttribArray(Semantic.Attr.TEXCOORD);
        }
        gl3.glBindVertexArray(0);

        return checkError(gl3, "initVertexArray");
    }

    @Override
    protected boolean render(GL gl) {

        GL3 gl3 = (GL3) gl;

        // Pass 1
        {
            gl3.glViewport(0, 0,
                    windowSize.x / framebufferSize,
                    windowSize.y / framebufferSize);
            gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[0]);
            gl3.glClearBufferuiv(GL_COLOR, 0, new int[]{0, 128, 255, 255}, 0);

            Mat4 projection = glm.ortho_(-2.0f, 2.0f, 2.0f, -2.0f, 2.0f, -2.0f);
            Mat4 view = new Mat4(1.0f);
            Mat4 model = new Mat4(1.0f).rotate(-0.3f, new Vec3(0.f, 0.f, 1.f));
            Mat4 mvp = projection.mul(view).mul(model);

            gl3.glUseProgram(programName[Program.RENDER]);
            gl3.glUniform1i(uniformDiffuse[Program.RENDER], 0);
            gl3.glUniformMatrix4fv(uniformMvp[Program.RENDER], 1, false, mvp.toFa_(), 0);

            gl3.glActiveTexture(GL_TEXTURE0);
            gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.DIFFUSE]);
            gl3.glBindVertexArray(vertexArrayName[0]);

            gl3.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 1);
        }

        // Pass 2
        {
            Mat4 perspective = glm.perspective_((float) Math.PI * 0.25f, (float) windowSize.x / windowSize.y, 0.1f, 100.0f);
            Mat4 model = new Mat4(1.0f);
            Mat4 mvp = perspective.mul(viewMat4()).mul(model);

            gl3.glUseProgram(programName[Program.SPLASH]);
            gl3.glUniform1i(uniformDiffuse[Program.SPLASH], 0);
            gl3.glUniformMatrix4fv(uniformMvp[Program.SPLASH], 1, false, mvp.toFa_(), 0);

            gl3.glViewport(0, 0, windowSize.x, windowSize.y);
            gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);
            gl3.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 0.5f, 0.0f, 1.0f}, 0);

            gl3.glActiveTexture(GL_TEXTURE0);
            gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.RENDERBUFFER]);
            gl3.glBindVertexArray(vertexArrayName[0]);

            gl3.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 1);
        }

        return checkError(gl3, "render");
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;

        gl3.glDeleteBuffers(1, bufferName, 0);
        gl3.glDeleteProgram(programName[Program.RENDER]);
        gl3.glDeleteProgram(programName[Program.SPLASH]);
        gl3.glDeleteTextures(Texture.MAX, textureName, 0);
        gl3.glDeleteFramebuffers(1, framebufferName, 0);
        gl3.glDeleteVertexArrays(1, vertexArrayName, 0);

        return checkError(gl3, "end");
    }
}
