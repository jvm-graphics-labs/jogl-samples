#version 420 core

#define POSITION	0
#define COLOR		3
#define FRAG_COLOR	0

layout(triangles, invocations = 1) in;
layout(triangle_strip, max_vertices = 3) out;

in gl_PerVertex
{
    vec4 gl_Position;
} gl_in[];

in Block
{
    vec4 color;
} inBlock[];

out Block
{
    layout(stream = 0) vec4 color;
} outBlock;

out gl_PerVertex
{
    vec4 gl_Position;
};

void main()
{
    for(int i = 0; i < gl_in.length(); ++i)
    {
        outBlock.color = inBlock[i].color;
        gl_Position = gl_in[i].gl_Position;
        EmitVertex();
    }
    EndPrimitive();
}

