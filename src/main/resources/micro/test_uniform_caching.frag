#version 330 core

#define FRAG_COLOR		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform vec4 Diffuse;

in block
{
	vec4 Color;
} In;

layout(location = FRAG_COLOR, index = 0) out vec4 Color;

void main()
{
	Color = (In.Color + Diffuse) * 0.5;
}
