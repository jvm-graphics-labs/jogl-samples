/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_430;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL3ES3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.BufferUtils;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import glf.Vertex_v2fv2f;
import glm.glm;
import glm.mat._4.Mat4;
import glm.vec._2.Vec2;
import glm.vec._2.i.Vec2i;
import glm.vec._3.i.Vec3i;
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
public class Gl_430_program_compute_variable_group_size extends Test {

    public static void main(String[] args) {
        Gl_430_program_compute_variable_group_size gl_430_program_compute_variable_group_size
                = new Gl_430_program_compute_variable_group_size();
    }

    public Gl_430_program_compute_variable_group_size() {
        super("gl-430-program-compute-variable-group-size", Profile.CORE, 4, 3);
    }

    private final String SHADERS_SOURCE = "program-compute-variable-group-size";
    private final String SHADERS_ROOT = "src/data/gl_430";
    private final String TEXTURE_DIFFUSE = "kueken7_rgba8_srgb.dds";

    private int vertexCount = 4;
    private int vertexSize = vertexCount * Vertex_v2fv2f.SIZE;
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

    private class Program {

        public static final int GRAPHICS = 0;
        public static final int COMPUTE = 1;
        public static final int MAX = 2;
    }

    private class Texture {

        public static final int INPUT = 0;
        public static final int OUTPUT = 1;
        public static final int MAX = 2;
    }

    private class ImageUnit {

        public static final int INPUT = 0;
        public static final int OUTPUT = 1;
        public static final int MAX = 2;
    }

    private class Buffer {

        public static final int ELEMENT = 0;
        public static final int VERTEX = 1;
        public static final int TRANSFORM = 2;
        public static final int MAX = 3;
    }

    private IntBuffer pipelineName = GLBuffers.newDirectIntBuffer(Program.MAX),
            bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX),
            textureName = GLBuffers.newDirectIntBuffer(Texture.MAX),
            vertexArrayName = GLBuffers.newDirectIntBuffer(1);
    private FloatBuffer white = GLBuffers.newDirectFloatBuffer(new float[]{1.0f, 1.0f, 1.0f, 1.0f});
    private FloatBuffer clear = GLBuffers.newDirectFloatBuffer(new float[]{1.0f, 0.5f, 0.0f, 1.0f});
    private int[] programName = new int[Program.MAX];
    private Vec2i textureSize;

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        logImplementationDependentLimit(gl4, GL_MAX_COMPUTE_UNIFORM_BLOCKS, "GL_MAX_COMPUTE_UNIFORM_BLOCKS");
        logImplementationDependentLimit(gl4, GL_MAX_COMPUTE_TEXTURE_IMAGE_UNITS, "GL_MAX_COMPUTE_TEXTURE_IMAGE_UNITS");
        logImplementationDependentLimit(gl4, GL_MAX_COMPUTE_IMAGE_UNIFORMS, "GL_MAX_COMPUTE_IMAGE_UNIFORMS");
        logImplementationDependentLimit(gl4, GL_MAX_COMPUTE_SHARED_MEMORY_SIZE, "GL_MAX_COMPUTE_SHARED_MEMORY_SIZE");
        logImplementationDependentLimit(gl4, GL_MAX_COMPUTE_UNIFORM_COMPONENTS, "GL_MAX_COMPUTE_UNIFORM_COMPONENTS");
        logImplementationDependentLimit(gl4, GL_MAX_COMPUTE_ATOMIC_COUNTER_BUFFERS, "GL_MAX_COMPUTE_ATOMIC_COUNTER_BUFFERS");
        logImplementationDependentLimit(gl4, GL_MAX_COMPUTE_ATOMIC_COUNTERS, "GL_MAX_COMPUTE_ATOMIC_COUNTERS");
        logImplementationDependentLimit(gl4, GL_MAX_COMBINED_COMPUTE_UNIFORM_COMPONENTS, "GL_MAX_COMBINED_COMPUTE_UNIFORM_COMPONENTS");
        logImplementationDependentLimit(gl4, GL_MAX_COMPUTE_WORK_GROUP_INVOCATIONS, "GL_MAX_COMPUTE_WORK_GROUP_INVOCATIONS");

        logImplementationDependentLimit(gl4, GL_TEXTURE_BUFFER_OFFSET_ALIGNMENT, "GL_TEXTURE_BUFFER_OFFSET_ALIGNMENT");

        boolean validated = checkExtension(gl4, "GL_ARB_compute_shader");

        if (validated) {
            validated = initProgram(gl4);
        }
        if (validated) {
            validated = initBuffer(gl4);
        }
        if (validated) {
            validated = initTexture(gl4);
        }
        if (validated) {
            validated = initVertexArray(gl4);
        }

        return validated;
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        gl4.glGenProgramPipelines(Program.MAX, pipelineName);

        // Create program
        if (validated) {

            if (validated) {

                ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER, this.getClass(), SHADERS_ROOT,
                        null, SHADERS_SOURCE, "vert", null, true);
                ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER, this.getClass(), SHADERS_ROOT,
                        null, SHADERS_SOURCE, "frag", null, true);

                ShaderProgram shaderProgram = new ShaderProgram();
                shaderProgram.init(gl4);

                programName[Program.GRAPHICS] = shaderProgram.program();

                gl4.glProgramParameteri(programName[Program.GRAPHICS], GL_PROGRAM_SEPARABLE, GL_TRUE);

                shaderProgram.add(vertShaderCode);
                shaderProgram.add(fragShaderCode);

                shaderProgram.link(gl4, System.out);
            }

            if (validated) {

                ShaderCode compShaderCode = ShaderCode.create(gl4, GL_COMPUTE_SHADER, this.getClass(), SHADERS_ROOT, 
                        null, SHADERS_SOURCE, "comp", null, true);

                ShaderProgram shaderProgram = new ShaderProgram();
                shaderProgram.init(gl4);

                programName[Program.COMPUTE] = shaderProgram.program();

                gl4.glProgramParameteri(programName[Program.COMPUTE], GL_PROGRAM_SEPARABLE, GL_TRUE);

                shaderProgram.add(compShaderCode);

                shaderProgram.link(gl4, System.out);
            }
        }

        if (validated) {

            gl4.glUseProgramStages(pipelineName.get(Program.GRAPHICS), GL_VERTEX_SHADER_BIT | GL_FRAGMENT_SHADER_BIT,
                    programName[Program.GRAPHICS]);
            gl4.glUseProgramStages(pipelineName.get(Program.COMPUTE), GL_COMPUTE_SHADER_BIT, programName[Program.COMPUTE]);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        boolean validated = true;

        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);

        gl4.glGenBuffers(Buffer.MAX, bufferName);

        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT));
        gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
        gl4.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        IntBuffer uniformBufferOffset = GLBuffers.newDirectIntBuffer(1);
        gl4.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset);
        int uniformBlockSize = Math.max(Mat4.SIZE, uniformBufferOffset.get(0));

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.TRANSFORM));
        gl4.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_DYNAMIC_DRAW);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        BufferUtils.destroyDirectBuffer(elementBuffer);
        BufferUtils.destroyDirectBuffer(vertexBuffer);
        BufferUtils.destroyDirectBuffer(uniformBufferOffset);

        return validated;
    }

    private boolean initTexture(GL4 gl4) {

        try {
            jgli.Texture2d texture = new Texture2d(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE));
            assert (!texture.empty());
            jgli.Gl.Format format = jgli.Gl.translate(texture.format());

            gl4.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

            gl4.glGenTextures(Texture.MAX, textureName);
            gl4.glActiveTexture(GL_TEXTURE0);
            gl4.glBindTexture(GL_TEXTURE_2D, textureName.get(Texture.INPUT));
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_R, GL_RED);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_G, GL_GREEN);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_B, GL_BLUE);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_A, GL_ALPHA);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, texture.levels() - 1);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            gl4.glTexStorage2D(GL_TEXTURE_2D, texture.levels(), format.internal.value,
                    texture.dimensions()[0], texture.dimensions()[1]);
            for (int level = 0; level < texture.levels(); ++level) {
                gl4.glTexSubImage2D(GL_TEXTURE_2D, level,
                        0, 0,
                        texture.dimensions(level)[0], texture.dimensions(level)[1],
                        format.external.value, format.type.value,
                        texture.data(level));
            }

            gl4.glPixelStorei(GL_UNPACK_ALIGNMENT, 4);

            gl4.glBindTexture(GL_TEXTURE_2D, textureName.get(Texture.OUTPUT));
            gl4.glTexStorage2D(GL_TEXTURE_2D, 1, GL_RGBA8, texture.dimensions()[0], texture.dimensions()[1]);
            
            textureSize = new Vec2i(texture.dimensions()[0], texture.dimensions()[1]);

        } catch (IOException ex) {
            Logger.getLogger(Gl_430_program_compute.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(1, vertexArrayName);
        gl4.glBindVertexArray(vertexArrayName.get(0));
        {
            int bindingIndex = 0;

            gl4.glVertexAttribFormat(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 0);
            gl4.glVertexAttribFormat(Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, Vec2.SIZE);

            gl4.glVertexAttribBinding(Semantic.Attr.POSITION, bindingIndex);
            gl4.glVertexAttribBinding(Semantic.Attr.TEXCOORD, bindingIndex);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl4.glEnableVertexAttribArray(Semantic.Attr.TEXCOORD);

            gl4.glBindVertexBuffer(bindingIndex, bufferName.get(Buffer.VERTEX), 0, Vertex_v2fv2f.SIZE);

            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT));
        }
        gl4.glBindVertexArray(0);

        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        {
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.TRANSFORM));
            ByteBuffer pointer = gl4.glMapBufferRange(
                    GL_UNIFORM_BUFFER, 0, Mat4.SIZE,
                    GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            Mat4 projection = glm.perspectiveFov_((float) Math.PI * 0.25f, windowSize.x, windowSize.y, 0.1f, 100.0f);
            Mat4 model = new Mat4(1.0f);

            pointer.asFloatBuffer().put(projection.mul(viewMat4()).mul(model).toFa_());

            // Make sure the uniform buffer is uploaded
            gl4.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        gl4.glBindProgramPipeline(pipelineName.get(Program.COMPUTE));
        gl4.glActiveTexture(GL_TEXTURE0 + Semantic.Image.DIFFUSE);
        gl4.glBindTexture(GL_TEXTURE_2D, textureName.get(Texture.INPUT));
        gl4.glBindImageTexture(ImageUnit.OUTPUT, textureName.get(Texture.OUTPUT), 0, false, 0, GL_WRITE_ONLY, GL_RGBA8);
        Vec2i workGroupSize = new Vec2i(32);
        Vec2i ndRange = textureSize.div_(workGroupSize); 
        gl4.glDispatchComputeGroupSizeARB(
                ndRange.x, ndRange.y, 1, // (8, 8, 1)
                workGroupSize.x, workGroupSize.y, 1); // (32, 32, 1)
        
        gl4.glMemoryBarrier(GL_TEXTURE_FETCH_BARRIER_BIT);
        
        gl4.glViewportIndexedf(0, 0, 0, windowSize.x, windowSize.y);
        gl4.glClearBufferfv(GL_COLOR, 0, white);

        gl4.glBindProgramPipeline(pipelineName.get(Program.GRAPHICS));
        gl4.glActiveTexture(GL_TEXTURE0 + Semantic.Image.DIFFUSE);
        gl4.glBindTexture(GL_TEXTURE_2D, textureName.get(Texture.OUTPUT));
        gl4.glBindVertexArray(vertexArrayName.get(0));
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName.get(Buffer.TRANSFORM));

        gl4.glDrawElementsInstancedBaseVertexBaseInstance(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0, 0);

        return true;
    }
    
    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;
     
        gl4.glDeleteBuffers(Buffer.MAX, bufferName);
        gl4.glDeleteProgramPipelines(Program.MAX, pipelineName);
        gl4.glDeleteTextures(Texture.MAX, textureName);
        gl4.glDeleteVertexArrays(1, vertexArrayName);
        
        BufferUtils.destroyDirectBuffer(bufferName);
        BufferUtils.destroyDirectBuffer(pipelineName);
        BufferUtils.destroyDirectBuffer(textureName);
        BufferUtils.destroyDirectBuffer(vertexArrayName);
        BufferUtils.destroyDirectBuffer(white);
        BufferUtils.destroyDirectBuffer(clear);
        
        return true;
    }
}
