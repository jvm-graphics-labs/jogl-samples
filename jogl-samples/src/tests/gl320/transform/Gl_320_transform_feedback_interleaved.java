/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl320.transform;

import static com.jogamp.opengl.GL2ES2.GL_ACTIVE_UNIFORMS;
import static com.jogamp.opengl.GL2ES2.GL_FLOAT_VEC4;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES3.GL_INTERLEAVED_ATTRIBS;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.Profile;
import framework.Semantic;
import framework.Test;

/**
 *
 * @author GBarbieri
 */
public class Gl_320_transform_feedback_interleaved extends Test {

    public static void main(String[] args) {
        Gl_320_transform_feedback_interleaved gl_320_transform_feedback_interleaved = new Gl_320_transform_feedback_interleaved();
    }

    public Gl_320_transform_feedback_interleaved() {
        super("gl-320-transform-feedback-interleaved", Profile.CORE, 3, 2);
    }

    private final String SHADERS_SOURCE_TRANSFORM = "transform-feedback-transform";
    private final String SHADERS_SOURCE_FEEDBACK = "transform-feedback-feedback";
    private final String SHADERS_ROOT = "src/data/gl_320/transform";

    private int vertexCount = 6;
    private int positionSize = vertexCount * 4 * Float.BYTES;
    private float[] positionData = {
        -1.0f, -1.0f, 0.0f, 1.0f,
        +1.0f, -1.0f, 0.0f, 1.0f,
        +1.0f, +1.0f, 0.0f, 1.0f,
        +1.0f, +1.0f, 0.0f, 1.0f,
        -1.0f, +1.0f, 0.0f, 1.0f,
        -1.0f, -1.0f, 0.0f, 1.0f};

    private enum Program {
        TRANSFORM,
        FEEDBACK,
        MAX
    }

    private enum Shader {
        VERT_TRANSFORM,
        VERT_FEEDBACK,
        FRAG_FEEDBACK,
        MAX
    }

    private int[] programName = new int[Program.MAX.ordinal()], vertexArrayName = new int[Program.MAX.ordinal()],
            bufferName = new int[Program.MAX.ordinal()];
    private int transformUniformMvp;

    private boolean initProgram(GL3 gl3) {

        boolean validated = true;

        ShaderCode[] shaderCodes = new ShaderCode[Shader.MAX.ordinal()];

        shaderCodes[Shader.VERT_TRANSFORM.ordinal()] = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_TRANSFORM, "vert", null, true);
        shaderCodes[Shader.VERT_FEEDBACK.ordinal()] = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_FEEDBACK, "frag", null, true);
        shaderCodes[Shader.FRAG_FEEDBACK.ordinal()] = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_FEEDBACK, "frag", null, true);

        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();
            shaderProgram.add(shaderCodes[Shader.VERT_TRANSFORM.ordinal()]);

            shaderProgram.init(gl3);

            programName[Program.TRANSFORM.ordinal()] = shaderProgram.program();

            gl3.glBindAttribLocation(programName[Program.TRANSFORM.ordinal()], Semantic.Attr.POSITION, "Position");

            String[] strings = {"gl_Position", "block.Color"};
            gl3.glTransformFeedbackVaryings(programName[Program.TRANSFORM.ordinal()],
                    2, strings, GL_INTERLEAVED_ATTRIBS);

            shaderProgram.link(gl3, System.out);

            byte[] name = new byte[64];
            int[] length = {0};
            int[] size = {0};
            int[] type = {0};

            gl3.glGetTransformFeedbackVarying(
                    programName[Program.TRANSFORM.ordinal()],
                    0,
                    name.length,
                    length, 0,
                    size, 0,
                    type, 0,
                    name, 0);

            validated = validated && (size[0] == 1) && (type[0] == GL_FLOAT_VEC4);
        }
        // Get variables locations
        if (validated) {

            transformUniformMvp = gl3.glGetUniformLocation(programName[Program.TRANSFORM.ordinal()], "MVP");

            int[] activeUniforms = {0};
            gl3.glGetProgramiv(programName[Program.TRANSFORM.ordinal()], GL_ACTIVE_UNIFORMS, activeUniforms, 0);

            byte[] name = new byte[64];
            int[] length = {0};
            int[] size = {0};
            int[] type = {0};

            for (int i = 0; i < activeUniforms[0]; i++) {

                gl3.glGetActiveUniform(
                        programName[Program.TRANSFORM.ordinal()],
                        i,
                        name.length,
                        length, 0,
                        size, 0,
                        type, 0,
                        name, 0);

                int location = gl3.glGetUniformLocation(programName[Program.TRANSFORM.ordinal()], new String(name));

                if (transformUniformMvp == location) {

                    validated = validated && (size[0] == 1) && (type[0] == GL_FLOAT_VEC4);
                    validated = validated && (transformUniformMvp > 0);
                }
            }
        }

        // Create program
        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();
            shaderProgram.add(shaderCodes[Shader.VERT_FEEDBACK.ordinal()]);
            shaderProgram.add(shaderCodes[Shader.FRAG_FEEDBACK.ordinal()]);

            shaderProgram.init(gl3);

            programName[Program.FEEDBACK.ordinal()] = shaderProgram.program();

            gl3.glBindAttribLocation(programName[Program.FEEDBACK.ordinal()], Semantic.Attr.POSITION, "Position");
            gl3.glBindAttribLocation(programName[Program.FEEDBACK.ordinal()], Semantic.Attr.COLOR, "Color");
            gl3.glBindFragDataLocation(programName[Program.FEEDBACK.ordinal()], Semantic.Frag.COLOR, "Color");

            shaderProgram.link(gl3, System.out);
        }

        return validated & checkError(gl3, "initProgram");
    }

    private boolean initVertexArray(GL3 gl3) {

        gl3.glGenVertexArrays(Program.MAX.ordinal(),  vertexArrayName,0);

        // Build a vertex array object
//        gl3.glBindVertexArray(vertexArrayName[Program.TRANSFORM.ordinal()]);
//        {
//            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Program.TRANSFORM.ordinal()]);
//            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 4, GL_FLOAT, false, 0, 0);
//            gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);
//
//            gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);
//        }
//        gl3.glBindVertexArray(0);
//
//        // Build a vertex array object
//        gl3.glBindVertexArray(vertexArrayName[Program.FEEDBACK.ordinal()]);
//        {
//            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Program.FEEDBACK.ordinal()]);
//            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 4, GL_FLOAT, false, V, 0);
//            gl3.glVertexAttribPointer(semantic::attr::COLOR, 4, GL_FLOAT, GL_FALSE, sizeof(glf::vertex_v4fc4f), BUFFER_OFFSET(sizeof(glm::vec4)));
//            gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);
//
//            gl3.glEnableVertexAttribArray(semantic::attr::POSITION);
//            gl3.glEnableVertexAttribArray(semantic::attr::COLOR);
//        }
//        gl3.glBindVertexArray(0);

        return checkError(gl3, "initVertexArray");
    }
}
