#version 400 core

#define ATTR_POSITION	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform mat4 MVP;

layout(location = ATTR_POSITION) in vec2 Position;

void main()
{	
	gl_Position = MVP * vec4(Position, 0.0, 1.0);
}

