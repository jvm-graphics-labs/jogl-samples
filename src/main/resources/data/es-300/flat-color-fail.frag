
#version 300 es

#include semantic.glsl


uniform highp vec4 Diffuse;

void main ()
{
	gl_FragColor = Diffuse;
}

