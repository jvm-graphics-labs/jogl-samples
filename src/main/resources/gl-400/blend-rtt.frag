#version 400 core

#define FRAG_COLOR		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(location = FRAG_COLOR) out vec4 Array[4];

void main()
{
	Array[0] = vec4(0.5, 0.5, 0.5, 0.5);
	Array[1] = vec4(0.5, 0.2, 0.2, 0.5);
	Array[2] = vec4(0.2, 0.5, 0.2, 0.5);
	Array[3] = vec4(0.2, 0.2, 0.5, 0.5);
}
