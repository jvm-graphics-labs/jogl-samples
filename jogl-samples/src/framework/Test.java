/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package framework;

import com.jogamp.newt.Display;
import com.jogamp.newt.NewtFactory;
import com.jogamp.newt.Screen;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_FRAMEBUFFER;
import static com.jogamp.opengl.GL.GL_FRAMEBUFFER_COMPLETE;
import static com.jogamp.opengl.GL.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT;
import static com.jogamp.opengl.GL.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT;
import static com.jogamp.opengl.GL.GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE;
import static com.jogamp.opengl.GL.GL_FRAMEBUFFER_UNSUPPORTED;
import static com.jogamp.opengl.GL.GL_INVALID_ENUM;
import static com.jogamp.opengl.GL.GL_INVALID_FRAMEBUFFER_OPERATION;
import static com.jogamp.opengl.GL.GL_INVALID_OPERATION;
import static com.jogamp.opengl.GL.GL_INVALID_VALUE;
import static com.jogamp.opengl.GL.GL_NO_ERROR;
import static com.jogamp.opengl.GL.GL_OUT_OF_MEMORY;
import static com.jogamp.opengl.GL2ES3.GL_FRAMEBUFFER_UNDEFINED;
import static com.jogamp.opengl.GL2ES3.GL_MAJOR_VERSION;
import static com.jogamp.opengl.GL2ES3.GL_MINOR_VERSION;
import static com.jogamp.opengl.GL2GL3.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER;
import static com.jogamp.opengl.GL2GL3.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER;
import com.jogamp.opengl.GL3;
import static com.jogamp.opengl.GL3.GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.GLBuffers;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;
import jglm.Vec2;
import jglm.Vec2i;

/**
 *
 * @author gbarbieri
 */
public class Test implements GLEventListener, KeyListener {

    private final int screenIdx = 0;
    protected Vec2i windowSize;
    protected GLWindow glWindow;
    protected Animator animator;
    private final int majorVersionRequire, minorVersionRequire;
    private final Vec2 translationOrigin, translationCurrent, rotationOrigin, rotationCurrent;
    protected final String TEXTURE_ROOT = "/data";

    public Test(String title, int majorVersionRequire, int minorVersionRequire, Vec2 orientation) {
        this(title, majorVersionRequire, minorVersionRequire, new Vec2i(640, 480), orientation, new Vec2(0, 4));
    }

    public Test(String title, int majorVersionRequire, int minorVersionRequire) {

        this(title, majorVersionRequire, minorVersionRequire, new Vec2i(640, 480),
                new Vec2(), new Vec2(0, 4));
    }

    public Test(String title, int majorVersionRequire, int minorVersionRequire, Vec2i windowSize,
            Vec2 orientation, Vec2 position) {

        this.majorVersionRequire = majorVersionRequire;
        this.minorVersionRequire = minorVersionRequire;
        this.windowSize = windowSize;
        this.translationOrigin = position;
        this.translationCurrent = position;
        this.rotationOrigin = orientation;
        this.rotationCurrent = orientation;

        initGL(title);
    }

    private void initGL(String title) {
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
        glWindow.setSize(windowSize.x, windowSize.y);
        glWindow.setVisible(true);
        glWindow.addGLEventListener(this);
        glWindow.addKeyListener(this);

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

    protected boolean begin(GL gl) {

        return true;
    }

    @Override
    public final void dispose(GLAutoDrawable drawable) {

        GL3 gl3 = drawable.getGL().getGL3();
        assert end(gl3);
        System.exit(0);
    }

    protected boolean end(GL gl) {
        return true;
    }

    @Override
    public final void display(GLAutoDrawable drawable) {

        GL3 gl3 = drawable.getGL().getGL3();

        assert render(gl3);

        assert checkError(gl3, "render");
    }

    protected boolean render(GL gl) {
        return true;
    }

    @Override
    public final void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

    }

    private float[] viewTranslate = new float[16], viewRotateX = new float[16],
            viewRotateY = new float[16], view = new float[16];
    protected float[] tmpVec = new float[3];

    protected final float[] view() {

        FloatUtil.makeTranslation(viewTranslate, true, 0, 0, -translationCurrent.y);
        FloatUtil.makeRotationAxis(viewRotateX, 0, rotationCurrent.y, 1f, 0f, 0f, tmpVec);
        FloatUtil.multMatrix(viewTranslate, viewRotateX, viewRotateX);
        FloatUtil.makeRotationAxis(viewRotateY, 0, rotationCurrent.x, 0f, 1f, 0f, tmpVec);
        FloatUtil.multMatrix(viewRotateX, viewRotateY, view);
        return view;
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

    protected boolean checkError(GL gl, String title) {

        int error = gl.glGetError();
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

    protected boolean isFramebufferComplete(GL gl, int framebufferName) {

        gl.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName);

        int status = gl.glCheckFramebufferStatus(GL_FRAMEBUFFER);

        switch (status) {

            case GL_FRAMEBUFFER_UNDEFINED:

                System.err.println("OpenGL Error(GL_FRAMEBUFFER_UNDEFINED)");
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
                System.err.println("OpenGL Error(GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT)");
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
                System.err.println("OpenGL Error(GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT)");
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER:
                System.err.println("OpenGL Error(GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER)");
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER:
                System.err.println("OpenGL Error(GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER)");
                break;
            case GL_FRAMEBUFFER_UNSUPPORTED:
                System.err.println("OpenGL Error(GL_FRAMEBUFFER_UNSUPPORTED)");
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE:
                System.err.println("OpenGL Error(GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE)");
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS:
                System.err.println("OpenGL Error(GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS)");
                break;
        }

        return status == GL_FRAMEBUFFER_COMPLETE;
    }

    protected float cameraDistance() {
        return translationCurrent.y;
    }

    protected final String getDataDirectory() {
        return "/data/";
    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

        switch (e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE:
                animator.stop();
                glWindow.destroy();
                break;
        }
    }

    protected void saveImage(GL3 gl3, int width, int height) {

        try {
            BufferedImage screenshot = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics graphics = screenshot.getGraphics();

            ByteBuffer buffer = GLBuffers.newDirectByteBuffer(width * height * 4);

            gl3.glReadBuffer(GL3.GL_COLOR_ATTACHMENT0);
            gl3.glReadPixels(0, 0, width, height, GL3.GL_RGBA, GL3.GL_UNSIGNED_BYTE, buffer);

            for (int h = 0; h < height; h++) {
                for (int w = 0; w < width; w++) {
                    // The color are the three consecutive bytes, it's like referencing
                    // to the next consecutive array elements, so we got red, green, blue..
                    // red, green, blue, and so on..
//                    System.out.println("(" + ((buffer.get() & 0xff) / 255) + ", "
//                            + ((buffer.get() & 0xff) / 255) + ", " + ((buffer.get() & 0xff) / 255)
//                            + ", " + ((buffer.get() & 0xff) / 255) + ")");
                    graphics.setColor(new Color((buffer.get() & 0xff), (buffer.get() & 0xff), 
                            (buffer.get() & 0xff)));
                    buffer.get();   // alpha
//                    graphics.drawRect(w, height - h, 1, 1); // height - h is for flipping the image
                    graphics.drawRect(w, h, 1, 1); // height - h is for flipping the image
                }
            }

//            File outputfile = new File("/home/elect/Downloads/texture.jpg");
            File outputfile = new File("D:\\Downloads\\texture.png");
            ImageIO.write(screenshot, "png", outputfile);
        } catch (IOException ex) {
            //  Logger.getLogger(EC_DepthPeeling.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
