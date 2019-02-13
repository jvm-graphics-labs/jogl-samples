#version 460 core

#define FRAG_COLOR		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

in block
{
	vec2 Texcoord;
	flat int DrawID;
} In;

layout(location = FRAG_COLOR, index = 0) out vec4 Color;

layout(binding = 0) uniform sampler2D Diffuse[3];

void main()
{
	Color = texture(Diffuse[In.DrawID], In.Texcoord.st);
}

