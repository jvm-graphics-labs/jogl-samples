package oglSamples.framework

import gli_.Format
import gli_.Texture
import gli_.Texture2d
import glm_.glm
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec3.Vec3ub
import oglSamples.allLessThanEqual
import oglSamples.clamp
import oglSamples.mix


var DEBUG = true
var AUTOMATED_TESTS = false

enum class Heuristic(val i: Int) {
    EQUAL_BIT(1 shl 0),
    ABSOLUTE_DIFFERENCE_MAX_ONE_BIT(1 shl 1),
    ABSOLUTE_DIFFERENCE_MAX_ONE_KERNEL_BIT(1 shl 2),
    ABSOLUTE_DIFFERENCE_MAX_ONE_LARGE_KERNEL_BIT(1 shl 3),
    MIPMAPS_ABSOLUTE_DIFFERENCE_MAX_ONE_BIT(1 shl 4),
    MIPMAPS_ABSOLUTE_DIFFERENCE_MAX_FOUR_BIT(1 shl 5),
    MIPMAPS_ABSOLUTE_DIFFERENCE_MAX_CHANNEL_BIT(1 shl 6),
    ALL(EQUAL_BIT.i or ABSOLUTE_DIFFERENCE_MAX_ONE_BIT.i or ABSOLUTE_DIFFERENCE_MAX_ONE_KERNEL_BIT.i or
            ABSOLUTE_DIFFERENCE_MAX_ONE_LARGE_KERNEL_BIT.i or MIPMAPS_ABSOLUTE_DIFFERENCE_MAX_ONE_BIT.i or
            MIPMAPS_ABSOLUTE_DIFFERENCE_MAX_FOUR_BIT.i);

    fun absoluteDifference(a: Texture, b: Texture, scale: Int): Texture {

        assert(a.format == Format.RGB8_UNORM_PACK8 && b.format == Format.RGB8_UNORM_PACK8)

        val result = Texture(a.target, a.format, a.extent(), a.layers(), a.faces(), a.levels())
        val texelCount = a.size<Vec3ub>()
        for (texelIndex in 0 until texelCount) {
            val texelA = a.data<Vec3ub>()[texelIndex]
            val texelB = b.data<Vec3ub>()[texelIndex]
            val texelResult = glm.mix(texelA - texelB, texelB - texelA, texelB greaterThan texelA) * scale
            result.data<Vec3ub>()[texelIndex] = texelResult
        }
        return result
    }

    fun test(a: Texture, b: Texture): Boolean {

        return when (this) {
            EQUAL_BIT -> a == b
            ABSOLUTE_DIFFERENCE_MAX_ONE_LARGE_KERNEL_BIT -> {

                fun kernel(texelCoordA: Vec2i, texelA: Vec3ub, textureB: Texture2d): Boolean {

                    val kernelSize = 9

                    val texelCoordInvA = Vec2i(texelCoordA.x, 480 - texelCoordA.y)
                    val texelB = Array(kernelSize * kernelSize) { Vec3ub() }

                    for (kernelIndexY in 0 until kernelSize)
                        for (kernelIndexX in 0 until kernelSize) {

                            val kernelCoordB = Vec2i(kernelIndexX - kernelSize / 2, kernelIndexY - kernelSize / 2)
                            val texelCoordB = texelCoordA + kernelCoordB

                            val clampedTexelCoord = glm.clamp(texelCoordB, Vec2i(0), Vec2i(textureB.extent()) - 1)
                            texelB[kernelIndexY * kernelSize + kernelIndexX] = textureB.load(clampedTexelCoord, 0)
                        }

                    for (kernelIndex in 0 until kernelSize * kernelSize) {
                        val texelDiff = glm.abs(Vec3(texelB[kernelIndex]) - Vec3(texelA))
                        if (texelDiff allLessThanEqual 2f)
                            return true
                        continue
                    }

                    return false
                }

                val textureA = Texture2d(a)
                val textureB = Texture2d(b)

                val texelIndex = Vec2i()
                val texelCount = a.extent()
                var res = true
                while (texelIndex.y < texelCount.y && res)
                    while (texelIndex.x < texelCount.x && res) {
                        val texelCoordA = texelIndex.incAssign()
                        val texelA = textureA.load<Vec3ub>(texelCoordA, 0)
                        val texelB = textureB.load<Vec3ub>(texelCoordA, 0)
                        if (texelA == texelB)
                            continue

                        val validTexel = kernel(texelCoordA, texelA, textureB)
                        if (!validTexel)
                            res = false
                    }
                res
            }
            ABSOLUTE_DIFFERENCE_MAX_ONE_KERNEL_BIT -> {

                val texture = Texture2d(absoluteDifference(a, b, 1))
                val absDiffMax = Vec3ub(0)
                val texelCount = texture.extent()
                for (texelIndexY in 0 until texelCount.y)
                    for (texelIndexX in 0 until texelCount.x) {

                        val texelCoord = Vec2i(texelIndexX, texelIndexY)
                        var texelDiff = texture.load<Vec3ub>(texelCoord, 0)

                        if (texelDiff allLessThanEqual 1)
                            continue

                        val textureA = Texture2d(a)
                        val textureB = Texture2d(b)
                        val texelA = textureA.load<Vec3ub>(texelCoord, 0)

                        var kernelAbsDiffMax = false
                        for (kernelIndexY in -1..1)
                            for (kernelIndexX in -1..1) {
                                val kernelCoord = Vec2i(kernelIndexX, kernelIndexY)
                                val clampedTexelCoord = glm.clamp(texelCoord + kernelCoord, Vec2i(0), Vec2i(texture.extent()) - 1)
                                val texelB = textureB.load<Vec3ub>(clampedTexelCoord, 0)

                                val diff = glm.abs(Vec3(texelB) - Vec3(texelA))
                                if (diff allLessThanEqual 1f)
                                    kernelAbsDiffMax = true
                            }

//                        if (kernelAbsDiffMax)
//                            texelDiff = glm.min(texelDiff, 1)
//                        absDiffMax = glm::max(TexelDiff, AbsDiffMax)
                    }

                return true //glm::all(glm::lessThanEqual(AbsDiffMax, glm::u8vec3(1)))
            }
            else -> false
        }
    }

    infix fun has(h: Heuristic) = i.and(h.i) != 0
}

enum class Vendor { DEFAULT, AMD, INTEL, NVIDIA }

//    int operator ()();
//    void log(csv & CSV, char const * String);
//    void setupView(bool Translate, bool RotateX, bool RotateY);

enum class ViewSetupFlag {
    TRANSLATE, ROTATE_X, ROTATE_Y;

    val i = 1 shl ordinal
}

enum class Success { RUN_ONLY, GENERATE_ERROR, MATCH_TEMPLATE }

enum class Exit { SUCCESS, FAILURE }

typealias GLint = Int
typealias GLuint = UInt