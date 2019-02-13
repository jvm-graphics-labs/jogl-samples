#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform mat4 MVP;

in vec2 Position;

void main()
{	
	gl_Position = MVP * vec4(Position, 0.0, 1.0);
}
