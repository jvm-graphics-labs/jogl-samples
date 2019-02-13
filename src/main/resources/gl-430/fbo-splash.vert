#version 420 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

out gl_PerVertex
{
	vec4 gl_Position;
};

void main()
{	
	gl_Position = gl_Position = vec4(4.f * (gl_VertexID % 2) - 1.f, 4.f * (gl_VertexID / 2) - 1.f, 0.0, 1.0);
}
