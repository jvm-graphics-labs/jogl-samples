#version 410 core

#define POSITION	0
#define DRAW_ID		5
#define TRANSFORM0	1
#define TRANSFORM1	2

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform transform
{
	mat4 MVP;
} Transform[2];

out gl_PerVertex
{
	vec4 gl_Position;
};

out block
{
	flat int Instance;
} Out;

layout(location = POSITION) in vec2 Position;
layout(location = DRAW_ID) in int Instance;

void main()
{
	Out.Instance = Instance;
	gl_Position = Transform[Instance].MVP * vec4(Position, 0.0, 1.0);
}
