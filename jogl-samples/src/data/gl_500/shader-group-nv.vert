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

layout(location = POSITION) in vec2 Position;
layout(location = TEXCOORD) in vec2 Texcoord;

out gl_PerVertex
{
	vec4 gl_Position;
};

out block
{
	vec2 Texcoord;
} Out;

void main()
{
	Out.Texcoord = Texcoord;
	gl_Position = Transform.MVP * vec4(Position, (float(gl_InstanceID) * 1.0 - 2.0) * 0.5, 1.0);
	//gl_Position = Transform.MVP * vec4(Position, 0.0, 1.0);
}
