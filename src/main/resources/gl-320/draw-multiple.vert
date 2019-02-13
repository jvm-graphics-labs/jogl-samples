#version 150 core

layout(std140) uniform;

uniform transform
{
	mat4 MVP;
} Transform;

in vec4 Position;

void main()
{
	gl_Position = Transform.MVP * Position;
}

