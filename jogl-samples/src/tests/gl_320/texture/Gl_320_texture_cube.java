/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_320.texture;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2GL3.*;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import core.glm;
import dev.Mat4;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import jgli.TextureCube;
import dev.Vec2;

/**
 *
 * @author GBarbieri
 */
public class Gl_320_texture_cube extends Test {

    public static void main(String[] args) {
        Gl_320_texture_cube gl_320_texture_cube = new Gl_320_texture_cube();
    }

    public Gl_320_texture_cube() {
        super("gl-320-texture-cube", Profile.CORE, 3, 2, new Vec2(Math.PI * 0.1f));
    }

    private final String SHADERS_SOURCE = "texture-cube";
    private final String SHADERS_ROOT = "src/data/gl_320/texture";

    // With DDS textures, v texture coordinate are reversed, from top to bottom
    private int vertexCount = 6;
    private int vertexSize = vertexCount * 2 * Float.BYTES;
    private float[] vertexData = {
        -1.0f, -1.0f,
        +1.0f, -1.0f,
        +1.0f, +1.0f,
        +1.0f, +1.0f,
        -1.0f, +1.0f,
        -1.0f, -1.0f};

    private class Shader {

        public static final int VERT = 0;
        public static final int FRAG = 1;
        public static final int MAX = 2;
    }

    private int[] shaderName = new int[Shader.MAX], vertexArrayName = {0}, bufferName = {0}, textureName = {0};
    private int programName, uniformMv, uniformMvp, uniformEnvironment, uniformCamera;

    @Override
    protected boolean begin(GL gl) {

        GL3 gl3 = (GL3) gl;

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
            gl3.glBindFragDataLocation(programName, Semantic.Frag.COLOR, "color");

            shaderProgram.link(gl3, System.out);
        }
        if (validated) {

            uniformMv = gl3.glGetUniformLocation(programName, "mv");
            uniformMvp = gl3.glGetUniformLocation(programName, "mvp");
            uniformEnvironment = gl3.glGetUniformLocation(programName, "environment");
            uniformCamera = gl3.glGetUniformLocation(programName, "camera");
        }

        return validated & checkError(gl3, "initProgram");
    }

    private boolean initBuffer(GL3 gl3) {

        gl3.glGenBuffers(1, bufferName, 0);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[0]);
        gl3.glBufferData(GL_ARRAY_BUFFER, vertexSize, GLBuffers.newDirectFloatBuffer(vertexData), GL_STATIC_DRAW);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        return true;
    }

    private boolean initTexture(GL3 gl3) {

        jgli.TextureCube texture = new TextureCube(jgli.Format.FORMAT_RGBA8_UNORM_PACK32, new int[]{2, 2}, 1);
        assert (!texture.empty());

        texture.clearFace(0, new byte[]{(byte) 255, (byte) 0, (byte) 0, (byte) 255});
        texture.clearFace(1, new byte[]{(byte) 255, (byte) 128, (byte) 0, (byte) 255});
        texture.clearFace(2, new byte[]{(byte) 255, (byte) 255, (byte) 0, (byte) 255});
        texture.clearFace(3, new byte[]{(byte) 0, (byte) 255, (byte) 0, (byte) 255});
        texture.clearFace(4, new byte[]{(byte) 0, (byte) 255, (byte) 255, (byte) 255});
        texture.clearFace(5, new byte[]{(byte) 0, (byte) 0, (byte) 255, (byte) 255});

        gl3.glActiveTexture(GL_TEXTURE0);
        gl3.glGenTextures(1, textureName, 0);
        gl3.glBindTexture(GL_TEXTURE_CUBE_MAP, textureName[0]);
        gl3.glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_BASE_LEVEL, 0);
        gl3.glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAX_LEVEL, 0);
        gl3.glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        gl3.glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        gl3.glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        gl3.glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        gl3.glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        gl3.glTexParameterfv(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_BORDER_COLOR, new float[]{0.0f, 0.0f, 0.0f, 0.0f}, 0);
        gl3.glTexParameterf(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_LOD, -1000.f);
        gl3.glTexParameterf(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAX_LOD, 1000.f);
        gl3.glTexParameterf(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_LOD_BIAS, 0.0f);
        gl3.glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_COMPARE_MODE, GL_NONE);
        gl3.glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);

        jgli.Gl.Format format = jgli.Gl.translate(texture.format());
        for (int face = 0; face < 6; ++face) {
            gl3.glTexImage2D(
                    GL_TEXTURE_CUBE_MAP_POSITIVE_X + face,
                    0,
                    format.internal.value,
                    texture.dimensions()[0], texture.dimensions()[1],
                    0,
                    format.external.value, format.type.value,
                    texture.data(0, face, 0));
        }

        return true;
    }

    private boolean initVertexArray(GL3 gl3) {

        gl3.glGenVertexArrays(1, vertexArrayName, 0);
        gl3.glBindVertexArray(vertexArrayName[0]);
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[0]);
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
            gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);
        }
        gl3.glBindVertexArray(0);

        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL3 gl3 = (GL3) gl;

        Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, 2.0f / 3.0f, 0.1f, 1000.0f);
        Mat4 view = viewMat4();
        Mat4 model = new Mat4(1.0f);
        Mat4 mvp = projection.mul(view).mul(model);
        Mat4 mv = view.mul(model);

        gl3.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, 0);

        gl3.glUseProgram(programName);
        gl3.glUniformMatrix4fv(uniformMv, 1, false, mv.toFa_(), 0);
        gl3.glUniformMatrix4fv(uniformMvp, 1, false, mvp.toFa_(), 0);
        gl3.glUniform1i(uniformEnvironment, 0);
        gl3.glUniform3fv(uniformCamera, 1, new float[]{0.0f, 0.0f, -cameraDistance()}, 0);

        gl3.glBindVertexArray(vertexArrayName[0]);

        gl3.glActiveTexture(GL_TEXTURE0);
        gl3.glBindTexture(GL_TEXTURE_CUBE_MAP, textureName[0]);

        gl3.glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS);
        gl3.glViewport(0, 0, windowSize.x >> 1, windowSize.y);

        gl3.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 1);

        gl3.glDisable(GL_TEXTURE_CUBE_MAP_SEAMLESS);
        gl3.glViewport(windowSize.x >> 1, 0, windowSize.x >> 1, windowSize.y);

        gl3.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 1);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;

        gl3.glDeleteBuffers(1, bufferName, 0);
        gl3.glDeleteProgram(programName);
        gl3.glDeleteTextures(1, textureName, 0);
        gl3.glDeleteVertexArrays(1, vertexArrayName, 0);

        return true;
    }
}
