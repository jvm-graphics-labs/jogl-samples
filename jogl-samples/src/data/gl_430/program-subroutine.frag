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

subroutine vec4 Diffuse();

layout(location = 0) subroutine uniform Diffuse diffuse;
layout(binding = SAMPLER_DXT1) uniform sampler2D diffuseDXT1;
layout(binding = SAMPLER_RGB8) uniform sampler2D diffuseRGB8;

in Block
{
    vec2 texCoord;
} inBlock;

layout(location = FRAG_COLOR, index = 0) out vec4 color;

layout(index = SUBROUTINE_DXT1) subroutine(Diffuse)
vec4 diffuseLQ()
{
	return texture(diffuseDXT1, inBlock.texCoord);
}

layout(index = SUBROUTINE_RGB8) subroutine(Diffuse)
vec4 diffuseHQ()
{
	return texture(diffuseRGB8, inBlock.texCoord);
}

void main()
{
    color = diffuse();
}
