/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_500;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_DYNAMIC_DRAW;
import static com.jogamp.opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TRUE;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER_BIT;
import static com.jogamp.opengl.GL2ES2.GL_PROGRAM_SEPARABLE;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER_BIT;
import static com.jogamp.opengl.GL2ES3.GL_STREAM_COPY;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT;
import static com.jogamp.opengl.GL2GL3.GL_EXTERNAL_VIRTUAL_MEMORY_BUFFER_AMD;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.BufferUtils;
import framework.Profile;
import framework.Test;
import glf.Vertex_v2fv2f;
import glm.mat._4.Mat4;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 *
 * @author GBarbieri
 */
public class Gl_500_buffer_pinned_amd extends Test {

    public static void main(String[] args) {
        Gl_500_buffer_pinned_amd gl_500_buffer_pinned_amd = new Gl_500_buffer_pinned_amd();
    }

    public Gl_500_buffer_pinned_amd() {
        super("gl-500-buffer-pinned-amd", Profile.CORE, 4, 5);
    }

    private final String SHADERS_SOURCE = "buffer-pinned-amd";
    private final String SHADERS_ROOT = "src/data/gl_500";
    private final String TEXTURE_DIFFUSE = "kueken7_rgba8_srgb.dds";

    private int vertexCount = 4;
    private int vertexSize = vertexCount * Vertex_v2fv2f.SIZE;
    private float[] vertexData = {
        -1.0f, -1.0f,/**/ 0.0f, 1.0f,
        +1.0f, -1.0f,/**/ 1.0f, 1.0f,
        +1.0f, +1.0f,/**/ 1.0f, 0.0f,
        -1.0f, +1.0f,/**/ 0.0f, 0.0f};

    private int elementCount = 6;
    private int elementSize = elementCount * Short.BYTES;
    private short[] elementData = {
        0, 1, 2,
        2, 3, 0};

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int ELEMENT = 1;
        public static final int TRANSFORM = 2;
        public static final int MAX = 3;
    }
    
    private final int PAGE_SIZE=4096;

    private IntBuffer pipelineName = GLBuffers.newDirectIntBuffer(1),
            vertexArrayName = GLBuffers.newDirectIntBuffer(1), textureName = GLBuffers.newDirectIntBuffer(1),
            bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX);
    private int programName;
    private ByteBuffer clientBufferAddress;

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

//		Viewport[texture::RGB8] = glm::ivec4(0, 0, WindowSize >> 1);
//		Viewport[texture::R] = glm::ivec4(WindowSize.x >> 1, 0, WindowSize >> 1);
//		Viewport[texture::G] = glm::ivec4(WindowSize.x >> 1, WindowSize.y >> 1, WindowSize >> 1);
//		Viewport[texture::B] = glm::ivec4(0, WindowSize.y >> 1, WindowSize >> 1);
        boolean validated = true;
        validated = validated && checkExtension(gl4, "GL_AMD_pinned_memory");

//		if(validated)
//			validated = initBlend();
//		if(validated)
//			validated = initProgram();
//		if(validated)
//			validated = initBuffer();
//		if(validated)
//			validated = initVertexArray();
//		if(validated)
//			validated = initTexture();
//		if(validated)
//			validated = initFramebuffer();
        return validated && checkError(gl4, "begin");
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        if (validated) {

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER, this.getClass(), SHADERS_ROOT, null, 
                    SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER, this.getClass(), SHADERS_ROOT, null, 
                    SHADERS_SOURCE, "frag", null, true);

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
    
    private boolean initBuffer(GL4 gl4)	{
        
		boolean validated=true;
                
                
		IntBuffer uniformBufferOffset=GLBuffers.newDirectIntBuffer(1);
                ShortBuffer elementBuffer=GLBuffers.newDirectShortBuffer(elementData);

		gl4.glGenBuffers(Buffer.MAX, bufferName);

//		gl4.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset);
		int uniformBlockSize = Math.max(Mat4.SIZE, uniformBufferOffset.get(0));

		gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.TRANSFORM));
		gl4.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_DYNAMIC_DRAW);
		gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

		gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT));
		gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
		gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

//		void* Pointer = initMemoryBuffer();

		gl4.glBindBuffer(GL_EXTERNAL_VIRTUAL_MEMORY_BUFFER_AMD, bufferName.get(Buffer.VERTEX));
//		gl4.glBufferData(GL_EXTERNAL_VIRTUAL_MEMORY_BUFFER_AMD, vertexSize, Pointer, GL_STREAM_COPY);
		gl4.glBindBuffer(GL_EXTERNAL_VIRTUAL_MEMORY_BUFFER_AMD, 0);
		gl4.glBindBuffer(GL_EXTERNAL_VIRTUAL_MEMORY_BUFFER_AMD, 0);
                
                BufferUtils.destroyDirectBuffer(uniformBufferOffset);

		return validated;
	}
    
    	private ByteBuffer initMemoryBuffer()	{
		clientBufferAddress = GLBuffers.newDirectByteBuffer(vertexSize + PAGE_SIZE - 1);
//		glm::byte* UnalignAddress = this->ClientBufferAddress.get();
//		glm::byte* AlignAddress = align(UnalignAddress, PAGE_SIZE);
//		memcpy(AlignAddress, VertexData, VertexSize);
//		return reinterpret_cast<void*>(AlignAddress);
            return null;
	}
}
