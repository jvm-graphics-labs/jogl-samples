#version 400 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

const int VertexCount = 3;
const vec2 Position[VertexCount] = vec2[](
	vec2(-1.0,-1.0),
	vec2( 3.0,-1.0),
	vec2(-1.0, 3.0));

void main()
{	
	gl_Position = vec4(Position[gl_VertexID], 0.0, 1.0);
}
