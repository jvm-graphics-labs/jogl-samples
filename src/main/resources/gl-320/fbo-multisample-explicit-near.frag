#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform sampler2DMS Diffuse;

in block
{
	vec2 Texcoord;
} In;

out vec4 Color;

void main()
{
	// integer UV coordinates, needed for fetching multisampled texture
	ivec2 Texcoord = ivec2(textureSize(Diffuse) * In.Texcoord);

	Color = texelFetch(Diffuse, Texcoord, 0);
}
