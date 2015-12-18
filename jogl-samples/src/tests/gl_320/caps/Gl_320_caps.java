/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_320.caps;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import com.jogamp.opengl.GL3;
import framework.Caps;
import framework.Profile;
import framework.Test;

/**
 *
 * @author gbarbieri
 */
public class Gl_320_caps extends Test {

    public static void main(String[] args) {
        Gl_320_caps gl_320_caps = new Gl_320_caps();
    }

    public Gl_320_caps() {
        super("gl-320-caps", Profile.CORE, 3, 2);
    }

    @Override
    protected boolean begin(GL gl) {

        Caps caps = new Caps(gl, Profile.CORE);

        boolean validated = true;

        validated = validated && caps.limits.MAX_VERTEX_UNIFORM_BLOCKS >= 12;
        validated = validated && caps.limits.MAX_GEOMETRY_UNIFORM_BLOCKS >= 12;
        validated = validated && caps.limits.MAX_FRAGMENT_UNIFORM_BLOCKS >= 12;

        validated = validated && caps.limits.MAX_VERTEX_UNIFORM_COMPONENTS >= 1024;
        validated = validated && caps.limits.MAX_GEOMETRY_UNIFORM_COMPONENTS >= 1024;
        validated = validated && caps.limits.MAX_FRAGMENT_UNIFORM_COMPONENTS >= 1024;

        validated = validated && caps.limits.MAX_COMBINED_UNIFORM_BLOCKS >= 36;
        validated = validated && caps.limits.MAX_UNIFORM_BUFFER_BINDINGS >= 36;
        validated = validated && caps.limits.MAX_UNIFORM_BLOCK_SIZE >= 16384;
        long combinedVertUniformCount = caps.limits.MAX_COMBINED_VERTEX_UNIFORM_COMPONENTS;
        long combinedGeomUniformCount = caps.limits.MAX_COMBINED_GEOMETRY_UNIFORM_COMPONENTS;
        long combinedFragUniformCount = caps.limits.MAX_COMBINED_FRAGMENT_UNIFORM_COMPONENTS;
        long vertUniformCount = (long) caps.limits.MAX_VERTEX_UNIFORM_BLOCKS
                * (long) caps.limits.MAX_UNIFORM_BLOCK_SIZE / 4 + (long) caps.limits.MAX_VERTEX_UNIFORM_COMPONENTS;
        long geomUniformCount = (long) caps.limits.MAX_GEOMETRY_UNIFORM_BLOCKS
                * (long) caps.limits.MAX_UNIFORM_BLOCK_SIZE / 4 + (long) caps.limits.MAX_GEOMETRY_UNIFORM_COMPONENTS;
        long fragUniformCount = (long) caps.limits.MAX_FRAGMENT_UNIFORM_BLOCKS
                * (long) caps.limits.MAX_UNIFORM_BLOCK_SIZE / 4 + (long) caps.limits.MAX_FRAGMENT_UNIFORM_COMPONENTS;

        validated = validated && combinedVertUniformCount <= vertUniformCount;
        validated = validated && combinedGeomUniformCount <= geomUniformCount;
        validated = validated && combinedFragUniformCount <= fragUniformCount;

        return validated;
    }

    @Override
    protected boolean render(GL gl) {

        GL3 gl3 = (GL3) gl;

        gl3.glViewport(0, 0, glWindow.getWidth(), glWindow.getHeight());
        gl3.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 0.5f, 0.0f, 1.0f}, 0);

        return true;
    }
}
