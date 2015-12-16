/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl320.primitive;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_LESS;
import static com.jogamp.opengl.GL.GL_MAP_INVALIDATE_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_MAP_WRITE_BIT;
import static com.jogamp.opengl.GL.GL_POINTS;
import static com.jogamp.opengl.GL.GL_SCISSOR_TEST;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import static com.jogamp.opengl.GL2ES3.GL_DEPTH;
import static com.jogamp.opengl.GL2GL3.GL_POINT_SPRITE_COORD_ORIGIN;
import static com.jogamp.opengl.GL2GL3.GL_UPPER_LEFT;
import com.jogamp.opengl.GL3;
import static com.jogamp.opengl.GL3.GL_PROGRAM_POINT_SIZE;
import static com.jogamp.opengl.GL3ES3.GL_GEOMETRY_SHADER;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.nio.ByteBuffer;
import jglm.Vec3;

/**
 *
 * @author GBarbieri
 */
public class Gl_320_primitive_point_quad extends Test {

    public static void main(String[] args) {
        Gl_320_primitive_point_quad gl_320_primitive_point_quad = new Gl_320_primitive_point_quad();
    }

    public Gl_320_primitive_point_quad() {
        super("gl-320-primitive-point-quad", Profile.CORE, 3, 2);
    }

    private final String SHADERS_SOURCE = "primitive-point-quad";
    private final String SHADERS_ROOT = "src/data/gl_320/primitive";

    private int vertexCount, programName, uniformMvp, uniformMv, uniformCameraPosition;
    private int[] vertexArrayName = new int[1], bufferName = new int[1];
    private float[] projection = new float[16], view = new float[16], model = new float[16],
            mvp = new float[16], mv = new float[16];

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
            ShaderCode geomShaderCode = ShaderCode.create(gl3, GL_GEOMETRY_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "geom", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

            ShaderProgram shaderProgram = new ShaderProgram();
            shaderProgram.add(vertShaderCode);
            shaderProgram.add(geomShaderCode);
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
            uniformCameraPosition = gl3.glGetUniformLocation(programName, "cameraPosition");
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
        gl3.glBufferData(GL_ARRAY_BUFFER, 2 * 4 * Float.BYTES * vertexCount, null, GL_STATIC_DRAW);

        ByteBuffer data = gl3.glMapBufferRange(GL_ARRAY_BUFFER,
                0, // Offset
                2 * 4 * Float.BYTES * vertexCount, // Size,
                GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

        float[] floatArray = {
            0.0f, 0.0f, -0.5f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            0.2f, 0.0f, 0.5f, 1.0f,
            1.0f, 0.5f, 0.0f, 1.0f,
            0.4f, 0.0f, 1.5f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,
            0.6f, 0.0f, 2.5f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.8f, 0.0f, 3.5f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f};

        for (float f : floatArray) {
            data.putFloat(f);
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
        FloatUtil.makePerspective(projection, 0, true, (float) Math.PI * 0.25f, 4.0f / 3.0f, 0.1f, 100.0f);
        view = view();
        FloatUtil.makeIdentity(model);
        FloatUtil.multMatrix(projection, view, mvp);
        FloatUtil.multMatrix(mvp, model);
        FloatUtil.multMatrix(view, model, mv);

        float[] depth = {1.0f};
        gl3.glViewport(0, 0, windowSize.x, windowSize.y);
        gl3.glClearBufferfv(GL_COLOR, 0, new float[]{0.0f, 0.0f, 0.0f, 0.0f}, 0);
        gl3.glClearBufferfv(GL_DEPTH, 0, depth, 0);

        gl3.glDisable(GL_SCISSOR_TEST);
        Vec3 cameraPosition = cameraPosition().negated();
        //glm::vec3 CameraPosition(glm::vec4(glm::normalize(glm::vec3(1.0)), 1.0) * this->view());

        gl3.glUseProgram(programName);
        gl3.glUniform3fv(uniformCameraPosition, 1, cameraPosition.toFloatArray(), 0);
        gl3.glUniformMatrix4fv(uniformMv, 1, false, mv, 0);
        gl3.glUniformMatrix4fv(uniformMvp, 1, false, mvp, 0);

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
