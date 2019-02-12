#version 420 core

#define POSITION	0
#define COLOR		3
#define TEXCOORD	4
#define FRAG_COLOR	0
#define TRANSFORM0	1
#define DIFFUSE		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(binding = DIFFUSE) uniform sampler2D Diffuse;

in block
{
	vec2 Texcoord;
} In;

layout(location = FRAG_COLOR, index = 0) out vec4 Color;

void main()
{
#	ifdef FLAT_COLOR
		Color = vec4(0.0, 0.5, 1.0, 1.0);
#	else
		Color = texture(Diffuse, In.Texcoord.st);
#endif//
}
