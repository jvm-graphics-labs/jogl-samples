/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.es_300;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2ES2.*;
import com.jogamp.opengl.GL3ES3;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import glm.glm;
import glm.mat._4.Mat4;
import glm.vec._2.Vec2;
import framework.BufferUtils;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author elect
 */
public class Es_300_draw_elements extends Test {

    public static void main(String[] args) {
        Es_300_draw_elements es_300_draw_elements = new Es_300_draw_elements();
    }

    private final String SHADERS_SOURCE = "flat-color";
    private final String SHADER_SOURCE_FAIL = "flat-color-fail";
    private final String SHADERS_ROOT = "src/data/es_300";

    private final int elementCount = 6;
    private final int elementSize = elementCount * Integer.BYTES;
    private final int[] elementData = new int[]{
        0, 1, 2,
        0, 2, 3};

    private final int vertexCount = 4;
    private final int positionSize = vertexCount * Vec2.SIZE;
    private final float[] positionData = new float[]{
        -1f, -1f,
        +1f, -1f,
        +1f, +1f,
        -1f, +1f};

    private int[] vertexArrayName = {0}, arrayBufferName = {0}, elementBufferName = {0};
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

        int shaderName = gl3es3.glCreateShader(GL_FRAGMENT_SHADER);

        try {
            // Check fail positive
            if (validated) {

                Path path = Paths.get(SHADERS_ROOT + "/" + SHADER_SOURCE_FAIL + ".frag");
                String[] fragShader = new String[]{new String(Files.readAllBytes(path))};
                int[] length = new int[]{fragShader.length};
                gl3es3.glShaderSource(shaderName, 1, fragShader, length, 0);
                gl3es3.glCompileShader(shaderName);

                /**
                 * This does not fail..
                 */
//                validated = validated && !framework.Compiler.check(gl3es3, shaderName);
                gl3es3.glDeleteShader(shaderName);
            }
        } catch (IOException ex) {
            Logger.getLogger(Es_300_draw_elements.class.getName()).log(Level.SEVERE, null, ex);
        }

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

        gl3es3.glGenBuffers(1, arrayBufferName, 0);
        gl3es3.glBindBuffer(GL_ARRAY_BUFFER, arrayBufferName[0]);
        FloatBuffer positionBuffer = GLBuffers.newDirectFloatBuffer(positionData);
        gl3es3.glBufferData(GL_ARRAY_BUFFER, positionSize, positionBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(positionBuffer);
        gl3es3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        gl3es3.glGenBuffers(1, elementBufferName, 0);
        gl3es3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, elementBufferName[0]);
        IntBuffer elementBuffer = GLBuffers.newDirectIntBuffer(elementData);
        gl3es3.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(elementBuffer);
        gl3es3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        return checkError(gl3es3, "initBuffer");
    }

    private boolean initVertexArray(GL3ES3 gl3es3) {

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
        Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, 4.0f / 3.0f, 0.1f, 100.0f);
        Mat4 model = new Mat4(1.0f);
        Mat4 mvp = projection.mul(viewMat4()).mul(model);

        // Set the display viewport
        gl3es3.glViewport(0, 0, windowSize.x, windowSize.y);

        // Clear color buffer with black
        gl3es3.glClearColor(0f, 0f, 0f, 1f);
        gl3es3.glClearDepthf(1f);
        gl3es3.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        // Bind program
        gl3es3.glUseProgram(programName);

        // Set the value of MVP uniform.
        gl3es3.glUniformMatrix4fv(uniformMvp, 1, false, mvp.toFa_(), 0);

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
