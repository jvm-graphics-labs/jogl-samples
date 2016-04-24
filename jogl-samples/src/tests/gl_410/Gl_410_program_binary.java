/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_410;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL3ES3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.BufferUtils;
import glm.glm;
import glm.mat._4.Mat4;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import glf.Vertex_v2fv2f;
import glm.vec._2.Vec2;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import jgli.Texture2d;

/**
 *
 * @author GBarbieri
 */
public class Gl_410_program_binary extends Test {

    public static void main(String[] args) {
        Gl_410_program_binary gl_410_program_binary = new Gl_410_program_binary();
    }

    public Gl_410_program_binary() {
        super("gl-410-program-binary", Profile.CORE, 4, 1);
    }

    private final String SHADERS_SOURCE = "binary";
    private final String SHADERS_ROOT = "src/data/gl_410";
    private final String TEXTURE_DIFFUSE = "kueken7_rgba_dxt5_unorm.dds";

    private int vertexCount = 4;
    private int vertexSize = vertexCount * Vertex_v2fv2f.SIZE;
    private float[] vertexData = {
        -1.0f, -1.0f,/**/ 0.0f, 1.0f,
        +1.0f, -1.0f,/**/ 1.0f, 1.0f,
        +1.0f, +1.0f,/**/ 1.0f, 0.0f,
        -1.0f, +1.0f,/**/ 0.0f, 0.0f};

    private int elementCount = 6;
    private int elementSize = elementCount * Integer.BYTES;
    private int[] elementData = {
        0, 1, 2,
        2, 3, 0};

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int ELEMENT = 1;
        public static final int MAX = 2;
    }

    private class Program {

        public static final int VERT = 0;
        public static final int GEOM = 1;
        public static final int FRAG = 2;
        public static final int MAX = 3;
    }

    private IntBuffer pipelineName = GLBuffers.newDirectIntBuffer(1),
            bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX), vertexArrayName = GLBuffers.newDirectIntBuffer(1),
            texture2dName = GLBuffers.newDirectIntBuffer(1);
    private int[] programName = new int[Program.MAX];
    private int uniformMvp, uniformDiffuse;

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;

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

        return validated;
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        gl4.glGenProgramPipelines(1, pipelineName);

        programName[Program.VERT] = gl4.glCreateProgram();
        gl4.glProgramParameteri(programName[Program.VERT], GL_PROGRAM_SEPARABLE, GL_TRUE);
        gl4.glProgramParameteri(programName[Program.VERT], GL_PROGRAM_BINARY_RETRIEVABLE_HINT, GL_TRUE);

        programName[Program.GEOM] = gl4.glCreateProgram();
        gl4.glProgramParameteri(programName[Program.GEOM], GL_PROGRAM_SEPARABLE, GL_TRUE);
        gl4.glProgramParameteri(programName[Program.GEOM], GL_PROGRAM_BINARY_RETRIEVABLE_HINT, GL_TRUE);

        programName[Program.FRAG] = gl4.glCreateProgram();
        gl4.glProgramParameteri(programName[Program.FRAG], GL_PROGRAM_SEPARABLE, GL_TRUE);
        gl4.glProgramParameteri(programName[Program.FRAG], GL_PROGRAM_BINARY_RETRIEVABLE_HINT, GL_TRUE);

        int[] numProgramBinaryFormats = {0};
        gl4.glGetIntegerv(GL_NUM_PROGRAM_BINARY_FORMATS, numProgramBinaryFormats, 0);

        int[] programBinaryFormats = new int[numProgramBinaryFormats[0]];
        gl4.glGetIntegerv(GL_PROGRAM_BINARY_FORMATS, programBinaryFormats, 0);

        validated = validated && numProgramBinaryFormats[0] > 0;

        try {

            validated = validated && loadShader(gl4, GL_VERTEX_SHADER);

            validated = validated && loadShader(gl4, GL_GEOMETRY_SHADER);

            validated = validated && loadShader(gl4, GL_FRAGMENT_SHADER);
            
//            validated = validated && loadVertexShader(gl4);
//
//            validated = validated && loadGeometryShader(gl4);
//
//            validated = validated && loadFragmentShader(gl4);

        } catch (IOException ex) {
            Logger.getLogger(Gl_410_program_binary.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (validated) {

            gl4.glUseProgramStages(pipelineName.get(0), GL_VERTEX_SHADER_BIT, programName[Program.VERT]);
            gl4.glUseProgramStages(pipelineName.get(0), GL_GEOMETRY_SHADER_BIT, programName[Program.GEOM]);
            gl4.glUseProgramStages(pipelineName.get(0), GL_FRAGMENT_SHADER_BIT, programName[Program.FRAG]);
            validated = validated && checkError(gl4, "initProgram - stage");
        }

        if (validated) {

            uniformMvp = gl4.glGetUniformLocation(programName[Program.VERT], "mvp");
            uniformDiffuse = gl4.glGetUniformLocation(programName[Program.FRAG], "diffuse");
        }

        return validated && checkError(gl4, "initProgram");
    }

    /**
     * We will first create the shader in the old-way, then save them as binary
     * and finally load them as binary. In this way you can also still modify
     * them if you want.
     */
    private boolean loadShader(GL4 gl4, int shaderType) throws IOException {

        IntBuffer success = GLBuffers.newDirectIntBuffer(1);

        String extension = "";
        switch (shaderType) {
            case GL_VERTEX_SHADER:
                extension = "vert";
                break;
            case GL_GEOMETRY_SHADER:
                extension = "geom";
                break;
            case GL_FRAGMENT_SHADER:
                extension = "frag";
                break;
        }

        ShaderCode vertexShaderCode = ShaderCode.create(gl4, shaderType, this.getClass(), SHADERS_ROOT, null,
                SHADERS_SOURCE, extension, null, true);
        ShaderProgram shaderProgram = new ShaderProgram();
        shaderProgram.init(gl4);
        /**
         * We need to set always the same parameters otherwise we won't get
         * GL_LINK_STATUS == GL_TRUE.
         */
        gl4.glProgramParameteri(shaderProgram.program(), GL_PROGRAM_SEPARABLE, GL_TRUE);
        gl4.glProgramParameteri(shaderProgram.program(), GL_PROGRAM_BINARY_RETRIEVABLE_HINT, GL_TRUE);
        shaderProgram.add(vertexShaderCode);
        shaderProgram.link(gl4, System.out);
        gl4.glGetProgramiv(shaderProgram.program(), GL_LINK_STATUS, success);

        if (success.get(0) != GL_TRUE) {
            return false;
        }

        IntBuffer length = GLBuffers.newDirectIntBuffer(1);
        gl4.glGetProgramiv(shaderProgram.program(), GL_PROGRAM_BINARY_LENGTH, length);

        ByteBuffer buffer = GLBuffers.newDirectByteBuffer(length.get(0));
        IntBuffer binaryFormat = GLBuffers.newDirectIntBuffer(1);
        gl4.glGetProgramBinary(shaderProgram.program(), length.get(0), length, binaryFormat, buffer);

        byte[] data = new byte[length.get(0)];
        for (int i = 0; i < buffer.capacity(); i++) {
            data[i] = buffer.get(i);
        }

        String path = SHADERS_ROOT + "/" + SHADERS_SOURCE + "." + extension + ".bin";
        Files.write(Paths.get(path), data);
        data = Files.readAllBytes(Paths.get(path));

        ByteBuffer dataBuffer = GLBuffers.newDirectByteBuffer(data);

        int program = 0;
        switch (shaderType) {
            case GL_VERTEX_SHADER:
                program = programName[Program.VERT];
                break;
            case GL_GEOMETRY_SHADER:
                program = programName[Program.GEOM];
                break;
            case GL_FRAGMENT_SHADER:
                program = programName[Program.FRAG];
                break;
        }
        
        gl4.glProgramBinary(program, binaryFormat.get(0), dataBuffer, length.get(0));
        gl4.glGetProgramiv(program, GL_LINK_STATUS, success);

        boolean validated = success.get(0) == GL_TRUE;

        BufferUtils.destroyDirectBuffer(success);
        BufferUtils.destroyDirectBuffer(length);
        BufferUtils.destroyDirectBuffer(buffer);
        BufferUtils.destroyDirectBuffer(binaryFormat);
        BufferUtils.destroyDirectBuffer(dataBuffer);

        return validated;
    }

    private boolean initTexture(GL4 gl4) {

        try {
            gl4.glGenTextures(1, texture2dName);

            gl4.glActiveTexture(GL_TEXTURE0);
            gl4.glBindTexture(GL_TEXTURE_2D, texture2dName.get(0));
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 1000);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_R, GL_RED);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_G, GL_GREEN);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_B, GL_BLUE);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_A, GL_ALPHA);

            jgli.Texture2d texture = new Texture2d(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE));
            for (int level = 0; level < texture.levels(); ++level) {
                gl4.glCompressedTexImage2D(
                        GL_TEXTURE_2D,
                        level,
                        GL_COMPRESSED_RGBA_S3TC_DXT5_EXT,
                        texture.dimensions(level)[0],
                        texture.dimensions(level)[1],
                        0,
                        texture.size(level),
                        texture.data(level));
            }
            gl4.glBindTexture(GL_TEXTURE_2D, 0);

        } catch (IOException ex) {
            Logger.getLogger(Gl_410_program_binary.class.getName()).log(Level.SEVERE, null, ex);
        }
        return checkError(gl4, "initTexture");
    }

    private boolean initBuffer(GL4 gl4) {

        IntBuffer elementBuffer = GLBuffers.newDirectIntBuffer(elementData);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);

        gl4.glGenBuffers(Buffer.MAX, bufferName);

        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT));
        gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
        gl4.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        BufferUtils.destroyDirectBuffer(elementBuffer);
        BufferUtils.destroyDirectBuffer(vertexBuffer);

        return checkError(gl4, "initBuffer");
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(1, vertexArrayName);

        gl4.glBindVertexArray(vertexArrayName.get(0));
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, Vertex_v2fv2f.SIZE, 0);
            gl4.glVertexAttribPointer(Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, Vertex_v2fv2f.SIZE, Vec2.SIZE);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl4.glEnableVertexAttribArray(Semantic.Attr.TEXCOORD);
        }
        gl4.glBindVertexArray(0);

        return checkError(gl4, "initVertexArray");
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, 4.0f / 3.0f, 0.1f, 100.0f);
        Mat4 model = new Mat4(1.0f);
        Mat4 mvp = projection.mul(viewMat4()).mul(model);

        gl4.glProgramUniformMatrix4fv(programName[Program.VERT], uniformMvp, 1, false, mvp.toFa_(), 0);
        gl4.glProgramUniform1i(programName[Program.FRAG], uniformDiffuse, 0);

        gl4.glViewportIndexedfv(0, viewport.put(0, 0).put(1, 0).put(2, windowSize.x).put(3, windowSize.y));
        gl4.glClearBufferfv(GL_COLOR, 0, clearColor.put(0, 0).put(1, 0).put(2, 0).put(3, 0));

        gl4.glBindProgramPipeline(pipelineName.get(0));

        gl4.glActiveTexture(GL_TEXTURE0);
        gl4.glBindTexture(GL_TEXTURE_2D, texture2dName.get(0));

        gl4.glBindVertexArray(vertexArrayName.get(0));
        //!\ Need to be called after glBindVertexArray
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT));
        gl4.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_INT, 0, 1, 0);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteBuffers(Buffer.MAX, bufferName);
        gl4.glDeleteVertexArrays(1, vertexArrayName);
        gl4.glDeleteTextures(1, texture2dName);
        for (int i = 0; i < Program.MAX; ++i) {
            gl4.glDeleteProgram(programName[i]);
        }
        gl4.glDeleteProgramPipelines(1, pipelineName);

        BufferUtils.destroyDirectBuffer(bufferName);
        BufferUtils.destroyDirectBuffer(vertexArrayName);
        BufferUtils.destroyDirectBuffer(pipelineName);

        return true;
    }
}
