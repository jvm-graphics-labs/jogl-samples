/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_420;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2ES3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import core.glm;
import dev.Mat4;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import jgli.Texture2d;
import jglm.Vec2;

/**
 *
 * @author GBarbieri
 */
public class Gl_420_image_unpack extends Test {

    public static void main(String[] args) {
        Gl_420_image_unpack gl_420_image_unpack = new Gl_420_image_unpack();
    }

    public Gl_420_image_unpack() {
        super("gl-420-image-unpack", Profile.CORE, 4, 2);
    }

    private final String SHADERS_SOURCE = "image-unpack";
    private final String SHADERS_ROOT = "src/data/gl_420";
    private final String TEXTURE_DIFFUSE = "kueken7_rgba8_unorm.dds";

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

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int ELEMENT = 1;
        public static final int TRANSFORM = 2;
        public static final int MATERIAL = 3;
        public static final int MAX = 4;
    }

    private class Program {

        public static final int VERT = 0;
        public static final int FRAG = 1;
        public static final int MAX = 2;
    }

    private int[] pipelineName = {0}, vertexArrayName = {0}, textureName = {0}, programName = new int[Program.MAX],
            bufferName = new int[Buffer.MAX];
    private Vec2 imageSize = new Vec2();

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

        try {

            if (validated) {

                String[] vertexSourceContent = new String[]{
                    new Scanner(new File(SHADERS_ROOT + "/" + SHADERS_SOURCE + ".vert")).useDelimiter("\\A").next()};
                String[] fragmentSourceContent = new String[]{
                    new Scanner(new File(SHADERS_ROOT + "/" + SHADERS_SOURCE + ".frag")).useDelimiter("\\A").next()};

                programName[Program.VERT] = gl4.glCreateShaderProgramv(GL_VERTEX_SHADER, 1, vertexSourceContent);
                programName[Program.FRAG] = gl4.glCreateShaderProgramv(GL_FRAGMENT_SHADER, 1, fragmentSourceContent);
            }

            if (validated) {

                validated = validated && framework.Compiler.checkProgram(gl4, programName[Program.VERT]);
                validated = validated && framework.Compiler.checkProgram(gl4, programName[Program.FRAG]);
            }

            if (validated) {

                gl4.glGenProgramPipelines(1, pipelineName, 0);
                gl4.glBindProgramPipeline(pipelineName[0]);
                gl4.glUseProgramStages(pipelineName[0], GL_VERTEX_SHADER_BIT, programName[Program.VERT]);
                gl4.glUseProgramStages(pipelineName[0], GL_FRAGMENT_SHADER_BIT, programName[Program.FRAG]);
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Gl_420_image_unpack.class.getName()).log(Level.SEVERE, null, ex);
        }

        return validated && checkError(gl4, "initProgram");
    }

    private boolean initTexture(GL4 gl4) {

        try {
            jgli.Texture2d texture = new Texture2d(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE));
            assert (!texture.empty());

            jgli.Gl.Format format = jgli.Gl.translate(texture.format());

            gl4.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

            gl4.glGenTextures(1, textureName, 0);

            gl4.glActiveTexture(GL_TEXTURE0);
            gl4.glBindTexture(GL_TEXTURE_2D, textureName[0]);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, texture.levels() - 1);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_R, GL_RED);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_G, GL_GREEN);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_B, GL_BLUE);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_A, GL_ALPHA);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            gl4.glTexStorage2D(GL_TEXTURE_2D, texture.levels(), format.internal.value,
                    texture.dimensions(0)[0], texture.dimensions(0)[1]);

            for (int level = 0; level < texture.levels(); ++level) {
                gl4.glTexSubImage2D(GL_TEXTURE_2D, level,
                        0, 0,
                        texture.dimensions(level)[0], texture.dimensions(level)[1],
                        format.external.value, format.type.value,
                        texture.data(level));
            }
            imageSize.x = texture.dimensions(0)[0];
            imageSize.y = texture.dimensions(0)[1];

            gl4.glPixelStorei(GL_UNPACK_ALIGNMENT, 4);

        } catch (IOException ex) {
            Logger.getLogger(Gl_420_image_unpack.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(1, vertexArrayName, 0);
        gl4.glBindVertexArray(vertexArrayName[0]);
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 2 * 2 * Float.BYTES, 0);
            gl4.glVertexAttribPointer(Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, 2 * 2 * Float.BYTES, 2 * Float.BYTES);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl4.glEnableVertexAttribArray(Semantic.Attr.TEXCOORD);
        }
        gl4.glBindVertexArray(0);

        return true;
    }

    private boolean initBuffer(GL4 gl4) {

        int[] uniformBufferOffset = {0};
        gl4.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset, 0);

        gl4.glGenBuffers(Buffer.MAX, bufferName, 0);

        {
            int uniformBlockSize = Math.max(Mat4.SIZE, uniformBufferOffset[0]);

            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
            gl4.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_DYNAMIC_DRAW);
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }

        {
            int uniformBlockSize = Math.max(2 * Float.BYTES, uniformBufferOffset[0]);

            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.MATERIAL]);
            gl4.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_DYNAMIC_DRAW);
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl4.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT]);
        IntBuffer elementBuffer = GLBuffers.newDirectIntBuffer(elementData);
        gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        {
            Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, (float) windowSize.x / windowSize.y, 0.1f, 1000.0f);
            Mat4 model = new Mat4(1.0f);
            Mat4 mvp = projection.mul(viewMat4()).mul(model);

            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
            ByteBuffer pointer = gl4.glMapBufferRange(GL_UNIFORM_BUFFER, 0, Mat4.SIZE, GL_MAP_WRITE_BIT);
            pointer.asFloatBuffer().put(mvp.toFa_());
            gl4.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        {
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.MATERIAL]);
            ByteBuffer pointer = gl4.glMapBufferRange(GL_UNIFORM_BUFFER, 0, 2 * Float.BYTES, GL_MAP_WRITE_BIT);
            // since unsigned floats
            pointer.putFloat(Float.intBitsToFloat((int) imageSize.x));
            pointer.putFloat(Float.intBitsToFloat((int) imageSize.y));
            pointer.rewind();
            gl4.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        gl4.glViewportIndexedf(0, 0, 0, windowSize.x, windowSize.y);
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 0.5f, 0.0f, 1.0f}, 0);

        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.TRANSFORM]);
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.MATERIAL, bufferName[Buffer.MATERIAL]);
        gl4.glBindProgramPipeline(pipelineName[0]);
        gl4.glBindImageTexture(Semantic.Image.DIFFUSE, textureName[0], 0, false, 0, GL_READ_ONLY, GL_R32UI);
        gl4.glBindVertexArray(vertexArrayName[0]);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT]);

        gl4.glDrawElementsInstancedBaseVertexBaseInstance(GL_TRIANGLES, elementCount, GL_UNSIGNED_INT, 0, 1, 0, 0);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteBuffers(Buffer.MAX, bufferName, 0);
        gl4.glDeleteTextures(1, textureName, 0);
        gl4.glDeleteVertexArrays(1, vertexArrayName, 0);
        gl4.glDeleteProgram(programName[Program.VERT]);
        gl4.glDeleteProgram(programName[Program.FRAG]);
        gl4.glDeleteProgramPipelines(1, pipelineName, 0);

        return true;
    }
}
