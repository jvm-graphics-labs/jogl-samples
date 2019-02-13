#version 400

#define FRAG_COLOR	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform samplerBuffer Diffuse;

in block
{
	flat int Instance;
} In;

layout(location = FRAG_COLOR, index = 0) out vec4 Color;

void main()
{
	Color = texelFetch(Diffuse, In.Instance);
}
