package oglSamples.tests.es200

import gli_.gl
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
import gln.DataType.Companion.UNSIGNED_SHORT
import gln.TextureTarget.Companion._2D
import gln.cap.Caps.Profile.ES
import gln.draw.glDrawElements
import gln.framebuffer.glBindFramebuffer
import gln.glf.glf
import gln.identifiers.GlBuffers
import gln.identifiers.GlProgram
import gln.misc.glClearColorBuffer
import gln.misc.glClearDepthBuffer
import gln.renderbuffer.GlRenderbuffers
import gln.texture.*
import gln.uniform.glUniform
import kool.*
import oglSamples.*
import oglSamples.framework.Framework
import oglSamples.framework.semantic
import oglSamples.tests.es200.es_200_fbo_shadow.Buffer.*
import org.lwjgl.opengl.*
import org.lwjgl.opengl.GL11.GL_FLOAT
import org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE
import org.lwjgl.opengl.GL20.glVertexAttribPointer
import java.nio.ByteBuffer
import gln.Attachment.Companion as Att

fun main() {
    es_200_fbo_shadow()()
}

private class es_200_fbo_shadow : Framework("es-200-fbo-shadow", ES, 2, 0, Vec2(0f, -glm.πf * 0.3f)) {

    val SHADER_SOURCE = arrayOf("es-200/fbo-shadow-render", "es-200/fbo-shadow-depth")

    val vertexCount = 8
    val vertexSize = vertexCount * glf.pos3_col4ub.stride
    val vertexData = Buffer(vertexSize).also {
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

    enum class Attachment { COLORBUFFER, DEPTHBUFFER }

    enum class Framebuffer { RENDER, DEPTH }

//    namespace shader
//    {
//        enum type
//                { VERT_RENDER,
//                  FRAG_RENDER,
//                  VERT_DEPTH,
//                  FRAG_DEPTH,
//                  MAX
//                };
//    }//namespace shader

    val shadowSize = Vec2i(64)

    val framebuffers = GlFramebuffers<Framebuffer>()
    val programs = Array(Framebuffer.values().size) { GlProgram.NULL }
    val buffers = GlBuffers<Buffer>()
    val textures = GlTextures<Attachment>()
    val renderbuffers = GlRenderbuffers<Attachment>()
    var uniformLightProj: UniformLocation = -1
    var uniformLightView: UniformLocation = -1
    var uniformLightWorld: UniformLocation = -1
    var uniformLightPointLightPosition: UniformLocation = -1
    var uniformLightClipNearFar: UniformLocation = -1

    var uniformRenderP: UniformLocation = -1
    var uniformRenderV: UniformLocation = -1
    var uniformRenderW: UniformLocation = -1
    var uniformRenderShadow: UniformLocation = -1
    var uniformRenderPointLightPosition: UniformLocation = -1
    var uniformRenderClipNearFar: UniformLocation = -1
    var uniformRenderBias: UniformLocation = -1

    override fun begin(): Boolean {

        var validated = initProgram()
        if (validated)
            validated = initBuffer()
        if (validated)
            validated = initTexture()
        if (validated)
            validated = initFramebuffer()

        gln.gl.depthTest = true
        gln.gl.depthFunc = CompareFunction.LESS

        buffers[VERTEX].bound(ARRAY) {
            // we cant use this because of the normalization on the second attribute
            // glf.pos3_col4ub.set()
            glVertexAttribPointer(semantic.attr.POSITION, Vec3.length, GL_FLOAT, false, Vec3.size + Vec4ub.size, 0)
            glVertexAttribPointer(semantic.attr.COLOR, Vec4ub.length, GL_UNSIGNED_BYTE, true, Vec3.size + Vec4ub.size, Vec3.size.L)
        }

        glf.pos3_col4ub.enable()

        buffers[ELEMENT] bind ELEMENT_ARRAY

        return validated && checkError("begin")
    }

    fun initProgram(): Boolean {

        var validated = try {
            programs[Framebuffer.DEPTH] = GlProgram.initFromPath(SHADER_SOURCE[Framebuffer.DEPTH]) {
                "Position".attrib = semantic.attr.POSITION
            }
            true
        } catch (_: Exception) {
            false
        }

        if (validated)
            programs[Framebuffer.DEPTH]{
                uniformLightProj = "LightProj".uniform
                uniformLightView = "LightView".uniform
                uniformLightWorld = "LightWorld".uniform
                uniformLightPointLightPosition = "PointLightPosition".uniform
                uniformLightClipNearFar = "ShadowClipNearFar".uniform
            }

        if (validated)
            try {
                programs[Framebuffer.RENDER] = GlProgram.initFromPath(SHADER_SOURCE[Framebuffer.RENDER]) {
                    "Position".attrib = semantic.attr.POSITION
                    "Color".attrib = semantic.attr.COLOR
                }
            } catch (_: Exception) {
                validated = false
            }

        if (validated)
            programs[Framebuffer.RENDER] {
                uniformRenderP = "P".uniform
                uniformRenderV = "V".uniform
                uniformRenderW = "W".uniform
                uniformRenderShadow = "Shadow".uniform
                uniformRenderPointLightPosition = "PointLightPosition".uniform
                uniformRenderClipNearFar = "ShadowClipNearFar".uniform
                uniformRenderBias = "Bias".uniform
            }

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

        renderbuffers.gen {
            Attachment.COLORBUFFER.bind { storage(gl.InternalFormat.D16, windowSize) }
            Attachment.DEPTHBUFFER.bind { storage(gl.InternalFormat.D16, shadowSize) }
        }
        textures.gen {
            activeTexture = 0
            Attachment.COLORBUFFER.bind(_2D) {
                image2D(InternalFormat.RGBA8, windowSize, gl.ExternalFormat.RGBA, gl.TypeFormat.U8)
            }
            Attachment.DEPTHBUFFER.bind(_2D) {
                wrapST = TexWrap.CLAMP_TO_EDGE
                filters(TexMinFilter.LINEAR, TexMagFilter.LINEAR)
                image2D(InternalFormat.RGBA32F, shadowSize, gl.ExternalFormat.RGBA, gl.TypeFormat.F32)
            }
        }
        return true
    }

    fun initFramebuffer(): Boolean {

        framebuffers.gen {

            Framebuffer.DEPTH.bind {
                texture(Att.COLOR0, textures[Framebuffer.DEPTH])
                renderbuffer(Att.DEPTH, renderbuffers[Framebuffer.DEPTH])
                if (!complete)
                    return false
            }
            Framebuffer.RENDER.bind {
                texture(Att.COLOR0, textures[Framebuffer.RENDER])
                renderbuffer(Att.DEPTH, renderbuffers[Framebuffer.RENDER])
                if (!complete)
                    return false
            }
        }
        return defaultFbo.bind { complete }
    }

    override fun render(): Boolean {

        run {
            val lightP = glm.perspective(glm.πf * 0.25f, 1f, 0.1f, 10f)
            val lightV = glm.lookAt(Vec3(0.5, 1, 2), Vec3(), Vec3(0, 0, 1))
            val lightW = Mat4()

            programs[Framebuffer.DEPTH].use {
                lightP to uniformLightProj
                lightV to uniformLightView
                lightW to uniformLightWorld
                glUniform(uniformLightPointLightPosition, 0f, 0f, 10f)
                glUniform(uniformLightClipNearFar, 0.01f, 10f)
            }
            renderShadow()
        }

        run {
            val renderP = glm.perspective(glm.πf * 0.25f, windowSize.aspect, 0.1f, 10f)
            val renderV = this.view
            val renderW = Mat4()

            programs[Framebuffer.RENDER].use {
                renderP to uniformRenderP
                renderV to uniformRenderV
                renderW to uniformRenderW
                0 to uniformRenderShadow
                glUniform(uniformRenderPointLightPosition, 0f, 0f, 10f)
                glUniform(uniformRenderClipNearFar, 0.01f, 10f)
                glUniform(uniformRenderBias, 0.002f)
            }
            renderFramebuffer()
        }

        return checkError("render")
    }

    fun renderShadow() {

        glViewport(shadowSize)

        framebuffers[Framebuffer.DEPTH].bind()
        glClearDepthBuffer(depth = 1f)

        glDrawElements(elements)

        checkError("renderShadow")
    }

    fun renderFramebuffer() {

        glViewport(windowSize)

        glBindFramebuffer()
        glClearDepthBuffer(depth = 1f)
        glClearColorBuffer(Vec4(0f, 0f, 0f, 1f))

        gln.gl.activeTexture = 0
        textures[Framebuffer.DEPTH] bind _2D

        glDrawElements(elements)

        checkError("renderFramebuffer")
    }

    override fun end(): Boolean {

        programs.forEach { it.delete() }

//        framebuffers.de TODO
//        glDeleteFramebuffers(framebuffer::MAX, & FramebufferName [0])
        buffers.delete()
        textures.delete()

        return checkError("end")
    }
}