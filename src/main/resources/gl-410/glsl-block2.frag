#version 410 core

in block
{
	vec4 Color;
	vec2 Texcoord;
	flat int Instance;
} In;

vec4 color(vec4 Color)
{
	return (Color + In.Color) * 0.5;
}
