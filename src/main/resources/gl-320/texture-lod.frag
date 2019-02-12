#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform sampler2DArray Diffuse;

in block
{
	vec2 Texcoord;
} In;

out vec4 Color;

void main()
{
	Color = texture(Diffuse, vec3(In.Texcoord, 0.0));
}
