/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_320.program;

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
import glm.vec._2.Vec2;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 *
 * @author GBarbieri
 */
public class Gl_320_program extends Test {

    public static void main(String[] args) {
        Gl_320_program gl_320_program = new Gl_320_program();
    }

    public Gl_320_program() {
        super("gl-320-program", Profile.CORE, 3, 2);
    }

    private final String SHADERS_SOURCE = "program";
    private final String SHADERS_ROOT = "src/data/gl_320/program";

    private int vertexCount = 4;
    private int positionSize = vertexCount * Vec2.SIZE;
    private float[] positionData = {
        -1.0f, -1.0f,
        +1.0f, -1.0f,
        +1.0f, +1.0f,
        -1.0f, +1.0f};

    private int elementCount = 6;
    private int elementSize = elementCount * Short.BYTES;
    private short[] elementData = {
        0, 1, 2,
        2, 3, 0};

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int ELEMENT = 1;
        public static final int TRANSFORM = 2;
        public static final int MATERIAL = 3;
        public static final int MAX = 4;
    }

    private class Program {

        public static final int USED = 0;
        public static final int MAX = 1;
    }

    private int[] programName = new int[Program.MAX];
    private IntBuffer bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX),
            vertexArrayName = GLBuffers.newDirectIntBuffer(1);
    private int uniformTransform, uniformMaterial;
    private FloatBuffer clearColor = GLBuffers.newDirectFloatBuffer(new float[]{1.0f, 1.0f, 1.0f, 1.0f});

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

        return validated && checkError(gl3, "begin");
    }

    private boolean initProgram(GL3 gl3) {

        boolean validated = true;

        if (validated) {

            ShaderCode vertShaderCode = ShaderCode.create(gl3, GL_VERTEX_SHADER, this.getClass(), SHADERS_ROOT, null,
                    SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl3, GL_FRAGMENT_SHADER, this.getClass(), SHADERS_ROOT, null,
                    SHADERS_SOURCE, "frag", null, true);

            ShaderProgram shaderProgram = new ShaderProgram();
            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);

            shaderProgram.init(gl3);

            programName[Program.USED] = shaderProgram.program();

            gl3.glBindAttribLocation(programName[Program.USED], Semantic.Attr.POSITION, "position");
            gl3.glBindFragDataLocation(programName[Program.USED], Semantic.Frag.COLOR, "color");

            shaderProgram.link(gl3, System.out);
        }
        if (validated) {

            uniformMaterial = gl3.glGetUniformBlockIndex(programName[Program.USED], "Material");
            uniformTransform = gl3.glGetUniformBlockIndex(programName[Program.USED], "Transform");
        }

        IntBuffer activeUniformBlocks = GLBuffers.newDirectIntBuffer(1);
        gl3.glGetProgramiv(programName[Program.USED], GL_ACTIVE_UNIFORM_BLOCKS, activeUniformBlocks);

        for (int i = 0; i < activeUniformBlocks.get(0); ++i) {

            IntBuffer length = GLBuffers.newDirectIntBuffer(1);
            byte[] bytes = new byte[128];
            ByteBuffer name = GLBuffers.newDirectByteBuffer(bytes);

            gl3.glGetActiveUniformBlockName(programName[Program.USED], i, name.capacity(), length, name);

            name.get(bytes);
            String stringName = new String(bytes);
            //remove the empty padding spaces at the end
            stringName = stringName.trim();

            BufferUtils.destroyDirectBuffer(length);
            BufferUtils.destroyDirectBuffer(name);

            validated = validated && (stringName.equals("Material") || stringName.equals("Transform"));
        }

        int[] activeUniform = {0};
        gl3.glGetProgramiv(programName[Program.USED], GL_ACTIVE_UNIFORMS, activeUniform, 0);

        for (int i = 0; i < activeUniformBlocks.get(0); ++i) {

            IntBuffer length = GLBuffers.newDirectIntBuffer(1);
            byte[] bytes = new byte[128];
            ByteBuffer name = GLBuffers.newDirectByteBuffer(bytes);

            gl3.glGetActiveUniformName(programName[Program.USED], i, name.capacity(), length, name);

            name.get(bytes);
            String stringName = new String(bytes);
            //remove the empty padding spaces at the end
            stringName = stringName.trim();

            BufferUtils.destroyDirectBuffer(length);
            BufferUtils.destroyDirectBuffer(name);

            validated = validated && (stringName.equals("Material.diffuse") || stringName.equals("Transform.mvp"));
        }

        BufferUtils.destroyDirectBuffer(activeUniformBlocks);

        return validated & checkError(gl3, "initProgram");
    }

    private boolean initVertexArray(GL3 gl3) {

        gl3.glGenVertexArrays(1, vertexArrayName);
        gl3.glBindVertexArray(vertexArrayName.get(0));
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 0, 0);

            gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);

            gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT));
        }
        gl3.glBindVertexArray(0);

        return checkError(gl3, "initVertexArray");
    }

    private boolean initBuffer(GL3 gl3) {

        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        FloatBuffer positionBuffer = GLBuffers.newDirectFloatBuffer(positionData);
        IntBuffer uniformBlockSize = GLBuffers.newDirectIntBuffer(1);

        gl3.glGenBuffers(Buffer.MAX, bufferName);

        gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT));
        gl3.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
        gl3.glBufferData(GL_ARRAY_BUFFER, positionSize, positionBuffer, GL_STATIC_DRAW);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        {
            gl3.glGetActiveUniformBlockiv(programName[Program.USED], uniformTransform, GL_UNIFORM_BLOCK_DATA_SIZE,
                    uniformBlockSize);

            gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.TRANSFORM));
            gl3.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize.get(0), null, GL_DYNAMIC_DRAW);
            gl3.glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }

        {

            FloatBuffer diffuseBuffer = GLBuffers.newDirectFloatBuffer(new float[]{1.0f, 0.5f, 0.0f, 1.0f});

            gl3.glGetActiveUniformBlockiv(programName[Program.USED], uniformMaterial, GL_UNIFORM_BLOCK_DATA_SIZE,
                    uniformBlockSize);

            gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.MATERIAL));
            gl3.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize.get(0), diffuseBuffer, GL_DYNAMIC_DRAW);
            gl3.glBindBuffer(GL_UNIFORM_BUFFER, 0);

            BufferUtils.destroyDirectBuffer(diffuseBuffer);
        }

        BufferUtils.destroyDirectBuffer(elementBuffer);
        BufferUtils.destroyDirectBuffer(positionBuffer);

        return checkError(gl3, "initBuffer");
    }

    @Override
    protected boolean render(GL gl) {

        GL3 gl3 = (GL3) gl;

        {
            gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.TRANSFORM));
            ByteBuffer pointer = gl3.glMapBufferRange(
                    GL_UNIFORM_BUFFER, 0, Mat4.SIZE,
                    GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, 4.0f / 3.0f, 0.1f, 100.0f);
            Mat4 model = new Mat4(1.0f);
            Mat4 mvp = projection.mul(viewMat4()).mul(model);

            pointer.asFloatBuffer().put(mvp.toFa_());

            // Make sure the uniform buffer is uploaded
            gl3.glUnmapBuffer(GL_UNIFORM_BUFFER);
            gl3.glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }

        gl3.glViewport(0, 0, windowSize.x, windowSize.y);
        gl3.glClearBufferfv(GL_COLOR, 0, clearColor);

        gl3.glUseProgram(programName[Program.USED]);
        gl3.glUniformBlockBinding(programName[Program.USED], uniformTransform, Semantic.Uniform.TRANSFORM0);
        gl3.glUniformBlockBinding(programName[Program.USED], uniformMaterial, Semantic.Uniform.MATERIAL);

        // Attach the buffer to UBO binding point semantic::uniform::TRANSFORM0
        gl3.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName.get(Buffer.TRANSFORM));

        // Attach the buffer to UBO binding point semantic::uniform::MATERIAL
        gl3.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.MATERIAL, bufferName.get(Buffer.MATERIAL));

        gl3.glBindVertexArray(vertexArrayName.get(0));
        gl3.glDrawElementsInstanced(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;

        gl3.glDeleteVertexArrays(1, vertexArrayName);
        gl3.glDeleteBuffers(Buffer.MAX, bufferName);
        gl3.glDeleteProgram(programName[Program.USED]);

        BufferUtils.destroyDirectBuffer(vertexArrayName);
        BufferUtils.destroyDirectBuffer(bufferName);

        BufferUtils.destroyDirectBuffer(clearColor);

        return checkError(gl3, "end");
    }
}
