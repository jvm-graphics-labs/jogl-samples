/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_430;

import framework.Profile;
import framework.Test;
import glm.vec._2.Vec2;
import glm.vec._2.i.Vec2i;

/**
 *
 * @author GBarbieri
 */
public class Gl_430_program_compute_variable_group_size extends Test{
    
    public static void main(String[] args) {
        Gl_430_program_compute_variable_group_size gl_430_program_compute_variable_group_size = 
                new Gl_430_program_compute_variable_group_size();
    }

    public Gl_430_program_compute_variable_group_size() {
        super("gl-430-program-compute-variable-group-size", Profile.CORE, 4, 3);
    }

    private final String SHADERS_SOURCE = "program-compute-variable-group-size";
    private final String SHADERS_ROOT = "src/data/gl_430";
    private final String TEXTURE_DIFFUSE = "kueken7_rgba8_srgb.dds";
    
    
}
