#version 420 core

#define FRAG_COLOR	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(binding = 0) uniform sampler2D Diffuse;

in block
{
	vec4 Position;
	vec2 Texcoord;
} In;

layout(location = FRAG_COLOR, index = 0) out vec4 Color;

void main()
{
	if(In.Position.z > 16)
		discard;

	Color = texture(Diffuse, In.Texcoord.st);
}
