/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_430;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import glm.glm;
import glm.mat._3.Mat3;
import glm.mat._4.Mat4;
import glm.vec._3.Vec3;
import framework.BufferUtils;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import glm.vec._4.Vec4;
import glm.vec._4.funcCommon;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 *
 * @author GBarbieri
 */
public class Gl_430_buffer_uniform extends Test {

    public static void main(String[] args) {
        Gl_430_buffer_uniform gl_430_buffer_uniform = new Gl_430_buffer_uniform();
    }

    public Gl_430_buffer_uniform() {
        super("gl-430-buffer-uniform", Profile.CORE, 4, 3);
    }

    private final String SHADERS_SOURCE = "buffer-uniform";
    private final String SHADERS_ROOT = "src/data/gl_430";

    private int vertexCount = 4;
    private int vertexSize = vertexCount * glf.Vertex_v3fn3fc4f.SIZE;
    public float[] vertexData = {
        -1.0f, -1.0f, 0.0f,/**/ 0.0f, 0.0f, 1.0f,/**/ 1.0f, 0.0f, 0.0f, 1.0f,
        +1.0f, -1.0f, 0.0f,/**/ 0.0f, 0.0f, 1.0f,/**/ 0.0f, 1.0f, 0.0f, 1.0f,
        +1.0f, +1.0f, 0.0f,/**/ 0.0f, 0.0f, 1.0f,/**/ 0.0f, 0.0f, 1.0f, 1.0f,
        -1.0f, +1.0f, 0.0f,/**/ 0.0f, 0.0f, 1.0f,/**/ 1.0f, 1.0f, 1.0f, 1.0f};

    private int elementCount = 6;
    private int elementSize = elementCount * Short.BYTES;
    private short[] elementData = {
        0, 1, 2,
        2, 3, 0};

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
    };

    private class Material {

        public Vec3 ambient;
        private final float padding1 = 0.0f;
        public Vec3 diffuse;
        private final float padding2 = 0.0f;
        public Vec3 specular;
        public float shininess;

        public Material(Vec3 ambient, Vec3 diffuse, Vec3 specular, float shininess) {
            this.ambient = ambient;
            this.diffuse = diffuse;
            this.specular = specular;
            this.shininess = shininess;
        }

        public static final int SIZE = (Vec3.SIZE + 1 * Float.BYTES) * 3;

        public ByteBuffer toDbb(ByteBuffer dbb) {
            ambient.toDbb(dbb, 0);
            dbb.putFloat(Vec3.SIZE, padding1);
            diffuse.toDbb(dbb, Vec4.SIZE);
            dbb.putFloat(Vec4.SIZE + Vec3.SIZE, padding2);
            specular.toDbb(dbb, Vec4.SIZE * 2);
            dbb.putFloat(Vec4.SIZE * 2 + Vec3.SIZE, shininess);
            return dbb;
        }
    };

    private class Light {

        public Vec3 position;

        public Light(Vec3 position) {
            this.position = position;
        }

        public static final int SIZE = Vec3.SIZE;

        public ByteBuffer toDbb(ByteBuffer dbb) {
            return position.toDbb(dbb);
        }
    };

    private class Transform {

        public Mat4 p;
        public Mat4 mv;
        public Mat3 normal;

        public static final int SIZE = 2 * Mat4.SIZE + Mat3.SIZE;

        public Transform() {
        }

        public ByteBuffer toDbb(ByteBuffer dbb) {
            p.toDbb(dbb, 0);
            mv.toDbb(dbb, Mat4.SIZE);
            normal.toDbb(dbb, Mat4.SIZE * 2);
            return dbb;
        }
    };

    private IntBuffer vertexArrayName = GLBuffers.newDirectIntBuffer(1),
            bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX);
    private int programName, uniformPerDraw, uniformPerPass, uniformPerScene;

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;

        if (validated) {
            validated = initProgram(gl4);
        }
        if (validated) {
            validated = initBuffer(gl4);
        }
        if (validated) {
            validated = initVertexArray(gl4);
        }

        gl4.glEnable(GL_DEPTH_TEST);
        gl4.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        gl4.glDrawBuffer(GL_BACK);
        if (!isFramebufferComplete(gl, 0)) {
            return false;
        }

        return validated;
    }

    private String getTypeString(int type) {

        switch (type) {
            case GL_INT:
                return "int";
            case GL_INT_VEC2:
                return "ivec2";
            case GL_INT_VEC3:
                return "ivec3";
            case GL_INT_VEC4:
                return "ivec4";

            case GL_UNSIGNED_INT:
                return "uint";
            case GL_UNSIGNED_INT_VEC2:
                return "uvec2";
            case GL_UNSIGNED_INT_VEC3:
                return "uvec3";
            case GL_UNSIGNED_INT_VEC4:
                return "uvec4";

            case GL_BOOL:
                return "bool";
            case GL_BOOL_VEC2:
                return "bvec2";
            case GL_BOOL_VEC3:
                return "bvec3";
            case GL_BOOL_VEC4:
                return "bvec4";

            case GL_FLOAT:
                return "float";
            case GL_FLOAT_VEC2:
                return "vec2";
            case GL_FLOAT_VEC3:
                return "vec3";
            case GL_FLOAT_VEC4:
                return "vec4";
            case GL_FLOAT_MAT2:
                return "mat2";
            case GL_FLOAT_MAT3:
                return "mat3";
            case GL_FLOAT_MAT4:
                return "mat4";
            case GL_FLOAT_MAT2x3:
                return "mat2x3";
            case GL_FLOAT_MAT2x4:
                return "mat2x4";
            case GL_FLOAT_MAT3x2:
                return "mat3x2";
            case GL_FLOAT_MAT3x4:
                return "mat3x4";
            case GL_FLOAT_MAT4x2:
                return "mat4x2";
            case GL_FLOAT_MAT4x3:
                return "mat4x3";

            case GL_DOUBLE:
                return "double";
            case GL_DOUBLE_VEC2:
                return "dvec2";
            case GL_DOUBLE_VEC3:
                return "dvec3";
            case GL_DOUBLE_VEC4:
                return "dvec4";
            case GL_DOUBLE_MAT2:
                return "dmat2";
            case GL_DOUBLE_MAT3:
                return "dmat3";
            case GL_DOUBLE_MAT4:
                return "dmat4";
            case GL_DOUBLE_MAT2x3:
                return "dmat2x3";
            case GL_DOUBLE_MAT2x4:
                return "dmat2x4";
            case GL_DOUBLE_MAT3x2:
                return "dmat3x2";
            case GL_DOUBLE_MAT3x4:
                return "dmat3x4";
            case GL_DOUBLE_MAT4x2:
                return "dmat4x2";
            case GL_DOUBLE_MAT4x3:
                return "dmat4x3";

            default:
                return "unknown";
        }
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        // Create program
        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER, this.getClass(), SHADERS_ROOT, null,
                    SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER, this.getClass(), SHADERS_ROOT, null,
                    SHADERS_SOURCE, "frag", null, true);

            shaderProgram.init(gl4);
            programName = shaderProgram.program();

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);

            gl4.glBindAttribLocation(programName, Semantic.Attr.POSITION, "position");
            gl4.glBindAttribLocation(programName, Semantic.Attr.NORMAL, "normal");
            gl4.glBindAttribLocation(programName, Semantic.Attr.COLOR, "color");
            gl4.glBindFragDataLocation(programName, Semantic.Frag.COLOR, "color");

            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            uniformPerDraw = gl4.glGetUniformBlockIndex(programName, "PerDraw");
            uniformPerPass = gl4.glGetUniformBlockIndex(programName, "PerPass");
            uniformPerScene = gl4.glGetUniformBlockIndex(programName, "PerScene");

            gl4.glUniformBlockBinding(programName, uniformPerDraw, Uniform.PER_DRAW);
            gl4.glUniformBlockBinding(programName, uniformPerPass, Uniform.PER_PASS);
            gl4.glUniformBlockBinding(programName, uniformPerPass, Uniform.PER_SCENE);
        }
        {
            IntBuffer maxNameLength = GLBuffers.newDirectIntBuffer(1);
            gl4.glGetProgramInterfaceiv(programName, GL_UNIFORM_BLOCK, GL_MAX_NAME_LENGTH, maxNameLength);
            ByteBuffer name = GLBuffers.newDirectByteBuffer(maxNameLength.get(0) + 1);

            IntBuffer activeResources = GLBuffers.newDirectIntBuffer(1);
            gl4.glGetProgramInterfaceiv(programName, GL_UNIFORM_BLOCK, GL_ACTIVE_RESOURCES, activeResources);

            class Property {

                public static final int binding = 0;
                public static final int dataSize = 1;
                public static final int numActiveVariables = 2;
//                GLint VertexShader;
//                GLint ControlShader;
//                GLint Evaluationhader;
//                GLint GeometryShader;
//                GLint FragmentShader;
//                GLint ComputeShader;
            };

            for (int resourceIndex = 0; resourceIndex < activeResources.get(0); ++resourceIndex) {
                gl4.glGetProgramResourceName(programName, GL_UNIFORM_BLOCK, resourceIndex, name.capacity(), null, name);

                IntBuffer properties = GLBuffers.newDirectIntBuffer(new int[]{
                    GL_BUFFER_BINDING,
                    GL_BUFFER_DATA_SIZE,
                    GL_NUM_ACTIVE_VARIABLES,
                    GL_REFERENCED_BY_VERTEX_SHADER,
                    GL_REFERENCED_BY_TESS_CONTROL_SHADER,
                    GL_REFERENCED_BY_TESS_EVALUATION_SHADER,
                    GL_REFERENCED_BY_GEOMETRY_SHADER,
                    GL_REFERENCED_BY_FRAGMENT_SHADER,
                    GL_REFERENCED_BY_COMPUTE_SHADER});

                IntBuffer params = GLBuffers.newDirectIntBuffer(properties.capacity());
                IntBuffer length = GLBuffers.newDirectIntBuffer(properties.capacity());

                gl4.glGetProgramResourceiv(programName, GL_UNIFORM_BLOCK, resourceIndex, properties.capacity(),
                        properties, params.capacity() * Integer.BYTES, length, params);

                byte[] name_ = new byte[name.capacity()];
                name.get(name_);
                name.rewind();

                System.out.printf("Uniform Block(binding %d, size %d, num var %d, name %s)\n",
                        params.get(Property.binding), params.get(Property.dataSize),
                        params.get(Property.numActiveVariables), new String(name_).trim());
                // clean
                for (int i = 0; i < name.capacity(); i++) {
                    name.put(i, (byte) 0);
                }
            }

            IntBuffer maxNumActiveVariables = GLBuffers.newDirectIntBuffer(1);
            gl4.glGetProgramInterfaceiv(programName, GL_UNIFORM_BLOCK, GL_MAX_NUM_ACTIVE_VARIABLES, maxNumActiveVariables);
        }
        {
            IntBuffer maxNameLength = GLBuffers.newDirectIntBuffer(1);
            gl4.glGetProgramInterfaceiv(programName, GL_UNIFORM, GL_MAX_NAME_LENGTH, maxNameLength);
            ByteBuffer name = GLBuffers.newDirectByteBuffer(maxNameLength.get(0) + 1);

            IntBuffer activeResources = GLBuffers.newDirectIntBuffer(1);
            gl4.glGetProgramInterfaceiv(programName, GL_UNIFORM, GL_ACTIVE_RESOURCES, activeResources);

            class Property {

                public static final int location = 0;
                public static final int blockIndex = 1;
                public static final int offset = 2;
                public static final int arraySize = 3;
                public static final int arrayStride = 4;
                public static final int type = 5;
//                    public static final int MatrixStride;
//                    public static final int IsRowMajor;
//                    public static final int VertexShader;
//                    public static final int ControlShader;
//                    public static final int Evaluationhader;
//                    public static final int GeometryShader;
//                    public static final int FragmentShader;
//                    public static final int ComputeShader;
            };

            for (int resourceIndex = 0; resourceIndex < activeResources.get(0); ++resourceIndex) {
                gl4.glGetProgramResourceName(programName, GL_UNIFORM, resourceIndex, name.capacity(), null, name);

                IntBuffer properties = GLBuffers.newDirectIntBuffer(new int[]{
                    GL_LOCATION,
                    GL_BLOCK_INDEX,
                    GL_OFFSET,
                    GL_ARRAY_SIZE,
                    GL_ARRAY_STRIDE,
                    GL_TYPE,
                    GL_MATRIX_STRIDE,
                    GL_IS_ROW_MAJOR,
                    GL_REFERENCED_BY_VERTEX_SHADER,
                    GL_REFERENCED_BY_TESS_CONTROL_SHADER,
                    GL_REFERENCED_BY_TESS_EVALUATION_SHADER,
                    GL_REFERENCED_BY_GEOMETRY_SHADER,
                    GL_REFERENCED_BY_FRAGMENT_SHADER,
                    GL_REFERENCED_BY_COMPUTE_SHADER});

                IntBuffer params = GLBuffers.newDirectIntBuffer(properties.capacity());
                IntBuffer length = GLBuffers.newDirectIntBuffer(properties.capacity());

                gl4.glGetProgramResourceiv(programName, GL_UNIFORM, resourceIndex, properties.capacity(),
                        properties, params.capacity() * Integer.BYTES, length, params);

                byte[] name_ = new byte[name.capacity()];
                name.get(name_);
                name.rewind();

                System.out.printf("Uniform %s (%s), location: %d, block: %d, offset: %d, array size %d, "
                        + "array stride %d\n", new String(name_).trim(), getTypeString(params.get(Property.type)),
                        params.get(Property.location), params.get(Property.blockIndex), params.get(Property.offset),
                        params.get(Property.arraySize), params.get(Property.arrayStride));
                // clean
                for (int i = 0; i < name.capacity(); i++) {
                    name.put(i, (byte) 0);
                }
            }
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(1, vertexArrayName);
        gl4.glBindVertexArray(vertexArrayName.get(0));
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 3, GL_FLOAT, false, glf.Vertex_v3fn3fc4f.SIZE,
                    glf.Vertex_v3fn3fc4f.OFFSET_POSITION);
            gl4.glVertexAttribPointer(Semantic.Attr.NORMAL, 3, GL_FLOAT, false, glf.Vertex_v3fn3fc4f.SIZE,
                    glf.Vertex_v3fn3fc4f.OFFSET_NORMAL);
            gl4.glVertexAttribPointer(Semantic.Attr.COLOR, 4, GL_FLOAT, false, glf.Vertex_v3fn3fc4f.SIZE,
                    glf.Vertex_v3fn3fc4f.OFFSET_COLOR);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl4.glEnableVertexAttribArray(Semantic.Attr.NORMAL);
            gl4.glEnableVertexAttribArray(Semantic.Attr.COLOR);

            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT));
        }
        gl4.glBindVertexArray(0);

        return true;
    }

    private boolean initBuffer(GL4 gl4) {

        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        ByteBuffer lightBuffer = GLBuffers.newDirectByteBuffer(Light.SIZE);
        ByteBuffer materialBuffer = GLBuffers.newDirectByteBuffer(Material.SIZE);

        gl4.glGenBuffers(Buffer.MAX, bufferName);

        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT));
        gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
        gl4.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        {
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.PER_DRAW));
            gl4.glBufferData(GL_UNIFORM_BUFFER, Transform.SIZE, null, GL_DYNAMIC_DRAW);
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }

        {
            new Light(new Vec3(0.0f, 0.0f, 100.f)).toDbb(lightBuffer);

            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.PER_PASS));
            gl4.glBufferData(GL_UNIFORM_BUFFER, Light.SIZE, lightBuffer, GL_STATIC_DRAW);
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }

        {
            new Material(new Vec3(0.7f, 0.0f, 0.0f), new Vec3(0.0f, 0.5f, 0.0f), new Vec3(0.0f, 0.0f, 0.5f), 128.0f)
                    .toDbb(materialBuffer);

            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.PER_SCENE));
            gl4.glBufferData(GL_UNIFORM_BUFFER, Material.SIZE, materialBuffer, GL_STATIC_DRAW);
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }

        BufferUtils.destroyDirectBuffer(elementBuffer);
        BufferUtils.destroyDirectBuffer(vertexBuffer);
        BufferUtils.destroyDirectBuffer(lightBuffer);
        BufferUtils.destroyDirectBuffer(materialBuffer);

        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        {
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.PER_DRAW));
            ByteBuffer transform = gl4.glMapBufferRange(GL_UNIFORM_BUFFER,
                    0, Transform.SIZE, GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, 4.0f / 3.0f, 0.1f, 100.0f);
            Mat4 view = viewMat4();
            Mat4 model = new Mat4(1.0f).rotate(-(float) Math.PI * 0.5f, new Vec3(0.0f, 0.0f, 1.0f));

            Transform t = new Transform();
            t.mv = view.mul(model);
            t.p = projection;
            t.normal = new Mat3(t.mv.invTransp3_());

            t.toDbb(transform);

            gl4.glUnmapBuffer(GL_UNIFORM_BUFFER);
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }

        gl4.glViewport(0, 0, windowSize.x, windowSize.y);
        gl4.glClearBufferfv(GL_COLOR, 0, clearColor.put(0, 0.2f).put(1, 0.2f).put(2, 0.2f).put(3, 1.0f));
        gl4.glClearBufferfv(GL_DEPTH, 0, clearDepth.put(0, 1.0f));

        gl4.glUseProgram(programName);
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Uniform.PER_SCENE, bufferName.get(Buffer.PER_SCENE));
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Uniform.PER_PASS, bufferName.get(Buffer.PER_PASS));
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Uniform.PER_DRAW, bufferName.get(Buffer.PER_DRAW));
        gl4.glBindVertexArray(vertexArrayName.get(0));

        gl4.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteVertexArrays(1, vertexArrayName);
        gl4.glDeleteBuffers(Buffer.MAX, bufferName);
        gl4.glDeleteProgram(programName);

        BufferUtils.destroyDirectBuffer(vertexArrayName);
        BufferUtils.destroyDirectBuffer(bufferName);

        return true;
    }
}
