#version 420 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;
layout(triangles, invocations = 1) in;
layout(triangle_strip, max_vertices = 4) out;

in gl_PerVertex
{
	vec4 gl_Position;
	float gl_PointSize;
	float gl_ClipDistance[];
} gl_in[];

struct Vertex
{
    vec4 color;
};

layout(location = 0) in Vertex stIn[];

in Block
{
    vec4 color;
} blIn[]; 

out gl_PerVertex 
{
    vec4 gl_Position;
    float gl_PointSize;
    float gl_ClipDistance[];
};

layout(location = 0) out Vertex stOut;

out vec4 colorGNI;

out Block
{
    vec4 color;
} blOut; 

out Block2
{
    vec4 color;
} blPou; 

void main()
{	
    for(int i = 0; i < gl_in.length(); ++i)
    {
        gl_Position = gl_in[i].gl_Position;
        colorGNI = stIn[i].color;
        stOut.color = stIn[i].color;
        blOut.color = blIn[i].color;
        blPou.color = stIn[i].color + blIn[i].color;
        EmitVertex();
    }
    EndPrimitive();
}

