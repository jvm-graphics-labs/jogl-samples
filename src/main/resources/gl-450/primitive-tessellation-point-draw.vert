#version 450 core

#define POSITION		0
#define COLOR			3
#define FRAG_COLOR		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(location = POSITION) in vec2 Position;
layout(location = COLOR) in vec4 Color;

out gl_PerVertex
{
	vec4 gl_Position;
	float gl_PointSize;
	float gl_ClipDistance[];
};

struct vertex
{
	vec4 Color;
};

layout(location = 0) out vertex Out;

void main()
{
	gl_Position = vec4(Position, 0.0, 1.0);
	Out.Color = Color;
}
