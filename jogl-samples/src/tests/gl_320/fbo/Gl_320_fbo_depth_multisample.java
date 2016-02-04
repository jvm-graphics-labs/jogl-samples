/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_320.fbo;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2ES3.*;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import core.glm;
import dev.Mat4;
import dev.Vec2;
import dev.Vec3;
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

/**
 *
 * @author GBarbieri
 */
public class Gl_320_fbo_depth_multisample extends Test {

    public static void main(String[] args) {
        Gl_320_fbo_depth_multisample gl_320_fbo_depth_multisample = new Gl_320_fbo_depth_multisample();
    }

    public Gl_320_fbo_depth_multisample() {
        super("Gl-320-fbo-depth-multisample", Profile.CORE, 3, 2, new Vec2(0.0f, -Math.PI * 0.48f));
    }

    private final String SHADERS_SOURCE_TEXTURE = "texture-2d";
    private final String SHADERS_SOURCE_SPLASH = "fbo-depth-multisample";
    private final String SHADERS_ROOT_TEXTURE = "src/data/gl_320/texture";
    private final String SHADERS_ROOT_SPLASH = "src/data/gl_320/fbo";
    private final String TEXTURE_DIFFUSE = "kueken7_rgb_dxt1_unorm.dds";

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

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int ELEMENT = 1;
        public static final int TRANSFORM = 2;
        public static final int MAX = 3;
    }

    private class Texture {

        public static final int DIFFUSE = 0;
        public static final int MULTISAMPLE = 1;
        public static final int MAX = 2;
    }

    private class Program {

        public static final int TEXTURE = 0;
        public static final int SPLASH = 1;
        public static final int MAX = 2;
    }

    private class Framebuffer {

        public static final int DEPTH_MULTISAMPLE = 0;
        public static final int MAX = 1;
    }

    private class Shader {

        public static final int VERT_TEXTURE = 0;
        public static final int FRAG_TEXTURE = 1;
        public static final int VERT_SPLASH = 2;
        public static final int FRAG_SPLASH = 3;
        public static final int MAX = 4;
    }

    private int[] framebufferName = new int[Framebuffer.MAX], programName = new int[Program.MAX],
            vertexArrayName = new int[Program.MAX], bufferName = new int[Buffer.MAX], textureName = new int[Texture.MAX];
    private int uniformTransform;

    @Override
    protected boolean begin(GL gl) {

        GL3 gl3 = (GL3) gl;

        boolean validated = true;

        if (validated) {
            validated = initProgram(gl3);
        }
        if (validated) {
            validated = initBuffer(gl3);
        }
        if (validated) {
            validated = initVertexArray(gl3);
        }
        if (validated) {
            validated = initTexture(gl3);
        }
        if (validated) {
            validated = initFramebuffer(gl3);
        }

        return validated && checkError(gl3, "begin");
    }

    private boolean initProgram(GL3 gl3) {

        boolean validated = true;

        ShaderCode[] shaderCode = new ShaderCode[Shader.MAX];

        // Create program
        if (validated) {

            shaderCode[Shader.VERT_TEXTURE] = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT_TEXTURE, null, SHADERS_SOURCE_TEXTURE, "vert", null, true);
            shaderCode[Shader.FRAG_TEXTURE] = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT_TEXTURE, null, SHADERS_SOURCE_TEXTURE, "frag", null, true);

            ShaderProgram program = new ShaderProgram();
            program.add(shaderCode[Shader.VERT_TEXTURE]);
            program.add(shaderCode[Shader.FRAG_TEXTURE]);
            program.init(gl3);

            programName[Program.TEXTURE] = program.program();

            gl3.glBindAttribLocation(programName[Program.TEXTURE], Semantic.Attr.POSITION, "position");
            gl3.glBindAttribLocation(programName[Program.TEXTURE], Semantic.Attr.TEXCOORD, "texCoord");
            gl3.glBindFragDataLocation(programName[Program.TEXTURE], Semantic.Frag.COLOR, "color");

            program.link(gl3, System.out);
        }
        if (validated) {

            shaderCode[Shader.VERT_SPLASH] = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT_SPLASH, null, SHADERS_SOURCE_SPLASH, "vert", null, true);
            shaderCode[Shader.FRAG_SPLASH] = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT_SPLASH, null, SHADERS_SOURCE_SPLASH, "frag", null, true);

            ShaderProgram program = new ShaderProgram();
            program.add(shaderCode[Shader.VERT_SPLASH]);
            program.add(shaderCode[Shader.FRAG_SPLASH]);
            program.init(gl3);

            programName[Program.SPLASH] = program.program();

            gl3.glBindFragDataLocation(programName[Program.SPLASH], Semantic.Frag.COLOR, "color");

            program.link(gl3, System.out);
        }
        if (validated) {

            uniformTransform = gl3.glGetUniformBlockIndex(programName[Program.TEXTURE], "Transform");
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
        int uniformBlockSize = Math.max(16 * Float.BYTES, uniformBufferOffset[0]);

        gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
        gl3.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_DYNAMIC_DRAW);
        gl3.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return checkError(gl3, "initBuffer");
    }

    private boolean initTexture(GL3 gl3) {

        boolean validated = true;

        try {
            jgli.Texture2d texture = new Texture2d(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE));

            if (texture.empty()) {
                throw new Error("texture empty");
            }

            gl3.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

            gl3.glGenTextures(Texture.MAX, textureName, 0);

            gl3.glActiveTexture(GL_TEXTURE0);
            gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.DIFFUSE]);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, texture.levels() - 1);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_NEAREST);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

            for (int level = 0; level < texture.levels(); ++level) {
                gl3.glCompressedTexImage2D(GL_TEXTURE_2D,
                        level,
                        GL_COMPRESSED_RGB_S3TC_DXT1_EXT,
                        texture.dimensions(level)[0],
                        texture.dimensions(level)[1],
                        0,
                        texture.size(level),
                        texture.data(level));
            }

            gl3.glActiveTexture(GL_TEXTURE0);
            gl3.glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, textureName[Texture.MULTISAMPLE]);

            checkError(gl3, "initTexture 1");

            gl3.glTexParameteri(GL_TEXTURE_2D_MULTISAMPLE, GL_TEXTURE_BASE_LEVEL, 0);
            gl3.glTexParameteri(GL_TEXTURE_2D_MULTISAMPLE, GL_TEXTURE_MAX_LEVEL, 0);

            checkError(gl3, "initTexture 2");

            gl3.glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, 4,
                    GL_DEPTH_COMPONENT24, windowSize.x, windowSize.y, true);

            gl3.glPixelStorei(GL_UNPACK_ALIGNMENT, 4);

        } catch (IOException ex) {
            Logger.getLogger(Gl_320_fbo_depth_multisample.class.getName()).log(Level.SEVERE, null, ex);
        }
        return validated && checkError(gl3, "initTexture");
    }

    private boolean initVertexArray(GL3 gl3) {

        gl3.glGenVertexArrays(Program.MAX, vertexArrayName, 0);
        gl3.glBindVertexArray(vertexArrayName[Program.TEXTURE]);
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 2 * 2 * Float.BYTES, 0);
            gl3.glVertexAttribPointer(Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, 2 * 2 * Float.BYTES, 2 * Float.BYTES);
            gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl3.glEnableVertexAttribArray(Semantic.Attr.TEXCOORD);

            gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT]);
        }
        gl3.glBindVertexArray(0);

        gl3.glBindVertexArray(vertexArrayName[Program.SPLASH]);
        gl3.glBindVertexArray(0);

        return checkError(gl3, "initVertexArray");
    }

    private boolean initFramebuffer(GL3 gl3) {

        boolean validated = true;

        gl3.glGenFramebuffers(Framebuffer.MAX, framebufferName, 0);
        gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[Framebuffer.DEPTH_MULTISAMPLE]);
        gl3.glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, textureName[Texture.MULTISAMPLE], 0);
        gl3.glDrawBuffer(GL_NONE);

        if (!isFramebufferComplete(gl3, framebufferName[Framebuffer.DEPTH_MULTISAMPLE])) {
            return false;
        }

        gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        return validated && checkError(gl3, "initFramebuffer");
    }

    @Override
    protected boolean render(GL gl) {

        GL3 gl3 = (GL3) gl;

        {
            gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
            ByteBuffer pointer = gl3.glMapBufferRange(
                    GL_UNIFORM_BUFFER, 0, 16 * Float.BYTES,
                    GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            //glm::mat4 Projection = glm::perspectiveFov(glm::pi<float>() * 0.25f, 640.f, 480.f, 0.1f, 100.0f);
            Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, 4.0f / 3.0f, 0.1f, 8.0f);
            Mat4 model = new Mat4(1.0f).scale(new Vec3(5.0f));

            pointer.asFloatBuffer().put(projection.mul(viewMat4()).mul(model).toFa_());

            // Make sure the uniform buffer is uploaded
            gl3.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        gl3.glEnable(GL_DEPTH_TEST);
        gl3.glDepthFunc(GL_LESS);

        gl3.glViewport(0, 0, windowSize.x, windowSize.y);

        gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[Framebuffer.DEPTH_MULTISAMPLE]);
        float[] depth = {1.0f};
        gl3.glClearBufferfv(GL_DEPTH, 0, depth, 0);

        // Bind rendering objects
        gl3.glUseProgram(programName[Program.TEXTURE]);
        gl3.glUniformBlockBinding(programName[Program.TEXTURE],
                uniformTransform, Semantic.Uniform.TRANSFORM0);

        gl3.glActiveTexture(GL_TEXTURE0);
        gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.DIFFUSE]);
        gl3.glBindVertexArray(vertexArrayName[Program.TEXTURE]);
        gl3.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.TRANSFORM]);

        gl3.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 2, 0);

        // Pass 2
        gl3.glDisable(GL_DEPTH_TEST);

        gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        gl3.glUseProgram(programName[Program.SPLASH]);

        gl3.glActiveTexture(GL_TEXTURE0);
        gl3.glBindVertexArray(vertexArrayName[Program.SPLASH]);
        gl3.glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, textureName[Texture.MULTISAMPLE]);

        gl3.glDrawArraysInstanced(GL_TRIANGLES, 0, 3, 1);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;

        gl3.glDeleteFramebuffers(framebufferName.length, framebufferName, 0);
        gl3.glDeleteProgram(programName[Program.SPLASH]);
        gl3.glDeleteProgram(programName[Program.TEXTURE]);
        gl3.glDeleteBuffers(Buffer.MAX, bufferName, 0);
        gl3.glDeleteTextures(Texture.MAX, textureName, 0);
        gl3.glDeleteVertexArrays(Program.MAX, vertexArrayName, 0);

        return checkError(gl3, "end");
    }
}
