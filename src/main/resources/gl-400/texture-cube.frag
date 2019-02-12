#version 400 core

#define FRAG_COLOR	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform samplerCube Environment;

in block
{
	vec3 Reflect;
} In;

layout(location = FRAG_COLOR, index = 0) out vec4 Color;

void main()
{
	Color = texture(Environment, vec3(In.Reflect));
}
