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
public class Check_Amd_Extensions extends Test {

    public static void main(String[] args) {
        Check_Amd_Extensions check_Amd_Extension = new Check_Amd_Extensions();
    }

    public Check_Amd_Extensions() {
        super("check-amd-extensions", Profile.COMPATIBILITY, 3, 3);
    }

    @Override
    protected boolean begin(GL gl) {

        GL3 gl3 = (GL3) gl;

        boolean validated = true;

        String[] extensions = {
            "GL_AMD_performance_monitor",
            "GL_AMD_blend_minmax_factor",
            "GL_AMD_pinned_memory",
            "GL_AMD_vertex_shader_viewport_index",
            "GL_AMD_vertex_shader_layer",
            "GL_AMD_sample_positions",
            "GL_AMD_depth_clamp_separate",
            "GL_AMD_sparse_texture"
        };
        
        for (String extension : extensions) {
            System.out.println(extension + ": "+gl3.isExtensionAvailable(extension));
        }

        return validated;
    }
    
    @Override
    protected boolean render(GL gl) {
        animator.stop();
        return true;
    }
}
