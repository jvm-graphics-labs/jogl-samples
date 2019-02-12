#version 450 core

#define POSITION	0
#define COLOR		3
#define TEXCOORD	4
#define FRAG_COLOR	0

#define TRANSFORM0	1

#define DIFFUSE		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(binding = TRANSFORM0) uniform transform
{
	mat4 MVP;
} Transform;

layout(location = POSITION) in vec3 Position;

out gl_PerVertex
{
	vec4 gl_Position;
};

void main()
{
	gl_Position = Transform.MVP * vec4(Position.xy, Position.z + float(gl_InstanceID) * 0.5 - 0.25, 1.0);
}
