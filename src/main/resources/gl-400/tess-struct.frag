#version 400 core

#define FRAG_COLOR		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

struct vertex
{
	vec4 Color;
};

in vertex Geom;
layout(location = FRAG_COLOR, index = 0) out vec4 Color;

void main()
{
	Color = Geom.Color;
}
