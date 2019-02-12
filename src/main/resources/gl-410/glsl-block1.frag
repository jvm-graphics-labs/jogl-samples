#version 410 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform sampler2D Diffuse;

vec4 color(vec4 Color);

in block
{
	vec4 Color;
	vec2 Texcoord;
	flat int Instance;
} In;

out vec4 Color;

void main()
{
	Color = texture(Diffuse, In.Texcoord) * 0.75 + color(In.Color) * 0.25;
}
