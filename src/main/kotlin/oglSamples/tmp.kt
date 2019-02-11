package oglSamples

import uno.glfw.glfw

operator fun glfw.invoke(block: glfw.() -> Unit) {
    glfw.block()
}