package oglSamples.tests.gl450

import glm_.BYTES
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.max
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import gln.cap.Caps
import gln.objects.GlProgram
import kool.IntBuffer
import kool.Ptr
import kool.shortBufferOf
import oglSamples.*
import oglSamples.framework.Framework
import oglSamples.framework.semantic
import oglSamples.tests.GlBufferEnum0
import org.lwjgl.opengl.GL11C.glGetInteger
import org.lwjgl.opengl.GL31C.GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT
import org.lwjgl.opengl.GL41C.GL_FRAGMENT_SHADER_BIT
import org.lwjgl.opengl.GL41C.GL_VERTEX_SHADER_BIT
import org.lwjgl.opengl.GL45C
import org.lwjgl.system.MemoryUtil.NULL
import kotlin.system.measureNanoTime

fun main() {

}

private class gl_450_directStateAccess : Framework("gl-450-direct-state-access", Caps.Profile.CORE, 4, 5, Vec2i(640, 480), Vec2(glm.πf * 0.2f)) {
    override fun end(): Boolean = true

    override fun render(): Boolean =true

    val SHADER_SOURCE = "gl-450/direct-state-access"
    val TEXTURE_DIFFUSE = "kueken7_rgba8_srgb.dds"
    val FRAMEBUFFER_SIZE = Vec2i(160)

    val vertexCount = 4
    val vertexSize = vertexCount * Vertex_v2v2.size
    val vertexData = vertex_v2v2_buffer_of(
            Vertex_v2v2(Vec2(-1f, -1f), Vec2(0f, 0f)),
            Vertex_v2v2(Vec2(+1f, -1f), Vec2(1f, 0f)),
            Vertex_v2v2(Vec2(+1f, +1f), Vec2(1f, 1f)),
            Vertex_v2v2(Vec2(-1f, +1f), Vec2(0f, 1f)))

    val elementCount = 6
    val elementSize = elementCount * Short.BYTES
    val elementData = shortBufferOf(
            0, 1, 2,
            2, 3, 0)

    enum class Program { VERTEX, FRAGMENT }

    enum class Framebuffer { RENDER, RESOLVE }

    enum class Buffer : GlBufferEnum0 { VERTEX, ELEMENT, TRANSFORM }

    enum class Texture { TEXTURE, MULTISAMPLE, COLORBUFFER }

    var vertexArray = GlVertexArray()
    var pipeline = GlPipeline()
    var program = GlProgram.NULL
    var sampler = GlSampler()
    var uniformBlockSize = 0
    var uniformPointer: Ptr = NULL
    val test = IntBuffer(3)

    override fun begin(): Boolean {

        var validated = true

        if (validated)
            validated = initProgram()
//        if (validated)
//            validated = initSampler()
        if (validated)
            validated = initBuffer()
//        if (validated)
//            validated = initVertexArray()
//        if (validated)
//            validated = initTexture()
//        if (validated)
//            validated = initFramebuffer()

        return validated
    }

    fun initProgram(): Boolean {

        var validated = true

        try {
            program = GlProgram.initFromPath("$SHADER_SOURCE.vert", "$SHADER_SOURCE.frag") {
                separable = true
                link()
            }
        } catch (exc: Exception) {
            validated = false
        }

        if (validated)
            pipeline = GlPipeline.gen().useStages(GL_VERTEX_SHADER_BIT or GL_FRAGMENT_SHADER_BIT, program)

        return validated
    }

    fun initBuffer(): Boolean {

        val uniformBufferOffset = glGetInteger(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT)
        uniformBlockSize = Mat4.size max uniformBufferOffset

        glCreateBuffers<Buffer>()
        val a = measureNanoTime {
            for (i in 0..999)
            Buffer.ELEMENT.storage(elementData)
        }
        val b = measureNanoTime {
            for (i in 0..999)
            GL45C.glNamedBufferStorage(test[0], elementData, 0)
        }
        println("$a, $b")
//        glNamedBufferStorage(BufferName[buffer::ELEMENT], elementSize, ElementData, 0)
//        glNamedBufferStorage(BufferName[buffer::VERTEX], vertexSize, VertexData, 0)
//        glNamedBufferStorage(BufferName[buffer::TRANSFORM], this->UniformBlockSize * 2, nullptr, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT)
//
//        this->UniformPointer = static_cast<glm::uint8*>(glMapNamedBufferRange(
//        BufferName[buffer::TRANSFORM], 0, this->UniformBlockSize * 2, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT | GL_MAP_INVALIDATE_BUFFER_BIT))

        return true
    }

//    bool initSampler ()
//    {
//        glCreateSamplers(1, & SamplerName)
//        glSamplerParameteri(SamplerName, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR)
//        glSamplerParameteri(SamplerName, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
//        glSamplerParameteri(SamplerName, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
//        glSamplerParameteri(SamplerName, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
//        glSamplerParameteri(SamplerName, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE)
//        glSamplerParameterfv(SamplerName, GL_TEXTURE_BORDER_COLOR, & glm ::vec4(0.0f)[0])
//        glSamplerParameterf(SamplerName, GL_TEXTURE_MIN_LOD, -1000.f)
//        glSamplerParameterf(SamplerName, GL_TEXTURE_MAX_LOD, 1000.f)
//        glSamplerParameterf(SamplerName, GL_TEXTURE_LOD_BIAS, 0.0f)
//        glSamplerParameteri(SamplerName, GL_TEXTURE_COMPARE_MODE, GL_NONE)
//        glSamplerParameteri(SamplerName, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL)
//
//        return true
//    }
//
//    bool initTexture ()
//    {
//        gli::texture2d Texture (gli::load(getDataDirectory() + TEXTURE_DIFFUSE))
//        if (Texture.empty())
//            return 0
//
//        gli::gl GL (gli::gl::PROFILE_GL33)
//        gli::gl::format const Format = GL.translate(Texture.format(), Texture.swizzles())
//        GLenum const Target = GL.translate(Texture.target())
//        glm::tvec2<GLsizei> const Dimensions(Texture.extent())
//
//        glCreateTextures(GL_TEXTURE_2D, 1, & TextureName [texture::TEXTURE])
//        glTextureParameteri(TextureName[texture::TEXTURE], GL_TEXTURE_BASE_LEVEL, 0)
//        glTextureParameteri(TextureName[texture::TEXTURE], GL_TEXTURE_MAX_LEVEL, static_cast<GLint>(Texture.levels() - 1))
//        glTextureParameteri(TextureName[texture::TEXTURE], GL_TEXTURE_SWIZZLE_R, Format.Swizzles[0])
//        glTextureParameteri(TextureName[texture::TEXTURE], GL_TEXTURE_SWIZZLE_G, Format.Swizzles[1])
//        glTextureParameteri(TextureName[texture::TEXTURE], GL_TEXTURE_SWIZZLE_B, Format.Swizzles[2])
//        glTextureParameteri(TextureName[texture::TEXTURE], GL_TEXTURE_SWIZZLE_A, Format.Swizzles[3])
//        glTextureStorage2D(TextureName[texture::TEXTURE],
//                static_cast<GLint>(Texture.levels()), Format.Internal,
//                Dimensions.x, Texture.target() == gli::TARGET_2D ? Dimensions . y : static_cast < GLsizei >(Texture.layers() * Texture.faces()))
//
//        for (gli:: texture2d::size_type Level = 0; Level < Texture.levels(); ++Level)
//        {
//            glTextureSubImage2D(TextureName[texture::TEXTURE], static_cast<GLint>(Level),
//                    0, 0,
//                    static_cast<GLsizei>(Texture[Level].extent().x), static_cast<GLsizei>(Texture[Level].extent().y),
//                    Format.External, Format.Type,
//                    Texture[Level].data())
//        }
//
//
//        glCreateTextures(GL_TEXTURE_2D_MULTISAMPLE, 1, & TextureName [texture::MULTISAMPLE])
//        glTextureParameteri(TextureName[texture::MULTISAMPLE], GL_TEXTURE_BASE_LEVEL, 0)
//        glTextureParameteri(TextureName[texture::MULTISAMPLE], GL_TEXTURE_MAX_LEVEL, 0)
//        glTextureStorage2DMultisample(TextureName[texture::MULTISAMPLE], 4, GL_RGBA8, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y, GL_FALSE)
//
//        glCreateTextures(GL_TEXTURE_2D, 1, & TextureName [texture::COLORBUFFER])
//        glTextureParameteri(TextureName[texture::COLORBUFFER], GL_TEXTURE_BASE_LEVEL, 0)
//        glTextureParameteri(TextureName[texture::COLORBUFFER], GL_TEXTURE_MAX_LEVEL, 0)
//        glTextureStorage2D(TextureName[texture::COLORBUFFER], 1, GL_RGBA8, GLsizei(FRAMEBUFFER_SIZE.x), GLsizei(FRAMEBUFFER_SIZE.y))
//
//        return true
//    }
//
//    bool initFramebuffer ()
//    {
//        glCreateFramebuffers(framebuffer::MAX, & FramebufferName [0])
//        glNamedFramebufferTexture(FramebufferName[framebuffer::RENDER], GL_COLOR_ATTACHMENT0, TextureName[texture::MULTISAMPLE], 0)
//        glNamedFramebufferTexture(FramebufferName[framebuffer::RESOLVE], GL_COLOR_ATTACHMENT0, TextureName[texture::COLORBUFFER], 0)
//
//        if (glCheckNamedFramebufferStatus(FramebufferName[framebuffer::RENDER], GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
//            return false
//        if (glCheckNamedFramebufferStatus(FramebufferName[framebuffer::RESOLVE], GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
//            return false
//
//        GLint Samples = 0
//        glGetNamedFramebufferParameteriv(FramebufferName[framebuffer::RENDER], GL_SAMPLES, & Samples)
//        if (Samples != 4)
//            return false
//
//        return true
//    }
//
//    bool initVertexArray ()
//    {
//        glCreateVertexArrays(1, & VertexArrayName)
//
//        glVertexArrayAttribBinding(VertexArrayName, semantic::attr::POSITION, 0)
//        glVertexArrayAttribFormat(VertexArrayName, semantic::attr::POSITION, 2, GL_FLOAT, GL_FALSE, 0)
//        glEnableVertexArrayAttrib(VertexArrayName, semantic::attr::POSITION)
//
//        glVertexArrayAttribBinding(VertexArrayName, semantic::attr::TEXCOORD, 0)
//        glVertexArrayAttribFormat(VertexArrayName, semantic::attr::TEXCOORD, 2, GL_FLOAT, GL_FALSE, sizeof(glm::vec2))
//        glEnableVertexArrayAttrib(VertexArrayName, semantic::attr::TEXCOORD)
//
//        glVertexArrayElementBuffer(VertexArrayName, BufferName[buffer::ELEMENT])
//        glVertexArrayVertexBuffer(VertexArrayName, 0, BufferName[buffer::VERTEX], 0, sizeof(glf::vertex_v2fv2f))
//
//        return true
//    }
//
//
//
//    bool end ()
//    {
//        glUnmapNamedBuffer(BufferName[buffer::TRANSFORM])
//
//        glDeleteProgramPipelines(1, & PipelineName)
//        glDeleteBuffers(buffer::MAX, & BufferName [0])
//        glDeleteProgram(ProgramName)
//        glDeleteTextures(texture::MAX, & TextureName [0])
//        glDeleteFramebuffers(framebuffer::MAX, & FramebufferName [0])
//        glDeleteVertexArrays(1, & VertexArrayName)
//        glDeleteSamplers(1, & SamplerName)
//
//        return true
//    }
//
//    void renderFBO ()
//    {
//        glEnable(GL_MULTISAMPLE)
//
//        glClipControl(GL_LOWER_LEFT, GL_NEGATIVE_ONE_TO_ONE)
//        glViewportIndexedf(0, 0, 0, static_cast<float>(FRAMEBUFFER_SIZE.x), static_cast<float>(FRAMEBUFFER_SIZE.y))
//        glClearNamedFramebufferfv(FramebufferName[framebuffer::RENDER], GL_COLOR, 0, & glm ::vec4(0.0f, 0.5f, 1.0f, 1.0f)[0])
//
//        glBindFramebuffer(GL_FRAMEBUFFER, FramebufferName[framebuffer::RENDER])
//        glBindBufferRange(GL_UNIFORM_BUFFER, semantic::uniform::TRANSFORM0, BufferName[buffer::TRANSFORM], 0, this->UniformBlockSize)
//        glBindSamplers(0, 1, & SamplerName)
//        glBindTextureUnit(0, TextureName[texture::TEXTURE])
//        glBindVertexArray(VertexArrayName)
//
//        glDrawElementsInstancedBaseVertexBaseInstance(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, nullptr, 1, 0, 0)
//
//        glDisable(GL_MULTISAMPLE)
//    }
//
//    void renderFB ()
//    {
//        glm::vec2 WindowSize (this->getWindowSize())
//
//        glClipControl(GL_LOWER_LEFT, GL_NEGATIVE_ONE_TO_ONE)
//        glViewportIndexedf(0, 0, 0, WindowSize.x, WindowSize.y)
//        glClearNamedFramebufferfv(0, GL_COLOR, 0, & glm ::vec4(0.0f, 0.5f, 1.0f, 1.0f)[0])
//
//        glBindFramebuffer(GL_FRAMEBUFFER, 0)
//        glBindBufferRange(GL_UNIFORM_BUFFER, semantic::uniform::TRANSFORM0, BufferName[buffer::TRANSFORM], this->UniformBlockSize, this->UniformBlockSize)
//        glBindSamplers(0, 1, & SamplerName)
//        glBindTextureUnit(0, TextureName[texture::COLORBUFFER])
//        glBindVertexArray(VertexArrayName)
//
//        glDrawElementsInstancedBaseVertexBaseInstance(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, nullptr, 1, 0, 0)
//    }
//
//    bool render ()
//    {
//        glm::vec2 WindowSize (this->getWindowSize());
//
//        {
//            glm::mat4 ProjectionA = glm ::scale(glm::perspective(glm::pi<float>() * 0.25f, float(FRAMEBUFFER_SIZE.x) / FRAMEBUFFER_SIZE.y, 0.1f, 100.0f), glm::vec3(1, -1, 1))
//            *reinterpret_cast < glm::mat4 * >(this->UniformPointer+0) = ProjectionA * this->view() * glm::mat4(1)
//
//            glm::mat4 ProjectionB = glm ::perspective(glm::pi<float>() * 0.25f, WindowSize.x / WindowSize.y, 0.1f, 100.0f)
//            *reinterpret_cast < glm::mat4 * >(this->UniformPointer+this->UniformBlockSize) = ProjectionB * this->view() * glm::scale(glm::mat4(1), glm::vec3(2))
//        }
//
//        // Step 1, render the scene in a multisampled framebuffer
//        glBindProgramPipeline(PipelineName)
//
//        renderFBO()
//
//        // Step 2: blit
//        glBlitNamedFramebuffer(FramebufferName[framebuffer::RENDER], FramebufferName[framebuffer::RESOLVE],
//                0, 0, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y,
//                0, 0, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y,
//                GL_COLOR_BUFFER_BIT, GL_NEAREST)
//
//        GLenum MaxColorAttachment = GL_COLOR_ATTACHMENT0
//                glInvalidateNamedFramebufferData(FramebufferName[framebuffer::RENDER], 1, & MaxColorAttachment)
//
//        // Step 3, render the colorbuffer from the multisampled framebuffer
//        renderFB()
//
//        return true
//    }
}