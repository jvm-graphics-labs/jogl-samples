/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_500;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_TRUE;
import static com.jogamp.opengl.GL2.GL_SM_COUNT_NV;
import static com.jogamp.opengl.GL2.GL_WARPS_PER_SM_NV;
import static com.jogamp.opengl.GL2.GL_WARP_SIZE_NV;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER_BIT;
import static com.jogamp.opengl.GL2ES2.GL_PROGRAM_SEPARABLE;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER_BIT;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.BufferUtils;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import glm.glm;
import glm.mat._4.Mat4;
import glm.vec._2.Vec2;
import glm.vec._2.i.Vec2i;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 *
 * @author GBarbieri
 */
public class Gl_500_shader_invocation_nv extends Test {

    public static void main(String[] args) {
        Gl_500_shader_invocation_nv gl_500_shader_invocation_nv = new Gl_500_shader_invocation_nv();
    }

    public Gl_500_shader_invocation_nv() {
        super("gl-500-shader-invocation-nv", Profile.CORE, 4, 5, new Vec2i(1280, 720));
    }

    private final String SHADERS_SOURCE = "shader-invocation";
    private final String SHADERS_ROOT = "src/data/gl_500";

    private class Buffer {

        public static final int CONSTANT = 0;
        public static final int TRANSFORM = 1;
        public static final int VERTEX = 2;
        public static final int MAX = 3;
    }

    private IntBuffer pipelineName = GLBuffers.newDirectIntBuffer(1), vertexArrayName = GLBuffers.newDirectIntBuffer(1),
            bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX);
    private int programName;
    private FloatBuffer white = GLBuffers.newDirectFloatBuffer(new float[]{1.0f, 1.0f, 1.0f, 1.0f});
    private int quadOverlapCount = 1, vertexCount;
    /**
     * https://jogamp.org/bugzilla/show_bug.cgi?id=1287
     */
    private boolean bug1287 = true;

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = gl4.isExtensionAvailable("GL_NV_shader_thread_group");

        if (validated) {
            validated = initBuffer(gl4);
        }
        if (validated) {
            validated = initProgram(gl4);
        }
        if (validated) {
            validated = initVertexArray(gl4);
        }

        return validated;
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        if (validated) {

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

            ShaderProgram shaderProgram = new ShaderProgram();
            shaderProgram.init(gl4);

            programName = shaderProgram.program();

            gl4.glProgramParameteri(programName, GL_PROGRAM_SEPARABLE, GL_TRUE);

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);
            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            gl4.glGenProgramPipelines(1, pipelineName);
            gl4.glUseProgramStages(pipelineName.get(0), GL_VERTEX_SHADER_BIT | GL_FRAGMENT_SHADER_BIT, programName);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initVertexArray(GL4 gl4) {

        int bindingIndex = 0;

        gl4.glCreateVertexArrays(1, vertexArrayName);

        gl4.glVertexArrayAttribBinding(vertexArrayName.get(0), Semantic.Attr.POSITION, bindingIndex);
        gl4.glVertexArrayAttribFormat(vertexArrayName.get(0), Semantic.Attr.POSITION, 2, GL_FLOAT, false, 0);
        gl4.glEnableVertexArrayAttrib(vertexArrayName.get(0), Semantic.Attr.POSITION);

        gl4.glVertexArrayVertexBuffer(vertexArrayName.get(0), bindingIndex, bufferName.get(Buffer.VERTEX), 0, Vec2.SIZE);

        return true;
    }

    private boolean initBuffer(GL4 gl4) {

        IntBuffer constants = GLBuffers.newDirectIntBuffer(3);

        gl4.glCreateBuffers(Buffer.MAX, bufferName);

        Mat4 mvp = glm.ortho_(0.0f, windowSize.x * 1.0f, 0.0f, windowSize.y * 1f);
        FloatBuffer mvpBuffer = GLBuffers.newDirectFloatBuffer(mvp.toFa_());

        if (!bug1287) {
            gl4.glNamedBufferStorage(bufferName.get(Buffer.TRANSFORM), mvp.SIZE, mvpBuffer, 0);
        } else {
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.TRANSFORM));
            gl4.glBufferStorage(GL_UNIFORM_BUFFER, mvp.SIZE, mvpBuffer, 0);
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }

        gl4.glGetIntegerv(GL_WARP_SIZE_NV, constants);
        constants.position(1);
        gl4.glGetIntegerv(GL_WARPS_PER_SM_NV, constants);
        constants.position(2);
        gl4.glGetIntegerv(GL_SM_COUNT_NV, constants);
        constants.rewind();

        System.out.println("GL_WARP_SIZE_NV: " + constants.get(0));
        System.out.println("GL_WARPS_PER_SM_NV: " + constants.get(1));
        System.out.println("GL_SM_COUNT_NV: " + constants.get(2));

        if (!bug1287) {
            gl4.glNamedBufferStorage(bufferName.get(Buffer.CONSTANT), constants.capacity() * Integer.BYTES, constants, 0);
        } else {
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.CONSTANT));
            gl4.glBufferStorage(GL_UNIFORM_BUFFER, constants.capacity() * Integer.BYTES, constants, 0);
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }

        int windowDiv = 1;
        vertexCount = (windowSize.x / windowDiv) * (windowSize.y / windowDiv) * 6 * quadOverlapCount;
        float[] vertexPosition = new float[vertexCount * 2];

        for (int quadCoordIndexY = 0, quadCoordCountY = windowSize.y / windowDiv; quadCoordIndexY < quadCoordCountY;
                quadCoordIndexY++) {

            for (int quadCoordIndexX = 0, quadCoordCountX = windowSize.x / windowDiv; quadCoordIndexX < quadCoordCountX;
                    quadCoordIndexX++) {

                for (int quadOverlapIndex = 0; quadOverlapIndex < quadOverlapCount; quadOverlapIndex++) {

                    int quadIndex = (quadCoordIndexX + quadCoordCountX * quadCoordIndexY) * quadOverlapCount
                            + quadOverlapIndex;

                    vertexPosition[(quadIndex * 6 + 0) * 2 + 0] = quadCoordIndexX * 1 + 0;
                    vertexPosition[(quadIndex * 6 + 0) * 2 + 1] = quadCoordIndexY * 1 + 0;
                    vertexPosition[(quadIndex * 6 + 1) * 2 + 0] = quadCoordIndexX * 1 + 1;
                    vertexPosition[(quadIndex * 6 + 1) * 2 + 1] = quadCoordIndexY * 1 + 0;
                    vertexPosition[(quadIndex * 6 + 2) * 2 + 0] = quadCoordIndexX * 1 + 1;
                    vertexPosition[(quadIndex * 6 + 2) * 2 + 1] = quadCoordIndexY * 1 + 1;
                    vertexPosition[(quadIndex * 6 + 3) * 2 + 0] = quadCoordIndexX * 1 + 0;
                    vertexPosition[(quadIndex * 6 + 3) * 2 + 1] = quadCoordIndexY * 1 + 0;
                    vertexPosition[(quadIndex * 6 + 4) * 2 + 0] = quadCoordIndexX * 1 + 1;
                    vertexPosition[(quadIndex * 6 + 4) * 2 + 1] = quadCoordIndexY * 1 + 1;
                    vertexPosition[(quadIndex * 6 + 5) * 2 + 0] = quadCoordIndexX * 1 + 0;
                    vertexPosition[(quadIndex * 6 + 5) * 2 + 1] = quadCoordIndexY * 1 + 1;
                }
            }
        }
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexPosition);
        if (!bug1287) {
            gl4.glNamedBufferStorage(bufferName.get(Buffer.VERTEX), vertexPosition.length * Float.BYTES, vertexBuffer, 0);
        } else {
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.VERTEX));
            gl4.glBufferStorage(GL_UNIFORM_BUFFER, vertexBuffer.capacity() * Float.BYTES, vertexBuffer, 0);
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }
        BufferUtils.destroyDirectBuffer(constants);

        BufferUtils.destroyDirectBuffer(mvpBuffer);
        BufferUtils.destroyDirectBuffer(constants);

        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glViewportIndexedf(0, 0, 0, windowSize.x, windowSize.y);
        gl4.glClearBufferfv(GL_COLOR, 0, white);

        gl4.glBindProgramPipeline(pipelineName.get(0));
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.CONSTANT, bufferName.get(Buffer.CONSTANT));
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName.get(Buffer.TRANSFORM));
        gl4.glBindVertexArray(vertexArrayName.get(0));

        gl4.glDrawArraysInstancedBaseInstance(GL_TRIANGLES, 0, 6 * vertexCount, 1, 0);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteBuffers(Buffer.MAX, bufferName);
        BufferUtils.destroyDirectBuffer(bufferName);
        gl4.glDeleteProgramPipelines(1, pipelineName);
        BufferUtils.destroyDirectBuffer(pipelineName);
        gl4.glDeleteProgram(programName);
        gl4.glDeleteVertexArrays(1, vertexArrayName);
        BufferUtils.destroyDirectBuffer(vertexArrayName);
        BufferUtils.destroyDirectBuffer(white);

        return true;
    }
}
