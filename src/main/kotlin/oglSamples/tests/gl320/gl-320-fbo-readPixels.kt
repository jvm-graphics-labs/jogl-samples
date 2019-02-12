//package oglSamples.tests.gl320
//
//import com.jogamp.opengl.GL.*
//import com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER
//import com.jogamp.opengl.GL3
//import com.jogamp.opengl.GLAutoDrawable
//import glm.BYTES
//import glm.L
//import glm.glm
//import glm.mat.Mat4
//import oglSamples.framework.Framework
//import oglSamples.framework.semantic
//import uno.buffer.byteBufferBig
//import uno.buffer.floatBufferOf
//import uno.buffer.intBufferBig
//import uno.buffer.shortBufferOf
//import uno.caps.Caps.Profile
//import uno.gl.checkError
//import uno.glf.Vertex_v2fv2f
//import uno.glsl.programOf
//import java.nio.ByteBuffer
//
///**
// * Created by GBarbieri on 29.03.2017.
// */
//
//fun main(args: Array<String>) {
//    gl_320_fbo_readPixels().setup()
//}
//
//class gl_320_fbo_readPixels : Framework("gl-320-fbo-readPixels", Profile.CORE, 3, 2) {
//
//    val SHADER_ROOT = "gl-320"
//    val SHADER_TEXTURE = "fbo-readpixels-sample"
//    val SHADER_SPLASH = "fbo-readpixels-blit"
//
//    val vertexCount = 4
//    val vertexSize = vertexCount * Vertex_v2fv2f.SIZE
//    val VertexData = floatBufferOf(
//            -1.0f, -1.0f, /**/ 0.0f, 1.0f,
//            +1.0f, -1.0f, /**/ 1.0f, 1.0f,
//            +1.0f, +1.0f, /**/ 1.0f, 0.0f,
//            -1.0f, +1.0f, /**/ 0.0f, 0.0f)
//
//    val elementCount = 6
//    val elementSize = elementCount * Short.BYTES
//    val elementData = shortBufferOf(
//            0, 1, 2,
//            2, 3, 0)
//
//    object buffer {
//        val VERTEX = 0
//        val ELEMENT = 1
//        val TRANSFORM = 2
//        val MAX = 3
//    }
//
//    object texture {
//        val DIFFUSE = 0
//        val COLORBUFFER = 1
//        val RENDERBUFFER = 2
//        val MAX = 3
//    }
//
//    object program {
//        val TEXTURE = 0
//        val SPLASH = 1
//        val MAX = 2
//    }
//
//    val programName = IntArray(program.MAX)
//    val vertexArrayName = intBufferBig(program.MAX)
//    val bufferName = intBufferBig(buffer.MAX)
//    val textureName = intBufferBig(texture.MAX)
//    val uniformDiffuse = IntArray(program.MAX)
//    val framebufferName = intBufferBig(1)
//    val framebufferScale = 2
//    var uniformTransform = -1
//    lateinit var readBuffer: ByteBuffer
//
//    override fun begin(drawable: GLAutoDrawable) {
//
//        val gl = drawable.gl.gL3
//
//        readBuffer = byteBufferBig(640 * 480)
//
//        initProgram(gl)
//    }
//
//    fun initProgram(gl: GL3) = with(gl) {
//
//        programName[program.TEXTURE] = programOf(gl, this::class.java,
//                SHADER_ROOT, "$SHADER_TEXTURE.vert", "$SHADER_TEXTURE.frag",
//                mapOf(
//                        "Position" to semantic.attr.POSITION,
//                        "Texcoord" to semantic.attr.TEXCOORD,
//                        "Color" to semantic.attr.COLOR))
//
//        programName[program.SPLASH] = programOf(gl, this::class.java,
//                SHADER_ROOT, "$SHADER_SPLASH.vert", "$SHADER_SPLASH.frag",
//                mapOf("Color" to semantic.attr.COLOR))
//
//        uniformTransform = glGetUniformBlockIndex(programName[program.TEXTURE], "transform")
//        uniformDiffuse[program.TEXTURE] = glGetUniformLocation(programName[program.TEXTURE], "Diffuse")
//        uniformDiffuse[program.SPLASH] = glGetUniformLocation(programName[program.SPLASH], "Diffuse")
//
//        glUseProgram(programName[program.TEXTURE])
//        glUniform1i(uniformDiffuse[program.TEXTURE], 0)
//        glUniformBlockBinding(programName[program.TEXTURE], uniformTransform, semantic.uniform.TRANSFORM0)
//
//        glUseProgram(programName[program.SPLASH])
//        glUniform1i(uniformDiffuse[program.SPLASH], 0)
//
//        checkError(gl, "initProgram")
//    }
//
//    fun initBuffer(gl: GL3) = with(gl) {
//
//        glGenBuffers(buffer.MAX, bufferName)
//
//        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[buffer.ELEMENT])
//        glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize.L, elementData, GL_STATIC_DRAW)
//        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
//
//        glBindBuffer(GL_ARRAY_BUFFER, bufferName[buffer.VERTEX])
//        glBufferData(GL_ARRAY_BUFFER, vertexSize.L, VertexData, GL_STATIC_DRAW)
//        glBindBuffer(GL_ARRAY_BUFFER, 0)
//
//        val uniformBlockSize = glm.max(Mat4.SIZE, caps.limits.UNIFORM_BUFFER_OFFSET_ALIGNMENT)
//
//        glBindBuffer(GL_UNIFORM_BUFFER, bufferName[buffer.TRANSFORM])
//        glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize.L, null, GL_DYNAMIC_DRAW)
//        glBindBuffer(GL_UNIFORM_BUFFER, 0)
//    }
//}