/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl320;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_COLOR_ATTACHMENT0;
import static com.jogamp.opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_FRAMEBUFFER;
import static com.jogamp.opengl.GL.GL_NEAREST;
import static com.jogamp.opengl.GL.GL_RGB;
import static com.jogamp.opengl.GL.GL_RGB8;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_UNSIGNED_BYTE;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_2D_ARRAY;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_BASE_LEVEL;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_MAX_LEVEL;
import com.jogamp.opengl.GL3;
import static com.jogamp.opengl.GL3ES3.GL_GEOMETRY_SHADER;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
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
public class Gl_320_fbo_layered extends Test {

    public static void main(String[] args) {
        Gl_320_fbo_layered gl_320_fbo_layered = new Gl_320_fbo_layered();
    }

    public Gl_320_fbo_layered() {
        super("Gl-320-fbo-layered", 3, 2);
    }

    private final String SHADERS_SOURCE1 = "fbo-layered";
    private final String SHADERS_SOURCE2 = "fbo-layered-rtt-array";
    private final String SHADERS_ROOT = "src/data/gl_320";

    private Vec2i framebufferSize = new Vec2i(320, 240);

    private int vertexCount = 4;
    private int vertexSize = vertexCount * 2 * 2 * Float.BYTES;
    private float[] vertexData = {
        -1.0f, -1.0f, 0.0f, 0.0f,
        +1.0f, -1.0f, 1.0f, 0.0f,
        +1.0f, +1.0f, 1.0f, 1.0f,
        -1.0f, +1.0f, 0.0f, 1.0f};

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

    private enum Program {

        LAYERING,
        SPLASH,
        MAX
    }

    private enum Shader {

        VERT1,
        GEOM1,
        FRAG1,
        VERT2,
        FRAG2,
        MAX
    }

    private int[] framebufferName = new int[1], vertexArrayName = new int[Program.MAX.ordinal()],
            programName = new int[Program.MAX.ordinal()], uniformMvp = new int[Program.MAX.ordinal()],
            bufferName = new int[Buffer.MAX.ordinal()], textureColorbufferName = new int[1];
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

        ShaderCode[] shaderCodes = new ShaderCode[Shader.MAX.ordinal()];

        // Create program
        if (validated) {

            shaderCodes[Shader.VERT1.ordinal()] = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE1, "vert", null, true);
            shaderCodes[Shader.GEOM1.ordinal()] = ShaderCode.create(gl3, GL_GEOMETRY_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE1, "geom", null, true);
            shaderCodes[Shader.FRAG1.ordinal()] = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE1, "frag", null, true);

            ShaderProgram program = new ShaderProgram();
            program.add(shaderCodes[Shader.VERT1.ordinal()]);
            program.add(shaderCodes[Shader.GEOM1.ordinal()]);
            program.add(shaderCodes[Shader.FRAG1.ordinal()]);
            program.init(gl3);

            programName[Program.LAYERING.ordinal()] = program.program();

            gl3.glBindAttribLocation(programName[Program.LAYERING.ordinal()], Semantic.Attr.POSITION, "Position");
            gl3.glBindFragDataLocation(programName[Program.LAYERING.ordinal()], Semantic.Frag.COLOR, "FragColor");

            program.link(gl3, System.out);
        }
        if (validated) {

            shaderCodes[Shader.VERT2.ordinal()] = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE2, "vert", null, true);
            shaderCodes[Shader.FRAG2.ordinal()] = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE2, "frag", null, true);

            ShaderProgram program = new ShaderProgram();
            program.add(shaderCodes[Shader.VERT2.ordinal()]);
            program.add(shaderCodes[Shader.FRAG2.ordinal()]);
            program.init(gl3);

            programName[Program.SPLASH.ordinal()] = program.program();

            gl3.glBindAttribLocation(programName[Program.SPLASH.ordinal()], Semantic.Attr.POSITION, "Position");
            gl3.glBindAttribLocation(programName[Program.SPLASH.ordinal()], Semantic.Attr.TEXCOORD, "Texcoord");
            gl3.glBindFragDataLocation(programName[Program.SPLASH.ordinal()], Semantic.Frag.COLOR, "Color");

            program.link(gl3, System.out);
        }
        if (validated) {

            for (int i = 0; i < Program.MAX.ordinal(); i++) {
                uniformMvp[i] = gl3.glGetUniformLocation(programName[i], "MVP");
            }
            uniformDiffuse = gl3.glGetUniformLocation(programName[Program.SPLASH.ordinal()], "Diffuse");
            uniformLayer = gl3.glGetUniformLocation(programName[Program.SPLASH.ordinal()], "Layer");
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

        if (!checkFramebuffer(gl3, framebufferName[0])) {
            return false;
        }

        gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        return true;
    }

    private boolean initVertexArray(GL3 gl3) {

        gl3.glGenVertexArrays(Program.MAX.ordinal(), vertexArrayName, 0);

        gl3.glBindVertexArray(vertexArrayName[Program.SPLASH.ordinal()]);
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

        gl3.glBindVertexArray(vertexArrayName[Program.LAYERING.ordinal()]);
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 2 * 2 * Float.BYTES, 0);
            gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);

            gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT.ordinal()]);
        }
        gl3.glBindVertexArray(0);

        return checkError(gl3, "initVertexArray");
    }
}
