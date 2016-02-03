/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_500;

import static com.jogamp.opengl.GL.GL_TRUE;
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
import dev.Mat4;
import dev.Vec2;
import dev.Vec4u8;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import glf.Vertex_v2fc4ub;

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

    private int[] programName = new int[Program.MAX], vertexArrayName = new int[Program.MAX],
            bufferName = new int[Buffer.MAX], textureName = new int[Texture.MAX], uniformDiffuse = new int[Program.MAX],
            framebufferName = new int[Framebuffer.MAX];
    private int vertexCount, framebufferScale = 3, uniformTransform;
    
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
    
    private boolean initBuffer(GL4 gl4)	{
        
//		int[] uniformBufferOffset={0};
//		gl4.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset,0);
//		int uniformBlockSize = Math.max(Mat4.SIZE, uniformBufferOffset[0]);
//
//		Vec2[] data=new Vec2[36];
//		for(int i = 0; i < data.length; ++i)		{
//			float angle = (float)Math.PI * 2.0f * i / data.length;
//			data[i] = new Vec2((float)Math.sin(angle), (float)Math.cos(angle)).normalize();
//		}
//		vertexCount = 18;//static_cast<GLsizei>(Data.size() - 8);
//
//		gl4.glCreateBuffers(buffer::MAX, &BufferName[0]);
//		gl4.glNamedBufferStorage(BufferName[buffer::ELEMENT], ElementSize, ElementData, 0);
//		gl4.glNamedBufferStorage(BufferName[buffer::VERTEX], data.size() * sizeof(glm::vec2), &data[0], 0);
//		gl4.glNamedBufferStorage(BufferName[buffer::TRANSFORM], uniformBlockSize, nullptr, GL_MAP_WRITE_BIT);

		return true;
	}
}
