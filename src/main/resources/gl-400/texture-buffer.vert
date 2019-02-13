#version 400

#define POSITION	0
#define COLOR		3
#define TEXCOORD	4

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform samplerBuffer Displacement;
uniform mat4 MVP;

layout(location = POSITION) in vec2 Position;

out block
{
	flat int Instance;
} Out;

void main()
{	
	Out.Instance = gl_InstanceID;
	gl_Position = MVP * (vec4(Position, 0.0, 0.0) + texelFetch(Displacement, gl_InstanceID));
}
