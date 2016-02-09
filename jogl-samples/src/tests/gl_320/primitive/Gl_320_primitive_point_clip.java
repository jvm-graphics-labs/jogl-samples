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
import glm.glm;
import glm.mat._4.Mat4;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import glm.vec._4.Vec4;
import java.nio.ByteBuffer;

/**
 *
 * @author GBarbieri
 */
public class Gl_320_primitive_point_clip extends Test {

    public static void main(String[] args) {
        Gl_320_primitive_point_clip gl_320_primitive_point_clip = new Gl_320_primitive_point_clip();
    }

    public Gl_320_primitive_point_clip() {
        super("gl-320-primitive-point-clip", Profile.CORE, 3, 2);
    }

    private final String SHADERS_SOURCE = "primitive-point-clip";
    private final String SHADERS_ROOT = "src/data/gl_320/primitive";

    private int[] vertexArrayName = {0}, bufferName = {0};
    private int programName, uniformMvp, uniformMv, vertexCount;

    @Override
    protected boolean begin(GL gl) {

        GL3 gl3 = (GL3) gl;
        //caps Caps(caps::CORE);

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
            gl3.glBindFragDataLocation(programName, Semantic.Frag.COLOR, "Color");

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
        // Generate a buffer object
        gl3.glGenBuffers(1, bufferName, 0);

        // Bind the buffer for use
        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[0]);

        vertexCount = 5;

        // Reserve buffer memory but don't copy the values
        gl3.glBufferData(GL_ARRAY_BUFFER, glf.Vertex_v4fc4f.SIZE * vertexCount, null, GL_STATIC_DRAW);

        ByteBuffer data = gl3.glMapBufferRange(GL_ARRAY_BUFFER,
                0, // Offset
                glf.Vertex_v4fc4f.SIZE * vertexCount, // Size,
                GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

        float[] floatArray = {
            -1.0f, 0.0f, 2.0f, 1.0f,
            +1.0f, 0.0f, 0.0f, 1.0f,
            -0.5f, 0.0f, 2.5f, 1.0f,
            +1.0f, 0.5f, 0.0f, 1.0f,
            +0.0f, 0.0f, 3.0f, 1.0f,
            +1.0f, 1.0f, 0.0f, 1.0f,
            +0.5f, 0.0f, 3.5f, 1.0f,
            +0.0f, 1.0f, 0.0f, 1.0f,
            +1.0f, 0.0f, 4.0f, 1.0f,
            +0.0f, 0.0f, 1.0f, 1.0f};

        data.asFloatBuffer().put(floatArray).rewind();

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
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 4, GL_FLOAT, false, glf.Vertex_v4fc4f.SIZE, 0);
            gl3.glVertexAttribPointer(Semantic.Attr.COLOR, 4, GL_FLOAT, false, glf.Vertex_v4fc4f.SIZE, Vec4.SIZE);
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
