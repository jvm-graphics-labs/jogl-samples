/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_410;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import com.jogamp.opengl.GL4;
import framework.Caps;
import framework.Profile;
import framework.Test;

/**
 *
 * @author GBarbieri
 */
public class Gl_410_caps extends Test {

    public static void main(String[] args) {
        Gl_410_caps gl_410_caps = new Gl_410_caps();
    }

    public Gl_410_caps() {
        super("gl-410-caps", Profile.CORE, 4, 1);
    }

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        return checkCaps(gl4);
    }

    private boolean checkCaps(GL4 gl4) {

        Caps caps = new Caps(gl4, Profile.CORE);

        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glViewport(0, 0, windowSize.x, windowSize.y);
        gl4.glClearBufferfv(GL_COLOR, 0, clearColor.put(0, 1).put(1, 0.5f).put(2, 0).put(3, 1));

        return true;
    }

    @Override
    protected boolean end(GL gl) {
        return true;
    }
}
