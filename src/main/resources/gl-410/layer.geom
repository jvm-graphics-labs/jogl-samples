#version 410 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;
layout(triangles, invocations = 4) in;
layout(triangle_strip, max_vertices = 3) out;

in gl_PerVertex
{
    vec4 gl_Position;
} gl_in[];

out block
{
	flat int Instance;
} Out;

uniform mat4 MVP;

void main()
{	
	for(int i = 0; i < gl_in.length(); ++i)
	{
		gl_Position = MVP * gl_in[i].gl_Position;
		gl_Layer = gl_InvocationID;
		Out.Instance = gl_InvocationID;
		EmitVertex();
	}

	EndPrimitive();
}

