package oglSamples.tests.gl300

import gli_.Texture2d
import gli_.gl
import gli_.gli
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import gln.BufferTarget.Companion.ARRAY
import gln.TextureTarget.Companion._2D
import gln.cap.Caps
import gln.draw.glDrawArrays
import gln.glViewport
import gln.glf.glf
import gln.objects.GlProgram
import gln.objects.GlTexture
import gln.texture.TexFilter
import gln.uniform.glUniform
import gln.vertexArray.glEnableVertexAttribArray
import gln.vertexArray.glVertexAttribPointer
import oglSamples.*
import oglSamples.framework.Framework
import oglSamples.framework.semantic
import org.lwjgl.opengl.GL11.GL_ALPHA_TEST
import org.lwjgl.opengl.GL11.glAlphaFunc
import org.lwjgl.opengl.GL11C.*
import org.lwjgl.opengl.GL30C.GL_MAX_VARYING_COMPONENTS
import org.lwjgl.opengl.GL41C.GL_MAX_VARYING_VECTORS

fun main() {
    gl_300_test_alpha()()
}

class gl_300_test_alpha : Framework("gl-300-test-alpha", Caps.Profile.COMPATIBILITY, 3, 0) {

    val SHADER_SOURCE = "gl-300/image-2d"
    val TEXTURE_DIFFUSE = "kueken7_rgba8_srgb.dds"

    class Vertex(val position: Vec2, val texCoord: Vec2) {
        companion object {
            val size = Vec2.size * 2
        }
    }

    // With DDS textures, v texture coordinate are reversed, from top to bottom
    val vertexCount = 6
    val vertexSize = vertexCount * Vertex.size
    val vertexData = vertex_v2v2_buffer_of(
            Vertex_v2v2(Vec2(-1f, -1f), Vec2(0f, 1f)),
            Vertex_v2v2(Vec2(+1f, -1f), Vec2(1f, 1f)),
            Vertex_v2v2(Vec2(+1f, +1f), Vec2(1f, 0f)),
            Vertex_v2v2(Vec2(+1f, +1f), Vec2(1f, 0f)),
            Vertex_v2v2(Vec2(-1f, +1f), Vec2(0f, 0f)),
            Vertex_v2v2(Vec2(-1f, -1f), Vec2(0f, 1f)))

    var vertexArray = GlVertexArray()
    var program = GlProgram.NULL
    var buffer = GlBuffer()
    var texture2D = GlTexture()
    var uniformMVP = -1
    var uniformDiffuse = -1

    override fun begin(): Boolean {

        var validated = true

        val maxVaryingOutputComp = glGetInteger(GL_MAX_VARYING_COMPONENTS)
        val maxVaryingOutputVec = glGetInteger(GL_MAX_VARYING_VECTORS)

        if (validated)
            validated = initTest()
        if (validated)
            validated = initProgram()
        if (validated)
            validated = initBuffer()
        if (validated)
            validated = initVertexArray()
        if (validated)
            validated = initTexture()

        return validated && checkError("begin")
    }

    fun initTest(): Boolean {
        glEnable(GL_ALPHA_TEST)
        glAlphaFunc(GL_GREATER, 0.2f)

        //To framework alpha blending:
        //glEnable(GL_BLEND);
        //glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        return checkError("initTest")
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
            uniformMVP = program getUniformLocation "MVP"
            uniformDiffuse = program getUniformLocation "Diffuse"
        }

        return validated && checkError("initProgram")
    }

    fun initBuffer(): Boolean {

        buffer = GlBuffer.gen().bound(ARRAY) {
            data(vertexData.data)
        }

        return checkError("initBuffer")
    }

    fun initTexture(): Boolean {

        texture2D = GlTexture.gen().bound(_2D,0) {

            minMagFilter = TexFilter.NEAREST

            gli.gl.profile = gl.Profile.GL32
            val dds = Texture2d(gli.loadDds(ClassLoader.getSystemResource(TEXTURE_DIFFUSE).toURI()))
            val format = gli.gl.translate(dds.format, dds.swizzles)
            for (level in 0 until dds.levels())
                image(level,
                        format.internal,
                        dds[level].extent(),
                        format.external, format.type,
                        dds[level].data()!!)
        }
        return checkError("initTexture")
    }

    fun initVertexArray(): Boolean {

        vertexArray = GlVertexArray.gen().bound {
            buffer.bound(ARRAY) {
                glVertexAttribPointer(glf.pos2_tc2)
            }
            glEnableVertexAttribArray(glf.pos2_tc2)
        }

        return checkError("initVertexArray")
    }

    override fun render(): Boolean {

        val projection = glm.perspective(glm.πf * 0.25f, windowSize.aspect, 0.1f, 100f)
        val model = Mat4(1f)
        val mvp = projection * view * model

        glViewport(windowSize)
        glClearColor(1f, 0.5f, 0f, 1f)
        glClear(GL_COLOR_BUFFER_BIT)

        program.use()
        glUniform(uniformDiffuse, 0)
        glUniform(uniformMVP, mvp)

        texture2D.bind(_2D,0)

        vertexArray.bind()

        glDrawArrays(vertexCount)

        return true
    }

    override fun end(): Boolean {

        buffer.delete()
        program.delete()
        texture2D.delete()
        vertexArray.delete()

        return true
    }
}