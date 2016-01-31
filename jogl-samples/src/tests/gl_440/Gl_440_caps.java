/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_440;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import com.jogamp.opengl.GL4;
import framework.Caps;
import framework.Profile;
import framework.Test;

/**
 *
 * @author elect
 */
public class Gl_440_caps extends Test {

    public static void main(String[] args) {
        Gl_440_caps gl_440_caps = new Gl_440_caps();
    }

    public Gl_440_caps() {
        super("gl-440-buffer-type", Profile.CORE, 4, 4);
    }

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        return checkCaps(gl4);
    }

    private boolean checkCaps(GL4 gl4) {

        Caps caps = new Caps(gl4, Profile.CORE);

        //Caps.Version.SHADING_LANGUAGE_VERSION
        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glViewport(0, 0, windowSize.x, windowSize.y);
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 0.5f, 0.0f, 1.0f}, 0);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        return true;
    }
}
