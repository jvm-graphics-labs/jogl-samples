/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl320.primitive;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_DYNAMIC_DRAW;
import static com.jogamp.opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_MAP_INVALIDATE_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_MAP_WRITE_BIT;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNSIGNED_BYTE;
import static com.jogamp.opengl.GL.GL_UNSIGNED_SHORT;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_QUERY_COUNTER_BITS;
import static com.jogamp.opengl.GL2ES2.GL_QUERY_RESULT;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import static com.jogamp.opengl.GL2ES3.GL_PRIMITIVES_GENERATED;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT;
import com.jogamp.opengl.GL3;
import static com.jogamp.opengl.GL3ES3.GL_GEOMETRY_SHADER;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.Semantic;
import framework.Test;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
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
        super("gl-320-primitive-shading", 3, 2);
    }

    private final String SHADERS_SOURCE = "primitive-shading";
    private final String SHADERS_ROOT = "src/data/gl_320/primitive";

    private int vertexCount = 4;
    private int vertexSize = vertexCount * (2 * Float.BYTES + 4 * Byte.BYTES);
    private float[] vertexV2fData = {
        -1.0f, -1.0f,
        +1.0f, -1.0f,
        +1.0f, +1.0f,
        -1.0f, +1.0f};
    private byte[] vertexV4ubData = {
        (byte) 255, (byte) 0, (byte) 0, (byte) 255,
        (byte) 255, (byte) 255, (byte) 255, (byte) 255,
        (byte) 0, (byte) 255, (byte) 0, (byte) 255,
        (byte) 0, (byte) 0, (byte) 255, (byte) 255};

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

    private enum Buffer {
        VERTEX,
        ELEMENT,
        TRANSFORM,
        CONSTANT,
        MAX
    }

    private int programName;
    private int[] vertexArrayName = new int[1], bufferName = new int[Buffer.MAX.ordinal()], queryName = new int[1];
    private float[] projection = new float[16], model = new float[16];

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

            ShaderCode vertShaderCode = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            ShaderCode geomShaderCode = ShaderCode.create(gl3, GL_GEOMETRY_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "geom", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

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
            gl3.glUniformBlockBinding(programName,
                    gl3.glGetUniformBlockIndex(programName, "Transform"), Semantic.Uniform.TRANSFORM0);
            gl3.glUniformBlockBinding(programName,
                    gl3.glGetUniformBlockIndex(programName, "Constant"), Semantic.Uniform.CONSTANT);
            gl3.glUseProgram(0);
        }

        return validated & checkError(gl3, "initProgram");
    }

    private boolean initVertexArray(GL3 gl3) {

        gl3.glGenVertexArrays(1, vertexArrayName, 0);
        gl3.glBindVertexArray(vertexArrayName[0]);
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 2 * Float.BYTES + 4 * Byte.BYTES, 0);
            gl3.glVertexAttribPointer(Semantic.Attr.COLOR, 4, GL_UNSIGNED_BYTE, true,
                    2 * Float.BYTES + 4 * Byte.BYTES, 2 * Float.BYTES);
            gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl3.glEnableVertexAttribArray(Semantic.Attr.COLOR);
            gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT.ordinal()]);
        }
        gl3.glBindVertexArray(0);

        return checkError(gl3, "initVertexArray");
    }

    private boolean initBuffer(GL3 gl3) {

        gl3.glGenBuffers(Buffer.MAX.ordinal(), bufferName, 0);

        gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT.ordinal()]);
        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        gl3.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);
        ByteBuffer vertexBuffer = GLBuffers.newDirectByteBuffer(vertexSize);
        for (int i = 0; i < vertexCount; i++) {
            vertexBuffer.putFloat(vertexV2fData[i * 2 + 0]);
            vertexBuffer.putFloat(vertexV2fData[i * 2 + 1]);
            vertexBuffer.put(vertexV4ubData[i * 4 + 0]);
            vertexBuffer.put(vertexV4ubData[i * 4 + 1]);
            vertexBuffer.put(vertexV4ubData[i * 4 + 2]);
            vertexBuffer.put(vertexV4ubData[i * 4 + 3]);
        }
        vertexBuffer.rewind();
        gl3.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        int[] uniformBufferOffset = {0};
        gl3.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset, 0);
        int uniformBlockSize = Math.max(16 * Float.BYTES, uniformBufferOffset[0]);

        gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM.ordinal()]);
        gl3.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_DYNAMIC_DRAW);
        gl3.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.CONSTANT.ordinal()]);
        FloatBuffer colorBuffer = GLBuffers.newDirectFloatBuffer(colorData);
        gl3.glBufferData(GL_UNIFORM_BUFFER, colorSize, colorBuffer, GL_STATIC_DRAW);
        gl3.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return checkError(gl3, "initBuffer");
    }

    private boolean initQuery(GL3 gl3) {

        gl3.glGenQueries(1, queryName, 0);

        int[] queryBits = {0};
        gl3.glGetQueryiv(GL_PRIMITIVES_GENERATED, GL_QUERY_COUNTER_BITS, queryBits, 0);

        boolean validated = queryBits[0] >= 32;

        return validated && checkError(gl3, "initQuery");
    }

    @Override
    protected boolean render(GL gl) {

        GL3 gl3 = (GL3) gl;

        {
            gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM.ordinal()]);
            ByteBuffer pointer = gl3.glMapBufferRange(
                    GL_UNIFORM_BUFFER, 0, 16 * Float.BYTES,
                    GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            FloatUtil.makePerspective(projection, 0, true, (float) Math.PI * 0.25f, 4.0f / 3.0f, 0.1f, 100.0f);
            FloatUtil.makeIdentity(model);
            FloatUtil.multMatrix(projection, view());
            FloatUtil.multMatrix(projection, model);

            for (float f : projection) {
                pointer.putFloat(f);
            }
            pointer.rewind();

            // Make sure the uniform buffer is uploaded
            gl3.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        gl3.glViewport(0, 0, windowSize.x, windowSize.y);
        gl3.glClearBufferfv(GL_COLOR, 0, new float[]{0.0f, 0.0f, 0.0f, 1.0f}, 0);

        gl3.glUseProgram(programName);

        gl3.glBindVertexArray(vertexArrayName[0]);
        gl3.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.TRANSFORM.ordinal()]);
        gl3.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.CONSTANT, bufferName[Buffer.CONSTANT.ordinal()]);

        gl3.glBeginQuery(GL_PRIMITIVES_GENERATED, queryName[0]);
        {
            gl3.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0);
        }
        gl3.glEndQuery(GL_PRIMITIVES_GENERATED);

        long[] primitivesGenerated = {0};
        gl3.glGetQueryObjectui64v(queryName[0], GL_QUERY_RESULT, primitivesGenerated, 0);

        return primitivesGenerated[0] > 0;
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;

        gl3.glDeleteBuffers(Buffer.MAX.ordinal(),  bufferName,0);
        gl3.glDeleteVertexArrays(1,  vertexArrayName, 0);
        gl3.glDeleteProgram(programName);

        return checkError(gl3, "end");
    }
}
