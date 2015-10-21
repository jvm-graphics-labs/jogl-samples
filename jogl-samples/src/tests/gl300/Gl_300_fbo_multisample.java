/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl300;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2ES3.*;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.Semantic;
import framework.Test;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import jgli.Gl;
import jgli.Load;
import jglm.Vec2i;
//import texture.TextureData;
//import texture.TextureIO;
//import texture.spi.DDSImage;

/**
 *
 * @author gbarbieri
 */
public class Gl_300_fbo_multisample extends Test {

    public static void main(String[] args) {
        Gl_300_fbo_multisample gl_300_fbo_multisample = new Gl_300_fbo_multisample();
    }

    public Gl_300_fbo_multisample() {
        super("gl-300-fbo-multisample", 3, 0);
    }

    private final String SHADERS_SOURCE = "image-2d";
    private final String SHADERS_ROOT = "src/data/gl_300";
    private final String TEXTURE_ROOT = "/data";
    private final String TEXTURE_DIFFUSE = "kueken7_rgba8_srgb.dds";
    private Vec2i FRAMEBUFFER_SIZE = new Vec2i(160, 120);
    // With DDS textures, v texture coordinate are reversed, from top to bottom
    private int vertexCount = 6;
    private int vertexSize = vertexCount * 4 * GLBuffers.SIZEOF_FLOAT;
    private float[] vertexData = new float[]{
        -2.0f, -1.5f, 0.0f, 0.0f,
        +2.0f, -1.5f, 1.0f, 0.0f,
        +2.0f, +1.5f, 1.0f, 1.0f,
        +2.0f, +1.5f, 1.0f, 1.0f,
        -2.0f, +1.5f, 0.0f, 1.0f,
        -2.0f, -1.5f, 0.0f, 0.0f
    };
    private int programName, uniformMvp, uniformDiffuse;
    private int[] vertexArrayName, bufferName, textureName, colorRenderbufferName,
            colorTextureName, framebufferRenderName, framebufferResolveName;
    private float[] perspective = new float[16], model = new float[16], mvp = new float[16];

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

        return validated & checkError(gl, "begin");
    }

    private boolean initProgram(GL3 gl3) {

        boolean validated = true;

        if (validated) {

            ShaderCode vertShader = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragShader = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

            ShaderProgram program = new ShaderProgram();
            program.add(vertShader);
            program.add(fragShader);
            program.link(gl3, System.out);

            programName = program.program();

            gl3.glBindAttribLocation(programName, Semantic.Attr.POSITION, "position");
            gl3.glBindAttribLocation(programName, Semantic.Attr.TEXCOORD, "texCoord");
            
            program.link(gl3, System.out);
        }
        if (validated) {

            uniformMvp = gl3.glGetUniformLocation(programName, "mvp");
            uniformDiffuse = gl3.glGetUniformLocation(programName, "diffuse");
        }
        return validated & checkError(gl3, "initProgram");
    }

    private boolean initBuffer(GL3 gl3) {

        bufferName = new int[1];
        gl3.glGenBuffers(1, bufferName, 0);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[0]);
        FloatBuffer floatBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl3.glBufferData(GL_ARRAY_BUFFER, vertexSize, floatBuffer, GL_STATIC_DRAW);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        return checkError(gl3, "initBuffer");
    }

    private boolean initTexture(GL3 gl3) {

        try {
            jgli.Texture texture = Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE);

            textureName = new int[1];
            gl3.glGenTextures(1, textureName, 0);
            gl3.glActiveTexture(GL_TEXTURE0);
            gl3.glBindTexture(GL_TEXTURE_2D, textureName[0]);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, texture.levels() - 1);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

            jgli.Gl.Format glFormat = Gl.instance.translate(texture.format);

            for (int level = 0; level < texture.levels(); level++) {

                gl3.glTexImage2D(GL_TEXTURE_2D, level, glFormat.internal.value,
                        texture.dimensions(level)[0], texture.dimensions(level)[1], 0,
                        glFormat.external.value, glFormat.type.value, texture.data(0, 0, level));
            }
        } catch (IOException ex) {
            Logger.getLogger(Gl_300_fbo_multisample.class.getName()).log(Level.SEVERE, null, ex);
        }
        return checkError(gl3, "initTexture");
    }

    private final boolean useRenderbuffer = true;

    private boolean initFramebuffer(GL3 gl3) {

        colorRenderbufferName = new int[1];
        if (useRenderbuffer) {
            gl3.glGenRenderbuffers(1, colorRenderbufferName, 0);
            gl3.glBindRenderbuffer(GL_RENDERBUFFER, colorRenderbufferName[0]);
            gl3.glRenderbufferStorageMultisample(GL_RENDERBUFFER, 8, GL_RGBA8, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y);
        } else {
            gl3.glGenTextures(1, colorRenderbufferName, 0);
            gl3.glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, colorRenderbufferName[0]);
            gl3.glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, 8, GL_RGBA8,
                    FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y, false);
        }
        // The second parameter is the number of samples.

        framebufferRenderName = new int[1];
        gl3.glGenFramebuffers(1, framebufferRenderName, 0);
        gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferRenderName[0]);
        if (useRenderbuffer) {
            gl3.glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER, colorRenderbufferName[0]);
        } else {
            gl3.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
                    GL_TEXTURE_2D_MULTISAMPLE, colorRenderbufferName[0], 0);
        }
        if (gl3.glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            return false;
        }
        gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);

        colorTextureName = new int[1];
        gl3.glGenTextures(1, colorTextureName, 0);
        gl3.glBindTexture(GL_TEXTURE_2D, colorTextureName[0]);
        gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        gl3.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y,
                0, GL_RGBA, GL_UNSIGNED_BYTE, null);

        framebufferResolveName = new int[1];
        gl3.glGenFramebuffers(1, framebufferResolveName, 0);
        gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferResolveName[0]);
        gl3.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, colorTextureName[0], 0);
        if (gl3.glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            return false;
        }
        gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);

        return checkError(gl3, "initFramebuffer");
    }

    private boolean initVertexArray(GL3 gl3) {

        vertexArrayName = new int[1];
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

        return checkError(gl3, "initVertexArray");
    }

    @Override
    public boolean render(GL gl) {

        GL3 gl3 = (GL3) gl;

        // Clear the framebuffer
        gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        gl3.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 0.5f, 0.0f, 1.0f}, 0);

        gl3.glUseProgram(programName);
        gl3.glUniform1i(uniformDiffuse, 0);

        // Pass 1
        // Render the scene in a multisampled framebuffer
        gl3.glEnable(GL_MULTISAMPLE);
        renderFBO(gl3, framebufferRenderName[0]);
        gl3.glDisable(GL_MULTISAMPLE);

//        saveImage(gl3);
        // Resolved multisampling
        gl3.glBindFramebuffer(GL_READ_FRAMEBUFFER, framebufferRenderName[0]);
        gl3.glBindFramebuffer(GL_DRAW_FRAMEBUFFER, framebufferResolveName[0]);
        gl3.glDrawBuffer(GL_COLOR_ATTACHMENT0);
        gl3.glBlitFramebuffer(
                0, 0, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y,
                0, 0, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y,
                GL_COLOR_BUFFER_BIT, GL_LINEAR);
        gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);

//        saveImage(gl3);
        // Pass 2
        // Render the colorbuffer from the multisampled framebuffer
        gl3.glViewport(0, 0, windowSize.x, windowSize.y);
        renderFB(gl3, colorTextureName[0]);
        return true;
    }

    private void renderFBO(GL3 gl3, int framebuffer) {

        gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);
        gl3.glClearColor(0.0f, 0.5f, 1.0f, 1.0f);
        gl3.glClear(GL_COLOR_BUFFER_BIT);

        FloatUtil.makePerspective(perspective, 0, true, (float) (Math.PI * 0.25f),
                (float) (FRAMEBUFFER_SIZE.x) / FRAMEBUFFER_SIZE.y, 0.1f, 100.0f);
        FloatUtil.makeIdentity(model);
        FloatUtil.multMatrix(perspective, view(), mvp);
        FloatUtil.multMatrix(mvp, model);
        gl3.glUniformMatrix4fv(uniformMvp, 1, false, mvp, 0);

        gl3.glViewport(0, 0, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y);

        gl3.glActiveTexture(GL_TEXTURE0);
        gl3.glBindTexture(GL_TEXTURE_2D, textureName[0]);

        gl3.glBindVertexArray(vertexArrayName[0]);
        gl3.glDrawArrays(GL_TRIANGLES, 0, vertexCount);

        checkError(gl3, "renderFBO");
    }

    private void renderFB(GL3 gl3, int texture2dName) {

        FloatUtil.makePerspective(perspective, 0, true, (float) (Math.PI * 0.25f),
                (float) windowSize.x / windowSize.y, 0.1f, 100.0f);
        FloatUtil.makeIdentity(model);
        FloatUtil.multMatrix(perspective, view(), mvp);
        FloatUtil.multMatrix(mvp, model);
        gl3.glUniformMatrix4fv(uniformMvp, 1, false, mvp, 0);

        gl3.glActiveTexture(GL_TEXTURE0);
        gl3.glBindTexture(GL_TEXTURE_2D, texture2dName);

        gl3.glBindVertexArray(vertexArrayName[0]);
        gl3.glDrawArrays(GL_TRIANGLES, 0, vertexCount);

        checkError(gl3, "renderFB");
    }

    private void saveImage(GL3 gl3) {
        try {
            BufferedImage screenshot = new BufferedImage(FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y,
                    BufferedImage.TYPE_INT_ARGB);
            Graphics graphics = screenshot.getGraphics();

            ByteBuffer buffer;
            if (useRenderbuffer) {
                buffer = GLBuffers.newDirectByteBuffer(FRAMEBUFFER_SIZE.x * FRAMEBUFFER_SIZE.y * 4);
            } else {
                buffer = GLBuffers.newDirectByteBuffer(256 * 256 * 4);
            }

            gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferResolveName[0]);
            gl3.glReadBuffer(GL3.GL_COLOR_ATTACHMENT0);

            gl3.glPixelStorei(GL4.GL_UNPACK_ALIGNMENT, 1);
            checkError(gl3, "pre");
            if (useRenderbuffer) {
                gl3.glReadPixels(0, 0, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y, GL3.GL_RGBA, GL3.GL_BYTE, buffer);
            } else {
                gl3.glGetTexImage(GL_TEXTURE_2D, 0, GL_RGBA, GL_BYTE, buffer);
            }
            checkError(gl3, "post");

            for (int h = 0; h < FRAMEBUFFER_SIZE.x; h++) {
                for (int w = 0; w < FRAMEBUFFER_SIZE.y; w++) {
                    // The color are the three consecutive bytes, it's like referencing
                    // to the next consecutive array elements, so we got red, green, blue..
                    // red, green, blue, and so on..
                    graphics.setColor(new Color(buffer.get() * 2, buffer.get() * 2, buffer.get() * 2));
                    buffer.get();
                    graphics.drawRect(w, FRAMEBUFFER_SIZE.y - h, 1, 1); // height - h is for flipping the image
                }
            }

            File outputfile = new File("D:\\Downloads\\texture.png");
            ImageIO.write(screenshot, "jpg", outputfile);
        } catch (IOException ex) {
            //Logger.getLogger(EC_DepthPeeling.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
