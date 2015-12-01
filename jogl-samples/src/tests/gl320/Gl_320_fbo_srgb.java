/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl320;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_CLAMP_TO_EDGE;
import static com.jogamp.opengl.GL.GL_DEPTH_COMPONENT24;
import static com.jogamp.opengl.GL.GL_DYNAMIC_DRAW;
import static com.jogamp.opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_LINEAR;
import static com.jogamp.opengl.GL.GL_LINEAR_MIPMAP_LINEAR;
import static com.jogamp.opengl.GL.GL_RGBA;
import static com.jogamp.opengl.GL.GL_SRGB8_ALPHA8;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_S;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_T;
import static com.jogamp.opengl.GL.GL_UNPACK_ALIGNMENT;
import static com.jogamp.opengl.GL.GL_UNSIGNED_BYTE;
import static com.jogamp.opengl.GL2ES2.GL_DEPTH_COMPONENT;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_BASE_LEVEL;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_MAX_LEVEL;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_SWIZZLE_A;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_SWIZZLE_B;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_SWIZZLE_G;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_SWIZZLE_R;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.Semantic;
import framework.Test;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 *
 * @author GBarbieri
 */
public class Gl_320_fbo_srgb extends Test {

    public static void main(String[] args) {
        Gl_320_fbo_srgb gl_320_fbo_srgb = new Gl_320_fbo_srgb();
    }

    public Gl_320_fbo_srgb() {
        super("Gl-320-fbo-srgb", 3, 2);
    }

    private final String SHADER_SOURCE_TEXTURE = "fbo-srgb";
    private final String SHADER_SOURCE_SPLASH = "fbo-srgb-blit";
    private final String SHADERS_ROOT = "src/data/gl_320";
    private final String TEXTURE_DIFFUSE = "kueken7_rgba8_srgb.dds";

    private int vertexCount = 4;
    private int vertexSize = vertexCount * 2 * 2 * Float.BYTES;
    private float[] vertexData = {
        -1.0f, -1.0f, 0.0f, 1.0f,
        +1.0f, -1.0f, 1.0f, 1.0f,
        +1.0f, +1.0f, 1.0f, 0.0f,
        -1.0f, +1.0f, 0.0f, 0.0f};

    private int elementCount = 6;
    private int elementSize = elementCount * Short.BYTES;
    short[] elementData = {
        0, 1, 2,
        2, 3, 0};

    private enum Buffer {
        VERTEX,
        ELEMENT,
        TRANSFORM,
        MAX
    }

    private enum Texture {
        DIFFUSE,
        COLORBUFFER,
        RENDERBUFFER,
        MAX
    }

    private enum Program {
        TEXTURE,
        SPLASH,
        MAX
    }

    private enum Shader {
        VERT_TEXTURE,
        FRAG_TEXTURE,
        VERT_SPLASH,
        FRAG_SPLASH,
        MAX
    }

    private int[] programName = new int[Program.MAX.ordinal()], vertexArrayName = new int[Program.MAX.ordinal()],
            bufferName = new int[Buffer.MAX.ordinal()], textureName = new int[Texture.MAX.ordinal()],
            uniformDiffuse = new int[Program.MAX.ordinal()], framebufferName = new int[1];
    private int framebufferSize = 2, uniformTransform;

    private boolean initProgram(GL3 gl3) {

        boolean validated = true;

        ShaderCode[] shaderCodes = new ShaderCode[Shader.MAX.ordinal()];

        if (validated) {

            shaderCodes[Shader.VERT_TEXTURE.ordinal()] = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADER_SOURCE_TEXTURE, "vert", null, true);
            shaderCodes[Shader.FRAG_TEXTURE.ordinal()] = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADER_SOURCE_TEXTURE, "frag", null, true);

            ShaderProgram program = new ShaderProgram();
            programName[Program.TEXTURE.ordinal()] = program.program();

            program.add(shaderCodes[Shader.VERT_TEXTURE.ordinal()]);
            program.add(shaderCodes[Shader.FRAG_TEXTURE.ordinal()]);

            program.init(gl3);

            programName[Program.TEXTURE.ordinal()] = program.program();

            gl3.glBindAttribLocation(programName[Program.TEXTURE.ordinal()], Semantic.Attr.POSITION, "Position");
            gl3.glBindAttribLocation(programName[Program.TEXTURE.ordinal()], Semantic.Attr.TEXCOORD, "Texcoord");
            gl3.glBindFragDataLocation(programName[Program.TEXTURE.ordinal()], Semantic.Frag.COLOR, "Color");

            program.link(gl3, System.out);
        }

        if (validated) {

            shaderCodes[Shader.VERT_SPLASH.ordinal()] = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADER_SOURCE_SPLASH, "vert", null, true);
            shaderCodes[Shader.FRAG_SPLASH.ordinal()] = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADER_SOURCE_SPLASH, "frag", null, true);

            ShaderProgram program = new ShaderProgram();
            programName[Program.SPLASH.ordinal()] = program.program();

            program.add(shaderCodes[Shader.VERT_SPLASH.ordinal()]);
            program.add(shaderCodes[Shader.FRAG_SPLASH.ordinal()]);

            program.init(gl3);

            programName[Program.SPLASH.ordinal()] = program.program();

            gl3.glBindFragDataLocation(programName[Program.SPLASH.ordinal()], Semantic.Frag.COLOR, "Color");

            program.link(gl3, System.out);
        }

        if (validated) {

            uniformTransform = gl3.glGetUniformBlockIndex(programName[Program.TEXTURE.ordinal()], "transform");
            uniformDiffuse[Program.TEXTURE.ordinal()]
                    = gl3.glGetUniformLocation(programName[Program.TEXTURE.ordinal()], "Diffuse");
            uniformDiffuse[Program.SPLASH.ordinal()]
                    = gl3.glGetUniformLocation(programName[Program.SPLASH.ordinal()], "Diffuse");

            gl3.glUseProgram(programName[Program.TEXTURE.ordinal()]);
            gl3.glUniform1i(uniformDiffuse[Program.TEXTURE.ordinal()], 0);
            gl3.glUniformBlockBinding(programName[Program.TEXTURE.ordinal()], uniformTransform,
                    Semantic.Uniform.TRANSFORM0);

            gl3.glUseProgram(programName[Program.SPLASH.ordinal()]);
            gl3.glUniform1i(uniformDiffuse[Program.SPLASH.ordinal()], 0);
        }

        return validated & checkError(gl3, "initProgram");
    }

    private boolean initBuffer(GL3 gl3) {

        gl3.glGenBuffers(Buffer.MAX.ordinal(), bufferName, 0);

        gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT.ordinal()]);
        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        gl3.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl3.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        int[] uniformBufferOffset = {0};
        gl3.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset, 0);
        int uniformBlockSize = Math.max(16 * Float.BYTES, uniformBufferOffset[0]);

        gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM.ordinal()]);
        gl3.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_DYNAMIC_DRAW);
        gl3.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return true;
    }
    
    private boolean initTexture(GL3 gl3)	{
        
		boolean validated=true;

//		gli::gl GL;
//
//		{
//			//gli::texture2D Texture(gli::SRGB8, gli::dim2_t(16));
//			//Texture.clear(glm::u8vec3(glm::convertRgbToSrgb(glm::vec4(1.0f, 0.5f, 0.0f, 1.0f)) * 255.0f));
//			gli::texture2D Texture(gli::FORMAT_RGBA8_SRGB, gli::dim2_t(16));
//			Texture.clear(glm::u8vec4(glm::convertLinearToSRGB(glm::vec4(1.0f, 0.5f, 0.0f, 1.0f)) * 255.0f));
//			//gli::texture2D Texture(gli::RGBA8_UNORM, gli::dim2_t(16));
//			//Texture.clear(glm::u8vec4(glm::vec4(1.0f, 0.5f, 0.0f, 1.0f) * 255.0f));
//			gli::save_dds(Texture, "orange.dds");
//		}

		jgli.Texture texture=jgli.Load.load(TEXTURE_ROOT+"/"+ TEXTURE_DIFFUSE);
		jgli.Gl.Format format = jgli.Gl.instance.translate(texture.format());
		jgli.Gl.Swizzles swizzles = jgli.Gl.instance.translate(texture.swizzles());

//		std::size_t Levels = texture.levels();

		//gli::texture2D Texture(gli::load_dds((getDataDirectory() + TEXTURE_DIFFUSE).c_str()));
		assert(!texture.empty());

		gl3.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

		gl3.glGenTextures(Texture.MAX.ordinal(), textureName,0);

		gl3.glActiveTexture(GL_TEXTURE0);
		gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.DIFFUSE.ordinal()]);
		gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
		gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, texture.levels() - 1);
		gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_R, swizzles.r.value);
		gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_G, swizzles.g.value);
		gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_B, swizzles.b.value);
		gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_A, swizzles.a.value);
		gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

		glm::vec4 Data2(glm::convertLinearToSRGB(glm::vec4(1.0f, 0.5f, 0.0f, 1.0f)) * 255.f);
		glm::vec4 Data(glm::convertLinearToSRGB(glm::vec4(0.0f, 0.5f, 1.0f, 1.0f)));
		gl3.glTexImage2D(GL_TEXTURE_2D, 0,
			GL_SRGB8_ALPHA8,
			1, 1, 0,
			GL_RGBA, GL_FLOAT,
			&Data[0]);
/*
		for (gli::texture2D::size_type Level = 0; Level < Texture.levels(); ++Level)
		{
			glTexImage2D(GL_TEXTURE_2D, static_cast<GLint>(Level),
				GL.internal_format(Texture.format()),
				static_cast<GLsizei>(Texture[Level].dimensions().x), static_cast<GLsizei>(Texture[Level].dimensions().y), 0,
				GL.external_format(Texture.format()), GL.type_format(Texture.format()),
				Texture[Level].data());
		}
*/
		glm::ivec2 WindowSize(this->getWindowSize() * this->FramebufferScale);

		gl3.glActiveTexture(GL_TEXTURE0);
		gl3.glBindTexture(GL_TEXTURE_2D, TextureName[texture::COLORBUFFER]);
		gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
		gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
		gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		gl3.glTexImage2D(GL_TEXTURE_2D, GLint(0), GL_SRGB8_ALPHA8, GLsizei(WindowSize.x), GLsizei(WindowSize.y), 0, GL_RGBA, GL_UNSIGNED_BYTE, nullptr);

		gl3.glActiveTexture(GL_TEXTURE0);
		gl3.glBindTexture(GL_TEXTURE_2D, TextureName[texture::RENDERBUFFER]);
		gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
		gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
		gl3.glTexImage2D(GL_TEXTURE_2D, GLint(0), GL_DEPTH_COMPONENT24, GLsizei(WindowSize.x), GLsizei(WindowSize.y), 0, GL_DEPTH_COMPONENT, GL_FLOAT, nullptr);

		gl3.glPixelStorei(GL_UNPACK_ALIGNMENT, 4);

		return validated;
	}
}
