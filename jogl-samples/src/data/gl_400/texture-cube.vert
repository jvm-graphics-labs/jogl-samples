#version 400 core

#define POSITION	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform Transform
{
    mat4 mvp;
    mat4 mv;
    vec3 camera;
} transform;

const vec3 constView = vec3(0, 0,-1);
const vec3 constNormal = vec3(0, 0, 1);

layout(location = POSITION) in vec2 position;

out gl_PerVertex
{
    vec4 gl_Position;
};

out Block
{
    vec3 refl;
} outBlock;

void main()
{	
    mat3 mv3x3 = mat3(transform.mv);

    gl_Position = transform.mvp * vec4(position, 0.0, 1.0);
    vec3 p = mv3x3 * vec3(position, 0.0);
    vec3 n = mv3x3 * constNormal;
    vec3 e = normalize(p - transform.camera);

    outBlock.refl = reflect(e, n);
}
