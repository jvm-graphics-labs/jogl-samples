/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_320.buffer;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2ES3.*;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import glm.mat._3.Mat3;
import glm.mat._4.Mat4;
import glm.vec._3.Vec3;
import glm.vec._4.Vec4;
import framework.BufferUtils;
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

    private class Vertex_v3fn3fc4f {

        public static final int SIZEOF = 2 * Vec3.SIZE + Vec4.SIZE;
    }

    private class Transform {

        public Mat4 p;
        public Mat4 mv;
        public Mat3 normal;

        public static final int SIZE = 2 * Mat4.SIZE + Mat3.SIZE;

        public Transform() {
        }

        public float[] toFa_() {
            return new float[]{
                p.m00, p.m01, p.m02, p.m03, p.m10, p.m11, p.m12, p.m13,
                p.m20, p.m21, p.m22, p.m23, p.m30, p.m31, p.m32, p.m33,
                mv.m00, mv.m01, mv.m02, mv.m03, mv.m10, mv.m11, mv.m12, mv.m13,
                mv.m20, mv.m21, mv.m22, mv.m23, mv.m30, mv.m31, mv.m32, mv.m33,
                normal.m00, normal.m01, normal.m02, normal.m10, normal.m11, normal.m12,
                normal.m20, normal.m21, normal.m22,};
        }
    };

    private class Light {

        public Vec3 position;
        public static final int SIZEOF = Vec3.SIZE;

        public Light(Vec3 position) {
            this.position = position;
        }

        public float[] toFa_() {
            return position.toFA_();
        }
    }

    private class Material {

        public Vec3 ambient;
        public float padding1;
        public Vec3 diffuse;
        public float padding2;
        public Vec3 specular;
        public float shininess;
        public static final int SIZEOF = 3 * Vec3.SIZE + 3 * Float.BYTES;

        public Material(Vec3 ambient, Vec3 diffuse, Vec3 specular, float shininess) {
            this.ambient = ambient;
            this.diffuse = diffuse;
            this.specular = specular;
            this.shininess = shininess;
        }

        public float[] toFloatArray() {
            float[] floatArray = new float[]{
                ambient.x, ambient.y, ambient.z,
                padding1,
                diffuse.x, diffuse.y, diffuse.z,
                padding2,
                specular.x, specular.y, specular.z,
                shininess};
            return floatArray;
        }
    }

    private int vertexCount = 4;
    private int vertexSize = vertexCount * Vertex_v3fn3fc4f.SIZEOF;
    private float[] vertexData = new float[]{
        -1.0f, -1.0f, 0.0f,/**/ 0.0f, 0.0f, 1.0f,/**/ 1.0f, 0.0f, 0.0f, 1.0f,
        +1.0f, -1.0f, 0.0f,/**/ 0.0f, 0.0f, 1.0f,/**/ 0.0f, 1.0f, 0.0f, 1.0f,
        +1.0f, +1.0f, 0.0f,/**/ 0.0f, 0.0f, 1.0f,/**/ 0.0f, 0.0f, 1.0f, 1.0f,
        -1.0f, +1.0f, 0.0f,/**/ 0.0f, 0.0f, 1.0f,/**/ 1.0f, 1.0f, 1.0f, 1.0f};

    private int elementCount = 6;
    private int elementSize = elementCount * Short.BYTES;
    private short[] elementData = new short[]{
        0, 1, 2,
        2, 3, 0
    };

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int ELEMENT = 1;
        public static final int PER_SCENE = 2;
        public static final int PER_PASS = 3;
        public static final int PER_DRAW = 4;
        public static final int MAX = 5;
    }

    private class Uniform {

        public static final int PER_SCENE = 0;
        public static final int PER_PASS = 1;
        public static final int PER_DRAW = 2;
        public static final int LIGHT = 3;
    }

    private int programName, uniformPerDraw, uniformPerPass, uniformPerScene;
    private int[] vertexArrayName = {0}, bufferName = new int[Buffer.MAX];

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

            gl3.glUniformBlockBinding(programName, uniformPerDraw, Uniform.PER_DRAW);
            gl3.glUniformBlockBinding(programName, uniformPerPass, Uniform.PER_PASS);
            gl3.glUniformBlockBinding(programName, uniformPerScene, Uniform.PER_SCENE);
        }

        return validated & checkError(gl3, "initProgram");
    }

    private boolean initBuffer(GL3 gl3) {

        gl3.glGenBuffers(Buffer.MAX, bufferName, 0);

        gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT]);
        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        gl3.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(elementBuffer);
        gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl3.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer.rewind(), GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(vertexBuffer);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        {
            gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.PER_DRAW]);
            gl3.glBufferData(GL_UNIFORM_BUFFER, Transform.SIZE, null, GL_DYNAMIC_DRAW);
            gl3.glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }

        {
            Light light = new Light(new Vec3(0.0f, 0.0f, 100.f));

            gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.PER_PASS]);
            FloatBuffer lightBuffer = GLBuffers.newDirectFloatBuffer(light.toFa_());
            gl3.glBufferData(GL_UNIFORM_BUFFER, Light.SIZEOF, lightBuffer, GL_STATIC_DRAW);
            BufferUtils.destroyDirectBuffer(lightBuffer);
            gl3.glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }

        {
            Material material = new Material(new Vec3(0.7f, 0.0f, 0.0f), new Vec3(0.0f, 0.5f, 0.0f),
                    new Vec3(0.0f, 0.0f, 0.5f), 128.0f);

            gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.PER_SCENE]);
            FloatBuffer materialBuffer = GLBuffers.newDirectFloatBuffer(material.toFloatArray());
            gl3.glBufferData(GL_UNIFORM_BUFFER, Material.SIZEOF, materialBuffer, GL_STATIC_DRAW);
            BufferUtils.destroyDirectBuffer(materialBuffer);
            gl3.glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }
        return true;
    }

    private boolean initVertexArray(GL3 gl3) {

        gl3.glGenVertexArrays(1, vertexArrayName, 0);
        gl3.glBindVertexArray(vertexArrayName[0]);
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 3, GL_FLOAT, false, Vertex_v3fn3fc4f.SIZEOF, 0);
            gl3.glVertexAttribPointer(Semantic.Attr.NORMAL, 3, GL_FLOAT, false, Vertex_v3fn3fc4f.SIZEOF, Vec3.SIZE);
            gl3.glVertexAttribPointer(Semantic.Attr.COLOR, 4, GL_FLOAT, false, Vertex_v3fn3fc4f.SIZEOF, 2 * Vec3.SIZE);
            gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl3.glEnableVertexAttribArray(Semantic.Attr.NORMAL);
            gl3.glEnableVertexAttribArray(Semantic.Attr.COLOR);

            gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT]);
        }
        gl3.glBindVertexArray(0);

        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL3 gl3 = (GL3) gl;
        {
            gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.PER_DRAW]);
            ByteBuffer transform = gl3.glMapBufferRange(GL_UNIFORM_BUFFER, 0, Transform.SIZE,
                    GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            Mat4 projection = new Mat4().perspective((float) Math.PI * 0.25f, 4.0f / 3.0f, 0.1f, 100.0f);
            Mat4 view = viewMat4();
            Mat4 model = new Mat4(1.0f).rotate((float) -Math.PI * 0.5f, 0.0f, 0.0f, 1.0f);

            Transform t = new Transform();
            t.mv = view.mul(model);
            t.p = projection;
            t.normal = new Mat3(t.mv.invTransp3_());

            transform.asFloatBuffer().put(t.toFa_());

            gl3.glUnmapBuffer(GL_UNIFORM_BUFFER);
            gl3.glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }
        gl3.glViewport(0, 0, windowSize.x, windowSize.y);
        gl3.glClearBufferfv(GL_COLOR, 0, new float[]{0.2f, 0.2f, 0.2f, 1.0f}, 0);
        gl3.glClearBufferfv(GL_DEPTH, 0, new float[]{1.0f}, 0);

        gl3.glUseProgram(programName);
        gl3.glBindBufferBase(GL_UNIFORM_BUFFER, Uniform.PER_SCENE, bufferName[Buffer.PER_SCENE]);
        gl3.glBindBufferBase(GL_UNIFORM_BUFFER, Uniform.PER_PASS, bufferName[Buffer.PER_PASS]);
        gl3.glBindBufferBase(GL_UNIFORM_BUFFER, Uniform.PER_DRAW, bufferName[Buffer.PER_DRAW]);
        gl3.glBindVertexArray(vertexArrayName[0]);

        gl3.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;

        gl3.glDeleteVertexArrays(1, vertexArrayName, 0);
        gl3.glDeleteBuffers(Buffer.MAX, bufferName, 0);
        gl3.glDeleteProgram(programName);

        return true;
    }
}
