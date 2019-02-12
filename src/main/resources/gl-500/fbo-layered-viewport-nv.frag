#version 420 core

#define FRAG_COLOR	0
#define DIFFUSE		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(binding = DIFFUSE) uniform sampler2DArray Diffuse;

in block
{
	vec2 Texcoord;
} In;

layout(location = FRAG_COLOR, index = 0) out vec4 Color;
in int gl_ViewportIndex;

void main()
{
	Color = texture(Diffuse, vec3(In.Texcoord, gl_ViewportIndex));
}
