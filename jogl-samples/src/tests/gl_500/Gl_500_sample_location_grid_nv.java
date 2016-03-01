/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_500;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_MAP_WRITE_BIT;
import static com.jogamp.opengl.GL.GL_NEAREST;
import static com.jogamp.opengl.GL.GL_RGBA8;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_TRUE;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_PROGRAM_SEPARABLE;
import static com.jogamp.opengl.GL2ES2.GL_TEXTURE_2D_MULTISAMPLE;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_BASE_LEVEL;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_MAX_LEVEL;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import glm.mat._4.Mat4;
import glm.vec._2.Vec2;
import framework.BufferUtils;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import glf.Vertex_v2fv2f;
import glm.vec._2.i.Vec2i;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 *
 * @author GBarbieri
 */
public class Gl_500_sample_location_grid_nv extends Test {

    public static void main(String[] args) {
        Gl_500_sample_location_grid_nv gl_500_sample_location_grid_nv = new Gl_500_sample_location_grid_nv();
    }

    public Gl_500_sample_location_grid_nv() {
        super("gl-500-sample-location-grid-nv", Profile.CORE, 4, 5);
    }

    private final String SHADERS_SOURCE_TEXTURE = "sample-location-render";
    private final String SHADERS_SOURCE_SPLASH = "sample-location-splash";
    private final String SHADERS_ROOT = "src/data/gl_500";

    // With DDS textures, v texture coordinate are reversed, from top to bottom
    private int _vertexCount = 4;
    private int vertexSize = _vertexCount * glf.Vertex_v2fv2f.SIZE;
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

    private class Texture {

        public static final int COLORBUFFER = 0;
        public static final int RENDERBUFFER = 1;
        public static final int MAX = 2;
    }

    private class Framebuffer {

        public static final int COLORBUFFER = 0;
        public static final int RENDERBUFFER0 = 1;
        public static final int RENDERBUFFER1 = 2;
        public static final int RENDERBUFFER2 = 3;
        public static final int RENDERBUFFER3 = 4;
        public static final int MAX = 5;
    }

    private class Program {

        public static final int TEXTURE = 0;
        public static final int SPLASH = 1;
        public static final int MAX = 2;
    }

    private class Shader {

        public static final int VERT_TEXTURE = 0;
        public static final int FRAG_TEXTURE = 1;
        public static final int VERT_SPLASH = 2;
        public static final int FRAG_SPLASH = 3;
        public static final int MAX = 4;
    }

    private IntBuffer vertexArrayName = GLBuffers.newDirectIntBuffer(Program.MAX),
            bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX), textureName = GLBuffers.newDirectIntBuffer(Texture.MAX),
            framebufferName = GLBuffers.newDirectIntBuffer(Framebuffer.MAX);
    private int[] programName = new int[Program.MAX], uniformDiffuse = new int[Program.MAX];
    private int vertexCount, framebufferScale = 3, uniformTransform;
    /**
     * https://jogamp.org/bugzilla/show_bug.cgi?id=1287
     */
    private boolean bug1287 = true;

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        ShaderCode[] shaderCodes = new ShaderCode[Shader.MAX];

        if (validated) {

            shaderCodes[Shader.VERT_TEXTURE] = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_TEXTURE, "vert", null, true);
            shaderCodes[Shader.FRAG_TEXTURE] = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_TEXTURE, "frag", null, true);

            ShaderProgram shaderProgram = new ShaderProgram();
            shaderProgram.init(gl4);

            programName[Program.TEXTURE] = shaderProgram.program();

            gl4.glProgramParameteri(programName[Program.TEXTURE], GL_PROGRAM_SEPARABLE, GL_TRUE);

            shaderProgram.add(shaderCodes[Shader.VERT_TEXTURE]);
            shaderProgram.add(shaderCodes[Shader.FRAG_TEXTURE]);

            gl4.glBindAttribLocation(programName[Program.TEXTURE], Semantic.Attr.POSITION, "Position");
            gl4.glBindAttribLocation(programName[Program.TEXTURE], Semantic.Attr.TEXCOORD, "Texcoord");
            gl4.glBindFragDataLocation(programName[Program.TEXTURE], Semantic.Frag.COLOR, "Color");

            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            shaderCodes[Shader.VERT_SPLASH] = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_SPLASH, "vert", null, true);
            shaderCodes[Shader.FRAG_SPLASH] = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_SPLASH, "frag", null, true);

            ShaderProgram shaderProgram = new ShaderProgram();
            shaderProgram.init(gl4);

            programName[Program.SPLASH] = shaderProgram.program();

            gl4.glProgramParameteri(programName[Program.SPLASH], GL_PROGRAM_SEPARABLE, GL_TRUE);

            shaderProgram.add(shaderCodes[Shader.VERT_SPLASH]);
            shaderProgram.add(shaderCodes[Shader.FRAG_SPLASH]);

            gl4.glBindFragDataLocation(programName[Program.SPLASH], Semantic.Frag.COLOR, "Color");

            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            uniformTransform = gl4.glGetUniformBlockIndex(programName[Program.TEXTURE], "transform");
            uniformDiffuse[Program.TEXTURE] = gl4.glGetUniformLocation(programName[Program.TEXTURE], "Diffuse");
            uniformDiffuse[Program.SPLASH] = gl4.glGetUniformLocation(programName[Program.SPLASH], "Diffuse");

            gl4.glUseProgram(programName[Program.TEXTURE]);
            gl4.glUniform1i(uniformDiffuse[Program.TEXTURE], 0);
            gl4.glUniformBlockBinding(programName[Program.TEXTURE], uniformTransform, Semantic.Uniform.TRANSFORM0);

            gl4.glUseProgram(programName[Program.SPLASH]);
            gl4.glUniform1i(uniformDiffuse[Program.SPLASH], 0);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        IntBuffer uniformBufferOffset = GLBuffers.newDirectIntBuffer(1);
        gl4.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset);
        int uniformBlockSize = Math.max(Mat4.SIZE, uniformBufferOffset.get(0));
        BufferUtils.destroyDirectBuffer(uniformBufferOffset);

        Vec2[] data = new Vec2[36];
        for (int i = 0; i < data.length; ++i) {
            float angle = (float) Math.PI * 2.0f * i / data.length;
            data[i] = new Vec2((float) Math.sin(angle), (float) Math.cos(angle)).normalize();
        }
        vertexCount = 18;//static_cast<GLsizei>(Data.size() - 8);

        gl4.glCreateBuffers(Buffer.MAX, bufferName);

        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        FloatBuffer dataBuffer = GLBuffers.newDirectFloatBuffer(data.length * Vec2.SIZE);
        for (Vec2 data1 : data) {
            dataBuffer.put(data1.x).put(data1.y);
        }
        dataBuffer.rewind();

        if (bug1287) {

            gl4.glNamedBufferStorage(bufferName.get(Buffer.ELEMENT), elementSize, elementBuffer, 0);
            gl4.glNamedBufferStorage(bufferName.get(Buffer.VERTEX), data.length * Vec2.SIZE, dataBuffer, 0);
            gl4.glNamedBufferStorage(bufferName.get(Buffer.TRANSFORM), uniformBlockSize, null, GL_MAP_WRITE_BIT);

        } else {

            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT));
            gl4.glBufferStorage(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, 0);
            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
            gl4.glBufferStorage(GL_ARRAY_BUFFER, vertexSize, dataBuffer, 0);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.TRANSFORM));
            gl4.glBufferStorage(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_MAP_WRITE_BIT);
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }

        return true;
    }

    private boolean initTexture(GL4 gl4) {

        boolean validated = true;

        Vec2i windowSizeF = new Vec2i(windowSize).shiftR(framebufferScale);

        textureName.position(Texture.COLORBUFFER);
        gl4.glCreateTextures(GL_TEXTURE_2D, 1, textureName);
        gl4.glTextureParameteri(textureName.get(Texture.COLORBUFFER), GL_TEXTURE_BASE_LEVEL, 0);
        gl4.glTextureParameteri(textureName.get(Texture.COLORBUFFER), GL_TEXTURE_MAX_LEVEL, 0);
        gl4.glTextureParameteri(textureName.get(Texture.COLORBUFFER), GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        gl4.glTextureParameteri(textureName.get(Texture.COLORBUFFER), GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        gl4.glTextureStorage2D(textureName.get(Texture.COLORBUFFER), 1, GL_RGBA8, windowSizeF.x, windowSizeF.y);

        textureName.position(Texture.RENDERBUFFER);
        gl4.glCreateTextures(GL_TEXTURE_2D_MULTISAMPLE, 1, textureName);
        gl4.glTextureParameteri(textureName.get(Texture.RENDERBUFFER), GL_TEXTURE_BASE_LEVEL, 0);
        gl4.glTextureParameteri(textureName.get(Texture.RENDERBUFFER), GL_TEXTURE_MAX_LEVEL, 0);
        gl4.glTextureStorage2DMultisample(textureName.get(Texture.RENDERBUFFER), 4, GL_RGBA8, windowSizeF.x,
                windowSizeF.y, true);

        textureName.rewind();

        return validated;
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glCreateVertexArrays(Program.MAX, vertexArrayName);

        gl4.glVertexArrayAttribBinding(vertexArrayName.get(Program.TEXTURE), Semantic.Attr.POSITION, 0);
        gl4.glVertexArrayAttribFormat(vertexArrayName.get(Program.TEXTURE), Semantic.Attr.POSITION, 2, GL_FLOAT, false, 0);
        gl4.glEnableVertexArrayAttrib(vertexArrayName.get(Program.TEXTURE), Semantic.Attr.POSITION);

        gl4.glVertexArrayAttribBinding(vertexArrayName.get(Program.TEXTURE), Semantic.Attr.TEXCOORD, 0);
        gl4.glVertexArrayAttribFormat(vertexArrayName.get(Program.TEXTURE), Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false,
                Vec2.SIZE);
        gl4.glEnableVertexArrayAttrib(vertexArrayName.get(Program.TEXTURE), Semantic.Attr.TEXCOORD);

        gl4.glVertexArrayElementBuffer(vertexArrayName.get(Program.TEXTURE), bufferName.get(Buffer.ELEMENT));
        gl4.glVertexArrayVertexBuffer(vertexArrayName.get(Program.TEXTURE), 0, bufferName.get(Buffer.VERTEX), 0,
                Vertex_v2fv2f.SIZE);

        return true;
    }
    
    private boolean initFramebuffer(GL4 gl4)	{
        
//		Vec2[] sampleLocations = new ;
//
//		static glm::vec2 SamplesPositions16[] =
//		{
//			glm::vec2( 1.f,  0.f) / 16.f,
//			glm::vec2( 4.f,  1.f) / 16.f,
//			glm::vec2( 3.f,  6.f) / 16.f,
//			glm::vec2( 7.f,  5.f) / 16.f,
//			glm::vec2( 8.f,  1.f) / 16.f,
//			glm::vec2(11.f,  3.f) / 16.f,
//			glm::vec2(12.f,  7.f) / 16.f,
//			glm::vec2(15.f,  4.f) / 16.f,
//			glm::vec2( 0.f,  8.f) / 16.f,
//			glm::vec2( 5.f, 10.f) / 16.f,
//			glm::vec2( 2.f, 12.f) / 16.f,
//			glm::vec2( 6.f, 14.f) / 16.f,
//			glm::vec2( 9.f,  9.f) / 16.f,
//			glm::vec2(13.f, 11.f) / 16.f,
//			glm::vec2(10.f, 13.f) / 16.f,
//			glm::vec2(14.f, 15.f) / 16.f
//		};
//
//		GLint SubPixelBits(0);
//		glm::ivec2 PixelGrid(0);
//		GLint TableSize(0);
//
//		glGetIntegerv(GL_SAMPLE_LOCATION_SUBPIXEL_BITS_NV, &SubPixelBits);
//		glGetIntegerv(GL_SAMPLE_LOCATION_PIXEL_GRID_WIDTH_NV, &PixelGrid.x);
//		glGetIntegerv(GL_SAMPLE_LOCATION_PIXEL_GRID_HEIGHT_NV, &PixelGrid.y);
//		glGetIntegerv(GL_PROGRAMMABLE_SAMPLE_LOCATION_TABLE_SIZE_NV, &TableSize);
//
//		glGenFramebuffers(framebuffer::MAX, &FramebufferName[0]);
//
//		for(int FramebufferIndex = 0; FramebufferIndex < 4; ++FramebufferIndex)
//		{
//			glBindFramebuffer(GL_FRAMEBUFFER, FramebufferName[framebuffer::RENDERBUFFER0 + FramebufferIndex]);
//			glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, TextureName[texture::RENDERBUFFER], 0);
//			glFramebufferParameteri(GL_FRAMEBUFFER, GL_FRAMEBUFFER_PROGRAMMABLE_SAMPLE_LOCATIONS_NV, FramebufferIndex == 0 ? GL_FALSE : GL_TRUE);
//			glFramebufferParameteri(GL_FRAMEBUFFER, GL_FRAMEBUFFER_SAMPLE_LOCATION_PIXEL_GRID_NV, GL_TRUE);
//			gl4.glFramebufferSampleLocationsfvNV(GL_FRAMEBUFFER, 0, TableSize, &SamplesPositions16[0][0]);
//		}
//
//		glBindFramebuffer(GL_FRAMEBUFFER, FramebufferName[texture::COLORBUFFER]);
//		glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, TextureName[texture::COLORBUFFER], 0);
//
//		for(int FramebufferIndex = 0; FramebufferIndex < framebuffer::MAX; ++FramebufferIndex)
//			if(!this->checkFramebuffer(FramebufferName[FramebufferIndex]))
//				return false;
//
//		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		return true;
	}
}
