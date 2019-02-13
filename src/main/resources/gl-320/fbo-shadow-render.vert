#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform transform
{
	mat4 MVP;
	mat4 DepthMVP;
	mat4 DepthBiasMVP;
} Transform;

in vec3 Position;
in vec4 Color;

out block
{
	vec4 Color;
	vec4 ShadowCoord;
} Out;

void main()
{
	gl_Position = Transform.MVP * vec4(Position, 1.0);
	Out.ShadowCoord = Transform.DepthBiasMVP * vec4(Position, 1.0);
	Out.Color = Color;
}
