#version 420 core

#define FRAG_COLOR		0
#define DIFFUSE			0
#define MATERIAL		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(binding = DIFFUSE, r32ui) coherent uniform uimage2D ImageData;

layout(binding = MATERIAL) uniform material
{
	uvec2 ImageSize;
} Material;

in block
{
	vec2 Texcoord;
} In;

layout(location = FRAG_COLOR, index = 0) out vec4 Color;

void main()
{
	uint Fetch = imageLoad(ImageData, ivec2(In.Texcoord * Material.ImageSize)).x;
	Color = unpackUnorm4x8(Fetch);
}
