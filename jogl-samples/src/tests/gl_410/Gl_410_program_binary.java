/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_410;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL3ES3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.Profile;
import framework.Semantic;
import framework.Test;
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
    private int vertexSize = vertexCount * 2 * 2 * Float.BYTES;
    private float[] vertexData = {
        -1.0f, -1.0f, 0.0f, 1.0f,
        +1.0f, -1.0f, 1.0f, 1.0f,
        +1.0f, +1.0f, 1.0f, 0.0f,
        -1.0f, +1.0f, 0.0f, 0.0f};

    private int elementCount = 6;
    private int elementSize = elementCount * Integer.BYTES;
    private int[] elementData = {
        0, 1, 2,
        2, 3, 0};

    private enum Buffer {
        VERTEX,
        ELEMENT,
        MAX
    }

    private enum Program {
        VERT,
        GEOM,
        FRAG,
        MAX
    }

    private int[] pipelineName = {0}, programName = new int[Program.MAX.ordinal()],
            bufferName = new int[Buffer.MAX.ordinal()], vertexArrayName = {0}, texture2dName = {0};
    private int uniformMvp, uniformDiffuse;
    private float[] projection = new float[16], model = new float[16], mvp = new float[16];

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

        gl4.glGenProgramPipelines(1, pipelineName, 0);

        programName[Program.VERT.ordinal()] = gl4.glCreateProgram();
        gl4.glProgramParameteri(programName[Program.VERT.ordinal()], GL_PROGRAM_SEPARABLE, GL_TRUE);
        gl4.glProgramParameteri(programName[Program.VERT.ordinal()], GL_PROGRAM_BINARY_RETRIEVABLE_HINT, GL_TRUE);

        programName[Program.GEOM.ordinal()] = gl4.glCreateProgram();
        gl4.glProgramParameteri(programName[Program.GEOM.ordinal()], GL_PROGRAM_SEPARABLE, GL_TRUE);
        gl4.glProgramParameteri(programName[Program.GEOM.ordinal()], GL_PROGRAM_BINARY_RETRIEVABLE_HINT, GL_TRUE);

        programName[Program.FRAG.ordinal()] = gl4.glCreateProgram();
        gl4.glProgramParameteri(programName[Program.FRAG.ordinal()], GL_PROGRAM_SEPARABLE, GL_TRUE);
        gl4.glProgramParameteri(programName[Program.FRAG.ordinal()], GL_PROGRAM_BINARY_RETRIEVABLE_HINT, GL_TRUE);

        int[] numProgramBinaryFormats = {0};
        gl4.glGetIntegerv(GL_NUM_PROGRAM_BINARY_FORMATS, numProgramBinaryFormats, 0);

        int[] programBinaryFormats = new int[numProgramBinaryFormats[0]];
        gl4.glGetIntegerv(GL_PROGRAM_BINARY_FORMATS, programBinaryFormats, 0);

        validated = validated && numProgramBinaryFormats[0] > 0;

        try {

            validated = validated && loadVertexShader(gl4);

            validated = validated && loadGeometryShader(gl4);

            validated = validated && loadFragmentShader(gl4);

        } catch (IOException ex) {
            Logger.getLogger(Gl_410_program_binary.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (validated) {

            gl4.glUseProgramStages(pipelineName[0], GL_VERTEX_SHADER_BIT, programName[Program.VERT.ordinal()]);
            gl4.glUseProgramStages(pipelineName[0], GL_GEOMETRY_SHADER_BIT, programName[Program.GEOM.ordinal()]);
            gl4.glUseProgramStages(pipelineName[0], GL_FRAGMENT_SHADER_BIT, programName[Program.FRAG.ordinal()]);
            validated = validated && checkError(gl4, "initProgram - stage");
        }

        if (validated) {

            uniformMvp = gl4.glGetUniformLocation(programName[Program.VERT.ordinal()], "mvp");
            uniformDiffuse = gl4.glGetUniformLocation(programName[Program.FRAG.ordinal()], "diffuse");
        }

        return validated && checkError(gl4, "initProgram");
    }

    /**
     * We will first create the shader in the old-way, then save them
     * as binary and finally load them as binary. 
     * In this way you can also still modify them if you want.
     */
    private boolean loadVertexShader(GL4 gl4) throws IOException {

        int[] success = {0};

        ShaderCode vertexShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER, this.getClass(),
                SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
        ShaderProgram shaderProgram = new ShaderProgram();
        shaderProgram.init(gl4);
        /**
         * We need to set always the same parameters otherwise we won't
         * get GL_LINK_STATUS == GL_TRUE.
         */
        gl4.glProgramParameteri(shaderProgram.program(), GL_PROGRAM_SEPARABLE, GL_TRUE);
        gl4.glProgramParameteri(shaderProgram.program(), GL_PROGRAM_BINARY_RETRIEVABLE_HINT, GL_TRUE);
        shaderProgram.add(vertexShaderCode);
        shaderProgram.link(gl4, System.out);
        gl4.glGetProgramiv(shaderProgram.program(), GL_LINK_STATUS, success, 0);

        if (success[0] != GL_TRUE) {
            return false;
        }

        int[] length = {0};
        gl4.glGetProgramiv(shaderProgram.program(), GL_PROGRAM_BINARY_LENGTH, length, 0);

        ByteBuffer buffer = GLBuffers.newDirectByteBuffer(length[0]);
        int[] binaryFormat = {0};
        gl4.glGetProgramBinary(shaderProgram.program(), length[0], length, 0, binaryFormat, 0, buffer);

        byte[] data = new byte[length[0]];
        for (int i = 0; i < buffer.capacity(); i++) {
            data[i] = buffer.get(i);
        }

        String path = SHADERS_ROOT + "/" + SHADERS_SOURCE + ".vert.bin";
        Files.write(Paths.get(path), data);
        data = Files.readAllBytes(Paths.get(path));

        gl4.glProgramBinary(programName[Program.VERT.ordinal()], binaryFormat[0],
                GLBuffers.newDirectByteBuffer(data), length[0]);
        gl4.glGetProgramiv(programName[Program.VERT.ordinal()], GL_LINK_STATUS, success, 0);

        return success[0] == GL_TRUE;
    }

    private boolean loadGeometryShader(GL4 gl4) throws IOException {

        int[] success = {0};

        ShaderCode geometryShaderCode = ShaderCode.create(gl4, GL_GEOMETRY_SHADER, this.getClass(),
                SHADERS_ROOT, null, SHADERS_SOURCE, "geom", null, true);
        ShaderProgram shaderProgram = new ShaderProgram();
        shaderProgram.init(gl4);
        gl4.glProgramParameteri(shaderProgram.program(), GL_PROGRAM_SEPARABLE, GL_TRUE);
        gl4.glProgramParameteri(shaderProgram.program(), GL_PROGRAM_BINARY_RETRIEVABLE_HINT, GL_TRUE);
        shaderProgram.add(geometryShaderCode);
        shaderProgram.link(gl4, System.out);
        gl4.glGetProgramiv(shaderProgram.program(), GL_LINK_STATUS, success, 0);

        if (success[0] != GL_TRUE) {
            return false;
        }

        int[] length = {0};
        gl4.glGetProgramiv(shaderProgram.program(), GL_PROGRAM_BINARY_LENGTH, length, 0);

        ByteBuffer buffer = GLBuffers.newDirectByteBuffer(length[0]);
        int[] binaryFormat = {0};
        gl4.glGetProgramBinary(shaderProgram.program(), length[0], length, 0, binaryFormat, 0, buffer);

        byte[] data = new byte[length[0]];
        for (int i = 0; i < buffer.capacity(); i++) {
            data[i] = buffer.get(i);
        }

        String path = SHADERS_ROOT + "/" + SHADERS_SOURCE + ".geom.bin";
        Files.write(Paths.get(path), data);
        data = Files.readAllBytes(Paths.get(path));

        gl4.glProgramBinary(programName[Program.GEOM.ordinal()], binaryFormat[0],
                GLBuffers.newDirectByteBuffer(data), length[0]);
        gl4.glGetProgramiv(programName[Program.GEOM.ordinal()], GL_LINK_STATUS, success, 0);

        return success[0] == GL_TRUE;
    }

    private boolean loadFragmentShader(GL4 gl4) throws IOException {

        int[] success = {0};

        ShaderCode fragmentShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER, this.getClass(),
                SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);
        ShaderProgram shaderProgram = new ShaderProgram();
        shaderProgram.init(gl4);
        gl4.glProgramParameteri(shaderProgram.program(), GL_PROGRAM_SEPARABLE, GL_TRUE);
        gl4.glProgramParameteri(shaderProgram.program(), GL_PROGRAM_BINARY_RETRIEVABLE_HINT, GL_TRUE);
        shaderProgram.add(fragmentShaderCode);
        shaderProgram.link(gl4, System.out);
        gl4.glGetProgramiv(shaderProgram.program(), GL_LINK_STATUS, success, 0);

        if (success[0] != GL_TRUE) {
            return false;
        }

        int[] length = {0};
        gl4.glGetProgramiv(shaderProgram.program(), GL_PROGRAM_BINARY_LENGTH, length, 0);

        ByteBuffer buffer = GLBuffers.newDirectByteBuffer(length[0]);
        int[] binaryFormat = {0};
        gl4.glGetProgramBinary(shaderProgram.program(), length[0], length, 0, binaryFormat, 0, buffer);

        byte[] data = new byte[length[0]];
        for (int i = 0; i < buffer.capacity(); i++) {
            data[i] = buffer.get(i);
        }

        String path = SHADERS_ROOT + "/" + SHADERS_SOURCE + ".frag.bin";
        Files.write(Paths.get(path), data);
        data = Files.readAllBytes(Paths.get(path));

        gl4.glProgramBinary(programName[Program.FRAG.ordinal()], binaryFormat[0],
                GLBuffers.newDirectByteBuffer(data), length[0]);
        gl4.glGetProgramiv(programName[Program.FRAG.ordinal()], GL_LINK_STATUS, success, 0);

        return success[0] == GL_TRUE;
    }

    private boolean initTexture(GL4 gl4) {

        try {
            gl4.glGenTextures(1, texture2dName, 0);

            gl4.glActiveTexture(GL_TEXTURE0);
            gl4.glBindTexture(GL_TEXTURE_2D, texture2dName[0]);
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

        gl4.glGenBuffers(Buffer.MAX.ordinal(), bufferName, 0);

        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT.ordinal()]);
        IntBuffer elementBuffer = GLBuffers.newDirectIntBuffer(elementData);
        gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl4.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        return checkError(gl4, "initBuffer");
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
        }
        gl4.glBindVertexArray(0);

        return checkError(gl4, "initVertexArray");
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        FloatUtil.makePerspective(projection, 0, true, (float) Math.PI * 0.25f, 4.0f / 3.0f, 0.1f, 100.0f);
        FloatUtil.makeIdentity(model);
        FloatUtil.multMatrix(projection, view(), mvp);
        FloatUtil.multMatrix(mvp, model);

        gl4.glProgramUniformMatrix4fv(programName[Program.VERT.ordinal()], uniformMvp, 1, false, mvp, 0);
        gl4.glProgramUniform1i(programName[Program.FRAG.ordinal()], uniformDiffuse, 0);

        gl4.glViewportIndexedfv(0, new float[]{0, 0, windowSize.x, windowSize.y}, 0);
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{0.0f, 0.0f, 0.0f, 0.0f}, 0);

        gl4.glBindProgramPipeline(pipelineName[0]);

        gl4.glActiveTexture(GL_TEXTURE0);
        gl4.glBindTexture(GL_TEXTURE_2D, texture2dName[0]);

        gl4.glBindVertexArray(vertexArrayName[0]);
        //!\ Need to be called after glBindVertexArray
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT.ordinal()]);
        gl4.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_INT, 0, 1, 0);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteBuffers(Buffer.MAX.ordinal(), bufferName, 0);
        gl4.glDeleteVertexArrays(1, vertexArrayName, 0);
        gl4.glDeleteTextures(1, texture2dName, 0);
        for (int i = 0; i < Program.MAX.ordinal(); ++i) {
            gl4.glDeleteProgram(programName[i]);
        }
        gl4.glDeleteProgramPipelines(1, pipelineName, 0);

        return true;
    }
}
