#version 420 core

#define POSITION	0
#define COLOR		3
#define TEXCOORD	4
#define REFLECT		6
#define FRAG_COLOR	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(binding = 0) uniform samplerCubeArray Environment;

in block
{
	vec3 Reflect;
} In;

layout(location = FRAG_COLOR, index = 0) out vec4 Color;

void main()
{
	Color = texture(Environment, vec4(In.Reflect, 0.0));
}
