/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_320.buffer;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_BACK;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_DYNAMIC_DRAW;
import static com.jogamp.opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_FRAMEBUFFER;
import static com.jogamp.opengl.GL.GL_MAP_INVALIDATE_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_MAP_WRITE_BIT;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNSIGNED_SHORT;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import static com.jogamp.opengl.GL2ES3.GL_DEPTH;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.nio.ByteBuffer;
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

    public Gl_320_buffer_uniform() {
        super("gl-320-buffer-uniform", Profile.CORE, 3, 2);
    }

    private final String SHADERS_SOURCE = "buffer-uniform";
    private final String SHADERS_ROOT = "src/data/gl_320/buffer";

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

    private float[] projection = new float[16], model = new float[16], normal = new float[9], mv = new float[16];

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
        gl3.glEnable(GL_DEPTH_TEST);
        gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        gl3.glDrawBuffer(GL_BACK);
        if (!isFramebufferComplete(gl, 0)) {
            return false;
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

            gl3.glBindAttribLocation(programName, Semantic.Attr.POSITION, "position");
            gl3.glBindAttribLocation(programName, Semantic.Attr.NORMAL, "normal");
            gl3.glBindAttribLocation(programName, Semantic.Attr.COLOR, "color");
            gl3.glBindFragDataLocation(programName, Semantic.Frag.COLOR, "color");

            program.link(gl3, System.out);
        }
        if (validated) {
            uniformPerDraw = gl3.glGetUniformBlockIndex(programName, "PerDraw");
            uniformPerPass = gl3.glGetUniformBlockIndex(programName, "PerPass");
            uniformPerScene = gl3.glGetUniformBlockIndex(programName, "PerScene");

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
        for (Vertex_v3fn3fc4f vertex : vertexData) {
            vertexBuffer.put(vertex.toFloatArray());
        }
        vertexBuffer.rewind();
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
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 3, GL_FLOAT, false,
                    Vertex_v3fn3fc4f.sizeOf, 0);
            gl3.glVertexAttribPointer(Semantic.Attr.NORMAL, 3, GL_FLOAT, false,
                    Vertex_v3fn3fc4f.sizeOf, 3 * GLBuffers.SIZEOF_FLOAT);
            gl3.glVertexAttribPointer(Semantic.Attr.COLOR, 4, GL_FLOAT, false,
                    Vertex_v3fn3fc4f.sizeOf, (3 + 3) * GLBuffers.SIZEOF_FLOAT);
            gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl3.glEnableVertexAttribArray(Semantic.Attr.NORMAL);
            gl3.glEnableVertexAttribArray(Semantic.Attr.COLOR);

            gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.element.ordinal()]);
        }
        gl3.glBindVertexArray(0);

        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL3 gl3 = (GL3) gl;
        {
            gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.perDraw.ordinal()]);
            ByteBuffer transform = gl3.glMapBufferRange(GL_UNIFORM_BUFFER, 0, Transform.sizeOf,
                    GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            FloatUtil.makePerspective(projection, 0, true, (float) Math.PI * 0.25f, 4.0f / 3.0f, 0.1f, 100.0f);
            float[] view = view();
            FloatUtil.makeRotationAxis(model, 0, (float) -Math.PI * 0.5f, 0.0f, 0.0f, 1.0f, tmpVec);

            FloatUtil.multMatrix(view, model, mv);
            for (int c = 0; c < 3; c++) {
                for (int r = 0; r < 3; r++) {
                    normal[c * 3 + r] = view[c * 4 + r];
                }
            }
            for (int i = 0; i < view.length; i++) {
                transform.putFloat((0 + i) * Float.BYTES, projection[i]);
                transform.putFloat((projection.length + i) * Float.BYTES, mv[i]);
            }
            for (int i = 0; i < normal.length; i++) {
                transform.putFloat((projection.length + mv.length + i) * Float.BYTES, normal[i]);
            }

            gl3.glUnmapBuffer(GL_UNIFORM_BUFFER);
            gl3.glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }
        gl3.glViewport(0, 0, glWindow.getWidth(), glWindow.getHeight());
        gl3.glClearBufferfv(GL_COLOR, 0, new float[]{0.2f, 0.2f, 0.2f, 1.0f}, 0);
        gl3.glClearBufferfv(GL_DEPTH, 0, new float[]{1.0f}, 0);

        gl3.glUseProgram(programName);
        gl3.glBindBufferBase(GL_UNIFORM_BUFFER, Uniform.perScene.ordinal(), bufferName[Buffer.perScene.ordinal()]);
        gl3.glBindBufferBase(GL_UNIFORM_BUFFER, Uniform.perPass.ordinal(), bufferName[Buffer.perPass.ordinal()]);
        gl3.glBindBufferBase(GL_UNIFORM_BUFFER, Uniform.perDraw.ordinal(), bufferName[Buffer.perDraw.ordinal()]);
        gl3.glBindVertexArray(vertexArrayName[0]);

        gl3.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;

        gl3.glDeleteVertexArrays(1, vertexArrayName, 0);
        gl3.glDeleteBuffers(Buffer.max.ordinal(), bufferName, 0);
        gl3.glDeleteProgram(programName);

        return true;
    }
}
