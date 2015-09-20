/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl320;

import framework.Test;

/**
 *
 * @author GBarbieri
 */
public class Gl_320_draw_multiple extends Test {

    public static void main(String[] args) {
        Gl_320_draw_multiple gl_320_draw_multiple = new Gl_320_draw_multiple();
    }

    public Gl_320_draw_multiple() {
        super("gl-320-draw-multiple", 3, 2);
    }

    private final String SHADERS_SOURCE = "draw-multiple";
    private final String SHADERS_ROOT = "src/data/gl_320";

    private int ElementCount = 6;
    private int ElementSize = ElementCount * Integer.BYTES;
    private short[] ElementData = new short[]{
        0, 1, 2,
        0, 2, 3
    };

    private int VertexCount = 8;
    private int PositionSize = VertexCount * 3 * Float.BYTES;
    private float[] PositionData = new float[]{
        -1.0f, -1.0f, 0.5f,
        +1.0f, -1.0f, 0.5f,
        +1.0f, 1.0f, 0.5f,
        -1.0f, 1.0f, 0.5f,
        -0.5f, -1.0f, -0.5f,
        +0.5f, -1.0f, -0.5f,
        +1.5f, 1.0f, -0.5f,
        -1.5f, 1.0f, -0.5f
    };

    private int[] Count = new int[]{ElementCount, ElementCount};
    private int[] BaseVertex = new int[]{0, 4};
    
    private int Count[2] = {ElementCount, ElementCount};
	GLint const BaseVertex[2] = {0, 4};
	
	namespace buffer
	{
		enum type
		{
			VERTEX,
			ELEMENT,
			TRANSFORM,
			MAX
		};
}
