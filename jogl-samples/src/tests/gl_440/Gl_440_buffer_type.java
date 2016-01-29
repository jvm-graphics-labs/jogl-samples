/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_440;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_BYTE;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_HALF_FLOAT;
import static com.jogamp.opengl.GL.GL_MAP_INVALIDATE_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_MAP_WRITE_BIT;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_TRUE;
import static com.jogamp.opengl.GL.GL_UNSIGNED_INT_10F_11F_11F_REV;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER_BIT;
import static com.jogamp.opengl.GL2ES2.GL_INT;
import static com.jogamp.opengl.GL2ES2.GL_PROGRAM_SEPARABLE;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER_BIT;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT;
import static com.jogamp.opengl.GL3ES3.GL_INT_2_10_10_10_REV;
import com.jogamp.opengl.GL4;
import static com.jogamp.opengl.GL4.GL_MAP_COHERENT_BIT;
import static com.jogamp.opengl.GL4.GL_MAP_PERSISTENT_BIT;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import core.glm;
import dev.Mat4;
import dev.Vec2;
import dev.Vec2i;
import dev.Vec2i8;
import dev.Vec3;
import dev.Vec4;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.nio.ByteBuffer;

/**
 *
 * @author GBarbieri
 */
public class Gl_440_buffer_type extends Test {

    public static void main(String[] args) {
        Gl_440_buffer_type gl_440_buffer_type = new Gl_440_buffer_type();
    }

    public Gl_440_buffer_type() {
        super("gl-430-buffer-type", Profile.CORE, 4, 4);
    }

    private final String SHADERS_SOURCE = "buffer-type";
    private final String SHADERS_ROOT = "src/data/gl_440";

    private int vertexCount = 6;
    private int positionSizeF16 = vertexCount * Short.BYTES * 2;
    private short[] positionDataF16 = {
        (short) 0.0f, (short) 0.0f,
        (short) 1.0f, (short) 0.0f,
        (short) 1.0f, (short) 1.0f,
        (short) 1.0f, (short) 1.0f,
        (short) 0.0f, (short) 1.0f,
        (short) 0.0f, (short) 0.0f};

    private int positionSizeF32 = vertexCount * Vec2.SIZEOF;
    private float[] positionDataF32 = {
        0.0f, 0.0f,
        1.0f, 0.0f,
        1.0f, 1.0f,
        1.0f, 1.0f,
        0.0f, 1.0f,
        0.0f, 0.0f};

    private int positionSizeI8 = vertexCount * Vec2i8.SIZEOF;
    private byte[] positionDataI8 = {
        (byte) 0, (byte) 0,
        (byte) 1, (byte) 0,
        (byte) 1, (byte) 1,
        (byte) 1, (byte) 1,
        (byte) 0, (byte) 1,
        (byte) 0, (byte) 0};

    private int positionSizeRGB10A2 = vertexCount * Integer.BYTES;
    private int[] positionDataRGB10A2 = {
        glm.packSnorm3x10_1x2(new Vec4(0.0f, 0.0f, 0.0f, 1.0f)),
        glm.packSnorm3x10_1x2(new Vec4(1.0f, 0.0f, 0.0f, 1.0f)),
        glm.packSnorm3x10_1x2(new Vec4(1.0f, 1.0f, 0.0f, 1.0f)),
        glm.packSnorm3x10_1x2(new Vec4(1.0f, 1.0f, 0.0f, 1.0f)),
        glm.packSnorm3x10_1x2(new Vec4(0.0f, 1.0f, 0.0f, 1.0f)),
        glm.packSnorm3x10_1x2(new Vec4(0.0f, 0.0f, 0.0f, 1.0f))};

    private int positionSizeI32 = vertexCount * Vec2i.SIZEOF;
    private int[] positionDataI32 = {
        0, 0,
        1, 0,
        1, 1,
        1, 1,
        0, 1,
        0, 0};

    private int positionSizeRG11FB10F = vertexCount * Integer.BYTES;
    private int[] positionDataRG11FB10F = {
        glm.packF2x11_1x10(new Vec3(0.0f, 0.0f, 0.0f)),
        glm.packF2x11_1x10(new Vec3(1.0f, 0.0f, 0.0f)),
        glm.packF2x11_1x10(new Vec3(1.0f, 1.0f, 0.0f)),
        glm.packF2x11_1x10(new Vec3(1.0f, 1.0f, 0.0f)),
        glm.packF2x11_1x10(new Vec3(0.0f, 1.0f, 0.0f)),
        glm.packF2x11_1x10(new Vec3(0.0f, 0.0f, 0.0f))};

    private enum VertexFormat {
        F32,
        I8,
        I32,
        RGB10A2,
        F16,
        RG11B10F,
        MAX
    }

    private enum Buffer {
        VERTEX,
        TRANSFORM,
        MAX
    }

    private enum Viewport {
        VIEWPORT0,
        VIEWPORT1,
        VIEWPORT2,
        VIEWPORT3,
        VIEWPORT4,
        VIEWPORT5,
        MAX
    }

    private class View {

        public Vec4 viewport;
        public VertexFormat vertexFormat;

        public View(Vec4 viewport, VertexFormat vertexFormat) {
            this.viewport = viewport;
            this.vertexFormat = vertexFormat;
        }
    }

    private int[] pipelineName = {0}, bufferName = new int[Buffer.MAX.ordinal()],
            vertexArrayName = new int[VertexFormat.MAX.ordinal()];
    private int programName;
    private ByteBuffer uniformPointer;
    private View[] viewport = new View[Viewport.MAX.ordinal()];

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

//        glm::vec2 ViewportSize = glm::vec2(this->getWindowSize()) * glm::vec2(0.33f, 0.50f);
        Vec2 viewportSize = new Vec2(windowSize.x * 0.33f, windowSize.y * 0.50f);

        viewport[Viewport.VIEWPORT0.ordinal()] = new View(new Vec4(viewportSize.x * 0.0f, viewportSize.y * 0.0f,
                viewportSize.x * 1.0f, viewportSize.y * 1.0f), VertexFormat.F16);
        viewport[Viewport.VIEWPORT1.ordinal()] = new View(new Vec4(viewportSize.x * 1.0f, viewportSize.y * 0.0f,
                viewportSize.x * 1.0f, viewportSize.y * 1.0f), VertexFormat.I32);
        viewport[Viewport.VIEWPORT2.ordinal()] = new View(new Vec4(viewportSize.x * 2.0f, viewportSize.y * 0.0f,
                viewportSize.x * 1.0f, viewportSize.y * 1.0f), VertexFormat.RGB10A2);
        viewport[Viewport.VIEWPORT3.ordinal()] = new View(new Vec4(viewportSize.x * 0.0f, viewportSize.y * 1.0f,
                viewportSize.x * 1.0f, viewportSize.y * 1.0f), VertexFormat.I8);
        viewport[Viewport.VIEWPORT4.ordinal()] = new View(new Vec4(viewportSize.x * 1.0f, viewportSize.y * 1.0f,
                viewportSize.x * 1.0f, viewportSize.y * 1.0f), VertexFormat.F32);
        viewport[Viewport.VIEWPORT5.ordinal()] = new View(new Vec4(viewportSize.x * 2.0f, viewportSize.y * 1.0f,
                viewportSize.x * 1.0f, viewportSize.y * 1.0f), VertexFormat.RG11B10F);

        boolean validated = true;
        validated = validated && checkExtension(gl4, "GL_ARB_vertex_type_10f_11f_11f_rev");

        if (validated) {
            validated = initProgram(gl4);
        }
        if (validated) {
            validated = initBuffer(gl4);
        }
        if (validated) {
            validated = initVertexArray(gl4);
        }
        if (validated) {
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM.ordinal()]);
            uniformPointer = gl4.glMapBufferRange(
                    GL_UNIFORM_BUFFER, 0, Mat4.SIZEOF,
                    GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);
        }

        return validated;
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        // Create program
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

            gl4.glGenProgramPipelines(1, pipelineName, 0);
            gl4.glUseProgramStages(pipelineName[0], GL_VERTEX_SHADER_BIT | GL_FRAGMENT_SHADER_BIT, programName);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        // Generate a buffer object
        gl4.glGenBuffers(Buffer.MAX.ordinal(), bufferName, 0);

        // Allocate and copy buffers memory
        byte[] data = new byte[positionSizeF32 + positionSizeI8 + positionSizeI32 + positionSizeRGB10A2
                + positionSizeF16 + positionSizeRG11FB10F];
        ByteBuffer dataBuffer = GLBuffers.newDirectByteBuffer(data);

        int currentOffset = 0;
        dataBuffer.asIntBuffer().put(positionSizeF32);
        currentOffset += positionSizeF32;
        dataBuffer.position(currentOffset);
        dataBuffer.put(positionDataI8);
        currentOffset += positionSizeI8;
        dataBuffer.position(currentOffset);
        dataBuffer.asIntBuffer().put(positionDataI32);
        currentOffset += positionSizeI32;
        dataBuffer.asIntBuffer().put(positionDataRGB10A2);
        currentOffset += positionSizeRGB10A2;
        dataBuffer.asShortBuffer().put(positionDataF16);
        currentOffset += positionSizeF16;
        dataBuffer.asIntBuffer().put(positionDataRG11FB10F);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);
        gl4.glBufferStorage(GL_ARRAY_BUFFER, data.length * Byte.BYTES, dataBuffer, 0);

        int[] uniformBufferOffset = {0};
        gl4.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset, 0);
        int uniformBlockSize = Math.max(Mat4.SIZEOF, uniformBufferOffset[0]);

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM.ordinal()]);
        gl4.glBufferStorage(GL_UNIFORM_BUFFER, uniformBlockSize, null,
                GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return checkError(gl4, "initBuffer");
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(VertexFormat.MAX.ordinal(), vertexArrayName, 0);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);

        int currentOffset = 0;
        gl4.glBindVertexArray(vertexArrayName[VertexFormat.F32.ordinal()]);
        gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, Vec2.SIZEOF, currentOffset);
        gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);

        currentOffset += positionSizeF32;
        gl4.glBindVertexArray(vertexArrayName[VertexFormat.I8.ordinal()]);
        gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_BYTE, false, Vec2i8.SIZEOF, currentOffset);
        gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);

        currentOffset += positionSizeI8;
        gl4.glBindVertexArray(vertexArrayName[VertexFormat.I32.ordinal()]);
        gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_INT, false, Vec2i.SIZEOF, currentOffset);
        gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);

        currentOffset += positionSizeI32;
        gl4.glBindVertexArray(vertexArrayName[VertexFormat.RGB10A2.ordinal()]);
        gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 4, GL_INT_2_10_10_10_REV, true, Integer.BYTES, currentOffset);
        gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);

        currentOffset += positionSizeRGB10A2;
        gl4.glBindVertexArray(vertexArrayName[VertexFormat.F16.ordinal()]);
        gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_HALF_FLOAT, false, Short.BYTES * 2, currentOffset);
        gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);

        currentOffset += positionSizeF16;
        gl4.glBindVertexArray(vertexArrayName[VertexFormat.RG11B10F.ordinal()]);
        gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 3, GL_UNSIGNED_INT_10F_11F_11F_REV, false, Integer.BYTES,
                currentOffset);
        gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);

        gl4.glBindVertexArray(0);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        return checkError(gl4, "initVertexArray");
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        {
            // Compute the MVP (Model View Projection matrix)
            float aspect = windowSize.x * 0.33f / windowSize.y * 0.50f;
            Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, aspect, 0.1f, 100.0f);
            Mat4 mvp = projection.mul(viewMat4()).mul(new Mat4(1.0f));

            uniformPointer.asFloatBuffer().put(mvp.toFA_());
        }

        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, 0);

        gl4.glBindProgramPipeline(pipelineName[0]);
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.TRANSFORM.ordinal()]);

        for (int index = 0; index < Viewport.MAX.ordinal(); ++index) {

            gl4.glViewportIndexedf(0,
                    viewport[index].viewport.x,
                    viewport[index].viewport.y,
                    viewport[index].viewport.z,
                    viewport[index].viewport.w);

            gl4.glBindVertexArray(vertexArrayName[viewport[index].vertexFormat.ordinal()]);
            gl4.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 1);
        }

        return true;
    }
}
