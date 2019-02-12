#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

in block
{
	vec4 Color;
} In;

out vec4 Color;

void main()
{
	Color = In.Color;
}

