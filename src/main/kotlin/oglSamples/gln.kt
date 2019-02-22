package oglSamples

import gln.BufferTarget
import gln.buffer.GlBufferDSL
import gln.framebuffer.Framebuffer
import gln.objects.GlProgram
import gln.objects.GlTexture
import gln.program.ProgramBase
import gln.renderbuffer.RenderBuffer
import kool.IntBuffer
import org.lwjgl.opengl.*
import java.nio.IntBuffer


inline class GlBuffer(val name: Int = -1) {

    inline fun bound(target: BufferTarget, block: GlBufferDSL.() -> Unit): GlBuffer {
        GL15C.glBindBuffer(target.i, name)
        GlBufferDSL.name = name
        GlBufferDSL.target = target
        GlBufferDSL.block()
        GL15C.glBindBuffer(target.i, 0)
        return this
    }

    fun delete() = GL15C.glDeleteBuffers(name)

    companion object {
        fun gen(): GlBuffer = GlBuffer(GL15C.glGenBuffers())
    }
}


inline class GlRenderBuffer(val name: Int = -1) {

    inline fun bind(block: RenderBuffer.() -> Unit): GlRenderBuffer {
        GL30C.glBindRenderbuffer(GL30C.GL_RENDERBUFFER, name)
        RenderBuffer.block()
        GL30C.glBindRenderbuffer(GL30C.GL_RENDERBUFFER, 0)
        return this
    }

    fun delete() = GL30C.glDeleteRenderbuffers(name)

    companion object {
        fun gen(): GlRenderBuffer = GlRenderBuffer(GL30C.glGenRenderbuffers())
    }
}


inline class GlVertexArray(val name: Int = -1) {

    fun bind() = GL30C.glBindVertexArray(name)
    inline fun bound(block: () -> Unit): GlVertexArray {
        GL30C.glBindVertexArray(name)
        block()
        GL30C.glBindVertexArray(0)
        return this
    }

    fun delete() = GL30C.glDeleteVertexArrays(name)

    companion object {
        fun gen(): GlVertexArray = GlVertexArray(GL30C.glGenVertexArrays())
    }
}


inline class GlFramebuffer(val name: Int = -1) {

    fun bind() = GL30C.glBindFramebuffer(GL30C.GL_FRAMEBUFFER, name)

    fun bindRead() = GL30C.glBindFramebuffer(GL30C.GL_READ_FRAMEBUFFER, name)
    fun bindDraw() = GL30C.glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, name)

    inline fun bind(block: Framebuffer.() -> Unit): GlFramebuffer {
        GL30C.glBindFramebuffer(GL30C.GL_FRAMEBUFFER, name)
        Framebuffer.name = name
        Framebuffer.block()
        GL30C.glBindFramebuffer(GL30C.GL_FRAMEBUFFER, 0)
        return this
    }

    fun delete() = GL30C.glDeleteFramebuffers(name)

    companion object {
        fun gen(): GlFramebuffer = GlFramebuffer(GL30C.glGenFramebuffers())
    }
}


inline class GlPipeline(val name: Int = -1) {

    fun bind() = GL30C.glBindFramebuffer(GL30C.GL_FRAMEBUFFER, name)

    fun bindRead() = GL30C.glBindFramebuffer(GL30C.GL_READ_FRAMEBUFFER, name)
    fun bindDraw() = GL30C.glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, name)

    inline fun bind(block: Framebuffer.() -> Unit) {
        GL30C.glBindFramebuffer(GL30C.GL_FRAMEBUFFER, name)
        Framebuffer.name = name
        Framebuffer.block()
        GL30C.glBindFramebuffer(GL30C.GL_FRAMEBUFFER, 0)
    }

    fun useStages(stages: Int, program: GlProgram): GlPipeline {
        GL41C.glUseProgramStages(name, stages, program.i)
        return this
    }

    fun delete() = GL30C.glDeleteFramebuffers(name)

    companion object {
        fun gen(): GlPipeline = GlPipeline(GL41C.glGenProgramPipelines())
    }
}

inline class GlSampler(val name: Int = -1) {

//    fun bind() = GL30C.glBindFramebuffer(GL30C.GL_FRAMEBUFFER, name)
//
//    fun bindRead() = GL30C.glBindFramebuffer(GL30C.GL_READ_FRAMEBUFFER, name)
//    fun bindDraw() = GL30C.glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, name)
//
//    inline fun bind(block: Framebuffer.() -> Unit) {
//        GL30C.glBindFramebuffer(GL30C.GL_FRAMEBUFFER, name)
//        Framebuffer.name = name
//        Framebuffer.block()
//        GL30C.glBindFramebuffer(GL30C.GL_FRAMEBUFFER, 0)
//    }

    fun delete() = GL33C.glDeleteSamplers(name)

    companion object {
        fun gen(): GlSampler = GlSampler(GL33C.glGenSamplers())
    }
}


fun Framebuffer.renderbuffer(attachment: Int, renderbuffer: GlRenderBuffer) = GL30C.glFramebufferRenderbuffer(GL30C.GL_FRAMEBUFFER, attachment, GL30C.GL_RENDERBUFFER, renderbuffer.name)

fun Framebuffer.texture(attachment: Int, texture: GlTexture, level: Int = 0) = GL30C.glFramebufferTexture2D(GL30C.GL_FRAMEBUFFER, attachment, GL11C.GL_TEXTURE_2D, texture.name, level)


inline fun <reified E : Enum<E>> GlProgram.uniformBlockBinding(uniformBlockIndex: Int, uniformBlockBinding: E) = GL31C.glUniformBlockBinding(i, uniformBlockIndex, uniformBlockBinding.ordinal)

fun glGenBuffers(size: Int) = IntBuffer(size).also(GL15C::glGenBuffers)
fun glCreateBuffers(size: Int) = IntBuffer(size).also(GL45C::glCreateBuffers)

var ProgramBase.separable: Boolean
    get() = throw Exception("Invalid")
    set(value) = GL41C.glProgramParameteri(program.i, GL41C.GL_PROGRAM_SEPARABLE, if(value) GL11C.GL_TRUE else GL11C.GL_FALSE)