/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl320.fbo;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_BACK;
import static com.jogamp.opengl.GL.GL_BGR;
import static com.jogamp.opengl.GL.GL_COLOR_ATTACHMENT0;
import static com.jogamp.opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_FRAMEBUFFER;
import static com.jogamp.opengl.GL.GL_NEAREST;
import static com.jogamp.opengl.GL.GL_RGB8;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNSIGNED_BYTE;
import static com.jogamp.opengl.GL.GL_UNSIGNED_SHORT;
import static com.jogamp.opengl.GL2ES2.GL_COLOR_ATTACHMENT1;
import static com.jogamp.opengl.GL2ES2.GL_COLOR_ATTACHMENT2;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_BASE_LEVEL;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_MAX_LEVEL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import jglm.Vec2i;
import jglm.Vec4i;

/**
 *
 * @author GBarbieri
 */
public class Gl_320_fbo_rtt extends Test {

    public static void main(String[] args) {
        Gl_320_fbo_rtt gl_320_fbo_rtt = new Gl_320_fbo_rtt();
    }

    public Gl_320_fbo_rtt() {
        super("gl-320-fbo-rtt", Profile.CORE, 3, 2);
    }

    private final String SHADERS_SOURCE1 = "fbo-rtt-multiple-output";
    private final String SHADERS_SOURCE2 = "fbo-rtt-single-output";
    private final String SHADERS_ROOT = "src/data/gl_320/fbo";

    private Vec2i FRAMEBUFFER_SIZE = new Vec2i(320, 240);

    private int vertexCount = 4;
    private int vertexSize = vertexCount * 2 * 2 * Float.BYTES;
    private float[] vertexData = {
        -4.0f, -3.0f, 0.0f, 0.0f,
        +4.0f, -3.0f, 1.0f, 0.0f,
        +4.0f, +3.0f, 1.0f, 1.0f,
        -4.0f, +3.0f, 0.0f, 1.0f};

    private int elementCount = 6;
    private int elementSize = elementCount * Short.BYTES;
    private short[] elementData = {
        0, 1, 2,
        2, 3, 0};

    private enum Buffer {
        VERTEX,
        ELEMENT,
        MAX
    }

    private enum Texture {
        TEXTURE_R,
        TEXTURE_G,
        TEXTURE_B,
        MAX
    }

    private enum Program {
        SINGLE,
        MULTIPLE,
        MAX
    }

    private enum Shader {
        VERT1,
        FRAG1,
        VERT2,
        FRAG2,
        MAX
    }

    private int[] framebufferName = new int[1], programName = new int[Program.MAX.ordinal()],
            uniformMvp = new int[Program.MAX.ordinal()], uniformDiffuse = new int[Program.MAX.ordinal()],
            vertexArrayName = new int[Program.MAX.ordinal()], bufferName = new int[Buffer.MAX.ordinal()],
            textureName = new int[Texture.MAX.ordinal()];
    private Vec4i[] viewport = new Vec4i[Texture.MAX.ordinal()];
    private float[] projection = new float[16], viewTranslate = new float[16], view = new float[16],
            model = new float[16], mvp = new float[16];

    @Override
    protected boolean begin(GL gl) {

        GL3 gl3 = (GL3) gl;

        viewport[Texture.TEXTURE_R.ordinal()] = new Vec4i(windowSize.x >> 1, 0, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y);
        viewport[Texture.TEXTURE_G.ordinal()]
                = new Vec4i(windowSize.x >> 1, windowSize.y >> 1, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y);
        viewport[Texture.TEXTURE_B.ordinal()] = new Vec4i(0, windowSize.y >> 1, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y);

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

            ShaderCode vertShaderCode = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE1, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE1, "frag", null, true);

            ShaderProgram shaderProgram = new ShaderProgram();
            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);

            shaderProgram.init(gl3);

            programName[Program.MULTIPLE.ordinal()] = shaderProgram.program();

            gl3.glBindAttribLocation(programName[Program.MULTIPLE.ordinal()], Semantic.Attr.POSITION, "position");
            gl3.glBindFragDataLocation(programName[Program.MULTIPLE.ordinal()], Semantic.Frag.RED, "red");
            gl3.glBindFragDataLocation(programName[Program.MULTIPLE.ordinal()], Semantic.Frag.GREEN, "green");
            gl3.glBindFragDataLocation(programName[Program.MULTIPLE.ordinal()], Semantic.Frag.BLUE, "blue");

            shaderProgram.link(gl3, System.out);
        }
        if (validated) {
            uniformMvp[Program.MULTIPLE.ordinal()]
                    = gl3.glGetUniformLocation(programName[Program.MULTIPLE.ordinal()], "mvp");
        }
        if (validated) {

            ShaderCode vertShaderCode = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE2, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE2, "frag", null, true);

            ShaderProgram shaderProgram = new ShaderProgram();
            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);

            shaderProgram.init(gl3);

            programName[Program.SINGLE.ordinal()] = shaderProgram.program();

            gl3.glBindAttribLocation(programName[Program.SINGLE.ordinal()], Semantic.Attr.POSITION, "position");
            gl3.glBindAttribLocation(programName[Program.SINGLE.ordinal()], Semantic.Attr.TEXCOORD, "texCoord");
            gl3.glBindFragDataLocation(programName[Program.SINGLE.ordinal()], Semantic.Frag.COLOR, "color");

            shaderProgram.link(gl3, System.out);
        }
        if (validated) {

            uniformMvp[Program.SINGLE.ordinal()]
                    = gl3.glGetUniformLocation(programName[Program.SINGLE.ordinal()], "mvp");
            uniformDiffuse[Program.SINGLE.ordinal()]
                    = gl3.glGetUniformLocation(programName[Program.SINGLE.ordinal()], "diffuse");
        }

        return validated & checkError(gl3, "initProgram");
    }

    private boolean initBuffer(GL3 gl3) {

        gl3.glGenBuffers(Buffer.MAX.ordinal(), bufferName, 0);

        gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT.ordinal()]);
        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        gl3.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl3.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        return checkError(gl3, "initBuffer");
    }

    private boolean initTexture(GL3 gl3) {

        gl3.glActiveTexture(GL_TEXTURE0);
        gl3.glGenTextures(Texture.MAX.ordinal(), textureName, 0);

        for (int i = Texture.TEXTURE_R.ordinal(); i <= Texture.TEXTURE_B.ordinal(); ++i) {
            gl3.glBindTexture(GL_TEXTURE_2D, textureName[i]);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

            gl3.glTexImage2D(
                    GL_TEXTURE_2D,
                    0,
                    GL_RGB8,
                    FRAMEBUFFER_SIZE.x,
                    FRAMEBUFFER_SIZE.y,
                    0,
                    GL_BGR,
                    GL_UNSIGNED_BYTE,
                    null);
        }

        return checkError(gl3, "initTexture");
    }

    private boolean initFramebuffer(GL3 gl3) {

        gl3.glGenFramebuffers(1, framebufferName, 0);
        gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[0]);
        gl3.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, textureName[0], 0);
        gl3.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, textureName[1], 0);
        gl3.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT2, textureName[2], 0);
        int[] drawBuffers = {GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1, GL_COLOR_ATTACHMENT2};
        gl3.glDrawBuffers(3, drawBuffers, 0);
        if (!isFramebufferComplete(gl3, framebufferName[0])) {
            return false;
        }

        gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        //GL_ARB_ES3_1_compability
        //GLenum const Buffers = GL_BACK; 
        //glDrawBuffers(1, &Buffers);
        gl3.glDrawBuffer(GL_BACK);
        if (!isFramebufferComplete(gl3, 0)) {
            return false;
        }

        return checkError(gl3, "initFramebuffer");
    }

    private boolean initVertexArray(GL3 gl3) {

        gl3.glGenVertexArrays(Program.MAX.ordinal(), vertexArrayName, 0);

        gl3.glBindVertexArray(vertexArrayName[Program.MULTIPLE.ordinal()]);
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 2 * 2 * Float.BYTES, 0);
            gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);

            gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT.ordinal()]);
        }
        gl3.glBindVertexArray(0);

        gl3.glBindVertexArray(vertexArrayName[Program.SINGLE.ordinal()]);
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 2 * 2 * Float.BYTES, 0);
            gl3.glVertexAttribPointer(Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, 2 * 2 * Float.BYTES, 2 * Float.BYTES);
            gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl3.glEnableVertexAttribArray(Semantic.Attr.TEXCOORD);

            gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT.ordinal()]);
        }
        gl3.glBindVertexArray(0);

        return checkError(gl3, "initVertexArray");
    }

    @Override
    protected boolean render(GL gl) {

        GL3 gl3 = (GL3) gl;

        int border = 16;

        // Pass 1
        {
            // Compute the MVP (Model View Projection matrix)
            FloatUtil.makeOrtho(projection, 0, true, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f);
            FloatUtil.makeTranslation(viewTranslate, true, 0.0f, 0.0f, 0.0f);
            view = viewTranslate;
            FloatUtil.makeIdentity(model);
            FloatUtil.multMatrix(projection, view, mvp);
            FloatUtil.multMatrix(mvp, model);

            gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[0]);
            gl3.glViewport(0, 0, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y);
            gl3.glScissor(border, border, FRAMEBUFFER_SIZE.x - border * 2, FRAMEBUFFER_SIZE.y - border * 2);
            gl3.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 0.5f, 0.0f, 1.0f}, 0);

            gl3.glUseProgram(programName[Program.MULTIPLE.ordinal()]);
            gl3.glUniformMatrix4fv(uniformMvp[Program.MULTIPLE.ordinal()], 1, false, mvp, 0);

            gl3.glBindVertexArray(vertexArrayName[Program.MULTIPLE.ordinal()]);
            gl3.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0);
        }

        // Pass 2
        {
            FloatUtil.makeOrtho(projection, 0, true, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f);
            FloatUtil.makeIdentity(view);
            FloatUtil.makeIdentity(model);
            FloatUtil.multMatrix(projection, view, mvp);
            FloatUtil.multMatrix(mvp, model);

            gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);
            gl3.glViewport(0, 0, windowSize.x, windowSize.y);
            gl3.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 0.5f, 0.0f, 1.0f}, 0);

            gl3.glUseProgram(programName[Program.SINGLE.ordinal()]);
            gl3.glUniformMatrix4fv(uniformMvp[Program.SINGLE.ordinal()], 1, false, mvp, 0);
            gl3.glUniform1i(uniformDiffuse[Program.SINGLE.ordinal()], 0);
        }

        for (int i = 0; i < Texture.MAX.ordinal(); ++i) {
            gl3.glViewport(viewport[i].x, viewport[i].y, viewport[i].z, viewport[i].w);

            gl3.glActiveTexture(GL_TEXTURE0);
            gl3.glBindTexture(GL_TEXTURE_2D, textureName[i]);

            gl3.glBindVertexArray(vertexArrayName[Program.SINGLE.ordinal()]);
            gl3.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0);
        }

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;

        for (int i = 0; i < Program.MAX.ordinal(); ++i) {
            gl3.glDeleteProgram(programName[i]);
        }

        gl3.glDeleteBuffers(Buffer.MAX.ordinal(), bufferName, 0);
        gl3.glDeleteTextures(Texture.MAX.ordinal(), textureName, 0);
        gl3.glDeleteFramebuffers(1, framebufferName, 0);

        return checkError(gl3, "end");
    }
}
