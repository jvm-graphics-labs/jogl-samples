#version 300 es

uniform highp vec4 diffuse;

void main()
{
    gl_FragColor = diffuse;
}