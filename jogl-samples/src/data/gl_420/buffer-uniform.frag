#version 420 core

#define FRAG_COLOR	0
#define MATERIAL	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(binding = MATERIAL) uniform Material
{
    vec4 diffuse;
} material;

layout(location = FRAG_COLOR, index = 0) out vec4 fragColor;

void main()
{
    fragColor = material.diffuse;
}
