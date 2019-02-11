package oglSamples.tests.es200

import com.jogamp.opengl.GL.*
import com.jogamp.opengl.GL2ES2
import com.jogamp.opengl.GLAutoDrawable
import glm_.BYTES
import glm.L
import glm.f
import glm.glm
import glm.mat.Mat4
import glm.vec._2.Vec2
import glm.vec._4.Vec4
import oglSamples.framework.Framework
import oglSamples.framework.semantic
import uno.buffer.destroy
import uno.buffer.floatBufferOf
import uno.buffer.intBufferBig
import uno.buffer.shortBufferOf
import uno.caps.Caps.Profile
import uno.gl.checkError
import uno.glsl.programOf

/**
 * Created by GBarbieri on 27.03.2017.
 */

fun main() {
    es_200_draw_elements().setup()
}

class es_200_draw_elements : Framework("es-200-draw-elements", Profile.ES, 2, 0) {

    val SHADER = "es-200/flat-color"

    val elementCount = 6
    val elementSize = elementCount * Short.BYTES
    val elementData = shortBufferOf(
            0, 1, 2,
            0, 2, 3)

    val vertexCount = 4
    val positionSize = vertexCount * Vec2.SIZE
    val positionData = floatBufferOf(
            -1.0f, -1.0f,
            +1.0f, -1.0f,
            +1.0f, +1.0f,
            -1.0f, +1.0f)

    object Buffer {
        val VERTEX = 0
        val ELEMENT = 1
        val MAX = 2
    }

    val bufferName = intBufferBig(Buffer.MAX)
    var programName = 0
    var uniformMVP = 0
    var uniformDiffuse = 0

    val projection = Mat4()

    override fun begin(drawable: GLAutoDrawable) = with(drawable.gl.gL2ES2) {

        println(caps.version.VENDOR)
        println(caps.version.RENDERER)
        println(caps.version.VERSION)
        caps.extensions.list.forEach(::println)

        initProgram(this)

        initBuffer(this)
    }

    fun initProgram(gl: GL2ES2) = with(gl) {

        // Create program
        val attributes = mapOf("Position" to semantic.attr.POSITION)
        programName = programOf(gl, this::class.java, data, "$SHADER.vert", "$SHADER.frag", attributes)

        // Get variables locations
        uniformMVP = glGetUniformLocation(programName, "MVP")
        uniformDiffuse = glGetUniformLocation(programName, "Diffuse")

        // Set some variables
        // Bind the program for use
        glUseProgram(programName)

        // Set uniform value
        glUniform4fv(uniformDiffuse, 1, Vec4(1.0f, 0.5f, 0.0f, 1.0f) to matBuffer)

        // Unbind the program
        glUseProgram(0)

        checkError(gl, "initProgram")
    }

    fun initBuffer(gl: GL2ES2) = with(gl) {

        glGenBuffers(Buffer.MAX, bufferName)

        glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX])
        glBufferData(GL_ARRAY_BUFFER, positionSize.L, positionData, GL_STATIC_DRAW)
        glBindBuffer(GL_ARRAY_BUFFER, 0)

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT])
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize.L, elementData, GL_STATIC_DRAW)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)

        checkError(gl, "initBuffer")
    }

    override fun display(drawable: GLAutoDrawable) = with(drawable.gl.gL2ES2) {

        // Compute the MVP (Model View Projection matrix)
        val model = Mat4()
        val mvp = projection * view() * model

        // Clear color buffer with black
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        glClearDepthf(1.0f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        // Bind program
        glUseProgram(programName)

        // Set the value of MVP uniform.
        glUniformMatrix4fv(uniformMVP, 1, false, mvp to matBuffer)

        glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX])
        glVertexAttribPointer(semantic.attr.POSITION, 2, GL_FLOAT, false, Vec2.SIZE, 0)
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT])

        glEnableVertexAttribArray(semantic.attr.POSITION)
        glDrawElements(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0)
        glDisableVertexAttribArray(semantic.attr.POSITION)

        // Unbind program
        glUseProgram(0)
    }

    override fun reshape(drawable: GLAutoDrawable, x: Int, y: Int, width: Int, height: Int) = with(drawable.gl.gL2ES2) {

        projection put glm.perspective(glm.PIf * 0.25f, width / height.f, 0.1f, 100.0f)

        // Set the display viewport
        glViewport(0, 0, windowSize.x, windowSize.y)
    }

    override fun end(drawable: GLAutoDrawable) = with(drawable.gl.gL2ES2) {
        glDeleteBuffers(Buffer.MAX, bufferName)
        glDeleteProgram(programName)
        bufferName.destroy()
    }
}