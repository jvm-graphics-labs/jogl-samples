#version 420 core

#define FRAG_COLOR		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

struct vertex
{
	vec4 Color;
};

layout(location = 0) in vertex st_In;

in block
{
	vec4 Color;
} bl_In; 

layout(location = FRAG_COLOR, index = 0) out vec4 Color;

void main()
{
	Color = st_In.Color + bl_In.Color;
}
