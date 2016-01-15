/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_430;

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
public class Gl_430_caps extends Test {

    public static void main(String[] args) {
        Gl_430_caps gl_430_caps = new Gl_430_caps();
    }

    public Gl_430_caps() {
        super("gl-430-caps", Profile.CORE, 4, 3);
    }

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        Caps caps = new Caps(gl4, Profile.CORE);

        boolean validated = true;

        validated = validated && caps.limits.MAX_VERTEX_UNIFORM_BLOCKS >= 14;
        validated = validated && caps.limits.MAX_TESS_CONTROL_UNIFORM_BLOCKS >= 14;
        validated = validated && caps.limits.MAX_TESS_EVALUATION_UNIFORM_BLOCKS >= 14;
        validated = validated && caps.limits.MAX_GEOMETRY_UNIFORM_BLOCKS >= 14;
        validated = validated && caps.limits.MAX_FRAGMENT_UNIFORM_BLOCKS >= 14;
        validated = validated && caps.limits.MAX_COMPUTE_UNIFORM_BLOCKS >= 14;

        validated = validated && caps.limits.MAX_VERTEX_UNIFORM_COMPONENTS >= 1024;
        validated = validated && caps.limits.MAX_TESS_CONTROL_UNIFORM_COMPONENTS >= 1024;
        validated = validated && caps.limits.MAX_TESS_EVALUATION_UNIFORM_COMPONENTS >= 1024;
        validated = validated && caps.limits.MAX_GEOMETRY_UNIFORM_COMPONENTS >= 512;
        validated = validated && caps.limits.MAX_FRAGMENT_UNIFORM_COMPONENTS >= 1024;
        validated = validated && caps.limits.MAX_COMPUTE_UNIFORM_COMPONENTS >= 512;

        validated = validated && caps.limits.MAX_COMBINED_UNIFORM_BLOCKS >= 70;
        validated = validated && caps.limits.MAX_UNIFORM_BUFFER_BINDINGS >= 84;
        validated = validated && caps.limits.MAX_UNIFORM_BLOCK_SIZE >= 16384;
        validated = validated && caps.limits.MAX_SHADER_STORAGE_BLOCK_SIZE >= (1 << 24);

        long combinedVertUniformCount = caps.limits.MAX_COMBINED_VERTEX_UNIFORM_COMPONENTS;
        long combinedContUniformCount = caps.limits.MAX_COMBINED_TESS_CONTROL_UNIFORM_COMPONENTS;
        long combinedEvalUniformCount = caps.limits.MAX_COMBINED_TESS_EVALUATION_UNIFORM_COMPONENTS;
        long combinedGeomUniformCount = caps.limits.MAX_COMBINED_GEOMETRY_UNIFORM_COMPONENTS;
        long combinedFragUniformCount = caps.limits.MAX_COMBINED_FRAGMENT_UNIFORM_COMPONENTS;
        long combinedCompUniformCount = caps.limits.MAX_COMBINED_COMPUTE_UNIFORM_COMPONENTS;

        long vertUniformCount = (caps.limits.MAX_VERTEX_UNIFORM_BLOCKS * caps.limits.MAX_UNIFORM_BLOCK_SIZE / 4)
                + caps.limits.MAX_VERTEX_UNIFORM_COMPONENTS;
        long contUniformCount = (caps.limits.MAX_TESS_CONTROL_UNIFORM_BLOCKS * caps.limits.MAX_UNIFORM_BLOCK_SIZE / 4)
                + caps.limits.MAX_TESS_CONTROL_UNIFORM_COMPONENTS;
        long evalUniformCount = (caps.limits.MAX_TESS_EVALUATION_UNIFORM_BLOCKS * caps.limits.MAX_UNIFORM_BLOCK_SIZE / 4)
                + caps.limits.MAX_TESS_EVALUATION_UNIFORM_COMPONENTS;
        long geomUniformCount = (caps.limits.MAX_GEOMETRY_UNIFORM_BLOCKS * caps.limits.MAX_UNIFORM_BLOCK_SIZE / 4)
                + caps.limits.MAX_GEOMETRY_UNIFORM_COMPONENTS;
        long fragUniformCount = (caps.limits.MAX_FRAGMENT_UNIFORM_BLOCKS * caps.limits.MAX_UNIFORM_BLOCK_SIZE / 4)
                + caps.limits.MAX_FRAGMENT_UNIFORM_COMPONENTS;
        long compUniformCount = (caps.limits.MAX_COMPUTE_UNIFORM_BLOCKS * caps.limits.MAX_UNIFORM_BLOCK_SIZE / 4)
                + caps.limits.MAX_COMPUTE_UNIFORM_COMPONENTS;

        validated = validated && combinedVertUniformCount <= vertUniformCount;
        validated = validated && combinedContUniformCount <= contUniformCount;
        validated = validated && combinedEvalUniformCount <= evalUniformCount;
        validated = validated && combinedGeomUniformCount <= geomUniformCount;
        validated = validated && combinedFragUniformCount <= fragUniformCount;
        validated = validated && combinedCompUniformCount <= compUniformCount;

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
        return true;
    }
}
