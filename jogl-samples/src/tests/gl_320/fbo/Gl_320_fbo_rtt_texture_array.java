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
import glf.Vertex_v2fv2f;
import glm.vec._2.Vec2;
import java.nio.FloatBuffer;
import jglm.Vec4i;

/**
 *
 * @author GBarbieri
 */
public class Gl_320_fbo_rtt_texture_array extends Test {

    public static void main(String[] args) {
        Gl_320_fbo_rtt_texture_array gl_320_fbo_rtt_texture_array = new Gl_320_fbo_rtt_texture_array();
    }

    public Gl_320_fbo_rtt_texture_array() {
        super("Gl-320-fbo-rtt-texture-array", Profile.CORE, 3, 2);
    }

    private final String SHADERS_SOURCE1 = "fbo-rtt-multiple-output";
    private final String SHADERS_SOURCE2 = "fbo-rtt-layer";
    private final String SHADERS_ROOT = "src/data/gl_320/fbo";

    private final int FRAMEBUFFER_SIZE = 2;

    // With DDS textures, v texture coordinate are reversed, from top to bottom
    private int vertexCount = 6;
    private int vertexSize = vertexCount * glf.Vertex_v2fv2f.SIZE;
    private float[] vertexData = {
        -1.0f, -1.0f,/**/ 0.0f, 0.0f,
        +1.0f, -1.0f,/**/ 1.0f, 0.0f,
        +1.0f, +1.0f,/**/ 1.0f, 1.0f,
        +1.0f, +1.0f,/**/ 1.0f, 1.0f,
        -1.0f, +1.0f,/**/ 0.0f, 1.0f,
        -1.0f, -1.0f,/**/ 0.0f, 0.0f};

    private class Texture {

        public static final int RED = 0;
        public static final int GREEN = 1;
        public static final int BLUE = 2;
        public static final int MAX = 3;
    }

    private class Program {

        public static final int MULTIPLE = 0;
        public static final int SPLASH = 1;
        public static final int MAX = 2;
    }

    private class Shader {

        public static final int VERT_MULTIPLE = 0;
        public static final int FRAG_MULTIPLE = 1;
        public static final int VERT_SPLASH = 2;
        public static final int FRAG_SPLASH = 3;
        public static final int MAX = 4;
    }

    private int[] shaderName = new int[Shader.MAX], framebufferName = {0}, bufferName = {0}, textureName = {0},
            vertexArrayName = new int[Program.MAX], programName = new int[Program.MAX], uniformMvp = new int[Program.MAX];
    private int uniformLayer, uniformDiffuse;
    private Vec4i[] viewport = new Vec4i[Texture.MAX];

    @Override
    protected boolean begin(GL gl) {

        GL3 gl3 = (GL3) gl;

        viewport[Texture.RED] = new Vec4i(windowSize.x >> 1, 0, windowSize.x / FRAMEBUFFER_SIZE,
                windowSize.y / FRAMEBUFFER_SIZE);
        viewport[Texture.GREEN] = new Vec4i(windowSize.x >> 1, windowSize.y >> 1, windowSize.x / FRAMEBUFFER_SIZE,
                windowSize.y / FRAMEBUFFER_SIZE);
        viewport[Texture.BLUE] = new Vec4i(0, windowSize.y >> 1, windowSize.x / FRAMEBUFFER_SIZE, 
                windowSize.y / FRAMEBUFFER_SIZE);

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

        if (validated) {

            ShaderCode vertexShaderCode = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE1, "vert", null, true);
            ShaderCode fragmentShaderCode = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE1, "frag", null, true);

            ShaderProgram program = new ShaderProgram();
            program.add(vertexShaderCode);
            program.add(fragmentShaderCode);

            program.init(gl3);

            programName[Program.MULTIPLE] = program.program();

            gl3.glBindAttribLocation(programName[Program.MULTIPLE], Semantic.Attr.POSITION, "position");
            gl3.glBindFragDataLocation(programName[Program.MULTIPLE], Semantic.Frag.RED, "red");
            gl3.glBindFragDataLocation(programName[Program.MULTIPLE], Semantic.Frag.GREEN, "greem");
            gl3.glBindFragDataLocation(programName[Program.MULTIPLE], Semantic.Frag.BLUE, "blue");

            program.link(gl3, System.out);
        }

        if (validated) {

            uniformMvp[Program.MULTIPLE]
                    = gl3.glGetUniformLocation(programName[Program.MULTIPLE], "mvp");
        }

        if (validated) {

            ShaderCode vertexShaderCode = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE2, "vert", null, true);
            ShaderCode fragmentShaderCode = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE2, "frag", null, true);

            ShaderProgram program = new ShaderProgram();
            program.add(vertexShaderCode);
            program.add(fragmentShaderCode);

            program.init(gl3);

            programName[Program.SPLASH] = program.program();

            gl3.glBindAttribLocation(programName[Program.SPLASH], Semantic.Attr.POSITION, "position");
            gl3.glBindAttribLocation(programName[Program.SPLASH], Semantic.Attr.TEXCOORD, "texCoord");
            gl3.glBindFragDataLocation(programName[Program.SPLASH], Semantic.Frag.COLOR, "color");

            program.link(gl3, System.out);
        }

        if (validated) {

            uniformMvp[Program.SPLASH]
                    = gl3.glGetUniformLocation(programName[Program.SPLASH], "mvp");
            uniformDiffuse = gl3.glGetUniformLocation(programName[Program.SPLASH], "diffuse");
            uniformLayer = gl3.glGetUniformLocation(programName[Program.SPLASH], "layer");
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

        gl3.glActiveTexture(GL_TEXTURE0);
        gl3.glGenTextures(1, textureName, 0);
        gl3.glBindTexture(GL_TEXTURE_2D_ARRAY, textureName[0]);
        gl3.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_BASE_LEVEL, 0);
        gl3.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAX_LEVEL, 0);
        gl3.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        gl3.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        gl3.glTexImage3D(GL_TEXTURE_2D_ARRAY,
                0,
                GL_RGB8,
                windowSize.x / FRAMEBUFFER_SIZE,
                windowSize.y / FRAMEBUFFER_SIZE,
                3,//depth
                0,
                GL_BGR, GL_UNSIGNED_BYTE,
                null);

        return checkError(gl3, "initTexture");
    }

    private boolean initFramebuffer(GL3 gl3) {

        gl3.glGenFramebuffers(1, framebufferName, 0);
        gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[0]);

        int[] drawBuffers = new int[3];
        drawBuffers[0] = GL_COLOR_ATTACHMENT0;
        drawBuffers[1] = GL_COLOR_ATTACHMENT1;
        drawBuffers[2] = GL_COLOR_ATTACHMENT2;
        gl3.glDrawBuffers(3, drawBuffers, 0);

        for (int i = Texture.RED; i <= Texture.BLUE; ++i) {
            gl3.glFramebufferTextureLayer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + (i - Texture.RED),
                    textureName[0], 0, i);
        }

        if (!isFramebufferComplete(gl3, framebufferName[0])) {
            return false;
        }

        gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);

        return checkError(gl3, "initFramebuffer");
    }

    private boolean initVertexArray(GL3 gl3) {

        gl3.glGenVertexArrays(Program.MAX, vertexArrayName, 0);

        gl3.glBindVertexArray(vertexArrayName[Program.MULTIPLE]);
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[0]);
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, Vertex_v2fv2f.SIZE, 0);
            gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);
        }
        gl3.glBindVertexArray(0);

        gl3.glBindVertexArray(vertexArrayName[Program.SPLASH]);
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[0]);
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, Vertex_v2fv2f.SIZE, 0);
            gl3.glVertexAttribPointer(Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, Vertex_v2fv2f.SIZE, Vec2.SIZE);
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
            Mat4 projection = glm.ortho_(-1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f);
            Mat4 view = new Mat4(1.0f).translate(new Vec3(0.0f, 0.0f, 0.0f));
            Mat4 model = new Mat4(1.0f);
            Mat4 mvp = projection.mul(view).mul(model);

            gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[0]);
            gl3.glViewport(0, 0, windowSize.x * FRAMEBUFFER_SIZE, windowSize.y * FRAMEBUFFER_SIZE);
            gl3.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 0.5f, 0.0f, 1.0f}, 0);

            gl3.glUseProgram(programName[Program.MULTIPLE]);
            gl3.glUniformMatrix4fv(uniformMvp[Program.MULTIPLE], 1, false, mvp.toFa_(), 0);

            gl3.glBindVertexArray(vertexArrayName[Program.MULTIPLE]);
            gl3.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 1);
        }

        // Pass 2
        {
            Mat4 projection = glm.ortho_(-1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f);
            Mat4 view = new Mat4(1.0f);
            Mat4 model = new Mat4(1.0f);
            Mat4 mvp = projection.mul(view).mul(model);

            gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);
            gl3.glViewport(0, 0, windowSize.x, windowSize.y);
            gl3.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 0.5f, 0.0f, 1.0f}, 0);

            gl3.glUseProgram(programName[Program.SPLASH]);
            gl3.glUniformMatrix4fv(uniformMvp[Program.SPLASH], 1, false, mvp.toFa_(), 0);
            gl3.glUniform1i(uniformDiffuse, 0);
        }

        gl3.glActiveTexture(GL_TEXTURE0);
        gl3.glBindTexture(GL_TEXTURE_2D_ARRAY, textureName[0]);

        gl3.glBindVertexArray(vertexArrayName[Program.SPLASH]);

        for (int i = 0; i < Texture.MAX; ++i) {
            gl3.glViewport(viewport[i].x, viewport[i].y, viewport[i].z, viewport[i].w);
            gl3.glUniform1i(uniformLayer, i);

            gl3.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 1);
        }

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;

        for (int i = 0; i < Program.MAX; ++i) {
            gl3.glDeleteProgram(programName[i]);
        }

        gl3.glDeleteBuffers(1, bufferName, 0);
        gl3.glDeleteTextures(1, textureName, 0);
        gl3.glDeleteFramebuffers(1, framebufferName, 0);
        gl3.glDeleteVertexArrays(Program.MAX, vertexArrayName, 0);

        return checkError(gl3, "end");
    }
}
