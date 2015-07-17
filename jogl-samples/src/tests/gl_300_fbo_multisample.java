/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.Semantic;
import framework.Test;

/**
 *
 * @author gbarbieri
 */
public class gl_300_fbo_multisample extends Test {

    private final String VERT_SHADER = "image-2d.vp";
    private final String FRAG_SHADER = "image-2d.fp";
    private final String SHADER_ROOT = "../shaders";
    private int programName;

    public gl_300_fbo_multisample(String title, int majorVersionRequire, int minorVersionRequire) {
        super("gl_300_fbo_multisample", 3, 0);
    }

    @Override
    protected boolean begin(GL3 gl3) {

        boolean validated = true;

        return validated & checkError(gl3, "begin");
    }

    private boolean initProgram(GL3 gl3) {

        boolean validated = true;

        ShaderCode vertShader = ShaderCode.create(gl3, GL_VERTEX_SHADER, this.getClass(),
                SHADER_ROOT, SHADER_ROOT + "/bin", VERT_SHADER, true);
        ShaderCode fragShader = ShaderCode.create(gl3, GL_VERTEX_SHADER, this.getClass(),
                SHADER_ROOT, SHADER_ROOT + "/bin", FRAG_SHADER, true);

        vertShader.defaultShaderCustomization(gl3, true, true);
        fragShader.defaultShaderCustomization(gl3, true, true);

        ShaderProgram program = new ShaderProgram();        
        program.add(vertShader);
        program.add(fragShader);        
        program.link(gl3, System.out);
        
        programName = program.program();

        gl3.glBindAttribLocation(programName, Semantic.Attr.position, "position");
        gl3.glBindAttribLocation(programName, Semantic.Attr.texCoord, "texCoord");
        
        
    }
}
