#version 150 core

#ifndef GEN_ERROR // If this is not declared, the compiler should generate an error
layout(triangles) in;
#endif

precision highp float;
precision highp int;
layout(std140, column_major) uniform;
layout(triangle_strip, max_vertices = 4) out;

uniform constant
{
	vec4 Color[3];
} Constant;

in block
{
	vec4 Color;
} In[];

out block
{
	vec4 Color;
} Out;

void main()
{
	for(int i = 0; i < gl_in.length(); ++i)
	{
		gl_Position = gl_in[i].gl_Position;
		Out.Color = (In[i].Color + Constant.Color[i]) * 0.5;
		EmitVertex();
	}
	EndPrimitive();
}

