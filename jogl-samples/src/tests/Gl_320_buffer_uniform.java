/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_DYNAMIC_DRAW;
import static com.jogamp.opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FALSE;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.Semantic;
import framework.Test;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 *
 * @author gbarbieri
 */
public class Gl_320_buffer_uniform extends Test {

    public static void main(String[] args) {
        Gl_320_buffer_uniform gl_320_buffer_uniform = new Gl_320_buffer_uniform();
    }

    private final String SHADERS_SOURCE = "buffer-uniform-shared";
    private final String SHADERS_ROOT = "src/data/gl_320";

    public Gl_320_buffer_uniform() {
        super("gl-320-buffer-uniform", 3, 2);
    }

    public class Vertex_v3fn3fc4f {

        public float[] position;
        public float[] normal;
        public float[] color;
        public static final int sizeOf = (3 + 3 + 4) * GLBuffers.SIZEOF_FLOAT;

        public Vertex_v3fn3fc4f(float[] position, float[] normal, float[] color) {
            this.position = position;
            this.normal = normal;
            this.color = color;
        }

        public float[] toFloatArray() {
            float[] floatArray = new float[3 + 3 + 4];
            System.arraycopy(position, 0, floatArray, 0, position.length);
            System.arraycopy(normal, 0, floatArray, position.length, normal.length);
            System.arraycopy(color, 0, floatArray, position.length + normal.length, color.length);
            return floatArray;
        }
    }

    public class Transform {

        public float[] p;
        public float[] mv;
        public float[] normal;

        public static final int sizeOf = (16 + 16 + 9) * GLBuffers.SIZEOF_FLOAT;
    }

    public class Light {

        public float[] position;
        public static final int sizeOf = 3 * GLBuffers.SIZEOF_FLOAT;

        public Light(float[] position) {
            this.position = position;
        }

        public float[] toFloatArray() {
            return position;
        }
    }

    public class Material {

        public float[] ambient;
        public float padding1;
        public float[] diffuse;
        public float padding2;
        public float[] specular;
        public float shininess;
        public static final int sizeOf = (3 + 1 + 3 + 1 + 3 + 1) * GLBuffers.SIZEOF_FLOAT;

        public Material(float[] ambient, float[] diffuse, float[] specular, float shininess) {
            this.ambient = ambient;
            this.diffuse = diffuse;
            this.specular = specular;
            this.shininess = shininess;
        }

        public float[] toFloatArray() {
            float[] floatArray = new float[3 + 1 + 3 + 1 + 3 + 1];
            System.arraycopy(ambient, 0, floatArray, 0, 3);
            System.arraycopy(diffuse, 0, floatArray, 4, 3);
            System.arraycopy(specular, 0, floatArray, 8, 3);
            floatArray[11] = shininess;
            return floatArray;
        }
    }

    private int vertexCount = 4;
    private int vertexSize = vertexCount * Vertex_v3fn3fc4f.sizeOf;
    private Vertex_v3fn3fc4f[] vertexData = new Vertex_v3fn3fc4f[]{
        new Vertex_v3fn3fc4f(new float[]{-1.0f, -1.0f, 0.0f}, new float[]{0.0f, 0.0f, 1.0f}, new float[]{1.0f, 0.0f, 0.0f, 1.0f}),
        new Vertex_v3fn3fc4f(new float[]{+1.0f, -1.0f, 0.0f}, new float[]{0.0f, 0.0f, 1.0f}, new float[]{0.0f, 1.0f, 0.0f, 1.0f}),
        new Vertex_v3fn3fc4f(new float[]{+1.0f, +1.0f, 0.0f}, new float[]{0.0f, 0.0f, 1.0f}, new float[]{0.0f, 0.0f, 1.0f, 1.0f}),
        new Vertex_v3fn3fc4f(new float[]{-1.0f, +1.0f, 0.0f}, new float[]{0.0f, 0.0f, 1.0f}, new float[]{1.0f, 1.0f, 1.0f, 1.0f})
    };
    private int elementCount = 6;
    private int elementSize = elementCount * GLBuffers.SIZEOF_SHORT;
    private short[] elementData = new short[]{
        0, 1, 2,
        2, 3, 0
    };
    private int programName, uniformPerDraw, uniformPerPass, uniformPerScene;
    private int[] vertexArrayName, bufferName;

    private enum Uniform {

        perScene, perPass, perDraw, light
    }

    private enum Buffer {

        vertex, element, perScene, perPass, perDraw, max
    }

    @Override
    protected boolean begin(GL gl) {

        boolean validated = true;

        GL3 gl3 = (GL3) gl;

        if (validated) {
            validated = initProgram(gl3);
        }
        if (validated) {
            validated = initBuffer(gl3);
        }
        if (validated) {
            validated = initVertexArray(gl3);
        }
        return validated;
    }

    private boolean initProgram(GL3 gl3) {

        boolean validated = true;

        if (validated) {
            ShaderCode vertShader = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragShader = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

            ShaderProgram program = new ShaderProgram();
            program.add(vertShader);
            program.add(fragShader);

            program.init(gl3);

            programName = program.program();

            gl3.glBindAttribLocation(programName, Semantic.Attr.position, "position");
            gl3.glBindAttribLocation(programName, Semantic.Attr.normal, "normal");
            gl3.glBindAttribLocation(programName, Semantic.Attr.color, "color");
            gl3.glBindFragDataLocation(programName, Semantic.Frag.color, "color");

            program.link(gl3, System.out);
        }
        if (validated) {
            uniformPerDraw = gl3.glGetUniformBlockIndex(programName, "perDraw");
            uniformPerPass = gl3.glGetUniformBlockIndex(programName, "perPass");
            uniformPerScene = gl3.glGetUniformBlockIndex(programName, "perScene");

            gl3.glUniformBlockBinding(programName, uniformPerDraw, Uniform.perDraw.ordinal());
            gl3.glUniformBlockBinding(programName, uniformPerPass, Uniform.perPass.ordinal());
            gl3.glUniformBlockBinding(programName, uniformPerScene, Uniform.perScene.ordinal());
        }

        return validated & checkError(gl3, "initProgram");
    }

    private boolean initBuffer(GL3 gl3) {
        bufferName = new int[Buffer.max.ordinal()];

        gl3.glGenBuffers(Buffer.max.ordinal(), bufferName, 0);

        gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.element.ordinal()]);
        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        gl3.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.vertex.ordinal()]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexSize);
        for (int v = 0; v < vertexCount; v++) {
            vertexBuffer.put(vertexData[v].toFloatArray());
        }
        gl3.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        {
            gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.perDraw.ordinal()]);
            gl3.glBufferData(GL_UNIFORM_BUFFER, Transform.sizeOf, null, GL_DYNAMIC_DRAW);
            gl3.glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }

        {
            Light light = new Light(new float[]{0.0f, 0.0f, 100.f});

            gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.perPass.ordinal()]);
            FloatBuffer lightBuffer = GLBuffers.newDirectFloatBuffer(light.toFloatArray());
            gl3.glBufferData(GL_UNIFORM_BUFFER, Light.sizeOf, lightBuffer, GL_STATIC_DRAW);
            gl3.glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }

        {
            Material material = new Material(new float[]{0.7f, 0.0f, 0.0f},
                    new float[]{0.0f, 0.5f, 0.0f}, new float[]{0.0f, 0.0f, 0.5f}, 128.0f);

            gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.perScene.ordinal()]);
            FloatBuffer materialBuffer = GLBuffers.newDirectFloatBuffer(material.toFloatArray());
            gl3.glBufferData(GL_UNIFORM_BUFFER, Material.sizeOf, materialBuffer, GL_STATIC_DRAW);
            gl3.glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }
        return true;
    }

    private boolean initVertexArray(GL3 gl3) {

        vertexArrayName = new int[1];
        gl3.glGenVertexArrays(1, vertexArrayName, 0);
        gl3.glBindVertexArray(vertexArrayName[0]);
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.vertex.ordinal()]);
            gl3.glVertexAttribPointer(Semantic.Attr.position, 3, GL_FLOAT,
                    false, Vertex_v3fn3fc4f.sizeOf, 0);
            gl3.glVertexAttribPointer(Semantic.Attr.normal, 3, GL_FLOAT,
                    false, Vertex_v3fn3fc4f.sizeOf, 3 * GLBuffers.SIZEOF_FLOAT);
            gl3.glVertexAttribPointer(Semantic.Attr.color, 4, GL_FLOAT,
                    false, Vertex_v3fn3fc4f.sizeOf, (3 + 3) * GLBuffers.SIZEOF_FLOAT);
            gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl3.glEnableVertexAttribArray(Semantic.Attr.position);
            gl3.glEnableVertexAttribArray(Semantic.Attr.normal);
            gl3.glEnableVertexAttribArray(Semantic.Attr.color);

            gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.element.ordinal()]);
        }
        gl3.glBindVertexArray(0);

        return true;
    }
}
