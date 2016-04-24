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
import static com.jogamp.opengl.GL2ES2.GL_DEBUG_SEVERITY_LOW;
import static com.jogamp.opengl.GL2ES2.GL_DEBUG_SOURCE_APPLICATION;
import static com.jogamp.opengl.GL2ES2.GL_DEBUG_TYPE_OTHER;
import com.jogamp.opengl.GL2ES3;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GL4;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.GLBuffers;
import glm.mat._4.Mat4;
import static framework.Profile.ES;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;
import glm.vec._2.Vec2;
import glm.vec._2.i.Vec2i;
import glm.vec._3.Vec3;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 *
 * @author gbarbieri
 */
public class Test implements GLEventListener, KeyListener {

    private final int screenIdx = 0;
    protected Vec2i windowSize;
    protected GLWindow glWindow;
    protected Animator animator;
    private Profile profile;
    private final int major, minor;
    private final Vec2 translationOrigin, translationCurrent, rotationOrigin, rotationCurrent;
    private boolean glDebug;
    protected final String TEXTURE_ROOT = "/data/textures";
    private Success success;
    protected FloatBuffer clearColor = GLBuffers.newDirectFloatBuffer(4),
            clearDepth = GLBuffers.newDirectFloatBuffer(1);

    public Test(String title, Profile profile, int major, int minor, Vec2 orientation, boolean glDebug) {
        this(title, profile, major, minor, new Vec2i(640, 480), orientation, new Vec2(0, 4), glDebug, Success.RUN_ONLY);
    }

    public Test(String title, Profile profile, int major, int minor, Vec2 orientation) {
        this(title, profile, major, minor, new Vec2i(640, 480), orientation, new Vec2(0, 4), false, Success.RUN_ONLY);
    }

    public Test(String title, Profile profile, int major, int minor, Vec2i windowSize) {
        this(title, profile, major, minor, windowSize, new Vec2(), new Vec2(0, 4), false, Success.RUN_ONLY);
    }

    public Test(String title, Profile profile, int major, int minor, Vec2i windowSize, Vec2 orientation) {
        this(title, profile, major, minor, windowSize, orientation, new Vec2(0, 4), false, Success.RUN_ONLY);
    }

    public Test(String title, Profile profile, int major, int minor, Success success) {
        this(title, profile, major, minor, new Vec2i(640, 480), new Vec2(), new Vec2(0, 4), false, success);
    }

    public Test(String title, Profile profile, int major, int minor) {
        this(title, profile, major, minor, new Vec2i(640, 480), new Vec2(), new Vec2(0, 4), false, Success.RUN_ONLY);
    }

    public Test(String title, Profile profile,
            int major, int minor,
            Vec2i windowSize, Vec2 orientation, Vec2 position,
            boolean glDebug, Success success) {

        this.profile = profile;
        this.major = major;
        this.minor = minor;
        this.windowSize = windowSize;
        this.translationOrigin = position;
        this.translationCurrent = position;
        this.rotationOrigin = orientation;
        this.rotationCurrent = orientation;
        this.glDebug = glDebug;
        this.success = success;

        initGL(title);
    }

    private void initGL(String title) {

        Display display = NewtFactory.createDisplay(null);
        Screen screen = NewtFactory.createScreen(display, screenIdx);
        GLProfile glProfile = getGlProfile();
        GLCapabilities glCapabilities = new GLCapabilities(glProfile);
        glWindow = GLWindow.create(screen, glCapabilities);
//        if (glDebug) {
        glWindow.setContextCreationFlags(GLContext.CTX_OPTION_DEBUG);
//        }

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
//        if (glDebug) {
        glWindow.getContext().addGLDebugListener(new GlDebugOutput());
//        }
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

        GL gl = getGl(drawable);

        assert checkGLVersion();

        if (success != Success.GENERATE_ERROR) {
            assert begin(gl);
        } else {
            begin(gl);
        }
    }

    private GLProfile getGlProfile() {
        switch (profile) {
            case ES:
                switch (major) {
                    case 1:
                        return GLProfile.get(GLProfile.GL2ES1);
                    case 2:
                        return GLProfile.get(GLProfile.GL2ES2);
                    case 3:
                        return GLProfile.get(GLProfile.GL4ES3);
                }
            case CORE:
                switch (major) {
                    case 1:
                        return GLProfile.get(GLProfile.GL2);
                    case 2:
                        return GLProfile.get(GLProfile.GL2);
                    case 3:
                        if (profile == Profile.COMPATIBILITY) {
                            return GLProfile.get(GLProfile.GL3bc);
                        } else {
                            return GLProfile.get(GLProfile.GL3);
                        }
                    case 4:
                        if (profile == Profile.COMPATIBILITY) {
                            return GLProfile.get(GLProfile.GL4bc);
                        } else {
                            return GLProfile.get(GLProfile.GL4);
                        }
                }
        }
        return GLProfile.getDefault();
    }

    private GL getGl(GLAutoDrawable drawable) {
        switch (profile) {
            case ES:
                switch (major) {
                    case 1:
                        return drawable.getGL().getGL2ES1();
                    case 2:
                        return drawable.getGL().getGL2ES2();
                    case 3:
                        return drawable.getGL().getGL2ES3();
                    default:
                        return drawable.getGL();
                }
            case CORE:
                switch (major) {
                    case 1:
                        return drawable.getGL().getGL();
                    case 2:
                        return drawable.getGL().getGL2();
                    case 3:
                        if (profile == Profile.COMPATIBILITY) {
                            return drawable.getGL().getGL3bc();
                        } else {
                            return drawable.getGL().getGL3();
                        }
                    case 4:
                        if (profile == Profile.COMPATIBILITY) {
                            return drawable.getGL().getGL4bc();
                        } else {
                            return drawable.getGL().getGL4();
                        }
                    default:
                        return drawable.getGL();
                }
            default:
                return drawable.getGL();
        }
    }

    protected boolean begin(GL gl) {

        return true;
    }

    @Override
    public final void dispose(GLAutoDrawable drawable) {
        GL gl = getGl(drawable);
        assert end(gl);
        BufferUtils.destroyDirectBuffer(clearColor);
        BufferUtils.destroyDirectBuffer(clearDepth);
        System.exit(0);
    }

    protected boolean end(GL gl) {        
        return true;
    }

    @Override
    public final void display(GLAutoDrawable drawable) {

        GL gl = getGl(drawable);

        assert render(gl);

//        assert checkError(gl, "render");
    }

    protected boolean render(GL gl) {
        return true;
    }

    @Override
    public final void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

    }

    private float[] viewTranslate = new float[16], viewRotateX = new float[16],
            viewRotateY = new float[16], view = new float[16];
    private Mat4 viewMat4 = new Mat4();
    protected float[] tmpVec3 = new float[3];
    protected float[] tmpMat4 = new float[16];

    protected final Mat4 viewMat4() {
        return viewMat4.identity().translate(0.0f, 0.0f, -translationCurrent.y)
                .rotate(rotationCurrent.y, 1.f, 0.f, 0.f)
                .rotate(rotationCurrent.x, 0.f, 1.f, 0.f);
    }

    private boolean checkGLVersion() {

        GLProfile glp = GLProfile.getMaxProgrammableCore(true);
//        int majorVersionContext = GLContext.getMaxMajor(GLContext.CONTEXT_CURRENT);
//        int minorVersionContext = GLContext.getMaxMinor(GLContext.CONTEXT_CURRENT, majorVersionContext);
//        System.out.println("OpenGL Version Needed " + major + "." + minor
//                + " ( " + majorVersionContext + "," + minorVersionContext + " found)");
        System.out.println("OpenGL Version Needed " + major + "." + minor + " ( " + glp.getName() + " found)");
//        return version(majorVersionContext[0], minorVersionContext[0])
//                >= version(major, minor);
        return GLContext.isValidGLVersion(GLContext.CONTEXT_CURRENT, major, minor);
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

    protected void logImplementationDependentLimit(GL4 gl4, int value, String string) {

        IntBuffer result = GLBuffers.newDirectIntBuffer(1);
        gl4.glDebugMessageControl(GL_DEBUG_SOURCE_APPLICATION, GL_DEBUG_TYPE_OTHER, GL_DEBUG_SEVERITY_LOW, 0, null, true);
        gl4.glGetIntegerv(value, result);
        String limit = string + ": " + result.get(0);
        if (gl4.isExtensionAvailable("GL_KHR_debug")) {
            gl4.glDebugMessageInsert(GL_DEBUG_SOURCE_APPLICATION, GL_DEBUG_TYPE_OTHER, 1, GL_DEBUG_SEVERITY_LOW,
                    limit.length(), limit);
        }
        gl4.glDebugMessageControl(GL_DEBUG_SOURCE_APPLICATION, GL_DEBUG_TYPE_OTHER, GL_DEBUG_SEVERITY_LOW, 0, null, false);
        BufferUtils.destroyDirectBuffer(result);
    }

    protected boolean validate(GL2ES3 gl4, int vertexArrayName, VertexAttrib[] expected) {

        boolean success_ = true;

        int[] maxVertexAttrib = {0};
        gl4.glGetIntegerv(GL_MAX_VERTEX_ATTRIBS, maxVertexAttrib, 0);

        gl4.glBindVertexArray(vertexArrayName);
        for (int attribLocation = 0; attribLocation < Math.min(maxVertexAttrib[0], expected.length); ++attribLocation) {
            VertexAttrib vertexAttrib = new VertexAttrib();
            int[] value = {0};
            gl4.glGetVertexAttribiv(attribLocation, GL_VERTEX_ATTRIB_ARRAY_ENABLED, value, 0);
            vertexAttrib.enabled = value[0];
            gl4.glGetVertexAttribiv(attribLocation, GL_VERTEX_ATTRIB_ARRAY_BUFFER_BINDING, value, 0);
            vertexAttrib.binding = value[0];
            gl4.glGetVertexAttribiv(attribLocation, GL_VERTEX_ATTRIB_ARRAY_SIZE, value, 0);
            vertexAttrib.size = value[0];
            gl4.glGetVertexAttribiv(attribLocation, GL_VERTEX_ATTRIB_ARRAY_STRIDE, value, 0);
            vertexAttrib.stride = value[0];
            gl4.glGetVertexAttribiv(attribLocation, GL_VERTEX_ATTRIB_ARRAY_TYPE, value, 0);
            vertexAttrib.type = value[0];
            gl4.glGetVertexAttribiv(attribLocation, GL_VERTEX_ATTRIB_ARRAY_NORMALIZED, value, 0);
            vertexAttrib.normalized = value[0] == GL_TRUE;
            if (profile != ES || (profile == ES && (major * 10 + minor >= 30))) {
                gl4.glGetVertexAttribiv(attribLocation, GL_VERTEX_ATTRIB_ARRAY_INTEGER, value, 0);
                vertexAttrib.integer = value[0];
            }
            if (profile != ES && (major * 10 + minor >= 44)) {
                gl4.glGetVertexAttribiv(attribLocation, GL_VERTEX_ATTRIB_ARRAY_LONG, value, 0);
                vertexAttrib.long_ = value[0];
            }
            if (profile != ES && (major * 10 + minor >= 31)) {
                gl4.glGetVertexAttribiv(attribLocation, GL_VERTEX_ATTRIB_ARRAY_DIVISOR, value, 0);
                vertexAttrib.divisor = value[0];
            }
//            gl4.glGetVertexAttribPointerv(attribLocation, GL_VERTEX_ATTRIB_ARRAY_POINTER,  value,0);
            success_ = success_ && (vertexAttrib.isEqual(expected[attribLocation]));
            assert (success_);
        }
        gl4.glBindVertexArray(0);

        return success_;
    }

    protected float cameraDistance() {
        return translationCurrent.y;
    }

    protected Vec3 cameraPosition() {
        return new Vec3(0.0f, 0.0f, -translationCurrent.y);
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

            gl3.glReadBuffer(GL_BACK);
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
                    graphics.drawRect(w, height - h, 1, 1); // height - h is for flipping the image
//                    graphics.drawRect(w, h, 1, 1); // height - h is for flipping the image
                }
            }
            BufferUtils.destroyDirectBuffer(buffer);

//            File outputfile = new File("/home/elect/Downloads/texture.jpg");
            File outputfile = new File("D:\\Downloads\\texture.png");
            ImageIO.write(screenshot, "png", outputfile);
        } catch (IOException ex) {
            //  Logger.getLogger(EC_DepthPeeling.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public enum Success {
        RUN_ONLY,
        GENERATE_ERROR,
        MATCH_TEMPLATE
    }
}
