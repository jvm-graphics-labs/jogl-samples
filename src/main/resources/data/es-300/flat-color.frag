
#version 300 es

#include semantic.glsl


precision mediump float;


uniform highp vec4 Diffuse;

layout(location = FRAG_COLOR) out vec4 FragColor;

void main()
{
	FragColor = Diffuse;
}
