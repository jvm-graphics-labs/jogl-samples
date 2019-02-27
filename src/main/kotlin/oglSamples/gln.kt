package oglSamples

import gln.*
import gln.program.ProgramBase
import kool.IntBuffer
import org.lwjgl.opengl.*
import java.nio.IntBuffer


operator fun IntBuffer.get(e: Enum<*>) = get(e.ordinal)


fun glCreateBuffers(size: Int) = IntBuffer(size).also(GL45C::glCreateBuffers)

var ProgramBase.separable: Boolean
    get() = throw Exception("Invalid")
    set(value) = GL41C.glProgramParameteri(program.i, GL41C.GL_PROGRAM_SEPARABLE, if(value) GL11C.GL_TRUE else GL11C.GL_FALSE)



var DSA = false



inline fun <reified E : Enum<E>> GlTextures(): GlTextures = GlTextures(IntBuffer<E>())



//object GlTexturesDsl {
//
//    lateinit var names: IntBuffer
//
//
//}