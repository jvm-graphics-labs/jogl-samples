#version 410 core

#define POSITION	0
#define COLOR		3
#define TEXCOORD	4
#define FRAG_COLOR	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;
layout(triangles, invocations = 5) in;
layout(triangle_strip, max_vertices = 4) out;

in gl_PerVertex
{
	vec4 gl_Position;
} gl_in[];

out gl_PerVertex 
{
	vec4 gl_Position;
};

layout(location = COLOR) in vec4 Color[];
layout(location = COLOR) out vec4 GeomColor;

uniform mat4 MVP;

void main()
{	
	for(int i = 0; i < gl_in.length(); ++i)
	{
		gl_Position = MVP * (gl_in[i].gl_Position + vec4(vec2(0.0), - 0.5 + 0.25 * float(gl_InvocationID), 0.0));
		GeomColor = (vec4(gl_InvocationID + 1) / 6.0 + Color[i]) / 2.0; 
		EmitVertex();
	}
	EndPrimitive();
}

