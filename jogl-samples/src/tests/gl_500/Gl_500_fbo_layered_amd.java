/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_500;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import dev.Vec2i;
import framework.Profile;
import framework.Test;

/**
 *
 * @author GBarbieri
 */
public class Gl_500_fbo_layered_amd extends Test {

    public static void main(String[] args) {
        Gl_500_fbo_layered_amd gl_500_fbo_layered_amd = new Gl_500_fbo_layered_amd();
    }

    public Gl_500_fbo_layered_amd() {
        super("gl-500-fbo-layered-amd", Profile.CORE, 4, 5);
    }

//    private final String SHADERS_SOURCE = "direct-state-access";
//    private final String SHADERS_ROOT = "src/data/gl_450";
//    private final String TEXTURE_DIFFUSE = "kueken7_rgba8_srgb.dds";
//    private final Vec2i FRAMEBUFFER_SIZE = new Vec2i(160, 160);
    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;

        validated = validated && checkExtension(gl4, "GL_AMD_vertex_shader_viewport_index");
        validated = validated && checkExtension(gl4, "GL_AMD_vertex_shader_layer");

//		if(validated)
//			validated = initProgram();
//		if(validated)
//			validated = initBuffer();
//		if(validated)
//			validated = initVertexArray();
//		if(validated)
//			validated = initTexture();
//		if(validated)
//			validated = initFramebuffer();
//		if(validated)
//			validated = initSampler();
        return validated;
    }
}
