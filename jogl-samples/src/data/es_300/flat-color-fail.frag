#version 300 es
#define FRAG_COLOR	0

uniform highp vec4 diffuse;

void main ()
{
    gl_FragColor = diffuse;
}