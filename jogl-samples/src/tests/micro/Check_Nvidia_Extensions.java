/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.micro;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import framework.Profile;
import framework.Test;

/**
 *
 * @author GBarbieri
 */
public class Check_Nvidia_Extensions extends Test {

    public static void main(String[] args) {
        Check_Nvidia_Extensions check_Nvidia_Extensions = new Check_Nvidia_Extensions();
    }

    public Check_Nvidia_Extensions() {
        super("check-nvidia-extension", Profile.CORE, 4, 5);
    }

    @Override
    protected boolean begin(GL gl) {

        GL3 gl3 = (GL3) gl;

        boolean validated = true;

        String[] extensions = {
            "GL_NV_explicit_multisample",
            "GL_NV_gpu_shader5",
            "GL_NV_conservative_raster",
            "GL_NV_viewport_array2",
            "GL_NV_fill_rectangle",
            "GL_NV_shader_buffer_load",
            "GL_NV_vertex_buffer_unified_memory",
            "GL_NV_geometry_shader_passthrough",
            "GL_NV_sample_locations",
            "GL_NV_internalformat_sample_query",
            "GL_NV_fragment_shader_interlock",
            "GL_NV_shader_thread_group",
            "GL_NV_bindless_texture"
        };

        for (String extension : extensions) {
            System.out.println(extension + ": " + gl3.isExtensionAvailable(extension));
        }

        return validated;
    }
    
    @Override
    protected boolean render(GL gl) {
        animator.stop();
        return true;
    }
}
