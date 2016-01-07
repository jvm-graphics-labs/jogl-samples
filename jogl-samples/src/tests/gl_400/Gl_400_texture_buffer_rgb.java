/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_400;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_RGB32F;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE1;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNSIGNED_SHORT;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import static com.jogamp.opengl.GL2ES3.GL_DEPTH;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_BUFFER;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import jglm.Vec2;

/**
 *
 * @author GBarbieri
 */
public class Gl_400_texture_buffer_rgb extends Test {

    public static void main(String[] args) {
        Gl_400_texture_buffer_rgb gl_400_texture_buffer_rgb = new Gl_400_texture_buffer_rgb();
    }

    public Gl_400_texture_buffer_rgb() {
        super("gl-400-texture-buffer-rgb", Profile.CORE, 4, 0, new Vec2((float) Math.PI * 0.2f, (float) Math.PI * 0.2f));
    }

    private final String SHADERS_SOURCE = "texture-buffer";
    private final String SHADERS_ROOT = "src/data/gl_400";

    private int vertexCount = 4;
    private int vertexSize = vertexCount * 2 * Float.BYTES;
    private float[] vertexData = {
        -1.0f, -1.0f,
        +1.0f, -1.0f,
        +1.0f, 1.0f,
        -1.0f, 1.0f};

    private int elementCount = 6;
    private int elementSize = elementCount * Short.BYTES;
    private short[] elementData = {
        0, 1, 2,
        2, 3, 0};

    private enum Buffer {
        VERTEX,
        ELEMENT,
        DISPLACEMENT,
        DIFFUSE,
        MAX
    };

    private enum Texture {
        DISPLACEMENT,
        DIFFUSE,
        MAX
    };

    private int[] bufferName = new int[Buffer.MAX.ordinal()], textureName = new int[Texture.MAX.ordinal()],
            vertexArrayName = {0};
    private int programName, uniformMvp, uniformDiffuse, uniformDisplacement;
    private float[] projection = new float[16], model = new float[16], mvp = new float[16];

    private boolean initTest(GL4 gl4) {

        boolean validated = true;
        gl4.glEnable(GL_DEPTH_TEST);

        return validated && checkError(gl4, "initTest");
    }

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;

        if (validated) {
            validated = initTest(gl4);
        }
        if (validated) {
            validated = initProgram(gl4);
        }
        if (validated) {
            validated = initBuffer(gl4);
        }
        if (validated) {
            validated = initTexture(gl4);
        }
        if (validated) {
            validated = initVertexArray(gl4);
        }

        return validated && checkError(gl4, "begin");
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        // Create program
        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            ShaderCode vertexShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragmentShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

            shaderProgram.init(gl4);

            shaderProgram.add(vertexShaderCode);
            shaderProgram.add(fragmentShaderCode);

            programName = shaderProgram.program();

            shaderProgram.link(gl4, System.out);

        }

        // Get variables locations
        if (validated) {

            uniformMvp = gl4.glGetUniformLocation(programName, "mvp");
            uniformDiffuse = gl4.glGetUniformLocation(programName, "diffuse");
            uniformDisplacement = gl4.glGetUniformLocation(programName, "displacement");
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        gl4.glGenBuffers(Buffer.MAX.ordinal(), bufferName, 0);

        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT.ordinal()]);
        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl4.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        float[] displacement = {
            +0.1f, +0.3f, -1.0f,
            -0.5f, +0.0f, -0.5f,
            -0.2f, -0.2f, +0.0f,
            +0.3f, +0.2f, +0.5f,
            +0.1f, -0.3f, +1.0f};

        gl4.glBindBuffer(GL_TEXTURE_BUFFER, bufferName[Buffer.DISPLACEMENT.ordinal()]);
        gl4.glBufferData(GL_TEXTURE_BUFFER, displacement.length * Float.BYTES,
                GLBuffers.newDirectFloatBuffer(displacement), GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_TEXTURE_BUFFER, 0);

        float[] diffuse = {
            1.0f, 0.0f, 0.0f,
            1.0f, 0.5f, 0.0f,
            1.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 1.0f};

        gl4.glBindBuffer(GL_TEXTURE_BUFFER, bufferName[Buffer.DIFFUSE.ordinal()]);
        gl4.glBufferData(GL_TEXTURE_BUFFER, diffuse.length * Float.BYTES,
                GLBuffers.newDirectFloatBuffer(diffuse), GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_TEXTURE_BUFFER, 0);

        return checkError(gl4, "initBuffer");
    }

    private boolean initTexture(GL4 gl4) {

        gl4.glGenTextures(Texture.MAX.ordinal(), textureName, 0);

        gl4.glBindTexture(GL_TEXTURE_BUFFER, textureName[Texture.DISPLACEMENT.ordinal()]);
        gl4.glTexBuffer(GL_TEXTURE_BUFFER, GL_RGB32F, bufferName[Buffer.DISPLACEMENT.ordinal()]);
        gl4.glBindTexture(GL_TEXTURE_BUFFER, 0);

        gl4.glBindTexture(GL_TEXTURE_BUFFER, textureName[Texture.DIFFUSE.ordinal()]);
        gl4.glTexBuffer(GL_TEXTURE_BUFFER, GL_RGB32F, bufferName[Buffer.DIFFUSE.ordinal()]);
        gl4.glBindTexture(GL_TEXTURE_BUFFER, 0);

        return checkError(gl4, "initTextureBuffer");
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(1, vertexArrayName, 0);
        gl4.glBindVertexArray(vertexArrayName[0]);
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 0, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);

            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT.ordinal()]);
        }
        gl4.glBindVertexArray(0);

        return checkError(gl4, "initVertexArray");
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        FloatUtil.makePerspective(projection, 0, true, (float) Math.PI * 0.25f,
                (float) windowSize.x / windowSize.y, 0.1f, 100.0f);
        FloatUtil.makeIdentity(model);
        FloatUtil.multMatrix(projection, view(), mvp);
        FloatUtil.multMatrix(mvp, model);

        gl4.glViewport(0, 0, windowSize.x, windowSize.y);

        float[] depth = {1.0f};
        gl4.glClearBufferfv(GL_DEPTH, 0, depth, 0);
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, 0);

        gl4.glUseProgram(programName);
        gl4.glUniformMatrix4fv(uniformMvp, 1, false, mvp, 0);
        gl4.glUniform1i(uniformDisplacement, 0);
        gl4.glUniform1i(uniformDiffuse, 1);

        gl4.glActiveTexture(GL_TEXTURE0);
        gl4.glBindTexture(GL_TEXTURE_BUFFER, textureName[Texture.DISPLACEMENT.ordinal()]);

        gl4.glActiveTexture(GL_TEXTURE1);
        gl4.glBindTexture(GL_TEXTURE_BUFFER, textureName[Texture.DIFFUSE.ordinal()]);

        gl4.glBindVertexArray(vertexArrayName[0]);
        gl4.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 5, 0);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteTextures(Texture.MAX.ordinal(), textureName, 0);
        gl4.glDeleteBuffers(Buffer.MAX.ordinal(), bufferName, 0);
        gl4.glDeleteProgram(programName);
        gl4.glDeleteVertexArrays(1, vertexArrayName, 0);

        return checkError(gl4, "end");
    }
}
