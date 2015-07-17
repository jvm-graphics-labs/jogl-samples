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
import static com.jogamp.opengl.GL.GL_INVALID_ENUM;
import static com.jogamp.opengl.GL.GL_INVALID_FRAMEBUFFER_OPERATION;
import static com.jogamp.opengl.GL.GL_INVALID_OPERATION;
import static com.jogamp.opengl.GL.GL_INVALID_VALUE;
import static com.jogamp.opengl.GL.GL_NO_ERROR;
import static com.jogamp.opengl.GL.GL_OUT_OF_MEMORY;
import static com.jogamp.opengl.GL2ES3.GL_MAJOR_VERSION;
import static com.jogamp.opengl.GL2ES3.GL_MINOR_VERSION;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.Animator;
import jglm.Vec2i;

/**
 *
 * @author gbarbieri
 */
public class Test implements GLEventListener {

    private final int screenIdx = 0;
    protected Vec2i windowSize = new Vec2i(640, 480);
    protected GLWindow glWindow;
    protected Animator animator;
    private final int majorVersionRequire, minorVersionRequire;

    public Test(String title, int majorVersionRequire, int minorVersionRequire) {

        this.majorVersionRequire = majorVersionRequire;
        this.minorVersionRequire = minorVersionRequire;

        Display display = NewtFactory.createDisplay(null);
        Screen screen = NewtFactory.createScreen(display, screenIdx);
        GLProfile glProfile = GLProfile.getDefault();
        GLCapabilities glCapabilities = new GLCapabilities(glProfile);
        glWindow = GLWindow.create(screen, glCapabilities);
        assert glWindow != null;

        glWindow.setUndecorated(false);
        glWindow.setAlwaysOnTop(false);
        glWindow.setFullscreen(false);
        glWindow.setPointerVisible(true);
        glWindow.confinePointer(false);
        glWindow.setTitle(title);

        animator = new Animator();
        animator.add(glWindow);
        animator.setRunAsFastAsPossible(false);
        animator.setExclusiveContext(true);
//        animator.setUpdateFPSFrames(10, System.out);
        animator.start();
    }

    @Override
    public final void init(GLAutoDrawable drawable) {

        GL3 gl3 = drawable.getGL().getGL3();

        assert checkGLVersion(gl3);

        assert begin(gl3);
    }

    protected boolean begin(GL3 gl3) {
        return true;
    }

    @Override
    public final void dispose(GLAutoDrawable drawable) {
        GL3 gl3 = drawable.getGL().getGL3();
        assert end(gl3);
    }

    protected boolean end(GL3 gl3) {
        return true;
    }

    @Override
    public final void display(GLAutoDrawable drawable) {

        GL3 gl3 = drawable.getGL().getGL3();

        assert render(gl3);

        assert checkError(gl3, "render");
    }

    protected boolean render(GL3 gl3) {
        return true;
    }

    @Override
    public final void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

    }

    private boolean checkGLVersion(GL3 gl3) {

        int[] majorVersionContext = new int[]{0};
        int[] minorVersionContext = new int[]{0};
        gl3.glGetIntegerv(GL_MAJOR_VERSION, majorVersionContext, 0);
        gl3.glGetIntegerv(GL_MINOR_VERSION, minorVersionContext, 0);
        System.out.println("OpenGL Version Needed " + majorVersionRequire + "." + minorVersionRequire
                + " ( " + majorVersionContext[0] + "," + minorVersionContext[0] + " found)");
        return version(majorVersionContext[0], minorVersionContext[0])
                >= version(majorVersionRequire, minorVersionRequire);
    }

    private int version(int major, int minor) {
        return major * 100 + minor * 10;
    }

    protected boolean checkError(GL3 gl3, String title) {

        int error = gl3.glGetError();
        if (error != GL_NO_ERROR) {
            String errorString;
            switch (error) {
                case GL_INVALID_ENUM:
                    errorString = "GL_INVALID_ENUM";
                    break;
                case GL_INVALID_VALUE:
                    errorString = "GL_INVALID_VALUE";
                    break;
                case GL_INVALID_OPERATION:
                    errorString = "GL_INVALID_OPERATION";
                    break;
                case GL_INVALID_FRAMEBUFFER_OPERATION:
                    errorString = "GL_INVALID_FRAMEBUFFER_OPERATION";
                    break;
                case GL_OUT_OF_MEMORY:
                    errorString = "GL_OUT_OF_MEMORY";
                    break;
                default:
                    errorString = "UNKNOWN";
                    break;
            }
            System.out.println("OpenGL Error(" + errorString + "): " + title);
            throw new Error();
        }
        return error == GL_NO_ERROR;
    }
}
