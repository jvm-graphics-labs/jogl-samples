#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform mat4 mv;
uniform mat4 mvp;
uniform vec3 camera;

const vec3 constView = vec3(0, 0,-1);
const vec3 constNormal = vec3(0, 0, 1);

in vec2 position;

out Block
{
    vec3 refl;
} outBlock;

void main()
{	
    mat3 mv3x3 = mat3(mv);

    gl_Position = mvp * vec4(position, 0.0, 1.0);
    vec3 p = mv3x3 * vec3(position, 0.0);
    vec3 n = mv3x3 * constNormal;
    vec3 e = normalize(p - camera);

    outBlock.refl = reflect(e, n);
}
