#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform sampler2D Diffuse;

in block
{
	vec2 Texcoord;
} In;

out vec4 Color;

void main()
{
	if(length(In.Texcoord - 0.5) > 0.5)
		discard;

	Color = texture(Diffuse, In.Texcoord);
}
