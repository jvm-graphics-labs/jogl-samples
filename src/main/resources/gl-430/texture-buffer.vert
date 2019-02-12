#version 430

#define POSITION		0
#define COLOR			3
#define TEXCOORD		4
#define INSTANCE		7
#define FRAG_COLOR		0

#define TRANSFORM0		1

precision highp float;
precision highp int;
layout(std140, column_major) uniform;
layout(std430, column_major) buffer;

layout(binding = 0) uniform samplerBuffer Displacement;

layout(binding = TRANSFORM0) uniform transform
{
	mat4 MVP;
} Transform;

layout(location = POSITION) in vec2 Position;

out gl_PerVertex
{
	vec4 gl_Position;
};

out block
{
	flat int Instance;
} Out;

void main()
{	
	Out.Instance = gl_InstanceID;
	gl_Position = Transform.MVP * (vec4(Position, 0.0, 0.0) + texelFetch(Displacement, gl_InstanceID));
}
