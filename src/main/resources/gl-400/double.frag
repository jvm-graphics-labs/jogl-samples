#version 400 core

#define FRAG_COLOR		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform dvec4 Diffuse;

layout(location = FRAG_COLOR, index = 0) out vec4 FragColor;

void main()
{
	FragColor = vec4(Diffuse);
}

