/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_440;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2ES3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import core.glm;
import dev.Mat4;
import dev.Vec2;
import dev.Vec2i;
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
 * @author elect
 */
public class Gl_440_fbo_depth_stencil extends Test {

    public static void main(String[] args) {
        Gl_440_fbo_depth_stencil gl_440_fbo_depth_stencil = new Gl_440_fbo_depth_stencil();
    }

    public Gl_440_fbo_depth_stencil() {
        super("gl-440-fbo-depth-stencil", Profile.CORE, 4, 4);
    }

    private final String SHADERS_SOURCE_TEXTURE = "fbo-depth-stencil-render";
    private final String SHADERS_SOURCE_SPLASH = "fbo-depth-stencil-blit";
    private final String SHADERS_ROOT = "src/data/gl_440";
    private final String TEXTURE_DIFFUSE = "kueken7_rgb_dxt1_unorm.dds";

    private int vertexCount = 4;
    private int vertexSize = vertexCount * 2 * Vec2.SIZEOF;
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

    private enum Buffer {
        VERTEX,
        ELEMENT,
        TRANSFORM,
        MAX
    }

    private enum Texture {
        DIFFUSE,
        COLORBUFFER,
        DEPTHBUFFER,
        STENCILBUFFER,
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

    private int[] programName = new int[Program.MAX.ordinal()],
            vertexArrayName = new int[Program.MAX.ordinal()], bufferName = new int[Buffer.MAX.ordinal()],
            textureName = new int[Texture.MAX.ordinal()], uniformDiffuse = new int[Program.MAX.ordinal()],
            framebufferName = {0};
    private int framebufferScale = 2, uniformTransform;

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;
        validated = validated && checkExtension(gl4, "GL_ARB_texture_stencil8");

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
        if (validated) {
            validated = initFramebuffer(gl4);
        }

        return validated;

    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        ShaderCode[] shaderCodes = new ShaderCode[Shader.MAX.ordinal()];

        // Create program
        if (validated) {

            shaderCodes[Shader.VERT_TEXTURE.ordinal()] = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_TEXTURE, "vert", null, true);
            shaderCodes[Shader.FRAG_TEXTURE.ordinal()] = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_TEXTURE, "frag", null, true);

            ShaderProgram shaderProgram = new ShaderProgram();
            shaderProgram.init(gl4);

            programName[Program.TEXTURE.ordinal()] = shaderProgram.program();

            shaderProgram.add(shaderCodes[Shader.VERT_TEXTURE.ordinal()]);
            shaderProgram.add(shaderCodes[Shader.FRAG_TEXTURE.ordinal()]);

            gl4.glBindAttribLocation(programName[Program.TEXTURE.ordinal()], Semantic.Attr.POSITION, "position");
            gl4.glBindAttribLocation(programName[Program.TEXTURE.ordinal()], Semantic.Attr.TEXCOORD, "texCoord");
            gl4.glBindFragDataLocation(programName[Program.TEXTURE.ordinal()], Semantic.Frag.COLOR, "color");

            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            shaderCodes[Shader.VERT_SPLASH.ordinal()] = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_SPLASH, "vert", null, true);
            shaderCodes[Shader.FRAG_SPLASH.ordinal()] = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_SPLASH, "frag", null, true);

            ShaderProgram shaderProgram = new ShaderProgram();
            shaderProgram.init(gl4);

            programName[Program.SPLASH.ordinal()] = shaderProgram.program();

            shaderProgram.add(shaderCodes[Shader.VERT_SPLASH.ordinal()]);
            shaderProgram.add(shaderCodes[Shader.FRAG_SPLASH.ordinal()]);

            gl4.glBindFragDataLocation(programName[Program.SPLASH.ordinal()], Semantic.Frag.COLOR, "color");

            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            uniformTransform = gl4.glGetUniformBlockIndex(programName[Program.TEXTURE.ordinal()], "Transform");
            uniformDiffuse[Program.TEXTURE.ordinal()]
                    = gl4.glGetUniformLocation(programName[Program.TEXTURE.ordinal()], "diffuse");
            uniformDiffuse[Program.SPLASH.ordinal()]
                    = gl4.glGetUniformLocation(programName[Program.SPLASH.ordinal()], "diffuse");

            gl4.glUseProgram(programName[Program.TEXTURE.ordinal()]);
            gl4.glUniform1i(uniformDiffuse[Program.TEXTURE.ordinal()], 0);
            gl4.glUniformBlockBinding(programName[Program.TEXTURE.ordinal()], uniformTransform, Semantic.Uniform.TRANSFORM0);

            gl4.glUseProgram(programName[Program.SPLASH.ordinal()]);
            gl4.glUniform1i(uniformDiffuse[Program.SPLASH.ordinal()], 0);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        gl4.glGenBuffers(Buffer.MAX.ordinal(), bufferName, 0);

        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT.ordinal()]);
        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(elementBuffer);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl4.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        int[] uniformBufferOffset = {0};
        gl4.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset, 0);
        int uniformBlockSize = Math.max(Mat4.SIZEOF, uniformBufferOffset[0]);

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM.ordinal()]);
        gl4.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_DYNAMIC_DRAW);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return true;
    }

    private boolean initTexture(GL4 gl4) {

        boolean validated = true;

        try {

            jgli.Texture2d texture = new Texture2d(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE));
            assert (!texture.empty());

            gl4.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

            gl4.glGenTextures(Texture.MAX.ordinal(), textureName, 0);

            gl4.glActiveTexture(GL_TEXTURE0);
            gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.DIFFUSE.ordinal()]);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, texture.levels() - 1);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

            for (int level = 0; level < texture.levels(); ++level) {

                gl4.glCompressedTexImage2D(GL_TEXTURE_2D,
                        level,
                        GL_COMPRESSED_RGB_S3TC_DXT1_EXT,
                        texture.dimensions(level)[0],
                        texture.dimensions(level)[1],
                        0,
                        texture.size(level),
                        texture.data(level));
            }

            Vec2i size = new Vec2i(windowSize.x * framebufferScale, windowSize.y * framebufferScale);

            gl4.glActiveTexture(GL_TEXTURE0);
            gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.COLORBUFFER.ordinal()]);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            gl4.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, size.x, size.y, 0, GL_RGBA,
                    GL_UNSIGNED_BYTE, null);

            gl4.glActiveTexture(GL_TEXTURE0);
            gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.DEPTHBUFFER.ordinal()]);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
            gl4.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT24, size.x, size.y, 0,
                    GL_DEPTH_COMPONENT, GL_FLOAT, null);

            gl4.glActiveTexture(GL_TEXTURE0);
            gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.STENCILBUFFER.ordinal()]);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
            gl4.glTexImage2D(GL_TEXTURE_2D, 0, GL_STENCIL_INDEX8, size.x, size.y, 0,
                    GL_STENCIL_INDEX, GL_UNSIGNED_BYTE, null);

            gl4.glPixelStorei(GL_UNPACK_ALIGNMENT, 4);

        } catch (IOException ex) {
            Logger.getLogger(Gl_440_fbo_depth_stencil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return validated;
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(Program.MAX.ordinal(), vertexArrayName, 0);
        gl4.glBindVertexArray(vertexArrayName[Program.TEXTURE.ordinal()]);
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 2 * Vec2.SIZEOF, 0);
            gl4.glVertexAttribPointer(Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, 2 * Vec2.SIZEOF, Vec2.SIZEOF);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl4.glEnableVertexAttribArray(Semantic.Attr.TEXCOORD);

            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT.ordinal()]);
        }
        gl4.glBindVertexArray(0);

        gl4.glBindVertexArray(vertexArrayName[Program.SPLASH.ordinal()]);
        gl4.glBindVertexArray(0);

        return true;
    }

    private boolean initFramebuffer(GL4 gl4) {

        gl4.glGenFramebuffers(1, framebufferName, 0);
        gl4.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[0]);
        gl4.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, textureName[Texture.COLORBUFFER.ordinal()], 0);
        gl4.glFramebufferTexture(GL_FRAMEBUFFER, GL_STENCIL_ATTACHMENT, textureName[Texture.STENCILBUFFER.ordinal()], 0);

        if (!isFramebufferComplete(gl4, framebufferName[0])) {
            return false;
        }

        gl4.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        {
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM.ordinal()]);
            ByteBuffer pointer = gl4.glMapBufferRange(GL_UNIFORM_BUFFER,
                    0, Mat4.SIZEOF, GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            //glm::mat4 Projection = glm::perspectiveFov(glm::pi<float>() * 0.25f, 640.f, 480.f, 0.1f, 100.0f);
            Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, (float) windowSize.x / windowSize.y, 0.1f, 100.0f);
            Mat4 model = new Mat4(1.0f);

            pointer.asFloatBuffer().put(projection.mul(viewMat4()).mul(model).toFA_());

            // Make sure the uniform buffer is uploaded
            gl4.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        {
            gl4.glViewport(0, 0, windowSize.x * framebufferScale, windowSize.y * framebufferScale);

            gl4.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[0]);
            gl4.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 0.5f, 0.0f, 1.0f}, 0);

            gl4.glUseProgram(programName[Program.TEXTURE.ordinal()]);

            gl4.glActiveTexture(GL_TEXTURE0);
            gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.DIFFUSE.ordinal()]);
            gl4.glBindVertexArray(vertexArrayName[Program.TEXTURE.ordinal()]);
            gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.TRANSFORM.ordinal()]);

            gl4.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 2, 0);
        }

        {
            gl4.glViewport(0, 0, windowSize.x, windowSize.y);

            gl4.glBindFramebuffer(GL_FRAMEBUFFER, 0);

            gl4.glUseProgram(programName[Program.SPLASH.ordinal()]);

            gl4.glActiveTexture(GL_TEXTURE0);
            gl4.glBindVertexArray(vertexArrayName[Program.SPLASH.ordinal()]);
            gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.COLORBUFFER.ordinal()]);

            gl4.glDrawArraysInstanced(GL_TRIANGLES, 0, 3, 1);
        }

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteFramebuffers(1, framebufferName, 0);
        gl4.glDeleteProgram(programName[Program.SPLASH.ordinal()]);
        gl4.glDeleteProgram(programName[Program.TEXTURE.ordinal()]);

        gl4.glDeleteBuffers(Buffer.MAX.ordinal(), bufferName, 0);
        gl4.glDeleteTextures(Texture.MAX.ordinal(), textureName, 0);
        gl4.glDeleteVertexArrays(Program.MAX.ordinal(), vertexArrayName, 0);

        return true;
    }
}
