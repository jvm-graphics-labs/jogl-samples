
#version 300 es

#include semantic.glsl


uniform mat4 MVP;

layout (location = POSITION) in highp vec4 Position;

void main()
{	
	gl_Position = MVP * Position;
}

