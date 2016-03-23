/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_320.primitive;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import static com.jogamp.opengl.GL3ES3.*;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import dev.Vec4u8;
import framework.BufferUtils;
import glm.glm;
import glm.mat._4.Mat4;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import glf.Vertex_v2fc4ub;
import glm.vec._2.Vec2;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 *
 * @author GBarbieri
 */
public class Gl_320_primitive_shading extends Test {

    public static void main(String[] args) {
        Gl_320_primitive_shading gl_320_primitive_shading = new Gl_320_primitive_shading();
    }

    public Gl_320_primitive_shading() {
        super("gl-320-primitive-shading", Profile.CORE, 3, 2);
    }

    private final String SHADERS_SOURCE = "primitive-shading";
    private final String SHADERS_ROOT = "src/data/gl_320/primitive";

    private int vertexCount = 4;
    private int vertexSize = vertexCount * Vertex_v2fc4ub.SIZE;
    private Vertex_v2fc4ub[] vertexData = {
        new Vertex_v2fc4ub(new Vec2(-1.0f, -1.0f), new Vec4u8(255, 0, 0, 255)),
        new Vertex_v2fc4ub(new Vec2(+1.0f, -1.0f), new Vec4u8(255, 255, 255, 255)),
        new Vertex_v2fc4ub(new Vec2(+1.0f, +1.0f), new Vec4u8(0, 255, 0, 255)),
        new Vertex_v2fc4ub(new Vec2(-1.0f, +1.0f), new Vec4u8(0, 0, 255, 255))};

    private int colorCount = 3;
    private int colorSize = colorCount * 4 * Float.BYTES;
    private float[] colorData = {
        0.5f, 0.5f, 0.5f, 1.0f,
        0.7f, 0.7f, 0.7f, 1.0f,
        0.3f, 0.3f, 0.3f, 1.0f};

    private int elementCount = 6;
    private int elementSize = elementCount * Short.BYTES;
    private short[] elementData = {
        0, 1, 2,
        2, 3, 0};

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int ELEMENT = 1;
        public static final int TRANSFORM = 2;
        public static final int CONSTANT = 3;
        public static final int MAX = 4;
    }

    private int programName;
    private IntBuffer vertexArrayName = GLBuffers.newDirectIntBuffer(1),
            bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX), queryName = GLBuffers.newDirectIntBuffer(1);
    private FloatBuffer clearColor = GLBuffers.newDirectFloatBuffer(new float[]{0.0f, 0.0f, 0.0f, 1.0f});

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

        if (validated) {

            ShaderCode vertShaderCode = ShaderCode.create(gl3, GL_VERTEX_SHADER, this.getClass(), SHADERS_ROOT, null,
                    SHADERS_SOURCE, "vert", null, true);
            ShaderCode geomShaderCode = ShaderCode.create(gl3, GL_GEOMETRY_SHADER, this.getClass(), SHADERS_ROOT, null,
                    SHADERS_SOURCE, "geom", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl3, GL_FRAGMENT_SHADER, this.getClass(), SHADERS_ROOT, null,
                    SHADERS_SOURCE, "frag", null, true);

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

            gl3.glUseProgram(programName);
            gl3.glUniformBlockBinding(programName, gl3.glGetUniformBlockIndex(programName, "Transform"),
                    Semantic.Uniform.TRANSFORM0);
            gl3.glUniformBlockBinding(programName, gl3.glGetUniformBlockIndex(programName, "Constant"),
                    Semantic.Uniform.CONSTANT);
            gl3.glUseProgram(0);
        }

        return validated & checkError(gl3, "initProgram");
    }

    private boolean initVertexArray(GL3 gl3) {

        gl3.glGenVertexArrays(1, vertexArrayName);
        gl3.glBindVertexArray(vertexArrayName.get(0));
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, Vertex_v2fc4ub.SIZE, 0);
            gl3.glVertexAttribPointer(Semantic.Attr.COLOR, 4, GL_UNSIGNED_BYTE, true, Vertex_v2fc4ub.SIZE, Vec2.SIZE);
            gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl3.glEnableVertexAttribArray(Semantic.Attr.COLOR);
            gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT));
        }
        gl3.glBindVertexArray(0);

        return checkError(gl3, "initVertexArray");
    }

    private boolean initBuffer(GL3 gl3) {

        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        ByteBuffer vertexBuffer = GLBuffers.newDirectByteBuffer(vertexSize);
        IntBuffer uniformBufferOffset = GLBuffers.newDirectIntBuffer(1);
        FloatBuffer colorBuffer = GLBuffers.newDirectFloatBuffer(colorData);

        gl3.glGenBuffers(Buffer.MAX, bufferName);

        gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT));
        gl3.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
        for (int i = 0; i < vertexCount; i++) {
            vertexData[i].toBb(vertexBuffer, i);
        }
        vertexBuffer.rewind();
        gl3.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        gl3.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset);
        int uniformBlockSize = Math.max(Mat4.SIZE, uniformBufferOffset.get(0));

        gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.TRANSFORM));
        gl3.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_DYNAMIC_DRAW);
        gl3.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.CONSTANT));
        gl3.glBufferData(GL_UNIFORM_BUFFER, colorSize, colorBuffer, GL_STATIC_DRAW);
        gl3.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        BufferUtils.destroyDirectBuffer(elementBuffer);
        BufferUtils.destroyDirectBuffer(vertexBuffer);
        BufferUtils.destroyDirectBuffer(uniformBufferOffset);
        BufferUtils.destroyDirectBuffer(colorBuffer);

        return checkError(gl3, "initBuffer");
    }

    private boolean initQuery(GL3 gl3) {

        gl3.glGenQueries(1, queryName);

        int[] queryBits = {0};
        gl3.glGetQueryiv(GL_PRIMITIVES_GENERATED, GL_QUERY_COUNTER_BITS, queryBits, 0);

        boolean validated = queryBits[0] >= 32;

        return validated && checkError(gl3, "initQuery");
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

            pointer.asFloatBuffer().put(projection.mul(viewMat4()).mul(model).toFa_());

            // Make sure the uniform buffer is uploaded
            gl3.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        gl3.glViewport(0, 0, windowSize.x, windowSize.y);
        gl3.glClearBufferfv(GL_COLOR, 0, clearColor);

        gl3.glUseProgram(programName);

        gl3.glBindVertexArray(vertexArrayName.get(0));
        gl3.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName.get(Buffer.TRANSFORM));
        gl3.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.CONSTANT, bufferName.get(Buffer.CONSTANT));

        gl3.glBeginQuery(GL_PRIMITIVES_GENERATED, queryName.get(0));
        {
            gl3.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0);
        }
        gl3.glEndQuery(GL_PRIMITIVES_GENERATED);

        long[] primitivesGenerated = {0};
        gl3.glGetQueryObjectui64v(queryName.get(0), GL_QUERY_RESULT, primitivesGenerated, 0);

        return primitivesGenerated[0] > 0;
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;

        gl3.glDeleteBuffers(Buffer.MAX, bufferName);
        gl3.glDeleteVertexArrays(1, vertexArrayName);
        gl3.glDeleteProgram(programName);
        gl3.glDeleteQueries(1, queryName);

        BufferUtils.destroyDirectBuffer(bufferName);
        BufferUtils.destroyDirectBuffer(vertexArrayName);
        BufferUtils.destroyDirectBuffer(queryName);

        BufferUtils.destroyDirectBuffer(clearColor);

        return checkError(gl3, "end");
    }
}
