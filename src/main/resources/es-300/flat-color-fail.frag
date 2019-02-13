#version 300 es
#define FRAG_COLOR	0

uniform highp vec4 Diffuse;

void main ()
{
	gl_FragColor = Diffuse;
}

