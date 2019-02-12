#version 420 core

#define POSITION	0
#define TRANSFORM0	1

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform int Instance;

layout(binding = TRANSFORM0) uniform transform
{
	mat4 MVP[2];
} Transform;

out gl_PerVertex
{
	vec4 gl_Position;
};

layout(location = POSITION) in vec2 Position;

void main()
{
	gl_Position = Transform.MVP[Instance] * vec4(Position, 0.0, 1.0);
}
