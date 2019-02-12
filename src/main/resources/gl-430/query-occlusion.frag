#version 420 core
#define FRAG_COLOR	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(location = FRAG_COLOR, index = 0) out vec4 Color;

void main()
{
	Color = vec4(1.0, 0.5, 0.0, 1.0);
}

