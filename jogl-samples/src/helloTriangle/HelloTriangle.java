/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package helloTriangle;

import com.jogamp.nativewindow.util.Dimension;
import com.jogamp.newt.Display;
import com.jogamp.newt.NewtFactory;
import com.jogamp.newt.Screen;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.GLBuffers;
import framework.Semantic;
import java.nio.FloatBuffer;

/**
 *
 * @author gbarbieri
 */
public class HelloTriangle implements GLEventListener, KeyListener {

    private static int screenIdx = 0;
    private static Dimension windowSize = new Dimension(1024, 768);
    private static boolean undecorated = false;
    private static boolean alwaysOnTop = false;
    private static boolean fullscreen = false;
    private static boolean mouseVisible = true;
    private static boolean mouseConfined = false;
    public static GLWindow glWindow;
    public static Animator animator;

    public static void main(String[] args) {

        Display display = NewtFactory.createDisplay(null);
        Screen screen = NewtFactory.createScreen(display, screenIdx);
        GLProfile glProfile = GLProfile.get(GLProfile.GL4);
        GLCapabilities glCapabilities = new GLCapabilities(glProfile);
        glWindow = GLWindow.create(screen, glCapabilities);

        glWindow.setSize(windowSize.getWidth(), windowSize.getHeight());
        glWindow.setPosition(50, 50);
        glWindow.setUndecorated(undecorated);
        glWindow.setAlwaysOnTop(alwaysOnTop);
        glWindow.setFullscreen(fullscreen);
        glWindow.setPointerVisible(mouseVisible);
        glWindow.confinePointer(mouseConfined);
        glWindow.setVisible(true);

        HelloTriangle helloTriangle = new HelloTriangle();
        glWindow.addGLEventListener(helloTriangle);
        glWindow.addKeyListener(helloTriangle);

        animator = new Animator(glWindow);
        animator.start();
    }

    private int[] objects = new int[Semantic.Object.size];
    private float[] vertexData = new float[]{
        -0.5f, -0.5f, 0f, 1f, 0f, 0f,
        +0.5f, -0.5f, 0f, 0f, 0f, 1f,
        +0.0f, +0.5f, 0f, 0f, 1f, 0f
    };

    private short[] indexData = new short[]{
        0, 2, 1
    };

    public HelloTriangle() {

    }

    @Override
    public void init(GLAutoDrawable drawable) {
        System.out.println("init");

        GL4 gl4 = drawable.getGL().getGL4();

        initVbo(gl4);

        initVao(gl4);
    }

    private void initVbo(GL4 gl4) {

        gl4.glGenBuffers(1, objects, Semantic.Object.vbo);
        gl4.glBindBuffer(GL4.GL_ARRAY_BUFFER, objects[Semantic.Object.vbo]);
        {
            FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
            int size = vertexData.length * GLBuffers.SIZEOF_FLOAT;
            gl4.glBufferData(GL4.GL_ARRAY_BUFFER, size, vertexBuffer, GL4.GL_STATIC_DRAW);
        }
        gl4.glBindBuffer(GL4.GL_ARRAY_BUFFER, 0);
    }

    private void initIbo(GL4 gl4) {

        gl4.glGenBuffers(1, objects, Semantic.Object.ibo);
        gl4.glBindBuffer(GL4.GL_ARRAY_BUFFER, objects[Semantic.Object.ibo]);
        {
            FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
            int size = vertexData.length * GLBuffers.SIZEOF_FLOAT;
            gl4.glBufferData(GL4.GL_ARRAY_BUFFER, size, vertexBuffer, GL4.GL_STATIC_DRAW);
        }
        gl4.glBindBuffer(GL4.GL_ARRAY_BUFFER, 0);
    }

    private void initVao(GL4 gl4) {

        gl4.glBindBuffer(GL4.GL_ARRAY_BUFFER, objects[Semantic.Object.vbo]);
        {
            gl4.glGenVertexArrays(1, objects, Semantic.Object.vao);
            gl4.glBindVertexArray(objects[Semantic.Object.vao]);
            {
                int stride = (3 + 3) * GLBuffers.SIZEOF_FLOAT;

                gl4.glEnableVertexAttribArray(0);
                gl4.glVertexAttribPointer(Semantic.Attr.position, 3, GL4.GL_FLOAT,
                        false, stride, 0 * GLBuffers.SIZEOF_FLOAT);

                gl4.glEnableVertexAttribArray(1);
                gl4.glVertexAttribPointer(Semantic.Attr.color, 3, GL4.GL_FLOAT,
                        false, stride, 3 * GLBuffers.SIZEOF_FLOAT);
            }
            gl4.glBindVertexArray(0);
        }
        gl4.glBindBuffer(GL4.GL_ARRAY_BUFFER, 0);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        System.out.println("dispose");
        System.exit(0);
    }

    @Override
    public void display(GLAutoDrawable drawable) {
//        System.out.println("display");
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            HelloTriangle.animator.stop();
            HelloTriangle.glWindow.destroy();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
