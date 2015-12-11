#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform Material
{
    vec4 diffuse;
} material;

out vec4 color;

void main()
{
    color = material.diffuse;
}

