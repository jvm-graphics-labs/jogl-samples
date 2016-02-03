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
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT;
import static com.jogamp.opengl.GL3ES3.GL_SHADER_STORAGE_BUFFER;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import dev.Mat4;
import framework.BufferUtils;
import framework.Profile;
import framework.Test;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 *
 * @author GBarbieri
 */
public class Gl_500_texture_sparse_arb extends Test {

    public static void main(String[] args) {
        Gl_500_texture_sparse_arb gl_500_texture_sparse_arb = new Gl_500_texture_sparse_arb();
    }

    public Gl_500_texture_sparse_arb() {
        super("gl-500-texture-sparse-arb", Profile.CORE, 4, 5);
    }

    private final String SHADERS_SOURCE = "texture-sparse";
    private final String SHADERS_ROOT = "src/data/gl_500";

    // With DDS textures, v texture coordinate are reversed, from top to bottom
    private int vertexCount = 4;
    private int vertexSize = vertexCount * glf.Vertex_v2fv2f.SIZE;
    private float[] vertexData = {
        -1.0f, -1.0f,/**/ 0.0f, 1.0f,
        +1.0f, -1.0f,/**/ 1.0f, 1.0f,
        +1.0f, +1.0f,/**/ 1.0f, 0.0f
        - 1.0f, +1.0f,/**/ 0.0f, 0.0f};

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

    private int[] bufferName = new int[Buffer.MAX], pipelineName = {0}, textureName = {0}, vertexArrayName = {0};
    private int programName;

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = checkExtension(gl4, "GL_ARB_sparse_texture");

        /*
		glm::vec2 const WindowSize(this->getWindowSize());
		glm::vec2 const WindowRange = WindowSize * 3.f;

		this->Viewports.resize(1000);
		for (std::size_t i = 0; i < this->Viewports.size(); ++i)
		{
			glm::vec2 const ViewportPos(i % 17u, i % 13u);
			glm::vec2 const ViewportSize(i % 11u);
			this->Viewports[i] = glm::vec4(ViewportPos / glm::vec2(17, 13) * WindowRange - WindowSize, ViewportSize / glm::vec2(11));
		}
         */
//		glm::vec2 WindowSize(this->getWindowSize());
//		this->Viewports.resize(1000);
//		for (std::size_t i = 0; i < this->Viewports.size(); ++i)
//		{
//			this->Viewports[i] = glm::vec4(
//				glm::linearRand(-WindowSize.x, WindowSize.x * 2.0f), 
//				glm::linearRand(-WindowSize.y, WindowSize.y * 2.0f),
//				WindowSize * glm::linearRand(0.0f, 1.0f));
//		}
//
//		if(Validated)
//			Validated = initProgram();
//		if(Validated)
//			Validated = initBuffer();
//		if(Validated)
//			Validated = initVertexArray();
//		if(Validated)
//			Validated = initTexture();
//		if(Validated)
//			Validated = initFramebuffer();
        return validated;
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        gl4.glGenProgramPipelines(1, pipelineName, 0);

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

            gl4.glUseProgramStages(pipelineName[0], GL_VERTEX_SHADER_BIT | GL_FRAGMENT_SHADER_BIT, programName);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        gl4.glGenBuffers(Buffer.MAX, bufferName, 0);

        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT]);
        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(elementBuffer);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_SHADER_STORAGE_BUFFER, bufferName[Buffer.VERTEX]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl4.glBufferData(GL_SHADER_STORAGE_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(vertexBuffer);
        gl4.glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);

        int[] uniformBufferOffset = {0};
        gl4.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset, 0);

        int uniformBlockSize = Math.max(Mat4.SIZE, uniformBufferOffset[0]);

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
        gl4.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_DYNAMIC_DRAW);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return true;
    }
    
    private boolean initTexture(GL4 gl4)	{
        
		gl4.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

		int size=16384;
		std::size_t const Levels = gli::levels(size);
		std::size_t const MaxLevels = 4;

		gl4.glGenTextures(1, &TextureName);
		gl4.glActiveTexture(GL_TEXTURE0);
		gl4.glBindTexture(GL_TEXTURE_2D_ARRAY, TextureName);
		gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SWIZZLE_R, GL_RED);
		gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SWIZZLE_G, GL_GREEN);
		gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SWIZZLE_B, GL_BLUE);
		gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SWIZZLE_A, GL_ALPHA);
		gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_BASE_LEVEL, 0);
		gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAX_LEVEL, MaxLevels - 1);
		gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_NEAREST);
		gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		gl4.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SPARSE_ARB, GL_TRUE);
		gl4.glTexStorage3D(GL_TEXTURE_2D_ARRAY, static_cast<GLsizei>(gli::levels(size)), GL_RGBA8, GLsizei(size), GLsizei(size), 1);

		glm::ivec3 PageSize;
		gl4.glGetInternalformativ(GL_TEXTURE_2D_ARRAY, GL_RGBA8, GL_VIRTUAL_PAGE_SIZE_X_ARB, 1, &PageSize.x);
		gl4.glGetInternalformativ(GL_TEXTURE_2D_ARRAY, GL_RGBA8, GL_VIRTUAL_PAGE_SIZE_Y_ARB, 1, &PageSize.y);
		gl4.glGetInternalformativ(GL_TEXTURE_2D_ARRAY, GL_RGBA8, GL_VIRTUAL_PAGE_SIZE_Z_ARB, 1, &PageSize.z);

		std::vector<glm::u8vec4> Page;
		Page.resize(static_cast<std::size_t>(PageSize.x * PageSize.y * PageSize.z));

		GLint Page3DSizeX(0);
		GLint Page3DSizeY(0);
		GLint Page3DSizeZ(0);
		gl4.glGetInternalformativ(GL_TEXTURE_3D, GL_RGBA32F, GL_VIRTUAL_PAGE_SIZE_X_ARB, 1, &Page3DSizeX);
		gl4.glGetInternalformativ(GL_TEXTURE_3D, GL_RGBA32F, GL_VIRTUAL_PAGE_SIZE_Y_ARB, 1, &Page3DSizeY);
		gl4.glGetInternalformativ(GL_TEXTURE_3D, GL_RGBA32F, GL_VIRTUAL_PAGE_SIZE_Z_ARB, 1, &Page3DSizeZ);

		for(std::size_t Level = 0; Level < MaxLevels; ++Level)
		{
			GLsizei LevelSize = (size >> Level);
			GLsizei TileCountY = LevelSize / PageSize.y;
			GLsizei TileCountX = LevelSize / PageSize.x;

			for(GLsizei j = 0; j < TileCountY; ++j)
			for(GLsizei i = 0; i < TileCountX; ++i)
			{
				if(glm::abs(glm::length(glm::vec2(i, j) / glm::vec2(TileCountX, TileCountY) * 2.0f - 1.0f)) > 1.0f)
					continue;

				std::fill(Page.begin(), Page.end(), glm::u8vec4(
					static_cast<unsigned char>(float(i) / float(LevelSize / PageSize.x) * 255),
					static_cast<unsigned char>(float(j) / float(LevelSize / PageSize.y) * 255),
					static_cast<unsigned char>(float(Level) / float(MaxLevels) * 255), 255));

		gl4.		glTexPageCommitmentARB(GL_TEXTURE_2D_ARRAY, static_cast<GLint>(Level),
					static_cast<GLsizei>(PageSize.x) * i, static_cast<GLsizei>(PageSize.y) * j, 0,
					static_cast<GLsizei>(PageSize.x), static_cast<GLsizei>(PageSize.y), 1,
					GL_TRUE);

		gl4.		glTexSubImage3D(GL_TEXTURE_2D_ARRAY, static_cast<GLint>(Level),
					static_cast<GLsizei>(PageSize.x) * i, static_cast<GLsizei>(PageSize.y) * j, 0,
					static_cast<GLsizei>(PageSize.x), static_cast<GLsizei>(PageSize.y), 1,
					GL_RGBA, GL_UNSIGNED_BYTE,
					&Page[0][0]);
			}
		}

		gl4.glPixelStorei(GL_UNPACK_ALIGNMENT, 4);

		return true;
	}
}
