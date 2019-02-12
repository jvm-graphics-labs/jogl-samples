#version 400 core

#define FRAG_COLOR	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform sampler2DArray Diffuse[2];
uniform uint DiffuseIndex;

in block
{
	vec2 Texcoord;
	float Instance;
} In;

layout(location = FRAG_COLOR, index = 0) out vec4 Color;

vec4 sampling(in sampler2DArray Sampler[2], in int Layer, in vec2 Texcoord)
{
	return texture(Sampler[DiffuseIndex], vec3(Texcoord, Layer));
}

void main()
{
	Color = sampling(Diffuse, 0, In.Texcoord);
}
