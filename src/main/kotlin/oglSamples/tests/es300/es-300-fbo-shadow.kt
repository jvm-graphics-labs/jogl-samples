package oglSamples.tests.es300

import glm_.glm
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec4.Vec4ub
import gln.BufferTarget
import gln.BufferTarget.Companion.ARRAY
import gln.BufferTarget.Companion.ELEMENT_ARRAY
import gln.TextureTarget.Companion._2D
import gln.UniformLocation
import gln.cap.Caps.Profile.ES
import gln.gl
import gln.glf.glf
import gln.identifiers.GlBuffers
import gln.identifiers.GlProgram
import kool.ByteBuffer
import kool.shortBufferOf
import oglSamples.GlFramebuffers
import oglSamples.GlTextures
import oglSamples.GlVertexArrays
import oglSamples.framework.Framework
import oglSamples.framework.semantic
import oglSamples.plusAssign
import oglSamples.get
import oglSamples.set
import oglSamples.tests.es300.es_300_fbo_shadow.Buffer.ELEMENT
import oglSamples.tests.es300.es_300_fbo_shadow.Buffer.VERTEX
import oglSamples.tests.es300.es_300_fbo_shadow.Texture.COLORBUFFER
import java.lang.Exception

fun main() {

}

private class es_300_fbo_shadow : Framework("es-300-fbo-shadow", ES, 3, 0, Vec2(0f, -glm.πf * 0.3f)) {

    val SHADER_SOURCE_DEPTH = "es-300/fbo-shadow-depth"
    val SHADER_SOURCE_RENDER = "es-300/fbo-shadow-render"

    val vertexCount = 8
    val vertexSize = vertexCount * glf.pos3_col4ub.stride
    val vertexData = ByteBuffer(vertexSize).also {
        it += Vec3(-1.0f, -1.0f, 0.0f); it += Vec4ub(255, 127, 0, 255)
        it += Vec3(+1.0f, -1.0f, 0.0f); Vec4ub(255, 127, 0, 255)
        it += Vec3(+1.0f, +1.0f, 0.0f); Vec4ub(255, 127, 0, 255)
        it += Vec3(-1.0f, +1.0f, 0.0f); Vec4ub(255, 127, 0, 255)
        it += Vec3(-0.1f, -0.1f, 0.2f); Vec4ub(0, 127, 255, 255)
        it += Vec3(+0.1f, -0.1f, 0.2f); Vec4ub(0, 127, 255, 255)
        it += Vec3(+0.1f, +0.1f, 0.2f); Vec4ub(0, 127, 255, 255)
        it += Vec3(-0.1f, +0.1f, 0.2f); Vec4ub(0, 127, 255, 255)
    }

    val elements = shortBufferOf(
            0, 1, 2,
            2, 3, 0,
            4, 5, 6,
            6, 7, 4)

    enum class Buffer { VERTEX, ELEMENT, TRANSFORM }

    enum class Texture { COLORBUFFER, DEPTHBUFFER, SHADOWMAP }

    enum class Program { DEPTH, RENDER }

    enum class Framebuffer { FRAMEBUFFER, SHADOW }

    val shadowSize = Vec2i(64)

    val framebuffers = GlFramebuffers<Framebuffer>()
    val programs = Array(Program.values().size) { GlProgram.NULL }
    val vertexArrays = GlVertexArrays<Program>()
    val buffers = GlBuffers<Buffer>()
    val textures = GlTextures<Texture>()
    var uniformRenderMVP: UniformLocation = 0
    var uniformRenderDepthBiasMVP: UniformLocation = 0
    var uniformRenderShadow: UniformLocation = 0
    var uniformDepthMVP: UniformLocation = 0

    override fun begin(): Boolean {

        var validated = initProgram()
        if (validated)
            validated = initBuffer()
        if (validated)
            validated = initVertexArray()
        if (validated)
            validated = initTexture()
        if (validated)
            validated = initFramebuffer()

        return validated && checkError("begin")
    }

    fun initProgram(): Boolean {

        var validated = try {
            programs[Program.RENDER] = GlProgram.initFromPath(SHADER_SOURCE_RENDER) {
                "Position".attrib = semantic.attr.POSITION
                "Color".attrib = semantic.attr.COLOR
            }
            true
        } catch (_: Exception) {
            false
        }

        if (validated)
            programs[Program.RENDER]{
                uniformRenderShadow = "Shadow".uniform
                uniformRenderMVP = "MVP".uniform
                uniformRenderDepthBiasMVP = "DepthBiasMVP".uniform
            }

        if (validated)
            try {
                programs[Program.DEPTH] = GlProgram.initFromPath(SHADER_SOURCE_DEPTH) {
                    "Position".attrib = semantic.attr.POSITION
                }
            } catch (_: Exception) {
                validated = false
            }

        if (validated)
            uniformDepthMVP = programs[Program.DEPTH] getUniformLocation "MVP"

        return validated
    }

    fun initBuffer(): Boolean {

        buffers.gen {
            ELEMENT.bound(ELEMENT_ARRAY) { data(elements) }
            VERTEX.bound(ARRAY) { data(vertexData) }
        }

        return true
    }

    fun initTexture(): Boolean {

        textures.gen {

            gl.activeTexture = 0
            COLORBUFFER.bind(_2D) {
                levels = 0..0
                storage2D(GL_TEXTURE_2D, GLint(1), GL_RGBA8, GLsizei(WindowSize.x), GLsizei(WindowSize.y))
            }
            glActiveTexture(GL_TEXTURE0)
            glBindTexture(GL_TEXTURE_2D, TextureName[texture::DEPTHBUFFER])
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0)
            glTexStorage2D(GL_TEXTURE_2D, GLint(1), GL_DEPTH_COMPONENT24, GLsizei(WindowSize.x), GLsizei(WindowSize.y))

            glActiveTexture(GL_TEXTURE0)
            glBindTexture(GL_TEXTURE_2D, TextureName[texture::SHADOWMAP])
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_R_TO_TEXTURE)
            glTexStorage2D(GL_TEXTURE_2D, GLint(1), GL_DEPTH_COMPONENT24, GLsizei(ShadowSize.x), GLsizei(ShadowSize.y))
        }
        return true
    }

    bool initVertexArray()
    {
        glGenVertexArrays(program::MAX, & VertexArrayName [0])
        glBindVertexArray(VertexArrayName[program::RENDER])
        glBindBuffer(GL_ARRAY_BUFFER, BufferName[buffer::VERTEX])
        glVertexAttribPointer(semantic::attr::POSITION, 3, GL_FLOAT, GL_FALSE, sizeof(glf::vertex_v3fv4u8), BUFFER_OFFSET(0))
        glVertexAttribPointer(semantic::attr::COLOR, 4, GL_UNSIGNED_BYTE, GL_TRUE, sizeof(glf::vertex_v3fv4u8), BUFFER_OFFSET(sizeof(glm::vec3)))
        glBindBuffer(GL_ARRAY_BUFFER, 0)

        glEnableVertexAttribArray(semantic::attr::POSITION)
        glEnableVertexAttribArray(semantic::attr::COLOR)

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, BufferName[buffer::ELEMENT])
        glBindVertexArray(0)

        return true
    }

    bool initFramebuffer()
    {
        glGenFramebuffers(framebuffer::MAX, & FramebufferName [0])

        GLenum const BuffersRender = GL_COLOR_ATTACHMENT0
        glBindFramebuffer(GL_FRAMEBUFFER, FramebufferName[framebuffer::FRAMEBUFFER])
        glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, TextureName[texture::COLORBUFFER], 0)
        glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, TextureName[texture::DEPTHBUFFER], 0)
        glDrawBuffers(1, & BuffersRender)
        if (!this->checkFramebuffer(FramebufferName[framebuffer::FRAMEBUFFER]))
        return false

        glBindFramebuffer(GL_FRAMEBUFFER, FramebufferName[framebuffer::SHADOW])
        glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, TextureName[texture::SHADOWMAP], 0)
        if (!this->checkFramebuffer(FramebufferName[framebuffer::SHADOW]))
        return false

        glBindFramebuffer(GL_FRAMEBUFFER, 0)
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
            return false

        return true
    }

    bool end()
    {
        for (std:: size_t i = 0; i < program::MAX; ++i)
        glDeleteProgram(ProgramName[i])

        glDeleteFramebuffers(framebuffer::MAX, & FramebufferName [0])
        glDeleteBuffers(buffer::MAX, & BufferName [0])
        glDeleteTextures(texture::MAX, & TextureName [0])
        glDeleteVertexArrays(program::MAX, & VertexArrayName [0])

        return this->checkError("end")
    }

    void renderShadow()
    {
        glEnable(GL_DEPTH_TEST)
        glDepthFunc(GL_LESS)

        glViewport(0, 0, ShadowSize.x, ShadowSize.y)

        glBindFramebuffer(GL_FRAMEBUFFER, FramebufferName[framebuffer::SHADOW])
        float Depth (1.0f)
        glClearBufferfv(GL_DEPTH, 0, & Depth)

        glBindVertexArray(VertexArrayName[program::RENDER])
        glDrawElements(GL_TRIANGLES, ElementCount, GL_UNSIGNED_SHORT, nullptr)

        glDisable(GL_DEPTH_TEST)

        this->checkError("renderShadow")
    }

    void renderFramebuffer()
    {
        glEnable(GL_DEPTH_TEST)
        glDepthFunc(GL_LESS)

        glm::ivec2 WindowSize (this->getWindowSize())
        glViewport(0, 0, WindowSize.x, WindowSize.y)

        glBindFramebuffer(GL_FRAMEBUFFER, 0)
        float Depth (1.0f)
        glClearBufferfv(GL_DEPTH, 0, & Depth)
        glClearBufferfv(GL_COLOR, 0, & glm ::vec4(0.0f, 0.0f, 0.0f, 1.0f)[0])

        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, TextureName[texture::SHADOWMAP])

        glBindVertexArray(VertexArrayName[program::RENDER])
        glDrawElements(GL_TRIANGLES, ElementCount, GL_UNSIGNED_SHORT, nullptr)

        glDisable(GL_DEPTH_TEST)

        this->checkError("renderFramebuffer")
    }

    bool render()
    {
        glm::ivec2 WindowSize (this->getWindowSize())

        glm::mat4 Model = glm ::mat4(1.0f)

        glm::mat4 DepthProjection = glm ::ortho(-1.0f, 1.0f, -1.0f, 1.0f, -4.0f, 8.0f)
        glm::mat4 DepthView = glm ::lookAt(glm::vec3(0.5, 1.0, 2.0), glm::vec3(0), glm::vec3(0, 0, 1))
        glm::mat4 DepthMVP = DepthProjection * DepthView * Model

                glm::mat4 BiasMatrix (
                0.5, 0.0, 0.0, 0.0,
        0.0, 0.5, 0.0, 0.0,
        0.0, 0.0, 0.5, 0.0,
        0.5, 0.5, 0.5, 1.0)

        glm::mat4 DepthMVPBias = BiasMatrix * DepthMVP

                glm::mat4 RenderProjection = glm ::perspective(glm::pi<float>() * 0.25f, 4.0f / 3.0f, 0.1f, 10.0f)
        glm::mat4 RenderMVP = RenderProjection * this->view() * Model

        glUseProgram(ProgramName[program::DEPTH])
        glUniformMatrix4fv(this->UniformDepthMVP, 1, GL_FALSE, &DepthMVP[0][0])

        renderShadow()

        glUseProgram(ProgramName[program::RENDER])
        glUniform1i(this->UniformRenderShadow, 0)
        glUniformMatrix4fv(this->UniformRenderMVP, 1, GL_FALSE, &RenderMVP[0][0])
        glUniformMatrix4fv(this->UniformRenderDepthBiasMVP, 1, GL_FALSE, &DepthMVPBias[0][0])

        renderFramebuffer()

        return this->checkError("render")
    }
}