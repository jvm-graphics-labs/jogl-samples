#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform transform
{
	mat4 MVP;
} Transform;

struct vertex
{
	vec2 Position;
	vec2 Texcoord;
};

in vertex Vertex;

out block
{
	vec2 Texcoord;
} Out;

out gl_PerVertex
{
	vec4 gl_Position;
};

void main()
{	
	Out.Texcoord = Vertex.Texcoord;
	gl_Position = Transform.MVP * vec4(Vertex.Position, 0.0, 1.0);
}
