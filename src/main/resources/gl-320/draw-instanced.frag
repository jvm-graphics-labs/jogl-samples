#version 150 core

precision highp float;
precision highp int;
layout(std140) uniform;

struct diffuse
{
	vec4 ColorA;
	vec4 ColorB;
};

uniform material
{
	diffuse Diffuse[2];
} Material;

in block
{
	flat int Instance;
} In;

out vec4 Color;

void main()
{
	Color = Material.Diffuse[In.Instance].ColorA + Material.Diffuse[In.Instance].ColorB;
}
