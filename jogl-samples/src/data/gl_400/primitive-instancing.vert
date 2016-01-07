#version 400 core

#define POSITION		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(location = POSITION) in vec3 position;

out Block
{
    vec3 color;
} outBlock;

void main()
{	
    gl_Position = vec4(position, 1.0);
    outBlock.color = vec3(1.0, 0.5, 0.0);
}

