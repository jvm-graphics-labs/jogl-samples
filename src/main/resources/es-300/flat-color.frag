#version 300 es
#define FRAG_COLOR	0

precision highp float;
precision highp int;

uniform highp vec4 Diffuse;

layout(location = FRAG_COLOR) out vec4 FragColor;

void main()
{
	FragColor = Diffuse;
}
