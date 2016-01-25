#version 430 core

#define POSITION		0
#define COLOR			3
#define FRAG_COLOR		0

struct Vertex
{
    vec4 color;
};

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

layout(location = 0) in Vertex stIn[][2];

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
        colorGNI = stIn[i][0].color + stIn[i][1].color;
        stOut.color = stIn[i][0].color + stIn[i][1].color;
        blOut.color = blIn[i].color;
        blPou.color = stIn[i][0].color + blIn[i].color;
        EmitVertex();
    }
    EndPrimitive();
}

