package oglSamples.tests.gl300

import gli_.Texture2d
import gli_.gl
import gli_.gli
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec4.Vec4
import gln.*
import gln.glClearColor as b
import gln.glEnable as a
import gln.cap.Caps
import gln.clear.glClearBuffer
import gln.draw.glDrawArrays
import gln.framebuffer.glBindFramebuffer
import gln.glf.glf
import gln.objects.GlProgram
import gln.renderbuffer.RenderBuffer
import gln.uniform.glUniform
import gln.vertexArray.glEnableVertexAttribArray
import gln.vertexArray.glVertexAttribPointer
import oglSamples.*
import oglSamples.framework.Framework
import oglSamples.framework.GLint
import oglSamples.framework.semantic
import org.lwjgl.opengl.GL11C
import org.lwjgl.opengl.GL13C
import org.lwjgl.opengl.GL30C
import org.lwjgl.opengl.GL30C.*

fun main() {
    gl_300_fbo_multisample()()
}

class gl_300_fbo_multisample : Framework("gl-300-fbo-multisample", Caps.Profile.COMPATIBILITY, 3, 0) {

    val SHADER_SOURCE = "gl-300/image-2d"
    val TEXTURE_DIFFUSE = "kueken7_rgba8_srgb.dds"
    val FRAMEBUFFER_SIZE = Vec2i(160, 120)

    // With DDS textures, v texture coordinate are reversed, from top to bottom
    val vertexCount = 6
    val vertexSize = vertexCount * Vertex_v2v2.size
    val vertexData = vertex_v2v2_BufferOf(
            Vertex_v2v2(Vec2(-2.0f, -1.5f), Vec2(0.0f, 0.0f)),
            Vertex_v2v2(Vec2(+2.0f, -1.5f), Vec2(1.0f, 0.0f)),
            Vertex_v2v2(Vec2(+2.0f, +1.5f), Vec2(1.0f, 1.0f)),
            Vertex_v2v2(Vec2(+2.0f, +1.5f), Vec2(1.0f, 1.0f)),
            Vertex_v2v2(Vec2(-2.0f, +1.5f), Vec2(0.0f, 1.0f)),
            Vertex_v2v2(Vec2(-2.0f, -1.5f), Vec2(0.0f, 0.0f)))

    var program = GlProgram.NULL
    val vertexArray = GlVertexArray()
    val buffer = GlArrayBuffer()
    val texture = GlTexture2d()
    val colorRenderbuffer = GlRenderBuffer()
    val colorTexture = GlTexture2d()
    val framebufferRender = GlFramebuffer()
    val framebufferResolve = GlFramebuffer()
    var uniformMVP = -1
    var uniformDiffuse = -1

    override fun begin(): Boolean {

        var validated = true

        val caps = Caps(Caps.Profile.COMPATIBILITY)

        if (validated)
            validated = initProgram()
        if (validated)
            validated = initBuffer()
        if (validated)
            validated = initVertexArray()
        if (validated)
            validated = initTexture()
        if (validated)
            validated = initFramebuffer()

        return validated && checkError("begin")
    }

    fun initProgram(): Boolean {

        var validated = true

        try {

            program = GlProgram.initFromPath("$SHADER_SOURCE.vert", "$SHADER_SOURCE.frag") {

                "Position".attrib = semantic.attr.POSITION
                "Texcoord".attrib = semantic.attr.TEXCOORD
            }
        } catch (exc: Exception) {
            validated = false
        }

        if (validated) {
            uniformMVP = program.getUniformLocation("MVP")
            uniformDiffuse = program.getUniformLocation("Diffuse")
        }

        return validated && checkError("initProgram")
    }

    fun initBuffer(): Boolean {
        buffer.gen().bind {
            data(vertexData.data)
        }
        return checkError("initBuffer")
    }

    fun initTexture(): Boolean {

        val texture = Texture2d(gli.loadDds(ClassLoader.getSystemResource(TEXTURE_DIFFUSE).toURI()))
        gl.profile = gl.Profile.GL32

        this.texture.gen().bind(0) {
            levels(0, texture.levels() - 1)
            filter(linear_mmLinear, linear)

            val format = gl.translate(texture.format, texture.swizzles)
            for (level in 0 until texture.levels())
                image(level,
                        format.internal,
                        texture[level].extent(),
                        format.external, format.type,
                        texture[level].data()!!)
        }
        return checkError("initTexture")
    }

    fun initVertexArray(): Boolean {

        vertexArray.gen().bind {
            buffer.bind {
                glVertexAttribPointer(glf.pos2_tc2)
            }
            glEnableVertexAttribArray(glf.pos2_tc2)
        }

        return checkError("initVertexArray")
    }

    fun initFramebuffer(): Boolean {

        colorRenderbuffer.gen().bind {
            // The first parameter is the number of samples.
            storageMultisample(8, GL11C.GL_RGBA8, FRAMEBUFFER_SIZE)

            if (!validate(FRAMEBUFFER_SIZE, 8, GL11C.GL_RGBA8))
                return false
        }

        framebufferRender.gen().bind {
            renderbuffer(GL30C.GL_COLOR_ATTACHMENT0, colorRenderbuffer)
            if (!complete)
                return false
        }

        colorTexture.gen().bind {
            filter(nearest, nearest)
            image(GL11C.GL_RGBA8, FRAMEBUFFER_SIZE, GL11C.GL_RGBA, GL11C.GL_UNSIGNED_BYTE)
        }
        framebufferResolve.gen().bind {
            texture(GL_COLOR_ATTACHMENT0, colorTexture)
            if (!complete)
                return false
        }

        return checkError("initFramebuffer")
    }

    fun RenderBuffer.validate(expectedSize: Vec2i, expectedSamples: GLint, expectedFormat: GLint): Boolean =
            expectedSize == size && expectedSamples == samples && expectedFormat == format

    override fun render(): Boolean {

        // Clear the framebuffer
        glBindFramebuffer(0)
        glClearBuffer(GL11C.GL_COLOR, 0, Vec4(1f, 0.5f, 0f, 1f))

        program.use()
        glUniform(uniformDiffuse, 0)

        // Pass 1
        // Render the scene in a multisampled framebuffer
        glEnable(GL13C.GL_MULTISAMPLE)
        renderFBO(framebufferRender)
        glDisable(GL_MULTISAMPLE)

        // Resolved multisampling
        framebufferRender.bindRead()
        framebufferResolve.bindDraw()
        glBlitFramebuffer(FRAMEBUFFER_SIZE)
        glBindFramebuffer()

        // Pass 2
        // Render the colorbuffer from the multisampled framebuffer
        glViewport(windowSize)
        renderFB(colorTexture)

        return true
    }

    fun renderFBO(framebuffer: GlFramebuffer)    {

        framebuffer.bind()
        glClearColor(0f, 0.5f, 1f, 1f)
        glClear(GL11C.GL_COLOR_BUFFER_BIT)

        val perspective = glm.perspective(glm.πf * 0.25f, FRAMEBUFFER_SIZE.aspect, 0.1f, 100f)
        val model = Mat4(1f)
        val mvp = perspective * view * model
        glUniform(uniformMVP, mvp)

        glViewport(FRAMEBUFFER_SIZE)

        texture.bind(0)

        vertexArray.bind()
        glDrawArrays(vertexCount)

        checkError("renderFBO")
    }

    fun renderFB(texture2D: GlTexture2d)    {

        val perspective = glm.perspective(glm.πf * 0.25f, windowSize.aspect, 0.1f, 100f)
        val model = Mat4(1f)
        val mvp = perspective * view * model
        glUniform(uniformMVP, mvp)

        texture2D.bind(0)

        vertexArray.bind()
        glDrawArrays(vertexCount)

        checkError("renderFB")
    }

    override fun end(): Boolean {

        buffer.delete()
        program.delete()
        texture.delete()
        colorTexture.delete()
        colorRenderbuffer.delete()
        framebufferRender.delete()
        framebufferResolve.delete()
        vertexArray.delete()

        return true
    }
}