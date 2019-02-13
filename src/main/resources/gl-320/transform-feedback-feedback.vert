#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

in vec4 Position;
in vec4 Color;

out block
{
	vec4 Color;
} Out;

void main()
{	
	gl_Position = Position;
	Out.Color = Color;
}

