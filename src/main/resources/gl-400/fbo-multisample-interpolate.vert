#version 400 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform mat4 MVP;

in vec2 Position;
in vec2 Texcoord;

out block
{
	vec2 Texcoord;
} Vert;

void main()
{
	Vert.Texcoord = Texcoord;
	gl_Position = MVP * vec4(Position, 0.0, 1.0);
}
