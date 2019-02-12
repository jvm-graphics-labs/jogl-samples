#version 400 core

#define POSITION		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(location = POSITION) in vec3 Position;

out block
{
	vec3 Color;
} Out;

void main()
{	
	gl_Position = vec4(Position, 1.0);
	Out.Color = vec3(1.0, 0.5, 0.0);
}

