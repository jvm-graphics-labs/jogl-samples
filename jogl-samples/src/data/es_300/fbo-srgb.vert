#version 150 core
#extension GL_ARB_explicit_attrib_location : enable
#define POSITION	0
#define TEXCOORD	4

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform transform
{
	mat4 MVP;
} Transform;

layout(location = POSITION) in vec2 AttribPosition;
layout(location = TEXCOORD) in vec2 AttribTexcoord;

out vec2 VertTexcoord;

void main()
{	
	VertTexcoord = AttribTexcoord;
	gl_Position = Transform.MVP * vec4(AttribPosition, 0.0, 1.0);
}
