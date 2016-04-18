/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_400;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL3ES3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.BufferUtils;
import glm.glm;
import glm.mat._4.Mat4;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import glm.vec._2.Vec2;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 *
 * @author GBarbieri
 */
public class Gl_400_draw_indirect extends Test {

    public static void main(String[] args) {
        Gl_400_draw_indirect gl_400_draw_indirect = new Gl_400_draw_indirect();
    }

    public Gl_400_draw_indirect() {
        super("gl-400-draw-indirect", Profile.CORE, 4, 0);
    }

    private final String SHADERS_SOURCE = "flat-color";
    private final String SHADERS_ROOT = "src/data/gl_400";

    private int elementCount = 6;
    private int elementSize = elementCount * Integer.BYTES;
    private int[] elementData = {
        0, 1, 2,
        0, 2, 3};

    private int vertexCount = 4;
    private int positionSize = vertexCount * Vec2.SIZE;
    private float[] positionData = {
        -1.0f, -1.0f,
        +1.0f, -1.0f,
        +1.0f, +1.0f,
        -1.0f, +1.0f};

    /**
     * We don't use the class in /framework because from GL 4.2 the last field
     * can be different from zero.
     * The baseInstance​ member of the DrawElementsIndirectCommand​ structure is defined only if the GL version is 4.2
     * or greater. For versions of the GL less than 4.2, this parameter is present but is reserved and should be set
     * to zero. On earlier versions of the GL, behavior is undefined if it is non-zero.
     */
    private class DrawElementsIndirectCommand {

        public int count;
        public int primCount;
        public int firstIndex;
        public int baseVertex;
        public int reservedMustBeZero;

        public DrawElementsIndirectCommand(int count, int primCount, int firstIndex, int baseVertex, int reservedMustBeZero) {
            // Specifies the number of elements to be rendered.
            this.count = count;
            // Specifies the number of instances of the indexed geometry that should be drawn.
            this.primCount = primCount;
            /**
             * Specifies a byte offset (cast to a pointer type) into the buffer bound to GL_ELEMENT_ARRAY_BUFFER
             * to start reading indices from.
             */
            this.firstIndex = firstIndex;
            /**
             * Specifies a constant that should be added to each element of indices​ when chosing elements
             * from the enabled vertex arrays.
             */
            this.baseVertex = baseVertex;
            /**
             * Specifies the base instance for use in fetching instanced vertex attributes.
             */
            this.reservedMustBeZero = reservedMustBeZero;
        }

        public final int SIZE = 5 * Integer.BYTES;

        public int[] toIa_() {
            return new int[]{count, primCount, firstIndex, baseVertex, reservedMustBeZero};
        }
    };

    private class Buffer {

        public static final int ARRAY = 0;
        public static final int ELEMENT = 1;
        public static final int INDIRECT = 2;
        public static final int MAX = 3;
    }

    private IntBuffer vertexArrayName = GLBuffers.newDirectIntBuffer(1),
            bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX);
    private int programName, uniformMvp, uniformDiffuse;
    private DrawElementsIndirectCommand command;
    private boolean useIndirect = false;

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;

        if (validated) {
            validated = initProgram(gl4);
        }
        if (validated) {
            validated = initBuffers(gl4);
        }
        if (validated) {
            validated = initVertexArray(gl4);
        }
        return validated && checkError(gl4, "begin");
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER, this.getClass(), SHADERS_ROOT, null,
                    SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER, this.getClass(), SHADERS_ROOT, null,
                    SHADERS_SOURCE, "frag", null, true);

            shaderProgram.init(gl4);

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);

            programName = shaderProgram.program();

            shaderProgram.link(gl4, System.out);
        }

        // Get variables locations
        if (validated) {

            uniformMvp = gl4.glGetUniformLocation(programName, "mvp");
            uniformDiffuse = gl4.glGetUniformLocation(programName, "diffuse");
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffers(GL4 gl4) {

        command = new DrawElementsIndirectCommand(elementCount, 1, 0, 0, 0);
        IntBuffer commandBuffer = GLBuffers.newDirectIntBuffer(command.toIa_());
        FloatBuffer positionBuffer = GLBuffers.newDirectFloatBuffer(positionData);
        IntBuffer elementBuffer = GLBuffers.newDirectIntBuffer(elementData);

        gl4.glGenBuffers(Buffer.MAX, bufferName);

        gl4.glBindBuffer(GL_DRAW_INDIRECT_BUFFER, bufferName.get(Buffer.INDIRECT));
        gl4.glBufferData(GL_DRAW_INDIRECT_BUFFER, command.SIZE, commandBuffer, GL_STATIC_READ);
        gl4.glBindBuffer(GL_DRAW_INDIRECT_BUFFER, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.ARRAY));
        gl4.glBufferData(GL_ARRAY_BUFFER, positionSize, positionBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT));
        gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        BufferUtils.destroyDirectBuffer(positionBuffer);
        BufferUtils.destroyDirectBuffer(elementBuffer);
        BufferUtils.destroyDirectBuffer(commandBuffer);

        return checkError(gl4, "initBuffers");
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(1, vertexArrayName);
        gl4.glBindVertexArray(vertexArrayName.get(0));
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.ARRAY));
            {
                gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 0, 0);
            }
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);

            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT));
        }
        gl4.glBindVertexArray(0);

        return checkError(gl4, "initVertexArray");
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, 4.0f / 3.0f, 0.1f, 100.0f);
        Mat4 model = new Mat4(1.0f);
        Mat4 mvp = projection.mul(viewMat4()).mul(model);

        gl4.glViewport(0, 0, windowSize.x, windowSize.y);
        gl4.glClearBufferfv(GL_COLOR, 0, clearColor.put(0, 0).put(1, 0).put(2, 0).put(3, 0));

        gl4.glUseProgram(programName);
        gl4.glUniformMatrix4fv(uniformMvp, 1, false, mvp.toFa_(), 0);
        gl4.glUniform4f(uniformDiffuse, 1.0f, 0.5f, 0.0f, 1.0f);

        gl4.glBindVertexArray(vertexArrayName.get(0));
        gl4.glBindBuffer(GL_DRAW_INDIRECT_BUFFER, bufferName.get(Buffer.INDIRECT));

        if (useIndirect) {
            gl4.glDrawElementsIndirect(
                    GL_TRIANGLES,
                    GL_UNSIGNED_INT,
                    0);
        } else {
            gl4.glDrawElementsInstancedBaseVertexBaseInstance(
                    GL_TRIANGLES,
                    command.count,
                    GL_UNSIGNED_INT,
                    command.firstIndex,
                    command.primCount,
                    command.baseVertex,
                    command.reservedMustBeZero);
        }

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteBuffers(Buffer.MAX, bufferName);
        gl4.glDeleteProgram(programName);
        gl4.glDeleteVertexArrays(1, vertexArrayName);

        BufferUtils.destroyDirectBuffer(bufferName);
        BufferUtils.destroyDirectBuffer(vertexArrayName);

        return checkError(gl4, "end");
    }
}
