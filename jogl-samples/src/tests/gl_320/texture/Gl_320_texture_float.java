/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_320.texture;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2ES3.*;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import glm.glm;
import framework.Glm;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import dev.Vec2i;
import framework.BufferUtils;
import glm.mat._4.Mat4;
import glm.vec._2.Vec2;

/**
 *
 * @author GBarbieri
 */
public class Gl_320_texture_float extends Test {

    public static void main(String[] args) {
        Gl_320_texture_float gl_320_texture_float = new Gl_320_texture_float();
    }

    public Gl_320_texture_float() {
        super("gl-320-texture-float", Profile.CORE, 3, 2, new Vec2i(1280, 1280));
    }

    private final String SHADERS_SOURCE = "texture-float";
    private final String SHADERS_ROOT = "src/data/gl_320/texture";

    private int vertexCount = 4;
    private int vertexSize = vertexCount * glf.Vertex_v2fv2f.SIZE;
    float scale = 0.8f;
    private float[] vertexData = {
        -scale, -scale, 0.0f, 1.0f,
        +scale, -scale, 1.0f, 1.0f,
        +scale, +scale, 1.0f, 0.0f,
        -scale, +scale, 0.0f, 0.0f};

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

    private class Shader {

        public static final int VERT = 0;
        public static final int FRAG = 1;
        public static final int MAX = 2;
    }

    private int[][] set1 = {
        {
            4, 0, 1, 2, 1, 7, 0, 7,
            7, 1, 0, 5, 6, 7, 7, 3,
            1, 5, 2, 5, 7, 2, 6, 1,
            1, 3, 3, 3, 0, 2, 4, 0,
            7, 5, 0, 6, 1, 2, 0, 3,
            7, 2, 4, 1, 7, 3, 4, 3,
            4, 3, 5, 1, 5, 2, 3, 6,
            7, 2, 2, 2, 5, 5, 6, 6},
        {
            0, 2, 3, 4, 1, 6, 5, 7,
            5, 4, 0, 5, 3, 7, 2, 1,
            6, 7, 1, 2, 5, 4, 3, 0,
            1, 5, 4, 3, 7, 0, 6, 2,
            3, 6, 7, 0, 2, 1, 4, 5,
            2, 0, 5, 1, 4, 3, 7, 6,
            4, 1, 2, 7, 6, 5, 0, 3,
            7, 3, 6, 5, 0, 2, 1, 4},
        {
            2, 3, 4, 5, 7, 2, 7, 5,
            6, 7, 1, 3, 6, 6, 2, 0,
            6, 7, 5, 7, 6, 6, 2, 4,
            6, 3, 1, 7, 6, 6, 7, 1,
            7, 0, 4, 3, 2, 2, 5, 1,
            0, 1, 4, 5, 3, 5, 4, 1,
            0, 0, 5, 5, 0, 4, 1, 1,
            2, 2, 7, 5, 4, 1, 1, 4},
        {
            0, 7, 6, 7, 3, 6, 7, 3,
            2, 6, 3, 6, 1, 3, 6, 1,
            3, 5, 4, 5, 0, 4, 5, 0,
            1, 4, 0, 4, 2, 0, 4, 2,
            2, 6, 3, 6, 1, 3, 6, 1,
            3, 5, 4, 5, 0, 4, 5, 0,
            2, 6, 3, 6, 1, 3, 6, 1,
            6, 1, 5, 1, 4, 5, 1, 4}
    };

    private int[][] set2 = {
        {
            4, 0, 1, 0, 7, 1, 4, 1,
            3, 7, 4, 7, 3, 6, 3, 0,
            1, 6, 5, 5, 6, 1, 7, 2,
            0, 5, 5, 4, 0, 2, 2, 1,
            7, 5, 7, 7, 0, 1, 4, 7,
            0, 7, 6, 7, 2, 3, 4, 6,
            3, 1, 3, 1, 0, 5, 3, 1,
            3, 1, 1, 0, 5, 5, 5, 3},
        {
            7, 3, 6, 0, 5, 4, 1, 2,
            4, 5, 2, 1, 7, 3, 6, 0,
            3, 6, 1, 4, 2, 0, 7, 5,
            2, 0, 7, 5, 6, 1, 4, 3,
            0, 7, 3, 6, 4, 5, 2, 1,
            5, 1, 4, 2, 3, 6, 0, 7,
            6, 2, 0, 3, 1, 7, 5, 4,
            1, 4, 5, 7, 0, 2, 3, 6},
        {
            2, 3, 4, 5, 7, 2, 7, 5,
            6, 7, 1, 3, 6, 6, 2, 0,
            6, 7, 5, 7, 6, 6, 2, 4,
            6, 3, 1, 7, 6, 6, 7, 1,
            7, 0, 4, 3, 2, 2, 5, 1,
            0, 1, 4, 5, 3, 5, 4, 1,
            0, 0, 5, 5, 0, 4, 1, 1,
            2, 2, 7, 5, 4, 1, 1, 4},
        {
            0, 7, 6, 7, 3, 6, 7, 3,
            2, 6, 3, 6, 1, 3, 6, 1,
            3, 5, 4, 5, 0, 4, 5, 0,
            1, 4, 0, 4, 2, 0, 4, 2,
            2, 6, 3, 6, 1, 3, 6, 1,
            3, 5, 4, 5, 0, 4, 5, 0,
            2, 6, 3, 6, 1, 3, 6, 1,
            6, 1, 5, 1, 4, 5, 1, 4}};

    private int[][] set3 = {
        {
            0, 4, 3, 4, 7, 2, 6, 5,
            3, 3, 5, 1, 2, 0, 3, 1,
            0, 2, 3, 2, 5, 3, 7, 4,
            3, 2, 5, 4, 3, 7, 3, 6,
            1, 7, 6, 1, 0, 6, 2, 3,
            2, 7, 1, 4, 5, 0, 4, 5,
            6, 6, 3, 3, 4, 5, 5, 4,
            1, 6, 4, 4, 3, 4, 3, 3},
        {
            6, 0, 7, 3, 2, 4, 5, 1,
            4, 1, 2, 5, 0, 7, 3, 6,
            7, 3, 5, 6, 4, 1, 0, 2,
            2, 7, 4, 0, 1, 3, 6, 5,
            0, 5, 1, 7, 6, 2, 4, 3,
            1, 2, 3, 4, 5, 6, 7, 0,
            5, 4, 6, 2, 3, 0, 1, 7,
            3, 6, 0, 1, 7, 5, 2, 4},
        {
            2, 3, 4, 5, 7, 2, 7, 5,
            6, 7, 1, 3, 6, 6, 2, 0,
            6, 7, 5, 7, 6, 6, 2, 4,
            6, 3, 1, 7, 6, 6, 7, 1,
            7, 0, 4, 3, 2, 2, 5, 1,
            0, 1, 4, 5, 3, 5, 4, 1,
            0, 0, 5, 5, 0, 4, 1, 1,
            2, 2, 7, 5, 4, 1, 1, 4},
        {
            0, 7, 6, 7, 3, 6, 7, 3,
            2, 6, 3, 6, 1, 3, 6, 1,
            3, 5, 4, 5, 0, 4, 5, 0,
            1, 4, 0, 4, 2, 0, 4, 2,
            2, 6, 3, 6, 1, 3, 6, 1,
            3, 5, 4, 5, 0, 4, 5, 0,
            2, 6, 3, 6, 1, 3, 6, 1,
            6, 1, 5, 1, 4, 5, 1, 4}};

    private int[][] set4 = {
        {
            2, 4, 7, 5, 0, 1, 6, 0,
            0, 5, 7, 0, 6, 1, 0, 3,
            3, 7, 3, 0, 6, 6, 4, 1,
            7, 5, 0, 4, 6, 0, 7, 2,
            4, 3, 6, 6, 5, 3, 6, 0,
            3, 2, 2, 1, 6, 0, 1, 2,
            2, 4, 6, 4, 1, 7, 6, 3,
            2, 5, 7, 6, 2, 5, 6, 1},
        {
            6, 7, 2, 3, 1, 5, 0, 4,
            4, 3, 0, 5, 7, 6, 1, 2,
            1, 5, 7, 6, 2, 4, 0, 3,
            0, 2, 1, 4, 3, 7, 6, 5,
            3, 4, 6, 2, 5, 0, 7, 1,
            7, 0, 5, 1, 4, 3, 2, 6,
            2, 6, 4, 7, 0, 1, 5, 3,
            5, 1, 3, 0, 6, 2, 4, 7},
        {
            2, 3, 4, 5, 7, 2, 7, 5,
            6, 7, 1, 3, 6, 6, 2, 0,
            6, 7, 5, 7, 6, 6, 2, 4,
            6, 3, 1, 7, 6, 6, 7, 1,
            7, 0, 4, 3, 2, 2, 5, 1,
            0, 1, 4, 5, 3, 5, 4, 1,
            0, 0, 5, 5, 0, 4, 1, 1,
            2, 2, 7, 5, 4, 1, 1, 4},
        {
            0, 7, 6, 7, 3, 6, 7, 3,
            2, 6, 3, 6, 1, 3, 6, 1,
            3, 5, 4, 5, 0, 4, 5, 0,
            1, 4, 0, 4, 2, 0, 4, 2,
            2, 6, 3, 6, 1, 3, 6, 1,
            3, 5, 4, 5, 0, 4, 5, 0,
            2, 6, 3, 6, 1, 3, 6, 1,
            6, 1, 5, 1, 4, 5, 1, 4}};

    private int[] vertexArrayName = {0}, textureName = {0}, bufferName = new int[Buffer.MAX];
    private int programName, uniformLayer;

    @Override
    protected boolean begin(GL gl) {

        GL3 gl3 = (GL3) gl;

        boolean validated = true;

        if (validated) {
            validated = initTexture(gl3);
        }
        if (validated) {
            validated = initProgram(gl3);
        }
        if (validated) {
            validated = initBuffer(gl3);
        }
        if (validated) {
            validated = initVertexArray(gl3);
        }

        {
            gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
            ByteBuffer pointer = gl3.glMapBufferRange(GL_UNIFORM_BUFFER, 0, Mat4.SIZE,
                    GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            pointer.asFloatBuffer().put(glm.ortho_(-1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f).toFa_());

            gl3.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        gl3.glUseProgram(programName);

        gl3.glActiveTexture(GL_TEXTURE0);
        gl3.glBindTexture(GL_TEXTURE_2D_ARRAY, textureName[0]);
        gl3.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.TRANSFORM]);
        gl3.glBindVertexArray(vertexArrayName[0]);

        return validated && checkError(gl3, "begin");
    }

    private boolean initProgram(GL3 gl3) {

        boolean validated = true;

        if (validated) {

            ShaderCode vertShaderCode = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

            ShaderProgram shaderProgram = new ShaderProgram();
            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);

            shaderProgram.init(gl3);

            programName = shaderProgram.program();

            gl3.glBindAttribLocation(programName, Semantic.Attr.POSITION, "position");
            gl3.glBindAttribLocation(programName, Semantic.Attr.TEXCOORD, "texCoord");
            gl3.glBindFragDataLocation(programName, Semantic.Frag.COLOR, "color");

            shaderProgram.link(gl3, System.out);
        }
        if (validated) {

            gl3.glUniformBlockBinding(
                    programName,
                    gl3.glGetUniformBlockIndex(programName, "Transform"),
                    Semantic.Uniform.TRANSFORM0);

            gl3.glUseProgram(programName);
            gl3.glUniform1i(gl3.glGetUniformLocation(programName, "diffuse"), 0);
            uniformLayer = gl3.glGetUniformLocation(programName, "layer");
            gl3.glUseProgram(0);
        }

        return validated & checkError(gl3, "initProgram");
    }

    private boolean initBuffer(GL3 gl3) {

        gl3.glGenBuffers(Buffer.MAX, bufferName, 0);

        gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT]);
        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        gl3.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(elementBuffer);
        gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl3.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(vertexBuffer);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        int[] uniformBufferOffset = {0};
        gl3.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset, 0);
        int uniformBlockSize = Math.max(Mat4.SIZE, uniformBufferOffset[0]);

        gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
        gl3.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_DYNAMIC_DRAW);
        gl3.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return checkError(gl3, "initBuffer");
    }

    private boolean initTexture(GL3 gl3) {

        int size = 8;

        byte[][] colorSRGB = {
            {(byte) 225, (byte) 0, (byte) 152}, // Rhodamine Red C
            {(byte) 239, (byte) 51, (byte) 64}, // Red 032 C
            {(byte) 254, (byte) 80, (byte) 0}, // Orange 021 C
            {(byte) 254, (byte) 221, (byte) 0}, // Yellow C
            {(byte) 0, (byte) 172, (byte) 140}, // Green C
            {(byte) 0, (byte) 131, (byte) 195}, // Process Blue C
            {(byte) 63, (byte) 67, (byte) 173}, // Blue 072 C
            {(byte) 187, (byte) 41, (byte) 187} // Purple C
        };

        byte[][] colorInstance2Comp3 = {
            {(byte) 239, (byte) 51, (byte) 64}, // Red 032 C
            {(byte) 0, (byte) 132, (byte) 61}, // 348 C (Green)
            {(byte) 214, (byte) 37, (byte) 152}, // Pink C
            {(byte) 0, (byte) 133, (byte) 202}, // Process Blue C
            {(byte) 255, (byte) 215, (byte) 0}, // Yellow 012 C
            {(byte) 78, (byte) 0, (byte) 142}, // Medium Purple C
            {(byte) 254, (byte) 80, (byte) 0}, // Orange 021 C
            {(byte) 16, (byte) 6, (byte) 159} // Blue 072 C
        };

        byte[][] colorGNI = {
            {(byte) 214, (byte) 37, (byte) 152}, // Pink C
            //glm::u8vec3(225,   0, 152), // Rhodamine Red C
            //glm::u8vec3(249,  56,  34), // Bright Red 032 C
            {(byte) 239, (byte) 51, (byte) 64}, // Red 032 C
            //glm::u8vec3(255,  94,   0), // Bright Orange C
            {(byte) 254, (byte) 80, (byte) 0}, // Orange 021 C
            {(byte) 255, (byte) 215, (byte) 0}, // Yellow 012 C
            //glm::u8vec3(0, 171, 132),  // Green C
            {(byte) 0, (byte) 132, (byte) 61}, // 348 C (Green)
            {(byte) 0, (byte) 133, (byte) 202}, // Process Blue C
            {(byte) 16, (byte) 6, (byte) 159}, // Blue 072 C
            {(byte) 78, (byte) 0, (byte) 142} // Medium Purple C
        };

        byte[][] color = {
            {(byte) 214, (byte) 37, (byte) 152}, // 0, Pink C
            {(byte) 239, (byte) 51, (byte) 64}, // 1, Red 032 C
            {(byte) 254, (byte) 80, (byte) 0}, // 2, Orange 021 C
            {(byte) 255, (byte) 215, (byte) 0}, // 3, Yellow 012 C
            {(byte) 0, (byte) 132, (byte) 61}, // 4, 348 C (Green)
            {(byte) 0, (byte) 133, (byte) 202}, // 5, Process Blue C
            {(byte) 16, (byte) 6, (byte) 159}, // 6, Blue 072 C
            {(byte) 78, (byte) 0, (byte) 142} // 7, Medium Purple C
        };

        float[] a = {254, 80, 0};
        float[] b = Glm.div(a, 255.0f);
        float[] hsvColor = Glm.hsvColor(b);
        float[][] tmp = {
            {1.0f, 0.65f, 1.0f},
            {1.0f, 0.70f, 1.0f},
            {1.0f, 0.75f, 1.0f},
            {1.0f, 0.80f, 1.0f},
            {1.0f, 0.85f, 1.0f},
            {1.0f, 0.90f, 1.0f},
            {1.0f, 0.95f, 1.0f},
            {1.0f, 1.00f, 1.0f}};
        for (int i = 0; i < tmp.length; i++) {
            tmp[i] = Glm.mult(hsvColor, tmp[i]);
            tmp[i] = Glm.rgbColor(tmp[i]);
            tmp[i] = Glm.mult(tmp[i], 255.0f);
        }
        byte[][] colorOrange = {
            {(byte) tmp[0][0], (byte) tmp[0][1], (byte) tmp[0][2]}, // 0
            {(byte) tmp[1][0], (byte) tmp[1][1], (byte) tmp[1][2]}, // 1
            {(byte) tmp[2][0], (byte) tmp[2][1], (byte) tmp[2][2]}, // 2
            {(byte) tmp[3][0], (byte) tmp[3][1], (byte) tmp[3][2]}, // 3
            {(byte) tmp[4][0], (byte) tmp[4][1], (byte) tmp[4][2]}, // 4
            {(byte) tmp[5][0], (byte) tmp[5][1], (byte) tmp[5][2]}, // 5
            {(byte) tmp[6][0], (byte) tmp[6][1], (byte) tmp[6][2]}, // 6
            {(byte) tmp[7][0], (byte) tmp[7][1], (byte) tmp[7][2]}, // 7
        };

        tmp = new float[][]{
            {1.0f, 0.90f, 1.0f},
            {1.0f, 0.90f, 1.0f},
            {1.0f, 0.90f, 1.0f},
            {1.0f, 0.90f, 1.0f},
            {1.0f, 1.00f, 1.0f},
            {1.0f, 1.00f, 1.0f},
            {1.0f, 1.00f, 1.0f},
            {1.0f, 1.00f, 1.0f}};
        for (int i = 0; i < tmp.length; i++) {
            tmp[i] = Glm.mult(hsvColor, tmp[i]);
            tmp[i] = Glm.rgbColor(tmp[i]);
            tmp[i] = Glm.mult(tmp[i], 255.0f);
        }

        byte[][] colorOrange2 = {
            {(byte) tmp[0][0], (byte) tmp[0][1], (byte) tmp[0][2]}, // 3
            {(byte) tmp[1][0], (byte) tmp[1][1], (byte) tmp[1][2]}, // 3
            {(byte) tmp[2][0], (byte) tmp[2][1], (byte) tmp[2][2]}, // 3
            {(byte) tmp[3][0], (byte) tmp[3][1], (byte) tmp[3][2]}, // 3
            {(byte) tmp[4][0], (byte) tmp[4][1], (byte) tmp[4][2]}, // 7
            {(byte) tmp[5][0], (byte) tmp[5][1], (byte) tmp[5][2]}, // 7
            {(byte) tmp[6][0], (byte) tmp[6][1], (byte) tmp[6][2]}, // 7
            {(byte) tmp[7][0], (byte) tmp[7][1], (byte) tmp[7][2]}, // 7
        };

        a[0] = 255;
        a[1] = 128;
        b = Glm.div(a, 255.0f);
        float[][] saturation = new float[8][3];
        for (int i = 0; i < saturation.length; i++) {
            saturation[i] = Glm.saturation(0.30f + 0.10f * i, b);
            saturation[i] = Glm.mult(saturation[i], 255.0f);
        }

        byte[][] ColorOrange3 = {
            {(byte) tmp[0][0], (byte) tmp[0][1], (byte) tmp[0][2]}, // 0
            {(byte) tmp[1][0], (byte) tmp[1][1], (byte) tmp[1][2]}, // 1
            {(byte) tmp[2][0], (byte) tmp[2][1], (byte) tmp[2][2]}, // 2
            {(byte) tmp[3][0], (byte) tmp[3][1], (byte) tmp[3][2]}, // 3
            {(byte) tmp[4][0], (byte) tmp[4][1], (byte) tmp[4][2]}, // 4
            {(byte) tmp[5][0], (byte) tmp[5][1], (byte) tmp[5][2]}, // 5
            {(byte) tmp[6][0], (byte) tmp[6][1], (byte) tmp[6][2]}, // 6
            {(byte) tmp[7][0], (byte) tmp[7][1], (byte) tmp[7][2]}, // 7
        };

        gl3.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        gl3.glGenTextures(1, textureName, 0);

        gl3.glActiveTexture(GL_TEXTURE0);
        gl3.glBindTexture(GL_TEXTURE_2D_ARRAY, textureName[0]);
        gl3.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_BASE_LEVEL, 0);
        gl3.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAX_LEVEL, 0);
        gl3.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        gl3.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        gl3.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        gl3.glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        gl3.glTexImage3D(GL_TEXTURE_2D_ARRAY, 0, GL_RGB8, size, size, 16, 0, GL_RGB, GL_UNSIGNED_BYTE, null);

        byte[][] c = color;
        ByteBuffer dataBuffer = GLBuffers.newDirectByteBuffer(3 * 8 * 8);
        // Instance 3
        {
            byte[] data1 = buildColorChart(c, set1[0]);
            dataBuffer.put(data1).rewind();
            gl3.glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0,
                    0, 0, 0,
                    size, size, 1,
                    GL_RGB, GL_UNSIGNED_BYTE, dataBuffer);
            byte[] data2 = buildColorChart(c, set1[1]);
            dataBuffer.put(data2).rewind();
            gl3.glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0,
                    0, 0, 1,
                    size, size, 1,
                    GL_RGB, GL_UNSIGNED_BYTE, dataBuffer);
            byte[] data3 = buildColorChart(c, set1[2]);
            dataBuffer.put(data3).rewind();
            gl3.glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0,
                    0, 0, 2,
                    size, size, 1,
                    GL_RGB, GL_UNSIGNED_BYTE, dataBuffer);
            byte[] data4 = buildColorChart(c, set1[3]);
            dataBuffer.put(data4).rewind();
            gl3.glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0,
                    0, 0, 3,
                    size, size, 1,
                    GL_RGB, GL_UNSIGNED_BYTE, dataBuffer);
        }

        // Instance 4
        {
            byte[] data1 = buildColorChart(c, set2[0]);
            dataBuffer.put(data1).rewind();
            gl3.glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0,
                    0, 0, 4,
                    size, size, 1,
                    GL_RGB, GL_UNSIGNED_BYTE, dataBuffer);
            byte[] data2 = buildColorChart(c, set2[1]);
            dataBuffer.put(data2).rewind();
            gl3.glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0,
                    0, 0, 5,
                    size, size, 1,
                    GL_RGB, GL_UNSIGNED_BYTE, dataBuffer);
            byte[] data3 = buildColorChart(c, set2[2]);
            dataBuffer.put(data3).rewind();
            gl3.glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0,
                    0, 0, 6,
                    size, size, 1,
                    GL_RGB, GL_UNSIGNED_BYTE, dataBuffer);
            byte[] data4 = buildColorChart(c, set2[3]);
            dataBuffer.put(data4).rewind();
            gl3.glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0,
                    0, 0, 7,
                    size, size, 1,
                    GL_RGB, GL_UNSIGNED_BYTE, dataBuffer);
        }

        // Instance 5
        {
            byte[] data1 = buildColorChart(c, set3[0]);
            dataBuffer.put(data1).rewind();
            gl3.glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0,
                    0, 0, 8,
                    size, size, 1,
                    GL_RGB, GL_UNSIGNED_BYTE, dataBuffer);
            byte[] data2 = buildColorChart(c, set3[1]);
            dataBuffer.put(data2).rewind();
            gl3.glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0,
                    0, 0, 9,
                    size, size, 1,
                    GL_RGB, GL_UNSIGNED_BYTE, dataBuffer);
            byte[] data3 = buildColorChart(c, set3[2]);
            dataBuffer.put(data3).rewind();
            gl3.glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0,
                    0, 0, 10,
                    size, size, 1,
                    GL_RGB, GL_UNSIGNED_BYTE, dataBuffer);
            byte[] data4 = buildColorChart(c, set3[3]);
            dataBuffer.put(data4).rewind();
            gl3.glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0,
                    0, 0, 11,
                    size, size, 1,
                    GL_RGB, GL_UNSIGNED_BYTE, dataBuffer);
        }

        // Instance 6
        {
            byte[] data1 = buildColorChart(c, set4[0]);
            dataBuffer.put(data1).rewind();
            gl3.glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0,
                    0, 0, 12,
                    size, size, 1,
                    GL_RGB, GL_UNSIGNED_BYTE, dataBuffer);
            byte[] data2 = buildColorChart(c, set4[1]);
            dataBuffer.put(data2).rewind();
            gl3.glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0,
                    0, 0, 13,
                    size, size, 1,
                    GL_RGB, GL_UNSIGNED_BYTE, dataBuffer);
            byte[] data3 = buildColorChart(c, set4[2]);
            dataBuffer.put(data3).rewind();
            gl3.glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0,
                    0, 0, 14,
                    size, size, 1,
                    GL_RGB, GL_UNSIGNED_BYTE, dataBuffer);
            byte[] data4 = buildColorChart(c, set4[3]);
            dataBuffer.put(data4).rewind();
            gl3.glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0,
                    0, 0, 15,
                    size, size, 1,
                    GL_RGB, GL_UNSIGNED_BYTE, dataBuffer);
        }
        BufferUtils.destroyDirectBuffer(dataBuffer);
        gl3.glPixelStorei(GL_UNPACK_ALIGNMENT, 4);

        return checkError(gl3, "initTexture");
    }

    private byte[] buildColorChart(byte[][] color, int[] component) {

        byte[] data = new byte[3 * 8 * 8];

        for (int j = 0; j < 8; j++) {
            for (int i = 0; i < 8; i++) {

                int texelIndex = i + j * 8;
                int colorIndex = component[texelIndex];
                assert (colorIndex >= 0 && colorIndex <= 7);
                data[texelIndex * 3 + 0] = color[colorIndex][0];
                data[texelIndex * 3 + 1] = color[colorIndex][1];
                data[texelIndex * 3 + 2] = color[colorIndex][2];
            }
        }

//        byte[] result = new byte[8 * 8];
//        System.arraycopy(data, 0, result, 0, result.length);
//        return result;
        return data;
    }

    private boolean initVertexArray(GL3 gl3) {

        gl3.glGenVertexArrays(1, vertexArrayName, 0);
        gl3.glBindVertexArray(vertexArrayName[0]);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
        gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, glf.Vertex_v2fv2f.SIZE, 0);
        gl3.glVertexAttribPointer(Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, glf.Vertex_v2fv2f.SIZE, Vec2.SIZE);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);
        gl3.glEnableVertexAttribArray(Semantic.Attr.TEXCOORD);

        gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT]);
        gl3.glBindVertexArray(0);

        return checkError(gl3, "initVertexArray");
    }

    @Override
    protected boolean render(GL gl) {

        GL3 gl3 = (GL3) gl;

        gl3.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, 0);

        for (int y = 0; y < 4; ++y) {
            for (int x = 0; x < 4; ++x) {

                gl3.glViewport(x * windowSize.x / 4, y * windowSize.y / 4, windowSize.x / 4, windowSize.y / 4);
                gl3.glUniform1i(uniformLayer, x + y * 4);
                gl3.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0);
            }
        }

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;

        gl3.glDeleteProgram(programName);
        gl3.glDeleteBuffers(Buffer.MAX, bufferName, 0);
        gl3.glDeleteTextures(1, textureName, 0);
        gl3.glDeleteVertexArrays(1, vertexArrayName, 0);

        return checkError(gl3, "end");
    }
}
