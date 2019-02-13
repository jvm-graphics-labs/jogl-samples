#version 150 core

layout(std140) uniform;

uniform transform
{
	mat4 MVP;
} Transform;

in vec3 Position;
in vec4 Color;

out block
{
	flat int Index;
	vec4 Color;
} Out;

void main()
{
	gl_Position = Transform.MVP * vec4(Position, 1.0);
	Out.Color = Color;
	Out.Index = gl_VertexID / 4;
}

