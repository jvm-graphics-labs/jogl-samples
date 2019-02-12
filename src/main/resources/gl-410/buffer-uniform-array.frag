#version 410 core
#define FRAG_COLOR	0
#define MATERIAL	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform material
{
	vec4 Diffuse[2];
} Material;

in block
{
	flat int Instance;
} In;

layout(location = FRAG_COLOR, index = 0) out vec4 Color;

void main()
{
	Color = Material.Diffuse[In.Instance];
}
