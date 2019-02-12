#version 430 core

layout(std140) uniform;

struct transform
{
	mat4 P;
	mat4 MV;
	mat3 Normal;
};

uniform per_draw
{
	transform Transform;
} PerDraw;

in vec3 Position;
in vec3 Normal;
in vec4 Color;

out block
{
	vec3 Normal;
	vec3 View;
	vec3 Color;
} Out;

void main()
{
	vec4 P = PerDraw.Transform.MV * vec4(Position, 1.0);

	Out.Normal = mat3(PerDraw.Transform.MV) * Normal;
	Out.View = -P.xyz;
	Out.Color = Color.rgb;

	gl_Position = PerDraw.Transform.P * P;
}
