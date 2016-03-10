/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_500;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL3ES3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import glm.mat._4.Mat4;
import glm.vec._2.Vec2;
import dev.Vec4u8;
import framework.BufferUtils;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import glf.Vertex_v2fc4ub;
import glm.glm;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

/**
 *
 * @author GBarbieri
 */
public class Gl_500_primitive_shading_nv extends Test {

    public static void main(String[] args) {
        Gl_500_primitive_shading_nv gl_500_primitive_shading_nv = new Gl_500_primitive_shading_nv();
    }

    public Gl_500_primitive_shading_nv() {
        super("gl-500-primitive-shading", Profile.CORE, 4, 5);
    }

    private final String SHADERS_SOURCE = "primitive-shading-nv";
    private final String SHADERS_ROOT = "src/data/gl_500";

    // With DDS textures, v texture coordinate are reversed, from top to bottom
    private int vertexCount = 6;
    private int vertexSize = vertexCount * glf.Vertex_v2fc4ub.SIZE;
    private Vertex_v2fc4ub[] vertexData = {
        new Vertex_v2fc4ub(new Vec2(-1.0f, -1.0f), new Vec4u8(255, 0, 0, 255)),
        new Vertex_v2fc4ub(new Vec2(+1.0f, -1.0f), new Vec4u8(255, 255, 255, 255)),
        new Vertex_v2fc4ub(new Vec2(+1.0f, +1.0f), new Vec4u8(0, 255, 0, 255)),
        new Vertex_v2fc4ub(new Vec2(-1.0f, +1.0f), new Vec4u8(0, 0, 255, 255))};

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
            bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX), queryName = GLBuffers.newDirectIntBuffer(1),
            pipelineName = GLBuffers.newDirectIntBuffer(1);
    private FloatBuffer black = GLBuffers.newDirectFloatBuffer(new float[]{0.0f, 0.0f, 0.0f, 1.0f});
    /**
     * https://jogamp.org/bugzilla/show_bug.cgi?id=1287
     */
    private boolean bug1287 = true;

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;

        validated = validated && checkExtension(gl4, "GL_NV_geometry_shader_passthrough");
        
        if (validated) {
            validated = initQuery(gl4);
        }
        if (validated) {
            validated = initProgram(gl4);
        }
        if (validated) {
            validated = initBuffer(gl4);
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
            ShaderCode geomShaderCode = ShaderCode.create(gl4, GL_GEOMETRY_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "geom", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

            ShaderProgram shaderProgram = new ShaderProgram();
            shaderProgram.init(gl4);

            programName = shaderProgram.program();

            gl4.glProgramParameteri(programName, GL_PROGRAM_SEPARABLE, GL_TRUE);

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(geomShaderCode);
            shaderProgram.add(fragShaderCode);
            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            gl4.glGenProgramPipelines(1, pipelineName);
            gl4.glUseProgramStages(pipelineName.get(0), GL_VERTEX_SHADER_BIT | GL_GEOMETRY_SHADER_BIT
                    | GL_FRAGMENT_SHADER_BIT, programName);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glCreateVertexArrays(1, vertexArrayName);

        gl4.glVertexArrayAttribBinding(vertexArrayName.get(0), Semantic.Attr.POSITION, Semantic.Buffer.STATIC);
        gl4.glVertexArrayAttribFormat(vertexArrayName.get(0), Semantic.Attr.POSITION, 2, GL_FLOAT, false, 0);
        gl4.glEnableVertexArrayAttrib(vertexArrayName.get(0), Semantic.Attr.POSITION);

        gl4.glVertexArrayAttribBinding(vertexArrayName.get(0), Semantic.Attr.COLOR, Semantic.Buffer.STATIC);
        gl4.glVertexArrayAttribFormat(vertexArrayName.get(0), Semantic.Attr.COLOR, 4, GL_UNSIGNED_BYTE, true, Vec2.SIZE);
        gl4.glEnableVertexArrayAttrib(vertexArrayName.get(0), Semantic.Attr.COLOR);

        gl4.glVertexArrayElementBuffer(vertexArrayName.get(0), bufferName.get(Buffer.ELEMENT));
        gl4.glVertexArrayVertexBuffer(vertexArrayName.get(0), 0, bufferName.get(Buffer.VERTEX), 0, Vertex_v2fc4ub.SIZE);

        return true;
    }

    private boolean initBuffer(GL4 gl4) {

        IntBuffer uniformBufferOffset = GLBuffers.newDirectIntBuffer(1);
        gl4.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset);
        int uniformBlockSize = Math.max(Mat4.SIZE, uniformBufferOffset.get(0));
        BufferUtils.destroyDirectBuffer(uniformBufferOffset);

        gl4.glCreateBuffers(Buffer.MAX, bufferName);

        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        ByteBuffer vertexBuffer = GLBuffers.newDirectByteBuffer(vertexSize);
        for (int i = 0; i < vertexCount; i++) {
            vertexData[i].toBb(vertexBuffer, i);
        }
        vertexBuffer.rewind();

        if (bug1287) {

            gl4.glNamedBufferStorage(bufferName.get(Buffer.TRANSFORM), uniformBlockSize, null, GL_MAP_WRITE_BIT);
            gl4.glNamedBufferStorage(bufferName.get(Buffer.ELEMENT), elementSize, elementBuffer, 0);
            gl4.glNamedBufferStorage(bufferName.get(Buffer.VERTEX), vertexSize, vertexBuffer, 0);

        } else {

            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.TRANSFORM));
            gl4.glBufferStorage(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_MAP_WRITE_BIT);
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);
            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT));
            gl4.glBufferStorage(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, 0);
            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
            gl4.glBufferStorage(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, 0);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);
        }

        return true;
    }

    private boolean initQuery(GL4 gl4) {

        gl4.glGenQueries(1, queryName);

        IntBuffer queryBitsBuffer = GLBuffers.newDirectIntBuffer(1);
        gl4.glGetQueryiv(GL_PRIMITIVES_GENERATED, GL_QUERY_COUNTER_BITS, queryBitsBuffer);
        int queryBits = queryBitsBuffer.get(0);
        BufferUtils.destroyDirectBuffer(queryBitsBuffer);

        return queryBits >= 32;
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        {
            ByteBuffer pointer = gl4.glMapNamedBufferRange(bufferName.get(Buffer.TRANSFORM),
                    0, Mat4.SIZE * 1, GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, 4.0f / 3.0f, 0.1f, 100.0f);
            Mat4 model = new Mat4(1.0f);

            pointer.asFloatBuffer().put(projection.mul(viewMat4()).mul(model).toFa_());

            // Make sure the uniform buffer is uploaded
            gl4.glUnmapNamedBuffer(bufferName.get(Buffer.TRANSFORM));
        }

        gl4.glViewport(0, 0, windowSize.x, windowSize.y);
        gl4.glClearBufferfv(GL_COLOR, 0, black);

        gl4.glBindProgramPipeline(pipelineName.get(0));
        gl4.glBindVertexArray(vertexArrayName.get(0));
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName.get(Buffer.TRANSFORM));

        gl4.glBeginQuery(GL_PRIMITIVES_GENERATED, queryName.get(0));
        {
            gl4.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0);
        }
        gl4.glEndQuery(GL_PRIMITIVES_GENERATED);

        LongBuffer primitivesGeneratedBuffer = GLBuffers.newDirectLongBuffer(1);
        gl4.glGetQueryObjectui64v(queryName.get(0), GL_QUERY_RESULT, primitivesGeneratedBuffer);
        long primitivesGenerated = primitivesGeneratedBuffer.get(0);

        return primitivesGenerated > 0;
    }
}
