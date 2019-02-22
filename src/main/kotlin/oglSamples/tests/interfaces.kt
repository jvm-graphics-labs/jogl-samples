package oglSamples.tests

import java.nio.IntBuffer
import kotlin.reflect.KMutableProperty0


interface GlEnum {
    val names: KMutableProperty0<IntBuffer>
}

interface GlProgramEnum {
    val names: KMutableProperty0<IntArray>
}


interface GlBufferEnum0 : GlEnum {
    override val names: KMutableProperty0<IntBuffer>
        get() = ::bufferName0
}

lateinit var bufferName0: IntBuffer


interface GlProgramEnum0 : GlProgramEnum {
    override val names: KMutableProperty0<IntArray>
        get() = ::programName0
}

lateinit var programName0: IntArray