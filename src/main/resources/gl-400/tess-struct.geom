#version 400 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;
layout(triangles, invocations = 1) in;
layout(triangle_strip, max_vertices = 4) out;

struct vertex
{
	vec4 Color;
};

in vertex Eval[];
out vertex Geom;

void main()
{	
	for(int i = 0; i < gl_in.length(); ++i)
	{
		gl_Position = gl_in[i].gl_Position;
		Geom.Color = Eval[i].Color;
		EmitVertex();
	}
	EndPrimitive();
}

