/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl320.fbo;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_CLAMP_TO_EDGE;
import static com.jogamp.opengl.GL.GL_COMPRESSED_RGB_S3TC_DXT1_EXT;
import static com.jogamp.opengl.GL.GL_DEPTH_ATTACHMENT;
import static com.jogamp.opengl.GL.GL_DEPTH_COMPONENT24;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_DYNAMIC_DRAW;
import static com.jogamp.opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_FRAMEBUFFER;
import static com.jogamp.opengl.GL.GL_LESS;
import static com.jogamp.opengl.GL.GL_MAP_INVALIDATE_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_MAP_WRITE_BIT;
import static com.jogamp.opengl.GL.GL_NEAREST;
import static com.jogamp.opengl.GL.GL_NEAREST_MIPMAP_NEAREST;
import static com.jogamp.opengl.GL.GL_NONE;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_S;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_T;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNPACK_ALIGNMENT;
import static com.jogamp.opengl.GL.GL_UNSIGNED_SHORT;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_TEXTURE_2D_MULTISAMPLE;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES3.GL_DEPTH;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_BASE_LEVEL;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_MAX_LEVEL;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT;
import com.jogamp.opengl.GL3;
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
import java.nio.ShortBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import jgli.Texture2D;
import jglm.Vec2;

/**
 *
 * @author GBarbieri
 */
public class Gl_320_fbo_depth_multisample extends Test {

    public static void main(String[] args) {
        Gl_320_fbo_depth_multisample gl_320_fbo_depth_multisample = new Gl_320_fbo_depth_multisample();
    }

    public Gl_320_fbo_depth_multisample() {
        super("Gl-320-fbo-depth-multisample", Profile.CORE, 3, 2, new Vec2(0.0f, -(float) Math.PI * 0.48f));
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

    private enum Buffer {

        VERTEX,
        ELEMENT,
        TRANSFORM,
        MAX
    }

    private enum Texture {

        DIFFUSE,
        MULTISAMPLE,
        MAX
    }

    private enum Program {

        TEXTURE,
        SPLASH,
        MAX
    }

    private enum Framebuffer {

        DEPTH_MULTISAMPLE,
        MAX
    }

    private enum Shader {

        VERT_TEXTURE,
        FRAG_TEXTURE,
        VERT_SPLASH,
        FRAG_SPLASH,
        MAX
    }

    private int[] framebufferName = new int[Framebuffer.MAX.ordinal()], programName = new int[Program.MAX.ordinal()],
            vertexArrayName = new int[Program.MAX.ordinal()], bufferName = new int[Buffer.MAX.ordinal()],
            textureName = new int[Texture.MAX.ordinal()];
    private int uniformTransform;
    private float[] projection = new float[16], model = new float[16];

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

        ShaderCode[] shaderCode = new ShaderCode[Shader.MAX.ordinal()];

        // Create program
        if (validated) {

            shaderCode[Shader.VERT_TEXTURE.ordinal()] = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT_TEXTURE, null, SHADERS_SOURCE_TEXTURE, "vert", null, true);
            shaderCode[Shader.FRAG_TEXTURE.ordinal()] = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT_TEXTURE, null, SHADERS_SOURCE_TEXTURE, "frag", null, true);

            ShaderProgram program = new ShaderProgram();
            program.add(shaderCode[Shader.VERT_TEXTURE.ordinal()]);
            program.add(shaderCode[Shader.FRAG_TEXTURE.ordinal()]);
            program.init(gl3);

            programName[Program.TEXTURE.ordinal()] = program.program();

            gl3.glBindAttribLocation(programName[Program.TEXTURE.ordinal()], Semantic.Attr.POSITION, "position");
            gl3.glBindAttribLocation(programName[Program.TEXTURE.ordinal()], Semantic.Attr.TEXCOORD, "texCoord");
            gl3.glBindFragDataLocation(programName[Program.TEXTURE.ordinal()], Semantic.Frag.COLOR, "color");

            program.link(gl3, System.out);
        }
        if (validated) {

            shaderCode[Shader.VERT_SPLASH.ordinal()] = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT_SPLASH, null, SHADERS_SOURCE_SPLASH, "vert", null, true);
            shaderCode[Shader.FRAG_SPLASH.ordinal()] = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT_SPLASH, null, SHADERS_SOURCE_SPLASH, "frag", null, true);

            ShaderProgram program = new ShaderProgram();
            program.add(shaderCode[Shader.VERT_SPLASH.ordinal()]);
            program.add(shaderCode[Shader.FRAG_SPLASH.ordinal()]);
            program.init(gl3);

            programName[Program.SPLASH.ordinal()] = program.program();

            gl3.glBindFragDataLocation(programName[Program.SPLASH.ordinal()], Semantic.Frag.COLOR, "color");

            program.link(gl3, System.out);
        }
        if (validated) {

            uniformTransform = gl3.glGetUniformBlockIndex(programName[Program.TEXTURE.ordinal()], "Transform");
        }
        return validated & checkError(gl3, "initProgram");
    }

    private boolean initBuffer(GL3 gl3) {

        gl3.glGenBuffers(Buffer.MAX.ordinal(), bufferName, 0);

        gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT.ordinal()]);
        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        gl3.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl3.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        int[] uniformBufferOffset = new int[1];
        gl3.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset, 0);
        int uniformBlockSize = Math.max(16 * Float.BYTES, uniformBufferOffset[0]);

        gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM.ordinal()]);
        gl3.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_DYNAMIC_DRAW);
        gl3.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return checkError(gl3, "initBuffer");
    }

    private boolean initTexture(GL3 gl3) {

        boolean validated = true;

        try {
            jgli.Texture2D texture = new Texture2D(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE));

            if (texture.empty()) {
                throw new Error("texture empty");
            }

            gl3.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

            gl3.glGenTextures(Texture.MAX.ordinal(), textureName, 0);

            gl3.glActiveTexture(GL_TEXTURE0);
            gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.DIFFUSE.ordinal()]);
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
                        texture.data(0, 0, level));
            }

            gl3.glActiveTexture(GL_TEXTURE0);
            gl3.glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, textureName[Texture.MULTISAMPLE.ordinal()]);

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

        gl3.glGenVertexArrays(Program.MAX.ordinal(), vertexArrayName, 0);
        gl3.glBindVertexArray(vertexArrayName[Program.TEXTURE.ordinal()]);
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 2 * 2 * Float.BYTES, 0);
            gl3.glVertexAttribPointer(Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, 2 * 2 * Float.BYTES, 2 * Float.BYTES);
            gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl3.glEnableVertexAttribArray(Semantic.Attr.TEXCOORD);

            gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT.ordinal()]);
        }
        gl3.glBindVertexArray(0);

        gl3.glBindVertexArray(vertexArrayName[Program.SPLASH.ordinal()]);
        gl3.glBindVertexArray(0);

        return checkError(gl3, "initVertexArray");
    }

    private boolean initFramebuffer(GL3 gl3) {

        boolean validated = true;

        gl3.glGenFramebuffers(Framebuffer.MAX.ordinal(), framebufferName, 0);
        gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[Framebuffer.DEPTH_MULTISAMPLE.ordinal()]);
        gl3.glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, textureName[Texture.MULTISAMPLE.ordinal()], 0);
        gl3.glDrawBuffer(GL_NONE);

        if (!isFramebufferComplete(gl3, framebufferName[Framebuffer.DEPTH_MULTISAMPLE.ordinal()])) {
            return false;
        }

        gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        return validated && checkError(gl3, "initFramebuffer");
    }

    @Override
    protected boolean render(GL gl) {

        GL3 gl3 = (GL3) gl;

        {
            gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM.ordinal()]);
            ByteBuffer pointer = gl3.glMapBufferRange(
                    GL_UNIFORM_BUFFER, 0, 16 * Float.BYTES,
                    GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            //glm::mat4 Projection = glm::perspectiveFov(glm::pi<float>() * 0.25f, 640.f, 480.f, 0.1f, 100.0f);
            FloatUtil.makePerspective(projection, 0, true, (float) Math.PI * 0.25f, 4.0f / 3.0f, 0.1f, 8.0f);
            FloatUtil.makeScale(model, true, 5.0f, 5.0f, 5.0f);

            FloatUtil.multMatrix(projection, view());
            FloatUtil.multMatrix(projection, model);

            for (int f = 0; f < projection.length; f++) {
                pointer.putFloat(projection[f]);
            }

            // Make sure the uniform buffer is uploaded
            gl3.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        gl3.glEnable(GL_DEPTH_TEST);
        gl3.glDepthFunc(GL_LESS);

        gl3.glViewport(0, 0, windowSize.x, windowSize.y);

        gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[Framebuffer.DEPTH_MULTISAMPLE.ordinal()]);
        float[] depth = {1.0f};
        gl3.glClearBufferfv(GL_DEPTH, 0, depth, 0);

        // Bind rendering objects
        gl3.glUseProgram(programName[Program.TEXTURE.ordinal()]);
        gl3.glUniformBlockBinding(programName[Program.TEXTURE.ordinal()],
                uniformTransform, Semantic.Uniform.TRANSFORM0);

        gl3.glActiveTexture(GL_TEXTURE0);
        gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.DIFFUSE.ordinal()]);
        gl3.glBindVertexArray(vertexArrayName[Program.TEXTURE.ordinal()]);
        gl3.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.TRANSFORM.ordinal()]);

        gl3.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 2, 0);

        // Pass 2
        gl3.glDisable(GL_DEPTH_TEST);

        gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        gl3.glUseProgram(programName[Program.SPLASH.ordinal()]);

        gl3.glActiveTexture(GL_TEXTURE0);
        gl3.glBindVertexArray(vertexArrayName[Program.SPLASH.ordinal()]);
        gl3.glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, textureName[Texture.MULTISAMPLE.ordinal()]);

        gl3.glDrawArraysInstanced(GL_TRIANGLES, 0, 3, 1);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;

        gl3.glDeleteFramebuffers(framebufferName.length, framebufferName, 0);
        gl3.glDeleteProgram(programName[Program.SPLASH.ordinal()]);
        gl3.glDeleteProgram(programName[Program.TEXTURE.ordinal()]);
        gl3.glDeleteBuffers(Buffer.MAX.ordinal(), bufferName, 0);
        gl3.glDeleteTextures(Texture.MAX.ordinal(), textureName, 0);
        gl3.glDeleteVertexArrays(Program.MAX.ordinal(), vertexArrayName, 0);

        return checkError(gl3, "end");
    }
}
