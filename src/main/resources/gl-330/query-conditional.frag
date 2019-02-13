#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform material
{
	vec4 Diffuse;
} Material;

out vec4 Color;

void main()
{
	Color = Material.Diffuse;
}

