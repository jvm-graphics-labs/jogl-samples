//package oglSamples.tests.gl450
//
//import gli_.Texture2d
//import gli_.gl
//import gli_.gli
//import glm_.BYTES
//import glm_.glm
//import glm_.mat4x4.Mat4
//import glm_.max
//import glm_.vec2.Vec2
//import glm_.vec2.Vec2i
//import glm_.vec4.Vec4
//import gln.CompareFunction.Companion.LEQUAL
//import gln.GlBufferEnum
//import gln.MagFilter.Companion.LINEAR
//import gln.MinFilter.Companion.LINEAR_MIPMAP_LINEAR
//import gln.TextureCompareMode.Companion.NONE
//import gln.cap.Caps
//import gln.objects.GlBuffers
//import gln.objects.GlProgram
//import gln.program.GlPipeline
//import gln.sampler.GlSampler
//import gln.texture.GlTextureDsl
//import gln.texture.TexWrap.Companion.CLAMP_TO_EDGE
//import gln.vertexArray.GlVertexArray
//import kool.shortBufferOf
//import oglSamples.*
//import oglSamples.framework.Framework
//import org.lwjgl.opengl.GL11C.glGetInteger
//import org.lwjgl.opengl.GL30C.GL_MAP_INVALIDATE_BUFFER_BIT
//import org.lwjgl.opengl.GL30C.GL_MAP_WRITE_BIT
//import org.lwjgl.opengl.GL31C.GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT
//import org.lwjgl.opengl.GL41C.GL_FRAGMENT_SHADER_BIT
//import org.lwjgl.opengl.GL41C.GL_VERTEX_SHADER_BIT
//import org.lwjgl.opengl.GL44C.GL_MAP_COHERENT_BIT
//import org.lwjgl.opengl.GL44C.GL_MAP_PERSISTENT_BIT
//import java.nio.ByteBuffer
//
//fun main() {
//    gl_450_directStateAccess()()
//}
//
//private class gl_450_directStateAccess : Framework("gl-450-direct-state-access", Caps.Profile.CORE, 4, 5, Vec2i(640, 480), Vec2(glm.πf * 0.2f)) {
//    override fun end(): Boolean = true
//
//    override fun render(): Boolean = true
//
//    val SHADER_SOURCE = "gl-450/direct-state-access"
//    val TEXTURE_DIFFUSE = "kueken7_rgba8_srgb.dds"
//    val FRAMEBUFFER_SIZE = Vec2i(160)
//
//    val vertexCount = 4
//    val vertexSize = vertexCount * Vertex_v2v2.size
//    val vertexData = vertex_v2v2_buffer_of(
//            Vertex_v2v2(Vec2(-1f, -1f), Vec2(0f, 0f)),
//            Vertex_v2v2(Vec2(+1f, -1f), Vec2(1f, 0f)),
//            Vertex_v2v2(Vec2(+1f, +1f), Vec2(1f, 1f)),
//            Vertex_v2v2(Vec2(-1f, +1f), Vec2(0f, 1f)))
//
//    val elementCount = 6
//    val elementSize = elementCount * Short.BYTES
//    val elementData = shortBufferOf(
//            0, 1, 2,
//            2, 3, 0)
//
//    enum class Program { VERTEX, FRAGMENT }
//
//    enum class Framebuffer { RENDER, RESOLVE }
//
//    enum class Buffer : GlBufferEnum { VERTEX, ELEMENT, TRANSFORM }
//
//    enum class Texture { TEXTURE, MULTISAMPLE, COLORBUFFER }
//
//    var vertexArray = GlVertexArray()
//    val bufferName = GlBuffers<Buffer>()
//    val textureName = GlTextures<Texture>()
//    var pipeline = GlPipeline()
//    var program = GlProgram.NULL
//    var sampler = GlSampler()
//    var uniformBlockSize = 0
//    var uniformPointer: ByteBuffer? = null
//
//    override fun begin(): Boolean {
//
//        DSA = true
//
//        var validated = true
//
//        if (validated)
//            validated = initProgram()
////        if (validated)
////            validated = initSampler()
//        if (validated)
//            validated = initBuffer()
////        if (validated)
////            validated = initVertexArray()
////        if (validated)
////            validated = initTexture()
////        if (validated)
////            validated = initFramebuffer()
//
//        return validated
//    }
//
//    fun initProgram(): Boolean {
//
//        var validated = true
//
//        try {
//            program = GlProgram.initFromPath("$SHADER_SOURCE.vert", "$SHADER_SOURCE.frag") {
//                separable = true
//                link()
//            }
//        } catch (exc: Exception) {
//            validated = false
//        }
//
//        if (validated)
//            pipeline = GlPipeline.gen().useStages(GL_VERTEX_SHADER_BIT or GL_FRAGMENT_SHADER_BIT, program)
//
//        return validated
//    }
//
//    fun initBuffer(): Boolean {
//
//        val uniformBufferOffset = glGetInteger(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT)
//        uniformBlockSize = Mat4.size max uniformBufferOffset
//
////        for (i in 0..9999) {
////            GL45C.glNamedBufferStorage(test[1], elementData, 0)
////            GL15C.glDeleteBuffers(test[1])
////            GL15C.nglGenBuffers(1, test.adr + Int.BYTES)
////        }
////
////        GL45C.glCreateBuffers(bufferName)
////        val a = measureNanoTime {
////            for (i in 0..999) {
////                bufferName[Buffer.ELEMENT].storage(elementData)
////                bufferName[Buffer.ELEMENT].delete()
////                bufferName.gen(Buffer.ELEMENT)
////            }
////        }
////        val b = measureNanoTime {
////            for (i in 0..999) {
////                GL45C.glNamedBufferStorage(test[1], elementData, 0)
////                GL15C.glDeleteBuffers(test[1])
////                GL15C.nglGenBuffers(1, test.adr + Int.BYTES)
////            }
////        }
////        val c = measureNanoTime {
////            for (i in 0..999) {
////                What.Eh.gen()
////                What.Eh.storage(elementData, 0)
////                What.Eh.delete()
////            }
////        }
////        println("$a, $b, $c")
//
//        bufferName.create {
//
//            Buffer.ELEMENT.storage(elementData)
//            Buffer.VERTEX.storage(vertexData.data)
//            Buffer.TRANSFORM.storage(uniformBlockSize * 2, GL_MAP_WRITE_BIT or GL_MAP_PERSISTENT_BIT or GL_MAP_COHERENT_BIT)
//
//            uniformPointer = Buffer.TRANSFORM.mapRange(uniformBlockSize * 2, GL_MAP_WRITE_BIT or GL_MAP_PERSISTENT_BIT or GL_MAP_COHERENT_BIT or GL_MAP_INVALIDATE_BUFFER_BIT)
//        }
//
//        return true
//    }
//
//    fun initSampler(): Boolean {
//
//        sampler = GlSampler.create {
//            minFiler = LINEAR_MIPMAP_LINEAR
//            magFiler = LINEAR
//            setWrapSTR(CLAMP_TO_EDGE)
//            borderColor = Vec4(0f)
//            minLod = -1000f
//            maxLod = 1000f
//            lodBias = 0f
//            compareMode = NONE
//            compareFunc = LEQUAL
//        }
//
//        return true
//    }
//
//    fun initTexture(): Boolean {
//
//        val texture = Texture2d(gli.load(TEXTURE_DIFFUSE))
//        if (texture.empty())
//            return false
//
//        gl.profile = gl.Profile.GL33
//        val (target, format) = gl.translate(texture)
//        val dimensions = Vec2i(texture.extent())
//
//        textureName.create {
//
//            Texture.TEXTURE {
//
//                levels = 0 until texture.levels()
//                swizzles = format.swizzles
//                GlTextureDsl.storage()
//                glTextureStorage2D(TextureName[texture::TEXTURE],
//                        static_cast<GLint>(texture.levels()), Format.Internal,
//                        Dimensions.x, texture.target() == gli::TARGET_2D ? Dimensions . y : static_cast < GLsizei >(texture.layers() * texture.faces()))
//
//                for (gli:: texture2d::size_type Level = 0; Level < texture.levels(); ++Level)
//                {
//                    glTextureSubImage2D(TextureName[texture::TEXTURE], static_cast<GLint>(Level),
//                            0, 0,
//                            static_cast<GLsizei>(texture[Level].extent().x), static_cast<GLsizei>(texture[Level].extent().y),
//                            Format.External, Format.Type,
//                            texture[Level].data())
//                }
//            }
//
//            glCreateTextures(GL_TEXTURE_2D_MULTISAMPLE, 1, & TextureName [texture::MULTISAMPLE])
//            glTextureParameteri(TextureName[texture::MULTISAMPLE], GL_TEXTURE_BASE_LEVEL, 0)
//            glTextureParameteri(TextureName[texture::MULTISAMPLE], GL_TEXTURE_MAX_LEVEL, 0)
//            glTextureStorage2DMultisample(TextureName[texture::MULTISAMPLE], 4, GL_RGBA8, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y, GL_FALSE)
//
//            glCreateTextures(GL_TEXTURE_2D, 1, & TextureName [texture::COLORBUFFER])
//            glTextureParameteri(TextureName[texture::COLORBUFFER], GL_TEXTURE_BASE_LEVEL, 0)
//            glTextureParameteri(TextureName[texture::COLORBUFFER], GL_TEXTURE_MAX_LEVEL, 0)
//            glTextureStorage2D(TextureName[texture::COLORBUFFER], 1, GL_RGBA8, GLsizei(FRAMEBUFFER_SIZE.x), GLsizei(FRAMEBUFFER_SIZE.y))
//        }
//        return true
//    }
//
////    bool initFramebuffer ()
////    {
////        glCreateFramebuffers(framebuffer::MAX, & FramebufferName [0])
////        glNamedFramebufferTexture(FramebufferName[framebuffer::RENDER], GL_COLOR_ATTACHMENT0, TextureName[texture::MULTISAMPLE], 0)
////        glNamedFramebufferTexture(FramebufferName[framebuffer::RESOLVE], GL_COLOR_ATTACHMENT0, TextureName[texture::COLORBUFFER], 0)
////
////        if (glCheckNamedFramebufferStatus(FramebufferName[framebuffer::RENDER], GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
////            return false
////        if (glCheckNamedFramebufferStatus(FramebufferName[framebuffer::RESOLVE], GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
////            return false
////
////        GLint Samples = 0
////        glGetNamedFramebufferParameteriv(FramebufferName[framebuffer::RENDER], GL_SAMPLES, & Samples)
////        if (Samples != 4)
////            return false
////
////        return true
////    }
////
////    bool initVertexArray ()
////    {
////        glCreateVertexArrays(1, & VertexArrayName)
////
////        glVertexArrayAttribBinding(VertexArrayName, semantic::attr::POSITION, 0)
////        glVertexArrayAttribFormat(VertexArrayName, semantic::attr::POSITION, 2, GL_FLOAT, GL_FALSE, 0)
////        glEnableVertexArrayAttrib(VertexArrayName, semantic::attr::POSITION)
////
////        glVertexArrayAttribBinding(VertexArrayName, semantic::attr::TEXCOORD, 0)
////        glVertexArrayAttribFormat(VertexArrayName, semantic::attr::TEXCOORD, 2, GL_FLOAT, GL_FALSE, sizeof(glm::vec2))
////        glEnableVertexArrayAttrib(VertexArrayName, semantic::attr::TEXCOORD)
////
////        glVertexArrayElementBuffer(VertexArrayName, BufferName[buffer::ELEMENT])
////        glVertexArrayVertexBuffer(VertexArrayName, 0, BufferName[buffer::VERTEX], 0, sizeof(glf::vertex_v2fv2f))
////
////        return true
////    }
////
////
////
////    bool end ()
////    {
////        glUnmapNamedBuffer(BufferName[buffer::TRANSFORM])
////
////        glDeleteProgramPipelines(1, & PipelineName)
////        glDeleteBuffers(buffer::MAX, & BufferName [0])
////        glDeleteProgram(ProgramName)
////        glDeleteTextures(texture::MAX, & TextureName [0])
////        glDeleteFramebuffers(framebuffer::MAX, & FramebufferName [0])
////        glDeleteVertexArrays(1, & VertexArrayName)
////        glDeleteSamplers(1, & SamplerName)
////
////        return true
////    }
////
////    void renderFBO ()
////    {
////        glEnable(GL_MULTISAMPLE)
////
////        glClipControl(GL_LOWER_LEFT, GL_NEGATIVE_ONE_TO_ONE)
////        glViewportIndexedf(0, 0, 0, static_cast<float>(FRAMEBUFFER_SIZE.x), static_cast<float>(FRAMEBUFFER_SIZE.y))
////        glClearNamedFramebufferfv(FramebufferName[framebuffer::RENDER], GL_COLOR, 0, & glm ::vec4(0.0f, 0.5f, 1.0f, 1.0f)[0])
////
////        glBindFramebuffer(GL_FRAMEBUFFER, FramebufferName[framebuffer::RENDER])
////        glBindBufferRange(GL_UNIFORM_BUFFER, semantic::uniform::TRANSFORM0, BufferName[buffer::TRANSFORM], 0, this->UniformBlockSize)
////        glBindSamplers(0, 1, & SamplerName)
////        glBindTextureUnit(0, TextureName[texture::TEXTURE])
////        glBindVertexArray(VertexArrayName)
////
////        glDrawElementsInstancedBaseVertexBaseInstance(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, nullptr, 1, 0, 0)
////
////        glDisable(GL_MULTISAMPLE)
////    }
////
////    void renderFB ()
////    {
////        glm::vec2 WindowSize (this->getWindowSize())
////
////        glClipControl(GL_LOWER_LEFT, GL_NEGATIVE_ONE_TO_ONE)
////        glViewportIndexedf(0, 0, 0, WindowSize.x, WindowSize.y)
////        glClearNamedFramebufferfv(0, GL_COLOR, 0, & glm ::vec4(0.0f, 0.5f, 1.0f, 1.0f)[0])
////
////        glBindFramebuffer(GL_FRAMEBUFFER, 0)
////        glBindBufferRange(GL_UNIFORM_BUFFER, semantic::uniform::TRANSFORM0, BufferName[buffer::TRANSFORM], this->UniformBlockSize, this->UniformBlockSize)
////        glBindSamplers(0, 1, & SamplerName)
////        glBindTextureUnit(0, TextureName[texture::COLORBUFFER])
////        glBindVertexArray(VertexArrayName)
////
////        glDrawElementsInstancedBaseVertexBaseInstance(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, nullptr, 1, 0, 0)
////    }
////
////    bool render ()
////    {
////        glm::vec2 WindowSize (this->getWindowSize());
////
////        {
////            glm::mat4 ProjectionA = glm ::scale(glm::perspective(glm::pi<float>() * 0.25f, float(FRAMEBUFFER_SIZE.x) / FRAMEBUFFER_SIZE.y, 0.1f, 100.0f), glm::vec3(1, -1, 1))
////            *reinterpret_cast < glm::mat4 * >(this->UniformPointer+0) = ProjectionA * this->view() * glm::mat4(1)
////
////            glm::mat4 ProjectionB = glm ::perspective(glm::pi<float>() * 0.25f, WindowSize.x / WindowSize.y, 0.1f, 100.0f)
////            *reinterpret_cast < glm::mat4 * >(this->UniformPointer+this->UniformBlockSize) = ProjectionB * this->view() * glm::scale(glm::mat4(1), glm::vec3(2))
////        }
////
////        // Step 1, render the scene in a multisampled framebuffer
////        glBindProgramPipeline(PipelineName)
////
////        renderFBO()
////
////        // Step 2: blit
////        glBlitNamedFramebuffer(FramebufferName[framebuffer::RENDER], FramebufferName[framebuffer::RESOLVE],
////                0, 0, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y,
////                0, 0, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y,
////                GL_COLOR_BUFFER_BIT, GL_NEAREST)
////
////        GLenum MaxColorAttachment = GL_COLOR_ATTACHMENT0
////                glInvalidateNamedFramebufferData(FramebufferName[framebuffer::RENDER], 1, & MaxColorAttachment)
////
////        // Step 3, render the colorbuffer from the multisampled framebuffer
////        renderFB()
////
////        return true
////    }
//}