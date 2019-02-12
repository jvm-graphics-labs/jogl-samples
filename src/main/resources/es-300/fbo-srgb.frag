#version 300 es
#define FRAG_COLOR	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

in block
{
	flat vec4 Color;
} In;

layout(location = FRAG_COLOR) out vec4 Color;

void main()
{
	Color = In.Color;
}
