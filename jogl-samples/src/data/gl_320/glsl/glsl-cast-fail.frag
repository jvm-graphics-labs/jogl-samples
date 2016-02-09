#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

#define COUNT 4

uniform sampler2D diffuse;

in Block
{
    vec2 texCoord;
    vec4 lumimance[COUNT];
} inBlock;

out vec4 color;

void main()
{
    highp uint first = uint(0);
    vec4 luminance = vec4(0.0);

    for(uint i = first; i < uint(COUNT); ++i)
        luminance += inBlock.lumimance[i];

    color = texture(diffuse, inBlock.texCoord) * luminance;
}
