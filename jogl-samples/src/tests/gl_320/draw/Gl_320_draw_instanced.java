/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_320.draw;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2ES3.*;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import glm.glm;
import glm.mat._4.Mat4;
import glm.vec._3.Vec3;
import framework.BufferUtils;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import glm.vec._2.Vec2;
import glm.vec._4.Vec4;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 *
 * @author GBarbieri
 */
public class Gl_320_draw_instanced extends Test {

    public static void main(String[] args) {
        Gl_320_draw_instanced gl_320_draw_instanced = new Gl_320_draw_instanced();
    }

    public Gl_320_draw_instanced() {
        super("gl-320-draw-instanced", Profile.CORE, 3, 2);
    }

    private final String SHADERS_SOURCE = "draw-instanced";
    private final String SHADERS_ROOT = "src/data/gl_320/draw";

    private int vertexCount = 6;
    private int positionSize = vertexCount * Vec2.SIZE;
    private float[] positionData = new float[]{
        -1.0f, -1.0f,
        +1.0f, -1.0f,
        +1.0f, +1.0f,
        +1.0f, +1.0f,
        -1.0f, +1.0f,
        -1.0f, -1.0f
    };

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int TRANSFORM = 1;
        public static final int MATERIAL = 2;
        public static final int MAX = 3;
    }

    private int[] bufferName = new int[Buffer.MAX], vertexArrayName = {0};
    private int programName, uniformTransform, uniformMaterial;

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

        return validated & checkError(gl4, "begin");
    }

    private boolean initTest(GL4 gl4) {

        boolean validated = true;

        gl4.glEnable(GL_DEPTH_TEST);

        return validated & checkError(gl4, "initTest");
    }

    private boolean initProgram(GL3 gl3) {

        boolean validated = true;
        // Create program
        if (validated) {
            ShaderCode vertShader = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragShader = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

            ShaderProgram program = new ShaderProgram();
            program.add(vertShader);
            program.add(fragShader);

            program.init(gl3);

            programName = program.program();

            gl3.glBindAttribLocation(programName, Semantic.Attr.POSITION, "position");
            gl3.glBindFragDataLocation(programName, Semantic.Frag.COLOR, "color");

            program.link(gl3, System.out);
        }
        // Get variables locations
        if (validated) {

            uniformTransform = gl3.glGetUniformBlockIndex(programName, "Transform");
            uniformMaterial = gl3.glGetUniformBlockIndex(programName, "Material");
        }

        return validated & checkError(gl3, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        gl4.glGenBuffers(Buffer.MAX, bufferName, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
        FloatBuffer positionBuffer = GLBuffers.newDirectFloatBuffer(positionData);
        gl4.glBufferData(GL_ARRAY_BUFFER, positionSize, positionBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(positionBuffer);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        int[] uniformBufferOffset = new int[]{0};
        gl4.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset, 0);

        int uniformTransformBlockSize = Math.max(2 * Mat4.SIZE, uniformBufferOffset[0]);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
        gl4.glBufferData(GL_UNIFORM_BUFFER, uniformTransformBlockSize, null, GL_DYNAMIC_DRAW);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        int uniformMaterialBlockSize = Math.max(2 * Vec4.SIZE, uniformBufferOffset[0]);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.MATERIAL]);
        gl4.glBufferData(GL_UNIFORM_BUFFER, uniformMaterialBlockSize, null, GL_STATIC_DRAW);

        ByteBuffer pointer = gl4.glMapBufferRange(GL_UNIFORM_BUFFER, 0, 2 * Vec4.SIZE,
                GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);
        pointer.asFloatBuffer().put(new float[]{1.0f, 0.5f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f, 1.0f}).rewind();

        gl4.glUnmapBuffer(GL_UNIFORM_BUFFER);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return checkError(gl4, "initBuffer");
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(1, vertexArrayName, 0);
        gl4.glBindVertexArray(vertexArrayName[0]);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
        gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 0, 0);

        gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
        gl4.glBindVertexArray(0);

        return checkError(gl4, "initVertexArray");
    }

    @Override
    protected boolean render(GL gl) {
        GL4 gl4 = (GL4) gl;
        {
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
            ByteBuffer pointer = gl4.glMapBufferRange(
                    GL_UNIFORM_BUFFER, 0, 2 * Mat4.SIZE,
                    GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, (float) windowSize.x / windowSize.y, 0.1f, 100.0f);
            Mat4 modelA = new Mat4(1.0f).translate(new Vec3(-1.1f, 0.0f, 0.0f));
            Mat4 modelB = new Mat4(1.0f).translate(new Vec3(1.1f, 0.0f, 0.0f));

            pointer.asFloatBuffer().put(projection.mul_(viewMat4()).mul(modelA).toFa_());
            pointer.position(Mat4.SIZE);
            pointer.asFloatBuffer().put(projection.mul(viewMat4()).mul(modelB).toFa_());
            pointer.rewind();

            // Make sure the uniform buffer is uploaded
            gl4.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        gl4.glViewport(0, 0, windowSize.x, windowSize.y);

        float[] depth = new float[]{1.0f};
        gl4.glClearBufferfv(GL_DEPTH, 0, depth, 0);
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, 0);

        gl4.glUseProgram(programName);
        gl4.glUniformBlockBinding(programName, uniformTransform, Semantic.Uniform.TRANSFORM0);
        gl4.glUniformBlockBinding(programName, uniformMaterial, Semantic.Uniform.MATERIAL);

        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.TRANSFORM]);
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.MATERIAL, bufferName[Buffer.MATERIAL]);
        gl4.glBindVertexArray(vertexArrayName[0]);

        gl4.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 2);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;
        gl3.glDeleteBuffers(Buffer.MAX, bufferName, 0);
        gl3.glDeleteProgram(programName);
        gl3.glDeleteVertexArrays(1, vertexArrayName, 0);

        return true;
    }
}
