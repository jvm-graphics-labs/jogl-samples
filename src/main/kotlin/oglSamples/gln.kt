package oglSamples

import gln.GL_ARRAY_BUFFER
import gln.buffer.glBindArrayBuffer
import gln.buffer.glBindBuffer
import gln.objects.GlBuffer
import kool.IntBuffer
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL15C
import java.nio.IntBuffer

inline class GlArrayBuffer(val buffer: IntBuffer) {

    fun gen(): GlArrayBuffer {
        GL15C.glGenBuffers(buffer)
        return this
    }

    inline fun bind(block: GlBuffer.() -> Unit) {
        GL15C.glBindBuffer(GL_ARRAY_BUFFER.i, buffer[0])
        GlBuffer.i = buffer[0]
        GlBuffer.target = GL_ARRAY_BUFFER
        GlBuffer.block()
        GL15C.glBindBuffer(GL_ARRAY_BUFFER.i, 0)
    }
}

fun GlArrayBuffer() = GlArrayBuffer(IntBuffer(1))