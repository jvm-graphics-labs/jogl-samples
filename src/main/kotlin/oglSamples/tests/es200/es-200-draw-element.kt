package oglSamples.tests.es200

import glm_.BYTES
import glm_.vec2.Vec2
import gln.cap.Caps
import gln.objects.GlShader
import gln.program.GlslProgram
import kool.IntBuffer
import kool.shortBufferOf
import oglSamples.framework.Framework
import oglSamples.vec2BufferOf
import org.lwjgl.opengl.GL11C.*


fun main() {
    es_200_draw_elements().setup()
}

class es_200_draw_elements : Framework("es-200-draw-elements", Caps.Profile.ES, 2, 0) {

    val VERTEX_SHADER_SOURCE = "es-200/flat-color.vert"
    val FRAGMENT_SHADER_SOURCE = "es-200/flat-color.frag"

    val elementCount = 6
    val elementSize = elementCount * Short.BYTES
    val elementData = shortBufferOf(
            0, 1, 2,
            0, 2, 3)

    val vertexCount = 4
    val positionSize = vertexCount * Vec2.size
    val positionData = vec2BufferOf(
            Vec2(-1f, -1f),
            Vec2(+1f, -1f),
            Vec2(+1f, +1f),
            Vec2(-1f, +1f))

    enum class Buffer { VERTEX, ELEMENT }

    val bufferName = IntBuffer<Buffer>()
    lateinit var programName: GlslProgram
    var uniformMVP = 0
    var uniformDiffuse = 0

    override fun begin(): Boolean {

        var validated = true

        val vendor = glGetString (GL_VENDOR)
        println(vendor)
        val renderer = glGetString (GL_RENDERER)
        println(renderer)
        val version = glGetString (GL_VERSION)
        println(version)
        val extensions = glGetString (GL_EXTENSIONS)
        println(extensions)

        if (validated)
            validated = initProgram()
        if (validated)
            validated = initBuffer()

        return validated
    }

    fun initProgram()    {

        var validated = true

        // Create program
        if (validated) {
            val vertShaderName = GlShader. create (GL_VERTEX_SHADER, getDataDirectory()+VERTEX_SHADER_SOURCE)
            GLuint FragShaderName = Compiler . create (GL_FRAGMENT_SHADER, getDataDirectory()+FRAGMENT_SHADER_SOURCE)

            ProgramName = glCreateProgram()
            glAttachShader(ProgramName, VertShaderName)
            glAttachShader(ProgramName, FragShaderName)

            glBindAttribLocation(ProgramName, semantic::attr::POSITION, "Position")
            glLinkProgram(ProgramName)

            Validated = Validated && Compiler.check()
            Validated = Validated && Compiler.check_program(ProgramName)
        }

        // Get variables locations
        if (Validated) {
            UniformMVP = glGetUniformLocation(ProgramName, "MVP")
            UniformDiffuse = glGetUniformLocation(ProgramName, "Diffuse")
        }

        // Set some variables
        if (Validated) {
            // Bind the program for use
            glUseProgram(ProgramName)

            // Set uniform value
            glUniform4fv(UniformDiffuse, 1, & glm ::vec4(1.0f, 0.5f, 0.0f, 1.0f)[0])

            // Unbind the program
            glUseProgram(0)
        }

        return Validated && this->checkError("initProgram")
    }

    bool initBuffer()
    {
        glGenBuffers(static_cast<GLsizei>(BufferName.size()), & BufferName [0])

        glBindBuffer(GL_ARRAY_BUFFER, BufferName[buffer::VERTEX])
        glBufferData(GL_ARRAY_BUFFER, PositionSize, PositionData, GL_STATIC_DRAW)
        glBindBuffer(GL_ARRAY_BUFFER, 0)

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, BufferName[buffer::ELEMENT])
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, ElementSize, ElementData, GL_STATIC_DRAW)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)

        return this->checkError("initBuffer")
    }



    bool end()
    {
        glDeleteBuffers(static_cast<GLsizei>(BufferName.size()), & BufferName [0])
        glDeleteProgram(ProgramName)

        return true
    }

    bool render()
    {
        // Compute the MVP (Model View Projection matrix)
        glm::mat4 Projection = glm ::perspective(glm::pi<float>() * 0.25f, 4.0f / 3.0f, 0.1f, 100.0f)
        glm::mat4 Model = glm ::mat4(1.0f)
        glm::mat4 MVP = Projection * this->view() * Model

        // Set the display viewport
        glm::uvec2 WindowSize = this->getWindowSize()
        glViewport(0, 0, WindowSize.x, WindowSize.y)

        // Clear color buffer with black
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        glClearDepthf(1.0f)
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)

        // Bind program
        glUseProgram(ProgramName)

        // Set the value of MVP uniform.
        glUniformMatrix4fv(UniformMVP, 1, GL_FALSE, & MVP [0][0])

        glBindBuffer(GL_ARRAY_BUFFER, BufferName[buffer::VERTEX])
        glVertexAttribPointer(semantic::attr::POSITION, 2, GL_FLOAT, GL_FALSE, 0, 0)
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, BufferName[buffer::ELEMENT])

        glEnableVertexAttribArray(semantic::attr::POSITION)
        glDrawElements(GL_TRIANGLES, ElementCount, GL_UNSIGNED_SHORT, 0)
        glDisableVertexAttribArray(semantic::attr::POSITION)

        // Unbind program
        glUseProgram(0)

        return true
    }
}