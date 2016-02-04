/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_320.primitive;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import static com.jogamp.opengl.GL3.*;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import core.glm;
import dev.Mat4;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.nio.FloatBuffer;
import java.util.Random;

/**
 *
 * @author GBarbieri
 */
public class Gl_320_primitive_point extends Test {

    public static void main(String[] args) {
        Gl_320_primitive_point gl_320_primitive_point = new Gl_320_primitive_point();
    }

    public Gl_320_primitive_point() {
        super("gl-320-primitive-point", Profile.CORE, 3, 2);
    }

    private final String SHADERS_SOURCE = "primitive-point";
    private final String SHADERS_ROOT = "src/data/gl_320/primitive";

    private int vertexCount = 4096, programName, uniformMvp, uniformMv;
    private int[] vertexArrayName = {0}, bufferName = {0};

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

        gl3.glEnable(GL_DEPTH_TEST);
        gl3.glDepthFunc(GL_LESS);
        gl3.glEnable(GL_PROGRAM_POINT_SIZE);
        //glPointParameteri(GL_POINT_SPRITE_COORD_ORIGIN, GL_LOWER_LEFT);
        gl3.glPointParameteri(GL_POINT_SPRITE_COORD_ORIGIN, GL_UPPER_LEFT);

        return validated && checkError(gl3, "begin");
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
            gl3.glBindAttribLocation(programName, Semantic.Attr.COLOR, "color");
            gl3.glBindFragDataLocation(programName, Semantic.Frag.COLOR, "color");

            shaderProgram.link(gl3, System.out);
        }
        if (validated) {

            uniformMvp = gl3.glGetUniformLocation(programName, "mvp");
            uniformMv = gl3.glGetUniformLocation(programName, "mv");
        }

        return validated & checkError(gl3, "initProgram");
    }

    // Buffer update using glBufferSubData
    private boolean initBuffer(GL3 gl3) {

        Random random = new Random();
        // Generate a buffer object
        gl3.glGenBuffers(1, bufferName, 0);

        // Bind the buffer for use
        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[0]);

        // Reserve buffer memory but don't copy the values
        gl3.glBufferData(GL_ARRAY_BUFFER, vertexCount * 2 * 4 * Float.BYTES, null, GL_STATIC_DRAW);

        FloatBuffer data = gl3.glMapBufferRange(
                GL_ARRAY_BUFFER,
                0, // Offset
                vertexCount * 2 * 4 * Float.BYTES, // Size,
                GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT).asFloatBuffer();

        for (int i = 0; i < vertexCount; ++i) {
            //Data[i].Position = glm::vec4(glm::linearRand(glm::vec2(-1), glm::vec2(1)), glm::gaussRand(0.0f, 1.0f), 1);
            //Data[i].Position = glm::vec4(glm::linearRand(glm::vec2(-1), glm::vec2(1)), /*glm::gaussRand(0.0f, 1.0f)*/0, 1);
            //Data[i].Position = glm::vec4(glm::sphericalRand(1.0f), 1);
            data.put((float) random.nextGaussian()).put((float) random.nextGaussian()).put((float) random.nextGaussian());
            data.put(new float[]{1, 1, 1, 1, 1});
            //Data[i].Position = glm::vec4(glm::circularRand(1.0f), 0, 1);
            //Data[i].Position = glm::vec4(glm::diskRand(1.0f), 0, 1);
            //Data[i].Position = glm::vec4(glm::ballRand(1.0f), 1);
        }
        data.rewind();

        gl3.glUnmapBuffer(GL_ARRAY_BUFFER);

        // Unbind the buffer
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        return checkError(gl3, "initArrayBuffer");
    }

    private boolean initVertexArray(GL3 gl3) {

        gl3.glGenVertexArrays(1, vertexArrayName, 0);
        gl3.glBindVertexArray(vertexArrayName[0]);
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[0]);
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 4, GL_FLOAT, false, 2 * 4 * Float.BYTES, 0);
            gl3.glVertexAttribPointer(Semantic.Attr.COLOR, 4, GL_FLOAT, false, 2 * 4 * Float.BYTES, 4 * Float.BYTES);
            gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl3.glEnableVertexAttribArray(Semantic.Attr.COLOR);
        }
        gl3.glBindVertexArray(0);

        return checkError(gl3, "initVertexArray");
    }

    @Override
    protected boolean render(GL gl) {

        GL3 gl3 = (GL3) gl;

        Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, 4.0f / 3.0f, 0.1f, 100.0f);
        Mat4 view = viewMat4();
        Mat4 model = new Mat4(1.0f);
        Mat4 mvp = projection.mul(view).mul(model);
        Mat4 mv = view.mul(model);

        float[] depth = {1.0f};
        gl3.glViewport(0, 0, windowSize.x, windowSize.y);
        gl3.glClearBufferfv(GL_COLOR, 0, new float[]{0.0f, 0.0f, 0.0f, 0.0f}, 0);
        gl3.glClearBufferfv(GL_DEPTH, 0, depth, 0);

        gl3.glDisable(GL_SCISSOR_TEST);

        gl3.glUseProgram(programName);
        gl3.glUniformMatrix4fv(uniformMv, 1, false, mv.toFa_(), 0);
        gl3.glUniformMatrix4fv(uniformMvp, 1, false, mvp.toFa_(), 0);

        gl3.glBindVertexArray(vertexArrayName[0]);

        gl3.glDrawArraysInstanced(GL_POINTS, 0, vertexCount, 1);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;

        gl3.glDeleteBuffers(1, bufferName, 0);
        gl3.glDeleteProgram(programName);
        gl3.glDeleteVertexArrays(1, vertexArrayName, 0);

        return checkError(gl3, "end");
    }
}
