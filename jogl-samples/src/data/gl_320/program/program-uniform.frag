#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform vec4 diffuse[2];

out vec4 color;

void main()
{
    color = diffuse[0] * diffuse[1];
}
