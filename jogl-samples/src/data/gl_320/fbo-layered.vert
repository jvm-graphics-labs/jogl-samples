#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

in vec2 Position;

void main()
{	
	gl_Position = vec4(Position, 0.0, 1.0);
}

