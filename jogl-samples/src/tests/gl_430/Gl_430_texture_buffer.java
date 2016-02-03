/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_430;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL3ES3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import core.glm;
import dev.Mat4;
import dev.Vec3;
import framework.BufferUtils;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import jglm.Vec2;
import jglm.Vec2i;

/**
 *
 * @author GBarbieri
 */
public class Gl_430_texture_buffer extends Test {

    public static void main(String[] args) {
        Gl_430_texture_buffer gl_430_texture_buffer = new Gl_430_texture_buffer();
    }

    public Gl_430_texture_buffer() {
        super("gl-430-texture-buffer", Profile.CORE, 4, 3, new Vec2i(640, 480),
                new Vec2((float) Math.PI * 0.2f, (float) Math.PI * 0.2f));
    }

    private final String SHADERS_SOURCE = "texture-buffer";
    private final String SHADERS_ROOT = "src/data/gl_430";

    private int vertexCount = 4;
    private int vertexSize = vertexCount * 2 * Float.BYTES;
    private float[] vertexData = {
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
        public static final int DISPLACEMENT = 2;
        public static final int DIFFUSE = 3;
        public static final int TRANSFORM = 4;
        public static final int MAX = 5;
    }

    private class Texture {

        public static final int DISPLACEMENT = 0;
        public static final int DIFFUSE = 1;
        public static final int MAX = 2;
    }

    private int[] vertexArrayName = {0}, bufferName = new int[Buffer.MAX],
            textureName = new int[Texture.MAX];
    private int programName, uniformMvp;
    private final Mat4 projection = new Mat4(), model = new Mat4();

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;

        validated = validated && checkExtension(gl4, "GL_ARB_texture_buffer_range");

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
            validated = initTexture(gl4);
        }
        if (validated) {
            validated = initVertexArray(gl4);
        }

        return validated;
    }

    private boolean initTest(GL4 gl4) {

        boolean validated = true;
        gl4.glEnable(GL_DEPTH_TEST);

        return validated && checkError(gl4, "initTest");
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

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);

            shaderProgram.link(gl4, System.out);
        }

        // Get variables locations
        if (validated) {

            uniformMvp = gl4.glGetUniformLocation(programName, "mvp");
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        float[] displacement = {
            +0.1f, +0.3f, -1.0f,
            -0.5f, +0.0f, -0.5f,
            -0.2f, -0.2f, +0.0f,
            +0.3f, +0.2f, +0.5f,
            +0.1f, -0.3f, +1.0f};

        float[] diffuse = {
            1.0f, 0.0f, 0.0f,
            1.0f, 0.5f, 0.0f,
            1.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 1.0f};

        int[] textureBufferOffsetAlignment = {0};
        gl4.glGetIntegerv(GL_TEXTURE_BUFFER_OFFSET_ALIGNMENT, textureBufferOffsetAlignment, 0);

        int displacementSize = Vec3.SIZEOF * displacement.length;
        int diffuseSize = Vec3.SIZEOF * diffuse.length;
        int displacementMultiple = glm.ceilMultiple(displacementSize, textureBufferOffsetAlignment[0]);
        int diffuseMultiple = glm.ceilMultiple(diffuseSize, textureBufferOffsetAlignment[0]);

        gl4.glGenBuffers(Buffer.MAX, bufferName, 0);

        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT]);
        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(elementBuffer);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl4.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(vertexBuffer);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_TEXTURE_BUFFER, bufferName[Buffer.DISPLACEMENT]);
        gl4.glBufferData(GL_TEXTURE_BUFFER, textureBufferOffsetAlignment[0] + displacementMultiple, null, GL_STATIC_DRAW);
        FloatBuffer displacementBuffer = GLBuffers.newDirectFloatBuffer(displacement);
        gl4.glBufferSubData(GL_TEXTURE_BUFFER, textureBufferOffsetAlignment[0], displacementSize, displacementBuffer);
        BufferUtils.destroyDirectBuffer(displacementBuffer);
        /*
        void * PointerDisplacement = glMapBufferRange(GL_TEXTURE_BUFFER,
                0, sizeof(Displacement), GL_MAP_WRITE_BIT );
        memcpy(PointerDisplacement, &Displacement[0], sizeof(Displacement));
        glUnmapBuffer(GL_TEXTURE_BUFFER);
         */
        gl4.glBindBuffer(GL_TEXTURE_BUFFER, 0);

        gl4.glBindBuffer(GL_TEXTURE_BUFFER, bufferName[Buffer.DIFFUSE]);
        gl4.glBufferData(GL_TEXTURE_BUFFER, textureBufferOffsetAlignment[0] + diffuseMultiple, null, GL_STATIC_DRAW);
        FloatBuffer diffuseBuffer = GLBuffers.newDirectFloatBuffer(diffuse);
        gl4.glBufferSubData(GL_TEXTURE_BUFFER, textureBufferOffsetAlignment[0], diffuseSize, diffuseBuffer);
        BufferUtils.destroyDirectBuffer(diffuseBuffer);
        gl4.glBindBuffer(GL_TEXTURE_BUFFER, 0);

        int[] uniformBufferOffset = {0};
        gl4.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset, 0);
        int uniformBlockSize = Math.max(Mat4.SIZEOF, uniformBufferOffset[0]);

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
        gl4.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_DYNAMIC_DRAW);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return true;
    }

    private boolean initTexture(GL4 gl4) {

        int[] textureBufferOffsetAlignment = {0};
        gl4.glGetIntegerv(GL_TEXTURE_BUFFER_OFFSET_ALIGNMENT, textureBufferOffsetAlignment, 0);

        gl4.glGenTextures(Texture.MAX, textureName, 0);

        gl4.glBindTexture(GL_TEXTURE_BUFFER, textureName[Texture.DISPLACEMENT]);
        gl4.glTexBufferRange(GL_TEXTURE_BUFFER, GL_RGB32F, bufferName[Buffer.DISPLACEMENT],
                textureBufferOffsetAlignment[0], Vec3.SIZEOF * 5);
        gl4.glBindTexture(GL_TEXTURE_BUFFER, 0);

        gl4.glBindTexture(GL_TEXTURE_BUFFER, textureName[Texture.DIFFUSE]);
        gl4.glTexBufferRange(GL_TEXTURE_BUFFER, GL_RGB32F, bufferName[Buffer.DIFFUSE],
                textureBufferOffsetAlignment[0], Vec3.SIZEOF * 5);
        gl4.glBindTexture(GL_TEXTURE_BUFFER, 0);

        return true;
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(1, vertexArrayName, 0);
        gl4.glBindVertexArray(vertexArrayName[0]);
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 0, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
        }
        gl4.glBindVertexArray(0);

        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        {
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
            ByteBuffer pointer = gl4.glMapBufferRange(GL_UNIFORM_BUFFER, 0, Mat4.SIZEOF,
                    GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            projection.perspectiveFov((float) Math.PI * 0.25f, windowSize.x, windowSize.y, 0.1f, 100.0f)
                    .mul(viewMat4()).mul(model.identity());

            pointer.asFloatBuffer().put(projection.toFA(new float[16]));

            // Make sure the uniform buffer is uploaded
            gl4.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        gl4.glViewportIndexedf(0, 0, 0, windowSize.x, windowSize.y);

        float[] depth = {1.0f};
        gl4.glClearBufferfv(GL_DEPTH, 0, depth, 0);
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, 0);

        gl4.glUseProgram(programName);

        gl4.glActiveTexture(GL_TEXTURE0);
        gl4.glBindTexture(GL_TEXTURE_BUFFER, textureName[Texture.DISPLACEMENT]);

        gl4.glActiveTexture(GL_TEXTURE1);
        gl4.glBindTexture(GL_TEXTURE_BUFFER, textureName[Texture.DIFFUSE]);

        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.TRANSFORM]);

        gl4.glBindVertexArray(vertexArrayName[0]);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT]);
        gl4.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 5, 0);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteTextures(Texture.MAX, textureName, 0);
        gl4.glDeleteBuffers(Buffer.MAX, bufferName, 0);
        gl4.glDeleteProgram(programName);
        gl4.glDeleteVertexArrays(1, vertexArrayName, 0);

        return true;
    }
}
