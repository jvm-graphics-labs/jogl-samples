/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_EXTENSIONS;
import static com.jogamp.opengl.GL.GL_RENDERER;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_VENDOR;
import static com.jogamp.opengl.GL.GL_VERSION;
import com.jogamp.opengl.GL2ES2;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.Semantic;
import framework.Test;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 *
 * @author elect
 */
public class es_200_draw_elements extends Test {

    private final String VERT_SHADER = "flat-color";
    private final String FRAG_SHADER = "flat-color";
    private int elementCount = 6;
    private int elementSize = elementCount * GLBuffers.SIZEOF_SHORT;
    private short[] elementData = new short[]{
        0, 1, 2,
        0, 2, 3
    };
    private int vertexCount = 4;
    private int positionSize = vertexCount * 2 * GLBuffers.SIZEOF_FLOAT;
    private float[] positionData = new float[]{
        -1f, -1f,
        +1f, -1f,
        +1f, +1f,
        -1f, +1f
    };

    private enum Buffer {

        vertex,
        element,
        max
    }

    private int[] bufferName = new int[Buffer.max.ordinal()];
    private int programName, uniformMvp, uniformDiffuse;

    public es_200_draw_elements(String title, int majorVersionRequire, int minorVersionRequire) {
        super(title, majorVersionRequire, minorVersionRequire);
    }

    @Override
    protected boolean begin(GL gl) {

        GL2ES2 gl2es2 = (GL2ES2) gl;

        boolean validated = true;

        System.out.println("Vendor " + gl2es2.glGetString(GL_VENDOR));
        System.out.println("Renderer " + gl2es2.glGetString(GL_RENDERER));
        System.out.println("Version " + gl2es2.glGetString(GL_VERSION));
        System.out.println("Extensions " + gl2es2.glGetString(GL_EXTENSIONS));

        if (validated) {
            validated = initProgram(gl2es2);
        }
        if(validated) {
            validated = initBuffer(gl2es2);
        }

        return validated;
    }

    private boolean initProgram(GL2ES2 gl2es2) {

        boolean validated = true;

        if (validated) {
            ShaderCode vertShader = ShaderCode.create(gl2es2, GL_VERTEX_SHADER, this.getClass(),
                    getDataDirectory() + "es_200", getDataDirectory() + "es_200/bin", VERT_SHADER, true);
            ShaderCode fragShader = ShaderCode.create(gl2es2, GL_FRAGMENT_SHADER, this.getClass(),
                    getDataDirectory() + "es_200", getDataDirectory() + "es_200/bin", FRAG_SHADER, true);

            ShaderProgram program = new ShaderProgram();
            program.add(vertShader);
            program.add(fragShader);

            program.link(gl2es2, System.out);

            programName = program.program();

            gl2es2.glBindAttribLocation(programName, Semantic.Attr.position, "position");
        }
        if (validated) {
            uniformMvp = gl2es2.glGetUniformLocation(programName, "mvp");
            uniformDiffuse = gl2es2.glGetUniformLocation(programName, "diffuse");
        }
        if (validated) {
            gl2es2.glUseProgram(programName);
            gl2es2.glUniform4fv(uniformDiffuse, 1, new float[]{1f, .5f, 0f, 1f}, 0);
            gl2es2.glUseProgram(0);
        }
        return validated & checkError(gl2es2, "initProgram");
    }

    private boolean initBuffer(GL2ES2 gl2es2) {
        gl2es2.glGenBuffers(bufferName.length, bufferName, 0);

        gl2es2.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.vertex.ordinal()]);
        FloatBuffer floatBuffer = GLBuffers.newDirectFloatBuffer(positionData);
        gl2es2.glBufferData(GL_ARRAY_BUFFER, positionSize, floatBuffer, GL_STATIC_DRAW);
        gl2es2.glBindBuffer(GL_ARRAY_BUFFER, 0);
        
        gl2es2.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.element.ordinal()]);
        ShortBuffer shortBuffer = GLBuffers.newDirectShortBuffer(elementData);
        gl2es2.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, shortBuffer, GL_STATIC_DRAW);
        gl2es2.glBindBuffer(GL_ARRAY_BUFFER, 0);
        
        return checkError(gl2es2, "initBuffer");
    }
    
    @Override
    protected boolean end(GL gl) {
        GL2ES2 gl2es2 = (GL2ES2) gl;
        
        gl2es2.glDeleteBuffers(bufferName.length, bufferName, 0);
        gl2es2.glDeleteProgram(programName);
        
        return true;
    }
    
    @Override
    protected boolean render(GL gl){
        
    }
}
