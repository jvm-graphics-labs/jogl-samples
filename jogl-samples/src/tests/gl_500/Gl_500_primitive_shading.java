/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_500;

import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_TRUE;
import static com.jogamp.opengl.GL.GL_UNSIGNED_BYTE;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER_BIT;
import static com.jogamp.opengl.GL2ES2.GL_PROGRAM_SEPARABLE;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER_BIT;
import static com.jogamp.opengl.GL2ES3.GL_GEOMETRY_SHADER_BIT;
import static com.jogamp.opengl.GL3ES3.GL_GEOMETRY_SHADER;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import glm.mat._4.Mat4;
import glm.vec._2.Vec2;
import dev.Vec4u8;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import glf.Vertex_v2fc4ub;

/**
 *
 * @author GBarbieri
 */
public class Gl_500_primitive_shading extends Test {

    public static void main(String[] args) {
        Gl_500_primitive_shading Gl_500_primitive_shading = new Gl_500_primitive_shading();
    }

    public Gl_500_primitive_shading() {
        super("gl-500-primitive-shading", Profile.CORE, 4, 5);
    }

    private final String SHADERS_SOURCE = "primitive-bindless-nv";
    private final String SHADERS_ROOT = "src/data/gl_500";
    private final String TEXTURE_DIFFUSE = "kueken7_rgba8_srgb.dds";

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
    private int[] vertexArrayName = {0}, bufferName = new int[Buffer.MAX], queryName = {0}, pipelineName = {0};

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

            gl4.glGenProgramPipelines(1, pipelineName, 0);
            gl4.glUseProgramStages(pipelineName[0], GL_VERTEX_SHADER_BIT | GL_GEOMETRY_SHADER_BIT
                    | GL_FRAGMENT_SHADER_BIT, programName);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glCreateVertexArrays(1, vertexArrayName, 0);

        gl4.glVertexArrayAttribBinding(vertexArrayName[0], Semantic.Attr.POSITION, Semantic.Buffer.STATIC);
        gl4.glVertexArrayAttribFormat(vertexArrayName[0], Semantic.Attr.POSITION, 2, GL_FLOAT, false, 0);
        gl4.glEnableVertexArrayAttrib(vertexArrayName[0], Semantic.Attr.POSITION);

        gl4.glVertexArrayAttribBinding(vertexArrayName[0], Semantic.Attr.COLOR, Semantic.Buffer.STATIC);
        gl4.glVertexArrayAttribFormat(vertexArrayName[0], Semantic.Attr.COLOR, 4, GL_UNSIGNED_BYTE, true, Vec2.SIZE);
        gl4.glEnableVertexArrayAttrib(vertexArrayName[0], Semantic.Attr.COLOR);

        gl4.glVertexArrayElementBuffer(vertexArrayName[0], bufferName[Buffer.ELEMENT]);
        gl4.glVertexArrayVertexBuffer(vertexArrayName[0], 0, bufferName[Buffer.VERTEX], 0, glf.Vertex_v2fc4ub.SIZE);

        return true;
    }

    private boolean initBuffer(GL4 gl4) {

//		int[] uniformBufferOffset={0};
//		gl4.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset,0);
//		int uniformBlockSize = Math.max(Mat4.SIZE, uniformBufferOffset[0]);
//
//		gl4.glCreateBuffers(Buffer.MAX, bufferName, 0);
//		gl4.glNamedBufferStorage(bufferName[Buffer.TRANSFORM], uniformBlockSize, nullptr, GL_MAP_WRITE_BIT);
//		gl4.glNamedBufferStorage(BufferName[buffer::ELEMENT], ElementSize, ElementData, 0);
//		gl4.glNamedBufferStorage(BufferName[buffer::VERTEX], VertexSize, VertexData, 0);
        return true;
    }
}
