#version 420 core

#include primitive-bindless-nv.glsl
#line 5

#define COUNT 24

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(binding = TRANSFORM0) uniform Transform
{
    mat4 mvp;
} transform;

layout(location = POSITION) in vec3 position;
layout(location = TEXCOORD) in vec2 texCoord;

out gl_PerVertex
{
    vec4 gl_Position;
};

out Block
{
    vec2 texCoord;
} outBlock;

void main()
{
    outBlock.texCoord = texCoord;
    gl_Position = transform.mvp * vec4(position, 1.0);
}
