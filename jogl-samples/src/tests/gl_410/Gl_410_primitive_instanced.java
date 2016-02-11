/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_410;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL3ES3.*;
import com.jogamp.opengl.GL4;
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
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import glm.vec._2.Vec2;

/**
 *
 * @author GBarbieri
 */
public class Gl_410_primitive_instanced extends Test {

    public static void main(String[] args) {
        Gl_410_primitive_instanced gl_410_primitive_instanced = new Gl_410_primitive_instanced();
    }

    public Gl_410_primitive_instanced() {
        super("gl-410-primitive-instanced", Profile.CORE, 4, 1, new Vec2(Math.PI * 0.3f));
    }

    private final String SHADERS_SOURCE = "primitive-instancing";
    private final String SHADERS_ROOT = "src/data/gl_410";

    private int vertexCount = 4;
    private int vertexSize = vertexCount * Vertex_v2fc4ub.SIZE;
    private Vertex_v2fc4ub[] vertexData = {
        new Vertex_v2fc4ub(new Vec2(-1.0f, -1.0f), new Vec4u8(255, 0, 0, 255)),
        new Vertex_v2fc4ub(new Vec2(+1.0f, -1.0f), new Vec4u8(255, 255, 0, 255)),
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
        public static final int MAX = 2;
    }

    private class Program {

        public static final int VERT = 0;
        public static final int FRAG = 1;
        public static final int MAX = 2;
    }

    private int[] pipelineName = {0}, programName = new int[Program.MAX], bufferName = new int[Buffer.MAX],
            vertexArrayName = {0};
    private int uniformMvp, uniformDiffuse;

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;

        if (validated) {
            validated = initProgram(gl4);
        }
        if (validated) {
            validated = initVertexBuffer(gl4);
        }
        if (validated) {
            validated = initVertexArray(gl4);
        }

        gl4.glEnable(GL_DEPTH_TEST);

        return validated;
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        gl4.glGenProgramPipelines(1, pipelineName, 0);

        // Create program
        if (validated) {

            ShaderProgram vertProgram = new ShaderProgram();
            ShaderProgram fragProgram = new ShaderProgram();

            ShaderCode vertexShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            ShaderCode geometryShaderCode = ShaderCode.create(gl4, GL_GEOMETRY_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "geom", null, true);
            ShaderCode fragmentShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

            vertProgram.init(gl4);
            fragProgram.init(gl4);

            vertProgram.add(vertexShaderCode);
            vertProgram.add(geometryShaderCode);
            fragProgram.add(fragmentShaderCode);

            programName[Program.VERT] = vertProgram.program();
            programName[Program.FRAG] = fragProgram.program();

            gl4.glProgramParameteri(programName[Program.VERT], GL_PROGRAM_SEPARABLE, GL_TRUE);
            gl4.glProgramParameteri(programName[Program.FRAG], GL_PROGRAM_SEPARABLE, GL_TRUE);

            vertProgram.link(gl4, System.out);
            fragProgram.link(gl4, System.out);
        }

        if (validated) {

            gl4.glUseProgramStages(pipelineName[0], GL_VERTEX_SHADER_BIT | GL_GEOMETRY_SHADER_BIT,
                    programName[Program.VERT]);
            gl4.glUseProgramStages(pipelineName[0], GL_FRAGMENT_SHADER_BIT, programName[Program.FRAG]);

        }

        // Get variables locations
        if (validated) {

            uniformMvp = gl4.glGetUniformLocation(programName[Program.VERT], "mvp");
            uniformDiffuse = gl4.glGetUniformLocation(programName[Program.FRAG], "diffuse");
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(1, vertexArrayName, 0);
        gl4.glBindVertexArray(vertexArrayName[0]);
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, Vertex_v2fc4ub.SIZE, 0);
            gl4.glVertexAttribPointer(Semantic.Attr.COLOR, 4, GL_UNSIGNED_BYTE, true, Vertex_v2fc4ub.SIZE, Vec2.SIZE);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl4.glEnableVertexAttribArray(Semantic.Attr.COLOR);
        }
        gl4.glBindVertexArray(0);

        return checkError(gl4, "initVertexArray");
    }

    private boolean initVertexBuffer(GL4 gl4) {

        gl4.glGenBuffers(Buffer.MAX, bufferName, 0);

        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT]);
        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(elementBuffer);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
        ByteBuffer vertexBuffer = GLBuffers.newDirectByteBuffer(vertexSize);
        for (int i = 0; i < vertexCount; i++) {
            vertexData[i].toBb(vertexBuffer, i);
        }
        vertexBuffer.rewind();
        gl4.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(vertexBuffer);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        return checkError(gl4, "initArrayBuffer");
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        // Compute the MVP (Model View Projection matrix)
        Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, (float) windowSize.x / windowSize.y, 0.1f, 100.0f);
        Mat4 model = new Mat4(1.0f);
        Mat4 mvp = projection.mul(viewMat4()).mul(model);

        // Set the value of uniforms
        gl4.glProgramUniformMatrix4fv(programName[Program.VERT], uniformMvp, 1, false, mvp.toFa_(), 0);
        gl4.glProgramUniform4fv(programName[Program.FRAG],
                uniformDiffuse, 1, new float[]{1.0f, 0.5f, 0.0f, 1.0f}, 0);

        // Set the display viewport
        gl4.glViewportIndexedfv(0, new float[]{0, 0, windowSize.x, windowSize.y}, 0);

        // Clear color buffer with white
        float[] depth = {1.0f};
        gl4.glClearBufferfv(GL_DEPTH, 0, depth, 0);
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, 0);

        // Bind program
        gl4.glBindProgramPipeline(pipelineName[0]);

        // Bind vertex array & draw 
        gl4.glBindVertexArray(vertexArrayName[0]);
        // Must be called after glBindVertexArray
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT]);
        gl4.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteBuffers(Buffer.MAX, bufferName, 0);
        gl4.glDeleteVertexArrays(1, vertexArrayName, 0);
        gl4.glDeleteProgram(programName[Program.VERT]);
        gl4.glDeleteProgram(programName[Program.FRAG]);
        gl4.glDeleteProgramPipelines(1, pipelineName, 0);

        return true;
    }
}
