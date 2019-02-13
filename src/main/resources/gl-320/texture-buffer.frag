#version 150

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform samplerBuffer Diffuse;

in block
{
	flat int Instance;
} In;

out vec4 Color;

void main()
{
	Color = texelFetch(Diffuse, In.Instance);
}
