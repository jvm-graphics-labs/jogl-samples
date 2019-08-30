package oglSamples

import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import glm_.vec4.Vec4ub
import gln.identifiers.GlFramebuffer
import gln.identifiers.GlFramebuffers
import gln.renderbuffer.GlRenderbuffers
import kool.FloatBuffer
import kool.IntBuffer
import org.lwjgl.opengl.GL15C
import uno.glfw.glfw
import java.nio.ByteBuffer
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

inline class Vertex_v2v2_Buffer(val data: FloatBuffer) {


}

fun Vertex_v2v2_Buffer(size: Int) = Vertex_v2v2_Buffer(FloatBuffer(size * Vertex_v2v2.length))

inline fun Vertex_v2v2_Buffer(size: Int, init: (Int) -> Vertex_v2v2): Vertex_v2v2_Buffer {
    val buffer = Vertex_v2v2_Buffer(size)
    for (i in 0 until size)
        init(i).to(buffer.data, i * Vertex_v2v2.length)
    return buffer
}

fun vertex_v2v2_buffer_of(vararg vertices: Vertex_v2v2) = Vertex_v2v2_Buffer(vertices.size) { vertices[it] }
fun vertex_v2v2_buffer_of(vararg vecs: Vec2) = Vertex_v2v2_Buffer(vecs.size / 2).apply {
    var i = 0
    while (i < vecs.size / 2) {
        vecs[i * 2].to(data, i * Vertex_v2v2.length)
        vecs[i * 2 + 1].to(data, i++ * Vertex_v2v2.length + Vec2.length)
    }
}


class Vertex_v3n3c4(val position: Vec3, val normal: Vec3, val color: Vec4) {

    fun to(floats: FloatBuffer, index: Int) {
        position.to(floats, index)
        normal.to(floats, index + Vec3.length)
        color.to(floats, index + Vec3.length * 2)
    }

    companion object {
        val size = Vec3.size * 2 + Vec4.size
        val length = Vec3.length * 2 + Vec4.size
    }
}

inline class Vertex_v3n3c4_Buffer(val data: FloatBuffer) {


}

fun Vertex_v3n3c4_Buffer(size: Int) = Vertex_v3n3c4_Buffer(FloatBuffer(size * Vertex_v3n3c4.length))

inline fun Vertex_v3n3c4_Buffer(size: Int, init: (Int) -> Vertex_v3n3c4): Vertex_v3n3c4_Buffer {
    val buffer = Vertex_v3n3c4_Buffer(size)
    for (i in 0 until size)
        init(i).to(buffer.data, i * Vertex_v3n3c4.length)
    return buffer
}

fun vertex_v3n3c4_buffer_of(vararg vertices: Vertex_v3n3c4): Vertex_v3n3c4_Buffer = Vertex_v3n3c4_Buffer(vertices.size) { vertices[it] }
fun vertex_v3n3c4_buffer_of(vararg vecs: Any): Vertex_v3n3c4_Buffer {
    val buf = Vertex_v3n3c4_Buffer(FloatBuffer(vecs.size / 3 * Vertex_v3n3c4.length))
    for(i in vecs.indices) {
        val p = vecs[i * 3] as Vec3
        val n = vecs[i * 3 + 1] as Vec3
        val c = vecs[i * 3 + 2] as Vec4
        p.to(buf.data, i * Vertex_v3n3c4.length)
        n.to(buf.data, i * Vertex_v3n3c4.length + Vec3.length)
        c.to(buf.data, i * Vertex_v3n3c4.length + Vec3.length * 2)
    }
    return buf
}




//fun <E> Enum<E>.bindArray() where E : Enum<E>, E : GlBufferEnum = bind(BufferTarget.ARRAY)
//fun <E> Enum<E>.bindElement() where E : Enum<E>, E : GlBufferEnum = bind(BufferTarget.ELEMENT_ARRAY)
//fun <E> Enum<E>.bindUniform() where E : Enum<E>, E : GlBufferEnum = bind(BufferTarget.UNIFORM)
//fun <E> Enum<E>.bind(target: BufferTarget) where E : Enum<E>, E : GlBufferEnum {
//    val values: Array<out Enum<*>> = Enum::class.java.enumConstants
//    val names by (values[0] as GlBufferEnum).names
//    GL15C.glBindBuffer(target.i, names[ordinal])
//}
//
//
//inline fun <E> Enum<E>.bindArray(block: GlBufferDSL.() -> Unit) where E : Enum<E>, E : GlBufferEnum = bound(BufferTarget.ARRAY, block)
//inline fun <E> Enum<E>.bindElement(block: GlBufferDSL.() -> Unit) where E : Enum<E>, E : GlBufferEnum = bound(BufferTarget.ELEMENT_ARRAY, block)
//inline fun <E> Enum<E>.bindUniform(block: GlBufferDSL.() -> Unit) where E : Enum<E>, E : GlBufferEnum = bound(BufferTarget.UNIFORM, block)
//inline fun <E> Enum<E>.bound(target: BufferTarget, block: GlBufferDSL.() -> Unit) where E : Enum<E>, E : GlBufferEnum {
//    GlBufferDSL.target = target
//    val names by (this as GlBufferEnum).names
//    GlBufferDSL.name = names[0]
//    GlBufferDSL.block()
//    GlBufferDSL.name = 0
//}
//
//inline fun <E> Enum<E>.storage(data: ByteBuffer, flags: Int = 0) where E : Enum<E>, E : GlBufferEnum {
//    val names by (this as GlBufferEnum).names
//    GL45C.glNamedBufferStorage(names[ordinal], data, flags)
//}
//inline fun <E> Enum<E>.storage(data: ShortBuffer, flags: Int = 0) where E : Enum<E>, E : GlBufferEnum {
//    val names by (this as GlBufferEnum).names
//    GL45C.glNamedBufferStorage(names[ordinal], data, flags)
//}
//
//inline fun <reified E> glGenBuffers() where E : Enum<E>, E : GlBufferEnum {
//    val values: Array<out Enum<*>> = E::class.java.enumConstants
//    var names by (values[0] as GlBufferEnum).names
//    names = glGenBuffers(values.size)
//}
//
//inline fun <reified E> glCreateBuffers() where E : Enum<E>, E : GlBufferEnum {
//    val values: Array<out Enum<*>> = E::class.java.enumConstants
//    var names by (values[0] as GlBufferEnum).names
//    names = glCreateBuffers(values.size)
//}
//
//inline fun <reified E> glDeleteBuffers() where E : Enum<E>, E : GlBufferEnum {
//    val values: Array<out Enum<*>> = E::class.java.enumConstants
//    val names by (values[0] as GlBufferEnum).names
//    GL15C.glDeleteBuffers(names)
//}

//operator fun <E: Enum<E>>IntBuffer.get(e: E): GlBuffer where E : Enum<E>, E : GlBufferEnum = GlBuffer(get(e.ordinal))
//fun <E: Enum<E>>IntBuffer.gen(e: E) where E : Enum<E>, E : GlBufferEnum = put(e.ordinal, GL15C.glGenBuffers())

operator fun ByteBuffer.plusAssign(v: Vec3) {
    putFloat(v.x).putFloat(v.y).putFloat(v.z)
}
operator fun ByteBuffer.plusAssign(v: Vec4ub) {
    put(v.x.v).put(v.y.v).put(v.z.v).put(v.w.v)
}

inline fun <reified E : Enum<E>> GlFramebuffers(): GlFramebuffers = GlFramebuffers(IntBuffer<E>())
inline fun <reified E : Enum<E>> GlRenderbuffers(): GlRenderbuffers = GlRenderbuffers(IntBuffer<E>())

operator fun <E : Enum<E>, T> Array<T>.get(index: E): T = get(index.ordinal)
operator fun <E : Enum<E>, T> Array<T>.set(index: E, value: T) = set(index.ordinal, value)

val defaultFbo = GlFramebuffer(0)