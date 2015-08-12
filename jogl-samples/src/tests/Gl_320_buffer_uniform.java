/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import com.jogamp.opengl.math.VectorUtil;
import com.jogamp.opengl.util.GLBuffers;

/**
 *
 * @author gbarbieri
 */
public class Gl_320_buffer_uniform {

    public static void main(String[] args) {
        Gl_320_buffer_uniform gl_320_buffer_uniform = new Gl_320_buffer_uniform();
    }

    private final String SHADERS_SOURCE = "buffer-uniform-shared";
    private final String SHADERS_ROOT = "src/data/gl_320";

    public class Vertex_v3fn3fc4f {

        public float[] position;
        public float[] texCoord;
        public float[] color;
        public static final int sizeOf = (3 + 3 + 4) * GLBuffers.SIZEOF_FLOAT;

        public Vertex_v3fn3fc4f(float[] position, float[] texCoord, float[] color) {
            this.position = position;
            this.texCoord = texCoord;
            this.color = color;
        }
    }

    private int vertexCount = 4;
    private int vertexSize = vertexCount * Vertex_v3fn3fc4f.sizeOf;
    private Vertex_v3fn3fc4f[] vertexData = new Vertex_v3fn3fc4f[]{
        new Vertex_v3fn3fc4f(new float[]{-1.0f, -1.0f, 0.0f}, new float[]{0.0f, 0.0f, 1.0f}, new float[]{1.0f, 0.0f, 0.0f, 1.0f}),
        new Vertex_v3fn3fc4f(new float[]{+1.0f, -1.0f, 0.0f}, new float[]{0.0f, 0.0f, 1.0f}, new float[]{0.0f, 1.0f, 0.0f, 1.0f}),
        new Vertex_v3fn3fc4f(new float[]{+1.0f, +1.0f, 0.0f}, new float[]{0.0f, 0.0f, 1.0f}, new float[]{0.0f, 0.0f, 1.0f, 1.0f}),
        new Vertex_v3fn3fc4f(new float[]{-1.0f, +1.0f, 0.0f}, new float[]{0.0f, 0.0f, 1.0f}, new float[]{1.0f, 1.0f, 1.0f, 1.0f})
    };
    private int elementCount = 6;
    private int elementSize = elementCount * GLBuffers.SIZEOF_SHORT;
    private short[] elementData = new short[]{
        0, 1, 2,
        2, 3, 0
    };
}
