/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_500;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2.*;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.BufferUtils;
import glm.mat._4.Mat4;
import glm.vec._2.Vec2;
import glm.vec._4.Vec4;
import framework.Caps;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import glf.Vertex_v2fv2f;
import glm.glm;
import glm.vec._2.u.Vec2u;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import jgli.Texture2d;

/**
 *
 * @author GBarbieri
 */
public class Gl_500_conservative_raster_nv extends Test {

    public static void main(String[] args) {
        Gl_500_conservative_raster_nv gl_500_conservative_raster_nv = new Gl_500_conservative_raster_nv();
    }

    public Gl_500_conservative_raster_nv() {
        super("gl-500-conservative-raster-nv", Profile.CORE, 4, 5, new Vec2(Math.PI * 0.25f));
    }

    private final String SHADERS_SOURCE_TEXTURE = "conservative-raster";
    private final String SHADERS_SOURCE_SPLASH = "conservative-raster-blit";
    private final String SHADERS_ROOT = "src/data/gl_500";
    private final String TEXTURE_DIFFUSE = "kueken7_rgb_dxt1_unorm.dds";

    private int vertexCount = 4;
    private int vertexSize = vertexCount * glf.Vertex_v2fv2f.SIZE;
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

    private IntBuffer framebufferName = GLBuffers.newDirectIntBuffer(1),
            vertexArrayName = GLBuffers.newDirectIntBuffer(Pipeline.MAX),
            bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX),
            textureName = GLBuffers.newDirectIntBuffer(Texture.MAX),
            pipelineName = GLBuffers.newDirectIntBuffer(Pipeline.MAX);
    private int[] programName = new int[Pipeline.MAX];
    private FloatBuffer whiteColor = GLBuffers.newDirectFloatBuffer(new float[]{1.0f, 1.0f, 1.0f, 1.0f});
    /**
     * https://jogamp.org/bugzilla/show_bug.cgi?id=1287
     */
    private boolean bug1287 = true;

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = checkExtension(gl4, "GL_NV_conservative_raster");

        Caps caps = new Caps(gl4, Profile.CORE);

        if (validated) {
            validated = initProgram(gl4);
        }
        if (validated) {
            validated = initBuffer(gl4);
        }
        if (validated) {
            validated = initVertexArray(gl4);
        }
        if (validated) {
            validated = initTexture(gl4);
        }
        if (validated) {
            validated = initFramebuffer(gl4);
        }

        IntBuffer subPixelBiasX = GLBuffers.newDirectIntBuffer(1);
        IntBuffer subPixelBiasY = GLBuffers.newDirectIntBuffer(1);
        IntBuffer maxSubPixelBias = GLBuffers.newDirectIntBuffer(1);
        gl4.glGetIntegerv(GL_SUBPIXEL_PRECISION_BIAS_X_BITS_NV, subPixelBiasX);
        gl4.glGetIntegerv(GL_SUBPIXEL_PRECISION_BIAS_Y_BITS_NV, subPixelBiasY);
        gl4.glGetIntegerv(GL_MAX_SUBPIXEL_PRECISION_BIAS_BITS_NV, maxSubPixelBias);
        System.out.println("GL_SUBPIXEL_PRECISION_BIAS_X_BITS_NV " + subPixelBiasX.get(0));
        System.out.println("GL_SUBPIXEL_PRECISION_BIAS_Z_BITS_NV " + subPixelBiasY.get(0));
        System.out.println("GL_MAX_SUBPIXEL_PRECISION_BIAS_BITS_NV " + maxSubPixelBias.get(0));
        BufferUtils.destroyDirectBuffer(subPixelBiasX);
        BufferUtils.destroyDirectBuffer(subPixelBiasY);
        BufferUtils.destroyDirectBuffer(maxSubPixelBias);

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

            gl4.glGenProgramPipelines(Pipeline.MAX, pipelineName);
            gl4.glUseProgramStages(pipelineName.get(Pipeline.TEXTURE), GL_VERTEX_SHADER_BIT | GL_FRAGMENT_SHADER_BIT,
                    programName[Pipeline.TEXTURE]);
            gl4.glUseProgramStages(pipelineName.get(Pipeline.SPLASH), GL_VERTEX_SHADER_BIT | GL_FRAGMENT_SHADER_BIT,
                    programName[Pipeline.SPLASH]);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        int[] uniformBufferOffset = {0};
        gl4.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset, 0);
        int uniformBlockSize = Math.max(Mat4.SIZE, uniformBufferOffset[0]);

        Vec4 material0 = new Vec4(1.0f, 0.5f, 0.0f, 1.0f);
        Vec4 material1 = new Vec4(0.0f, 0.5f, 1.0f, 1.0f);

        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        FloatBuffer material0Buffer = GLBuffers.newDirectFloatBuffer(material0.toFA_());
        FloatBuffer material1Buffer = GLBuffers.newDirectFloatBuffer(material1.toFA_());

        gl4.glCreateBuffers(Buffer.MAX, bufferName);

        if (!bug1287) {

            gl4.glNamedBufferStorage(bufferName.get(Buffer.VERTEX), vertexSize, vertexBuffer, 0);
            gl4.glNamedBufferStorage(bufferName.get(Buffer.ELEMENT), elementSize, elementBuffer, 0);
            gl4.glNamedBufferStorage(bufferName.get(Buffer.TRANSFORM), uniformBlockSize, null, GL_MAP_WRITE_BIT);
            gl4.glNamedBufferStorage(bufferName.get(Buffer.MATERIAL0), Vec4.SIZE, material0Buffer, 0);
            gl4.glNamedBufferStorage(bufferName.get(Buffer.MATERIAL1), Vec4.SIZE, material1Buffer, 0);

        } else {

            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
            gl4.glBufferStorage(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, 0);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);
            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT));
            gl4.glBufferStorage(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, 0);
            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.TRANSFORM));
            gl4.glBufferStorage(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_MAP_WRITE_BIT);
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);
            gl4.glBindBuffer(GL_SHADER_STORAGE_BUFFER, bufferName.get(Buffer.MATERIAL0));
            gl4.glBufferStorage(GL_SHADER_STORAGE_BUFFER, Vec4.SIZE, material0Buffer, 0);
            gl4.glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
            gl4.glBindBuffer(GL_SHADER_STORAGE_BUFFER, bufferName.get(Buffer.MATERIAL1));
            gl4.glBufferStorage(GL_SHADER_STORAGE_BUFFER, Vec4.SIZE, material1Buffer, 0);
            gl4.glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
        }
        BufferUtils.destroyDirectBuffer(vertexBuffer);
        BufferUtils.destroyDirectBuffer(elementBuffer);
        BufferUtils.destroyDirectBuffer(material0Buffer);
        BufferUtils.destroyDirectBuffer(material1Buffer);

        return true;
    }

    private boolean initTexture(GL4 gl4) {

        try {
            jgli.Texture2d texture = new Texture2d(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE));
            assert (!texture.empty());

            
            Vec2u framebufferSize = new Vec2u(windowSize).div(16);

            gl4.glCreateTextures(GL_TEXTURE_2D_ARRAY, Texture.MAX, textureName);
            gl4.glTextureParameteri(textureName.get(0), GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTextureParameteri(textureName.get(0), GL_TEXTURE_MAX_LEVEL, 0);
            gl4.glTextureParameteri(textureName.get(0), GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            gl4.glTextureParameteri(textureName.get(0), GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            gl4.glTextureStorage3D(textureName.get(0), 1, GL_RGBA8, framebufferSize.x, framebufferSize.y, 1);

        } catch (IOException ex) {
            Logger.getLogger(Gl_500_conservative_raster_nv.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glCreateVertexArrays(Pipeline.MAX, vertexArrayName);

        gl4.glVertexArrayAttribBinding(vertexArrayName.get(0), Semantic.Attr.POSITION, Semantic.Buffer.STATIC);
        gl4.glVertexArrayAttribFormat(vertexArrayName.get(0), Semantic.Attr.POSITION, 2, GL_FLOAT, false, 0);
        gl4.glEnableVertexArrayAttrib(vertexArrayName.get(0), Semantic.Attr.POSITION);

        gl4.glVertexArrayAttribBinding(vertexArrayName.get(0), Semantic.Attr.TEXCOORD, Semantic.Buffer.STATIC);
        gl4.glVertexArrayAttribFormat(vertexArrayName.get(0), Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, Vec2.SIZE);
        gl4.glEnableVertexArrayAttrib(vertexArrayName.get(0), Semantic.Attr.TEXCOORD);

        gl4.glVertexArrayElementBuffer(vertexArrayName.get(0), bufferName.get(Buffer.ELEMENT));
        gl4.glVertexArrayVertexBuffer(vertexArrayName.get(0), Semantic.Buffer.STATIC, bufferName.get(Buffer.VERTEX), 0,
                Vertex_v2fv2f.SIZE);

        return true;
    }

    private boolean initFramebuffer(GL4 gl4) {

        gl4.glCreateFramebuffers(1, framebufferName);
        gl4.glNamedFramebufferTexture(framebufferName.get(0), GL_COLOR_ATTACHMENT0, textureName.get(Texture.COLORBUFFER),
                0);

        return isFramebufferComplete(gl4, framebufferName.get(0));
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        {
            ByteBuffer pointer = gl4.glMapNamedBufferRange(bufferName.get(Buffer.TRANSFORM),
                    0, Mat4.SIZE, GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, (float) windowSize.x / windowSize.y, 0.1f, 100.0f);
            Mat4 model = new Mat4(1.0f);

            pointer.asFloatBuffer().put(projection.mul(viewMat4()).mul(model).toFa_());

            // Make sure the uniform buffer is uploaded
            gl4.glUnmapNamedBuffer(bufferName.get(Buffer.TRANSFORM));
        }

        gl4.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName.get(0));
        gl4.glViewportIndexedf(0, 0, 0, windowSize.x / 16.0f, windowSize.y / 16.0f);

        gl4.glClearBufferfv(GL_COLOR, 0, whiteColor);

        // Bind rendering objects
        gl4.glViewportIndexedf(0, 0, 0, windowSize.x / 16.0f, windowSize.y / 16.0f);
        gl4.glBindProgramPipeline(pipelineName.get(Pipeline.TEXTURE));
        gl4.glBindVertexArray(vertexArrayName.get(Pipeline.TEXTURE));
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName.get(Buffer.TRANSFORM));

        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.MATERIAL, bufferName.get(Buffer.MATERIAL0));

        //glSubpixelPrecisionBiasNV(-8, -8);
        gl4.glEnable(GL_CONSERVATIVE_RASTERIZATION_NV);
        gl4.glDrawElementsInstancedBaseVertexBaseInstance(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0, 0);

        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.MATERIAL, bufferName.get(Buffer.MATERIAL1));

        gl4.glDisable(GL_CONSERVATIVE_RASTERIZATION_NV);
        gl4.glDrawElementsInstancedBaseVertexBaseInstance(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0, 0);

        gl4.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        gl4.glViewportIndexedf(0, 0, 0, windowSize.x, windowSize.y);

        gl4.glBindProgramPipeline(pipelineName.get(Pipeline.SPLASH));
        gl4.glBindVertexArray(vertexArrayName.get(Pipeline.SPLASH));
        gl4.glBindTextureUnit(0, textureName.get(Texture.COLORBUFFER));

        gl4.glDrawArraysInstancedBaseInstance(GL_TRIANGLES, 0, 3, 1, 0);

        return true;
    }
}
