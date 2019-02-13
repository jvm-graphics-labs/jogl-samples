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

in block
{
	vec2 Texcoord;
} In[];

out block
{
	vec2 Texcoord;
	flat int Instance;
} Out;

uniform mat4 MVP;

void main()
{	
	for(int i = 0; i < gl_in.length(); ++i)
	{
		gl_Layer = gl_InvocationID;
		gl_ViewportIndex = gl_InvocationID;
		gl_Position = MVP * gl_in[i].gl_Position;
		Out.Instance = gl_InvocationID;
		Out.Texcoord = In[i].Texcoord;
		EmitVertex();
	}

	EndPrimitive();
}

