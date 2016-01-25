/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_320.texture;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
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
public class Gl_320_texture_compressed_ext extends Test {

    public static void main(String[] args) {
        Gl_320_texture_compressed_ext gl_320_texture_compressed_ext = new Gl_320_texture_compressed_ext();
    }

    public Gl_320_texture_compressed_ext() {
        super("gl-320-texture-compressed-ext", Profile.CORE, 3, 2);
    }

    private final String SHADERS_SOURCE = "texture-compressed";
    private final String SHADERS_ROOT = "src/data/gl_320/texture";
    private final String TEXTURE_DIFFUSE_BC1 = "kueken7_rgb_dxt1_srgb.dds";
    private final String TEXTURE_DIFFUSE_BC3 = "kueken7_rgba_dxt5_srgb.dds";
    private final String TEXTURE_DIFFUSE_BC4 = "kueken7_r_ati1n_unorm.dds";
    private final String TEXTURE_DIFFUSE_BC5 = "kueken7_rg_ati2n_unorm.dds";

    public static class Vertex {

        public float[] position;
        public float[] texCoord;
        public static int sizeOf = 2 * 2 * Float.BYTES;

        public Vertex(float[] position, float[] texCoord) {
            this.position = position;
            this.texCoord = texCoord;
        }

        public float[] toFloatArray() {
            return new float[]{position[0], position[1], texCoord[0], texCoord[1]};
        }
    }

    // With DDS textures, v texture coordinate are reversed, from top to bottom
    private int vertexCount = 6;
    private int vertexSize = vertexCount * Vertex.sizeOf;
    private Vertex[] vertexData = {
        new Vertex(new float[]{-1.0f, -1.0f}, new float[]{0.0f, 1.0f}),
        new Vertex(new float[]{+1.0f, -1.0f}, new float[]{1.0f, 1.0f}),
        new Vertex(new float[]{+1.0f, 1.0f}, new float[]{1.0f, 0.0f}),
        new Vertex(new float[]{+1.0f, 1.0f}, new float[]{1.0f, 0.0f}),
        new Vertex(new float[]{-1.0f, 1.0f}, new float[]{0.0f, 0.0f}),
        new Vertex(new float[]{-1.0f, -1.0f}, new float[]{0.0f, 1.0f})};

    private enum Texture {
        BC1,
        BC3,
        BC4,
        BC5,
        MAX
    };

    private enum Shader {
        VERT,
        FRAG,
        MAX
    }

    private int[] shaderName = new int[Shader.MAX.ordinal()], vertexArrayName = new int[1], bufferName = new int[1],
            textureName = new int[Texture.MAX.ordinal()];
    private int programName, uniformMvp, uniformDiffuse;
    private Vec4i[] viewport = new Vec4i[Texture.MAX.ordinal()];
    private float[] projection = new float[16], model = new float[16], mvp = new float[16];

    @Override
    protected boolean begin(GL gl) {

        GL3 gl3 = (GL3) gl;

        viewport[Texture.BC1.ordinal()] = new Vec4i(0, 0, windowSize.x >> 1, windowSize.y >> 1);
        viewport[Texture.BC3.ordinal()] = new Vec4i(windowSize.x >> 1, 0, windowSize.x >> 1, windowSize.y >> 1);
        viewport[Texture.BC4.ordinal()] = new Vec4i(windowSize.x >> 1, windowSize.y >> 1,
                windowSize.x >> 1, windowSize.y >> 1);
        viewport[Texture.BC5.ordinal()] = new Vec4i(0, windowSize.y >> 1, windowSize.x >> 1, windowSize.y >> 1);

        boolean validated = true;
        validated = validated && checkExtension(gl3, "GL_EXT_texture_compression_s3tc");

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

        return validated;
    }

    private boolean initProgram(GL3 gl3) {

        boolean validated = true;

        if (validated) {

            ShaderCode vertShaderCode = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

            ShaderProgram shaderProgram = new ShaderProgram();
            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);

            shaderProgram.init(gl3);

            programName = shaderProgram.program();

            gl3.glBindAttribLocation(programName, Semantic.Attr.POSITION, "position");
            gl3.glBindAttribLocation(programName, Semantic.Attr.TEXCOORD, "texCoord");
            gl3.glBindFragDataLocation(programName, Semantic.Frag.COLOR, "color");

            shaderProgram.link(gl3, System.out);
        }
        if (validated) {

            uniformMvp = gl3.glGetUniformLocation(programName, "mvp");
            uniformDiffuse = gl3.glGetUniformLocation(programName, "diffuse");
        }

        return validated & checkError(gl3, "initProgram");
    }

    private boolean initBuffer(GL3 gl3) {

        gl3.glGenBuffers(1, bufferName, 0);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[0]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexCount * 4);
        for (Vertex vertex : vertexData) {
            vertexBuffer.put(vertex.toFloatArray());
        }
        vertexBuffer.rewind();
        gl3.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        return true;
    }

    private boolean initTexture(GL3 gl3) {

        try {
            gl3.glActiveTexture(GL_TEXTURE0);
            gl3.glGenTextures(Texture.MAX.ordinal(), textureName, 0);

            {
                jgli.Texture2d texture = new Texture2d(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE_BC1));
                assert (!texture.empty());

                gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.BC1.ordinal()]);
                gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
                gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, texture.levels() - 1);

                jgli.Gl.Format format = jgli.Gl.translate(texture.format());
                for (int level = 0; level < texture.levels(); ++level) {
                    gl3.glCompressedTexImage2D(GL_TEXTURE_2D, level,
                            format.internal.value,
                            texture.dimensions(level)[0], texture.dimensions(level)[1],
                            0,
                            texture.size(level),
                            texture.data(level));
                }
            }

            {
                jgli.Texture2d texture = new Texture2d(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE_BC3));
                assert (!texture.empty());

                gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.BC3.ordinal()]);
                gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
                gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, texture.levels() - 1);

                jgli.Gl.Format format = jgli.Gl.translate(texture.format());
                for (int level = 0; level < texture.levels(); ++level) {
                    gl3.glCompressedTexImage2D(GL_TEXTURE_2D, level,
                            format.internal.value,
                            texture.dimensions(level)[0], texture.dimensions(level)[1],
                            0,
                            texture.size(level),
                            texture.data(level));
                }
            }

            {
                jgli.Texture2d texture = new Texture2d(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE_BC4));
                assert (!texture.empty());

                gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.BC4.ordinal()]);
                gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
                gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, texture.levels() - 1);

                jgli.Gl.Format format = jgli.Gl.translate(texture.format());
                for (int level = 0; level < texture.levels(); ++level) {
                    gl3.glCompressedTexImage2D(GL_TEXTURE_2D, level,
                            format.internal.value,
                            texture.dimensions(level)[0], texture.dimensions(level)[1],
                            0,
                            texture.size(level),
                            texture.data(level));
                }
            }

            {
                jgli.Texture2d texture = new Texture2d(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE_BC5));
                assert (!texture.empty());

                gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.BC5.ordinal()]);
                gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
                gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, texture.levels() - 1);

                jgli.Gl.Format format = jgli.Gl.translate(texture.format());
                for (int level = 0; level < texture.levels(); ++level) {
                    gl3.glCompressedTexImage2D(GL_TEXTURE_2D, level,
                            format.internal.value,
                            texture.dimensions(level)[0], texture.dimensions(level)[1],
                            0,
                            texture.size(level),
                            texture.data(level));
                }
            }

            gl3.glActiveTexture(GL_TEXTURE0);
            gl3.glBindTexture(GL_TEXTURE_2D, 0);

        } catch (IOException ex) {
            Logger.getLogger(Gl_320_texture_compressed_ext.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    private boolean initVertexArray(GL3 gl3) {

        gl3.glGenVertexArrays(1, vertexArrayName, 0);
        gl3.glBindVertexArray(vertexArrayName[0]);
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[0]);
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, Vertex.sizeOf, 0);
            gl3.glVertexAttribPointer(Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, Vertex.sizeOf, 2 * Float.BYTES);
            gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl3.glEnableVertexAttribArray(Semantic.Attr.TEXCOORD);
        }
        gl3.glBindVertexArray(0);

        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL3 gl3 = (GL3) gl;

        FloatUtil.makePerspective(projection, 0, true, (float) Math.PI * 0.25f, 4.0f / 3.0f, 0.1f, 1000.0f);
        FloatUtil.makeIdentity(model);
        FloatUtil.multMatrix(projection, view(), mvp);
        FloatUtil.multMatrix(mvp, model);

        gl3.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 0.5f, 0.0f, 1.0f}, 0);

        // Bind the program for use
        gl3.glUseProgram(programName);
        gl3.glUniformMatrix4fv(uniformMvp, 1, false, mvp, 0);
        gl3.glUniform1i(uniformDiffuse, 0);

        gl3.glBindVertexArray(vertexArrayName[0]);

        gl3.glActiveTexture(GL_TEXTURE0);
        for (int index = 0; index < Texture.MAX.ordinal(); ++index) {
            gl3.glViewport(viewport[index].x, viewport[index].y, viewport[index].z, viewport[index].w);
            gl3.glBindTexture(GL_TEXTURE_2D, textureName[index]);
            gl3.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 1);
        }

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;

        gl3.glDeleteBuffers(1, bufferName, 0);
        gl3.glDeleteProgram(programName);
        gl3.glDeleteTextures(Texture.MAX.ordinal(), textureName, 0);
        gl3.glDeleteVertexArrays(1, vertexArrayName, 0);

        return true;
    }
}
