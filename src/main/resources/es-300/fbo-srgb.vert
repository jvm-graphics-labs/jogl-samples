#version 300 es

#define POSITION	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform transform
{
	mat4 MVP;
} Transform;

layout(location = POSITION) in vec3 Position;
const vec4 Color[] = vec4[3](
	vec4(1.0f, 0.0f, 0.0f, 1.0f),
	vec4(0.0f, 1.0f, 0.0f, 1.0f),
	vec4(0.0f, 0.0f, 1.0f, 1.0f));

out block
{
	flat vec4 Color;
} Out;

void main()
{
	Out.Color = abs(vec4(Position, 1.0));
	gl_Position = Transform.MVP * vec4(Position, 1.0);
}

