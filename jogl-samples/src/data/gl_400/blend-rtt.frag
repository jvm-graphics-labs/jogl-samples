#version 400 core

#define FRAG_COLOR		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(location = FRAG_COLOR) out vec4 array[4];

void main()
{
    array[0] = vec4(0.5, 0.5, 0.5, 0.5);
    array[1] = vec4(0.5, 0.2, 0.2, 0.5);
    array[2] = vec4(0.2, 0.5, 0.2, 0.5);
    array[3] = vec4(0.2, 0.2, 0.5, 0.5);
}
