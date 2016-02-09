#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

in vec4 position;
in vec4 color;

out Block
{
    vec4 color;
} outBlock;

void main()
{	
    gl_Position = position;
    outBlock.color = color;
}

