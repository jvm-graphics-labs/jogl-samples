/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_BACK;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_FRAMEBUFFER;
import static com.jogamp.opengl.GL.GL_FRAMEBUFFER_COMPLETE;
import static com.jogamp.opengl.GL.GL_MAP_FLUSH_EXPLICIT_BIT;
import static com.jogamp.opengl.GL.GL_MAP_INVALIDATE_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_MAP_UNSYNCHRONIZED_BIT;
import static com.jogamp.opengl.GL.GL_MAP_WRITE_BIT;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.Semantic;
import framework.Test;
import java.nio.ByteBuffer;

/**
 *
 * @author gbarbieri
 */
public class Gl_320_buffer_update extends Test {

    public static void main(String[] args) {
        Gl_320_buffer_update gl_320_buffer_update = new Gl_320_buffer_update();
    }

    public Gl_320_buffer_update() {
        super("gl-320-buffer-update", 3, 2);
    }

    private final String SHADERS_SOURCE = "buffer-update";
    private final String SHADERS_ROOT = "src/data/gl_320";

    private int vertexCount = 6;
    private int positionSize = vertexCount * 2 * GLBuffers.SIZEOF_FLOAT;
    private float[] positionData = new float[]{
        -1.0f, -1.0f,
        +1.0f, -1.0f,
        +1.0f, +1.0f,
        +1.0f, +1.0f,
        -1.0f, +1.0f,
        -1.0f, -1.0f,};

    private enum Buffer {

        array, copy, material, transform, max
    }

    private int[] bufferName = new int[Buffer.max.ordinal()], vertexArrayName = new int[1];
    private int programName, uniformTransform, uniformMaterial;

    @Override
    protected boolean begin(GL gl) {

        boolean validated = true;

        GL3 gl3 = (GL3) gl;

        if (validated) {
            validated = initProgram(gl3);
        }
        if (validated) {
//            validated = initBuffer(gl3);
        }
        if (validated) {
//            validated = initVertexArray(gl3);
        }
        gl3.glEnable(GL_DEPTH_TEST);
        gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        gl3.glDrawBuffer(GL_BACK);
        if (gl3.glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            return false;
        }
        return validated;
    }

    private boolean initProgram(GL3 gl3) {

        boolean validated = true;
        // Create program
        if (validated) {
            ShaderCode vertShader = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragShader = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

            ShaderProgram program = new ShaderProgram();
            program.add(vertShader);
            program.add(fragShader);

            program.init(gl3);

            programName = program.program();

            gl3.glBindAttribLocation(programName, Semantic.Attr.position, "Position");
            gl3.glBindAttribLocation(programName, Semantic.Attr.color, "Color");

            program.link(gl3, System.out);
        }
        // Get variables locations
        if (validated) {
            uniformTransform = gl3.glGetUniformBlockIndex(programName, "transform");
            uniformMaterial = gl3.glGetUniformBlockIndex(programName, "material");

            gl3.glUniformBlockBinding(programName, uniformTransform, Semantic.Uniform.transform0);
            gl3.glUniformBlockBinding(programName, uniformMaterial, Semantic.Uniform.material);
        }

        return validated & checkError(gl3, "initProgram");
    }

    // Buffer update using glMapBufferRange
    private boolean initBuffer(GL3 gl3) {
        // Generate a buffer object
        gl3.glGenBuffers(Buffer.max.ordinal(), bufferName, 0);

        // Bind the buffer for use
        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.array.ordinal()]);

        // Reserve buffer memory but don't copy the values
        gl3.glBufferData(
                GL_ARRAY_BUFFER,
                positionSize,
                null,
                GL_STATIC_DRAW);

		// Copy the vertex data in the buffer, in this sample for the whole range of data.
        // It doesn't required to be the buffer size but pointers require no memory overlapping.
        ByteBuffer data = gl3.glMapBufferRange(
                GL_ARRAY_BUFFER,
                0, // Offset
                positionSize, // Size,
                GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT | GL_MAP_UNSYNCHRONIZED_BIT | GL_MAP_FLUSH_EXPLICIT_BIT);
		
//        for(int f)
//
//		// Explicitly send the data to the graphic card.
//		glFlushMappedBufferRange(GL_ARRAY_BUFFER, 0, PositionSize);
//
//		glUnmapBuffer(GL_ARRAY_BUFFER);
//
//		// Unbind the buffer
//		glBindBuffer(GL_ARRAY_BUFFER, 0);
//
//		// Copy buffer
//		glBindBuffer(GL_ARRAY_BUFFER, BufferName[buffer::COPY]);
//		glBufferData(GL_ARRAY_BUFFER, PositionSize, 0, GL_STATIC_DRAW);
//		glBindBuffer(GL_ARRAY_BUFFER, 0);
//
//		glBindBuffer(GL_COPY_READ_BUFFER, BufferName[buffer::ARRAY]);
//		glBindBuffer(GL_COPY_WRITE_BUFFER, BufferName[buffer::COPY]);
//
//		glCopyBufferSubData(
//			GL_COPY_READ_BUFFER, GL_COPY_WRITE_BUFFER,
//			0, 0,
//			PositionSize);
//
//		glBindBuffer(GL_COPY_READ_BUFFER, 0);
//		glBindBuffer(GL_COPY_WRITE_BUFFER, 0);
//
//		GLint UniformBlockSize = 0;
//
//		{
//			glGetActiveUniformBlockiv(
//				ProgramName, 
//				UniformTransform,
//				GL_UNIFORM_BLOCK_DATA_SIZE,
//				&UniformBlockSize);
//
//			glBindBuffer(GL_UNIFORM_BUFFER, BufferName[buffer::TRANSFORM]);
//			glBufferData(GL_UNIFORM_BUFFER, UniformBlockSize, 0, GL_DYNAMIC_DRAW);
//			glBindBuffer(GL_UNIFORM_BUFFER, 0);
//		}
//
//		{
//			glm::vec4 Diffuse(1.0f, 0.5f, 0.0f, 1.0f);
//
//			glGetActiveUniformBlockiv(
//				ProgramName, 
//				UniformMaterial,
//				GL_UNIFORM_BLOCK_DATA_SIZE,
//				&UniformBlockSize);
//
//			glBindBuffer(GL_UNIFORM_BUFFER, BufferName[buffer::MATERIAL]);
//			glBufferData(GL_UNIFORM_BUFFER, UniformBlockSize, &Diffuse[0], GL_DYNAMIC_DRAW);
//			glBindBuffer(GL_UNIFORM_BUFFER, 0);
//		}
//
//		return this->checkError("initBuffer");
    }
}
