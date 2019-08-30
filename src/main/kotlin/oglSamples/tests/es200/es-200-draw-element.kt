package oglSamples.tests.es200

import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec4.Vec4
import gln.BufferTarget.Companion.ARRAY
import gln.BufferTarget.Companion.ELEMENT_ARRAY
import gln.DataType.Companion.UNSIGNED_SHORT
import gln.cap.Caps.Profile.ES
import gln.draw.glDrawElements
import gln.gl
import gln.glViewport
import gln.glf.glf
import gln.identifiers.GlBuffers
import gln.identifiers.GlProgram
import gln.uniform.glUniform
import gln.vertexArray.glDisableVertexAttribArray
import gln.vertexArray.glEnableVertexAttribArray
import gln.vertexArray.glVertexAttribPointer
import kool.rem
import kool.shortBufferOf
import oglSamples.framework.Framework
import oglSamples.framework.semantic
import oglSamples.tests.es200.es_200_draw_elements.Buffer.ELEMENT
import oglSamples.tests.es200.es_200_draw_elements.Buffer.VERTEX
import oglSamples.vec2BufferOf
import org.lwjgl.opengl.GL41C.*


fun main() {
    es_200_draw_elements()()
}

private class es_200_draw_elements : Framework("es-200-draw-elements", ES, 2, 0) {

    val SHADER_SOURCE = "es-200/flat-color"

    val elements = shortBufferOf(
            0, 1, 2,
            0, 2, 3)

    val positions = vec2BufferOf(
            Vec2(-1f, -1f),
            Vec2(+1f, -1f),
            Vec2(+1f, +1f),
            Vec2(-1f, +1f))

    enum class Buffer { VERTEX, ELEMENT }

    val buffers = GlBuffers<Buffer>()

    var program = GlProgram.NULL
    var uniformMVP = -1
    var uniformDiffuse = -1

    override fun begin(): Boolean {

        window.caps.apply {
            println(version.VENDOR)
            println(version.RENDERER)
            println(version.VERSION)
            println(extensions.list)
        }
        var validated = initProgram()
        if (validated)
            validated = initBuffer()

        return validated
    }

    fun initProgram(): Boolean {

        // Create program
        val validated = try {
            program = GlProgram.initFromPath(SHADER_SOURCE) {
                "Position".attrib = semantic.attr.POSITION
            }
            true
        } catch (_: Exception) {
            false
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
                glUniform(uniformDiffuse, Vec4(1f, 0.5f, 0f, 1f))
            }   // Unbind the program

        return validated && checkError("initProgram")
    }

    fun initBuffer(): Boolean {

        buffers.gen {
            VERTEX.bind(ARRAY) { data(positions.data) }
            ELEMENT.bind(ELEMENT_ARRAY) { data(elements) }
        }

        return checkError("initBuffer")
    }

    override fun end(): Boolean {

        buffers.delete()
        program.delete()

        return true
    }

    override fun render(): Boolean {

        // Compute the MVP (Model View Projection matrix)
        val projection = glm.perspective(glm.πf * 0.25f, window.aspect, 0.1f, 100f)
        val model = Mat4(1f)
        val mvp = projection * this.view * model

        // Set the display viewport
        glViewport(windowSize)

        // Clear color buffer with black
        glClearColor(0f, 0f, 0f, 1f)
        glClearDepthf(1f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        // Bind program
        program.used {

            // Set the value of MVP uniform.
            glUniform(uniformMVP, mvp)

            buffers {
                VERTEX.bind(ARRAY) {
                    glf.pos2.set()
                }
                ELEMENT bind ELEMENT_ARRAY
            }

            glf.pos2.enabled {
                glDrawElements(elements)
            }
        }   // Unbind program

        return true
    }
}