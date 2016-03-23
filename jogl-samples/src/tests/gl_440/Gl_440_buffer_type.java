/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_440;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import glm.glm;
import glm.mat._4.Mat4;
import glm.vec._2.Vec2;
import glm.vec._2.i.Vec2i;
import dev.Vec2i8;
import framework.BufferUtils;
import glm.vec._3.Vec3;
import glm.vec._4.Vec4;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 *
 * @author GBarbieri
 */
public class Gl_440_buffer_type extends Test {

    public static void main(String[] args) {
        Gl_440_buffer_type gl_440_buffer_type = new Gl_440_buffer_type();
    }

    public Gl_440_buffer_type() {
        super("gl-440-buffer-type", Profile.CORE, 4, 4);
    }

    private final String SHADERS_SOURCE = "buffer-type";
    private final String SHADERS_ROOT = "src/data/gl_440";

    private int vertexCount = 6;
    private int positionSizeF16 = vertexCount * Short.BYTES * 2;
    private short[] positionDataF16 = {
        glm.packHalf1x16(0.0f), glm.packHalf1x16(0.0f),
        glm.packHalf1x16(1.0f), glm.packHalf1x16(0.0f),
        glm.packHalf1x16(1.0f), glm.packHalf1x16(1.0f),
        glm.packHalf1x16(1.0f), glm.packHalf1x16(1.0f),
        glm.packHalf1x16(0.0f), glm.packHalf1x16(1.0f),
        glm.packHalf1x16(0.0f), glm.packHalf1x16(0.0f)};

    private int positionSizeF32 = vertexCount * Vec2.SIZE;
    private float[] positionDataF32 = {
        0.0f, 0.0f,
        1.0f, 0.0f,
        1.0f, 1.0f,
        1.0f, 1.0f,
        0.0f, 1.0f,
        0.0f, 0.0f};

    private int positionSizeI8 = vertexCount * Vec2i8.SIZE;
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

    private int positionSizeI32 = vertexCount * Vec2i.SIZE;
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

    private class VertexFormat {

        public static final int F32 = 0;
        public static final int I8 = 1;
        public static final int I32 = 2;
        public static final int RGB10A2 = 3;
        public static final int F16 = 4;
        public static final int RG11B10F = 5;
        public static final int MAX = 6;
    }

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int TRANSFORM = 1;
        public static final int MAX = 2;
    }

    private class Viewport {

        public static final int _0 = 0;
        public static final int _1 = 1;
        public static final int _2 = 2;
        public static final int _3 = 3;
        public static final int _4 = 4;
        public static final int _5 = 5;
        public static final int MAX = 6;
    }

    private class View {

        public Vec4 viewport;
        public int vertexFormat;

        public View(Vec4 viewport, int vertexFormat) {
            this.viewport = viewport;
            this.vertexFormat = vertexFormat;
        }
    }

    private IntBuffer pipelineName = GLBuffers.newDirectIntBuffer(1),
            bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX),
            vertexArrayName = GLBuffers.newDirectIntBuffer(VertexFormat.MAX);
    private int programName;
    private ByteBuffer uniformPointer;
    private View[] viewport;

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        Vec2 viewportSize = new Vec2(windowSize.x * 0.33f, windowSize.y * 0.50f);

        viewport = new View[Viewport.MAX];

        viewport[Viewport._0] = new View(new Vec4(viewportSize.x * 0.0f, viewportSize.y * 0.0f, viewportSize.x * 1.0f,
                viewportSize.y * 1.0f), VertexFormat.F16);
        viewport[Viewport._1] = new View(new Vec4(viewportSize.x * 1.0f, viewportSize.y * 0.0f, viewportSize.x * 1.0f,
                viewportSize.y * 1.0f), VertexFormat.I32);
        viewport[Viewport._2] = new View(new Vec4(viewportSize.x * 2.0f, viewportSize.y * 0.0f, viewportSize.x * 1.0f,
                viewportSize.y * 1.0f), VertexFormat.RGB10A2);
        viewport[Viewport._3] = new View(new Vec4(viewportSize.x * 0.0f, viewportSize.y * 1.0f, viewportSize.x * 1.0f,
                viewportSize.y * 1.0f), VertexFormat.I8);
        viewport[Viewport._4] = new View(new Vec4(viewportSize.x * 1.0f, viewportSize.y * 1.0f, viewportSize.x * 1.0f,
                viewportSize.y * 1.0f), VertexFormat.F32);
        viewport[Viewport._5] = new View(new Vec4(viewportSize.x * 2.0f, viewportSize.y * 1.0f, viewportSize.x * 1.0f,
                viewportSize.y * 1.0f), VertexFormat.RG11B10F);

        boolean validated = true;
        validated = validated && gl4.isExtensionAvailable("GL_ARB_vertex_type_10f_11f_11f_rev");

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
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.TRANSFORM));
            uniformPointer = gl4.glMapBufferRange(GL_UNIFORM_BUFFER, 0, Mat4.SIZE, GL_MAP_WRITE_BIT
                    | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);
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

            gl4.glGenProgramPipelines(1, pipelineName);
            gl4.glUseProgramStages(pipelineName.get(0), GL_VERTEX_SHADER_BIT | GL_FRAGMENT_SHADER_BIT, programName);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        // Generate a buffer object
        gl4.glGenBuffers(Buffer.MAX, bufferName);

        // Allocate and copy buffers memory
        byte[] data = new byte[positionSizeF32 + positionSizeI8 + positionSizeI32 + positionSizeRGB10A2
                + positionSizeF16 + positionSizeRG11FB10F];
        ByteBuffer dataBuffer = GLBuffers.newDirectByteBuffer(data);

        int currentOffset = 0;
        dataBuffer.asFloatBuffer().put(positionDataF32);

        currentOffset += positionSizeF32;
        dataBuffer.position(currentOffset);
        dataBuffer.put(positionDataI8);

        currentOffset += positionSizeI8;
        dataBuffer.position(currentOffset);
        dataBuffer.asIntBuffer().put(positionDataI32);

        currentOffset += positionSizeI32;
        dataBuffer.position(currentOffset);
        dataBuffer.asIntBuffer().put(positionDataRGB10A2);

        currentOffset += positionSizeRGB10A2;
        dataBuffer.position(currentOffset);
        dataBuffer.asShortBuffer().put(positionDataF16);

        currentOffset += positionSizeF16;
        dataBuffer.position(currentOffset);
        dataBuffer.asIntBuffer().put(positionDataRG11FB10F);

        dataBuffer.rewind();

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
        gl4.glBufferStorage(GL_ARRAY_BUFFER, data.length * Byte.BYTES, dataBuffer, 0);

        int[] uniformBufferOffset = {0};
        gl4.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset, 0);
        int uniformBlockSize = Math.max(Mat4.SIZE, uniformBufferOffset[0]);

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.TRANSFORM));
        gl4.glBufferStorage(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT
                | GL_MAP_COHERENT_BIT);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return checkError(gl4, "initBuffer");
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(VertexFormat.MAX, vertexArrayName);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));

        int currentOffset = 0;
        gl4.glBindVertexArray(vertexArrayName.get(VertexFormat.F32));
        gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, Vec2.SIZE, currentOffset);
        gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);

        currentOffset += positionSizeF32;
        gl4.glBindVertexArray(vertexArrayName.get(VertexFormat.I8));
        gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_BYTE, false, Vec2i8.SIZE, currentOffset);
        gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);

        currentOffset += positionSizeI8;
        gl4.glBindVertexArray(vertexArrayName.get(VertexFormat.I32));
        gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_INT, false, Vec2i.SIZE, currentOffset);
        gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);

        currentOffset += positionSizeI32;
        gl4.glBindVertexArray(vertexArrayName.get(VertexFormat.RGB10A2));
        gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 4, GL_INT_2_10_10_10_REV, true, Integer.BYTES, currentOffset);
        gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);

        currentOffset += positionSizeRGB10A2;
        gl4.glBindVertexArray(vertexArrayName.get(VertexFormat.F16));
        gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_HALF_FLOAT, false, Short.BYTES * 2, currentOffset);
        gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);

        currentOffset += positionSizeF16;
        gl4.glBindVertexArray(vertexArrayName.get(VertexFormat.RG11B10F));
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
            float aspect = (windowSize.x * 0.33f) / (windowSize.y * 0.50f);
            Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, aspect, 0.1f, 100.0f);
            Mat4 mvp = projection.mul(viewMat4()).mul(new Mat4(1.0f));

            uniformPointer.asFloatBuffer().put(mvp.toFa_());
        }

        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, 0);

        gl4.glBindProgramPipeline(pipelineName.get(0));
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName.get(Buffer.TRANSFORM));

        for (int index = 0; index < Viewport.MAX; ++index) {

            gl4.glViewportIndexedf(0,
                    viewport[index].viewport.x,
                    viewport[index].viewport.y,
                    viewport[index].viewport.z,
                    viewport[index].viewport.w);

            gl4.glBindVertexArray(vertexArrayName.get(viewport[index].vertexFormat));
            gl4.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 1);
        }

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        if (uniformPointer == null) {

            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.TRANSFORM));
            gl4.glUnmapBuffer(GL_UNIFORM_BUFFER);
            BufferUtils.destroyDirectBuffer(uniformPointer);
        }

        gl4.glDeleteBuffers(Buffer.MAX,  bufferName);
        BufferUtils.destroyDirectBuffer(bufferName);
        gl4.glDeleteVertexArrays(VertexFormat.MAX,  vertexArrayName);
        BufferUtils.destroyDirectBuffer(vertexArrayName);
        gl4.glDeleteProgramPipelines(1,  pipelineName);
        BufferUtils.destroyDirectBuffer(pipelineName);
        gl4.glDeleteProgram(programName);

        return true;
    }
}
