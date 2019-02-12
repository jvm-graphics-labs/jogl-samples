#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform usampler2D Diffuse;

in block
{
	vec2 Texcoord;
} In;

out uvec4 Color;

void main()
{
	Color = texture(Diffuse, In.Texcoord);
}
