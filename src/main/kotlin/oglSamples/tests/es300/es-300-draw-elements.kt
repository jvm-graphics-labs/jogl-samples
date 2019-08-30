package oglSamples.tests.es300

import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec4.Vec4
import gln.BufferTarget.Companion.ARRAY
import gln.BufferTarget.Companion.ELEMENT_ARRAY
import gln.UniformLocation
import gln.cap.Caps.Profile.ES
import gln.draw.glDrawElements
import gln.gl
import gln.glViewport
import gln.glf.glf
import gln.glf.semantic
import gln.identifiers.GlBuffer
import gln.identifiers.GlProgram
import gln.uniform.glUniform
import gln.vertexArray.GlVertexArray
import kool.intBufferOf
import oglSamples.framework.Framework
import oglSamples.vec2BufferOf
import org.lwjgl.opengl.GL11C.*
import org.lwjgl.opengl.GL20C
import org.lwjgl.opengl.GL20C.glDrawBuffers
import org.lwjgl.opengl.GL41C.glClearDepthf
import java.lang.Exception

/**
 * Created by GBarbieri on 30.03.2017.
 */

fun main() {
    es_300_draw_elements()()
}

private class es_300_draw_elements : Framework("es-300-draw-elements", ES, 3, 0) {

    val SHADER_SOURCE = "es-300/flat-color"
    val FRAGMENT_SHADER_SOURCE_FAIL = "es-300/flat-color-fail.frag"

    val elements = intBufferOf(
            0, 1, 2,
            0, 2, 3)

    val positions = vec2BufferOf(
            Vec2(-1f, -1f),
            Vec2(+1f, -1f),
            Vec2(+1f, +1f),
            Vec2(-1f, +1f))

    var vertexArray = GlVertexArray()
    var program = GlProgram.NULL
    var arrayBuffer = GlBuffer()
    var elementBuffer = GlBuffer()
    var uniformMVP: UniformLocation = 0
    var uniformDiffuse: UniformLocation = 0

    override fun begin(): Boolean {

        var validated = true

        window.caps.apply {
            println(version.VENDOR)
            println(version.RENDERER)
            println(version.VERSION)
            println(extensions.list)
        }

        if (validated)
            validated = initProgram()
        if (validated)
            validated = initBuffer()
        if (validated)
            validated = initVertexArray()

        return validated
    }

    fun initProgram(): Boolean {

        // Check fail positive
        var validated = try {
            GlProgram.initFromPath("", FRAGMENT_SHADER_SOURCE_FAIL)
            false
        } catch (_: Exception) {
            true
        }

        // Create program
        if (validated)
            try {
                program = GlProgram.initFromPath(SHADER_SOURCE) {
                    "Position".attrib = semantic.attr.POSITION
                }
            } catch (_: Exception) {
                validated = false
            }

        // Get variables locations
        if (validated)
            program {
                uniformMVP = "MVP".uniform
                uniformDiffuse = "Diffuse".uniform
            }

        // Set some variables
        if (validated)
        // Bind the program for use
            program.used {
                // Set uniform value
                Vec4(1f, 0.5f, 0f, 1f) to uniformDiffuse
            } // Unbind the program

        return validated && checkError("initProgram")
    }

    fun initBuffer(): Boolean {

        gl {
            genBuffers(::arrayBuffer).bound(ARRAY) {
                data(positions.data)
            }
            genBuffers(::elementBuffer).bound(ELEMENT_ARRAY) {
                data(elements)
            }
        }
        return checkError("initBuffer")
    }

    fun initVertexArray(): Boolean {

        gl.genVertexArrays(::vertexArray).bound {
            arrayBuffer.bound(ARRAY) {
                glf.pos2.set()
            }
            elementBuffer bind ELEMENT_ARRAY

            glf.pos2.enable()
        }

        return checkError("initVertexArray")
    }

    override fun render(): Boolean {

        glDrawBuffers(GL_BACK)

        // Compute the MVP (Model View Projection matrix)
        val projection = glm.perspective(glm.πf * 0.25f, windowSize.aspect, 0.1f, 100f)
        val model = Mat4()
        val mvp = projection * this.view * model

        // Set the display viewport
        glViewport(windowSize)

        // Clear color buffer with black
        glClearColor(0f, 0f, 0f, 1f)
        glClearDepthf(1f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        // Bind program
        program.use()

        // Set the value of MVP uniform.
        glUniform(uniformMVP, mvp)

        vertexArray.bind()

        glDrawElements(elements)

        return true
    }

    override fun end(): Boolean {

        // Delete objects
        arrayBuffer.delete()
        elementBuffer.delete()
        program.delete()

        return true
    }
}