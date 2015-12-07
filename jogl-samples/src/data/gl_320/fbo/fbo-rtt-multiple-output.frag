#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

out vec4 red;
out vec4 green;
out vec4 blue;

void main()
{
    red = vec4(1.0, 0.0, 0.0, 1.0);
    green = vec4(0.0, 1.0, 0.0, 1.0);
    blue = vec4(0.0, 0.0, 1.0, 1.0);
}
