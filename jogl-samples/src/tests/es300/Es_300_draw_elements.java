/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.es300;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_BACK;
import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_EXTENSIONS;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_RENDERER;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNSIGNED_INT;
import static com.jogamp.opengl.GL.GL_VENDOR;
import static com.jogamp.opengl.GL.GL_VERSION;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import com.jogamp.opengl.GL3ES3;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 *
 * @author elect
 */
public class Es_300_draw_elements extends Test {

    public static void main(String[] args) {
        Es_300_draw_elements es_300_draw_elements = new Es_300_draw_elements();
    }
    
    private final String SHADERS_SOURCE = "flat-color";
    private final String SHADERS_ROOT = "src/data/es_300";

    private final int elementCount = 6;
    private final int elementSize = elementCount * GLBuffers.SIZEOF_INT;
    private final int[] elementData = new int[]{
        0, 1, 2,
        0, 2, 3};

    private final int vertexCount = 4;
    private final int positionSize = vertexCount * 2 * GLBuffers.SIZEOF_FLOAT;
    private final float[] positionData = new float[]{
        -1f, -1f,
        +1f, -1f,
        +1f, +1f,
        -1f, +1f};

    private int[] vertexArrayName, arrayBufferName, elementBufferName;
    private int programName, uniformMvp, uniformDiffuse;

    public Es_300_draw_elements() {
        super("es_300_draw_elements", Profile.ES, 3, 0);
    }

    @Override
    protected boolean begin(GL gl) {

        boolean validated = true;

        GL3ES3 gl3es3 = (GL3ES3) gl;

        System.out.println("Vendor " + gl3es3.glGetString(GL_VENDOR));
        System.out.println("Renderer " + gl3es3.glGetString(GL_RENDERER));
        System.out.println("Version " + gl3es3.glGetString(GL_VERSION));
        System.out.println("Extensions " + gl3es3.glGetString(GL_EXTENSIONS));

        if (validated) {
            validated = initProgram(gl3es3);
        }
        if (validated) {
            validated = initBuffer(gl3es3);
        }
        if (validated) {
            validated = initVertexArray(gl3es3);
        }
        return validated;
    }

    private boolean initProgram(GL3ES3 gl3es3) {

        boolean validated = true;

        // Create program
        if (validated) {
            ShaderCode vertShader = ShaderCode.create(gl3es3, GL_VERTEX_SHADER, this.getClass(),
                    SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragShader = ShaderCode.create(gl3es3, GL_FRAGMENT_SHADER, this.getClass(),
                    SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

            ShaderProgram program = new ShaderProgram();
            program.add(vertShader);
            program.add(fragShader);
            
            program.init(gl3es3);

            programName = program.program();
            gl3es3.glBindAttribLocation(programName, Semantic.Attr.POSITION, "position");            
            
            program.link(gl3es3, System.out);
        }
        // Get variables locations
        if (validated) {
            uniformMvp = gl3es3.glGetUniformLocation(programName, "mvp");
            uniformDiffuse = gl3es3.glGetUniformLocation(programName, "diffuse");
        }
        // Set some variables 
        if (validated) {
            gl3es3.glUseProgram(programName);
            gl3es3.glUniform4fv(uniformDiffuse, 1, new float[]{1f, .5f, 0f, 1f}, 0);
            gl3es3.glUseProgram(0);
        }

        return validated & checkError(gl3es3, "initProgram");
    }

    private boolean initBuffer(GL3ES3 gl3es3) {
        arrayBufferName = new int[1];
        gl3es3.glGenBuffers(1, arrayBufferName, 0);
        gl3es3.glBindBuffer(GL_ARRAY_BUFFER, arrayBufferName[0]);
        FloatBuffer floatBuffer = GLBuffers.newDirectFloatBuffer(positionData);
        gl3es3.glBufferData(GL_ARRAY_BUFFER, positionSize, floatBuffer, GL_STATIC_DRAW);
        gl3es3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        elementBufferName = new int[1];
        gl3es3.glGenBuffers(1, elementBufferName, 0);
        gl3es3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, elementBufferName[0]);
        IntBuffer intBuffer = GLBuffers.newDirectIntBuffer(elementData);
        gl3es3.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, intBuffer, GL_STATIC_DRAW);
        gl3es3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        return checkError(gl3es3, "initBuffer");
    }

    private boolean initVertexArray(GL3ES3 gl3es3) {
        vertexArrayName = new int[1];
        gl3es3.glGenVertexArrays(1, vertexArrayName, 0);
        gl3es3.glBindVertexArray(vertexArrayName[0]);
        gl3es3.glBindBuffer(GL_ARRAY_BUFFER, arrayBufferName[0]);
        gl3es3.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 0, 0);
        gl3es3.glBindBuffer(GL_ARRAY_BUFFER, 0);
        gl3es3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, elementBufferName[0]);

        gl3es3.glEnableVertexAttribArray(Semantic.Attr.POSITION);
        gl3es3.glBindVertexArray(0);

        return checkError(gl3es3, "initVertexArray");
    }

    @Override
    protected boolean render(GL gl) {

        GL3ES3 gl3es3 = (GL3ES3) gl;
        
        int[] buffer = new int[]{GL_BACK};
        gl3es3.glDrawBuffers(1, buffer, 0);
        
        // Compute the MVP (Model View Projection matrix)
        float[] projection = FloatUtil.makePerspective(new float[16], 0,
                true, FloatUtil.QUARTER_PI, 4f / 3f, .1f, 100f);
        float[] model = FloatUtil.makeIdentity(new float[16]);
        float[] mvp = FloatUtil.multMatrix(projection, FloatUtil.multMatrix(view(), model));

        // Set the display viewport
        gl3es3.glViewport(0, 0, windowSize.x, windowSize.y);

        // Clear color buffer with black
        gl3es3.glClearColor(0f, 0f, 0f, 1f);
        gl3es3.glClearDepthf(1f);
        gl3es3.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        
        // Bind program
        gl3es3.glUseProgram(programName);

        // Set the value of MVP uniform.
        gl3es3.glUniformMatrix4fv(uniformMvp, 1, false, mvp, 0);

        gl3es3.glBindVertexArray(vertexArrayName[0]);
        
        gl3es3.glDrawElements(GL_TRIANGLES, elementCount, GL_UNSIGNED_INT, 0);
        
        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL3ES3 gl3es3 = (GL3ES3) gl;

        // Delete objects
        gl3es3.glDeleteBuffers(1, arrayBufferName, 0);
        gl3es3.glDeleteBuffers(1, elementBufferName, 0);
        gl3es3.glDeleteProgram(programName);

        return true;
    }
}
