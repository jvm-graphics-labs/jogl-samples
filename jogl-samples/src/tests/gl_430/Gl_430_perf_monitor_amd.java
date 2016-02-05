/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_430;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import framework.Profile;
import framework.Test;
import dev.Vec2;
import dev.Vec2i;

/**
 *
 * @author elect
 */
public class Gl_430_perf_monitor_amd extends Test {

    public static void main(String[] args) {
        Gl_430_perf_monitor_amd gl_430_perf_monitor_amd = new Gl_430_perf_monitor_amd();
    }

    public Gl_430_perf_monitor_amd() {
        super("gl-430-perf-monitor-amd", Profile.CORE, 4, 3, new Vec2i(640, 480), new Vec2(-Math.PI * 0.2f));
    }

    private final String SHADERS_SOURCE_TEXTURE = "fbo-texture-2d";
    private final String SHADERS_SOURCE_SPLASH = "fbo-splash";
    private final String SHADERS_ROOT = "src/data/gl_430";
    private final String TEXTURE_DIFFUSE = "kueken7_rgba_dxt1_unorm.dds";

    private int vertexCount = 4;
    private int vertexSize = vertexCount * 2 * 2 * Float.BYTES;
    private float[] vertexData = {
        -1.0f, -1.0f, 0.0f, 1.0f,
        +1.0f, -1.0f, 1.0f, 1.0f,
        +1.0f, +1.0f, 1.0f, 0.0f,
        -1.0f, +1.0f, 0.0f, 0.0f};

    private int elementCount = 6;
    private int elementSize = elementCount * Short.BYTES;
    private short[] elementData = {
        0, 1, 2,
        2, 3, 0};

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int ELEMENT = 1;
        public static final int TRANSFORM = 2;
        public static final int MAX = 3;
    }

    private class Texture {

        public static final int DIFFUSE = 0;
        public static final int COLORBUFFER = 1;
        public static final int RENDERBUFFER = 2;
        public static final int MAX = 3;
    }

    private class Pipeline {

        public static final int TEXTURE = 0;
        public static final int SPLASH = 1;
        public static final int MAX = 2;
    }

    private int[] framebufferName = {0}, pipelineName = new int[Pipeline.MAX],
            programName = new int[Pipeline.MAX], vertexArrayName = new int[Pipeline.MAX],
            bufferName = new int[Buffer.MAX], texturename = new int[Texture.MAX];

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;
        validated = validated && checkExtension(gl4, "GL_AMD_performance_monitor");

//		if(validated)
//		{
//			this->Monitor.reset(new monitor());
//			this->Monitor->record("CP", 1);
//		}
//
//		if(validated)
//			validated = initProgram();
//		if(validated)
//			validated = initBuffer();
//		if(validated)
//			validated = initVertexArray();
//		if(validated)
//			validated = initTexture();
//		if(validated)
//			validated = initFramebuffer();
        return validated;
    }
}
