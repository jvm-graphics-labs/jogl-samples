#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

out block
{
	float Angle;
	float Opacity;
} Out;

uniform transform
{
	mat4 MVP;
	mat4 MV;
} Transform;

void main()
{
	float Angle = radians(float(gl_VertexID)) * 1.0;
	Out.Angle = Angle;
	Out.Opacity = 0.042 - float(gl_InstanceID) * 0.02;

	float Scale = float(gl_InstanceID) * 0.5 + 0.25;

	vec4 Position = vec4(cos(Angle) * Scale, sin(Angle) * Scale, 0.0, 1.0);

	gl_Position = Transform.MVP * Position;
	gl_PointSize = 512.0 / -(Transform.MV * Position).z;
}
