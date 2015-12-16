/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl320.fbo;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_ALWAYS;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_CLAMP_TO_EDGE;
import static com.jogamp.opengl.GL.GL_COLOR_ATTACHMENT0;
import static com.jogamp.opengl.GL.GL_COMPRESSED_RGB_S3TC_DXT1_EXT;
import static com.jogamp.opengl.GL.GL_DEPTH24_STENCIL8;
import static com.jogamp.opengl.GL.GL_DEPTH_STENCIL;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_DYNAMIC_DRAW;
import static com.jogamp.opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_FRAMEBUFFER;
import static com.jogamp.opengl.GL.GL_KEEP;
import static com.jogamp.opengl.GL.GL_LEQUAL;
import static com.jogamp.opengl.GL.GL_LINEAR;
import static com.jogamp.opengl.GL.GL_LINEAR_MIPMAP_LINEAR;
import static com.jogamp.opengl.GL.GL_MAP_INVALIDATE_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_MAP_WRITE_BIT;
import static com.jogamp.opengl.GL.GL_NOTEQUAL;
import static com.jogamp.opengl.GL.GL_REPLACE;
import static com.jogamp.opengl.GL.GL_RGBA;
import static com.jogamp.opengl.GL.GL_RGBA8;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_STENCIL_TEST;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_S;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_T;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNPACK_ALIGNMENT;
import static com.jogamp.opengl.GL.GL_UNSIGNED_BYTE;
import static com.jogamp.opengl.GL.GL_UNSIGNED_SHORT;
import static com.jogamp.opengl.GL2ES2.GL_DEPTH_COMPONENT;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import static com.jogamp.opengl.GL2ES3.GL_DEPTH_STENCIL_ATTACHMENT;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_BASE_LEVEL;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_MAX_LEVEL;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.Glm;
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

/**
 *
 * @author GBarbieri
 */
public class Gl_320_fbo_depth_stencil extends Test {

    public static void main(String[] args) {
        Gl_320_fbo_depth_stencil gl_320_fbo_depth_stencil = new Gl_320_fbo_depth_stencil();
    }

    public Gl_320_fbo_depth_stencil() {
        super("Gl-320-fbo-depth-stencil", Profile.CORE, 3, 2);
    }

    private final String SHADERS_SOURCE_TEXTURE = "texture-2d";
    private final String SHADERS_SOURCE_SPLASH = "fbo";
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
        2, 3, 0
    };

    private enum Buffer {

        VERTEX,
        ELEMENT,
        TRANSFORM,
        MAX
    }

    private enum Texture {

        DIFFUSE,
        COLORBUFFER,
        RENDERBUFFER,
        MAX
    }

    private enum Program {

        TEXTURE,
        SPLASH,
        MAX
    }

    private enum Shader {

        VERT_TEXTURE,
        FRAG_TEXTURE,
        VERT_SPLASH,
        FRAG_SPLASH,
        MAX
    }

    private int[] programName = new int[Program.MAX.ordinal()], vertexArrayName = new int[Program.MAX.ordinal()],
            bufferName = new int[Buffer.MAX.ordinal()], textureName = new int[Texture.MAX.ordinal()],
            uniformDiffuse = new int[Program.MAX.ordinal()], framebufferName = new int[1];
    private int framebufferScale = 2, uniformTransform;
    private float[] projection = new float[16], model = new float[16], mvp = new float[16];

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

        return validated;
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
            uniformDiffuse[Program.TEXTURE.ordinal()]
                    = gl3.glGetUniformLocation(programName[Program.TEXTURE.ordinal()], "diffuse");
            uniformDiffuse[Program.SPLASH.ordinal()]
                    = gl3.glGetUniformLocation(programName[Program.SPLASH.ordinal()], "diffuse");

            gl3.glUseProgram(programName[Program.TEXTURE.ordinal()]);
            gl3.glUniform1i(uniformDiffuse[Program.TEXTURE.ordinal()], 0);
            gl3.glUniformBlockBinding(programName[Program.TEXTURE.ordinal()], uniformTransform, Semantic.Uniform.TRANSFORM0);

            gl3.glUseProgram(programName[Program.SPLASH.ordinal()]);
            gl3.glUniform1i(uniformDiffuse[Program.SPLASH.ordinal()], 0);
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

        int[] uniformBufferOffsetAlignment = new int[1];
        gl3.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffsetAlignment, 0);
        uniformBufferOffsetAlignment[0] = Glm.ceilMultiple(16 * Float.BYTES, uniformBufferOffsetAlignment[0]);

        gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM.ordinal()]);
        gl3.glBufferData(GL_UNIFORM_BUFFER, uniformBufferOffsetAlignment[0] * 2, null, GL_DYNAMIC_DRAW);
        gl3.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return true;
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
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

            for (int level = 0; level < texture.levels(); ++level) {

                gl3.glCompressedTexImage2D(GL_TEXTURE_2D, level,
                        GL_COMPRESSED_RGB_S3TC_DXT1_EXT,
                        texture.dimensions(level)[0],
                        texture.dimensions(level)[1],
                        0,
                        texture.size(level),
                        texture.data(0, 0, level));
            }

            gl3.glActiveTexture(GL_TEXTURE0);
            gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.COLORBUFFER.ordinal()]);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            gl3.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, windowSize.x * framebufferScale,
                    windowSize.y * framebufferScale, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);

            gl3.glActiveTexture(GL_TEXTURE0);
            gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.RENDERBUFFER.ordinal()]);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
            gl3.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH24_STENCIL8, windowSize.x * framebufferScale,
                    windowSize.y * framebufferScale, 0, GL_DEPTH_COMPONENT, GL_FLOAT, null);

            gl3.glPixelStorei(GL_UNPACK_ALIGNMENT, 4);

        } catch (IOException ex) {
            Logger.getLogger(Gl_320_fbo_depth_stencil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return validated;
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

        return true;
    }

    private boolean initFramebuffer(GL3 gl3) {

        gl3.glGenFramebuffers(1, framebufferName, 0);
        gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[0]);
        gl3.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, textureName[Texture.COLORBUFFER.ordinal()], 0);
        gl3.glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT,
                textureName[Texture.RENDERBUFFER.ordinal()], 0);

        if (!isFramebufferComplete(gl3, framebufferName[0])) {
            return false;
        }

        gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL3 gl3 = (GL3) gl;

        int[] uniformBufferOffsetAlignment = new int[1];
        {
            gl3.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffsetAlignment, 0);
            uniformBufferOffsetAlignment[0] = Glm.ceilMultiple(16 * Float.BYTES, uniformBufferOffsetAlignment[0]);

            gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM.ordinal()]);
            ByteBuffer pointer = gl3.glMapBufferRange(GL_UNIFORM_BUFFER, 0, uniformBufferOffsetAlignment[0] * 2,
                    GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            FloatUtil.makePerspective(projection, 0, true, (float) Math.PI * 0.25f,
                    (float) windowSize.x / windowSize.y, 0.1f, 100.0f);
            FloatUtil.makeScale(model, true, 1.00f, 1.00f, 1.00f);

            FloatUtil.multMatrix(projection, view(), mvp);
            FloatUtil.multMatrix(mvp, model);

            for (int i = 0; i < mvp.length; i++) {
                pointer.putFloat(uniformBufferOffsetAlignment[0] * 0 + i * Float.BYTES, mvp[i]);
            }

            FloatUtil.makeScale(model, true, 1.05f, 1.05f, 1.05f);

            FloatUtil.multMatrix(projection, view(), mvp);
            FloatUtil.multMatrix(mvp, model);

            for (int i = 0; i < mvp.length; i++) {
                pointer.putFloat(uniformBufferOffsetAlignment[0] * 1 + i * Float.BYTES, mvp[i]);
            }
            pointer.rewind();

            // Make sure the uniform buffer is uploaded
            gl3.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        {
            gl3.glEnable(GL_STENCIL_TEST);
            gl3.glEnable(GL_DEPTH_TEST);
            gl3.glDepthFunc(GL_LEQUAL);
            gl3.glStencilMask(0xFF);

            gl3.glViewport(0, 0, windowSize.x * framebufferScale, windowSize.y * framebufferScale);

            gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[0]);
            gl3.glClearBufferfi(GL_DEPTH_STENCIL, 0, 1.0f, 0);
            gl3.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 0.5f, 0.0f, 1.0f}, 0);

            gl3.glUseProgram(programName[Program.TEXTURE.ordinal()]);

            gl3.glActiveTexture(GL_TEXTURE0);
            gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.DIFFUSE.ordinal()]);
            gl3.glBindVertexArray(vertexArrayName[Program.TEXTURE.ordinal()]);

            gl3.glBindBufferRange(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0,
                    bufferName[Buffer.TRANSFORM.ordinal()], uniformBufferOffsetAlignment[0] * 0,
                    uniformBufferOffsetAlignment[0]);

            gl3.glDisable(GL_DEPTH_TEST);
            gl3.glStencilMask(0xFF);
            gl3.glStencilFunc(GL_ALWAYS, 1, 0xFF);
            gl3.glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);

            gl3.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0);

            gl3.glBindTexture(GL_TEXTURE_2D, 0); // 
            gl3.glBindBufferRange(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.TRANSFORM.ordinal()],
                    uniformBufferOffsetAlignment[0] * 1, uniformBufferOffsetAlignment[0]);

            gl3.glStencilMask(0x00);
            gl3.glStencilFunc(GL_NOTEQUAL, 1, 0xFF);

            gl3.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0);
        }

        {
            gl3.glDisable(GL_DEPTH_TEST);

            gl3.glViewport(0, 0, windowSize.x, windowSize.y);

            gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);

            gl3.glUseProgram(programName[Program.SPLASH.ordinal()]);

            gl3.glActiveTexture(GL_TEXTURE0);
            gl3.glBindVertexArray(vertexArrayName[Program.SPLASH.ordinal()]);
            gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.COLORBUFFER.ordinal()]);

            gl3.glDrawArraysInstanced(GL_TRIANGLES, 0, 3, 1);
        }

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;

        gl3.glDeleteFramebuffers(1, framebufferName, 0);
        gl3.glDeleteProgram(programName[Program.SPLASH.ordinal()]);
        gl3.glDeleteProgram(programName[Program.TEXTURE.ordinal()]);

        gl3.glDeleteBuffers(Buffer.MAX.ordinal(), bufferName, 0);
        gl3.glDeleteTextures(Texture.MAX.ordinal(), textureName, 0);
        gl3.glDeleteVertexArrays(Program.MAX.ordinal(), vertexArrayName, 0);

        return true;
    }
}
