/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_330;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_ALPHA;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_COMPRESSED_RGBA_S3TC_DXT5_EXT;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_ONE;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_ZERO;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_RED;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES3.GL_BLUE;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import static com.jogamp.opengl.GL2ES3.GL_GREEN;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_SWIZZLE_A;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_SWIZZLE_B;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_SWIZZLE_G;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_SWIZZLE_R;
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
public class Gl_330_texture_swizzle extends Test {

    public static void main(String[] args) {
        Gl_330_texture_swizzle gl_330_texture_swizzle = new Gl_330_texture_swizzle();
    }

    public Gl_330_texture_swizzle() {
        super("gl-330-texture-swizzle", Profile.CORE, 3, 3);
    }

    private final String SHADERS_SOURCE = "texture-2d";
    private final String SHADERS_ROOT = "src/data/gl_330";
    private final String TEXTURE_DIFFUSE = "kueken7_rgba_dxt5_unorm.dds";

    public static class Vertex {

        public float[] position;
        public float[] texCoord;
        public static final int SIZEOF = 2 * 2 * Float.BYTES;

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
    private int vertexSize = vertexCount * Vertex.SIZEOF;
    private Vertex[] vertexData = {
        new Vertex(new float[]{-1.0f, -1.0f}, new float[]{0.0f, 1.0f}),
        new Vertex(new float[]{+1.0f, -1.0f}, new float[]{1.0f, 1.0f}),
        new Vertex(new float[]{+1.0f, +1.0f}, new float[]{1.0f, 0.0f}),
        new Vertex(new float[]{+1.0f, +1.0f}, new float[]{1.0f, 0.0f}),
        new Vertex(new float[]{-1.0f, +1.0f}, new float[]{0.0f, 0.0f}),
        new Vertex(new float[]{-1.0f, -1.0f}, new float[]{0.0f, 1.0f})};

    private enum Viewport {
        V00,
        V10,
        V11,
        V01,
        MAX
    };

    private int[] vertexArrayName = {0}, bufferName = {0}, texture2dName = {0},
            swizzleR = new int[Viewport.MAX.ordinal()], swizzleG = new int[Viewport.MAX.ordinal()],
            swizzleB = new int[Viewport.MAX.ordinal()], swizzleA = new int[Viewport.MAX.ordinal()];
    private int programName, uniformMvp, uniformDiffuse;
    private Vec4i[] viewport = new Vec4i[Viewport.MAX.ordinal()];
    private float[] projection = new float[16], model = new float[16], mvp = new float[16];

    @Override
    protected boolean begin(GL gl) {

        GL3 gl3 = (GL3) gl;

        viewport[Viewport.V00.ordinal()] = new Vec4i(0, 0, windowSize.x >> 1, windowSize.y >> 1);
        viewport[Viewport.V10.ordinal()] = new Vec4i(windowSize.x >> 1, 0, windowSize.x >> 1, windowSize.y >> 1);
        viewport[Viewport.V11.ordinal()] = new Vec4i(windowSize.x >> 1, windowSize.y >> 1,
                windowSize.x >> 1, windowSize.y >> 1);
        viewport[Viewport.V01.ordinal()] = new Vec4i(0, windowSize.y >> 1, windowSize.x >> 1, windowSize.y >> 1);

        boolean validated = true;

        if (validated) {
            validated = initProgram(gl3);
        }
        if (validated) {
            validated = initBuffer(gl3);
        }
        if (validated) {
            validated = initTexture(gl3);
        }
        if (validated) {
            validated = initVertexArray(gl3);
        }

        return validated && checkError(gl3, "begin");
    }

    private boolean initProgram(GL3 gl3) {

        boolean validated = true;

        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            ShaderCode vertShaderCode = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

            shaderProgram.init(gl3);

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);

            programName = shaderProgram.program();

            shaderProgram.link(gl3, System.out);
        }

        // Get variables locations
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

        return checkError(gl3, "initBuffer");
    }

    private boolean initTexture(GL3 gl3) {

        try {
            gl3.glGenTextures(1, texture2dName, 0);

            gl3.glActiveTexture(GL_TEXTURE0);
            gl3.glBindTexture(GL_TEXTURE_2D, texture2dName[0]);

            jgli.Texture2d texture = new Texture2d(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE));
            for (int level = 0; level < texture.levels(); ++level) {
                gl3.glCompressedTexImage2D(
                        GL_TEXTURE_2D,
                        level,
                        GL_COMPRESSED_RGBA_S3TC_DXT5_EXT,
                        texture.dimensions(level)[0],
                        texture.dimensions(level)[1],
                        0,
                        texture.size(level),
                        texture.data(level));
            }

            swizzleR[Viewport.V00.ordinal()] = GL_RED;
            swizzleG[Viewport.V00.ordinal()] = GL_GREEN;
            swizzleB[Viewport.V00.ordinal()] = GL_BLUE;
            swizzleA[Viewport.V00.ordinal()] = GL_ALPHA;

            swizzleR[Viewport.V10.ordinal()] = GL_BLUE;
            swizzleG[Viewport.V10.ordinal()] = GL_GREEN;
            swizzleB[Viewport.V10.ordinal()] = GL_RED;
            swizzleA[Viewport.V10.ordinal()] = GL_ALPHA;

            swizzleR[Viewport.V11.ordinal()] = GL_ONE;
            swizzleG[Viewport.V11.ordinal()] = GL_GREEN;
            swizzleB[Viewport.V11.ordinal()] = GL_BLUE;
            swizzleA[Viewport.V11.ordinal()] = GL_ALPHA;

            swizzleR[Viewport.V01.ordinal()] = GL_ZERO;
            swizzleG[Viewport.V01.ordinal()] = GL_GREEN;
            swizzleB[Viewport.V01.ordinal()] = GL_BLUE;
            swizzleA[Viewport.V01.ordinal()] = GL_ALPHA;

            gl3.glActiveTexture(GL_TEXTURE0);
            gl3.glBindTexture(GL_TEXTURE_2D, 0);

        } catch (IOException ex) {
            Logger.getLogger(Gl_330_texture_swizzle.class.getName()).log(Level.SEVERE, null, ex);
        }
        return checkError(gl3, "initTexture");
    }

    private boolean initVertexArray(GL3 gl3) {

        gl3.glGenVertexArrays(1, vertexArrayName, 0);
        gl3.glBindVertexArray(vertexArrayName[0]);
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[0]);
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, Vertex.SIZEOF, 0);
            gl3.glVertexAttribPointer(Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, Vertex.SIZEOF, 2 * Float.BYTES);
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

        FloatUtil.makePerspective(projection, 0, true, (float) Math.PI * 0.25f, (float) windowSize.x / windowSize.y, 0.1f, 1000.0f);
        FloatUtil.makeIdentity(model);
        FloatUtil.multMatrix(projection, view(), mvp);
        FloatUtil.multMatrix(mvp, model);

        gl3.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 0.5f, 0.0f, 1.0f}, 0);

        gl3.glUseProgram(programName);
        gl3.glUniformMatrix4fv(uniformMvp, 1, false, mvp, 0);
        gl3.glUniform1i(uniformDiffuse, 0);

        gl3.glActiveTexture(GL_TEXTURE0);
        gl3.glBindTexture(GL_TEXTURE_2D, texture2dName[0]);

        gl3.glBindVertexArray(vertexArrayName[0]);

        for (int index = 0; index < Viewport.MAX.ordinal(); ++index) {
            gl3.glViewport(viewport[index].x, viewport[index].y, viewport[index].z, viewport[index].w);

            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_R, swizzleR[index]);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_G, swizzleG[index]);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_B, swizzleB[index]);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_A, swizzleA[index]);

            gl3.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 1);
        }

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;

        gl3.glDeleteBuffers(1, bufferName, 0);
        gl3.glDeleteProgram(programName);
        gl3.glDeleteTextures(1, texture2dName, 0);
        gl3.glDeleteVertexArrays(1, vertexArrayName, 0);

        return checkError(gl3, "end");
    }
}
