/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_420;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.BufferUtils;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import jgli.Texture2d;
import jglm.Vec4i;

/**
 *
 * @author GBarbieri
 */
public class Gl_420_texture_conversion extends Test {

    public static void main(String[] args) {
        Gl_420_texture_conversion gl_420_texture_conversion = new Gl_420_texture_conversion();
    }

    public Gl_420_texture_conversion() {
        super("gl-420-texture-conversion", Profile.CORE, 4, 2);
    }

    private final String VERT_SHADER_SOURCE = "texture-conversion";
    private final String[] FRAG_SHADERS_SOURCE = {
        "texture-conversion-normalized",
        "texture-conversion-uint"
    };
    private final String SHADERS_ROOT = "src/data/gl_420";
    private final String TEXTURE_DIFFUSE = "kueken7_rgb8_unorm.dds";

    private int vertexCount = 4;
    private int vertexSize = vertexCount * 2 * 2 * Float.BYTES;
    private float[] vertexData = {
        -1.0f, -1.0f, 0.0f, 1.0f,
        +1.0f, -1.0f, 1.0f, 1.0f,
        +1.0f, +1.0f, 1.0f, 0.0f,
        -1.0f, +1.0f, 0.0f, 0.0f};

    private int elementCount = 6;
    private int elementSize = elementCount * Short.BYTES;
    private short[] elementData = {
        0, 1, 2,
        2, 3, 0};

    private enum Texture {
        RGBA8, // GL_RGBA8
        RGBA8UI, // GL_RGBA8UI
        RGBA32F, // GL_RGBA32F
        RGBA8_SNORM,
        MAX
    }

    private enum Program {
        NORM,
        UINT,
        MAX
    }

    private enum Buffer {
        VERTEX,
        ELEMENT,
        TRANSFORM,
        MAX
    }

    private int[] textureInternalFormat = {
        GL_COMPRESSED_RGBA_S3TC_DXT5_EXT,
        GL_RGBA8UI,
        GL_COMPRESSED_RGBA_BPTC_UNORM_ARB,
        GL_RGBA8_SNORM};

    private int[] textureFormat = {
        GL_RGB,
        GL_RGB_INTEGER,
        GL_RGB,
        GL_RGB};

    private Vec4i[] viewport = {
        new Vec4i(0, 0, 320, 240),
        new Vec4i(320, 0, 320, 240),
        new Vec4i(320, 240, 320, 240),
        new Vec4i(0, 240, 320, 240)};

    private int[] vertexArrayName = {0}, pipelineName = new int[Program.MAX.ordinal()],
            programName = new int[Program.MAX.ordinal()], bufferName = new int[Buffer.MAX.ordinal()],
            textureName = new int[Texture.MAX.ordinal()];
    private float[] projection = new float[16], model = new float[16];

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;

        if (validated) {
            validated = initTexture(gl4);
        }
        if (validated) {
            validated = initProgram(gl4);
        }
        if (validated) {
            validated = initBuffer(gl4);
        }
        if (validated) {
            validated = initVertexArray(gl4);
        }

        return validated;
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        gl4.glGenProgramPipelines(Program.MAX.ordinal(), pipelineName, 0);

        ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                this.getClass(), SHADERS_ROOT, null, VERT_SHADER_SOURCE, "vert", null, true);
        ShaderCode fragShaderCodeNorm = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                this.getClass(), SHADERS_ROOT, null, FRAG_SHADERS_SOURCE[Program.NORM.ordinal()], "frag", null, true);
        ShaderCode fragShaderCodeUint = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                this.getClass(), SHADERS_ROOT, null, FRAG_SHADERS_SOURCE[Program.UINT.ordinal()], "frag", null, true);

        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            shaderProgram.init(gl4);
            programName[Program.NORM.ordinal()] = shaderProgram.program();
            gl4.glProgramParameteri(programName[Program.NORM.ordinal()], GL_PROGRAM_SEPARABLE, GL_TRUE);

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCodeNorm);

            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            shaderProgram.init(gl4);
            programName[Program.UINT.ordinal()] = shaderProgram.program();
            gl4.glProgramParameteri(programName[Program.UINT.ordinal()], GL_PROGRAM_SEPARABLE, GL_TRUE);

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCodeUint);

            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            gl4.glUseProgramStages(pipelineName[Program.NORM.ordinal()],
                    GL_VERTEX_SHADER_BIT | GL_FRAGMENT_SHADER_BIT, programName[Program.NORM.ordinal()]);
            gl4.glUseProgramStages(pipelineName[Program.UINT.ordinal()],
                    GL_VERTEX_SHADER_BIT | GL_FRAGMENT_SHADER_BIT, programName[Program.UINT.ordinal()]);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        boolean validated = true;

        gl4.glGenBuffers(Buffer.MAX.ordinal(), bufferName, 0);

        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT.ordinal()]);
        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(elementBuffer);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl4.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(vertexBuffer);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        int[] uniformBufferOffset = {0};
        gl4.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset, 0);
        int uniformBlockSize = Math.max(projection.length * Float.BYTES, uniformBufferOffset[0]);

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM.ordinal()]);
        gl4.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_DYNAMIC_DRAW);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return validated;
    }

    private boolean initTexture(GL4 gl4) {

        try {
            jgli.Texture2d texture = new Texture2d(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE));
            assert (!texture.empty());

            gl4.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

            gl4.glGenTextures(Texture.MAX.ordinal(), textureName, 0);
            gl4.glActiveTexture(GL_TEXTURE0);

            for (int i = 0; i < Texture.MAX.ordinal(); ++i) {
                gl4.glBindTexture(GL_TEXTURE_2D, textureName[i]);
                gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
                gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, texture.levels() - 1);
                gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_NEAREST);
                gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
                gl4.glTexStorage2D(GL_TEXTURE_2D, texture.levels(), textureInternalFormat[i],
                        texture.dimensions(0)[0], texture.dimensions(0)[1]);

                for (int level = 0; level < texture.levels(); ++level) {
                    gl4.glTexSubImage2D(GL_TEXTURE_2D, level,
                            0, 0,
                            texture.dimensions(level)[0], texture.dimensions(level)[1],
                            textureFormat[i], GL_UNSIGNED_BYTE,
                            texture.data(level));
                }
            }

            gl4.glBindTexture(GL_TEXTURE_2D, 0);
            gl4.glPixelStorei(GL_UNPACK_ALIGNMENT, 4);

        } catch (IOException ex) {
            Logger.getLogger(Gl_420_texture_conversion.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(1, vertexArrayName, 0);
        gl4.glBindVertexArray(vertexArrayName[0]);
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 2 * 2 * Float.BYTES, 0);
            gl4.glVertexAttribPointer(Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, 2 * 2 * Float.BYTES, 2 * Float.BYTES);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl4.glEnableVertexAttribArray(Semantic.Attr.TEXCOORD);

            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT.ordinal()]);
        }
        gl4.glBindVertexArray(0);

        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 0.5f, 0.0f, 1.0f}, 0);

        {
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM.ordinal()]);
            ByteBuffer pointer = gl4.glMapBufferRange(
                    GL_UNIFORM_BUFFER, 0, projection.length * Float.BYTES,
                    GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            FloatUtil.makePerspective(projection, 0, true, (float) Math.PI * 0.25f,
                    (float) windowSize.x / windowSize.y, 0.1f, 100.0f);
            FloatUtil.makeIdentity(model);

            FloatUtil.multMatrix(projection, view());
            FloatUtil.multMatrix(projection, model);

            for (float f : projection) {
                pointer.putFloat(f);
            }
            pointer.rewind();

            // Make sure the uniform buffer is uploaded
            gl4.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.TRANSFORM.ordinal()]);

        gl4.glBindVertexArray(vertexArrayName[0]);
        gl4.glActiveTexture(GL_TEXTURE0);

        gl4.glBindProgramPipeline(pipelineName[Program.UINT.ordinal()]);
        {
            gl4.glViewportIndexedfv(0, viewport[Texture.RGBA8UI.ordinal()].toFloatArray(), 0);
            gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.RGBA8UI.ordinal()]);

            gl4.glDrawElementsInstancedBaseVertexBaseInstance(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0, 0);
        }

        gl4.glBindProgramPipeline(pipelineName[Program.NORM.ordinal()]);
        {
            gl4.glViewportIndexedfv(0, viewport[Texture.RGBA32F.ordinal()].toFloatArray(), 0);
            gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.RGBA32F.ordinal()]);

            gl4.glDrawElementsInstancedBaseVertexBaseInstance(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0, 0);

            gl4.glViewportIndexedfv(0, viewport[Texture.RGBA8.ordinal()].toFloatArray(), 0);
            gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.RGBA8.ordinal()]);

            gl4.glDrawElementsInstancedBaseVertexBaseInstance(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0, 0);

            gl4.glViewportIndexedfv(0, viewport[Texture.RGBA8_SNORM.ordinal()].toFloatArray(), 0);
            gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.RGBA8_SNORM.ordinal()]);

            gl4.glDrawElementsInstancedBaseVertexBaseInstance(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0, 0);
        }

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteBuffers(Buffer.MAX.ordinal(), bufferName, 0);
        for (int i = 0; i < Program.MAX.ordinal(); ++i) {
            gl4.glDeleteProgram(programName[i]);
        }
        gl4.glDeleteTextures(Texture.MAX.ordinal(), textureName, 0);
        gl4.glDeleteVertexArrays(1, vertexArrayName, 0);
        gl4.glDeleteProgramPipelines(Program.MAX.ordinal(), pipelineName, 0);

        return true;
    }
}
