#version 420 core

#define POSITION		0
#define COLOR			3
#define TEXCOORD		4
#define INSTANCE		7
#define FRAG_COLOR		0
#define TRANSFORM0	1

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(binding = TRANSFORM0) uniform transform
{
	mat4 MVP;
} Transform;

layout(location = POSITION) in vec2 Position;
layout(location = TEXCOORD) in vec2 Texcoord;

out block
{
	vec2 Texcoord;
	float Instance;
} Out;

void main()
{
	vec2 Offset = vec2(gl_InstanceID % 5, gl_InstanceID / 5) - vec2(2, 1);
	Out.Instance = gl_InstanceID;
	Out.Texcoord = Texcoord;
	gl_Position = Transform.MVP * vec4(Position + Offset, 0.0, 1.0);
}
