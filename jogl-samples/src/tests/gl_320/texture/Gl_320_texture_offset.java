/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_320.texture;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2ES3.*;
import com.jogamp.opengl.GL3;
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
import glm.vec._2.Vec2;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import jgli.Texture2d;
import jglm.Vec4i;

/**
 *
 * @author GBarbieri
 */
public class Gl_320_texture_offset extends Test {

    public static void main(String[] args) {
        Gl_320_texture_offset gl_320_texture_offset = new Gl_320_texture_offset();
    }

    public Gl_320_texture_offset() {
        super("gl-320-texture-offset", Profile.CORE, 3, 2);
    }

    private final String SHADERS_VERT = "texture-offset";
    private final String[] SHADERS_FRAG = {
        "texture-offset",
        "texture-offset-bicubic",};
    private final String SHADERS_ROOT = "src/data/gl_320/texture";
    private final String TEXTURE_DIFFUSE = "kueken7_rgba8_srgb.dds";

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
        2, 3, 0
    };

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int ELEMENT = 1;
        public static final int MAX = 2;
    }

    private class Program {

        public static final int OFFSET = 0;
        public static final int BICUBIC = 1;
        public static final int MAX = 2;
    }

    private class Shader {

        public static final int VERT = 0;
        public static final int FRAG = 1;
        public static final int FRAG_BICUBIC = 2;
        public static final int MAX = 3;
    }

    private int[] vertexArrayName = {0}, textureName = {0}, bufferName = new int[Buffer.MAX],
            programName = new int[Program.MAX], uniformMvp = new int[Program.MAX], uniformDiffuse = new int[Program.MAX];
    private int uniformOffset;
    private Vec4i[] viewport = new Vec4i[Program.MAX];

    @Override
    protected boolean begin(GL gl) {

        GL3 gl3 = (GL3) gl;

        boolean validated = true;

        int border = 1;

        viewport[Program.OFFSET] = new Vec4i(border, border, windowSize.x / 2 - border * 2, windowSize.y - border * 2);
        viewport[Program.BICUBIC] = new Vec4i(border + windowSize.x / 2, border, windowSize.x / 2 - border * 2,
                windowSize.y - border * 2);

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

        ShaderCode[] shaderCodes = new ShaderCode[Shader.MAX];

        shaderCodes[Shader.VERT] = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                this.getClass(), SHADERS_ROOT, null, SHADERS_VERT, "vert", null, true);
        shaderCodes[Shader.FRAG] = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                this.getClass(), SHADERS_ROOT, null, SHADERS_FRAG[Program.OFFSET], "frag", null, true);
        shaderCodes[Shader.FRAG_BICUBIC] = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                this.getClass(), SHADERS_ROOT, null, SHADERS_FRAG[Program.BICUBIC], "frag", null, true);

        for (int i = 0; i < Program.MAX; i++) {

            ShaderProgram shaderProgram = new ShaderProgram();
            shaderProgram.add(shaderCodes[Shader.VERT]);
            shaderProgram.add(shaderCodes[Shader.FRAG + i]);

            shaderProgram.init(gl3);

            programName[i] = shaderProgram.program();

            gl3.glBindAttribLocation(programName[i], Semantic.Attr.POSITION, "position");
            gl3.glBindAttribLocation(programName[i], Semantic.Attr.TEXCOORD, "texCoord");
            gl3.glBindFragDataLocation(programName[i], Semantic.Frag.COLOR, "color");

            shaderProgram.link(gl3, System.out);

            uniformMvp[i] = gl3.glGetUniformLocation(programName[i], "mvp");
            uniformDiffuse[i] = gl3.glGetUniformLocation(programName[i], "diffuse");

            if (i == Program.OFFSET) {
                uniformOffset = gl3.glGetUniformLocation(programName[i], "offset");
            }
        }

        return validated & checkError(gl3, "initProgram");
    }

    private boolean initBuffer(GL3 gl3) {

        gl3.glGenBuffers(Buffer.MAX, bufferName, 0);

        gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT]);
        IntBuffer elementBuffer = GLBuffers.newDirectIntBuffer(elementData);
        gl3.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(elementBuffer);
        gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl3.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(vertexBuffer);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        return true;
    }

    private boolean initTexture(GL3 gl3) {

        try {
            jgli.Texture2d texture = new Texture2d(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE));

            jgli.Gl.Format format = jgli.Gl.translate(texture.format());

            gl3.glGenTextures(1, textureName, 0);
            gl3.glActiveTexture(GL_TEXTURE0);
            gl3.glBindTexture(GL_TEXTURE_2D, textureName[0]);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, texture.levels() - 1);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_NEAREST);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

            for (int level = 0; level < texture.levels(); ++level) {

                gl3.glTexImage2D(GL_TEXTURE_2D, level,
                        format.internal.value,
                        texture.dimensions(level)[0], texture.dimensions(level)[1],
                        0,
                        format.external.value, format.type.value,
                        texture.data(level));
            }

        } catch (IOException ex) {
            Logger.getLogger(Gl_320_texture_offset.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    private boolean initVertexArray(GL3 gl3) {

        gl3.glGenVertexArrays(1, vertexArrayName, 0);
        gl3.glBindVertexArray(vertexArrayName[0]);
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, Vertex_v2fv2f.SIZE, 0);
            gl3.glVertexAttribPointer(Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, Vertex_v2fv2f.SIZE, Vec2.SIZE);
            gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl3.glEnableVertexAttribArray(Semantic.Attr.TEXCOORD);

            gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT]);
        }
        gl3.glBindVertexArray(0);

        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL3 gl3 = (GL3) gl;

        Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, 2.0f / 3.0f, 0.1f, 1000.0f);
        Mat4 model = new Mat4(1.0f);
        Mat4 mvp = projection.mul(viewMat4()).mul(model);

        gl3.glViewport(0, 0, windowSize.x, windowSize.y);
        gl3.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 0.5f, 0.0f, 1.0f}, 0);

        gl3.glActiveTexture(GL_TEXTURE0);
        gl3.glBindTexture(GL_TEXTURE_2D, textureName[0]);

        gl3.glBindVertexArray(vertexArrayName[0]);

        gl3.glUseProgram(programName[Program.OFFSET]);
        gl3.glUniform2iv(uniformOffset, 1, new int[]{48, -80}, 0);

        for (int i = 0; i < Program.MAX; ++i) {
            gl3.glUseProgram(programName[i]);
            gl3.glUniformMatrix4fv(uniformMvp[i], 1, false, mvp.toFa_(), 0);
            gl3.glUniform1i(uniformDiffuse[i], 0);

            gl3.glViewport(viewport[i].x, viewport[i].y, viewport[i].z, viewport[i].w);

            gl3.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_INT, 0, 1, 0);
        }

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;

        gl3.glDeleteBuffers(Buffer.MAX, bufferName, 0);
        for (int i = 0; i < Program.MAX; ++i) {
            gl3.glDeleteProgram(programName[i]);
        }
        gl3.glDeleteTextures(1, textureName, 0);
        gl3.glDeleteVertexArrays(1, vertexArrayName, 0);

        return true;
    }
}
