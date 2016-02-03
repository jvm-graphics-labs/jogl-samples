/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_500;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import framework.Profile;
import framework.Test;

/**
 *
 * @author GBarbieri
 */
public class Gl_500_shader_blend_intel extends Test {

    public static void main(String[] args) {
        Gl_500_shader_blend_intel gl_500_shader_blend_intel = new Gl_500_shader_blend_intel();
    }

    public Gl_500_shader_blend_intel() {
        super("gl-500-shader-blend-intel", Profile.CORE, 4, 5);
    }

    private final String SHADERS_SOURCE_TEXTURE = "sample-location-render";
    private final String SHADERS_SOURCE_SPLASH = "sample-location-splash";
    private final String SHADERS_ROOT = "src/data/gl_500";

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = checkExtension(gl4, "GL_INTEL_fragment_shader_ordering");

        /*
		glm::vec2 const WindowSize(this->getWindowSize());
		glm::vec2 const WindowRange = WindowSize * 3.f;

		this->Viewports.resize(1000);
		for (std::size_t i = 0; i < this->Viewports.size(); ++i)
		{
			glm::vec2 const ViewportPos(i % 17u, i % 13u);
			glm::vec2 const ViewportSize(i % 11u);
			this->Viewports[i] = glm::vec4(ViewportPos / glm::vec2(17, 13) * WindowRange - WindowSize, ViewportSize / glm::vec2(11));
		}
         */
//		glm::vec2 WindowSize(this->getWindowSize());
//		this->Viewports.resize(1000);
//		for (std::size_t i = 0; i < this->Viewports.size(); ++i)
//		{
//			this->Viewports[i] = glm::vec4(
//				glm::linearRand(-WindowSize.x, WindowSize.x * 2.0f), 
//				glm::linearRand(-WindowSize.y, WindowSize.y * 2.0f),
//				WindowSize * glm::linearRand(0.0f, 1.0f));
//		}
//
//		if(Validated)
//			Validated = initProgram();
//		if(Validated)
//			Validated = initBuffer();
//		if(Validated)
//			Validated = initVertexArray();
//		if(Validated)
//			Validated = initTexture();
//		if(Validated)
//			Validated = initFramebuffer();
        return validated;
    }
}
