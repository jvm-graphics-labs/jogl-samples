#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform transform
{
	mat4 MVP;
} Transform;

in vec2 Position;
in vec2 Texcoord;

out block
{
	vec4 Color;
	vec2 Texcoord;
	flat int Instance;
} Out;

out gl_PerVertex
{
	vec4 gl_Position;
};

void main()
{
	Out.Color = vec4(1.0, 0.5, 0.0, 1.0);
	Out.Texcoord = Texcoord;
	Out.Instance = 0;
	gl_Position = Transform.MVP * vec4(Position, 0.0, 1.0);
}
