#version 300 es

uniform mat4 MVP;

in highp vec4 Position;

void main()
{	
	gl_Position = MVP * Position;
}

