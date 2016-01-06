/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_400;

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
public class Gl_400_caps extends Test {

    public static void main(String[] args) {
        Gl_400_caps gl_400_caps = new Gl_400_caps();
    }

    public Gl_400_caps() {
        super("gl-400-caps", Profile.CORE, 4, 0);
    }

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        Caps caps = new Caps(gl4, Profile.CORE);

        boolean validated = true;

        validated = validated && caps.limits.MAX_PATCH_VERTICES >= 32;
        validated = validated && caps.limits.MAX_TESS_GEN_LEVEL >= 64;

        validated = validated && caps.limits.MAX_TEXTURE_BUFFER_SIZE >= 65536;
        validated = validated && caps.values.MAX_TEXTURE_SIZE >= 16384;
        validated = validated && caps.values.MAX_3D_TEXTURE_SIZE >= 2048;
        validated = validated && caps.values.MAX_CUBE_MAP_TEXTURE_SIZE >= 16384;
        validated = validated && caps.limits.MAX_TEXTURE_IMAGE_UNITS >= 16;

        return validated;
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

        GL4 gl4 = (GL4) gl;

        return true;
    }
}
