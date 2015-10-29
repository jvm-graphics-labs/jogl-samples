#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform usampler2D diffuse;

in Block
{
    vec2 texCoord;
} inBlock;

out uvec4 color;

void main()
{
    color = texture(diffuse, inBlock.texCoord);
}
