/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import com.jogamp.common.util.IOUtil;
import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_LINEAR;
import static com.jogamp.opengl.GL.GL_LINEAR_MIPMAP_LINEAR;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_BASE_LEVEL;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_MAX_LEVEL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import com.jogamp.opengl.util.texture.spi.DDSImage.ImageInfo;
import framework.Semantic;
import framework.Test;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.FloatBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import jglm.Vec2i;
import texture.TextureData;
import texture.TextureIO;
import texture.spi.DDSImage;

/**
 *
 * @author gbarbieri
 */
public class Gl_300_fbo_multisample extends Test {

    public static void main(String[] args) {
        Gl_300_fbo_multisample gl_300_fbo_multisample = new Gl_300_fbo_multisample();
    }

    private final String VERT_SHADER = "image-2d";
    private final String FRAG_SHADER = "image-2d";
    private final String TEXTURE_DIFFUSE = "kueken7_rgba8_srgb.dds";
    private Vec2i FRAMEBUFFER_SIZE = new Vec2i(160, 120);
    // With DDS textures, v texture coordinate are reversed, from top to bottom
    private int vertexCount = 6;
    private int vertexSize = vertexCount * 4 * GLBuffers.SIZEOF_FLOAT;
    private float[] vertexData = new float[]{
        -2.0f, -1.5f, 0.0f, 0.0f,
        2.0f, -1.5f, 1.0f, 0.0f,
        2.0f, 1.5f, 1.0f, 1.0f,
        2.0f, 1.5f, 1.0f, 1.0f,
        -2.0f, 1.5f, 0.0f, 1.0f,
        -2.0f, -1.5f, 0.0f, 0.0f
    };
    private int programName, uniformMvp, uniformDiffuse;
    private int[] vertexArrayName, bufferName, textureName, colorRenderbufferName, colorTextureName,
            framebufferRenderName, framebufferResolveName;

    public Gl_300_fbo_multisample() {

        super("Gl_300_fbo_multisample", 3, 0);
        programName = 0;
        vertexArrayName = new int[]{0};
        bufferName = new int[]{0};
        textureName = new int[]{0};
        colorRenderbufferName = new int[]{0};
        colorTextureName = new int[]{0};
        framebufferRenderName = new int[]{0};
        framebufferResolveName = new int[]{0};
        uniformMvp = -1;
        uniformDiffuse = -1;
    }

    @Override
    protected boolean begin(GL gl) {

        GL3 gl3 = (GL3) gl;

        boolean validated = true;

        if (validated) {
            initProgram(gl3);
        }
        if (validated) {
            initTexture(gl3);
        }

        return validated & checkError(gl, "begin");
    }

    private boolean initProgram(GL3 gl3) {

        boolean validated = true;

        if (validated) {

            ShaderCode vertShader = ShaderCode.create(gl3, GL_VERTEX_SHADER, this.getClass(),
                    getDataDirectory() + "gl_300", getDataDirectory() + "gl_300/bin", VERT_SHADER, true);
            ShaderCode fragShader = ShaderCode.create(gl3, GL_FRAGMENT_SHADER, this.getClass(),
                    getDataDirectory() + "gl_300", getDataDirectory() + "gl_300/bin", FRAG_SHADER, true);

            ShaderProgram program = new ShaderProgram();
            program.add(vertShader);
            program.add(fragShader);
            program.link(gl3, System.out);

            programName = program.program();

            gl3.glBindAttribLocation(programName, Semantic.Attr.position, "position");
            gl3.glBindAttribLocation(programName, Semantic.Attr.texCoord, "texCoord");
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
            URLConnection conn = IOUtil.getResource(this.getClass(), getDataDirectory() + TEXTURE_DIFFUSE);

            DDSImage ddsImage = DDSImage.read(conn.getURL().getFile());

            gl3.glGenTextures(1, textureName, 0);
            gl3.glActiveTexture(GL_TEXTURE0);
            gl3.glBindTexture(GL_TEXTURE_2D, textureName[0]);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, ddsImage.getNumMipMaps() - 1);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

            System.out.println("" + ddsImage.isCompressed() + " " + ddsImage.getNumMipMaps());

            TextureData textureData = TextureIO.newTextureData(gl3.getGLProfile(),
                    conn.getURL(), ddsImage.getNumMipMaps() > 1, TextureIO.DDS);

            for (int level = 0; level < ddsImage.getNumMipMaps(); level++) {

                ImageInfo imageInfo = ddsImage.getMipMap(level);
//                System.out.println("imageInfo.toString() " + ddsImage.getCompressionFormat());

//                TextureData textureData = new TextureData(gl3.getGLProfile(),
//                        ddsImage.getCompressionFormat(), ddsImage.getWidth(), ddsImage.getHeight(),
//                        0, ddsImage.getPixelFormat(), GL_UNSIGNED_BYTE, false, true, false,
//                        imageInfo.getData(), null);
//                System.out.println("data.toString() " + textureData.toString());
                gl3.glTexImage2D(GL_TEXTURE_2D, level, textureData.getInternalFormat(),
                        imageInfo.getWidth(), imageInfo.getHeight(), 0, textureData.getPixelFormat(), 
                        textureData.getPixelType(), textureData.getBuffer());
            }
        } catch (IOException ex) {
            Logger.getLogger(Gl_300_fbo_multisample.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

}
