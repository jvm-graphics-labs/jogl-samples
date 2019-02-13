#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

void main()
{	
	gl_Position = vec4(4.f * (gl_VertexID % 2) - 1.f, 4.f * (gl_VertexID / 2) - 1.f, 0.0, 1.0);
}
