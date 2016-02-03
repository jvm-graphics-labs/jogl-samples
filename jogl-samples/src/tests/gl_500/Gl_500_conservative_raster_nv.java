/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_500;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_MAP_WRITE_BIT;
import static com.jogamp.opengl.GL.GL_TRUE;
import static com.jogamp.opengl.GL2.GL_MAX_SUBPIXEL_PRECISION_BIAS_BITS_NV;
import static com.jogamp.opengl.GL2.GL_SUBPIXEL_PRECISION_BIAS_X_BITS_NV;
import static com.jogamp.opengl.GL2.GL_SUBPIXEL_PRECISION_BIAS_Y_BITS_NV;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER_BIT;
import static com.jogamp.opengl.GL2ES2.GL_PROGRAM_SEPARABLE;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER_BIT;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import dev.Mat4;
import dev.Vec4;
import framework.Caps;
import framework.Profile;
import framework.Test;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 *
 * @author GBarbieri
 */
public class Gl_500_conservative_raster_nv extends Test {

    public static void main(String[] args) {
        Gl_500_conservative_raster_nv gl_500_conservative_raster_nv = new Gl_500_conservative_raster_nv();
    }

    public Gl_500_conservative_raster_nv() {
        super("gl-500-conservative-raster-nv", Profile.CORE, 4, 5, new jglm.Vec2((float) Math.PI * 0.25f, (float) Math.PI * 0.25f));
    }

    private final String SHADERS_SOURCE_TEXTURE = "conservative-raster";
    private final String SHADERS_SOURCE_SPLASH = "conservative-raster-blit";
    private final String SHADERS_ROOT = "src/data/gl_500";
    private final String TEXTURE_DIFFUSE = "kueken7_rgb_dxt1_unorm.dds";

    private int vertexCount = 4;
    private int vertexSize = vertexCount * glf.Vertex_v2fv2f.SIZEOF;
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
        public static final int MATERIAL0 = 3;
        public static final int MATERIAL1 = 4;
        public static final int MAX = 5;
    }

    private class Texture {

        public static final int COLORBUFFER = 0;
        public static final int MAX = 1;
    }

    private class Pipeline {

        public static final int TEXTURE = 0;
        public static final int SPLASH = 1;
        public static final int MAX = 2;
    }

    private int[] framebufferName = {0}, programName = new int[Pipeline.MAX], vertexArrayName = new int[Pipeline.MAX],
            bufferName = new int[Buffer.MAX], textureName = new int[Texture.MAX], pipelineName = new int[Pipeline.MAX];

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;

        Caps caps = new Caps(gl4, Profile.CORE);

        if (validated) {
            validated = initProgram(gl4);
        }
        if (validated) {
            validated = initBuffer(gl4);
        }
//		if(validated)
//			validated = initVertexArray(gl4);
//		if(validated)
//			validated = initTexture(gl4);
//		if(validated)
//			validated = initFramebuffer(gl4);

        int[] subPixelBiasX = {0};
        int[] subPixelBiasY = {0};
        int[] maxSubPixelBias = {0};
        gl4.glGetIntegerv(GL_SUBPIXEL_PRECISION_BIAS_X_BITS_NV, subPixelBiasX, 0);
        gl4.glGetIntegerv(GL_SUBPIXEL_PRECISION_BIAS_Y_BITS_NV, subPixelBiasY, 0);
        gl4.glGetIntegerv(GL_MAX_SUBPIXEL_PRECISION_BIAS_BITS_NV, maxSubPixelBias, 0);

        return validated;
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        if (validated) {

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_TEXTURE, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_TEXTURE, "frag", null, true);

            ShaderProgram shaderProgram = new ShaderProgram();
            shaderProgram.init(gl4);

            programName[Pipeline.TEXTURE] = shaderProgram.program();

            gl4.glProgramParameteri(programName[Pipeline.TEXTURE], GL_PROGRAM_SEPARABLE, GL_TRUE);

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);
            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_SPLASH, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_SPLASH, "frag", null, true);

            ShaderProgram shaderProgram = new ShaderProgram();
            shaderProgram.init(gl4);

            programName[Pipeline.SPLASH] = shaderProgram.program();

            gl4.glProgramParameteri(programName[Pipeline.SPLASH], GL_PROGRAM_SEPARABLE, GL_TRUE);

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);
            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            gl4.glGenProgramPipelines(Pipeline.MAX, pipelineName, 0);
            gl4.glUseProgramStages(pipelineName[Pipeline.TEXTURE], GL_VERTEX_SHADER_BIT | GL_FRAGMENT_SHADER_BIT,
                    programName[Pipeline.TEXTURE]);
            gl4.glUseProgramStages(pipelineName[Pipeline.SPLASH], GL_VERTEX_SHADER_BIT | GL_FRAGMENT_SHADER_BIT,
                    programName[Pipeline.SPLASH]);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        int[] uniformBufferOffset = {0};
        gl4.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset, 0);
        int uniformBlockSize = Math.max(Mat4.SIZEOF, uniformBufferOffset[0]);

        Vec4 material0 = new Vec4(1.0f, 0.5f, 0.0f, 1.0f);
        Vec4 material1 = new Vec4(0.0f, 0.5f, 1.0f, 1.0f);

        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        FloatBuffer material0Buffer = GLBuffers.newDirectFloatBuffer(material0.toFA_());
        FloatBuffer material1Buffer = GLBuffers.newDirectFloatBuffer(material1.toFA_());

        gl4.glCreateBuffers(Buffer.MAX, bufferName, 0);
        gl4.glNamedBufferStorage(bufferName[Buffer.VERTEX], vertexSize, vertexBuffer, 0);
        gl4.glNamedBufferStorage(bufferName[Buffer.ELEMENT], elementSize, elementBuffer, 0);
        gl4.glNamedBufferStorage(bufferName[Buffer.TRANSFORM], uniformBlockSize, null, GL_MAP_WRITE_BIT);
        gl4.glNamedBufferStorage(bufferName[Buffer.MATERIAL0], Vec4.SIZEOF, material0Buffer, 0);
        gl4.glNamedBufferStorage(bufferName[Buffer.MATERIAL1], Vec4.SIZEOF, material1Buffer, 0);

        return true;
    }
}
