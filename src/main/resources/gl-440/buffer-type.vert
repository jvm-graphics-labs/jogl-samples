#version 440 core

#define ATTR_POSITION	0
#define ATTR_COLOR		3
#define ATTR_TEXCOORD	4
#define VERT_POSITION	0

#define TRANSFORM0		1

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(binding = TRANSFORM0) uniform transform
{
	mat4 MVP;
} Transform;

layout(location = ATTR_POSITION) in vec4 Position;

out gl_PerVertex
{
	vec4 gl_Position;
};

void main()
{
	gl_Position = Transform.MVP * Position;
}

