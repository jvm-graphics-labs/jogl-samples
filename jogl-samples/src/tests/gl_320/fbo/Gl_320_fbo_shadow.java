/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_320.fbo;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2.GL_COMPARE_R_TO_TEXTURE;
import static com.jogamp.opengl.GL2ES3.*;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import glm.glm;
import glm.mat._4.Mat4;
import glm.vec._2.Vec2;
import glm.vec._2.i.Vec2i;
import dev.Vec4u8;
import glm.vec._3.Vec3;
import framework.BufferUtils;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import glf.Vertex_v3fv4u8;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

/**
 *
 * @author GBarbieri
 */
public class Gl_320_fbo_shadow extends Test {

    public static void main(String[] args) {
        Gl_320_fbo_shadow gl_320_fbo_shadow = new Gl_320_fbo_shadow();
    }

    public Gl_320_fbo_shadow() {
        super("Gl-320-fbo-shadow", Profile.CORE, 3, 2, new Vec2(0.0f, -Math.PI * 0.3f));
    }

    private final String SHADER_SOURCE_DEPTH = "fbo-shadow-depth";
    private final String SHADER_SOURCE_RENDER = "fbo-shadow-render";
    private final String SHADERS_ROOT = "src/data/gl_320/fbo";

    private int vertexCount = 8;
    private int vertexSize = vertexCount * glf.Vertex_v3fv4u8.SIZE;
    private glf.Vertex_v3fv4u8[] vertexData = {
        new Vertex_v3fv4u8(new Vec3(-1.0f, -1.0f, 0.0f), new Vec4u8(255, 127, 0, 255)),
        new Vertex_v3fv4u8(new Vec3(+1.0f, -1.0f, 0.0f), new Vec4u8(255, 127, 0, 255)),
        new Vertex_v3fv4u8(new Vec3(+1.0f, +1.0f, 0.0f), new Vec4u8(255, 127, 0, 255)),
        new Vertex_v3fv4u8(new Vec3(-1.0f, +1.0f, 0.0f), new Vec4u8(255, 127, 0, 255)),
        new Vertex_v3fv4u8(new Vec3(-0.1f, -0.1f, 0.2f), new Vec4u8(0, 127, 255, 255)),
        new Vertex_v3fv4u8(new Vec3(+0.1f, -0.1f, 0.2f), new Vec4u8(0, 127, 255, 255)),
        new Vertex_v3fv4u8(new Vec3(+0.1f, +0.1f, 0.2f), new Vec4u8(0, 127, 255, 255)),
        new Vertex_v3fv4u8(new Vec3(-0.1f, +0.1f, 0.2f), new Vec4u8(0, 127, 255, 255))};

    private int elementCount = 12;
    private int elementSize = elementCount * Short.BYTES;
    private short[] elementData = {
        0, 1, 2,
        2, 3, 0,
        4, 5, 6,
        6, 7, 4};

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int ELEMENT = 1;
        public static final int TRANSFORM = 2;
        public static final int MAX = 3;
    }

    private class Program {

        public static final int DEPTH = 0;
        public static final int RENDER = 1;
        public static final int MAX = 2;
    }

    private class Shader {

        public static final int VERT_RENDER = 0;
        public static final int FRAG_RENDER = 1;
        public static final int VERT_DEPTH = 2;
        public static final int MAX = 3;
    }

    private int[] framebufferName = {0}, programName = new int[Program.MAX], vertexArrayName = new int[Program.MAX],
            bufferName = new int[Buffer.MAX], textureName = {0}, uniformTransform = new int[Program.MAX];
    private int uniformShadow;
    private Vec2i shadowSize = new Vec2i(64, 64);

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

        ShaderCode[] shaderCodes = new ShaderCode[Shader.MAX];

        if (validated) {

            shaderCodes[Shader.VERT_RENDER] = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADER_SOURCE_RENDER, "vert", null, true);
            shaderCodes[Shader.FRAG_RENDER] = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADER_SOURCE_RENDER, "frag", null, true);

            ShaderProgram program = new ShaderProgram();

            program.add(shaderCodes[Shader.VERT_RENDER]);
            program.add(shaderCodes[Shader.FRAG_RENDER]);

            program.init(gl3);

            programName[Program.RENDER] = program.program();

            gl3.glBindAttribLocation(programName[Program.RENDER], Semantic.Attr.POSITION, "position");
            gl3.glBindAttribLocation(programName[Program.RENDER], Semantic.Attr.COLOR, "color");
            gl3.glBindFragDataLocation(programName[Program.RENDER], Semantic.Frag.COLOR, "color");

            program.link(gl3, System.out);
        }

        if (validated) {

            uniformTransform[Program.RENDER]
                    = gl3.glGetUniformBlockIndex(programName[Program.RENDER], "Transform");
            uniformShadow = gl3.glGetUniformLocation(programName[Program.RENDER], "shadow");
        }

        if (validated) {

            shaderCodes[Shader.VERT_DEPTH] = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADER_SOURCE_DEPTH, "vert", null, true);

            ShaderProgram program = new ShaderProgram();
            program.add(shaderCodes[Shader.VERT_DEPTH]);

            program.init(gl3);

            programName[Program.DEPTH] = program.program();

            gl3.glBindAttribLocation(programName[Program.DEPTH], Semantic.Attr.POSITION, "position");

            program.link(gl3, System.out);
        }

        if (validated) {

            uniformTransform[Program.DEPTH]
                    = gl3.glGetUniformBlockIndex(programName[Program.DEPTH], "Transform");
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
        ByteBuffer vertexBuffer = GLBuffers.newDirectByteBuffer(vertexSize);
        for (int i = 0; i < vertexCount; i++) {
            vertexData[i].toBb(vertexBuffer, i);
        }
        vertexBuffer.rewind();
        gl3.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(vertexBuffer);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        int[] uniformBufferOffset = {0};
        gl3.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset, 0);
        int uniformBlockSize = Math.max(Mat4.SIZE * 3, uniformBufferOffset[0]);

        gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
        gl3.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_DYNAMIC_DRAW);
        gl3.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return true;
    }

    private boolean initTexture(GL3 gl3) {

        boolean validated = true;

        gl3.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        gl3.glGenTextures(1, textureName, 0);

        gl3.glActiveTexture(GL_TEXTURE0);
        gl3.glBindTexture(GL_TEXTURE_2D, textureName[0]);
        gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
        gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
        gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);
        gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_R_TO_TEXTURE);
        gl3.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT24, shadowSize.x, shadowSize.y, 0, GL_DEPTH_COMPONENT,
                GL_FLOAT, null);

        gl3.glPixelStorei(GL_UNPACK_ALIGNMENT, 4);

        return validated;
    }

    private boolean initVertexArray(GL3 gl3) {

        gl3.glGenVertexArrays(Program.MAX, vertexArrayName, 0);
        gl3.glBindVertexArray(vertexArrayName[Program.RENDER]);
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 3, GL_FLOAT, false, Vertex_v3fv4u8.SIZE, 0);
            gl3.glVertexAttribPointer(Semantic.Attr.COLOR, 4, GL_UNSIGNED_BYTE, true, Vertex_v3fv4u8.SIZE, Vec3.SIZE);
            gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl3.glEnableVertexAttribArray(Semantic.Attr.COLOR);

            gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT]);
        }
        gl3.glBindVertexArray(0);

        return true;
    }

    private boolean initFramebuffer(GL3 gl3) {

        gl3.glGenFramebuffers(1, framebufferName, 0);

        gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[0]);
        gl3.glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, textureName[0], 0);
        gl3.glDrawBuffer(GL_NONE);
        if (!isFramebufferComplete(gl3, framebufferName[0])) {
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

        gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM]);
        ByteBuffer pointer = gl3.glMapBufferRange(
                GL_UNIFORM_BUFFER, 0, Mat4.SIZE * 3,
                GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

        // Update of the MVP matrix for the render pass
        {
            Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, 4.0f / 3.0f, 0.1f, 10.0f);
            Mat4 model = new Mat4(1.0f);
            pointer.asFloatBuffer().put(projection.mul(viewMat4()).mul(model).toFa_());
        }

        // Update of the MVP matrix for the depth pass
        {
            Mat4 projection = glm.ortho_(-1.0f, 1.0f, -1.0f, 1.0f, -4.0f, 8.0f);
            Mat4 view = glm.lookAt_(new Vec3(0.5, 1.0, 2.0), new Vec3(0), new Vec3(0, 0, 1));
            Mat4 model = new Mat4(1.0f);
            Mat4 depthMVP = projection.mul(view).mul(model);
            pointer.position(Mat4.SIZE);
            pointer.asFloatBuffer().put(depthMVP.toFa_());

            Mat4 biasMatrix = new Mat4(
                    0.5, 0.0, 0.0, 0.0,
                    0.0, 0.5, 0.0, 0.0,
                    0.0, 0.0, 0.5, 0.0,
                    0.5, 0.5, 0.5, 1.0);

            pointer.position(2 * Mat4.SIZE);
            pointer.asFloatBuffer().put(biasMatrix.mul(depthMVP).toFa_());

            pointer.rewind();
        }

        gl3.glUnmapBuffer(GL_UNIFORM_BUFFER);

        gl3.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.TRANSFORM]);

        renderShadow(gl3);
        renderFramebuffer(gl3);

        return checkError(gl3, "render");
    }

    private void renderShadow(GL3 gl3) {

        gl3.glEnable(GL_DEPTH_TEST);
        gl3.glDepthFunc(GL_LESS);

        gl3.glViewport(0, 0, shadowSize.x, shadowSize.y);

        gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[0]);
        float[] depth = {1.0f};
        gl3.glClearBufferfv(GL_DEPTH, 0, depth, 0);

        // Bind rendering objects
        gl3.glUseProgram(programName[Program.DEPTH]);
        gl3.glUniformBlockBinding(programName[Program.DEPTH], uniformTransform[Program.DEPTH],
                Semantic.Uniform.TRANSFORM0);

        gl3.glBindVertexArray(vertexArrayName[Program.RENDER]);

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

        gl3.glUseProgram(programName[Program.RENDER]);
        gl3.glUniform1i(uniformShadow, 0);
        gl3.glUniformBlockBinding(programName[Program.RENDER], uniformTransform[Program.RENDER],
                Semantic.Uniform.TRANSFORM0);

        gl3.glActiveTexture(GL_TEXTURE0);
        gl3.glBindTexture(GL_TEXTURE_2D, textureName[0]);

        gl3.glBindVertexArray(vertexArrayName[Program.RENDER]);
        gl3.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0);

        gl3.glDisable(GL_DEPTH_TEST);

        checkError(gl3, "renderFramebuffer");
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;

        for (int i = 0; i < Program.MAX; ++i) {
            gl3.glDeleteProgram(programName[i]);
        }

        gl3.glDeleteFramebuffers(1, framebufferName, 0);
        gl3.glDeleteBuffers(Buffer.MAX, bufferName, 0);
        gl3.glDeleteTextures(1, textureName, 0);
        gl3.glDeleteVertexArrays(Program.MAX, vertexArrayName, 0);

        return checkError(gl3, "end");
    }
}
