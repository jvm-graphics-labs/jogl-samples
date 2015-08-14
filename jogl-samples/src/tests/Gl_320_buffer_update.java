/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import com.jogamp.opengl.util.GLBuffers;
import framework.Test;

/**
 *
 * @author gbarbieri
 */
public class Gl_320_buffer_update extends Test {

    public static void main(String[] args) {
        Gl_320_buffer_update gl_320_buffer_update = new Gl_320_buffer_update();
    }
    
    public Gl_320_buffer_update() {
        super("gl-320-buffer-update", 3, 2);
    }
    
    private final String SHADERS_SOURCE = "buffer-update";
    private final String SHADERS_ROOT = "src/data/gl_320";

    private int vertexCount = 6;
    private int positionSize = vertexCount * 2 * GLBuffers.SIZEOF_FLOAT;
    private float[] positionData = new float[]{
        -1.0f, -1.0f,
        +1.0f, -1.0f,
        +1.0f, +1.0f,
        +1.0f, +1.0f,
        -1.0f, +1.0f,
        -1.0f, -1.0f,
    };
    
    private enum Buffer {

        array, copy, material, transform, max
    }
}
