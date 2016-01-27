/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_430;

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
import static com.jogamp.opengl.GL.GL_UNSIGNED_INT;
import static com.jogamp.opengl.GL.GL_UNSIGNED_SHORT;
import static com.jogamp.opengl.GL2ES2.GL_BOOL;
import static com.jogamp.opengl.GL2ES2.GL_BOOL_VEC2;
import static com.jogamp.opengl.GL2ES2.GL_BOOL_VEC3;
import static com.jogamp.opengl.GL2ES2.GL_BOOL_VEC4;
import static com.jogamp.opengl.GL2ES2.GL_FLOAT_MAT2;
import static com.jogamp.opengl.GL2ES2.GL_FLOAT_MAT3;
import static com.jogamp.opengl.GL2ES2.GL_FLOAT_MAT4;
import static com.jogamp.opengl.GL2ES2.GL_FLOAT_VEC2;
import static com.jogamp.opengl.GL2ES2.GL_FLOAT_VEC3;
import static com.jogamp.opengl.GL2ES2.GL_FLOAT_VEC4;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_INT;
import static com.jogamp.opengl.GL2ES2.GL_INT_VEC2;
import static com.jogamp.opengl.GL2ES2.GL_INT_VEC3;
import static com.jogamp.opengl.GL2ES2.GL_INT_VEC4;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import static com.jogamp.opengl.GL2ES3.GL_DEPTH;
import static com.jogamp.opengl.GL2ES3.GL_FLOAT_MAT2x3;
import static com.jogamp.opengl.GL2ES3.GL_FLOAT_MAT2x4;
import static com.jogamp.opengl.GL2ES3.GL_FLOAT_MAT3x2;
import static com.jogamp.opengl.GL2ES3.GL_FLOAT_MAT3x4;
import static com.jogamp.opengl.GL2ES3.GL_FLOAT_MAT4x2;
import static com.jogamp.opengl.GL2ES3.GL_FLOAT_MAT4x3;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER;
import static com.jogamp.opengl.GL2ES3.GL_UNSIGNED_INT_VEC2;
import static com.jogamp.opengl.GL2ES3.GL_UNSIGNED_INT_VEC3;
import static com.jogamp.opengl.GL2ES3.GL_UNSIGNED_INT_VEC4;
import static com.jogamp.opengl.GL2GL3.GL_DOUBLE;
import static com.jogamp.opengl.GL3.GL_DOUBLE_MAT2;
import static com.jogamp.opengl.GL3.GL_DOUBLE_MAT2x3;
import static com.jogamp.opengl.GL3.GL_DOUBLE_MAT2x4;
import static com.jogamp.opengl.GL3.GL_DOUBLE_MAT3;
import static com.jogamp.opengl.GL3.GL_DOUBLE_MAT3x2;
import static com.jogamp.opengl.GL3.GL_DOUBLE_MAT3x4;
import static com.jogamp.opengl.GL3.GL_DOUBLE_MAT4;
import static com.jogamp.opengl.GL3.GL_DOUBLE_MAT4x2;
import static com.jogamp.opengl.GL3.GL_DOUBLE_MAT4x3;
import static com.jogamp.opengl.GL3.GL_DOUBLE_VEC2;
import static com.jogamp.opengl.GL3.GL_DOUBLE_VEC3;
import static com.jogamp.opengl.GL3.GL_DOUBLE_VEC4;
import static com.jogamp.opengl.GL3ES3.GL_ACTIVE_RESOURCES;
import static com.jogamp.opengl.GL3ES3.GL_ARRAY_SIZE;
import static com.jogamp.opengl.GL3ES3.GL_ARRAY_STRIDE;
import static com.jogamp.opengl.GL3ES3.GL_BLOCK_INDEX;
import static com.jogamp.opengl.GL3ES3.GL_BUFFER_BINDING;
import static com.jogamp.opengl.GL3ES3.GL_BUFFER_DATA_SIZE;
import static com.jogamp.opengl.GL3ES3.GL_IS_ROW_MAJOR;
import static com.jogamp.opengl.GL3ES3.GL_LOCATION;
import static com.jogamp.opengl.GL3ES3.GL_MATRIX_STRIDE;
import static com.jogamp.opengl.GL3ES3.GL_MAX_NAME_LENGTH;
import static com.jogamp.opengl.GL3ES3.GL_MAX_NUM_ACTIVE_VARIABLES;
import static com.jogamp.opengl.GL3ES3.GL_NUM_ACTIVE_VARIABLES;
import static com.jogamp.opengl.GL3ES3.GL_OFFSET;
import static com.jogamp.opengl.GL3ES3.GL_REFERENCED_BY_COMPUTE_SHADER;
import static com.jogamp.opengl.GL3ES3.GL_REFERENCED_BY_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL3ES3.GL_REFERENCED_BY_GEOMETRY_SHADER;
import static com.jogamp.opengl.GL3ES3.GL_REFERENCED_BY_TESS_CONTROL_SHADER;
import static com.jogamp.opengl.GL3ES3.GL_REFERENCED_BY_TESS_EVALUATION_SHADER;
import static com.jogamp.opengl.GL3ES3.GL_REFERENCED_BY_VERTEX_SHADER;
import static com.jogamp.opengl.GL3ES3.GL_TYPE;
import static com.jogamp.opengl.GL3ES3.GL_UNIFORM;
import static com.jogamp.opengl.GL3ES3.GL_UNIFORM_BLOCK;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.BufferUtils;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
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

    private class VertexV3fN3fC4f {

        // vec3
        public float[] position;
        // vec3
        public float[] texCoord;
        // vec4
        public float[] color;

        public VertexV3fN3fC4f(float[] position, float[] texCoord, float[] color) {
            this.position = position;
            this.texCoord = texCoord;
            this.color = color;
        }

        public static final int SIZEOF = (3 + 3 + 4) * Float.BYTES;
        public static final int OFFSET_POSITION = 0;
        public static final int OFFSET_NORMAL = 3 * Float.BYTES;
        public static final int OFFSET_COLOR = (3 + 3) * Float.BYTES;
    }

    private int vertexCount = 4;
    private int vertexSize = vertexCount * VertexV3fN3fC4f.SIZEOF;
    VertexV3fN3fC4f[] vertexData = {
        new VertexV3fN3fC4f(new float[]{-1.0f, -1.0f, 0.0f}, new float[]{0.0f, 0.0f, 1.0f},
        new float[]{1.0f, 0.0f, 0.0f, 1.0f}),
        new VertexV3fN3fC4f(new float[]{1.0f, -1.0f, 0.0f}, new float[]{0.0f, 0.0f, 1.0f},
        new float[]{0.0f, 1.0f, 0.0f, 1.0f}),
        new VertexV3fN3fC4f(new float[]{1.0f, 1.0f, 0.0f}, new float[]{0.0f, 0.0f, 1.0f},
        new float[]{0.0f, 0.0f, 1.0f, 1.0f}),
        new VertexV3fN3fC4f(new float[]{-1.0f, 1.0f, 0.0f}, new float[]{0.0f, 0.0f, 1.0f},
        new float[]{1.0f, 1.0f, 1.0f, 1.0f})};

    private int elementCount = 6;
    private int elementSize = elementCount * Short.BYTES;
    private short[] elementData = {
        0, 1, 2,
        2, 3, 0};

    private enum Buffer {
        VERTEX,
        ELEMENT,
        PER_SCENE,
        PER_PASS,
        PER_DRAW,
        MAX
    }

    private enum Uniform {
        PER_SCENE,
        PER_PASS,
        PER_DRAW,
        LIGHT
    };

    private class Material {

        // vec3
        public float[] ambient;
        private final float padding1 = 0.0f;
        // vec3
        public float[] diffuse;
        private final float padding2 = 0.0f;
        // vec3
        public float[] specular;
        public float shininess;

        public Material(float[] ambient, float[] diffuse, float[] specular, float shininess) {
            this.ambient = ambient;
            this.diffuse = diffuse;
            this.specular = specular;
            this.shininess = shininess;
        }

        public static final int SIZE_OF = (3 + 1 + 3 + 1 + 3 + 1) * Float.BYTES;

        public float[] toFloatArray() {
            return new float[]{ambient[0], ambient[1], ambient[2], padding1,
                diffuse[0], diffuse[1], diffuse[2], padding2,
                specular[0], specular[1], specular[2], shininess};
        }
    };

    private class Light {

        // vec3
        public float[] position;

        public Light(float[] position) {
            this.position = position;
        }

        public static final int SIZE_OF = 3 * Float.BYTES;
    };

    private class Transform {

        // mat4
        public float[] p;
        // mat4
        public float[] mv;
        // mat3
        public float[] normal;

        public static final int SIZE_OF = (16 + 16 + 9) * Float.BYTES;
    };

    private int[] vertexArrayName = {0}, bufferName = new int[Buffer.MAX.ordinal()];
    private int programName, uniformPerDraw, uniformPerPass, uniformPerScene;
    private float[] projection = new float[16], view = new float[16], model = new float[16],
            mv = new float[16], normal = new float[9];

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

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

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

            gl4.glUniformBlockBinding(programName, uniformPerDraw, Uniform.PER_DRAW.ordinal());
            gl4.glUniformBlockBinding(programName, uniformPerPass, Uniform.PER_PASS.ordinal());
            gl4.glUniformBlockBinding(programName, uniformPerPass, Uniform.PER_SCENE.ordinal());
        }
        {
            int[] maxNameLength = {0};
            gl4.glGetProgramInterfaceiv(programName, GL_UNIFORM_BLOCK, GL_MAX_NAME_LENGTH, maxNameLength, 0);
            byte[] name = new byte[maxNameLength[0] + 1];

            int[] activeResources = {0};
            gl4.glGetProgramInterfaceiv(programName, GL_UNIFORM_BLOCK, GL_ACTIVE_RESOURCES, activeResources, 0);

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

            for (int resourceIndex = 0; resourceIndex < activeResources[0]; ++resourceIndex) {
                gl4.glGetProgramResourceName(programName, GL_UNIFORM_BLOCK, resourceIndex, name.length, null, 0, name, 0);

                int[] properties = {
                    GL_BUFFER_BINDING,
                    GL_BUFFER_DATA_SIZE,
                    GL_NUM_ACTIVE_VARIABLES,
                    GL_REFERENCED_BY_VERTEX_SHADER,
                    GL_REFERENCED_BY_TESS_CONTROL_SHADER,
                    GL_REFERENCED_BY_TESS_EVALUATION_SHADER,
                    GL_REFERENCED_BY_GEOMETRY_SHADER,
                    GL_REFERENCED_BY_FRAGMENT_SHADER,
                    GL_REFERENCED_BY_COMPUTE_SHADER};

                int[] params = new int[properties.length];
                int[] length = {properties.length};

                gl4.glGetProgramResourceiv(programName, GL_UNIFORM_BLOCK, resourceIndex, properties.length,
                        properties, 0, params.length * Integer.BYTES, length, 0, params, 0);

                System.out.printf("Uniform Block(binding %d, size %d, num var %d, name %s)\n", params[Property.binding],
                        params[Property.dataSize], params[Property.numActiveVariables], new String(name).trim());
                // clean
                for (int i = 0; i < name.length; i++) {
                    name[i] = (byte) 0;
                }
            }

            int[] maxNumActiveVariables = {0};
            gl4.glGetProgramInterfaceiv(programName, GL_UNIFORM_BLOCK, GL_MAX_NUM_ACTIVE_VARIABLES,
                    maxNumActiveVariables, 0);
        }
        {
            int[] maxNameLength = {0};
            gl4.glGetProgramInterfaceiv(programName, GL_UNIFORM, GL_MAX_NAME_LENGTH, maxNameLength, 0);
            byte[] name = new byte[maxNameLength[0] + 1];

            int[] activeResources = {0};
            gl4.glGetProgramInterfaceiv(programName, GL_UNIFORM, GL_ACTIVE_RESOURCES, activeResources, 0);

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

            for (int resourceIndex = 0; resourceIndex < activeResources[0]; ++resourceIndex) {
                gl4.glGetProgramResourceName(programName, GL_UNIFORM, resourceIndex, name.length, null, 0, name, 0);

                int[] properties = {
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
                    GL_REFERENCED_BY_COMPUTE_SHADER
                };

                int[] params = new int[properties.length];
                int[] length = {properties.length};

                gl4.glGetProgramResourceiv(programName, GL_UNIFORM, resourceIndex, properties.length,
                        properties, 0, params.length * Integer.BYTES, length, 0, params, 0);

                System.out.printf("Uniform %s (%s), location: %d, block: %d, offset: %d, array size %d, "
                        + "array stride %d\n", new String(name).trim(), getTypeString(params[Property.type]),
                        params[Property.location], params[Property.blockIndex], params[Property.offset],
                        params[Property.arraySize], params[Property.arrayStride]);
                // clean
                for (int i = 0; i < name.length; i++) {
                    name[i] = (byte) 0;
                }
            }
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(1, vertexArrayName, 0);
        gl4.glBindVertexArray(vertexArrayName[0]);
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 3, GL_FLOAT, false, VertexV3fN3fC4f.SIZEOF,
                    VertexV3fN3fC4f.OFFSET_POSITION);
            gl4.glVertexAttribPointer(Semantic.Attr.NORMAL, 3, GL_FLOAT, false, VertexV3fN3fC4f.SIZEOF,
                    VertexV3fN3fC4f.OFFSET_NORMAL);
            gl4.glVertexAttribPointer(Semantic.Attr.COLOR, 4, GL_FLOAT, false, VertexV3fN3fC4f.SIZEOF,
                    VertexV3fN3fC4f.OFFSET_COLOR);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl4.glEnableVertexAttribArray(Semantic.Attr.NORMAL);
            gl4.glEnableVertexAttribArray(Semantic.Attr.COLOR);

            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT.ordinal()]);
        }
        gl4.glBindVertexArray(0);

        return true;
    }

    private boolean initBuffer(GL4 gl4) {

        gl4.glGenBuffers(Buffer.MAX.ordinal(), bufferName, 0);

        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT.ordinal()]);
        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(elementBuffer);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX.ordinal()]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexCount * (3 + 3 + 4));
        for (int i = 0; i < vertexCount; i++) {
            vertexBuffer.put(vertexData[i].position);
            vertexBuffer.put(vertexData[i].texCoord);
            vertexBuffer.put(vertexData[i].color);
        }
        vertexBuffer.rewind();
        gl4.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        BufferUtils.destroyDirectBuffer(vertexBuffer);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        {
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.PER_DRAW.ordinal()]);
            gl4.glBufferData(GL_UNIFORM_BUFFER, Transform.SIZE_OF, null, GL_DYNAMIC_DRAW);
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }

        {
            Light light = new Light(new float[]{0.0f, 0.0f, 100.f});

            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.PER_PASS.ordinal()]);
            FloatBuffer lightBuffer = GLBuffers.newDirectFloatBuffer(light.position);
            gl4.glBufferData(GL_UNIFORM_BUFFER, Light.SIZE_OF, lightBuffer, GL_STATIC_DRAW);
            BufferUtils.destroyDirectBuffer(lightBuffer);
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }

        {
            Material material = new Material(new float[]{0.7f, 0.0f, 0.0f}, new float[]{0.0f, 0.5f, 0.0f},
                    new float[]{0.0f, 0.0f, 0.5f}, 128.0f);

            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.PER_SCENE.ordinal()]);
            FloatBuffer materialBuffer = GLBuffers.newDirectFloatBuffer(material.toFloatArray());
            gl4.glBufferData(GL_UNIFORM_BUFFER, Material.SIZE_OF, materialBuffer, GL_STATIC_DRAW);
            BufferUtils.destroyDirectBuffer(materialBuffer);
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }

        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        {
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.PER_DRAW.ordinal()]);
            ByteBuffer transform = gl4.glMapBufferRange(GL_UNIFORM_BUFFER,
                    0, Transform.SIZE_OF, GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            FloatUtil.makePerspective(projection, 0, true, (float) Math.PI * 0.25f, 4.0f / 3.0f, 0.1f, 100.0f);
            view = view();
            FloatUtil.makeRotationAxis(model, 0, -(float) Math.PI * 0.5f, 0.0f, 0.0f, 1.0f, tmpVec3);

            FloatUtil.multMatrix(view, model, mv);

            for (float f : projection) {
                transform.putFloat(f);
            }

            for (float f : mv) {
                transform.putFloat(f);
            }

            FloatUtil.invertMatrix(mv, model);
            FloatUtil.transposeMatrix(model, view);

            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    normal[i * 3 + j] = view[i * 4 + j];
                }
            }
            for (float f : normal) {
                transform.putFloat(f);
            }
            transform.rewind();

            gl4.glUnmapBuffer(GL_UNIFORM_BUFFER);
            gl4.glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }

        gl4.glViewport(0, 0, windowSize.x, windowSize.y);
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{0.2f, 0.2f, 0.2f, 1.0f}, 0);
        gl4.glClearBufferfv(GL_DEPTH, 0, new float[]{1.0f}, 0);

        gl4.glUseProgram(programName);
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Uniform.PER_SCENE.ordinal(), bufferName[Buffer.PER_SCENE.ordinal()]);
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Uniform.PER_PASS.ordinal(), bufferName[Buffer.PER_PASS.ordinal()]);
        gl4.glBindBufferBase(GL_UNIFORM_BUFFER, Uniform.PER_DRAW.ordinal(), bufferName[Buffer.PER_DRAW.ordinal()]);
        gl4.glBindVertexArray(vertexArrayName[0]);

        gl4.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 1, 0);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteVertexArrays(1, vertexArrayName, 0);
        gl4.glDeleteBuffers(Buffer.MAX.ordinal(), bufferName, 0);
        gl4.glDeleteProgram(programName);

        return true;
    }
}
