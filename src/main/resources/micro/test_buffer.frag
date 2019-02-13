#version 330 core
#extension GL_ARB_shading_language_420pack : require

#define FRAG_COLOR		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(location = FRAG_COLOR, index = 0) out vec4 Color;

in block
{
	vec4 Color;
} In;

void main()
{
	Color = (vec4(1.0f, 0.5f, 0.0f, 1.0f) + In.Color) * 0.5;
}
