package oglSamples.tests.gl300

import gli_.Texture2d
import gli_.gli
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import gln.cap.Caps
import gln.objects.GlProgram
import kool.IntBuffer
import oglSamples.GlArrayBuffer
import oglSamples.Vertex_v2v2
import oglSamples.framework.Framework
import oglSamples.framework.semantic
import oglSamples.glGenBuffers
import oglSamples.vertex_v2v2_BufferOf

fun main() {

}

class gl_300_fbo_multisample : Framework("gl-300-fbo-multisample", Caps.Profile.COMPATIBILITY, 3, 0) {

    val SHADER_SOURCE = "gl-300/image-2d"
    val TEXTURE_DIFFUSE = "kueken7_rgba8_srgb.dds"
    val FRAMEBUFFER_SIZE = Vec2i(160, 120)

    // With DDS textures, v texture coordinate are reversed, from top to bottom
    val vertexCount = 6
    val vertexSize = vertexCount * Vertex_v2v2.size
    val vertexData = vertex_v2v2_BufferOf(
            Vertex_v2v2(Vec2(-2.0f, -1.5f), Vec2(0.0f, 0.0f)),
            Vertex_v2v2(Vec2(+2.0f, -1.5f), Vec2(1.0f, 0.0f)),
            Vertex_v2v2(Vec2(+2.0f, +1.5f), Vec2(1.0f, 1.0f)),
            Vertex_v2v2(Vec2(+2.0f, +1.5f), Vec2(1.0f, 1.0f)),
            Vertex_v2v2(Vec2(-2.0f, +1.5f), Vec2(0.0f, 1.0f)),
            Vertex_v2v2(Vec2(-2.0f, -1.5f), Vec2(0.0f, 0.0f)))

//    program(0),
//    VertexArrayName(0),
//    BufferName(0),
//    TextureName(0),
//    ColorRenderbufferName(0),
//    ColorTextureName(0),
//    FramebufferRenderName(0),
//    FramebufferResolveName(0),
//    UniformMVP(-1),
//    UniformDiffuse(-1)
//    {}

    var program = GlProgram.NULL
    val vertexArrayName = IntBuffer(1)
    val bufferName = GlArrayBuffer()
    //    GLuint TextureName
//    GLuint ColorRenderbufferName
//    GLuint ColorTextureName
//    GLuint FramebufferRenderName
//    GLuint FramebufferResolveName
    var uniformMVP = 0
    var uniformDiffuse = 0

    override fun begin(): Boolean {

        var validated = true

        val caps = Caps(Caps.Profile.COMPATIBILITY)

        if (validated)
            validated = initProgram()
        if (validated)
            validated = initBuffer()
        if (Validated)
            Validated = initVertexArray()
        if (Validated)
            Validated = initTexture()
        if (Validated)
            Validated = initFramebuffer()

        return Validated && this->checkError("begin")
    }

    fun initProgram(): Boolean {

        var validated = true

        try {

            program = GlProgram.initFromPath("$SHADER_SOURCE.vert", "$SHADER_SOURCE.frag") {

                "Position".attrib = semantic.attr.POSITION
                "Texcoord".attrib = semantic.attr.TEXCOORD
            }
        } catch (exc: Exception) {
            validated = false
        }

        if (validated) {
            uniformMVP = program.getUniformLocation("MVP")
            uniformDiffuse = program.getUniformLocation("Diffuse")
        }

        return validated && checkError("initProgram")
    }

    fun initBuffer(): Boolean {
        bufferName.gen().bind {
            data(vertexData.data)
        }
        return checkError("initBuffer")
    }

    fun initTexture(): Boolean    {

        val texture = Texture2d (gli.loadDds(TEXTURE_DIFFUSE))
        gli::gl GL (gli::gl::PROFILE_GL32)

        glGenTextures(1, & TextureName)
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, TextureName)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, GLint(Texture.levels() - 1))
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

        gli::gl::format const Format = GL.translate(Texture.format(), Texture.swizzles())
        for (std:: size_t Level = 0; Level < Texture.levels(); ++Level)
        {
            glTexImage2D(GL_TEXTURE_2D, GLint(Level),
                    Format.Internal,
                    GLsizei(Texture[Level].extent().x), GLsizei(Texture[Level].extent().y),
                    0,
                    Format.External, Format.Type,
                    Texture[Level].data())
        }

        return this->checkError("initTexture")
    }

    bool validateRenderbuffer(GLuint Name, GLint ExpectedWidth, GLint ExpectedHeight, GLint ExpectedSamples, GLint ExpectedFormat)
    {
        GLint QueriedWidth = 0, QueriedHeight = 0, QueriedSamples = 0, QueriedFormat = 0
        glBindRenderbuffer(GL_RENDERBUFFER, Name)
        glGetRenderbufferParameteriv(GL_RENDERBUFFER, GL_RENDERBUFFER_WIDTH, & QueriedWidth)
        glGetRenderbufferParameteriv(GL_RENDERBUFFER, GL_RENDERBUFFER_HEIGHT, & QueriedHeight)
        glGetRenderbufferParameteriv(GL_RENDERBUFFER, GL_RENDERBUFFER_SAMPLES, & QueriedSamples)
        glGetRenderbufferParameteriv(GL_RENDERBUFFER, GL_RENDERBUFFER_INTERNAL_FORMAT, & QueriedFormat)

        if (QueriedWidth != ExpectedWidth || QueriedHeight != ExpectedHeight)
            return false
        if (QueriedSamples != ExpectedSamples)
            return false
        if (QueriedFormat != ExpectedFormat)
            return false

        return true
    }

    bool initFramebuffer()
    {
        glGenRenderbuffers(1, & ColorRenderbufferName)
        glBindRenderbuffer(GL_RENDERBUFFER, ColorRenderbufferName)
        glRenderbufferStorageMultisample(GL_RENDERBUFFER, 8, GL_RGBA8, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y)
        // The second parameter is the number of samples.

        if (!validateRenderbuffer(ColorRenderbufferName, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y, 8, GL_RGBA8))
            return false

        glGenFramebuffers(1, & FramebufferRenderName)
        glBindFramebuffer(GL_FRAMEBUFFER, FramebufferRenderName)
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER, ColorRenderbufferName)
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
            return false
        glBindFramebuffer(GL_FRAMEBUFFER, 0)

        glGenTextures(1, & ColorTextureName)
        glBindTexture(GL_TEXTURE_2D, ColorTextureName)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0)

        glGenFramebuffers(1, & FramebufferResolveName)
        glBindFramebuffer(GL_FRAMEBUFFER, FramebufferResolveName)
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, ColorTextureName, 0)
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
            return false
        glBindFramebuffer(GL_FRAMEBUFFER, 0)

        return this->checkError("initFramebuffer")
    }

    bool initVertexArray()
    {
        glGenVertexArrays(1, & VertexArrayName)
        glBindVertexArray(VertexArrayName)
        glBindBuffer(GL_ARRAY_BUFFER, BufferName)
        glVertexAttribPointer(semantic::attr::POSITION, 2, GL_FLOAT, GL_FALSE, sizeof(glf::vertex_v2fv2f), BUFFER_OFFSET(0))
        glVertexAttribPointer(semantic::attr::TEXCOORD, 2, GL_FLOAT, GL_FALSE, sizeof(glf::vertex_v2fv2f), BUFFER_OFFSET(sizeof(glm::vec2)))
        glBindBuffer(GL_ARRAY_BUFFER, 0)

        glEnableVertexAttribArray(semantic::attr::POSITION)
        glEnableVertexAttribArray(semantic::attr::TEXCOORD)
        glBindVertexArray(0)

        return this->checkError("initVertexArray")
    }

    void renderFBO(GLuint Framebuffer)
    {
        glBindFramebuffer(GL_FRAMEBUFFER, Framebuffer)
        glClearColor(0.0f, 0.5f, 1.0f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT)

        glm::mat4 Perspective = glm ::perspective(glm::pi<float>() * 0.25f, float(FRAMEBUFFER_SIZE.x) / FRAMEBUFFER_SIZE.y, 0.1f, 100.0f)
        glm::mat4 Model = glm ::mat4(1.0f)
        glm::mat4 MVP = Perspective * this->view() * Model
        glUniformMatrix4fv(UniformMVP, 1, GL_FALSE, & MVP [0][0])

        glViewport(0, 0, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y)

        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, TextureName)

        glBindVertexArray(VertexArrayName)
        glDrawArrays(GL_TRIANGLES, 0, vertexCount)

        this->checkError("renderFBO")
    }

    void renderFB(GLuint Texture2DName)
    {
        glm::vec2 WindowSize (this->getWindowSize())

        glm::mat4 Perspective = glm ::perspective(glm::pi<float>() * 0.25f, WindowSize.x / WindowSize.y, 0.1f, 100.0f)
        glm::mat4 Model = glm ::mat4(1.0f)
        glm::mat4 MVP = Perspective * this->view() * Model
        glUniformMatrix4fv(UniformMVP, 1, GL_FALSE, & MVP [0][0])

        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, Texture2DName)

        glBindVertexArray(VertexArrayName)
        glDrawArrays(GL_TRIANGLES, 0, vertexCount)

        this->checkError("renderFB")
    }



    bool end()
    {
        glDeleteBuffers(1, & BufferName)
        glDeleteProgram(program)
        glDeleteTextures(1, & TextureName)
        glDeleteTextures(1, & ColorTextureName)
        glDeleteRenderbuffers(1, & ColorRenderbufferName)
        glDeleteFramebuffers(1, & FramebufferRenderName)
        glDeleteFramebuffers(1, & FramebufferResolveName)
        glDeleteVertexArrays(1, & VertexArrayName)

        return true
    }

    bool render()
    {
        // Clear the framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
        glClearBufferfv(GL_COLOR, 0, & glm ::vec4(1.0f, 0.5f, 0.0f, 1.0f)[0])

        glUseProgram(program)
        glUniform1i(UniformDiffuse, 0)

        // Pass 1
        // Render the scene in a multisampled framebuffer
        glEnable(GL_MULTISAMPLE)
        renderFBO(FramebufferRenderName)
        glDisable(GL_MULTISAMPLE)

        // Resolved multisampling
        glBindFramebuffer(GL_READ_FRAMEBUFFER, FramebufferRenderName)
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, FramebufferResolveName)
        glBlitFramebuffer(
                0, 0, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y,
                0, 0, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y,
                GL_COLOR_BUFFER_BIT, GL_LINEAR)
        glBindFramebuffer(GL_FRAMEBUFFER, 0)

        glm::ivec2 WindowSize (this->getWindowSize())

        // Pass 2
        // Render the colorbuffer from the multisampled framebuffer
        glViewport(0, 0, WindowSize.x, WindowSize.y)
        renderFB(ColorTextureName)

        return true
    }
}