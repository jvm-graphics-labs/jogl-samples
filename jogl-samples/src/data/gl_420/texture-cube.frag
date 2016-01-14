#version 420 core

#define FRAG_COLOR	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(binding = 0) uniform samplerCubeArray environment;

in Block
{
    vec3 reflection;
} inBlock;

layout(location = FRAG_COLOR, index = 0) out vec4 color;

void main()
{
    color = texture(environment, vec4(inBlock.reflection, 0.0));
}
