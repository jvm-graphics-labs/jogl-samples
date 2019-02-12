#version 330 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform transform
{
	mat4 MVP;
} Transform;

in vec4 Position;

void main()
{	
	gl_Position = Transform.MVP * Position;
}

