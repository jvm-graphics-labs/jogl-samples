/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_400;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL3.*;
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
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import jgli.Texture2d;
import glm.vec._2.Vec2;

/**
 *
 * @author GBarbieri
 */
public class Gl_400_program_subroutine extends Test {

    public static void main(String[] args) {
        Gl_400_program_subroutine gl_400_program_subroutine = new Gl_400_program_subroutine();
    }

    public Gl_400_program_subroutine() {
        super("gl-400-program-subroutine", Profile.CORE, 4, 0, new Vec2(0.0f, 0.0f));
    }

    private final String SHADERS_SOURCE = "subroutine";
    private final String SHADERS_ROOT = "src/data/gl_400";
    private final String TEXTURE_DIFFUSE_RGB8 = "kueken7_rgba8_srgb.dds";
    private final String TEXTURE_DIFFUSE_DXT1 = "kueken7_rgb_dxt1_unorm.dds";

    private int vertexCount = 4;
    private int vertexSize = vertexCount * Vertex_v2fv2f.SIZE;
    private float[] vertexData = {
        -1.0f, -1.0f,/**/ 0.0f, 1.0f,
        +1.0f, -1.0f,/**/ 1.0f, 1.0f,
        +1.0f, +1.0f,/**/ 1.0f, 0.0f,
        -1.0f, +1.0f,/**/ 0.0f, 0.0f};

    private int elementCount = 6;
    private int elementSize = elementCount * Integer.BYTES;
    private int[] elementData = {
        0, 1, 2,
        2, 3, 0};

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int ELEMENT = 1;
        public static final int MAX = 2;
    }

    private class Texture {

        public static final int DXT1 = 0;
        public static final int RGB8 = 1;
        public static final int MAX = 2;
    }

    private int programName, uniformMvp, uniformDiffuse, uniformRGB8, uniformDXT1, uniformDisplacement;
    private int[] bufferName = new int[Buffer.MAX], textureName = new int[Texture.MAX], vertexArrayName = {0},
            indexDXT1 = {0}, indexRGB8 = {0};

    private boolean initTest(GL4 gl4) {

        gl4.glEnable(GL_DEPTH_TEST);

        return checkError(gl4, "initTest");
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
            validated = initVertexArray(gl4);
        }
        if (validated) {
            validated = initTexture(gl4);
        }

        logImplementationDependentLimit(gl4, GL_MAX_SUBROUTINES, "GL_MAX_SUBROUTINES");

        return validated;
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        // Create program
        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

            shaderProgram.init(gl4);

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);

            programName = shaderProgram.program();

            shaderProgram.link(gl4, System.out);

        }

        // Get variables locations
        if (validated) {

            int[] programVertSubroutine = {0};
            int[] programFragSubroutine = {0};
            gl4.glGetProgramStageiv(programName, GL_VERTEX_SHADER, GL_ACTIVE_SUBROUTINE_UNIFORMS,
                    programVertSubroutine, 0);
            gl4.glGetProgramStageiv(programName, GL_FRAGMENT_SHADER, GL_ACTIVE_SUBROUTINE_UNIFORMS,
                    programFragSubroutine, 0);

            uniformMvp = gl4.glGetUniformLocation(programName, "mvp");
            uniformDXT1 = gl4.glGetUniformLocation(programName, "diffuseDXT1");
            uniformRGB8 = gl4.glGetUniformLocation(programName, "diffuseRGB8");
            uniformDisplacement = gl4.glGetUniformLocation(programName, "displacement");
            uniformDiffuse = gl4.glGetSubroutineUniformLocation(programName, GL_FRAGMENT_SHADER, "diffuse");
            indexDXT1[0] = gl4.glGetSubroutineIndex(programName, GL_FRAGMENT_SHADER, "diffuseLQ");
            indexRGB8[0] = gl4.glGetSubroutineIndex(programName, GL_FRAGMENT_SHADER, "diffuseHQ");
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        gl4.glGenBuffers(Buffer.MAX, bufferName, 0);

        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT]);
        IntBuffer elementBuffer = GLBuffers.newDirectIntBuffer(elementData);
        gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(elementBuffer);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl4.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(vertexBuffer);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        return true;
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(1, vertexArrayName, 0);
        gl4.glBindVertexArray(vertexArrayName[0]);
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, Vertex_v2fv2f.SIZE, 0);
            gl4.glVertexAttribPointer(Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, Vertex_v2fv2f.SIZE, Vec2.SIZE);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl4.glEnableVertexAttribArray(Semantic.Attr.TEXCOORD);
        }
        gl4.glBindVertexArray(0);

        return true;
    }

    private boolean initTexture(GL4 gl4) {

        try {
            gl4.glGenTextures(Texture.MAX, textureName, 0);

            {
                jgli.Texture2d texture = new Texture2d(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE_RGB8));
                jgli.Gl.Format format = jgli.Gl.translate(texture.format());

                gl4.glActiveTexture(GL_TEXTURE0);
                gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.RGB8]);
                gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
                gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, texture.levels());
                gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_R, GL_GREEN);
                gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_G, GL_GREEN);
                gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_B, GL_GREEN);
                gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_A, GL_ONE);
                gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
                gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
                for (int level = 0; level < texture.levels(); ++level) {
                    gl4.glTexImage2D(GL_TEXTURE_2D, level,
                            format.internal.value,
                            texture.dimensions(level)[0], texture.dimensions(level)[1],
                            0,
                            format.external.value, format.type.value,
                            texture.data(level));
                }

            }

            {
                jgli.Texture2d texture = new Texture2d(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE_DXT1));
                jgli.Gl.Format format = jgli.Gl.translate(texture.format());
                jgli.Gl.Swizzles swizzles = jgli.Gl.translate(texture.swizzles());

                gl4.glActiveTexture(GL_TEXTURE0);
                gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.DXT1]);
                gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
                gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, texture.levels());
                gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_R, swizzles.r.value);
                gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_G, swizzles.g.value);
                gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_B, swizzles.b.value);
                gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_A, swizzles.a.value);
                gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
                gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
                for (int level = 0; level < texture.levels(); ++level) {
                    gl4.glCompressedTexImage2D(GL_TEXTURE_2D, level,
                            format.internal.value,
                            texture.dimensions(level)[0], texture.dimensions(level)[1],
                            0,
                            texture.size(level),
                            texture.data(level));
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Gl_400_program_subroutine.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;
        
        Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, (float) windowSize.x / windowSize.y, 0.1f, 100.0f);
        Mat4 model = new Mat4(1.0f);
        Mat4 mvp = projection.mul(viewMat4()).mul(model);

        gl4.glViewport(0, 0, windowSize.x, windowSize.y);

        float[] depth = {1.0f};
        gl4.glClearBufferfv(GL_DEPTH, 0, depth, 0);
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{0.0f, 0.0f, 0.0f, 0.0f}, 0);

        gl4.glActiveTexture(GL_TEXTURE0);
        gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.RGB8]);

        gl4.glActiveTexture(GL_TEXTURE1);
        gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.DXT1]);

        gl4.glUseProgram(programName);
        gl4.glUniformMatrix4fv(uniformMvp, 1, false, mvp.toFa_(), 0);
        gl4.glUniform1i(uniformRGB8, 0);
        gl4.glUniform1i(uniformDXT1, 1);

        gl4.glBindVertexArray(vertexArrayName[0]);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT]);

        gl4.glUniform1f(uniformDisplacement, 1.1f);
        gl4.glUniformSubroutinesuiv(GL_FRAGMENT_SHADER, 1, indexDXT1, 0);
        gl4.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_INT, 0, 1, 0);

        gl4.glUniform1f(uniformDisplacement, -1.1f);
        gl4.glUniformSubroutinesuiv(GL_FRAGMENT_SHADER, 1, indexRGB8, 0);
        gl4.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_INT, 0, 1, 0);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteVertexArrays(1, vertexArrayName, 0);
        gl4.glDeleteBuffers(Buffer.MAX, bufferName, 0);
        gl4.glDeleteProgram(programName);
        gl4.glDeleteTextures(Texture.MAX, textureName, 0);

        return checkError(gl4, "end");
    }
}
