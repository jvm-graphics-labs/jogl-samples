package oglSamples

import glm_.glm
import org.lwjgl.opengl.GL15C
import uno.glfw.glfw
import java.nio.IntBuffer

operator fun glfw.invoke(block: glfw.() -> Unit) {
    glfw.block()
}

infix fun Int.wo(b: Int) = and(b.inv())

fun glBeginQuery(target: Int, id: IntBuffer) = GL15C.glBeginQuery(target, id[0])

fun glGetQueryObjectui(id: IntBuffer, name: Int) = GL15C.glGetQueryObjectui(id[0], name)