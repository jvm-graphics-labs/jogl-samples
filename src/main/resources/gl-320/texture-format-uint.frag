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
	vec2 Size = textureSize(Diffuse, 0) - 1;

	ivec2 Coord = ivec2(In.Texcoord * Size);
	uvec4 Texel = texelFetch(Diffuse, Coord + ivec2(0, 0), 0);

	Color = vec4(Texel) / 255.f;
}
