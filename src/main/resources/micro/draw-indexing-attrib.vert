#version 420 core
#define POSITION		0
#define DRAW_ID			5
#define TRANSFORM0		1

layout(binding = TRANSFORM0) uniform transform
{
	mat4 MVP;
} Transform;

layout(location = POSITION) in vec2 Position;
layout(location = DRAW_ID) in int DrawID;

out block
{
	flat int DrawID;
} Out;

out gl_PerVertex
{
	vec4 gl_Position;
};

void main()
{
	Out.DrawID = DrawID;
	gl_Position = Transform.MVP * vec4(Position, 0.0, 1.0);
}
