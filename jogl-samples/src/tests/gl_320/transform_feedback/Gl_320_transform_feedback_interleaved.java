/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_320.transform_feedback;

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
import glf.Vertex_v4fc4f;
import glm.vec._4.Vec4;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 *
 * @author GBarbieri
 */
public class Gl_320_transform_feedback_interleaved extends Test {

    public static void main(String[] args) {
        Gl_320_transform_feedback_interleaved gl_320_transform_feedback_interleaved
                = new Gl_320_transform_feedback_interleaved();
    }

    public Gl_320_transform_feedback_interleaved() {
        super("gl-320-transform-feedback-interleaved", Profile.CORE, 3, 2);
    }

    private final String SHADERS_SOURCE_TRANSFORM = "transform-feedback-transform";
    private final String SHADERS_SOURCE_FEEDBACK = "transform-feedback-feedback";
    private final String SHADERS_ROOT = "src/data/gl_320/transform_feedback";

    private int vertexCount = 6;
    private int positionSize = vertexCount * Vec4.SIZE;
    private float[] positionData = {
        -1.0f, -1.0f, 0.0f, 1.0f,
        +1.0f, -1.0f, 0.0f, 1.0f,
        +1.0f, +1.0f, 0.0f, 1.0f,
        +1.0f, +1.0f, 0.0f, 1.0f,
        -1.0f, +1.0f, 0.0f, 1.0f,
        -1.0f, -1.0f, 0.0f, 1.0f};

    private class Program {

        public static final int TRANSFORM = 0;
        public static final int FEEDBACK = 1;
        public static final int MAX = 2;
    }

    private class Shader {

        public static final int VERT_TRANSFORM = 0;
        public static final int VERT_FEEDBACK = 1;
        public static final int FRAG_FEEDBACK = 2;
        public static final int MAX = 3;
    }

    private class feedbackOutput {

        public static final int POSITION = 0;
    }

    private int[] programName = new int[Program.MAX];
    private IntBuffer vertexArrayName = GLBuffers.newDirectIntBuffer(Program.MAX),
            bufferName = GLBuffers.newDirectIntBuffer(Program.MAX), queryName = GLBuffers.newDirectIntBuffer(1);
    private int transformUniformMvp;

    @Override
    protected boolean begin(GL gl) {

        GL3 gl3 = (GL3) gl;

        boolean validated = true;

        if (validated) {
            validated = initQuery(gl3);
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

        ShaderCode[] shaderCodes = new ShaderCode[Shader.MAX];

        shaderCodes[Shader.VERT_TRANSFORM] = ShaderCode.create(gl3, GL_VERTEX_SHADER, this.getClass(), SHADERS_ROOT,
                null, SHADERS_SOURCE_TRANSFORM, "vert", null, true);
        shaderCodes[Shader.VERT_FEEDBACK] = ShaderCode.create(gl3, GL_VERTEX_SHADER, this.getClass(), SHADERS_ROOT,
                null, SHADERS_SOURCE_FEEDBACK, "vert", null, true);
        shaderCodes[Shader.FRAG_FEEDBACK] = ShaderCode.create(gl3, GL_FRAGMENT_SHADER, this.getClass(), SHADERS_ROOT,
                null, SHADERS_SOURCE_FEEDBACK, "frag", null, true);

        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            shaderProgram.add(shaderCodes[Shader.VERT_TRANSFORM]);

            shaderProgram.init(gl3);

            programName[Program.TRANSFORM] = shaderProgram.program();

            gl3.glBindAttribLocation(programName[Program.TRANSFORM], Semantic.Attr.POSITION, "position");

            String[] strings = {"gl_Position", "Block.color"};
            gl3.glTransformFeedbackVaryings(programName[Program.TRANSFORM], 2, strings, GL_INTERLEAVED_ATTRIBS);

            shaderProgram.link(gl3, System.out);

            ByteBuffer name = GLBuffers.newDirectByteBuffer(64);
            IntBuffer length = GLBuffers.newDirectIntBuffer(1);
            IntBuffer size = GLBuffers.newDirectIntBuffer(1);
            IntBuffer type = GLBuffers.newDirectIntBuffer(1);

            gl3.glGetTransformFeedbackVarying(
                    programName[Program.TRANSFORM],
                    0,
                    name.capacity(),
                    length,
                    size,
                    type,
                    name);

            validated = validated && (size.get(0) == 1) && (type.get(0) == GL_FLOAT_VEC4);

            BufferUtils.destroyDirectBuffer(name);
            BufferUtils.destroyDirectBuffer(length);
            BufferUtils.destroyDirectBuffer(size);
            BufferUtils.destroyDirectBuffer(type);
        }
        // Get variables locations
        if (validated) {

            transformUniformMvp = gl3.glGetUniformLocation(programName[Program.TRANSFORM], "mvp");

            IntBuffer activeUniforms = GLBuffers.newDirectIntBuffer(1);
            gl3.glGetProgramiv(programName[Program.TRANSFORM], GL_ACTIVE_UNIFORMS, activeUniforms);

            ByteBuffer name = GLBuffers.newDirectByteBuffer(64);
            IntBuffer length = GLBuffers.newDirectIntBuffer(1);
            IntBuffer size = GLBuffers.newDirectIntBuffer(1);
            IntBuffer type = GLBuffers.newDirectIntBuffer(1);

            for (int i = 0; i < activeUniforms.get(0); i++) {

                gl3.glGetActiveUniform(
                        programName[Program.TRANSFORM],
                        i,
                        name.capacity(),
                        length,
                        size,
                        type,
                        name);

                byte[] bytes = new byte[name.capacity()];
                name.get(bytes);
                int location = gl3.glGetUniformLocation(programName[Program.TRANSFORM], new String(bytes));

                if (transformUniformMvp == location) {

                    validated = validated && (size.get(0) == 1) && (type.get(0) == GL_FLOAT_VEC4);
                    validated = validated && (transformUniformMvp > 0);
                }
            }
            BufferUtils.destroyDirectBuffer(name);
            BufferUtils.destroyDirectBuffer(length);
            BufferUtils.destroyDirectBuffer(size);
            BufferUtils.destroyDirectBuffer(type);
        }

        // Create program
        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            shaderProgram.add(shaderCodes[Shader.VERT_FEEDBACK]);
            shaderProgram.add(shaderCodes[Shader.FRAG_FEEDBACK]);

            shaderProgram.init(gl3);

            programName[Program.FEEDBACK] = shaderProgram.program();

            gl3.glBindAttribLocation(programName[Program.FEEDBACK], Semantic.Attr.POSITION, "position");
            gl3.glBindAttribLocation(programName[Program.FEEDBACK], Semantic.Attr.COLOR, "color");
            gl3.glBindFragDataLocation(programName[Program.FEEDBACK], Semantic.Frag.COLOR, "color");

            shaderProgram.link(gl3, System.out);
        }

        return validated & checkError(gl3, "initProgram");
    }

    private boolean initVertexArray(GL3 gl3) {

        gl3.glGenVertexArrays(Program.MAX, vertexArrayName);

        // Build a vertex array object
        gl3.glBindVertexArray(vertexArrayName.get(Program.TRANSFORM));
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Program.TRANSFORM));
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 4, GL_FLOAT, false, 0, 0);
            gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);
        }
        gl3.glBindVertexArray(0);

        // Build a vertex array object
        gl3.glBindVertexArray(vertexArrayName.get(Program.FEEDBACK));
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Program.FEEDBACK));
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 4, GL_FLOAT, false, Vertex_v4fc4f.SIZE, 0);
            gl3.glVertexAttribPointer(Semantic.Attr.COLOR, 4, GL_FLOAT, false, Vertex_v4fc4f.SIZE, Vec4.SIZE);
            gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl3.glEnableVertexAttribArray(Semantic.Attr.COLOR);
        }
        gl3.glBindVertexArray(0);

        return checkError(gl3, "initVertexArray");
    }

    private boolean initBuffer(GL3 gl3) {

        FloatBuffer positionBuffer = GLBuffers.newDirectFloatBuffer(positionData);

        // Generate a buffer object
        gl3.glGenBuffers(Program.MAX, bufferName);

        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Program.TRANSFORM));
        gl3.glBufferData(GL_ARRAY_BUFFER, positionSize, positionBuffer, GL_STATIC_DRAW);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Program.FEEDBACK));
        gl3.glBufferData(GL_ARRAY_BUFFER, Vertex_v4fc4f.SIZE * vertexCount, null, GL_STATIC_COPY);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        BufferUtils.destroyDirectBuffer(positionBuffer);

        return checkError(gl3, "initBuffer");
    }

    private boolean initQuery(GL3 gl3) {

        gl3.glGenQueries(1, queryName);

        IntBuffer queryBits = GLBuffers.newDirectIntBuffer(1);
        gl3.glGetQueryiv(GL_TRANSFORM_FEEDBACK_PRIMITIVES_WRITTEN, GL_QUERY_COUNTER_BITS, queryBits);

        boolean validated = queryBits.get(0) >= 1;

        BufferUtils.destroyDirectBuffer(queryBits);

        return validated && checkError(gl3, "initQuery");
    }

    @Override
    protected boolean render(GL gl) {

        GL3 gl3 = (GL3) gl;

        Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, 4.0f / 3.0f, 0.1f, 100.0f);
        Mat4 model = new Mat4(1.0f);
        Mat4 mvp = projection.mul(viewMat4()).mul(model);

        // Set the display viewport
        gl3.glViewport(0, 0, windowSize.x, windowSize.y);

        // Clear color buffer
        clearColor.put(new float[]{0.0f, 0.0f, 0.0f, 1.0f}).rewind();
        gl3.glClearBufferfv(GL_COLOR, 0, clearColor);

        // First draw, capture the attributes
        // Disable rasterisation, vertices processing only!
        gl3.glEnable(GL_RASTERIZER_DISCARD);

        gl3.glUseProgram(programName[Program.TRANSFORM]);
        gl3.glUniformMatrix4fv(transformUniformMvp, 1, false, mvp.toFa_(), 0);

        gl3.glBindBufferBase(GL_TRANSFORM_FEEDBACK_BUFFER, feedbackOutput.POSITION, bufferName.get(Program.FEEDBACK));
        gl3.glBindVertexArray(vertexArrayName.get(Program.TRANSFORM));

        gl3.glBeginQuery(GL_TRANSFORM_FEEDBACK_PRIMITIVES_WRITTEN, queryName.get(0));
        gl3.glBeginTransformFeedback(GL_TRIANGLES);
        {
            gl3.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 1);
        }
        gl3.glEndTransformFeedback();
        gl3.glEndQuery(GL_TRANSFORM_FEEDBACK_PRIMITIVES_WRITTEN);

        gl3.glDisable(GL_RASTERIZER_DISCARD);

        // Second draw, reuse the captured attributes
        gl3.glUseProgram(programName[Program.FEEDBACK]);

        IntBuffer primitivesWritten = GLBuffers.newDirectIntBuffer(1);
        gl3.glGetQueryObjectuiv(queryName.get(0), GL_QUERY_RESULT, primitivesWritten);

        gl3.glBindVertexArray(vertexArrayName.get(Program.FEEDBACK));
        gl3.glDrawArraysInstanced(GL_TRIANGLES, 0, primitivesWritten.get(0) * 3, 1);

        BufferUtils.destroyDirectBuffer(primitivesWritten);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;

        for (int i = 0; i < Program.MAX; ++i) {
            gl3.glDeleteProgram(programName[i]);
        }
        gl3.glDeleteVertexArrays(Program.MAX, vertexArrayName);
        gl3.glDeleteBuffers(Program.MAX, bufferName);
        gl3.glDeleteQueries(1, queryName);

        BufferUtils.destroyDirectBuffer(vertexArrayName);
        BufferUtils.destroyDirectBuffer(bufferName);
        BufferUtils.destroyDirectBuffer(queryName);

        return checkError(gl3, "end");
    }
}
