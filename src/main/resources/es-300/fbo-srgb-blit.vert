#version 300 es

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

void main()
{
	gl_Position = vec4(4.0 * float(gl_VertexID % 2) - 1.0, 4.0 * float(gl_VertexID / 2) - 1.0, 0.0, 1.0);
}
