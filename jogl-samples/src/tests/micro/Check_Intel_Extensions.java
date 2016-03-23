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
public class Check_Intel_Extensions extends Test{
    
    public static void main(String[] args) {
        Check_Intel_Extensions check_Intel_Extensions = new Check_Intel_Extensions();
    }

    public Check_Intel_Extensions() {
        super("check-intel-extensions", Profile.COMPATIBILITY, 3, 3);
    }

    @Override
    protected boolean begin(GL gl) {

        GL3 gl3 = (GL3) gl;

        boolean validated = true;

        String[] extensions = {
            "GL_INTEL_performance_query",
            "GL_INTEL_fragment_shader_ordering"
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
