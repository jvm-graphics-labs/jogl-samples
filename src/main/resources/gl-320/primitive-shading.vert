#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform transform
{
	mat4 MVP;
} Transform;

in vec2 Position;
in vec4 Color;

out block
{
	vec4 Color;
} Out;

void main()
{	
	gl_Position = Transform.MVP * vec4(Position, 0.0, 1.0);
	Out.Color = Color;
}

