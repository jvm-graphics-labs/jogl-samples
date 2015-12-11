/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl320.texture;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.Semantic;
import framework.Test;
import jgli.Format;
import jgli.Target;
import jgli.Texture;
import jglm.Vec2;
import jglm.Vec4i;

/**
 *
 * @author GBarbieri
 */
public class Gl_320_texture_cube extends Test {

    public static void main(String[] args) {
        Gl_320_texture_cube gl_320_texture_cube = new Gl_320_texture_cube();
    }

    public Gl_320_texture_cube() {
        super("gl-320-texture-cube", 3, 2, new Vec2((float) Math.PI * 0.2f, (float) Math.PI * 0.2f));
    }

    private final String SHADERS_SOURCE = "texture-cube";
    private final String SHADERS_ROOT = "src/data/gl_320/texture";

    // With DDS textures, v texture coordinate are reversed, from top to bottom
    private int vertexCount = 6;
    private int vertexSize = vertexCount * 2 * Float.BYTES;
    private float[] vertexData = {
        -1.0f, -1.0f,
        +1.0f, -1.0f,
        +1.0f, +1.0f,
        +1.0f, +1.0f,
        -1.0f, +1.0f,
        -1.0f, -1.0f};

    private enum Shader {
        VERT,
        FRAG,
        MAX
    }

    private int[] shaderName = new int[Shader.MAX.ordinal()], vertexArrayName = new int[1], bufferName = new int[1],
            textureName = new int[1];
    private int programName, uniformMv, uniformMvp, uniformEnvironment, uniformCamera;
    private Vec4i[] viewport;

    private boolean initProgram(GL3 gl3) {

        boolean validated = true;

        if (validated) {

            ShaderCode vertShaderCode = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

            ShaderProgram shaderProgram = new ShaderProgram();
            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);

            shaderProgram.init(gl3);

            programName = shaderProgram.program();

            gl3.glBindAttribLocation(programName, Semantic.Attr.POSITION, "Position");
            gl3.glBindFragDataLocation(programName, Semantic.Frag.COLOR, "Color");

            shaderProgram.link(gl3, System.out);
        }
        if (validated) {

            uniformMv = gl3.glGetUniformLocation(programName, "MV");
            uniformMvp = gl3.glGetUniformLocation(programName, "MVP");
            uniformEnvironment = gl3.glGetUniformLocation(programName, "Environment");
            uniformCamera = gl3.glGetUniformLocation(programName, "Camera");
        }

        return validated & checkError(gl3, "initProgram");
    }

    private boolean initBuffer(GL3 gl3) {

        gl3.glGenBuffers(1, bufferName, 0);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[0]);
        gl3.glBufferData(GL_ARRAY_BUFFER, vertexSize, GLBuffers.newDirectFloatBuffer(vertexData), GL_STATIC_DRAW);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        return true;
    }
    
    private boolean initTexture(GL3 gl3)	{
        
		jgli.Texture texture=new Texture(Target.TARGET_CUBE, Format.FORMAT_RGBA8_UNORM, new int[3], 1, 1, 1);
		assert(!texture.empty());

		texture[0].clear<glm::u8vec4>(glm::u8vec4(255,   0,   0, 255));
		texture[1].clear<glm::u8vec4>(glm::u8vec4(255, 128,   0, 255));
		texture[2].clear<glm::u8vec4>(glm::u8vec4(255, 255,   0, 255));
		texture[3].clear<glm::u8vec4>(glm::u8vec4(  0, 255,   0, 255));
		texture[4].clear<glm::u8vec4>(glm::u8vec4(  0, 255, 255, 255));
		texture[5].clear<glm::u8vec4>(glm::u8vec4(  0,   0, 255, 255));

		glActiveTexture(GL_TEXTURE0);
		glGenTextures(1, &TextureName);
		glBindTexture(GL_TEXTURE_CUBE_MAP, TextureName);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_BASE_LEVEL, 0);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAX_LEVEL, GLint(texture.levels() - 1));
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
		glTexParameterfv(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_BORDER_COLOR, &glm::vec4(0.0f)[0]);
		glTexParameterf(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_LOD, -1000.f);
		glTexParameterf(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAX_LOD, 1000.f);
		glTexParameterf(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_LOD_BIAS, 0.0f);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_COMPARE_MODE, GL_NONE);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);

		gli::gl GL;
		gli::gl::format const Format = GL.translate(texture.format());
		for(gli::textureCube::size_type Face = 0; Face < texture.faces(); ++Face)
		{
			glTexImage2D(
				GL_TEXTURE_CUBE_MAP_POSITIVE_X + GLenum(Face),
				0,
				Format.Internal,
				static_cast<GLsizei>(texture.dimensions().x), static_cast<GLsizei>(texture.dimensions().y),
				0,
				Format.External, Format.Type,
				texture[Face].data());
		}

		return true;
	}
}
