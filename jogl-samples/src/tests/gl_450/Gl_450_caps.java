/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_450;

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
public class Gl_450_caps extends Test {

    public static void main(String[] args) {
        Gl_450_caps gl_450_caps = new Gl_450_caps();
    }

    public Gl_450_caps() {
        super("gl-450-caps", Profile.CORE, 4, 5);
    }

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        return checkCaps(gl4);
    }

    private boolean checkCaps(GL4 gl4) {

        Caps caps = new Caps(gl4, Profile.CORE);

        return caps.limits.MAX_SHADER_STORAGE_BLOCK_SIZE >= (2 << 27);
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
