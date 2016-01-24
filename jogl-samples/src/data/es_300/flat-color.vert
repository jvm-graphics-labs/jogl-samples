#version 300 es

uniform mat4 mvp;

in highp vec4 position;

void main()
{	
    gl_Position = mvp * position;
}