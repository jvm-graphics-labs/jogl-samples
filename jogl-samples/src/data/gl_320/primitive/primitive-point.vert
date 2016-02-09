#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform mat4 mvp;
uniform mat4 mv;

in vec4 position;
in vec4 color;

out Block
{
    vec4 color;
} outBlock;

void main()
{
    outBlock.color = color;
    gl_Position = mvp * position;
    gl_PointSize = 4.f / -(mv * position).z;
}
