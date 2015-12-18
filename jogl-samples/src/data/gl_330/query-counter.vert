#version 330 core
#define POSITION	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform mat4 mvp;

layout(location = POSITION) in vec4 position;

void main()
{
    gl_Position = mvp * position;
}

