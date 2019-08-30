package oglSamples

import gln.buffer.GlBuffersDsl
import gln.identifiers.GlBuffers


//fun Buffers(size: Int) = Buffers(GlBuffers(size))
//
//inline class Buffers(val buffers: GlBuffers) {
//
//    val VERTEX get() = buffers[0]
//    val ELEMENT get() = buffers[1]
//
//    fun gen() = buffers.gen()
//    inline fun gen(block: GlBuffersDsl.() -> Unit) = buffers.gen(block)
//}