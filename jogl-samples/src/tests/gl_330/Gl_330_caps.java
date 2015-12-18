/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_330;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import com.jogamp.opengl.GL3;
import framework.Caps;
import framework.Profile;
import framework.Test;

/**
 *
 * @author GBarbieri
 */
public class Gl_330_caps extends Test {

    public static void main(String[] args) {
        Gl_330_caps gl_330_caps = new Gl_330_caps();
    }

    public Gl_330_caps() {
        super("gl-330-caps", Profile.CORE, 3, 3);
    }

    @Override
    protected boolean begin(GL gl) {

        GL3 gl3 = (GL3) gl;

        return checkCaps(gl3);
    }

    private boolean checkCaps(GL3 gl3) {

        Caps caps = new Caps(gl3, Profile.CORE);

        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL3 gl3 = (GL3) gl;

        gl3.glViewport(0, 0, windowSize.x, windowSize.y);
        gl3.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 0.5f, 0.0f, 1.0f}, 0);

        return true;
    }

    @Override
    protected boolean end(GL gl) {
        return true;
    }
}
