#version 400 core

#define FRAG_COLOR		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform sampler2D Diffuse;

in int gl_SampleID;
//in vec2 gl_SamplePosition;
//out int gl_SampleMask[];

uniform int gl_NumSamples;

in vert
{
	vec2 Texcoord;
} Vert;

layout(location = FRAG_COLOR, index = 0) out vec4 Color;

void main()
{
	Color = vec4(vec3(float(gl_SampleID + 1) / float(gl_NumSamples)), 1.0f);
}
