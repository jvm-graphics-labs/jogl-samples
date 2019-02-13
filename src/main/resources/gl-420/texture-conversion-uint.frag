#version 420 core

#define FRAG_COLOR	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(binding = 0) uniform usampler2D Diffuse;

in block
{
	vec2 Texcoord;
} In;

layout(location = FRAG_COLOR, index = 0) out vec4 Color;

void main()
{
	vec2 Size = textureSize(Diffuse, 0) - 1;

	ivec2 Coord = ivec2(In.Texcoord * Size);
	uvec4 Texel = texelFetch(Diffuse, Coord + ivec2(0, 0), 0);

	Color = vec4(Texel) / 255.f;
}
