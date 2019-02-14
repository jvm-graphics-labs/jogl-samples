package oglSamples

import glm_.vec2.Vec2
import gln.GL_STATIC_DRAW
import gln.Usage
import gln.buffer.Buffer
import gln.buffer.BufferTarget
import gln.buffer.bufferName
import kool.FloatBuffer
import kool.IntBuffer
import kool.free
import org.lwjgl.opengl.GL15C
import uno.glfw.glfw
import java.nio.FloatBuffer
import java.nio.IntBuffer

operator fun glfw.invoke(block: glfw.() -> Unit) {
    glfw.block()
}

infix fun Int.wo(b: Int) = and(b.inv())

fun glBeginQuery(target: Int, id: IntBuffer) = GL15C.glBeginQuery(target, id[0])

fun glGetQueryObjectui(id: IntBuffer, name: Int) = GL15C.glGetQueryObjectui(id[0], name)


inline class Vec2Buffer(val data: FloatBuffer)

fun Vec2Buffer(size: Int) = Vec2Buffer(FloatBuffer(size * Vec2.length))

inline fun Vec2Buffer(size: Int, init: (Int) -> Vec2): Vec2Buffer {
    val buffer = Vec2Buffer(size)
    for (i in 0 until size)
        init(i).to(buffer.data, i * Vec2.length)
    return buffer
}

fun vec2BufferOf(vararg vecs: Vec2) = Vec2Buffer(vecs.size) { vecs[it] }


class Vertex_v2v2(val a: Vec2, val b: Vec2) {

    fun to(floats: FloatBuffer, index: Int) {
        a.to(floats, index)
        b.to(floats, index + Vec2.length)
    }

    companion object {
        val size = Vec2.size * 2
        val length = Vec2.length * 2
    }
}

inline class Vertex_v2v2Buffer(val data: FloatBuffer) {


}

fun Vertex_v2v2_Buffer(size: Int) = Vertex_v2v2Buffer(FloatBuffer(size * Vertex_v2v2.length))

inline fun Vertex_v2v2_Buffer(size: Int, init: (Int) -> Vertex_v2v2): Vertex_v2v2Buffer {
    val buffer = Vertex_v2v2_Buffer(size)
    for (i in 0 until size)
        init(i).to(buffer.data, i * Vertex_v2v2.length)
    return buffer
}

fun vertex_v2v2_buffer_of(vararg vertices: Vertex_v2v2) = Vertex_v2v2_Buffer(vertices.size) { vertices[it] }


fun Buffer.data(data: Vec2Buffer, usage: Usage = GL_STATIC_DRAW) = GL15C.glBufferData(target.i, data.data, usage.i)


interface GlBufferI {
//    val target: BufferTarget
}

fun <E> Enum<E>.bindArray() where E : Enum<E>, E : GlBufferI = bind(BufferTarget.Array)
fun <E> Enum<E>.bindElement() where E : Enum<E>, E : GlBufferI = bind(BufferTarget.ElementArray)
fun <E> Enum<E>.bind(target: BufferTarget) where E : Enum<E>, E : GlBufferI = GL15C.glBindBuffer(target.i, bufferName[ordinal])


inline fun <E> Enum<E>.bindArray(block: Buffer.() -> Unit) where E : Enum<E>, E : GlBufferI = bind(BufferTarget.Array, block)
inline fun <E> Enum<E>.bindElement(block: Buffer.() -> Unit) where E : Enum<E>, E : GlBufferI = bind(BufferTarget.ElementArray, block)
inline fun <E> Enum<E>.bind(target: BufferTarget, block: Buffer.() -> Unit) where E : Enum<E>, E : GlBufferI {
    Buffer.target = target
    Buffer.name = bufferName[ordinal]
    Buffer.block()
    Buffer.name = 0
}

inline fun <reified E : Enum<E>> glGenBuffers(): IntBuffer {
    bufferName = IntBuffer<E>()
    return bufferName
}

fun glDeleteBuffers() = bufferName.free()
