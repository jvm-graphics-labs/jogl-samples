/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_420;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_TRUE;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER_BIT;
import static com.jogamp.opengl.GL2ES2.GL_PROGRAM_SEPARABLE;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER_BIT;
import static com.jogamp.opengl.GL2ES3.GL_GEOMETRY_SHADER_BIT;
import static com.jogamp.opengl.GL2ES3.GL_INTERLEAVED_ATTRIBS;
import static com.jogamp.opengl.GL2ES3.GL_TRANSFORM_FEEDBACK;
import static com.jogamp.opengl.GL2ES3.GL_TRANSFORM_FEEDBACK_BUFFER;
import static com.jogamp.opengl.GL3ES3.GL_GEOMETRY_SHADER;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import jglm.Vec2;

/**
 *
 * @author GBarbieri
 */
public class Gl_420_transform_feedback_instanced extends Test {

    public static void main(String[] args) {
        Gl_420_transform_feedback_instanced gl_420_transform_feedback_instanced = new Gl_420_transform_feedback_instanced();
    }

    public Gl_420_transform_feedback_instanced() {
        super("gl-420-transform-feedback-instanced", Profile.CORE, 4, 2,
                new Vec2((float) Math.PI * 0.2f, (float) Math.PI * 0.2f));
    }

    private final String SHADERS_SOURCE_TRANSFORM = "transform-stream";
    private final String SHADERS_SOURCE_FEEDBACK = "feedback-stream";
    private final String SHADERS_ROOT = "src/data/gl_420";

    private int vertexCount = 4;
    private int vertexSize = vertexCount * 4 * Float.BYTES;
    private float[] vertexData = {
        -1.0f, -1.0f, 0.0f, 1.0f,
        +1.0f, -1.0f, 0.0f, 1.0f,
        +1.0f, +1.0f, 0.0f, 1.0f,
        -1.0f, +1.0f, 0.0f, 1.0f};

    private int elementCount = 6;
    private int elementSize = elementCount * Integer.BYTES;
    private int[] elementData = {
        0, 1, 2,
        2, 3, 0};

    private enum Pipeline {
        TRANSFORM,
        FEEDBACK,
        MAX
    }

    private enum Buffer {
        TRANSFORM_ELEMENT,
        TRANSFORM_VERTEX,
        FEEDBACK_VERTEX,
        MAX
    }

    private int[] feedbackName = {0}, query = {0}, pipelineName = new int[Pipeline.MAX.ordinal()],
            programName = new int[Pipeline.MAX.ordinal()], vertexArrayName = new int[Pipeline.MAX.ordinal()],
            bufferName = new int[Buffer.MAX.ordinal()];
    private int transformUniformMvp, feedbackUniformMvp;

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        // Create program
        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_TRANSFORM, "vert", null, true);
            ShaderCode geomShaderCode = ShaderCode.create(gl4, GL_GEOMETRY_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_TRANSFORM, "geom", null, true);

            shaderProgram.init(gl4);
            programName[Pipeline.TRANSFORM.ordinal()] = shaderProgram.program();
            gl4.glProgramParameteri(programName[Pipeline.TRANSFORM.ordinal()], GL_PROGRAM_SEPARABLE, GL_TRUE);
            shaderProgram.add(vertShaderCode);
            shaderProgram.add(geomShaderCode);
            shaderProgram.link(gl4, System.out);

            String[] strings = {"gl_Position", "block.Color"};
            gl4.glTransformFeedbackVaryings(programName[Pipeline.TRANSFORM.ordinal()], 2, strings, GL_INTERLEAVED_ATTRIBS);
        }

        // Get variables locations
        if (validated) {

            transformUniformMvp = gl4.glGetUniformLocation(programName[Pipeline.TRANSFORM.ordinal()], "MVP");
            validated = validated && (transformUniformMvp >= 0);
        }

        // Create program
        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_FEEDBACK, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE_FEEDBACK, "frag", null, true);

            shaderProgram.init(gl4);
            programName[Pipeline.FEEDBACK.ordinal()] = shaderProgram.program();
            gl4.glProgramParameteri(programName[Pipeline.FEEDBACK.ordinal()], GL_PROGRAM_SEPARABLE, GL_TRUE);
            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);
            shaderProgram.link(gl4, System.out);
        }

        // Get variables locations
        if (validated) {

            feedbackUniformMvp = gl4.glGetUniformLocation(programName[Pipeline.FEEDBACK.ordinal()], "MVP");
            validated = validated && (feedbackUniformMvp >= 0);
        }

        if (validated) {

            gl4.glGenProgramPipelines(Pipeline.MAX.ordinal(), pipelineName, 0);
            gl4.glUseProgramStages(pipelineName[Pipeline.FEEDBACK.ordinal()], GL_VERTEX_SHADER_BIT
                    | GL_FRAGMENT_SHADER_BIT, programName[Pipeline.FEEDBACK.ordinal()]);
            gl4.glUseProgramStages(pipelineName[Pipeline.TRANSFORM.ordinal()], GL_VERTEX_SHADER_BIT
                    | GL_GEOMETRY_SHADER_BIT, programName[Pipeline.TRANSFORM.ordinal()]);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initVertexArray(GL4 gl4) {

        // Build a vertex array objects
        gl4.glGenVertexArrays(Pipeline.MAX.ordinal(), vertexArrayName, 0);

        gl4.glBindVertexArray(vertexArrayName[Pipeline.TRANSFORM.ordinal()]);
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.TRANSFORM_VERTEX.ordinal()]);
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 4, GL_FLOAT, false, 0, 0);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);

            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.TRANSFORM_ELEMENT.ordinal()]);
        }
        gl4.glBindVertexArray(0);

        gl4.glBindVertexArray(vertexArrayName[Pipeline.FEEDBACK.ordinal()]);
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.FEEDBACK_VERTEX.ordinal()]);
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 4, GL_FLOAT, false, 2 * 4 * Float.BYTES, 0);
            gl4.glVertexAttribPointer(Semantic.Attr.COLOR, 4, GL_FLOAT, false, 2 * 4 * Float.BYTES, 4 * Float.BYTES);
            gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl4.glEnableVertexAttribArray(Semantic.Attr.COLOR);

            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        }
        gl4.glBindVertexArray(0);

        return true;
    }

    private boolean initFeedback(GL4 gl4) {

        // Generate a buffer object
        gl4.glGenTransformFeedbacks(1, feedbackName, 0);
        gl4.glBindTransformFeedback(GL_TRANSFORM_FEEDBACK, feedbackName[0]);
        gl4.glBindBufferBase(GL_TRANSFORM_FEEDBACK_BUFFER, 0, bufferName[Buffer.FEEDBACK_VERTEX.ordinal()]);
        gl4.glBindTransformFeedback(GL_TRANSFORM_FEEDBACK, 0);

        return true;
    }
    
    private boolean initBuffer(GL4 gl4)	{
        
		gl4.glGenBuffers(Buffer.MAX.ordinal(), bufferName,0);

		gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.TRANSFORM_ELEMENT.ordinal()]);
//		gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, ElementSize, ElementData, GL_STATIC_DRAW);
//		gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
//
//		gl4.glBindBuffer(GL_ARRAY_BUFFER, BufferName[buffer::TRANSFORM_VERTEX]);
//		gl4.glBufferData(GL_ARRAY_BUFFER, VertexSize, VertexData, GL_STATIC_DRAW);
//		gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);
//
//		gl4.glBindBuffer(GL_ARRAY_BUFFER, BufferName[buffer::FEEDBACK_VERTEX]);
//		gl4.glBufferData(GL_ARRAY_BUFFER, sizeof(glf::vertex_v4fc4f) * 6, NULL, GL_STATIC_DRAW);
//		gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

		return true;
	}
}
