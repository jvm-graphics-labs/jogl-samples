#version 400 core

#define POSITION		0
#define COLOR			3
#define TEXCOORD		4

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform mat4 MVP;

layout(location = POSITION) in vec2 Position;
layout(location = COLOR) in vec4 Color;

out block
{
	vec4 Color;
} Out;

void main()
{	
	gl_Position = MVP * vec4(Position, 0.0, 1.0);
	Out.Color = Color;
}
