//package oglSamples.tests.es300
//
//import com.jogamp.opengl.GL.*
//import com.jogamp.opengl.GL4ES3
//import com.jogamp.opengl.GLAutoDrawable
//import com.jogamp.opengl.GLException
//import glm.*
//import glm.mat.Mat4
//import glm.vec._2.Vec2
//import glm.vec._4.Vec4
//import oglSamples.framework.Framework
//import oglSamples.framework.semantic
//import uno.buffer.*
//import uno.caps.Caps.Profile
//import uno.gl.checkError
//import uno.glsl.programOf
//import uno.glsl.shaderCodeOf
//import uno.kotlin.shallThrow
//
///**
// * Created by GBarbieri on 30.03.2017.
// */
//
//fun main(args: Array<String>) {
//    es_300_draw_elements().setup()
//}
//
//class es_300_draw_elements : Framework("es-300-draw-elements", Profile.ES, 3, 0) {
//
//    val SHADER = "es-300/flat-color"
//    val FRAGMENT_FAIL = "$SHADER-fail"
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
//        println(caps.version.VENDOR)
//        println(caps.version.RENDERER)
//        println(caps.version.VERSION)
//        caps.extensions.list.forEach(::println)
//
//        initProgram(this)
//
//        initBuffer(this)
//
//        initVertexArray(this)
//    }
//
//    fun initProgram(gl: GL4ES3) = with(gl) {
//
//        // Check fail positive
//        { shaderCodeOf(gl, this::class.java, data + "$FRAGMENT_FAIL.frag") } shallThrow  GLException::class.java
//
//        // Create program
//        val attributes = mapOf("Position" to semantic.attr.POSITION)
//        programName = programOf(gl, this::class.java, data, "$SHADER.vert", "$SHADER.frag", attributes)
//
//        // Get variables locations
//        uniformMVP = glGetUniformLocation(programName, "MVP")
//        uniformDiffuse = glGetUniformLocation(programName, "Diffuse")
//
//        // Set some variables
//        // Bind the program for use
//        glUseProgram(programName)
//
//        // Set uniform value
//        glUniform4fv(uniformDiffuse, 1, Vec4(1.0f, 0.5f, 0.0f, 1.0f) to matBuffer)
//
//        // Unbind the program
//        glUseProgram(0)
//
//        checkError(gl, "initProgram")
//    }
//
//    fun initBuffer(gl: GL4ES3) = with(gl) {
//
//        glGenBuffers(buffer.MAX, bufferName)
//
//        glBindBuffer(GL_ARRAY_BUFFER, bufferName[buffer.VERTEX])
//        glBufferData(GL_ARRAY_BUFFER, positionSize.L, positionData, GL_STATIC_DRAW)
//        glBindBuffer(GL_ARRAY_BUFFER, 0)
//
//        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[buffer.ELEMENT])
//        glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize.L, elementData, GL_STATIC_DRAW)
//        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
//
//        checkError(gl, "initBuffer")
//    }
//
//    fun initVertexArray(gl: GL4ES3) = with(gl) {
//
//        glGenVertexArrays(1, vertexArrayName)
//        glBindVertexArray(vertexArrayName[0])
//        glBindBuffer(GL_ARRAY_BUFFER, bufferName[buffer.VERTEX])
//        glVertexAttribPointer(semantic.attr.POSITION, 2, GL_FLOAT, false, Vec2.SIZE, 0)
//        glBindBuffer(GL_ARRAY_BUFFER, 0)
//        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[buffer.ELEMENT])
//
//        glEnableVertexAttribArray(semantic.attr.POSITION)
//        glBindVertexArray(0)
//
//        checkError(gl, "initVertexArray")
//    }
//
//    override fun display(drawable: GLAutoDrawable) = with(drawable.gl.gL4ES3) {
//
//        buffers[0] = GL_BACK
////        glDrawBuffers(1, buffers)
//
//        // Compute the MVP (Model View Projection matrix)
//        val model = Mat4()
//        val mvp = projection * view() * model
//
//        // Clear color buffer with black
//        glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
//        glClearDepthf(1.0f)
//        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
//
//        // Bind program
//        glUseProgram(programName)
//
//        // Set the value of MVP uniform.
//        glUniformMatrix4fv(uniformMVP, 1, false, mvp to matBuffer)
//
//        glBindVertexArray(vertexArrayName[0])
//
//        glDrawElements(GL_TRIANGLES, elementCount, GL_UNSIGNED_INT, 0)
//    }
//
//    override fun reshape(drawable: GLAutoDrawable, x: Int, y: Int, width: Int, height: Int) = with(drawable.gl.gL4ES3) {
//
//        projection put glm.perspective(glm.PIf * 0.25f, width / height.f, 0.1f, 100.0f)
//
//        // Set the display viewport
//        glViewport(0, 0, windowSize.x, windowSize.y)
//    }
//
//    override fun end(drawable: GLAutoDrawable) = with(drawable.gl.gL4ES3) {
//
//        glDeleteBuffers(buffer.MAX, bufferName)
//        glDeleteVertexArrays(1, vertexArrayName)
//        glDeleteProgram(programName)
//
//        destroyBuffers(bufferName, vertexArrayName, buffers)
//    }
//}