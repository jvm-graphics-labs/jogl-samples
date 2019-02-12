#version 420 core

#define POSITION	0
#define COLOR		3
#define TEXCOORD	4
#define FRAG_COLOR	0

#define TRANSFORM0	1

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(binding = TRANSFORM0) uniform transform
{
	mat4 MVP;
} Transform;

layout(location = POSITION) in vec2 Position;

out gl_PerVertex
{
	vec4 gl_Position;
};

void main()
{	
	gl_Position = Transform.MVP * vec4(Position, 0.0, 1.0);
}
