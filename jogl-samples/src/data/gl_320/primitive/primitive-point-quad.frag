#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

in Block
{
    vec4 color;
} inBlock;

out vec4 color;

void main()
{
    color = inBlock.color;
}

