#version 430 core

#define DIFFUSE			0
#define FRAG_COLOR		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;
//layout(std430, column_major) buffer; AMD bug

layout(binding = DIFFUSE) uniform sampler2DArray Diffuse;

in block
{
	vec2 Texcoord;
} In;

layout(location = FRAG_COLOR, index = 0) out vec4 Color;

void main()
{
	Color = texture(Diffuse, vec3(In.Texcoord.st, 0.0));
}
