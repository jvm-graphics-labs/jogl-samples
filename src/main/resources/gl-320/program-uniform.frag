#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform vec4 Diffuse[2];

out vec4 Color;

void main()
{
	Color = Diffuse[0] * Diffuse[1];
}
