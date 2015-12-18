/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_320.fbo;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_BACK;
import static com.jogamp.opengl.GL.GL_CLAMP_TO_EDGE;
import static com.jogamp.opengl.GL.GL_COLOR_ATTACHMENT0;
import static com.jogamp.opengl.GL.GL_COMPRESSED_RGB_S3TC_DXT1_EXT;
import static com.jogamp.opengl.GL.GL_DEPTH_ATTACHMENT;
import static com.jogamp.opengl.GL.GL_DEPTH_COMPONENT24;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_DYNAMIC_DRAW;
import static com.jogamp.opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_FRAMEBUFFER;
import static com.jogamp.opengl.GL.GL_LEQUAL;
import static com.jogamp.opengl.GL.GL_LESS;
import static com.jogamp.opengl.GL.GL_LINEAR;
import static com.jogamp.opengl.GL.GL_MAP_INVALIDATE_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_MAP_WRITE_BIT;
import static com.jogamp.opengl.GL.GL_NEAREST;
import static com.jogamp.opengl.GL.GL_NEAREST_MIPMAP_NEAREST;
import static com.jogamp.opengl.GL.GL_NONE;
import static com.jogamp.opengl.GL.GL_RGBA;
import static com.jogamp.opengl.GL.GL_RGBA8;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
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
import static com.jogamp.opengl.GL2.GL_COMPARE_R_TO_TEXTURE;
import static com.jogamp.opengl.GL2ES2.GL_DEPTH_COMPONENT;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_TEXTURE_COMPARE_FUNC;
import static com.jogamp.opengl.GL2ES2.GL_TEXTURE_COMPARE_MODE;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
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
import java.nio.ShortBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import jgli.Texture2D;
import jglm.Vec2;
import jglm.Vec2i;

/**
 *
 * @author GBarbieri
 */
public class Gl_320_fbo_shadow extends Test {

    public static void main(String[] args) {
        Gl_320_fbo_shadow gl_320_fbo_shadow = new Gl_320_fbo_shadow();
    }

    public Gl_320_fbo_shadow() {
        super("Gl-320-fbo-shadow", Profile.CORE, 3, 2, new Vec2(0.0f, -(float) Math.PI * 0.3f));
    }

    private final String SHADER_SOURCE_DEPTH = "fbo-shadow-depth";
    private final String SHADER_SOURCE_RENDER = "fbo-shadow-render";
    private final String SHADERS_ROOT = "src/data/gl_320/fbo";
    private final String TEXTURE_DIFFUSE = "kueken7_rgb_dxt1_unorm.dds";

    private int vertexCount = 8;
    private int vertexSize = vertexCount * (3 * Float.BYTES + 4 * Byte.BYTES);
    private float[] vertexV3fData = {
        -1.0f, -1.0f, 0.0f,
        +1.0f, -1.0f, 0.0f,
        +1.0f, +1.0f, 0.0f,
        -1.0f, +1.0f, 0.0f,
        -0.1f, -0.1f, 0.2f,
        +0.1f, -0.1f, 0.2f,
        +0.1f, +0.1f, 0.2f,
        -0.1f, +0.1f, 0.2f};
    private byte[] vertexV4u8Data = {
        (byte) 255, (byte) 127, (byte) 0, (byte) 255,
        (byte) 255, (byte) 127, (byte) 0, (byte) 255,
        (byte) 255, (byte) 127, (byte) 0, (byte) 255,
        (byte) 255, (byte) 127, (byte) 0, (byte) 255,
        (byte) 0, (byte) 127, (byte) 255, (byte) 255,
        (byte) 0, (byte) 127, (byte) 255, (byte) 255,
        (byte) 0, (byte) 127, (byte) 255, (byte) 255,
        (byte) 0, (byte) 127, (byte) 255, (byte) 255};
    private byte[] vertexData = new byte[vertexSize];

    private int elementCount = 12;
    private int elementSize = elementCount * Short.BYTES;
    private short[] elementData = {
        0, 1, 2,
        2, 3, 0,
        4, 5, 6,
        6, 7, 4};

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
        SHADOWMAP,
        MAX
    }

    private enum Program {
        DEPTH,
        RENDER,
        MAX
    }

    private enum Framebuffer {
        FRAMEBUFFER,
        SHADOW,
        MAX
    }

    private enum Shader {
        VERT_RENDER,
        FRAG_RENDER,
        VERT_DEPTH,
        MAX
    }

    private int[] framebufferName = new int[Framebuffer.MAX.ordinal()], programName = new int[Program.MAX.ordinal()],
            vertexArrayName = new int[Program.MAX.ordinal()], bufferName = new int[Buffer.MAX.ordinal()],
            textureName = new int[Texture.MAX.ordinal()], uniformTransform = new int[Program.MAX.ordinal()];
    private int uniformShadow;
    private Vec2i shadowSize = new Vec2i(64, 64);
    private float[] projection = new float[16], view = new float[16], model = new float[16], depthMvp = new float[16];

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

        ShaderCode[] shaderCodes = new ShaderCode[Shader.MAX.ordinal()];

        if (validated) {

            shaderCodes[Shader.VERT_RENDER.ordinal()] = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADER_SOURCE_RENDER, "vert", null, true);
            shaderCodes[Shader.FRAG_RENDER.ordinal()] = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADER_SOURCE_RENDER, "frag", null, true);

            ShaderProgram program = new ShaderProgram();

            program.add(shaderCodes[Shader.VERT_RENDER.ordinal()]);
            program.add(shaderCodes[Shader.FRAG_RENDER.ordinal()]);

            program.init(gl3);

            programName[Program.RENDER.ordinal()] = program.program();

            gl3.glBindAttribLocation(programName[Program.RENDER.ordinal()], Semantic.Attr.POSITION, "position");
            gl3.glBindAttribLocation(programName[Program.RENDER.ordinal()], Semantic.Attr.COLOR, "color");
            gl3.glBindFragDataLocation(programName[Program.RENDER.ordinal()], Semantic.Frag.COLOR, "color");

            program.link(gl3, System.out);
        }

        if (validated) {

            uniformTransform[Program.RENDER.ordinal()]
                    = gl3.glGetUniformBlockIndex(programName[Program.RENDER.ordinal()], "Transform");
            uniformShadow = gl3.glGetUniformLocation(programName[Program.RENDER.ordinal()], "shadow");
        }

        if (validated) {

            shaderCodes[Shader.VERT_DEPTH.ordinal()] = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADER_SOURCE_DEPTH, "vert", null, true);

            ShaderProgram program = new ShaderProgram();
            program.add(shaderCodes[Shader.VERT_DEPTH.ordinal()]);

            program.init(gl3);

            programName[Program.DEPTH.ordinal()] = program.program();

            gl3.glBindAttribLocation(programName[Program.DEPTH.ordinal()], Semantic.Attr.POSITION, "position");

            program.link(gl3, System.out);
        }

        if (validated) {

            uniformTransform[Program.DEPTH.ordinal()]
                    = gl3.glGetUniformBlockIndex(programName[Program.DEPTH.ordinal()], "Transform");
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
        ByteBuffer vertexBuffer = GLBuffers.newDirectByteBuffer(vertexSize);
        for (int v = 0; v < vertexCount; v++) {
            for (int i = 0; i < 3; i++) {
                vertexBuffer.putFloat(vertexV3fData[v * 3 + i]);
            }
            for (int i = 0; i < 4; i++) {
                vertexBuffer.put(vertexV4u8Data[v * 4 + i]);
            }
        }
        vertexBuffer.rewind();
        gl3.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        int[] uniformBufferOffset = new int[1];
        gl3.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset, 0);
        int uniformBlockSize = Math.max(16 * Float.BYTES * 3, uniformBufferOffset[0]);

        gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM.ordinal()]);
        gl3.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_DYNAMIC_DRAW);
        gl3.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return true;
    }

    private boolean initTexture(GL3 gl3) {

        boolean validated = true;

        try {

            jgli.Texture2D texture = new Texture2D(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE));
            assert (!texture.empty());

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
                gl3.glCompressedTexImage2D(
                        GL_TEXTURE_2D,
                        level,
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
            gl3.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, windowSize.x, windowSize.y, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);

            gl3.glActiveTexture(GL_TEXTURE0);
            gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.RENDERBUFFER.ordinal()]);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
            gl3.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT24, windowSize.x, windowSize.y,
                    0, GL_DEPTH_COMPONENT, GL_FLOAT, null);

            gl3.glActiveTexture(GL_TEXTURE0);
            gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.SHADOWMAP.ordinal()]);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_R_TO_TEXTURE);
            gl3.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT24, shadowSize.x, shadowSize.y,
                    0, GL_DEPTH_COMPONENT, GL_FLOAT, null);

            gl3.glPixelStorei(GL_UNPACK_ALIGNMENT, 4);

            return validated;
        } catch (IOException ex) {
            Logger.getLogger(Gl_320_fbo_shadow.class.getName()).log(Level.SEVERE, null, ex);
        }
        return validated;
    }

    private boolean initVertexArray(GL3 gl3) {

        gl3.glGenVertexArrays(Program.MAX.ordinal(), vertexArrayName, 0);
        gl3.glBindVertexArray(vertexArrayName[Program.RENDER.ordinal()]);
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 3, GL_FLOAT, false, 3 * Float.BYTES + 4 * Byte.BYTES, 0);
            gl3.glVertexAttribPointer(Semantic.Attr.COLOR, 4, GL_UNSIGNED_BYTE, true,
                    3 * Float.BYTES + 4 * Byte.BYTES, 3 * Float.BYTES);
            gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl3.glEnableVertexAttribArray(Semantic.Attr.COLOR);

            gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT.ordinal()]);
        }
        gl3.glBindVertexArray(0);

        return true;
    }

    private boolean initFramebuffer(GL3 gl3) {

        gl3.glGenFramebuffers(Framebuffer.MAX.ordinal(), framebufferName, 0);

        int[] buffersRender = new int[]{GL_COLOR_ATTACHMENT0};
        gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[Framebuffer.FRAMEBUFFER.ordinal()]);
        gl3.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, textureName[Texture.COLORBUFFER.ordinal()], 0);
        gl3.glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, textureName[Texture.RENDERBUFFER.ordinal()], 0);
        gl3.glDrawBuffers(1, buffersRender, 0);
        if (!isFramebufferComplete(gl3, framebufferName[Framebuffer.FRAMEBUFFER.ordinal()])) {
            return false;
        }

        gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[Framebuffer.SHADOW.ordinal()]);
        gl3.glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, textureName[Texture.SHADOWMAP.ordinal()], 0);
        gl3.glDrawBuffer(GL_NONE);
        if (!isFramebufferComplete(gl3, framebufferName[Framebuffer.SHADOW.ordinal()])) {
            return false;
        }
        gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        gl3.glDrawBuffer(GL_BACK);
        if (!isFramebufferComplete(gl3, 0)) {
            return false;
        }

        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL3 gl3 = (GL3) gl;

        gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM.ordinal()]);
        ByteBuffer pointer = gl3.glMapBufferRange(
                GL_UNIFORM_BUFFER, 0, 16 * Float.BYTES * 3,
                GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

        // Update of the MVP matrix for the render pass
        {
            FloatUtil.makePerspective(projection, 0, true, (float) Math.PI * 0.25f, 4.0f / 3.0f, 0.1f, 10.0f);
            FloatUtil.makeIdentity(model);
            FloatUtil.multMatrix(projection, view());
            FloatUtil.multMatrix(projection, model);

            for (float f : projection) {
                pointer.putFloat(f);
            }
        }

        // Update of the MVP matrix for the depth pass
        {
            FloatUtil.makeOrtho(projection, 0, true, -1.0f, 1.0f, -1.0f, 1.0f, -4.0f, 8.0f);
            FloatUtil.makeLookAt(view, 0, new float[]{0.5f, 1.0f, 2.0f}, 0, new float[3], 0,
                    new float[]{0, 0, 1}, 0, model);
            FloatUtil.makeIdentity(model);
            FloatUtil.multMatrix(projection, view, depthMvp);
            FloatUtil.multMatrix(depthMvp, model);

            for (float f : depthMvp) {
                pointer.putFloat(f);
            }

            float[] biasMatrix = {
                0.5f, 0.0f, 0.0f, 0.0f,
                0.0f, 0.5f, 0.0f, 0.0f,
                0.0f, 0.0f, 0.5f, 0.0f,
                0.5f, 0.5f, 0.5f, 1.0f};

            FloatUtil.multMatrix(biasMatrix, depthMvp);

            for (float f : biasMatrix) {
                pointer.putFloat(f);
            }

            pointer.rewind();
        }

        gl3.glUnmapBuffer(GL_UNIFORM_BUFFER);

        gl3.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.TRANSFORM.ordinal()]);

        renderShadow(gl3);
        renderFramebuffer(gl3);

        return checkError(gl3, "render");
    }

    private void renderShadow(GL3 gl3) {

        gl3.glEnable(GL_DEPTH_TEST);
        gl3.glDepthFunc(GL_LESS);

        gl3.glViewport(0, 0, shadowSize.x, shadowSize.y);

        gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[Framebuffer.SHADOW.ordinal()]);
        float[] depth = {1.0f};
        gl3.glClearBufferfv(GL_DEPTH, 0, depth, 0);

        // Bind rendering objects
        gl3.glUseProgram(programName[Program.DEPTH.ordinal()]);
        gl3.glUniformBlockBinding(programName[Program.DEPTH.ordinal()], uniformTransform[Program.DEPTH.ordinal()],
                Semantic.Uniform.TRANSFORM0);

        gl3.glBindVertexArray(vertexArrayName[Program.RENDER.ordinal()]);

        checkError(gl3, "renderShadow 0");

        gl3.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0);

        checkError(gl3, "renderShadow 1");

        gl3.glDisable(GL_DEPTH_TEST);

        checkError(gl3, "renderShadow");
    }

    private void renderFramebuffer(GL3 gl3) {

        gl3.glEnable(GL_DEPTH_TEST);
        gl3.glDepthFunc(GL_LESS);

        gl3.glViewport(0, 0, windowSize.x, windowSize.y);

        gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        float[] depth = {1.0f};
        gl3.glClearBufferfv(GL_DEPTH, 0, depth, 0);
        gl3.glClearBufferfv(GL_COLOR, 0, new float[]{0.0f, 0.0f, 0.0f, 1.0f}, 0);

        gl3.glUseProgram(programName[Program.RENDER.ordinal()]);
        gl3.glUniform1i(uniformShadow, 0);
        gl3.glUniformBlockBinding(programName[Program.RENDER.ordinal()], uniformTransform[Program.RENDER.ordinal()],
                Semantic.Uniform.TRANSFORM0);

        gl3.glActiveTexture(GL_TEXTURE0);
        gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.SHADOWMAP.ordinal()]);

        gl3.glBindVertexArray(vertexArrayName[Program.RENDER.ordinal()]);
        gl3.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0);

        gl3.glDisable(GL_DEPTH_TEST);

        checkError(gl3, "renderFramebuffer");
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;

        for (int i = 0; i < Program.MAX.ordinal(); ++i) {
            gl3.glDeleteProgram(programName[i]);
        }

        gl3.glDeleteFramebuffers(Framebuffer.MAX.ordinal(), framebufferName, 0);
        gl3.glDeleteBuffers(Buffer.MAX.ordinal(), bufferName, 0);
        gl3.glDeleteTextures(Texture.MAX.ordinal(), textureName, 0);
        gl3.glDeleteVertexArrays(Program.MAX.ordinal(), vertexArrayName, 0);

        return checkError(gl3, "end");
    }
}
