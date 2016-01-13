/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_420;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_COLOR_ATTACHMENT0;
import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DRAW_FRAMEBUFFER;
import static com.jogamp.opengl.GL.GL_DYNAMIC_DRAW;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_FRAMEBUFFER;
import static com.jogamp.opengl.GL.GL_LINE_LOOP;
import static com.jogamp.opengl.GL.GL_LINE_SMOOTH_HINT;
import static com.jogamp.opengl.GL.GL_MAP_INVALIDATE_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_MAP_WRITE_BIT;
import static com.jogamp.opengl.GL.GL_NEAREST;
import static com.jogamp.opengl.GL.GL_NICEST;
import static com.jogamp.opengl.GL.GL_READ_FRAMEBUFFER;
import static com.jogamp.opengl.GL.GL_RGBA;
import static com.jogamp.opengl.GL.GL_RGBA8;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_TRUE;
import static com.jogamp.opengl.GL.GL_UNSIGNED_BYTE;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER_BIT;
import static com.jogamp.opengl.GL2ES2.GL_PROGRAM_SEPARABLE;
import static com.jogamp.opengl.GL2ES2.GL_TEXTURE_2D_MULTISAMPLE;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER_BIT;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import static jdk.nashorn.internal.objects.ArrayBufferView.buffer;
import jglm.Vec2;
import jglm.Vec2i;

/**
 *
 * @author GBarbieri
 */
public class Gl_420_primitive_line_aa extends Test {

    public static void main(String[] args) {
        Gl_420_primitive_line_aa gl_420_primitive_line_aa = new Gl_420_primitive_line_aa();
    }

    public Gl_420_primitive_line_aa() {
        super("gl-420-primitive-line-aa", Profile.CORE, 4, 2);
    }

    private final String SHADERS_SOURCE_AA = "primitive-line-aa";
    private final String SHADERS_SOURCE_SPLASH = "primitive-line-splash";
    private final String SHADERS_ROOT = "src/data/gl_420";

    private final Vec2i FRAMEBUFFER_SIZE = new Vec2i(80, 60);
    private Vec2[] vertexData;

    private enum Buffer {
        VERTEX,
        TRANSFORM,
        MAX
    }

    private enum Texture {
        MULTISAMPLE,
        COLOR,
        MAX
    }

    private enum Framebuffer {
        MULTISAMPLE,
        COLOR,
        MAX
    }

    private enum Pipeline {
        MULTISAMPLE,
        SPLASH,
        MAX
    }

    private int[] pipelineName = new int[Pipeline.MAX.ordinal()], programName = new int[Pipeline.MAX.ordinal()],
            vertexArrayName = new int[Pipeline.MAX.ordinal()], framebufferName = new int[Framebuffer.MAX.ordinal()],
            bufferName = new int[Buffer.MAX.ordinal()], textureName = new int[Texture.MAX.ordinal()];
    private float[] projection = new float[16], model = new float[16];

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;

        if (validated) {
            validated = initState(gl4);
        }
        if (validated) {
            validated = initProgram(gl4);
        }
        if (validated) {
            validated = initBuffer(gl4);
        }
        if (validated) {
            validated = initTexture(gl4);
        }
        if (validated) {
            validated = initFramebuffer(gl4);
        }
        if (validated) {
            validated = initVertexArray(gl4);
        }

        return validated;
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        // Create program
        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_AA, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_AA, "frag", null, true);

            shaderProgram.init(gl4);
            programName[Pipeline.MULTISAMPLE.ordinal()] = shaderProgram.program();

            gl4.glProgramParameteri(programName[Pipeline.MULTISAMPLE.ordinal()], GL_PROGRAM_SEPARABLE, GL_TRUE);

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);
            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_SPLASH, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_SPLASH, "frag", null, true);

            shaderProgram.init(gl4);
            programName[Pipeline.SPLASH.ordinal()] = shaderProgram.program();

            gl4.glProgramParameteri(programName[Pipeline.SPLASH.ordinal()], GL_PROGRAM_SEPARABLE, GL_TRUE);

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);
            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            gl4.glGenProgramPipelines(Pipeline.MAX.ordinal(), pipelineName, 0);
            gl4.glUseProgramStages(pipelineName[Pipeline.MULTISAMPLE.ordinal()],
                    GL_VERTEX_SHADER_BIT | GL_FRAGMENT_SHADER_BIT, programName[Pipeline.MULTISAMPLE.ordinal()]);
            gl4.glUseProgramStages(pipelineName[Pipeline.SPLASH.ordinal()],
                    GL_VERTEX_SHADER_BIT | GL_FRAGMENT_SHADER_BIT, programName[Pipeline.SPLASH.ordinal()]);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        int count = 1000;
        float step = (float) (Math.PI * 2.0f / count);

        vertexData = new Vec2[count];
        for (int i = 0; i < count; ++i) //VertexData[i] = glm::vec2(glm::fastSin(glm::radians(Step * float(i))), glm::fastCos(glm::radians(Step * float(i))));
        /*
			VertexData[i] = glm::vec2(
				glm::fastSin(glm::mod(Step * float(i), glm::pi<float>())),
				glm::fastCos(glm::mod(Step * float(i), glm::pi<float>())));
         */ /*
			VertexData[i] = glm::vec2(
				glm::sin(Step * float(i)),
				glm::fastCos(glm::mod(Step * float(i), glm::pi<float>())));
         */ {
            vertexData[i] = new Vec2(
                    (float) Math.sin(step * i),
                    (float) Math.cos(step * i));
        }
        gl4.glGenBuffers(Buffer.MAX.ordinal(), bufferName, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData.length * 2);
        for (Vec2 vertex : vertexData) {
            vertexBuffer.put(vertex.x);
            vertexBuffer.put(vertex.y);
        }
        vertexBuffer.rewind();
        gl4.glBufferData(GL_ARRAY_BUFFER, vertexData.length * 2 * Float.BYTES, vertexBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        int[] uniformBufferOffset = {0};
        gl4.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset, 0);
        int uniformBlockSize = Math.max(projection.length * Float.BYTES, uniformBufferOffset[0]);

        gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM.ordinal()]);
        gl4.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_DYNAMIC_DRAW);
        gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        return true;
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(Pipeline.MAX.ordinal(), vertexArrayName, 0);

        gl4.glBindVertexArray(vertexArrayName[Pipeline.MULTISAMPLE.ordinal()]);
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
        }
        gl4.glBindVertexArray(0);

        gl4.glBindVertexArray(vertexArrayName[Pipeline.SPLASH.ordinal()]);
        gl4.glBindVertexArray(0);

        return true;
    }

    private boolean initFramebuffer(GL4 gl4) {

        gl4.glGenFramebuffers(Framebuffer.MAX.ordinal(), framebufferName, 0);

        gl4.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[Framebuffer.MULTISAMPLE.ordinal()]);
        gl4.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, textureName[Texture.MULTISAMPLE.ordinal()], 0);
        if (!isFramebufferComplete(gl4, framebufferName[Framebuffer.MULTISAMPLE.ordinal()])) {
            return false;
        }
        gl4.glBindFramebuffer(GL_FRAMEBUFFER, 0);

        gl4.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[Framebuffer.COLOR.ordinal()]);
        gl4.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, textureName[Texture.COLOR.ordinal()], 0);
        if (!isFramebufferComplete(gl4, framebufferName[Framebuffer.COLOR.ordinal()])) {
            return false;
        }
        gl4.glBindFramebuffer(GL_FRAMEBUFFER, 0);

        return true;
    }

    private boolean initTexture(GL4 gl4) {

        gl4.glGenTextures(Texture.MAX.ordinal(), textureName, 0);

        {
            gl4.glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, textureName[Texture.MULTISAMPLE.ordinal()]);
            gl4.glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, 8, GL_RGBA8,
                    FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y, true);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            gl4.glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, 0);
        }

        {
            gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.COLOR.ordinal()]);
            gl4.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y,
                    0, GL_RGBA, GL_UNSIGNED_BYTE, null);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            gl4.glBindTexture(GL_TEXTURE_2D, 0);
        }

        return true;
    }

    private boolean initState(GL4 gl4) {

        gl4.glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);

        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        {
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.TRANSFORM.ordinal()]);
            ByteBuffer pointer = gl4.glMapBufferRange(
                    GL_UNIFORM_BUFFER, 0, projection.length * Float.BYTES,
                    GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            FloatUtil.makePerspective(projection, 0, true, (float) Math.PI * 0.25f,
                    (float) windowSize.x / windowSize.y, 0.1f, 100.0f);
            //glm::mat4 Projection = glm::perspective(glm::pi<float>() * 0.25f, 4.0f / 3.0f, 0.1f, 100.0f);
            FloatUtil.makeIdentity(model);

            FloatUtil.multMatrix(projection, view());
            FloatUtil.multMatrix(projection, model);

            for (float f : projection) {
                pointer.putFloat(f);
            }
            pointer.rewind();

            // Make sure the uniform buffer is uploaded
            gl4.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        //////////////////////////////
        // Render multisampled texture
        //glBindFramebuffer(GL_FRAMEBUFFER, 0);
        gl4.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName[Framebuffer.MULTISAMPLE.ordinal()]);
        //glEnable(GL_MULTISAMPLE);
        gl4.glViewportIndexedf(0, 0, 0, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y);
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 0.5f, 0.0f, 1.0f}, 0);

        gl4.glBindProgramPipeline(pipelineName[Pipeline.MULTISAMPLE.ordinal()]);
        gl4.glBindVertexArray(vertexArrayName[Pipeline.MULTISAMPLE.ordinal()]);
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName[Buffer.TRANSFORM.ordinal()]);

        gl4.glDrawArraysInstancedBaseInstance(GL_LINE_LOOP, 0, vertexData.length, 3, 0);

        //////////////////////////
        // Resolving multisampling
        gl4.glBindFramebuffer(GL_READ_FRAMEBUFFER, framebufferName[Framebuffer.MULTISAMPLE.ordinal()]);
        gl4.glBindFramebuffer(GL_DRAW_FRAMEBUFFER, framebufferName[Framebuffer.COLOR.ordinal()]);
        gl4.glBlitFramebuffer(
                0, 0, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y,
                0, 0, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y,
                GL_COLOR_BUFFER_BIT, GL_NEAREST);
        gl4.glBindFramebuffer(GL_FRAMEBUFFER, 0);

        //////////////////////////////////////
        // Render resolved multisample texture
        gl4.glViewportIndexedf(0, 0, 0, windowSize.x, windowSize.y);
        gl4.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        //glDisable(GL_MULTISAMPLE);
        gl4.glActiveTexture(GL_TEXTURE0);
        //glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, TextureName[texture::MULTISAMPLE]);
        gl4.glBindTexture(GL_TEXTURE_2D, textureName[Texture.COLOR.ordinal()]);
        gl4.glBindVertexArray(vertexArrayName[Pipeline.SPLASH.ordinal()]);
        gl4.glBindProgramPipeline(pipelineName[Pipeline.SPLASH.ordinal()]);

        gl4.glDrawArraysInstancedBaseInstance(GL_TRIANGLES, 0, 6, 1, 0);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteBuffers(Texture.MAX.ordinal(), textureName, 0);
        gl4.glDeleteProgramPipelines(Pipeline.MAX.ordinal(), pipelineName, 0);
        gl4.glDeleteProgram(programName[Pipeline.MULTISAMPLE.ordinal()]);
        gl4.glDeleteProgram(programName[Pipeline.SPLASH.ordinal()]);
        gl4.glDeleteBuffers(Buffer.MAX.ordinal(), bufferName, 0);
        gl4.glDeleteVertexArrays(Pipeline.MAX.ordinal(), vertexArrayName, 0);

        return true;
    }
}
