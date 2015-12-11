#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform Transform
{
    mat4 mvp;
} transform;

in vec2 position;
in vec4 color;

out Block
{
    vec4 color;
} outBlock;

void main()
{	
    gl_Position = transform.mvp * vec4(position, 0.0, 1.0);
    outBlock.color = color;
}

