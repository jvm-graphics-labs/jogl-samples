#version 400 core

#define POSITION		0
#define COLOR			3
#define TEXCOORD		4

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform mat4 MVP;

struct vertex
{
	vec4 Color;
};

layout(location = POSITION) in vec2 Position;
layout(location = COLOR) in vec4 Color;

out vertex Vert;

void main()
{	
	gl_Position = MVP * vec4(Position, 0.0, 1.0);
	Vert.Color = Color;
}
