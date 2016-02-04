/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.micro;

import framework.Profile;
import framework.Test;
import tests.gl_500.Gl_500_texture_sparse_arb;

/**
 *
 * @author GBarbieri
 */
public class Test_small_primitive extends Test{
    
    public static void main(String[] args) {
        Test_small_primitive test_small_primitive = new Test_small_primitive();
    }

    public Test_small_primitive() {
        super("test-small-primitive", Profile.CORE, 4, 2);
    }

    private final String SHADERS_SOURCE = "texture-sparse";
    private final String SHADERS_ROOT = "src/data/gl_500";
}
