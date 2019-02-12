#version 420 core
#extension GL_ARB_explicit_uniform_location : require

#define POSITION		0
#define COLOR			3
#define TEXCOORD		4
#define FRAG_COLOR		0

#define SAMPLER_DXT1	0
#define SAMPLER_RGB8	1

#define SUBROUTINE_DXT1	0
#define SUBROUTINE_RGB8	1

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

subroutine vec4 diffuse();

layout(location = 0) subroutine uniform diffuse Diffuse;
layout(binding = SAMPLER_DXT1) uniform sampler2D DiffuseDXT1;
layout(binding = SAMPLER_RGB8) uniform sampler2D DiffuseRGB8;

in block
{
	vec2 Texcoord;
} In;

layout(location = FRAG_COLOR, index = 0) out vec4 Color;

layout(index = SUBROUTINE_DXT1) subroutine(diffuse)
vec4 diffuseLQ()
{
	return texture(DiffuseDXT1, In.Texcoord);
}

layout(index = SUBROUTINE_RGB8) subroutine(diffuse)
vec4 diffuseHQ()
{
	return texture(DiffuseRGB8, In.Texcoord);
}

void main()
{
	Color = Diffuse();
}
