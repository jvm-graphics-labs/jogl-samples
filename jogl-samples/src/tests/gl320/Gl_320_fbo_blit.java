/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl320;

import framework.Test;

/**
 *
 * @author elect
 */
public class Gl_320_fbo_blit extends Test {

    public static void main(String[] args) {
        Gl_320_fbo_blit gl_320_fbo_blit = new Gl_320_fbo_blit();
    }

    public Gl_320_fbo_blit() {
        super("gl-320-fbo-blit", 3, 2);
    }

    private final String SHADERS_SOURCE = "fbo-blit";
    private final String SHADERS_ROOT = "src/data/gl_320";
    private final String TEXTURE_DIFFUSE = "kueken7_rgba8_srgb.dds";

    private class Vertex {

        public float[] position;
        public float[] texCoord;
        public static final int sizeOf = 2 * 2 * Float.BYTES;

        public Vertex(float[] position, float[] texCoord) {
            this.position = position;
            this.texCoord = texCoord;
        }
    }

    // With DDS textures, v texture coordinate are reversed, from top to bottom
    private int vertexCount = 6;
    private int vertexSize = vertexCount * Vertex.sizeOf;
    private Vertex[] vertexData = {
        new Vertex(new float[]{-1.5f, -1.5f}, new float[]{0.0f, 0.0f}),
        new Vertex(new float[]{+1.5f, -1.5f}, new float[]{1.0f, 0.0f}),
        new Vertex(new float[]{+1.5f, +1.5f}, new float[]{1.0f, 1.0f}),
        new Vertex(new float[]{+1.5f, +1.5f}, new float[]{1.0f, 1.0f}),
        new Vertex(new float[]{-1.5f, +1.5f}, new float[]{0.0f, 1.0f}),
        new Vertex(new float[]{-1.5f, -1.5f}, new float[]{0.0f, 0.0f})};

    private enum Framebuffer {

        RENDER,
        RESOLVE,
        MAX
    }

    private enum Texture {

        DIFFUSE,
        COLORBUFFER,
        MAX
    }
    
    private int[] textureName = new int[Texture.MAX.ordinal()], 
            framebufferName = new int[Framebuffer.MAX.ordinal()], vertexArrayName = new int[1],
            bufferName = new int[1], colorRenderbufferName = new int[1];
    private int programName, uniformMvp, uniformDiffuse;
    
    
}
