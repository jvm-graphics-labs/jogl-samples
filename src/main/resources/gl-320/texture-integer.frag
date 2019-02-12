#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform usampler2D Diffuse;

in block
{
	vec2 Texcoord;
} In;

out vec4 Color;

void main()
{
	uvec4 IntColor = texture(Diffuse, In.Texcoord);

	Color = vec4(IntColor) / 255.0;
}
