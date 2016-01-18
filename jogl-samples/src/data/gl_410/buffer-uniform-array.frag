#version 410 core
#define FRAG_COLOR	0
#define MATERIAL	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform Material
{
    vec4 diffuse[2];
} material;

in Block
{
    flat int instance;
} inBlock;

layout(location = FRAG_COLOR, index = 0) out vec4 color;

void main()
{
    color = material.diffuse[inBlock.instance];
}
