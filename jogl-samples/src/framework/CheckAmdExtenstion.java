/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package framework;

import com.jogamp.newt.Display;
import com.jogamp.newt.NewtFactory;
import com.jogamp.newt.Screen;
import com.jogamp.newt.opengl.GLWindow;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.Animator;

/**
 *
 * @author GBarbieri
 */
public class CheckAmdExtenstion implements GLEventListener {

    private final int screenIdx = 0;
    protected GLWindow glWindow;
    protected Animator animator;

    public static void main(String[] args) {
        new CheckAmdExtenstion();
    }

    public CheckAmdExtenstion() {
        Display display = NewtFactory.createDisplay(null);
        Screen screen = NewtFactory.createScreen(display, screenIdx);
        GLProfile glProfile = GLProfile.get(GLProfile.GL4);
        GLCapabilities glCapabilities = new GLCapabilities(glProfile);
        glWindow = GLWindow.create(screen, glCapabilities);

        assert glWindow != null;

        glWindow.setUndecorated(false);
        glWindow.setAlwaysOnTop(false);
        glWindow.setFullscreen(false);
        glWindow.setPointerVisible(true);
        glWindow.confinePointer(false);
        glWindow.setTitle("CheckAmdExtenstion");
        glWindow.setSize(640, 480);
        glWindow.setVisible(true);
        glWindow.addGLEventListener(this);

        animator = new Animator();
        animator.add(glWindow);
        animator.start();
    }

    String[] extenstions = new String[]{
        "GL_AMD_performance_monitor", // gl-430-perf-monitor-amd
        "GL_AMD_blend_minmax_factor", // gl-500-blend-op-amd
        "GL_AMD_pinned_memory", // gl-500-buffer-pinned-amd
        "GL_AMD_vertex_shader_viewport_index", // gl-500-fbo-layered-amd
        "GL_AMD_vertex_shader_layer", // gl-500-fbo-layered-amd
        "GL_AMD_sample_positions", // gl-500-fbo-multisample-position-amd
        "GL_AMD_depth_clamp_separate", // gl-500-test-depth-clamp-separate-amd
        "GL_AMD_sparse_texture" // gl-500-texture-sparse-amd
    };

    @Override
    public void init(GLAutoDrawable drawable) {

        GL4 gl4 = drawable.getGL().getGL4();

        for (String extenstion : extenstions) {
            System.out.println(extenstion + ": " + gl4.isExtensionAvailable(extenstion));
        }
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        animator.stop();
        System.exit(0);
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        
        GL4 gl4 = drawable.getGL().getGL4();

        gl4.glViewport(0, 0, 640, 480);
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 0.5f, 0.0f, 1.0f}, 0);
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

    }

}
