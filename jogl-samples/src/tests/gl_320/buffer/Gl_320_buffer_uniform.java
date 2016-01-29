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
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import core.glm;
import dev.Mat3;
import dev.Mat4;
import dev.Vec3;
import dev.Vec4;
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

        public static final int SIZEOF = 2 * Vec3.SIZEOF + Vec4.SIZEOF;
    }

    private class Transform {

        public Mat4 p;
        public Mat4 mv;
        public Mat3 normal;

        public static final int SIZEOF = 2 * Mat4.SIZEOF + Mat3.SIZEOF;
    }

    private class Light {

        public Vec3 position;
        public static final int SIZEOF = Vec3.SIZEOF;

        public Light(Vec3 position) {
            this.position = position;
        }

        public float[] toFloatArray() {
            return position.toFA(new float[3]);
        }
    }

    private class Material {

        public Vec3 ambient;
        public float padding1;
        public Vec3 diffuse;
        public float padding2;
        public Vec3 specular;
        public float shininess;
        public static final int SIZEOF = 3 * Vec3.SIZEOF + 3 * Float.BYTES;

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
        BufferUtils.destroyDirectBuffer(elementBuffer);
        gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.vertex.ordinal()]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl3.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer.rewind(), GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(vertexBuffer);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        {
            gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.perDraw.ordinal()]);
            gl3.glBufferData(GL_UNIFORM_BUFFER, Transform.SIZEOF, null, GL_DYNAMIC_DRAW);
            gl3.glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }

        {
            Light light = new Light(new Vec3(0.0f, 0.0f, 100.f));

            gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.perPass.ordinal()]);
            FloatBuffer lightBuffer = GLBuffers.newDirectFloatBuffer(light.toFloatArray());
            gl3.glBufferData(GL_UNIFORM_BUFFER, Light.SIZEOF, lightBuffer, GL_STATIC_DRAW);
            BufferUtils.destroyDirectBuffer(lightBuffer);
            gl3.glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }

        {
            Material material = new Material(new Vec3(0.7f, 0.0f, 0.0f), new Vec3(0.0f, 0.5f, 0.0f),
                    new Vec3(0.0f, 0.0f, 0.5f), 128.0f);

            gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.perScene.ordinal()]);
            FloatBuffer materialBuffer = GLBuffers.newDirectFloatBuffer(material.toFloatArray());
            gl3.glBufferData(GL_UNIFORM_BUFFER, Material.SIZEOF, materialBuffer, GL_STATIC_DRAW);
            BufferUtils.destroyDirectBuffer(materialBuffer);
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
                    Vertex_v3fn3fc4f.SIZEOF, 0);
            gl3.glVertexAttribPointer(Semantic.Attr.NORMAL, 3, GL_FLOAT, false,
                    Vertex_v3fn3fc4f.SIZEOF, Vec3.SIZEOF);
            gl3.glVertexAttribPointer(Semantic.Attr.COLOR, 4, GL_FLOAT, false,
                    Vertex_v3fn3fc4f.SIZEOF, 2 * Vec3.SIZEOF);
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
            ByteBuffer transform = gl3.glMapBufferRange(GL_UNIFORM_BUFFER, 0, Transform.SIZEOF,
                    GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            Mat4 projection = new Mat4().perspective((float) Math.PI * 0.25f, 4.0f / 3.0f, 0.1f, 100.0f);
            Mat4 view = viewMat4();
            Mat4 model = new Mat4(1.0f).rotate((float) -Math.PI * 0.5f, 0.0f, 0.0f, 1.0f);

            Mat4 mv = view.mul(model);
            Mat4 p = projection;
            Mat3 normal = new Mat3(mv.invTransp(new Mat4()));

            transform.asFloatBuffer()
                    .put(p.toFA_())
                    .put(mv.toFA_())
                    .put(normal.toFA_());

            gl3.glUnmapBuffer(GL_UNIFORM_BUFFER);
            gl3.glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }
        gl3.glViewport(0, 0, windowSize.x, windowSize.y);
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
