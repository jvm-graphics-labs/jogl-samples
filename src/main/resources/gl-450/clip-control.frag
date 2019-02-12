#version 430 core

#define FRAG_COLOR	0
#define DIFFUSE		0

layout(binding = DIFFUSE) uniform sampler2DArray Diffuse;

in block
{
	vec2 Texcoord;
} In;

layout(location = FRAG_COLOR, index = 0) out vec4 Color;

void main()
{
	Color = texture(Diffuse, vec3(In.Texcoord.st, 0.0));
}
