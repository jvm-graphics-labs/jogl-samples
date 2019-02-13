#version 460 core

#define POSITION		0
#define TEXCOORD		4

#define TRANSFORM0		1
#define INDIRECTION		3

#define MAX_DRAW		3

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(binding = INDIRECTION) uniform indirection
{
	int Transform[MAX_DRAW];
} Indirection;

layout(binding = TRANSFORM0) uniform transform
{
	mat4 MVP[MAX_DRAW];
} Transform;

layout(location = POSITION) in vec2 Position;
layout(location = TEXCOORD) in vec2 Texcoord;

out gl_PerVertex
{
	vec4 gl_Position;
};

out block
{
	vec2 Texcoord;
	flat int DrawID;
} Out;

void main()
{
	Out.DrawID = gl_DrawID;
	Out.Texcoord = Texcoord.st;
	gl_Position = Transform.MVP[Indirection.Transform[gl_DrawID]] * vec4(Position, 0.0, 1.0);
}
