#version 400 core

#define FRAG_COLOR		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform vec4 Diffuse;

in vec3 GeomColor;

in block
{
	vec3 Color;
} In;

layout(location = FRAG_COLOR, index = 0) out vec4 Color;

void main()
{
	Color = vec4(In.Color, 1.0) * Diffuse;
}

