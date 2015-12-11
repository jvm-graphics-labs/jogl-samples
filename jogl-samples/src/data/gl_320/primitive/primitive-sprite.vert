#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform mat4 mvp;
uniform mat4 mv;

in vec2 position;
in vec4 color;

out Block
{
    vec4 color;
} outBlock;

void main()
{
    outBlock.color = color;
    gl_Position = mvp * vec4(position, 0.0, 1.0);
    gl_PointSize = 256.0 / -(mv * vec4(position, 0.0, 1.0)).z;
}
