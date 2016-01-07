#version 400 core

#define FRAG_COLOR		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

in Block
{
    vec4 color;
} inBlock;

layout(location = FRAG_COLOR, index = 0) out vec4 fragColor;

void main()
{
    fragColor = inBlock.color;
}
