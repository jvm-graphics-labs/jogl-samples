#version 400 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;
layout(triangles, invocations = 4) in;
layout(triangle_strip, max_vertices = 3) out;

flat out int GeomInstance;

uniform mat4 MVP;

void main()
{	
	gl_Layer = gl_InvocationID;

	for(int i = 0; i < gl_in.length(); ++i)
	{
		gl_Position = MVP * gl_in[i].gl_Position;
		GeomInstance = gl_InvocationID;
		EmitVertex();
	}

	EndPrimitive();
}

