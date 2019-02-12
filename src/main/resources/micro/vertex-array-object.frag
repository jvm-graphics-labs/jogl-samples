#version 420 core
#define FRAG_COLOR		0

layout(location = FRAG_COLOR, index = 0) out vec4 Color;
uniform vec4 Diffuse;

void main()
{
	Color = Diffuse;
}
