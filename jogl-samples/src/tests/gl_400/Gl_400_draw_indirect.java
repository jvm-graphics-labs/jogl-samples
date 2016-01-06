/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_400;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNSIGNED_INT;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import static com.jogamp.opengl.GL2ES3.GL_STATIC_READ;
import static com.jogamp.opengl.GL3ES3.GL_DRAW_INDIRECT_BUFFER;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.Profile;
import framework.Semantic;
import framework.Test;
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
    private int positionSize = vertexCount * 2 * Float.BYTES;
    private float[] positionData = {
        -1.0f, -1.0f,
        +1.0f, -1.0f,
        +1.0f, +1.0f,
        -1.0f, +1.0f};

    private class DrawElementsIndirectCommand {

        public int count;
        public int primCount;
        public int firstIndex;
        public int baseVertex;
        public int reservedMustBeZero;

        public DrawElementsIndirectCommand(int count, int primCount, int firstIndex, int baseVertex, int reservedMustBeZero) {
            this.count = count;
            this.primCount = primCount;
            this.firstIndex = firstIndex;
            this.baseVertex = baseVertex;
            this.reservedMustBeZero = reservedMustBeZero;
        }

        public final int SIZEOF = 5 * Integer.BYTES;

        public int[] toIntArray() {
            return new int[]{count, primCount, firstIndex, baseVertex, reservedMustBeZero};
        }
    };

    private int[] vertexArrayName = {0}, arrayBufferName = {0}, indirectBufferName = {0}, elementBufferName = {0};
    private int programName, uniformMvp, uniformDiffuse;
    private float[] projection = new float[16], model = new float[16], mvp = new float[16];

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;

        if (validated) {
            validated = initProgram(gl4);
        }
        if (validated) {
            validated = initArrayBuffer(gl4);
        }
        if (validated) {
            validated = initIndirectBuffer(gl4);
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

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

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

    private boolean initIndirectBuffer(GL4 gl4) {

        DrawElementsIndirectCommand command = new DrawElementsIndirectCommand(elementCount, 1, 0, 0, 0);

        gl4.glGenBuffers(1, indirectBufferName, 0);
        gl4.glBindBuffer(GL_DRAW_INDIRECT_BUFFER, indirectBufferName[0]);
        IntBuffer commandBuffer = GLBuffers.newDirectIntBuffer(command.toIntArray());
        gl4.glBufferData(GL_DRAW_INDIRECT_BUFFER, command.SIZEOF, commandBuffer, GL_STATIC_READ);
        gl4.glBindBuffer(GL_DRAW_INDIRECT_BUFFER, 0);

        return checkError(gl4, "initIndirectBuffer");
    }

    private boolean initArrayBuffer(GL4 gl4) {

        gl4.glGenBuffers(1, arrayBufferName, 0);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, arrayBufferName[0]);
        FloatBuffer positionBuffer = GLBuffers.newDirectFloatBuffer(positionData);
        gl4.glBufferData(GL_ARRAY_BUFFER, positionSize, positionBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        gl4.glGenBuffers(1, elementBufferName, 0);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, elementBufferName[0]);
        IntBuffer elementBuffer = GLBuffers.newDirectIntBuffer(elementData);
        gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        return checkError(gl4, "initArrayBuffer");
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(1, vertexArrayName, 0);
        gl4.glBindVertexArray(vertexArrayName[0]);
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, arrayBufferName[0]);
            {
                gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 0, 0);
            }
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);

            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, elementBufferName[0]);
        }
        gl4.glBindVertexArray(0);

        return checkError(gl4, "initVertexArray");
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        FloatUtil.makePerspective(projection, 0, true, (float) Math.PI * 0.25f, 4.0f / 3.0f, 0.1f, 100.0f);
        FloatUtil.makeIdentity(model);
        FloatUtil.multMatrix(projection, view(), mvp);
        FloatUtil.multMatrix(mvp, model);

        gl4.glViewport(0, 0, windowSize.x, windowSize.y);
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{0.0f, 0.0f, 0.0f, 0.0f}, 0);

        gl4.glUseProgram(programName);
        gl4.glUniformMatrix4fv(uniformMvp, 1, false, mvp, 0);
        gl4.glUniform4fv(uniformDiffuse, 1, new float[]{1.0f, 0.5f, 0.0f, 1.0f}, 0);

        gl4.glBindVertexArray(vertexArrayName[0]);
        gl4.glBindBuffer(GL_DRAW_INDIRECT_BUFFER, indirectBufferName[0]);

        gl4.glDrawElementsIndirect(GL_TRIANGLES, GL_UNSIGNED_INT, 0);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteBuffers(1, arrayBufferName, 0);
        gl4.glDeleteBuffers(1, indirectBufferName, 0);
        gl4.glDeleteBuffers(1, elementBufferName, 0);
        gl4.glDeleteProgram(programName);
        gl4.glDeleteVertexArrays(1, vertexArrayName, 0);

        return checkError(gl4, "end");
    }
}
