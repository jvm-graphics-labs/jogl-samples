/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import framework.Test;

/**
 *
 * @author gbarbieri
 */
public class Gl_320_caps extends Test{
    
    public static void main(String[] args) {
        Gl_320_caps gl_320_caps = new Gl_320_caps();
    }

    public Gl_320_caps() {
        super("gl-320-caps", 3, 2);
    }
    
    @Override
    protected boolean begin(GL gl) {
        
        GL3 gl3 = (GL3)gl;
        
        
    }
}