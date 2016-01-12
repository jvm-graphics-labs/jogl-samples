/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_420;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_ALPHA;
import static com.jogamp.opengl.GL.GL_BACK;
import static com.jogamp.opengl.GL.GL_NEAREST;
import static com.jogamp.opengl.GL.GL_NONE;
import static com.jogamp.opengl.GL.GL_RGBA8;
import static com.jogamp.opengl.GL.GL_SCISSOR_TEST;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_WRITE_ONLY;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER_BIT;
import static com.jogamp.opengl.GL2ES2.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS;
import static com.jogamp.opengl.GL2ES2.GL_MAX_TEXTURE_IMAGE_UNITS;
import static com.jogamp.opengl.GL2ES2.GL_RED;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER_BIT;
import static com.jogamp.opengl.GL2ES3.GL_BLUE;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import static com.jogamp.opengl.GL2ES3.GL_GREEN;
import static com.jogamp.opengl.GL2ES3.GL_MAX_ARRAY_TEXTURE_LAYERS;
import static com.jogamp.opengl.GL2ES3.GL_MAX_COMBINED_IMAGE_UNIFORMS;
import static com.jogamp.opengl.GL2ES3.GL_MAX_FRAGMENT_IMAGE_UNIFORMS;
import static com.jogamp.opengl.GL2ES3.GL_MAX_GEOMETRY_IMAGE_UNIFORMS;
import static com.jogamp.opengl.GL2ES3.GL_MAX_IMAGE_UNITS;
import static com.jogamp.opengl.GL2ES3.GL_MAX_TESS_CONTROL_IMAGE_UNIFORMS;
import static com.jogamp.opengl.GL2ES3.GL_MAX_TESS_EVALUATION_IMAGE_UNIFORMS;
import static com.jogamp.opengl.GL2ES3.GL_MAX_VERTEX_IMAGE_UNIFORMS;
import static com.jogamp.opengl.GL2ES3.GL_READ_ONLY;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_BASE_LEVEL;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_MAX_LEVEL;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_SWIZZLE_A;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_SWIZZLE_B;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_SWIZZLE_G;
import static com.jogamp.opengl.GL2ES3.GL_TEXTURE_SWIZZLE_R;
import static com.jogamp.opengl.GL2GL3.GL_MAX_COMBINED_IMAGE_UNITS_AND_FRAGMENT_OUTPUTS;
import com.jogamp.opengl.GL4;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import jglm.Vec2;

/**
 *
 * @author GBarbieri
 */
public class Gl_420_image_store extends Test {

    public static void main(String[] args) {
        Gl_420_image_store gl_420_image_store = new Gl_420_image_store();
    }

    public Gl_420_image_store() {
        super("gl-420-image-store", Profile.CORE, 4, 2);
    }

    private final String SHADERS_SOURCE_SAVE = "image-store-write";
    private final String SHADERS_SOURCE_READ = "image-store-read";
    private final String SHADERS_ROOT = "src/data/gl_420";

    private enum Program {
        VERT_SAVE,
        FRAG_SAVE,
        VERT_READ,
        FRAG_READ,
        MAX
    }

    private enum Pipeline {
        READ,
        SAVE,
        MAX
    }

    private int[] vertexArrayName = {0}, textureName = {0}, programName = new int[Program.MAX.ordinal()],
            pipelineName = new int[Pipeline.MAX.ordinal()];
    private Vec2 imageSize = new Vec2();

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;

        logImplementationDependentLimit(gl4, GL_MAX_IMAGE_UNITS, "GL_MAX_IMAGE_UNITS");
        logImplementationDependentLimit(gl4, GL_MAX_VERTEX_IMAGE_UNIFORMS, "GL_MAX_VERTEX_IMAGE_UNIFORMS");
        logImplementationDependentLimit(gl4, GL_MAX_TESS_CONTROL_IMAGE_UNIFORMS, "GL_MAX_TESS_CONTROL_IMAGE_UNIFORMS");
        logImplementationDependentLimit(gl4, GL_MAX_TESS_EVALUATION_IMAGE_UNIFORMS,
                "GL_MAX_TESS_EVALUATION_IMAGE_UNIFORMS");
        logImplementationDependentLimit(gl4, GL_MAX_GEOMETRY_IMAGE_UNIFORMS, "GL_MAX_GEOMETRY_IMAGE_UNIFORMS");
        logImplementationDependentLimit(gl4, GL_MAX_FRAGMENT_IMAGE_UNIFORMS, "GL_MAX_FRAGMENT_IMAGE_UNIFORMS");
        logImplementationDependentLimit(gl4, GL_MAX_COMBINED_IMAGE_UNIFORMS, "GL_MAX_COMBINED_IMAGE_UNIFORMS");
        logImplementationDependentLimit(gl4, GL_MAX_ARRAY_TEXTURE_LAYERS, "GL_MAX_ARRAY_TEXTURE_LAYERS");
        logImplementationDependentLimit(gl4, GL_MAX_TEXTURE_IMAGE_UNITS, "GL_MAX_TEXTURE_IMAGE_UNITS");
        logImplementationDependentLimit(gl4, GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS, "GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS");
        logImplementationDependentLimit(gl4, GL_MAX_COMBINED_IMAGE_UNITS_AND_FRAGMENT_OUTPUTS,
                "GL_MAX_COMBINED_IMAGE_UNITS_AND_FRAGMENT_OUTPUTS");
        //this->logImplementationDependentLimit(GL_MAX_TEXTURE_UNITS, "GL_MAX_TEXTURE_UNITS");

        if (validated) {
            validated = initTexture(gl4);
        }
        if (validated) {
            validated = initProgram(gl4);
        }
        if (validated) {
            validated = initVertexArray(gl4);
        }

        return validated;
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        try {

            if (validated) {

                String[] vertexSourceContent = new String[]{
                    new Scanner(new File(SHADERS_ROOT + "/" + SHADERS_SOURCE_READ + ".vert")).useDelimiter("\\A").next()};
                programName[Program.VERT_READ.ordinal()]
                        = gl4.glCreateShaderProgramv(GL_VERTEX_SHADER, 1, vertexSourceContent);
            }

            if (validated) {

                String[] fragmentSourceContent = new String[]{
                    new Scanner(new File(SHADERS_ROOT + "/" + SHADERS_SOURCE_READ + ".frag")).useDelimiter("\\A").next()};
                programName[Program.FRAG_READ.ordinal()]
                        = gl4.glCreateShaderProgramv(GL_FRAGMENT_SHADER, 1, fragmentSourceContent);
            }

            if (validated) {

                String[] vertexSourceContent = new String[]{
                    new Scanner(new File(SHADERS_ROOT + "/" + SHADERS_SOURCE_SAVE + ".vert")).useDelimiter("\\A").next()};
                programName[Program.VERT_SAVE.ordinal()]
                        = gl4.glCreateShaderProgramv(GL_VERTEX_SHADER, 1, vertexSourceContent);
            }

            if (validated) {

                String[] fragmentSourceContent = new String[]{
                    new Scanner(new File(SHADERS_ROOT + "/" + SHADERS_SOURCE_SAVE + ".frag")).useDelimiter("\\A").next()};
                programName[Program.FRAG_SAVE.ordinal()]
                        = gl4.glCreateShaderProgramv(GL_FRAGMENT_SHADER, 1, fragmentSourceContent);
            }

            if (validated) {

                validated = validated && checkProgram(gl4, programName[Program.VERT_READ.ordinal()]);
                validated = validated && checkProgram(gl4, programName[Program.FRAG_READ.ordinal()]);
                validated = validated && checkProgram(gl4, programName[Program.VERT_SAVE.ordinal()]);
                validated = validated && checkProgram(gl4, programName[Program.FRAG_SAVE.ordinal()]);
            }

            if (validated) {

                gl4.glGenProgramPipelines(Pipeline.MAX.ordinal(), pipelineName, 0);
                gl4.glUseProgramStages(pipelineName[Pipeline.READ.ordinal()],
                        GL_VERTEX_SHADER_BIT, programName[Program.VERT_READ.ordinal()]);
                gl4.glUseProgramStages(pipelineName[Pipeline.READ.ordinal()],
                        GL_FRAGMENT_SHADER_BIT, programName[Program.FRAG_READ.ordinal()]);
                gl4.glUseProgramStages(pipelineName[Pipeline.SAVE.ordinal()],
                        GL_VERTEX_SHADER_BIT, programName[Program.VERT_SAVE.ordinal()]);
                gl4.glUseProgramStages(pipelineName[Pipeline.SAVE.ordinal()],
                        GL_FRAGMENT_SHADER_BIT, programName[Program.FRAG_SAVE.ordinal()]);
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Gl_420_image_store.class.getName()).log(Level.SEVERE, null, ex);
        }

        return validated && checkError(gl4, "initProgram");
    }

    private boolean initTexture(GL4 gl4) {

        gl4.glGenTextures(1, textureName, 0);
        gl4.glActiveTexture(GL_TEXTURE0);
        gl4.glBindTexture(GL_TEXTURE_2D, textureName[0]);
        gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
        gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 1);
        gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_R, GL_RED);
        gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_G, GL_GREEN);
        gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_B, GL_BLUE);
        gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_A, GL_ALPHA);
        gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        gl4.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        gl4.glTexStorage2D(GL_TEXTURE_2D, 1, GL_RGBA8, windowSize.x, windowSize.y);
        gl4.glBindTexture(GL_TEXTURE_2D, 0);

        return true;
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(1, vertexArrayName, 0);
        gl4.glBindVertexArray(vertexArrayName[0]);
        gl4.glBindVertexArray(0);

        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glViewportIndexedf(0, 0, 0, windowSize.x, windowSize.y);
        gl4.glDrawBuffer(GL_BACK);
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{0.0f, 0.5f, 1.0f, 1.0f}, 0);

        // Renderer to image
        {
            gl4.glDrawBuffer(GL_NONE);

            gl4.glBindProgramPipeline(pipelineName[Pipeline.SAVE.ordinal()]);
            gl4.glBindImageTexture(Semantic.Image.DIFFUSE, textureName[0], 0, false, 0, GL_WRITE_ONLY, GL_RGBA8);
            gl4.glBindVertexArray(vertexArrayName[0]);
            gl4.glDrawArraysInstancedBaseInstance(GL_TRIANGLES, 0, 3, 1, 0);
        }

        // Read from image
        {
            int border = 8;
            gl4.glEnable(GL_SCISSOR_TEST);
            gl4.glScissorIndexed(0, border, border, (windowSize.x - 2) * border, (windowSize.y - 2) * border);

            gl4.glDrawBuffer(GL_BACK);

            gl4.glBindProgramPipeline(pipelineName[Pipeline.READ.ordinal()]);
            gl4.glBindImageTexture(Semantic.Image.DIFFUSE, textureName[0], 0, false, 0, GL_READ_ONLY, GL_RGBA8);
            gl4.glBindVertexArray(vertexArrayName[0]);
            gl4.glDrawArraysInstancedBaseInstance(GL_TRIANGLES, 0, 3, 1, 0);

            gl4.glDisable(GL_SCISSOR_TEST);
        }

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteTextures(1, textureName, 0);
        gl4.glDeleteVertexArrays(1, vertexArrayName, 0);
        gl4.glDeleteProgram(programName[Program.VERT_SAVE.ordinal()]);
        gl4.glDeleteProgram(programName[Program.FRAG_SAVE.ordinal()]);
        gl4.glDeleteProgram(programName[Program.VERT_READ.ordinal()]);
        gl4.glDeleteProgram(programName[Program.FRAG_READ.ordinal()]);
        gl4.glDeleteProgramPipelines(Pipeline.MAX.ordinal(), pipelineName, 0);

        return true;
    }
}
