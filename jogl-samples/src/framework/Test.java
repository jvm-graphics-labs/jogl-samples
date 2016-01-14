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
import static com.jogamp.opengl.GL.GL_EXTENSIONS;
import static com.jogamp.opengl.GL.GL_FALSE;
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
import static com.jogamp.opengl.GL.GL_TRUE;
import com.jogamp.opengl.GL2ES2;
import static com.jogamp.opengl.GL2ES2.GL_DEBUG_SEVERITY_LOW;
import static com.jogamp.opengl.GL2ES2.GL_DEBUG_SOURCE_APPLICATION;
import static com.jogamp.opengl.GL2ES2.GL_DEBUG_TYPE_OTHER;
import static com.jogamp.opengl.GL2ES2.GL_INFO_LOG_LENGTH;
import static com.jogamp.opengl.GL2ES2.GL_LINK_STATUS;
import static com.jogamp.opengl.GL2ES2.GL_MAX_VERTEX_ATTRIBS;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_ATTRIB_ARRAY_BUFFER_BINDING;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_ATTRIB_ARRAY_ENABLED;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_ATTRIB_ARRAY_NORMALIZED;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_ATTRIB_ARRAY_SIZE;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_ATTRIB_ARRAY_STRIDE;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_ATTRIB_ARRAY_TYPE;
import com.jogamp.opengl.GL2ES3;
import static com.jogamp.opengl.GL2ES3.GL_FRAMEBUFFER_UNDEFINED;
import static com.jogamp.opengl.GL2ES3.GL_MAJOR_VERSION;
import static com.jogamp.opengl.GL2ES3.GL_MINOR_VERSION;
import static com.jogamp.opengl.GL2ES3.GL_NUM_EXTENSIONS;
import static com.jogamp.opengl.GL2ES3.GL_VERTEX_ATTRIB_ARRAY_DIVISOR;
import static com.jogamp.opengl.GL2ES3.GL_VERTEX_ATTRIB_ARRAY_INTEGER;
import static com.jogamp.opengl.GL2GL3.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER;
import static com.jogamp.opengl.GL2GL3.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER;
import com.jogamp.opengl.GL3;
import static com.jogamp.opengl.GL3.GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS;
import com.jogamp.opengl.GL4;
import static com.jogamp.opengl.GL4.GL_VERTEX_ATTRIB_ARRAY_LONG;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.GLBuffers;
import static framework.Profile.ES;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;
import jglm.Vec2;
import jglm.Vec2i;
import jglm.Vec3;

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
    protected final String TEXTURE_ROOT = "/data";

    public Test(String title, Profile profile, int major, int minor, Vec2 orientation) {
        this(title, profile, major, minor, new Vec2i(640, 480), orientation, new Vec2(0, 4));
    }

    public Test(String title, Profile profile, int major, int minor, Vec2i windowSize) {
        this(title, profile, major, minor, windowSize, new Vec2(), new Vec2(0, 4));
    }

    public Test(String title, Profile profile, int major, int minor, Vec2i windowSize, Vec2 orientation) {
        this(title, profile, major, minor, windowSize, orientation, new Vec2(0, 4));
    }

    public Test(String title, Profile profile, int major, int minor) {

        this(title, profile, major, minor, new Vec2i(640, 480), new Vec2(), new Vec2(0, 4));
    }

    public Test(String title, Profile profile, int major, int minor, Vec2i windowSize, Vec2 orientation, Vec2 position) {

        this.profile = profile;
        this.major = major;
        this.minor = minor;
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
        glWindow.setContextCreationFlags(GLContext.CTX_OPTION_DEBUG);

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
//                glWindow.getContext().enableGLDebugMessage(true);
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

        GL gl = getCorrespondingGl(drawable);

        assert checkGLVersion(gl);

        assert begin(gl);
    }

    private GL getCorrespondingGl(GLAutoDrawable drawable) {
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
                        return drawable.getGL().getGL3();
                    case 4:
                        return drawable.getGL().getGL4();
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

        GL gl = getCorrespondingGl(drawable);
        assert end(gl);
        System.exit(0);
    }

    protected boolean end(GL gl) {
        return true;
    }

    @Override
    public final void display(GLAutoDrawable drawable) {

        GL gl = getCorrespondingGl(drawable);

        assert render(gl);

        assert checkError(gl, "render");
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

    private boolean checkGLVersion(GL gl) {

        int[] majorVersionContext = new int[]{0};
        int[] minorVersionContext = new int[]{0};
        gl.glGetIntegerv(GL_MAJOR_VERSION, majorVersionContext, 0);
        gl.glGetIntegerv(GL_MINOR_VERSION, minorVersionContext, 0);
        System.out.println("OpenGL Version Needed " + major + "." + minor
                + " ( " + majorVersionContext[0] + "," + minorVersionContext[0] + " found)");
        return version(majorVersionContext[0], minorVersionContext[0])
                >= version(major, minor);
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

    protected boolean checkExtension(GL3 gl3, String extensionName) {

        int[] extensionCount = {0};
        gl3.glGetIntegerv(GL_NUM_EXTENSIONS, extensionCount, 0);
        for (int i = 0; i < extensionCount[0]; i++) {
            if (gl3.glGetStringi(GL_EXTENSIONS, i).equals(extensionName)) {
                return true;
            }
        }
        System.out.println("Failed to find Extension: " + extensionName);
        return false;
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

        int[] result = {0};
        gl4.glGetIntegerv(value, result, 0);
        System.out.println(value + "(" + string + "): " + result[0]);
        if (checkExtension(gl4, "GL_ARB_debug_output")) {
            gl4.glDebugMessageInsert(GL_DEBUG_SOURCE_APPLICATION, GL_DEBUG_TYPE_OTHER, 1,
                    GL_DEBUG_SEVERITY_LOW, string.length(), string);
        }
    }

    protected boolean checkProgram(GL2ES2 gl2, int programName) {

        if (programName == 0) {
            return false;
        }

        int[] result = {GL_FALSE};
        gl2.glGetProgramiv(programName, GL_LINK_STATUS, result, 0);

        if (result[0] == GL_TRUE) {
            return true;
        }

        int[] infoLogLength = {0};
        gl2.glGetProgramiv(programName, GL_INFO_LOG_LENGTH, infoLogLength, 0);
        if (infoLogLength[0] > 0) {
            byte[] buffer = new byte[infoLogLength[0]];
            gl2.glGetProgramInfoLog(programName, infoLogLength[0], null, 0, buffer, 0);
            System.out.println(new String(buffer));
        }

        return result[0] == GL_TRUE;
    }

    protected boolean validate(GL2ES3 gl4, int vertexArrayName, VertexAttrib[] expected) {

        boolean success = true;

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
            success = success && (vertexAttrib.isEqual(expected[attribLocation]));
            assert (success);
        }
        gl4.glBindVertexArray(0);

        return success;
    }
    
    protected float cameraDistance() {
        return translationCurrent.y;
    }

    protected Vec3 cameraPosition() {
        return new Vec3(0.0f, 0.0f, -translationCurrent.y);
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
            BufferUtils.destroyDirectBuffer(buffer);

//            File outputfile = new File("/home/elect/Downloads/texture.jpg");
            File outputfile = new File("D:\\Downloads\\texture.png");
            ImageIO.write(screenshot, "png", outputfile);
        } catch (IOException ex) {
            //  Logger.getLogger(EC_DepthPeeling.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
