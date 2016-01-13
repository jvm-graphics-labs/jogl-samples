/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_420;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_DYNAMIC_DRAW;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.Profile;
import framework.Test;
import java.nio.FloatBuffer;
import jgli.Texture2dArray;

/**
 *
 * @author GBarbieri
 */
public class Gl_420_texture_array extends Test {

    public static void main(String[] args) {
        Gl_420_texture_array gl_420_texture_array = new Gl_420_texture_array();
    }

    public Gl_420_texture_array() {
        super("gl-420-texture-array", Profile.CORE, 4, 2);
    }

    private final String SHADERS_SOURCE = "texture-array";
    private final String SHADERS_ROOT = "src/data/gl_420";

    private int vertexCount = 6;
    private int vertexSize = vertexCount * 2 * 2 * Float.BYTES;
    private float[] vertexData = {
        -0.4f, -0.4f, 0.0f, 1.0f,
        +0.4f, -0.4f, 1.0f, 1.0f,
        +0.4f, +0.4f, 1.0f, 0.0f,
        +0.4f, +0.4f, 1.0f, 0.0f,
        -0.4f, +0.4f, 0.0f, 0.0f,
        -0.4f, -0.4f, 0.0f, 1.0f};

    private enum Buffer {
        VERTEX,
        TRANSFORM,
        MAX
    }

    private int[] vertexArrayName = {0}, samplerName = {0}, textureName = {0}, bufferName = new int[Buffer.MAX.ordinal()];
    private int programName;
    private float[] projection = new float[16], model = new float[16];

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        // Create program
        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

            shaderProgram.init(gl4);
            programName = shaderProgram.program();

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);
            shaderProgram.link(gl4, System.out);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        gl4.glGenBuffers(Buffer.MAX.ordinal(), bufferName, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl4.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM.ordinal()]);
        gl4.glBufferData(GL_UNIFORM_BUFFER, projection.length * Float.BYTES, null, GL_DYNAMIC_DRAW);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return true;
    }
    
    private boolean initTexture(GL4 gl4)	{
        
		int[] maxTextureArrayLayers={0};
//		gl4.glGetIntegerv(GL_MAX_ARRAY_TEXTURE_LAYERS, maxTextureArrayLayers,0);
//
//		gl4.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
//
//		gl4.glGenTextures(1, textureName,0);
//
//		gl4.glActiveTexture(GL_TEXTURE0);
//		gl4.glBindTexture(GL_TEXTURE_2D_ARRAY, textureName[0]);
//
//		jgli.Texture2dArray texture=new Texture2dArray(jgli.Format.FORMAT_RGBA8_UNORM, new int[]{4,4,4}, 15, 1);
//		for(int layerIndex = 0; layerIndex < texture.layers(); ++layerIndex)
//		{
//			byte[] color(Math.rlinearRand(glm::vec4(0), glm::vec4(1)) * 255.f);
//			texture.clear(layerIndex, 0, 0, color);
//		}
//
//		jgli::gl::format const Format = GL.translate(texture.format());
//
//		gl4.glTexStorage3D(GL_TEXTURE_2D_ARRAY, GLsizei(texture.levels()),
//			Format.Internal,
//			GLsizei(texture.dimensions().x), GLsizei(texture.dimensions().y), GLsizei(texture.layers()));
//
//		for(gli::texture2DArray::size_type Array = 0; Array < texture.layers(); ++Array)
//		for(gli::texture2DArray::size_type Level = 0; Level < texture.levels(); ++Level)
//		{
//		gl4.	glTexSubImage3D(GL_TEXTURE_2D_ARRAY, GLint(Level),
//				0, 0, GLint(Array),
//				GLsizei(texture[Array][Level].dimensions().x), GLsizei(texture[Array][Level].dimensions().y), GLsizei(1),
//				Format.External, Format.Type,
//				texture[Array][Level].data());
//		}
//
//		gl4.glPixelStorei(GL_UNPACK_ALIGNMENT, 4);

		return true;
	}
}
