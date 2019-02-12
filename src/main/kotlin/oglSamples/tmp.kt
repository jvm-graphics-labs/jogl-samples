package oglSamples

import glm_.vec2.Vec2
import kool.FloatBuffer
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