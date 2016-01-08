/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_410;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_TRUE;
import static com.jogamp.opengl.GL2ES2.GL_LINK_STATUS;
import static com.jogamp.opengl.GL2ES2.GL_NUM_PROGRAM_BINARY_FORMATS;
import static com.jogamp.opengl.GL2ES2.GL_PROGRAM_BINARY_FORMATS;
import static com.jogamp.opengl.GL2ES2.GL_PROGRAM_BINARY_LENGTH;
import static com.jogamp.opengl.GL2ES2.GL_PROGRAM_SEPARABLE;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES3.GL_PROGRAM_BINARY_RETRIEVABLE_HINT;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.Profile;
import framework.Test;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;

        if (validated) {
            validated = initProgram(gl4);
        }
//		if(validated)
//			validated = initBuffer();
//		if(validated)
//			validated = initVertexArray();
//		if(validated)
//			validated = initTexture();

        return validated;
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        int[] success = {0};

        gl4.glGenProgramPipelines(1, pipelineName, 0);

        programName[Program.VERT.ordinal()] = gl4.glCreateProgram();
        gl4.glProgramParameteri(programName[Program.VERT.ordinal()], GL_PROGRAM_SEPARABLE, GL_TRUE);
        gl4.glProgramParameteri(programName[Program.VERT.ordinal()], GL_PROGRAM_BINARY_RETRIEVABLE_HINT, GL_TRUE);

//        programName[Program.GEOM.ordinal()] = gl4.glCreateProgram();
//        gl4.glProgramParameteri(programName[Program.GEOM.ordinal()], GL_PROGRAM_SEPARABLE, GL_TRUE);
//        gl4.glProgramParameteri(programName[Program.GEOM.ordinal()], GL_PROGRAM_BINARY_RETRIEVABLE_HINT, GL_TRUE);
//
//        programName[Program.FRAG.ordinal()] = gl4.glCreateProgram();
//        gl4.glProgramParameteri(programName[Program.FRAG.ordinal()], GL_PROGRAM_SEPARABLE, GL_TRUE);
//        gl4.glProgramParameteri(programName[Program.FRAG.ordinal()], GL_PROGRAM_BINARY_RETRIEVABLE_HINT, GL_TRUE);
        int[] numProgramBinaryFormats = {0};
        gl4.glGetIntegerv(GL_NUM_PROGRAM_BINARY_FORMATS, numProgramBinaryFormats, 0);

        int[] programBinaryFormats = new int[numProgramBinaryFormats[0]];
        gl4.glGetIntegerv(GL_PROGRAM_BINARY_FORMATS, programBinaryFormats, 0);

        validated = validated && numProgramBinaryFormats[0] > 0;

        try {

            {
                ShaderCode vertexShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER, this.getClass(),
                        SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
                ShaderProgram shaderProgram = new ShaderProgram();
                shaderProgram.init(gl4);
                shaderProgram.add(vertexShaderCode);
                shaderProgram.link(gl4, System.out);

                System.out.println("program status: " + checkProgram(gl4, shaderProgram.program()));

                int[] length = {0};

                gl4.glGetProgramiv(shaderProgram.program(), GL_PROGRAM_BINARY_LENGTH, length, 0);

                byte[] data = new byte[length[0]];
                ByteBuffer buffer = GLBuffers.newDirectByteBuffer(data.length);
                int[] binaryFormat = {0};
                gl4.glGetProgramBinary(shaderProgram.program(), data.length, length, 0, binaryFormat, 0, buffer);
                /**
                 *
                 */
                String path = SHADERS_ROOT + "/" + SHADERS_SOURCE + ".vert.bin";
                Files.write(Paths.get(path), data);
                data = Files.readAllBytes(Paths.get(path));
                gl4.glProgramBinary(programName[Program.VERT.ordinal()], binaryFormat[0], 
                        GLBuffers.newDirectByteBuffer(data), data.length);
                gl4.glGetProgramiv(programName[Program.VERT.ordinal()], GL_LINK_STATUS, success, 0);
                System.out.println("");
            }
        } catch (IOException ex) {
            Logger.getLogger(Gl_410_program_binary.class.getName()).log(Level.SEVERE, null, ex);
        }

        return validated && checkError(gl4, "initProgram");
    }
}
