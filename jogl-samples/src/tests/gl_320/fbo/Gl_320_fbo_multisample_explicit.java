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
import framework.BufferUtils;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import jgli.Texture2d;
import jglm.Vec2;
import jglm.Vec2i;

/**
 *
 * @author GBarbieri
 */
public class Gl_320_fbo_multisample_explicit extends Test {

    public static void main(String[] args) {
        Gl_320_fbo_multisample_explicit gl_320_fbo_multisample_explicit = new Gl_320_fbo_multisample_explicit();
    }

    public Gl_320_fbo_multisample_explicit() {
        super("Gl-320-fbo-multisample-explicit", Profile.CORE, 3, 2, 
                new Vec2((float) Math.PI * 0.2f, (float) Math.PI * 0.2f));
    }

    private final String TEXTURE_DIFFUSE = "kueken7_rgba8_srgb.dds";
    private final String SHADERS_ROOT = "src/data/gl_320/fbo";
    private final String VERT_SHADER_SOURCE = "fbo-multisample-explicit";
    private final String[] FRAG_SHADER_SOURCE = new String[]{
        "fbo-multisample-explicit-texture",
        "fbo-multisample-explicit-box",
        "fbo-multisample-explicit-near"};

    private Vec2i framebufferSize = new Vec2i(160, 120);
    private int vertexCount = 6;
    private int vertexSize = vertexCount * 2 * 2 * Float.BYTES;
    private float[] vertexData = new float[]{
        -1.0f, -1.0f, 0.0f, 1.0f,
        +1.0f, -1.0f, 1.0f, 1.0f,
        +1.0f, +1.0f, 1.0f, 0.0f,
        +1.0f, +1.0f, 1.0f, 0.0f,
        -1.0f, +1.0f, 0.0f, 0.0f,
        -1.0f, -1.0f, 0.0f, 1.0f
    };

    private enum Program {

        THROUGH,
        RESOLVE_BOX,
        RESOLVE_NEAR,
        MAX
    }

    private enum Texture {

        COLORBUFFER,
        MULTISAMPLE_DEPTHBUFFER,
        MULTISAMPLE_COLORBUFFER,
        DIFFUSE,
        MAX
    }

    private enum Shader {

        VERT,
        FRAG_TEXTURE,
        FRAG_BOX,
        FRAG_NEAR,
        MAX
    }

    private int[] vertexArrayName = new int[1], bufferName = new int[1], framebufferRenderName = new int[1],
            framebufferResolveName = new int[1], programName = new int[Program.MAX.ordinal()],
            textureName = new int[Texture.MAX.ordinal()], uniformMvp = new int[Program.MAX.ordinal()],
            uniformDiffuse = new int[Program.MAX.ordinal()];
    private float[] perspective = new float[16], model = new float[16], mvp = new float[16], viewflip = new float[16],
            view = new float[16];

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

        ShaderCode[] shaderCodes = new ShaderCode[Shader.MAX.ordinal()];

        shaderCodes[Shader.VERT.ordinal()] = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                this.getClass(), SHADERS_ROOT, null, VERT_SHADER_SOURCE, "vert", null, true);

        for (int i = 0; i < Program.MAX.ordinal(); i++) {

            shaderCodes[Shader.FRAG_TEXTURE.ordinal() + i] = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, FRAG_SHADER_SOURCE[i], "frag", null, true);

            ShaderProgram program = new ShaderProgram();
            program.add(shaderCodes[Shader.VERT.ordinal()]);
            program.add(shaderCodes[Shader.FRAG_TEXTURE.ordinal() + i]);

            program.init(gl3);

            programName[i] = program.program();

            gl3.glBindAttribLocation(programName[i], Semantic.Attr.POSITION, "position");
            gl3.glBindAttribLocation(programName[i], Semantic.Attr.TEXCOORD, "texCoord");
            gl3.glBindFragDataLocation(programName[i], Semantic.Frag.COLOR, "color");

            program.link(gl3, System.out);
        }

        if (validated) {

            for (int i = 0; i < Program.MAX.ordinal(); i++) {
                uniformMvp[i] = gl3.glGetUniformLocation(programName[i], "mvp");
                uniformDiffuse[i] = gl3.glGetUniformLocation(programName[i], "diffuse");
            }
        }
        return validated & checkError(gl3, "initProgram");
    }

    private boolean initBuffer(GL3 gl3) {

        gl3.glGenBuffers(1, bufferName, 0);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[0]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl3.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(vertexBuffer);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        return checkError(gl3, "initBuffer");
    }

    private boolean initTexture(GL3 gl3) {

        try {
            jgli.Texture2d texture = new Texture2d(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE));

            gl3.glGenTextures(Texture.MAX.ordinal(), textureName, 0);

            gl3.glActiveTexture(GL_TEXTURE0);
            gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.DIFFUSE.ordinal()]);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

            gl3.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

            jgli.Gl.Format format = jgli.Gl.translate(texture.format());
            for (int level = 0; level < texture.levels(); level++) {
                gl3.glTexImage2D(GL_TEXTURE_2D, level,
                        format.internal.value,
                        texture.dimensions(level)[0], texture.dimensions(level)[1],
                        0,
                        format.external.value, format.type.value,
                        texture.data(level));
            }

            gl3.glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, textureName[Texture.MULTISAMPLE_COLORBUFFER.ordinal()]);
            gl3.glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, 4, GL_RGBA8,
                    framebufferSize.x, framebufferSize.y, true);
            gl3.glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, textureName[Texture.MULTISAMPLE_DEPTHBUFFER.ordinal()]);
            gl3.glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, 4, GL_DEPTH_COMPONENT24,
                    framebufferSize.x, framebufferSize.y, true);
            gl3.glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, 0);

            gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.COLORBUFFER.ordinal()]);
            gl3.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, framebufferSize.x, framebufferSize.y, 0,
                    GL_RGBA, GL_UNSIGNED_BYTE, null);
            gl3.glBindTexture(GL_TEXTURE_2D, 0);

        } catch (IOException ex) {
            Logger.getLogger(Gl_320_fbo_multisample_explicit.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    private boolean initFramebuffer(GL3 gl3) {

        gl3.glGenFramebuffers(1, framebufferRenderName, 0);
        gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferRenderName[0]);
        gl3.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
                textureName[Texture.MULTISAMPLE_COLORBUFFER.ordinal()], 0);
        gl3.glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT,
                textureName[Texture.MULTISAMPLE_DEPTHBUFFER.ordinal()], 0);

        if (!isFramebufferComplete(gl3, framebufferRenderName[0])) {
            return false;
        }
        gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);

        gl3.glGenFramebuffers(1, framebufferResolveName, 0);
        gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferResolveName[0]);
        gl3.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, textureName[Texture.COLORBUFFER.ordinal()], 0);

        if (!isFramebufferComplete(gl3, framebufferResolveName[0])) {
            return false;
        }
        gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);

        return true;
    }

    private boolean initVertexArray(GL3 gl3) {

        gl3.glGenVertexArrays(1, vertexArrayName, 0);
        gl3.glBindVertexArray(vertexArrayName[0]);
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[0]);
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 2 * 2 * Float.BYTES, 0);
            gl3.glVertexAttribPointer(Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, 2 * 2 * Float.BYTES, 2 * Float.BYTES);
            gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl3.glEnableVertexAttribArray(Semantic.Attr.TEXCOORD);
        }
        gl3.glBindVertexArray(0);

        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL3 gl3 = (GL3) gl;

        // Clear the framebuffer
        gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        gl3.glClearBufferfv(GL_COLOR, 0, new float[]{0.0f, 0.5f, 1.0f, 1.0f}, 0);

        // Pass 1
        // Render the scene in a multisampled framebuffer
        gl3.glEnable(GL_MULTISAMPLE);
        renderFBO(gl3, framebufferRenderName[0]);
        gl3.glDisable(GL_MULTISAMPLE);

        // Pass 2
        // Resolved and render the colorbuffer from the multisampled framebuffer
        resolveMultisampling(gl3);

        return true;
    }

    private void renderFBO(GL3 gl3, int framebuffer) {

        FloatUtil.makePerspective(perspective, 0, true, (float) Math.PI * 0.25f,
                (float) framebufferSize.x / framebufferSize.y, 0.1f, 100.0f);
        FloatUtil.makeScale(model, true, 1.0f, 1.0f, 1.0f);
        FloatUtil.multMatrix(perspective, view(), mvp);
        FloatUtil.multMatrix(mvp, model);

        gl3.glEnable(GL_DEPTH_TEST);

        gl3.glUseProgram(programName[Program.THROUGH.ordinal()]);
        gl3.glUniform1i(uniformDiffuse[Program.THROUGH.ordinal()], 0);
        gl3.glUniformMatrix4fv(uniformMvp[Program.THROUGH.ordinal()], 1, false, mvp, 0);

        gl3.glViewport(0, 0, framebufferSize.x, framebufferSize.y);

        gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);
        float[] depth = new float[]{1.0f};
        gl3.glClearBufferfv(GL_DEPTH, 0, depth, 0);
        gl3.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 0.5f, 0.0f, 1.0f}, 0);

        gl3.glActiveTexture(GL_TEXTURE0);
        gl3.glBindTexture(GL_TEXTURE_2D, textureName[Texture.DIFFUSE.ordinal()]);
        gl3.glBindVertexArray(vertexArrayName[0]);

        gl3.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 5);

        gl3.glDisable(GL_DEPTH_TEST);
    }

    private void resolveMultisampling(GL3 gl3) {

        FloatUtil.makeOrtho(perspective, 0, true, -1.0f, 1.0f, -1.0f, 1.0f, 0.0f, 100.0f);
        FloatUtil.makeScale(viewflip, true, 1.0f, -1.0f, 1.0f);
        FloatUtil.makeTranslation(view, true, 0.0f, 0.0f, -cameraDistance() * 1.0f);
        FloatUtil.multMatrix(view, viewflip);
        FloatUtil.makeIdentity(model);
        FloatUtil.multMatrix(perspective, view, mvp);
        FloatUtil.multMatrix(mvp, model);

        gl3.glViewport(0, 0, windowSize.x, windowSize.y);
        gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);

        gl3.glActiveTexture(GL_TEXTURE0);
        gl3.glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, textureName[Texture.MULTISAMPLE_COLORBUFFER.ordinal()]);

        gl3.glBindVertexArray(vertexArrayName[0]);

        gl3.glEnable(GL_SCISSOR_TEST);

        // Box
        {
            gl3.glScissor(1, 1, windowSize.x / 2 - 2, windowSize.y - 2);

            gl3.glUseProgram(programName[Program.RESOLVE_BOX.ordinal()]);
            gl3.glUniform1i(uniformDiffuse[Program.RESOLVE_BOX.ordinal()], 0);
            gl3.glUniformMatrix4fv(uniformMvp[Program.RESOLVE_BOX.ordinal()], 1, false, mvp, 0);

            gl3.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 5);
        }

        // Near
        {
            gl3.glScissor(windowSize.x / 2 + 1, 1, windowSize.x / 2 - 2, windowSize.y - 2);

            gl3.glUseProgram(programName[Program.RESOLVE_NEAR.ordinal()]);
            gl3.glUniform1i(uniformDiffuse[Program.RESOLVE_NEAR.ordinal()], 0);
            gl3.glUniformMatrix4fv(uniformMvp[Program.RESOLVE_NEAR.ordinal()], 1, false, mvp, 0);

            gl3.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 5);
        }

        gl3.glDisable(GL_SCISSOR_TEST);
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;

        for (int i = 0; i < Program.MAX.ordinal(); ++i) {
            gl3.glDeleteProgram(programName[i]);
        }

        gl3.glDeleteBuffers(1, bufferName, 0);
        gl3.glDeleteTextures(Texture.MAX.ordinal(), textureName, 0);
        gl3.glDeleteFramebuffers(1, framebufferRenderName, 0);
        gl3.glDeleteFramebuffers(1, framebufferResolveName, 0);
        gl3.glDeleteVertexArrays(1, vertexArrayName, 0);

        return checkError(gl3, "end");
    }
}
