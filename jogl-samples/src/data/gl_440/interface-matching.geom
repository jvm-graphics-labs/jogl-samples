#version 440 core

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
layout(std430, column_major) buffer;
layout(triangles, invocations = 1) in;
layout(triangle_strip, max_vertices = 4) out;

in gl_PerVertex
{
    vec4 gl_Position;
    float gl_PointSize;
    float gl_ClipDistance[];
} gl_in[];

layout(location = 0) in Vertex st_In[][2];

/*layout(location = 0 + 1 * st_In[0].length()) in block*/
layout(location = 0 + 1 * 2) in Block
{
    vec4 color;
} bl_In[]; 

out gl_PerVertex 
{
    vec4 gl_Position;
    float gl_PointSize;
    float gl_ClipDistance[];
};

layout(location = 0) out Vertex st_Out;

out vec4 colorGNI;

layout(location = 0 + 1) out Block
{
    vec4 color;
} bl_Out; 

layout(location = 0 + 2) out Block2
{
    vec4 color;
} bl_Pou; 

void main()
{	
    for(int i = 0; i < gl_in.length(); ++i)
    {
        gl_Position = gl_in[i].gl_Position;
        colorGNI = st_In[i][0].color + st_In[i][1].color;
        st_Out.color = st_In[i][0].color + st_In[i][1].color;
        bl_Out.color = bl_In[i].color;
        bl_Pou.color = st_In[i][0].color + bl_In[i].color;
        EmitVertex();
    }
    EndPrimitive();
}
