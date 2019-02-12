//package oglSamples.tests.es300
//
//import com.jogamp.opengl.GL4ES3
//import com.jogamp.opengl.GLAutoDrawable
//import glm.BYTES
//import glm.mat.Mat4
//import glm.vec._2.Vec2
//import oglSamples.framework.Framework
//import uno.buffer.floatBufferOf
//import uno.buffer.intBufferBig
//import uno.buffer.intBufferOf
//import uno.caps.Caps
//
///**
// * Created by GBarbieri on 30.03.2017.
// */
//
//fun main(args: Array<String>) {
//    es_300_fbo_srgb().setup()
//}
//
//class es_300_fbo_srgb : Framework("es-300-fbo-srgb", Caps.Profile.ES, 3, 0) {
//
//    val SHADER_RENDER = "es-300/flat-srgb"
//    val SHADER_SPLASH = "es-300/fbo-srgb-blit"
//
//    val elementCount = 6
//    val elementSize = elementCount * Int.BYTES
//    val elementData = intBufferOf(
//            0, 1, 2,
//            0, 2, 3)
//
//    val vertexCount = 4
//    val positionSize = vertexCount * Vec2.SIZE
//    val positionData = floatBufferOf(
//            -1.0f, -1.0f,
//            +1.0f, -1.0f,
//            +1.0f, +1.0f,
//            -1.0f, +1.0f)
//
//    object buffer {
//        val VERTEX = 0
//        val ELEMENT = 1
//        val MAX = 2
//    }
//
//    val bufferName = intBufferBig(buffer.MAX)
//    val vertexArrayName = intBufferBig(1)
//    var programName = 0
//    var uniformMVP = 0
//    var uniformDiffuse = 0
//
//    val projection = Mat4()
//
//    val buffers = intBufferBig(1)
//
//    override fun begin(drawable: GLAutoDrawable) = with(drawable.gl.gL4ES3) {
//
//        initProgram(this)
//
////        initBuffer(this)
////
////        initVertexArray(this)
//    }
//
//    fun initProgram(gL4ES3: GL4ES3) {
//
//
//    }
//}