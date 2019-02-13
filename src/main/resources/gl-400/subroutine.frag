#version 400 core

#define FRAG_COLOR		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

subroutine vec4 diffuse();

subroutine uniform diffuse Diffuse;
uniform sampler2D DiffuseDXT1;
uniform sampler2D DiffuseRGB8;

in block
{
	vec2 Texcoord;
} In;

layout(location = FRAG_COLOR, index = 0) out vec4 Color;

subroutine(diffuse)
vec4 diffuseLQ()
{
	return texture(DiffuseDXT1, In.Texcoord);
}

subroutine(diffuse)
vec4 diffuseHQ()
{
	return texture(DiffuseRGB8, In.Texcoord);
}

void main()
{
	Color = Diffuse();
}
