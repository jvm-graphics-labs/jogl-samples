#version 150 core

layout(std140) uniform;

uniform material
{
	vec4 Diffuse;
} Material;

out vec4 Color;

void main()
{
	Color = Material.Diffuse;
}

