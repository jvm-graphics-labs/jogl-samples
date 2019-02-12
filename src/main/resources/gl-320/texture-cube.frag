#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform samplerCube Environment;

in block
{
	vec3 Reflect;
} In;

out vec4 Color;

void main()
{
	Color = texture(Environment, In.Reflect);
}
