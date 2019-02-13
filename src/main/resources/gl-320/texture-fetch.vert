#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform transform
{
	mat4 MVP;
} Transform;

in vec2 Position;
in vec2 Texcoord;

out block
{
	vec2 Texcoord;
} Out;

void main()
{	
	Out.Texcoord = Texcoord;
	gl_Position = Transform.MVP * vec4(Position, 0.0, 1.0);
}