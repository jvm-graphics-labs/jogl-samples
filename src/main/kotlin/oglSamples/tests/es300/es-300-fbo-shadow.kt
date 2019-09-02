package oglSamples.tests.es300

import glm_.L
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import glm_.vec4.Vec4ub
import gln.*
import gln.BufferTarget.Companion.ARRAY
import gln.BufferTarget.Companion.ELEMENT_ARRAY
import gln.TextureTarget.Companion._2D
import gln.cap.Caps.Profile.ES
import gln.draw.glDrawElements
import gln.glf.glf
import gln.identifiers.GlBuffers
import gln.identifiers.GlProgram
import gln.misc.glClearColorBuffer
import gln.misc.glClearDepthBuffer
import gln.texture.TexMagFilter
import gln.texture.TexMinFilter
import gln.texture.TexWrap
import gln.uniform.glUniform
import kool.ByteBuffer
import kool.pos
import kool.shortBufferOf
import oglSamples.*
import oglSamples.framework.Framework
import oglSamples.framework.semantic
import oglSamples.tests.es300.es_300_fbo_shadow.Buffer.ELEMENT
import oglSamples.tests.es300.es_300_fbo_shadow.Buffer.VERTEX
import oglSamples.tests.es300.es_300_fbo_shadow.Texture.*
import org.lwjgl.opengl.GL13C.GL_TEXTURE0
import org.lwjgl.opengl.GL13C.glActiveTexture
import org.lwjgl.opengl.GL20C.*
import org.lwjgl.opengl.GL30C.GL_COLOR_ATTACHMENT0

fun main() {
    es_300_fbo_shadow()()
}

private class es_300_fbo_shadow : Framework("es-300-fbo-shadow", ES, 3, 0, Vec2(0f, -glm.πf * 0.3f)) {

    val SHADER_SOURCE_DEPTH = "es-300/fbo-shadow-depth"
    val SHADER_SOURCE_RENDER = "es-300/fbo-shadow-render"

    val vertexCount = 8
    val vertexSize = vertexCount * glf.pos3_col4ub.stride
    val vertexData = ByteBuffer(vertexSize).also {
        it += Vec3(-1.0f, -1.0f, 0.0f); it += Vec4ub(255, 127, 0, 255)
        it += Vec3(+1.0f, -1.0f, 0.0f); it += Vec4ub(255, 127, 0, 255)
        it += Vec3(+1.0f, +1.0f, 0.0f); it += Vec4ub(255, 127, 0, 255)
        it += Vec3(-1.0f, +1.0f, 0.0f); it += Vec4ub(255, 127, 0, 255)
        it += Vec3(-0.1f, -0.1f, 0.2f); it += Vec4ub(0, 127, 255, 255)
        it += Vec3(+0.1f, -0.1f, 0.2f); it += Vec4ub(0, 127, 255, 255)
        it += Vec3(+0.1f, +0.1f, 0.2f); it += Vec4ub(0, 127, 255, 255)
        it += Vec3(-0.1f, +0.1f, 0.2f); it += Vec4ub(0, 127, 255, 255)
        it.pos = 0
    }

    val elements = shortBufferOf(
            0, 1, 2,
            2, 3, 0,
            4, 5, 6,
            6, 7, 4)

    enum class Buffer { VERTEX, ELEMENT, TRANSFORM }

    enum class Texture { COLORBUFFER, DEPTHBUFFER, SHADOWMAP }

    enum class Program { DEPTH, RENDER }

    enum class Framebuffer { FRAMEBUFFER, SHADOW }

    val shadowSize = Vec2i(64)

    val framebuffers = GlFramebuffers<Framebuffer>()
    val programs = Array(Program.values().size) { GlProgram.NULL }
    val vertexArrays = GlVertexArrays<Program>()
    val buffers = GlBuffers<Buffer>()
    val textures = GlTextures<Texture>()
    var uniformRenderMVP: UniformLocation = 0
    var uniformRenderDepthBiasMVP: UniformLocation = 0
    var uniformRenderShadow: UniformLocation = 0
    var uniformDepthMVP: UniformLocation = 0

    override fun begin(): Boolean {

        var validated = initProgram()
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

        var validated = try {
            programs[Program.RENDER] = GlProgram.initFromPath(SHADER_SOURCE_RENDER) {
                "Position".attrib = semantic.attr.POSITION
                "Color".attrib = semantic.attr.COLOR
            }
            true
        } catch (_: Exception) {
            false
        }

        if (validated)
            programs[Program.RENDER]{
                uniformRenderShadow = "Shadow".uniform
                uniformRenderMVP = "MVP".uniform
                uniformRenderDepthBiasMVP = "DepthBiasMVP".uniform
            }

        if (validated)
            try {
                programs[Program.DEPTH] = GlProgram.initFromPath(SHADER_SOURCE_DEPTH) {
                    "Position".attrib = semantic.attr.POSITION
                }
            } catch (_: Exception) {
                validated = false
            }

        if (validated)
            uniformDepthMVP = programs[Program.DEPTH] getUniformLocation "MVP"

        return validated
    }

    fun initBuffer(): Boolean {

        buffers.gen {
            ELEMENT.bound(ELEMENT_ARRAY) { data(elements) }
            VERTEX.bound(ARRAY) { data(vertexData) }
        }

        return true
    }

    fun initTexture(): Boolean {

        textures.gen {

            gl.activeTexture = 0
            COLORBUFFER.bind(_2D) {
                levels = 0..0
                storage2D(gli_.gl.InternalFormat.RGBA8_UNORM, windowSize)
            }
            DEPTHBUFFER.bind(_2D) {
                levels = 0..0
                storage2D(gli_.gl.InternalFormat.D24, windowSize)
            }

            SHADOWMAP.bind(_2D) {
                levels = 0..0
                wrapST = TexWrap.CLAMP_TO_EDGE
                filters(TexMinFilter.LINEAR, TexMagFilter.LINEAR)
                compareFunc = CompareFunction.LEQUAL
                compareMode = TextureCompareMode.COMPARE_REF_TO_TEXTURE
                storage2D(gli_.gl.InternalFormat.D24, shadowSize)
            }
        }
        return true
    }

    fun initVertexArray(): Boolean {

        vertexArrays.gen {
            Program.RENDER.bound {
                buffers[VERTEX].bound(ARRAY) {
                    glVertexAttribPointer(semantic.attr.POSITION, Vec3.length, GL_FLOAT, false, glf.pos3_col4ub.stride, 0)
                    glVertexAttribPointer(semantic.attr.COLOR, Vec4ub.length, GL_UNSIGNED_BYTE, true, glf.pos3_col4ub.stride, Vec3.size.L)
                }

                glf.pos3_col4ub.enable()

                buffers[ELEMENT] bind ELEMENT_ARRAY
            }
        }

        return true
    }

    fun initFramebuffer(): Boolean {

        framebuffers.gen {

            Framebuffer.FRAMEBUFFER.bind {
                texture(Attachment.COLOR0, textures[COLORBUFFER])
                texture(Attachment.DEPTH, textures[DEPTHBUFFER])
                glDrawBuffers(GL_COLOR_ATTACHMENT0)
                if (!complete)
                    return false
            }

            framebuffers[Framebuffer.SHADOW].bind {
                texture(Attachment.DEPTH, textures[SHADOWMAP])
                if (!complete)
                    return false
            }
        }

        return defaultFbo.bind { complete }
    }

    override fun render(): Boolean {

        val model = Mat4()

        val depthProjection = glm.ortho(-1f, 1f, -1f, 1f, -4f, 8f)
        val depthView = glm.lookAt(Vec3(0.5, 1.0, 2.0), Vec3(), Vec3(0, 0, 1))
        val depthMVP = depthProjection * depthView * model

        val biasMatrix = Mat4(
                0.5, 0.0, 0.0, 0.0,
                0.0, 0.5, 0.0, 0.0,
                0.0, 0.0, 0.5, 0.0,
                0.5, 0.5, 0.5, 1.0)

        val depthMVPBias = biasMatrix * depthMVP

        val renderProjection = glm.perspective(glm.πf * 0.25f, windowSize.aspect, 0.1f, 10f)
        val renderMVP = renderProjection * this.view * model

        programs[Program.DEPTH].use()
        glUniform(uniformDepthMVP, depthMVP)

        renderShadow()

        programs[Program.RENDER].use()
        glUniform(uniformRenderShadow, 0)
        glUniform(uniformRenderMVP, renderMVP)
        glUniform(uniformRenderDepthBiasMVP, depthMVPBias)

        renderFramebuffer()

        return checkError("render")
    }

    fun renderShadow() {

        glEnable(GL_DEPTH_TEST)
        glDepthFunc(GL_LESS)

        glViewport(shadowSize)

        framebuffers[Framebuffer.SHADOW].bind()
        glClearDepthBuffer(1f)

        vertexArrays[Program.RENDER].bind()
        glDrawElements(elements)

        glDisable(GL_DEPTH_TEST)

        checkError("renderShadow")
    }

    fun renderFramebuffer() {

        glEnable(GL_DEPTH_TEST)
        glDepthFunc(GL_LESS)

        glViewport(windowSize)

        defaultFbo.bind()
        glClearDepthBuffer(1f)
        glClearColorBuffer(Vec4(0f, 0f, 0f, 1f))

        glActiveTexture(GL_TEXTURE0)
        textures[Texture.SHADOWMAP] bind _2D

        vertexArrays[Program.RENDER].bind()
        glDrawElements(elements)

        glDisable(GL_DEPTH_TEST)

        checkError("renderFramebuffer")
    }

    override fun end(): Boolean {

        programs.forEach { it.delete() }

        framebuffers.delete()
        buffers.delete()
        textures.delete()
        vertexArrays.delete()

        return checkError("end")
    }
}