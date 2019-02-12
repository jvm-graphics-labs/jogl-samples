#version 420 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

const int VertexCount = 3;
const vec2 Position[VertexCount] = vec2[](
	vec2(-1.0f,-1.0f),
	vec2( 3.0f,-1.0f),
	vec2(-1.0f, 3.0f));

out gl_PerVertex
{
	vec4 gl_Position;
};

void main()
{
	gl_Position = vec4(Position[gl_VertexID], 0.0, 1.0);
}
