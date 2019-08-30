package oglSamples

import glm_.glm
import glm_.vec2.Vec2i
import glm_.vec3.Vec3bool
import glm_.vec3.Vec3ub

fun glm.clamp(a: Vec2i, min: Vec2i, max: Vec2i) = clamp(a, min, max, Vec2i())
fun glm.clamp(a: Vec2i, min: Vec2i, max: Vec2i, res: Vec2i): Vec2i {
    res.x = clamp(a.x, min.x, max.x)
    res.y = clamp(a.y, min.y, max.y)
    return res
}

fun glm.mix(a: Vec3ub, b: Vec3ub, interp: Vec3bool) = mix(a, b, interp, Vec3ub())
fun glm.mix(a: Vec3ub, b: Vec3ub, interp: Vec3bool, res: Vec3ub): Vec3ub {
    res.x = if(interp.x) b.x else a.x
    res.y = if(interp.y) b.y else a.y
    res.z = if(interp.z) b.z else a.z
    return res
}

infix fun Vec3ub.allLessThanEqual(a: Int): Boolean = x <= a && y <= a && z <= a