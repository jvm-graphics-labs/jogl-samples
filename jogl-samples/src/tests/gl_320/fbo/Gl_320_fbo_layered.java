/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_320.fbo;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2ES3.*;
import com.jogamp.opengl.GL3;
import static com.jogamp.opengl.GL3ES3.GL_GEOMETRY_SHADER;
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
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import jglm.Vec2i;
import jglm.Vec4i;

/**
 *
 * @author GBarbieri
 */
public class Gl_320_fbo_layered extends Test {

    public static void main(String[] args) {
        Gl_320_fbo_layered gl_320_fbo_layered = new Gl_320_fbo_layered();
    }

    public Gl_320_fbo_layered() {
        super("Gl-320-fbo-layered", Profile.CORE, 3, 2);
    }

    private final String SHADERS_SOURCE1 = "fbo-layered";
    private final String SHADERS_SOURCE2 = "fbo-layered-rtt-array";
    private final String SHADERS_ROOT = "src/data/gl_320/fbo";

    private Vec2i framebufferSize = new Vec2i(320, 240);

    private int vertexCount = 4;
    private int vertexSize = vertexCount * 2 * Vec2.SIZE;
    private float[] vertexData = {
        -1.0f, -1.0f,/**/ 0.0f, 0.0f,
        +1.0f, -1.0f,/**/ 1.0f, 0.0f,
        +1.0f, +1.0f,/**/ 1.0f, 1.0f,
        -1.0f, +1.0f,/**/ 0.0f, 1.0f};

    private int elementCount = 6;
    private int elementSize = elementCount * Short.BYTES;
    private short[] elementData = {
        0, 1, 2,
        2, 3, 0};

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int ELEMENT = 1;
        public static final int MAX = 2;
    }

    private class Program {

        public static final int LAYERING = 0;
        public static final int SPLASH = 1;
        public static final int MAX = 2;
    }

    private class Shader {

        public static final int VERT1 = 0;
        public static final int GEOM1 = 1;
        public static final int FRAG1 = 2;
        public static final int VERT2 = 3;
        public static final int FRAG2 = 4;
        public static final int MAX = 5;
    }

    private int[] framebufferName = {0}, vertexArrayName = new int[Program.MAX], programName = new int[Program.MAX],
            uniformMvp = new int[Program.MAX], bufferName = new int[Buffer.MAX], textureColorbufferName = {0};
    private int uniformDiffuse, uniformLayer;
    private Vec4i[] viewport = new Vec4i[4];

    @Override
    protected boolean begin(GL gl) {

        GL3 gl3 = (GL3) gl;

        int border = 2;
        viewport[0] = new Vec4i(border, border, framebufferSize.x - 2 * border, framebufferSize.y - 2 * border);
        viewport[1] = new Vec4i((windowSize.x >> 1) + border, 1, framebufferSize.x - 2 * border,
                framebufferSize.y - 2 * border);
        viewport[2] = new Vec4i((windowSize.x >> 1) + border, (windowSize.y >> 1) + border,
                framebufferSize.x - 2 * border, framebufferSize.y - 2 * border);
        viewport[3] = new Vec4i(border, (windowSize.y >> 1) + border, framebufferSize.x - 2 * border,
                framebufferSize.y - 2 * border);

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
            shaderCodes[Shader.GEOM1] = ShaderCode.create(gl3, GL_GEOMETRY_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE1, "geom", null, true);
            shaderCodes[Shader.FRAG1] = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE1, "frag", null, true);

            ShaderProgram program = new ShaderProgram();
            program.add(shaderCodes[Shader.VERT1]);
            program.add(shaderCodes[Shader.GEOM1]);
            program.add(shaderCodes[Shader.FRAG1]);
            program.init(gl3);

            programName[Program.LAYERING] = program.program();

            gl3.glBindAttribLocation(programName[Program.LAYERING], Semantic.Attr.POSITION, "position");
            gl3.glBindFragDataLocation(programName[Program.LAYERING], Semantic.Frag.COLOR, "fragColor");

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

            for (int i = 0; i < Program.MAX; i++) {
                uniformMvp[i] = gl3.glGetUniformLocation(programName[i], "mvp");
            }
            uniformDiffuse = gl3.glGetUniformLocation(programName[Program.SPLASH], "diffuse");
            uniformLayer = gl3.glGetUniformLocation(programName[Program.SPLASH], "layer");
        }
        return validated & checkError(gl3, "initProgram");
    }

    private boolean initBuffer(GL3 gl3) {

        gl3.glGenBuffers(Buffer.MAX, bufferName, 0);

        gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT]);
        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        gl3.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(elementBuffer);
        gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl3.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(vertexBuffer);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        return checkError(gl3, "initBuffer");
    }

    private boolean initTexture(GL3 gl3) {

        gl3.glGenTextures(1, textureColorbufferName, 0);

        gl3.glActiveTexture(GL_TEXTURE0);
        gl3.glBindTexture(GL_TEXTURE_2D_ARRAY, textureColorbufferName[0]);
        gl3.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_BASE_LEVEL, 0);
        gl3.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAX_LEVEL, 0);
        gl3.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        gl3.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        gl3.glTexImage3D(
                GL_TEXTURE_2D_ARRAY,
                0,
                GL_RGB8,
                framebufferSize.x,
                framebufferSize.y,
                4, //depth
                0,
                GL_RGB,
                GL_UNSIGNED_BYTE,
                null);

        return checkError(gl3, "initTexture");
    }

    private boolean initFramebuffer(GL3 gl3) {

        gl3.glGenFramebuffers(1, framebufferName, 0);
        gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[0]);
        gl3.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, textureColorbufferName[0], 0);

        if (!isFramebufferComplete(gl3, framebufferName[0])) {
            return false;
        }

        gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        return true;
    }

    private boolean initVertexArray(GL3 gl3) {

        gl3.glGenVertexArrays(Program.MAX, vertexArrayName, 0);

        gl3.glBindVertexArray(vertexArrayName[Program.SPLASH]);
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 2 * Vec2.SIZE, 0);
            gl3.glVertexAttribPointer(Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, 2 * Vec2.SIZE, Vec2.SIZE);
            gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl3.glEnableVertexAttribArray(Semantic.Attr.TEXCOORD);

            gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT]);
        }
        gl3.glBindVertexArray(0);

        gl3.glBindVertexArray(vertexArrayName[Program.LAYERING]);
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 2 * Vec2.SIZE, 0);
            gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);

            gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT]);
        }
        gl3.glBindVertexArray(0);

        return checkError(gl3, "initVertexArray");
    }

    @Override
    protected boolean render(GL gl) {

        GL3 gl3 = (GL3) gl;

        Mat4 projection = glm.ortho_(-1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f);
        Mat4 view = new Mat4(1.0f);
        Mat4 model = new Mat4(1.0f);
        Mat4 mvp = projection.mul(view).mul(model);

        // Pass 1
        {
            gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[0]);
            gl3.glViewport(0, 0, framebufferSize.x, framebufferSize.y);

            gl3.glUseProgram(programName[Program.LAYERING]);
            gl3.glUniformMatrix4fv(uniformMvp[Program.LAYERING], 1, false, mvp.toFa_(), 0);

            gl3.glBindVertexArray(vertexArrayName[Program.LAYERING]);
            gl3.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0);
        }

        // Pass 2
        {
            gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);
            gl3.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, 0);

            gl3.glUseProgram(programName[Program.SPLASH]);
            gl3.glUniformMatrix4fv(uniformMvp[Program.SPLASH], 1, false, mvp.toFa_(), 0);
            gl3.glUniform1i(uniformDiffuse, 0);

            gl3.glActiveTexture(GL_TEXTURE0);
            gl3.glBindTexture(GL_TEXTURE_2D_ARRAY, textureColorbufferName[0]);

            gl3.glBindVertexArray(vertexArrayName[Program.SPLASH]);

            for (int i = 0; i < 4; ++i) {
                gl3.glUniform1i(uniformLayer, i);
                gl3.glViewport(viewport[i].x, viewport[i].y, viewport[i].z, viewport[i].w);
                gl3.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0);
            }
        }

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;

        for (int i = 0; i < Program.MAX; ++i) {
            gl3.glDeleteProgram(programName[i]);
        }
        gl3.glDeleteVertexArrays(Program.MAX, vertexArrayName, 0);
        gl3.glDeleteBuffers(Buffer.MAX, bufferName, 0);
        gl3.glDeleteTextures(1, textureColorbufferName, 0);
        gl3.glDeleteFramebuffers(1, framebufferName, 0);

        return checkError(gl3, "end");
    }
}
