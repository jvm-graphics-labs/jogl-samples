/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_410;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_ALPHA;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_COMPRESSED_RGBA_S3TC_DXT5_EXT;
import static com.jogamp.opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNSIGNED_INT;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER_BIT;
import static com.jogamp.opengl.GL2ES2.GL_RED;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER_BIT;
import static com.jogamp.opengl.GL2ES3.GL_BLUE;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import static com.jogamp.opengl.GL2ES3.GL_GREEN;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_BASE_LEVEL;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_MAX_LEVEL;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_SWIZZLE_A;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_SWIZZLE_B;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_SWIZZLE_G;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_SWIZZLE_R;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import jgli.Texture2D;

/**
 *
 * @author GBarbieri
 */
public class Gl_410_program_separate extends Test {

    public static void main(String[] args) {
        Gl_410_program_separate gl_410_program_separate = new Gl_410_program_separate();
    }

    public Gl_410_program_separate() {
        super("gl-410-program-separate", Profile.CORE, 4, 1);
    }

    private final String SHADERS_SOURCE = "separate";
    private final String SHADERS_ROOT = "src/data/gl_410";
    private final String TEXTURE_DIFFUSE = "kueken7_rgba_dxt5_unorm.dds";

    private int vertexCount = 4;
    private int vertexSize = vertexCount * 2 * 2 * Float.BYTES;
    private float[] vertexData = {
        -1.0f, -1.0f, 0.0f, 1.0f,
        +1.0f, -1.0f, 1.0f, 1.0f,
        +1.0f, +1.0f, 1.0f, 0.0f,
        -1.0f, +1.0f, 0.0f, 0.0f};

    private int elementCount = 6;
    private int elementSize = elementCount * Integer.BYTES;
    private int[] elementData = {
        0, 1, 2,
        2, 3, 0};

    private enum Buffer {
        VERTEX,
        ELEMENT,
        MAX
    }

    private enum Program {
        VERTEX,
        FRAGMENT,
        MAX
    }

    private int[] pipelineName = {0}, separateProgramName = new int[Program.MAX.ordinal()],
            bufferName = new int[Buffer.MAX.ordinal()], vertexArrayName = {0}, textureName = {0};
    private int separateUniformMvp, separateUniformDiffuse, unifiedProgramName, unifiedUniformMvp, unifiedUniformDiffuse;
    private float[] projection = new float[16], model = new float[16], mvp = new float[16];

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;

        if (validated) {
            validated = initSeparateProgram(gl4);
        }
        if (validated) {
            validated = initUnifiedProgram(gl4);
        }
        if (validated) {
            validated = initVertexBuffer(gl4);
        }
        if (validated) {
            validated = initVertexArray(gl4);
        }
        if (validated) {
            validated = initTexture(gl4);
        }

        return validated;
    }

    private boolean initUnifiedProgram(GL4 gl4) {

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

            unifiedProgramName = shaderProgram.program();

            shaderProgram.link(gl4, System.out);
        }

        // Get variables locations
        if (validated) {

            unifiedUniformMvp = gl4.glGetUniformLocation(unifiedProgramName, "mvp");
            unifiedUniformDiffuse = gl4.glGetUniformLocation(unifiedProgramName, "diffuse");
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initSeparateProgram(GL4 gl4) {

        boolean validated = true;

        try {

            if (validated) {

                String[] vertexSourceContent = new String[]{
                    new Scanner(new File(SHADERS_ROOT + "/" + SHADERS_SOURCE + ".vert")).useDelimiter("\\A").next()};
                separateProgramName[Program.VERTEX.ordinal()]
                        = gl4.glCreateShaderProgramv(GL_VERTEX_SHADER, 1, vertexSourceContent);
            }

            if (validated) {

                String[] fragmentSourceContent = new String[]{
                    new Scanner(new File(SHADERS_ROOT + "/" + SHADERS_SOURCE + ".frag")).useDelimiter("\\A").next()};
                separateProgramName[Program.FRAGMENT.ordinal()]
                        = gl4.glCreateShaderProgramv(GL_FRAGMENT_SHADER, 1, fragmentSourceContent);
            }

            if (validated) {

                validated = validated && checkProgram(gl4, separateProgramName[Program.VERTEX.ordinal()]);
                validated = validated && checkProgram(gl4, separateProgramName[Program.FRAGMENT.ordinal()]);
            }
            if (validated) {

                gl4.glGenProgramPipelines(1, pipelineName, 0);
                gl4.glUseProgramStages(pipelineName[0], GL_VERTEX_SHADER_BIT,
                        separateProgramName[Program.VERTEX.ordinal()]);
                gl4.glUseProgramStages(pipelineName[0], GL_FRAGMENT_SHADER_BIT,
                        separateProgramName[Program.FRAGMENT.ordinal()]);
            }
            if (validated) {

                separateUniformMvp = gl4.glGetUniformLocation(separateProgramName[Program.VERTEX.ordinal()], "mvp");
                separateUniformDiffuse
                        = gl4.glGetUniformLocation(separateProgramName[Program.FRAGMENT.ordinal()], "diffuse");
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Gl_410_program_64.class.getName()).log(Level.SEVERE, null, ex);
        }

        return validated && checkError(gl4, "initProgram");
    }

    private boolean initTexture(GL4 gl4) {

        try {
            jgli.Texture2D texture = new Texture2D(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE));

            gl4.glGenTextures(1, textureName, 0);

            gl4.glActiveTexture(GL_TEXTURE0);
            gl4.glBindTexture(GL_TEXTURE_2D, textureName[0]);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, texture.levels() - 1);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_R, GL_RED);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_G, GL_GREEN);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_B, GL_BLUE);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_A, GL_ALPHA);

            for (int level = 0; level < texture.levels(); ++level) {
                gl4.glCompressedTexImage2D(
                        GL_TEXTURE_2D,
                        level,
                        GL_COMPRESSED_RGBA_S3TC_DXT5_EXT,
                        texture.dimensions(level)[0],
                        texture.dimensions(level)[1],
                        0,
                        texture.size(level),
                        texture.data(0, 0, level));
            }

        } catch (IOException ex) {
            Logger.getLogger(Gl_410_program_separate.class.getName()).log(Level.SEVERE, null, ex);
        }
        return checkError(gl4, "initTexture");
    }

    private boolean initVertexBuffer(GL4 gl4) {

        gl4.glGenBuffers(Buffer.MAX.ordinal(), bufferName, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl4.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT.ordinal()]);
        IntBuffer elementBuffer = GLBuffers.newDirectIntBuffer(elementData);
        gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        return checkError(gl4, "initArrayBuffer");
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(1, vertexArrayName, 0);
        gl4.glBindVertexArray(vertexArrayName[0]);
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 2 * 2 * Float.BYTES, 0);
            gl4.glVertexAttribPointer(Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, 2 * 2 * Float.BYTES, 2 * Float.BYTES);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl4.glEnableVertexAttribArray(Semantic.Attr.TEXCOORD);
        }
        gl4.glBindVertexArray(0);

        return checkError(gl4, "initVertexArray");
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        // Compute the MVP (Model View Projection matrix)
        FloatUtil.makePerspective(projection, 0, true, (float) Math.PI * 0.25f,
                windowSize.x * 0.5f / windowSize.y, 0.1f, 100.0f);
        FloatUtil.makeIdentity(model);
        FloatUtil.multMatrix(projection, view(), mvp);
        FloatUtil.multMatrix(mvp, model);

        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{0.0f, 0.0f, 0.0f, 0.0f}, 0);

        gl4.glBindTexture(GL_TEXTURE_2D, textureName[0]);
        gl4.glBindVertexArray(vertexArrayName[0]);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT.ordinal()]);

        // Render with the separate programs
        gl4.glViewportIndexedfv(0, new float[]{0, 0, windowSize.x / 2, windowSize.y}, 0);
        gl4.glProgramUniformMatrix4fv(separateProgramName[Program.VERTEX.ordinal()],
                separateUniformMvp, 1, false, mvp, 0);
        gl4.glProgramUniform1i(separateProgramName[Program.FRAGMENT.ordinal()], separateUniformDiffuse, 0);
        gl4.glBindProgramPipeline(pipelineName[0]);
        {
            gl4.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_INT, 0, 1, 0);
        }
        gl4.glBindProgramPipeline(0);

        // Render with the unified programs
        gl4.glViewportIndexedfv(0, new float[]{windowSize.x / 2, 0, windowSize.x / 2, windowSize.y}, 0);
        gl4.glProgramUniformMatrix4fv(unifiedProgramName, unifiedUniformMvp, 1, false, mvp, 0);
        gl4.glProgramUniform1i(unifiedProgramName, unifiedUniformDiffuse, 0);
        gl4.glUseProgram(unifiedProgramName);
        {
            gl4.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_INT, 0, 1, 0);
        }
        gl4.glUseProgram(0);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteBuffers(Buffer.MAX.ordinal(), bufferName, 0);
        gl4.glDeleteVertexArrays(1, vertexArrayName, 0);
        gl4.glDeleteTextures(1, textureName, 0);
        gl4.glDeleteProgram(separateProgramName[Program.VERTEX.ordinal()]);
        gl4.glDeleteProgram(separateProgramName[Program.FRAGMENT.ordinal()]);
        gl4.glDeleteProgram(unifiedProgramName);
        gl4.glDeleteProgramPipelines(1, pipelineName, 0);

        return true;
    }
}
