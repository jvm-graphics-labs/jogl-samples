#version 420 core

#define FRAG_COLOR	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(binding = 0) uniform sampler2D Diffuse;

layout(origin_upper_left) in vec4 gl_FragCoord;

in block
{
	vec2 Texcoord;
} In;

layout(location = FRAG_COLOR, index = 0) out vec4 Color;

vec4 textureTrilinear(in sampler2D Sampler, in vec2 Texcoord);
vec4 textureBicubicLod(in sampler2D Sampler, in vec2 Texcoord, in int Lod);

void main()
{
	//Color = textureTrilinear(Diffuse, In.Texcoord);
	Color = textureBicubicLod(Diffuse, In.Texcoord, 0);
}
