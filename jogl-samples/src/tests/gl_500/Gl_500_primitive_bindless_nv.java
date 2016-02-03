/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_500;

import static com.jogamp.opengl.GL.GL_ALPHA;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_DYNAMIC_DRAW;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TRUE;
import static com.jogamp.opengl.GL.GL_UNPACK_ALIGNMENT;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER_BIT;
import static com.jogamp.opengl.GL2ES2.GL_PROGRAM_SEPARABLE;
import static com.jogamp.opengl.GL2ES2.GL_RED;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER_BIT;
import static com.jogamp.opengl.GL2ES3.GL_BLUE;
import static com.jogamp.opengl.GL2ES3.GL_GREEN;
import static com.jogamp.opengl.GL2ES3.GL_READ_ONLY;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_BASE_LEVEL;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_MAX_LEVEL;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_SWIZZLE_A;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_SWIZZLE_B;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_SWIZZLE_G;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_SWIZZLE_R;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER;
import static com.jogamp.opengl.GL2GL3.GL_BUFFER_GPU_ADDRESS_NV;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import dev.Mat4;
import framework.BufferUtils;
import framework.Profile;
import framework.Test;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import jgli.Texture2d;

/**
 *
 * @author GBarbieri
 */
public class Gl_500_primitive_bindless_nv extends Test {

    public static void main(String[] args) {
        Gl_500_primitive_bindless_nv gl_500_primitive_bindless_nv = new Gl_500_primitive_bindless_nv();
    }

    public Gl_500_primitive_bindless_nv() {
        super("gl-500-primitive-bindless-nv", Profile.CORE, 4, 5);
    }

    private final String SHADERS_SOURCE = "primitive-bindless-nv";
    private final String SHADERS_ROOT = "src/data/gl_500";
    private final String TEXTURE_DIFFUSE = "kueken7_rgba8_srgb.dds";

    // With DDS textures, v texture coordinate are reversed, from top to bottom
    private int vertexCount = 6;
    private int vertexSize = vertexCount * glf.Vertex_v2fv2f.SIZEOF;
    private float[] vertexData = {
        -1.0f, -1.0f,/**/ 0.0f, 1.0f,
        +1.0f, -1.0f,/**/ 1.0f, 1.0f,
        +1.0f, +1.0f,/**/ 1.0f, 0.0f,
        +1.0f, +1.0f,/**/ 1.0f, 0.0f,
        -1.0f, +1.0f,/**/ 0.0f, 0.0f,
        -1.0f, -1.0f,/**/ 0.0f, 1.0f};

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int TRANSFORM = 1;
        public static final int MAX = 2;
    }

    private int[] bufferName = new int[Buffer.MAX], vertexArrayName = {0}, pipelineName = {0}, textureName = {0};
    private int programName;
    private long[] address = {0};

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

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

            gl4.glGenProgramPipelines(1, pipelineName, 0);
            gl4.glUseProgramStages(pipelineName[0], GL_VERTEX_SHADER_BIT | GL_FRAGMENT_SHADER_BIT, programName);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        gl4.glGenBuffers(Buffer.MAX, bufferName, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl4.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(vertexBuffer);
        gl4.glGetBufferParameterui64vNV(GL_ARRAY_BUFFER, GL_BUFFER_GPU_ADDRESS_NV, address, 0);
        gl4.glMakeBufferResidentNV(GL_ARRAY_BUFFER, GL_READ_ONLY);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
        gl4.glBufferData(GL_UNIFORM_BUFFER, Mat4.SIZEOF, null, GL_DYNAMIC_DRAW);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return true;
    }

    private boolean initTexture(GL4 gl4) {

        try {
            jgli.Texture2d texture = new Texture2d(jgli.Load.load(TEXTURE_ROOT + TEXTURE_DIFFUSE));
            assert (!texture.empty());
            jgli.Gl.Format format = jgli.Gl.translate(texture.format());

            gl4.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

            gl4.glCreateTextures(GL_TEXTURE_2D, 1, textureName, 0);
            gl4.glTextureParameteri(textureName[0], GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTextureParameteri(textureName[0], GL_TEXTURE_MAX_LEVEL, texture.levels() - 1);
            gl4.glTextureParameteri(textureName[0], GL_TEXTURE_SWIZZLE_R, GL_RED);
            gl4.glTextureParameteri(textureName[0], GL_TEXTURE_SWIZZLE_G, GL_GREEN);
            gl4.glTextureParameteri(textureName[0], GL_TEXTURE_SWIZZLE_B, GL_BLUE);
            gl4.glTextureParameteri(textureName[0], GL_TEXTURE_SWIZZLE_A, GL_ALPHA);

            gl4.glTextureStorage2D(textureName[0], texture.levels(), format.internal.value,
                    texture.dimensions()[0], texture.dimensions()[1]);

            for (int level = 0; level < texture.levels(); ++level) {

                gl4.glTextureSubImage2D(textureName[0], level,
                        0, 0,
                        texture.dimensions(level)[0], texture.dimensions(level)[1],
                        format.external.value, format.type.value,
                        texture.data(level));
            }

        } catch (IOException ex) {
            Logger.getLogger(Gl_500_primitive_bindless_nv.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
    
    private boolean initVertexArray(GL4 gl4)	{
        
		glCreateVertexArrays(1, &VertexArrayName);
		glBindVertexArray(VertexArrayName);
			glVertexAttribFormatNV(semantic::attr::POSITION, 2, GL_FLOAT, GL_FALSE, sizeof(glf::vertex_v2fv2f));
			glVertexAttribFormatNV(semantic::attr::TEXCOORD, 2, GL_FLOAT, GL_FALSE, sizeof(glf::vertex_v2fv2f));

			glEnableClientState(GL_VERTEX_ATTRIB_ARRAY_UNIFIED_NV);
			glEnableVertexAttribArray(semantic::attr::POSITION);
			glEnableVertexAttribArray(semantic::attr::TEXCOORD);
		glBindVertexArray(0);

		return true;
	}
}
