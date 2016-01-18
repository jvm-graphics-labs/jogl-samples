/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_430;

import framework.Profile;
import framework.Test;

/**
 *
 * @author GBarbieri
 */
public class Gl_430_draw_without_vertex_attrib extends Test {

    public static void main(String[] args) {
        Gl_430_draw_without_vertex_attrib gl_430_draw_without_vertex_attrib = new Gl_430_draw_without_vertex_attrib();
    }

    public Gl_430_draw_without_vertex_attrib() {
        super("gl-430-draw-without-vertex-attrib", Profile.CORE, 4, 3);
    }

    private final String SHADERS_SOURCE = "draw-vertex-attrib-binding";
    private final String SHADERS_ROOT = "src/data/gl_430";
    private final String TEXTURE_DIFFUSE = "kueken7_rgba8_srgb.dds";

    private int vertexCount = 6;
    private int vertexSize = vertexCount * 2 * 2 * Float.BYTES;
    private float[] vertexData = {
        -1.0f, -1.0f, 0.0f, 1.0f,
        +1.0f, -1.0f, 1.0f, 1.0f,
        +1.0f, +1.0f, 1.0f, 0.0f,
        +1.0f, +1.0f, 1.0f, 0.0f,
        -1.0f, +1.0f, 0.0f, 0.0f,
        -1.0f, -1.0f, 0.0f, 1.0f};

    private enum Buffer {
        TRANSFORM,
        VERTEX,
        MAX
    }
    
    private int []pipelineName={0},vertexArrayName={0},textureName={0};
    private int programName;
}
