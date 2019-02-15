package oglSamples

import glm_.L
import glm_.vec3.Vec3
import gln.GL_ARRAY_BUFFER
import gln.GL_STATIC_DRAW
import gln.Usage
import gln.buffer.Buffer
import gln.buffer.MappedBuffer
import gln.framebuffer.Framebuffer
import gln.objects.GlBuffer
import gln.objects.GlProgram
import gln.renderbuffer.RenderBuffer
import gln.texture.Texture2d
import kool.IntBuffer
import kool.adr
import kool.pos
import kool.stak
import org.lwjgl.opengl.*
import java.nio.ByteBuffer
import java.nio.IntBuffer

inline class GlArrayBuffer(val name: IntBuffer) {

    fun gen(): GlArrayBuffer {
        GL15C.glGenBuffers(name)
        return this
    }

    inline fun bind(block: GlBuffer.() -> Unit) {
        GL15C.glBindBuffer(GL_ARRAY_BUFFER.i, name[0])
        GlBuffer.i = name[0]
        GlBuffer.target = GL_ARRAY_BUFFER
        GlBuffer.block()
        GL15C.glBindBuffer(GL_ARRAY_BUFFER.i, 0)
    }

    fun delete() = GL15C.glDeleteBuffers(name)
}

fun GlArrayBuffer() = GlArrayBuffer(IntBuffer(1))

inline class GlTexture2d(val name: IntBuffer) {

    fun gen(): GlTexture2d {
        GL11C.glGenTextures(name)
        return this
    }

    fun bind(unit: Int) {
        GL13C.glActiveTexture(GL13C.GL_TEXTURE0 + unit)
        GL13C.glBindTexture(GL11C.GL_TEXTURE_2D, name[0])
    }

    inline fun bind(unit: Int, block: Texture2d.() -> Unit) {
        GL13C.glActiveTexture(GL13C.GL_TEXTURE0 + unit)
        bind(block)
    }

    inline fun bind(block: Texture2d.() -> Unit) {
        Texture2d.name = name[0] // bind
        Texture2d.block()
        Texture2d.name = 0 // unbind
    }

    fun delete() = GL15C.glDeleteTextures(name)
}

fun GlTexture2d() = GlTexture2d(IntBuffer(1))


inline class GlRenderBuffer(val name: IntBuffer) {

    fun gen(): GlRenderBuffer {
        GL30C.glGenRenderbuffers(name)
        return this
    }

    inline fun bind(block: RenderBuffer.() -> Unit) {
        GL30C.glBindRenderbuffer(GL30C.GL_RENDERBUFFER, name[0])
        RenderBuffer.block()
        GL30C.glBindRenderbuffer(GL30C.GL_RENDERBUFFER, 0)
    }

    fun delete() = GL30C.glDeleteRenderbuffers(name)
}

fun GlRenderBuffer() = GlRenderBuffer(IntBuffer(1))


inline class GlVertexArray(val name: IntBuffer) {

    fun gen(): GlVertexArray {
        GL30C.glGenVertexArrays(name)
        return this
    }

    fun bind() = GL30C.glBindVertexArray(name[0])
    inline fun bind(block: () -> Unit) {
        GL30C.glBindVertexArray(name[0])
        block()
        GL30C.glBindVertexArray(0)
    }

    fun delete() = GL30C.glDeleteVertexArrays(name)
}

fun GlVertexArray() = GlVertexArray(IntBuffer(1))


inline class GlFramebuffer(val name: IntBuffer) {

    fun gen(): GlFramebuffer {
        GL30C.glGenFramebuffers(name)
        return this
    }

    fun bind() = GL30C.glBindFramebuffer(GL30C.GL_FRAMEBUFFER, name[0])

    fun bindRead() = GL30C.glBindFramebuffer(GL30C.GL_READ_FRAMEBUFFER, name[0])
    fun bindDraw() = GL30C.glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, name[0])

    inline fun bind(block: Framebuffer.() -> Unit) {
        GL30C.glBindFramebuffer(GL30C.GL_FRAMEBUFFER, name[0])
        Framebuffer.name = name[0]
        Framebuffer.block()
        GL30C.glBindFramebuffer(GL30C.GL_FRAMEBUFFER, 0)
    }

    fun delete() = GL30C.glDeleteFramebuffers(name)
}

fun GlFramebuffer() = GlFramebuffer(IntBuffer(1))

fun Framebuffer.renderbuffer(attachment: Int, renderbuffer: GlRenderBuffer) = GL30C.glFramebufferRenderbuffer(GL30C.GL_FRAMEBUFFER, attachment, GL30C.GL_RENDERBUFFER, renderbuffer.name[0])

fun Framebuffer.texture(attachment: Int, texture: GlTexture2d, level: Int = 0) = GL30C.glFramebufferTexture2D(GL30C.GL_FRAMEBUFFER, attachment, GL11C.GL_TEXTURE_2D, texture.name[0], level)

fun Buffer.data(data: Vec3, usage: Usage = GL_STATIC_DRAW) = stak { GL15C.glBufferData(Buffer.target.i, data.toBuffer(it), usage.i) }

fun Buffer.mapBufferRange(size: Int, flags: Int) = mapBufferRange(0, size, flags)
fun Buffer.mapBufferRange(offset: Int, size: Int, flags: Int) = GL30C.glMapBufferRange(target.i, offset.L, size.L, flags)

inline fun Buffer.mapBufferRange(size: Int, flags: Int, block: MappedBuffer.(ByteBuffer?) -> Unit) = mapBufferRange(0, size, flags, block)
inline fun Buffer.mapBufferRange(offset: Int, size: Int, flags: Int, block: MappedBuffer.(ByteBuffer?) -> Unit) {
    MappedBuffer.block(GL30C.glMapBufferRange(target.i, offset.L, size.L, flags))
    GL30C.glUnmapBuffer(target.i)
}

inline fun <reified E : Enum<E>> GlProgram.uniformBlockBinding(uniformBlockIndex: Int, uniformBlockBinding: E) = GL31C.glUniformBlockBinding(i, uniformBlockIndex, uniformBlockBinding.ordinal)

fun glGenBuffers(size: Int) = IntBuffer(size)