#version 330 core

#define FRAG_COLOR	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform sampler2DRect Diffuse;

in block
{
	vec2 Texcoord;
} In;

layout(location = FRAG_COLOR, index = 0) out vec4 Color;

void main()
{
	Color = texture(Diffuse, In.Texcoord);
}
