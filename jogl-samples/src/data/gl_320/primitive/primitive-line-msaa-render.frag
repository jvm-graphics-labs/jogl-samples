#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

in Block
{
    vec2 texCoord;
} inBlock;

out vec4 color;

void main()
{
    color = vec4(1.0, 0.5, 0.0, 1.0);
}
