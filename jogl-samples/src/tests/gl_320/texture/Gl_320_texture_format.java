/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_320.texture;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2ES3.*;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import jgli.Texture2d;
import jglm.Vec4i;

/**
 *
 * @author GBarbieri
 */
public class Gl_320_texture_format extends Test {

    public static void main(String[] args) {
        Gl_320_texture_format gl_320_texture_format = new Gl_320_texture_format();
    }

    public Gl_320_texture_format() {
        super("gl-320-texture-format", Profile.CORE, 3, 2);
    }

    private final String VERT_SHADERS_SOURCE = "texture-format";
    private final String[] FRAG_SHADERS_SOURCE = {
        "texture-format-normalized",
        "texture-format-uint"
    };
    private final String SHADERS_ROOT = "src/data/gl_320/texture";
    private final String TEXTURE_DIFFUSE = "kueken7_rgb8_unorm.dds";

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

    private enum Texture {
        RGBA8, // GL_RGBA8
        RGBA8UI, // GL_RGBA8UI
        RGBA16F, // GL_RGBA16F
        RGBA8_SNORM, // GL_RGBA8_SNORM
        MAX
    }

    private enum Program {
        NORMALIZED,
        UINT,
        MAX
    }

    private int[] textureInternalFormat = {
        GL_RGBA8,
        GL_RGBA8UI,
        GL_RGBA16F,
        GL_RGBA8_SNORM
    };

    private int[] textureFormat = {
        GL_RGB,
        GL_RGB_INTEGER,
        GL_RGB,
        GL_RGB
    };

    private int[] vertexArrayName = {0}, programName = new int[Program.MAX.ordinal()], bufferName = {0},
            textureName = new int[Texture.MAX.ordinal()], uniformMvp = new int[Program.MAX.ordinal()],
            uniformDiffuse = new int[Program.MAX.ordinal()];
    private float[] projection = new float[16], model = new float[16], mvp = new float[16];

    private Vec4i[] viewport = new Vec4i[]{
        new Vec4i(0, 0, 320, 240),
        new Vec4i(320, 0, 320, 240),
        new Vec4i(320, 240, 320, 240),
        new Vec4i(0, 240, 320, 240)
    };

    private enum Shader {
        VERT,
        FRAG_NORMALIZED,
        FRAG_UINT,
        MAX
    }

    @Override
    protected boolean begin(GL gl) {

        GL3 gl3 = (GL3) gl;

        boolean validated = true;

        if (validated) {
            validated = initTexture(gl3);
        }
        if (validated) {
            validated = initProgram(gl3);
        }
        if (validated) {
            validated = initBuffer(gl3);
        }
        if (validated) {
            validated = initVertexArray(gl3);
        }

        return validated && checkError(gl3, "begin");
    }

    private boolean initProgram(GL3 gl3) {

        boolean validated = true;

        ShaderCode[] shaderCodes = new ShaderCode[Shader.MAX.ordinal()];

        shaderCodes[Shader.VERT.ordinal()] = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                this.getClass(), SHADERS_ROOT, null, VERT_SHADERS_SOURCE, "vert", null, true);

        for (int i = 0; i < Program.MAX.ordinal(); i++) {
            shaderCodes[Shader.FRAG_NORMALIZED.ordinal() + i] = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, FRAG_SHADERS_SOURCE[i], "frag", null, true);
        }

        for (int i = 0; (i < Program.MAX.ordinal() && validated); i++) {

            ShaderProgram shaderProgram = new ShaderProgram();
            shaderProgram.add(shaderCodes[Shader.VERT.ordinal()]);
            shaderProgram.add(shaderCodes[Shader.FRAG_NORMALIZED.ordinal() + i]);

            shaderProgram.init(gl3);

            programName[i] = shaderProgram.program();

            gl3.glBindAttribLocation(programName[i], Semantic.Attr.POSITION, "position");
            gl3.glBindAttribLocation(programName[i], Semantic.Attr.TEXCOORD, "texCoord");
            gl3.glBindFragDataLocation(programName[i], Semantic.Frag.COLOR, "color");

            shaderProgram.link(gl3, System.out);

            uniformMvp[i] = gl3.glGetUniformLocation(programName[i], "mvp");
            validated = validated && (uniformMvp[i] != -1);
            uniformDiffuse[i] = gl3.glGetUniformLocation(programName[i], "diffuse");
            validated = validated && (uniformDiffuse[i] != -1);
        }

        return validated & checkError(gl3, "initProgram");
    }

    private boolean initBuffer(GL3 gl3) {

        gl3.glGenBuffers(1, bufferName, 0);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[0]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl3.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        return checkError(gl3, "initBuffer");
    }

    protected boolean initTexture(GL3 gl3) {

        try {
            jgli.Texture2d texture = new Texture2d(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE));

            gl3.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

            gl3.glGenTextures(Texture.MAX.ordinal(), textureName, 0);
            gl3.glActiveTexture(GL_TEXTURE0);

            for (int i = 0; i < Texture.MAX.ordinal(); ++i) {

                gl3.glBindTexture(GL_TEXTURE_2D, textureName[i]);
                gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
                gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
                gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
                gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);

                gl3.glTexImage2D(GL_TEXTURE_2D, 0,
                        textureInternalFormat[i],
                        texture.dimensions()[0], texture.dimensions()[1],
                        0,
                        textureFormat[i], GL_UNSIGNED_BYTE,
                        texture.data());
            }

        } catch (IOException ex) {
            Logger.getLogger(Gl_320_texture_format.class.getName()).log(Level.SEVERE, null, ex);
        }
        return checkError(gl3, "initTexture");
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

        FloatUtil.makePerspective(projection, 0, true, (float) Math.PI * 0.25f, 4.0f / 3.0f, 0.1f, 100.0f);
        FloatUtil.makeScale(model, true, 3.0f, 3.0f, 3.0f);
        FloatUtil.multMatrix(projection, view(), mvp);
        FloatUtil.multMatrix(mvp, model);

        gl3.glViewport(0, 0, windowSize.x, windowSize.y);
        gl3.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 0.5f, 0.0f, 1.0f}, 0);
        gl3.glBindVertexArray(vertexArrayName[0]);
        gl3.glActiveTexture(GL_TEXTURE0);

        {
            int viewportIndex = 0;
            gl3.glViewport(viewport[viewportIndex].x, viewport[viewportIndex].y,
                    viewport[viewportIndex].z, viewport[viewportIndex].w);

            gl3.glUseProgram(programName[Program.UINT.ordinal()]);
            gl3.glUniform1i(uniformDiffuse[Program.UINT.ordinal()], 0);
            gl3.glUniformMatrix4fv(uniformMvp[Program.UINT.ordinal()], 1, false, mvp, 0);

            gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.RGBA8UI.ordinal()]);

            gl3.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 1);
        }

        {
            int viewportIndex = 1;
            gl3.glViewport(viewport[viewportIndex].x, viewport[viewportIndex].y,
                    viewport[viewportIndex].z, viewport[viewportIndex].w);

            gl3.glUseProgram(programName[Program.NORMALIZED.ordinal()]);
            gl3.glUniform1i(uniformDiffuse[Program.NORMALIZED.ordinal()], 0);
            gl3.glUniformMatrix4fv(uniformMvp[Program.NORMALIZED.ordinal()], 1, false, mvp, 0);

            gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.RGBA16F.ordinal()]);

            gl3.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 1);
        }

        {
            int viewportIndex = 2;
            gl3.glViewport(viewport[viewportIndex].x, viewport[viewportIndex].y,
                    viewport[viewportIndex].z, viewport[viewportIndex].w);

            gl3.glUseProgram(programName[Program.NORMALIZED.ordinal()]);
            gl3.glUniform1i(uniformDiffuse[Program.NORMALIZED.ordinal()], 0);
            gl3.glUniformMatrix4fv(uniformMvp[Program.NORMALIZED.ordinal()], 1, false, mvp, 0);

            gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.RGBA8.ordinal()]);

            gl3.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 1);
        }

        {
            int viewportIndex = 3;
            gl3.glViewport(viewport[viewportIndex].x, viewport[viewportIndex].y,
                    viewport[viewportIndex].z, viewport[viewportIndex].w);

            gl3.glUseProgram(programName[Program.NORMALIZED.ordinal()]);
            gl3.glUniform1i(uniformDiffuse[Program.NORMALIZED.ordinal()], 0);
            gl3.glUniformMatrix4fv(uniformMvp[Program.NORMALIZED.ordinal()], 1, false, mvp, 0);

            gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.RGBA8_SNORM.ordinal()]);

            gl3.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 1);
        }

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;

        gl3.glDeleteBuffers(1, bufferName, 0);
        for (int i = 0; i < Program.MAX.ordinal(); ++i) {
            gl3.glDeleteProgram(programName[i]);
        }
        gl3.glDeleteTextures(Texture.MAX.ordinal(), textureName, 0);
        gl3.glDeleteVertexArrays(1, vertexArrayName, 0);

        return checkError(gl3, "end");
    }
}
